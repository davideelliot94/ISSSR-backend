package com.isssr.ticketing_system.mail.mailHandler;

import com.isssr.ticketing_system.enumeration.*;
import com.isssr.ticketing_system.exception.EntityNotFoundException;
import com.isssr.ticketing_system.mail.exception.FormatNotRespectedException;
import com.isssr.ticketing_system.mail.exception.MailRejectedException;
import com.isssr.ticketing_system.entity.*;
import com.isssr.ticketing_system.controller.*;
import lombok.NoArgsConstructor;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserCache;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.Message.RecipientType;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import javax.mail.search.FlagTerm;
import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.Properties;


/**
 * Handler mail reception
 */
@NoArgsConstructor
@Service
@EnableScheduling
public class MailReceiverHandler extends MailHandler {

    private MimeBodyPart bodyPart;
    private String fileName;
    private boolean flag;
    private boolean isRunning;

    @Autowired
    private UserController userController;

    @Autowired
    private TargetController targetController;

    @Autowired
    private TicketController ticketController;

    @Autowired
    private MailSenderHandler mailSenderController;

    @Autowired
    private CompanyController companyController;

    @Autowired
    private AuthenticationManager authenticationManager;


    /**
     * Receive an email
     */
    public void receiveMail() {
        (new Thread(this)).start();
    }

