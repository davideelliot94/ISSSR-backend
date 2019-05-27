package com.isssr.ticketing_system.mail.mailHandler;

public interface IMailHandler {

    /**
     * Specify receiver's address and the mail type
     *
     * @param address the address to which the mail is sent
     * @param mailType the type of mail
     */
    public void sendMail(String address, String mailType);

    /**
     * Make a custom e-mail with a certain text
     *
     * @param address the address to which the mail is sent
     * @param mailType the type of mail
     * @param text the content of the mail
     */
    public void sendMail(String address, String mailType, String text);

    /**
     * Make imap server to scan INBOX folder for new email
     *
     */
    public void receiveMail();

    /**
     * Check if server is running
     *
     * @return true if server is running, false otherwise
     */
    public boolean isServerRunning();
}