    /**
     * Returns a Properties object which is configured for a IMAP server.
     *
     * @param  host an hostname of the IMAP server
     * @param  port the port of the IMAP server
     * @return      the Properties object
     */
    private Properties getServerProperties(String host, String port) {
        Properties properties = new Properties();

        // server setting
        properties.put("mail.imap.host", host);
        properties.put("mail.imap.port", port);

        // SSL setting
        properties.setProperty("mail.imap.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        properties.setProperty("mail.imap.socketFactory.fallback", "true");
        properties.setProperty("mail.imap.socketFactory.port", String.valueOf(port));
        properties.put("mail.imap.ssl.enable", "true");
        properties.put("mail.imap.starttls.enable", "true");
        properties.put("mail.imap.ssl.trust", "*");

        return properties;
    }


    /**
     * Periodic thread that waits for email.
     * When a mail is received, the this thread parses it,
     * build a new Ticket object and save it into the db
     */
    public void run() {

        // Authentication of current thread: required to write targets/tickets into the db
        Authentication token = new UsernamePasswordAuthenticationToken("admin", "password");
        Authentication auth = authenticationManager.authenticate(token);
        SecurityContextHolder.getContext().setAuthentication(auth);

        System.out.println("Reading emails...");
        this.isRunning = true;
        Properties properties = getServerProperties(receiverHost, port);
        Session session = Session.getDefaultInstance(properties);

        try {
            // connects to the message store
            Store store = session.getStore("imap");
            store.connect(receiverHost, userName, password);

            // opens the inbox folder
            Folder folderInbox = store.getFolder("INBOX");
            folderInbox.open(Folder.READ_WRITE);

            // fetches new messages from server
            Message[] messages = folderInbox.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));
            TicketSource ticketSource = null;
            TicketStatus ticketStatus = null;
            Visibility visibility = null;
            User customer;
            if (messages.length != 0) {
                ticketSource = TicketSource.MAIL;
                ticketStatus = TicketStatus.VALIDATION;
                visibility = Visibility.PRIVATE;
            }

            //Read all messages in INBOX
            for (int i = 0; i < messages.length; i++) {

                Message msg = messages[i];
                Address[] fromAddress = msg.getFrom();
                String from = fromAddress[0].toString();
                try {
                    from = from.substring(from.indexOf("<") + 1, from.indexOf(">"));
                } catch (Exception e) {
                    throw new MailRejectedException("Invalid email address found.");
                }
                //Checking sender address, looking for match in db
                if (!checkAddress(from)) {
                    if (!checkDomain(from)) {
                        msg.setFlag(Flags.Flag.SEEN, true);
                        throw new MailRejectedException("***** E-mail rejected ******");
                    } else customer = null;
                } else customer = userController.findByEmail(from.toLowerCase().trim()).get();


                /*
                if (!assignee.isPresent()){
                    msg.setFlag(Flags.Flag.SEEN, true);
                    throw new MailRejectedException("***** E-mail rejected ******");
                }*/

                String subject = msg.getSubject();
                String toList = parseAddresses(msg.getRecipients(RecipientType.TO));
                String ccList = parseAddresses(msg.getRecipients(RecipientType.CC));
                String sentDate = msg.getSentDate().toString();

                String messageContent = null;
                try {
                    messageContent = getTextFromMessage(msg);
                } catch (IOException e) {
                    msg.setFlag(Flags.Flag.SEEN, true);
                    e.printStackTrace();
                }

                // print out details of each message
                System.out.println("Message #" + (i + 1) + ":");
                System.out.println("\t From: " + from);
                System.out.println("\t To: " + toList);
                System.out.println("\t CC: " + ccList);
                System.out.println("\t Subject: " + subject);
                System.out.println("\t Sent Date: " + sentDate);
                System.out.println("\t Message: " + messageContent);

                if (isFormatted(messageContent)) {
                    System.out.println("Email message correctly formatted");
                    if (parseFormattedEmail(subject, messageContent, ticketSource, ticketStatus, visibility, customer) == null) {
                        System.out.println("Email message correctly formatted, but there is an error " +
                                "in parsing its content");
                        this.mailSenderController.sendMail(from, "FORMAT");
                        throw new MailRejectedException("***** Syntax Error *****");
                    } else this.mailSenderController.sendMail(from, "TICKET_OPENED");
                } else {
                    //Send email response
                    System.out.println("Email message not formatted");
                    this.mailSenderController.sendMail(from, "FORMAT");
                }

                //Set email as read
                msg.setFlag(Flags.Flag.SEEN, true);
            }

            // disconnect
            folderInbox.close(false);
            store.close();

        } catch (AuthenticationFailedException e) {
            System.out.println("Too many simultaneous connections.");
        } catch (NoSuchProviderException ex) {
            System.out.println("No provider for protocol: " + protocol);
            //ex.printStackTrace();
        } catch (MessagingException ex) {
            System.out.println("Could not connect to the message store");
            //ex.printStackTrace();
        } catch (MailRejectedException e) {
            System.out.println("Email rejected");
        } finally {
            this.isRunning = false;
            //Thread.currentThread().interrupt();
        }
    }

    /**
     * Check if a email address has a valid domain
     *
     * @return true if address has a valid domain, false otherwise
     */
    private boolean checkDomain(String from) {
        return this.companyController.existsByDomain(from.substring(from.indexOf("@") + 1));
    }

    /**
     * Returns a list of addresses in String format separated by comma
     *
     * @param address array of Address objects
     * @return a list of addresses in String format separated by comma
     */
    private String parseAddresses(Address[] address) {
        String listAddress = "";

        if (address != null) {
            for (Address addres : address) {
                listAddress += addres.toString() + ", ";
            }
        }
        if (listAddress.length() > 1) {
            listAddress = listAddress.substring(0, listAddress.length() - 2);
        }

        return listAddress;
    }

    /**
     * Get text from message, plain
     *
     * @param message message to convert in plain text
     * @return plain text
     * @throws MessagingException
     * @throws IOException
     */
    private String getTextFromMessage(Message message) throws MessagingException, IOException {
        String result = "";
        if (message.isMimeType("text/plain")) {
            result = message.getContent().toString();
        } else if (message.isMimeType("multipart/*")) {
            this.flag = false;
            MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
            result = getTextFromMimeMultipart(mimeMultipart);
        }
        return result;
    }


    /**
     * Get text from message, multipart
     *
     * @param mimeMultipart multipart which contain the message
     * @return message extracted from multipart
     * @throws MessagingException
     * @throws IOException
     */
    private String getTextFromMimeMultipart(MimeMultipart mimeMultipart) throws MessagingException, IOException {
        String result = "";
        int count = mimeMultipart.getCount();
        for (int i = 0; i < count; i++) {
            this.bodyPart = (MimeBodyPart) mimeMultipart.getBodyPart(i);
            if (Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition())) {
                //insertUser attachment file name and his bodypart
                this.flag = true;
                this.fileName = bodyPart.getFileName();
                bodyPart.saveFile(saveDirectory + File.separator + fileName);
            } else if (bodyPart.isMimeType("text/plain")) {
                result = result + "\n" + bodyPart.getContent();
                break; // without break same text appears twice
            } else if (bodyPart.isMimeType("text/html")) {
                String html = (String) bodyPart.getContent();
                result = result + "\n" + Jsoup.parse(html).text();
            } else if (bodyPart.getContent() instanceof MimeMultipart) {
                result = result + getTextFromMimeMultipart((MimeMultipart) bodyPart.getContent());
            }
        }
        return result;
    }

    //Switch between formatted and unformatted email
    /**
     * Check is message content is correctly formatted
     *
     * @param content the content of the email message received
     * @return true if correcly formatted, false othrewise
     */
    private boolean isFormatted(String content) {
        String[] ticketAttribute = format;

        //Avoid case sensitive match error
        content = content.toLowerCase();

        for (String aTicketAttribute : ticketAttribute) {
            if (!content.contains(aTicketAttribute.toLowerCase() + ":"))
                if (!content.contains(aTicketAttribute.toLowerCase() + " :"))
                    return false;
        }
        return true;
    }


    /**
     * Check if email address belongs to a registered user
     * @param sender the address of the sender
     * @return true if the email address belongs to a registered user, false otherwise
     *
     */
    private boolean checkAddress(String sender) {
        return userController.existsByEmail(sender);
    }


    /**
     * Parser for a formatted e-mail
     * returns a new ticket to insert in the db
     *
     * @param subject ticket subject
     * @param content ticket content
     * @param ticketSource ticket source
     * @param ticketStatus ticket status
     * @param visibility ticket visibility
     * @param assignee ticket assignee
     * @return a new ticket
     */
    private Ticket parseFormattedEmail(String subject, String content, TicketSource ticketSource, TicketStatus ticketStatus, Visibility visibility, User assignee) {
        Ticket ticket = new Ticket();
        try {
            //Get message's lines split
            content = content.trim(); // alcuni client di posta elettronica inseriscono un '\n' all'inizio del messaggio
            String[] lines = content.split("\n");

            //Get right formatted text
            String target0 = lines[0].substring(lines[0].indexOf(": ") + 1);
            String category0 = lines[1].substring(lines[1].indexOf(": ") + 1).toLowerCase();
            String priority = lines[2].substring(lines[2].indexOf(": ") + 1).toLowerCase();
            String description = lines[3].substring(lines[3].indexOf(": ") + 1).toLowerCase();
            for (int i = 4; i < lines.length; i++) {
                description += lines[i];
            }

            TicketCategory category;
            TicketPriority ticketPriority;

            try {
                category = TicketCategory.valueOf(category0.toUpperCase().trim());
                ticketPriority = TicketPriority.valueOf(priority.toUpperCase().trim());
            } catch(IllegalArgumentException e) {
                //Check existing category, throw exception otherwise
                throw new FormatNotRespectedException("Format not respected, ticket priority problem");
            }

            Target target;
            try {
                target = targetController.getByName(target0.trim());
            } catch (EntityNotFoundException e) {
                throw new FormatNotRespectedException("Format not respected, ticket target problem");
            }

            //Setting ticket's default values and retrieved ones
            ticket.setTitle(subject);
            ticket.setDescription(description);
            ticket.setCustomerPriority(ticketPriority);
            ticket.setTarget(target);
            ticket.setCategory(category);
            ticket.setCurrentTicketStatus(ticketStatus);
            ticket.setSource(ticketSource);
            ticket.setVisibility(visibility);
            ticket.setCreationTimestamp(Instant.now());
            if (assignee != null) ticket.setCustomer(assignee);
            ticket.setAssignee(assignee);

            this.ticketController.insertTicket(ticket);

            if (this.flag) {
                bodyPart.saveFile(System.getProperty("user.dir") + saveDirectory + File.separator + fileName);
                ticket.setAttachments(fileName);
                this.flag = false;
            }

        } catch (Exception e) {
            System.out.println("Email rejected, format not respected");
            e.printStackTrace();
            return null;
        }
        this.isRunning = false;
        return ticket;
    }

    @Override
    public void sendMail(String address, String mailType) {
    }

    @Override
    public void sendMail(String address, String mailType, String text) {
    }

    /**
     * Check if server is running
     *
     * @return true if server is running, false otherwise
     */
    @Override
    public boolean isServerRunning() {
        return isRunning;
    }
}
