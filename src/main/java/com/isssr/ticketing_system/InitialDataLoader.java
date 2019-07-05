package com.isssr.ticketing_system;


import com.isssr.ticketing_system.acl.Authority;
import com.isssr.ticketing_system.acl.AuthorityName;
import com.isssr.ticketing_system.acl.groups.Group;
import com.isssr.ticketing_system.dao.SprintDao;
import com.isssr.ticketing_system.entity.auto_generated.enumeration.COperatorsEnum;
import com.isssr.ticketing_system.enumeration.*;
import com.isssr.ticketing_system.exception.EntityNotFoundException;
import com.isssr.ticketing_system.exception.UpdateException;
import com.isssr.ticketing_system.mail.MailService;
import com.isssr.ticketing_system.mail.model.Mail;
import com.isssr.ticketing_system.entity.*;
import com.isssr.ticketing_system.entity.auto_generated.query.DBScheduledCountQuery;
import com.isssr.ticketing_system.entity.auto_generated.query.Query;
import com.isssr.ticketing_system.entity.auto_generated.query.QueryType;
import com.isssr.ticketing_system.entity.db_connection.DBConnectionInfo;
import com.isssr.ticketing_system.controller.*;
import com.isssr.ticketing_system.controller.auto_generated.QueryService;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.time.Instant;
import java.util.List;

@Component
public class InitialDataLoader implements ApplicationListener<ContextRefreshedEvent> {

    @Value("${wrong.query.mail.type}")
    private String wrongQueryMailType;

    @Value("${root.mode.username}")
    private String ROOT_USERNAME;

    @Value("${root.mode.password}")
    private String ROOT_PASSWORD;

    @Value("${admin.email}")
    private String ADMIN_EMAIL;

    @Autowired
    private AuthorityController authorityController;

    @Autowired
    private UserController userController;

    @Autowired
    private TicketRelationTypeController ticketRelationTypeController;

    @Autowired
    private TeamController teamController;

    @Autowired
    private TicketController ticketController;

    @Autowired
    private TargetController targetController;

    @Autowired
    private GroupController groupController;

    @Autowired
    private MailService mailService;

    @Autowired
    private CompanyController companyController;

    @Autowired
    private SetupController setupController;

    @Autowired
    private QueryService queryService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private boolean firstSchedulingAlreadyDone = false;

    @Override
    @Transactional
    public void onApplicationEvent(ContextRefreshedEvent event) {
        //Check if db is already setup, Otherwise set up it


        if (!this.checkConfig()) {
            //Create DB and users
            this.createDB();
            this.createReadOnlyUser();


            /*
            //Create start table
            this.configureAuthority();
            this.configureCompany();
            this.configurePrivileges();
            this.configureRoles();
            this.configureVisibilities();
            this.configureUsers();
            this.configurePriorities();
            this.configureCategories();
            this.configureSources();
            this.configureStatuses();
            this.configureDifficulties();
            this.configureRelationTypes();
            this.configureTargets();
            this.configureTeams();
            this.configureEmail();
            */
            //this.configureGroups();


            //Only for demo
            //this.demoInit();

            //Make db setup
            this.setAlreadySetup(true);
        }
        //this.configureGroups();

        if (this.firstSchedulingAlreadyDone) return;

        this.startScheduling();

        this.firstSchedulingAlreadyDone = true;

    }

    private void createDB() {
        try {
            Connection con = DriverManager.getConnection("jdbc:postgres://localhost:3306/", ROOT_USERNAME, ROOT_PASSWORD);
            Statement statement = con.createStatement();
            statement.execute("CREATE DATABASE ticketing_system_db\n" +
                    "    WITH \n" +
                    "    OWNER = " + ROOT_USERNAME + "\n" +
                    "    ENCODING = 'UTF8'\n" +
                    "    CONNECTION LIMIT = -1;");
            statement.close();
        } catch (SQLException e) {
            System.out.println("DB has been created");
        }
    }

    private void setAlreadySetup(boolean b) {
        this.setupController.save(new Setup(true));
    }

    private boolean checkConfig() {
        return this.setupController.existsBySetup(true);
    }

    private void configureCompany() {
        if (!this.companyController.existsByName("System")) {
            //this.companyController.insertUser(new Company("System", false, "test.it"));
            this.companyController.save(new Company("System", "test.it"));
        }
    }
/*
    private void configurePrivileges() {
        this.privilegeController.insertUser(new Privilege("READ_PRIVILEGE"));
        this.privilegeController.insertUser(new Privilege("WRITE_PRIVILEGE"));

        this.privilegeController.insertUser(new Privilege("READ_ALL_PRIVILEGE"));
        this.privilegeController.insertUser(new Privilege("WRITE_ALL_PRIVILEGE"));
    }
*/
    private void configureEmail() {
        if (!this.mailService.existsByType("FORMAT"))
            this.mailService.save(new Mail("Format error", "Format not respected. In attachment you can find rules for opening tickets by e-mail.\n" +
                    "You can also contact our help desk for more assistance.", "FORMAT"));
        if (!this.mailService.existsByType("TICKET_OPENED"))
            this.mailService.save(new Mail("Ticket opened", "Your ticket has been successfully created", "TICKET_OPENED"));
        if (!this.mailService.existsByType(this.wrongQueryMailType))
            this.mailService.save(new Mail("Wrong query", "A query with wrong behaviour has been executed. It has been disabled. \n\nCause:", this.wrongQueryMailType));
    }
/*
    private void configureRoles() {
        // External
        this.roleController.insertUser(new Role("ROLE_CUSTOMER", new ArrayList<>(Arrays.asList(privilegeController.findByName("READ_PRIVILEGE").get(), privilegeController.findByName("WRITE_PRIVILEGE").get()))));
        this.roleController.insertUser(new Role("ROLE_HELP_DESK_OPERATOR", new ArrayList<>(Arrays.asList(privilegeController.findByName("READ_ALL_PRIVILEGE").get(), privilegeController.findByName("WRITE_ALL_PRIVILEGE").get()))));

        // Internal
        this.roleController.insertUser(new Role("ROLE_TEAM_COORDINATOR", new ArrayList<>(Arrays.asList(privilegeController.findByName("READ_ALL_PRIVILEGE").get(), privilegeController.findByName("WRITE_PRIVILEGE").get()))));
        this.roleController.insertUser(new Role("ROLE_TEAM_LEADER", new ArrayList<>(Arrays.asList(privilegeController.findByName("READ_ALL_PRIVILEGE").get(), privilegeController.findByName("WRITE_PRIVILEGE").get()))));
        this.roleController.insertUser(new Role("ROLE_TEAM_MEMBER", new ArrayList<>(Arrays.asList(privilegeController.findByName("READ_ALL_PRIVILEGE").get(), privilegeController.findByName("WRITE_PRIVILEGE").get()))));
        this.roleController.insertUser(new Role("ROLE_ADMIN", new ArrayList<>(Arrays.asList(privilegeController.findByName("READ_ALL_PRIVILEGE").get(), privilegeController.findByName("WRITE_ALL_PRIVILEGE").get()))));
    }

    private void configureVisibilities() {
        this.visibilityController.insertUser(new Visibility("PRIVATE"));
        this.visibilityController.insertUser(new Visibility("PUBLIC"));
    }
*/
    private void configureUsers() {
        User u = new User("Admin", "Admin", ADMIN_EMAIL, "admin", "password", this.companyController.findByName("System").get(), UserRole.ADMIN);
        this.userController.insertUser(u);

        Authority a = new Authority(AuthorityName.ROLE_ADMIN);
        this.authorityController.save(a);

        Group g = new Group("GRUPPO_ADMIN");
        g.addMember(u);
        g.addAuthority(a);
        this.groupController.saveGroup(g);


    }
/*
    private void configurePriorities() {
        this.ticketPriorityController.insertUser(new TicketPriority("LOW"));
        this.ticketPriorityController.insertUser(new TicketPriority("MEDIUM"));
        this.ticketPriorityController.insertUser(new TicketPriority("HIGH"));
    }

    private void configureCategories() {
        this.ticketCategoryController.insertUser(new TicketCategory("BUG"));
        this.ticketCategoryController.insertUser(new TicketCategory("ERROR"));
        this.ticketCategoryController.insertUser(new TicketCategory("FAILURE"));
        this.ticketCategoryController.insertUser(new TicketCategory("SUPPORT"));
        this.ticketCategoryController.insertUser(new TicketCategory("HUMAN ERROR"));
        this.ticketCategoryController.insertUser(new TicketCategory("GENERAL"));
    }

    private void configureSources() {
        this.ticketSourceController.insertUser(new TicketSource("SYSTEM"));
        this.ticketSourceController.insertUser(new TicketSource("HELP_DESK"));
        this.ticketSourceController.insertUser(new TicketSource("CLIENT"));
        this.ticketSourceController.insertUser(new TicketSource("MAIL"));
    }

    private void configureStatuses() {
        this.ticketStatusController.insertUser(new TicketStatus("PENDING"));
        this.ticketStatusController.insertUser(new TicketStatus("INITIALIZED"));
        this.ticketStatusController.insertUser(new TicketStatus("WORK_IN_PROGRESS"));
        this.ticketStatusController.insertUser(new TicketStatus("FINISHED"));
    }

    private void configureDifficulties() {
        this.ticketDifficultyController.insertUser(new TicketDifficulty("LOW"));
        this.ticketDifficultyController.insertUser(new TicketDifficulty("MEDIUM"));
        this.ticketDifficultyController.insertUser(new TicketDifficulty("HIGH"));
    }
*/
    private void configureRelationTypes() {
        this.ticketRelationTypeController.save(new TicketRelationType("LINKED"));
        this.ticketRelationTypeController.save(new TicketRelationType("DEPENDENT"));
        this.ticketRelationTypeController.save(new TicketRelationType("SIMILAR"));
    }

    private void configureTargets() {
        this.targetController.insertTarget(new Target("System", "1.0"));
    }

    private void configureTeams() {
        Team systemTeam = this.teamController.insertTeam(new Team("System team", userController.findByEmail(ADMIN_EMAIL).get()));
        if (!this.teamController.existsByName(systemTeam.getName())) this.teamController.insertTeam(systemTeam);
    }

    private void configureAuthority() {
        this.authorityController.save(new Authority(AuthorityName.ROLE_TEAM_MEMBER));
        this.authorityController.save(new Authority(AuthorityName.ROLE_GROUP_READER));
        this.authorityController.save(new Authority(AuthorityName.ROLE_SOFTWARE_PRODUCT_READER));
        this.authorityController.save(new Authority(AuthorityName.ROLE_TEAM_READER));
        this.authorityController.save(new Authority(AuthorityName.ROLE_ADMIN));
        this.authorityController.save(new Authority(AuthorityName.ROLE_GROUP_COORDINATOR));
        this.authorityController.save(new Authority(AuthorityName.ROLE_SOFTWARE_PRODUCT_COORDINATOR));
        this.authorityController.save(new Authority(AuthorityName.ROLE_TEAM_COORDINATOR));
        this.authorityController.save(new Authority(AuthorityName.ROLE_CUSTOMER));
        this.authorityController.save(new Authority(AuthorityName.ROLE_EDITOR));
        this.authorityController.save(new Authority(AuthorityName.ROLE_TEAM_LEADER));
        this.authorityController.save(new Authority(AuthorityName.ROLE_SCRUM));
    }

    private void configureGroups() {
        Group adminGroup = new Group("GRUPPO ADMIN");
        adminGroup.addAuthority(authorityController.getBySid(AuthorityName.ROLE_ADMIN));
        this.groupController.saveGroup(adminGroup);

        Group assistantGroup = new Group("GRUPPO ASSISTANT");
        assistantGroup.addAuthority(authorityController.getBySid(AuthorityName.ROLE_TEAM_LEADER));
        this.groupController.saveGroup(assistantGroup);

        Group customerGroup = new Group("GRUPPO CUSTOMER");
        customerGroup.addAuthority(authorityController.getBySid(AuthorityName.ROLE_CUSTOMER));
        this.groupController.saveGroup(customerGroup);

        Group scrumGroup = new Group("GRUPPO SCRUM");
        customerGroup.addAuthority(authorityController.getBySid(AuthorityName.ROLE_SCRUM));
        this.groupController.saveGroup(scrumGroup);
    }

    private void createReadOnlyUser() {
        try {
            Connection connection = this.jdbcTemplate.getDataSource().getConnection();
            Statement statement = connection.createStatement();
            statement.execute("CREATE ROLE Read_Only_User WITH LOGIN PASSWORD 'user' " +
                    "NOSUPERUSER INHERIT NOCREATEDB NOCREATEROLE NOREPLICATION VALID UNTIL 'infinity';");
            statement.execute("GRANT CONNECT ON DATABASE ticketing_system_db TO Read_Only_User;\n" +
                    "GRANT USAGE ON SCHEMA public TO Read_Only_User;\n" +
                    "GRANT SELECT ON ALL TABLES IN SCHEMA public TO Read_Only_User;\n" +
                    "GRANT SELECT ON ALL SEQUENCES IN SCHEMA public TO Read_Only_User;");
            statement.close();
        } catch (SQLException e) {
            System.out.println("User has been created");
        }
    }

    public void startScheduling() {
        List<Query> queryList = this.queryService.findAllActiveQueries();
        for (int i = 0; i < queryList.size(); i++) {
            Query query = queryList.get(i);
            try {
                this.queryService.activateQuery(query);
                this.queryService.updateOne(query.getId(), query);

            } catch (ParseException | SchedulerException | EntityNotFoundException | UpdateException e) {
                System.out.println("Error while trying to start scheduling");
                e.printStackTrace();
            }
        }
    }

    /*
        DEMO INITIALIZATION
     */

    private void demoInit() {
        this.demoGenerateCompanies();
        this.demoGenerateUsers();
        this.demoGenerateTeams();
        this.demoGenerateQueries();
        this.demoGenerateTargets();
        this.demoGenerateTickets();
    }

    private void demoGenerateCompanies() {
        //this.companyController.insertUser(new Company("ISSSR", true, "gmail.com"));
        this.companyController.save(new Company("ISSSR", "gmail.com"));
    }

    private void demoGenerateUsers() {

        this.userController.insertUser(new User("Andrea", "Silvi", "andrea.silvi94@gmail.com", "silvi", "password", this.companyController.findByName("ISSSR").get(), UserRole.CUSTOMER));
        this.userController.insertUser(new User("Alessio", "Vintari", "alessio.vintari@gmail.com", "vintari", "password", this.companyController.findByName("ISSSR").get(), UserRole.CUSTOMER));
        this.userController.insertUser(new User("Luca", "Menzolini", "luca.menzolini@gmail.com", "menzolini", "password", this.companyController.findByName("ISSSR").get(), UserRole.CUSTOMER));
        this.userController.insertUser(new User("Tiziano", "Ditoma", "tiziano.ditoma@gmail.com", "ditoma","password", this.companyController.findByName("ISSSR").get(), UserRole.CUSTOMER));
        this.userController.insertUser(new User("Simone", "Mancini", "2simonemancini5@gmail.com", "mancini", "password", this.companyController.findByName("ISSSR").get(), UserRole.CUSTOMER));
        this.userController.insertUser(new User("Francesco", "Ottaviano", "francesco.ottaviano@gmail.com", "ottaviano","password", this.companyController.findByName("ISSSR").get(), UserRole.CUSTOMER));
    }

    private void demoGenerateTeams() {
        Team systemTeam = this.teamController.insertTeam(new Team("Team 2", userController.findByEmail("andrea.silvi94@gmail.com").get()));

        systemTeam.getTeamMembers().add(this.userController.findByEmail("alessio.vintari@gmail.com").get());
        systemTeam.getTeamMembers().add(this.userController.findByEmail("luca.menzolini@gmail.com").get());
        systemTeam.getTeamMembers().add(this.userController.findByEmail("tiziano.ditoma@gmail.com").get());
        systemTeam.getTeamMembers().add(this.userController.findByEmail("2simonemancini5@gmail.com").get());
        systemTeam.getTeamMembers().add(this.userController.findByEmail("francesco.ottaviano@gmail.com").get());

        this.teamController.insertTeam(systemTeam);
    }

    private void demoGenerateTargets() {
        this.targetController.insertTarget(new Target("ISSSR", "1.0"));
    }

    private void demoGenerateQueries() {
        this.queryService.create(new DBScheduledCountQuery(
                "DBScheduledCountQuery: This query check number of targets in target table, if it is grater than 1 generate alert ticket",
                        TicketPriority.HIGH,
                false,
                false,
                true,
                this.userController.findByEmail(ADMIN_EMAIL).get().getEmail(),
                "*/5 * * * * ?",
                "SELECT count(*) FROM ts_target",
                new DBConnectionInfo(null, null, null),
                QueryType.DATA_BASE_INSTANT_CHECK,
                COperatorsEnum.GREATER,
                BigInteger.valueOf(1)));

        this.queryService.create(new DBScheduledCountQuery(
                "DBScheduledCountQuery: This query check number of teams in team table, if it is less than 1 generate alert ticket",
                TicketPriority.HIGH,
                false,
                false,
                false,
                this.userController.findByEmail(ADMIN_EMAIL).get().getEmail(),
                "*/8 * * * * ?",
                "SELECT count(*) FROM ts_team",
                new DBConnectionInfo(null, null, null),
                QueryType.DATA_BASE_INSTANT_CHECK,
                COperatorsEnum.LESS,
                BigInteger.valueOf(1)
        ));

        this.queryService.create(new DBScheduledCountQuery(
                "Rapporto ultimo minuto: sono stati rilevati 1 o più commenti che non siano sotto il post denominato 'Supporto' riguardante il sistema 'ISSSR blog demo' che riporta le parole errore, bug o crash",
                TicketPriority.HIGH,
                false,
                false,
                true,
                this.userController.findByEmail(ADMIN_EMAIL).get().getEmail(),
                "*/30 * * * * ?",
                "SELECT count(*) FROM wp_comments AS c JOIN wp_users AS u ON c.user_id = u.ID JOIN wp_posts AS p ON p.ID = c.comment_post_ID WHERE p.post_title != 'Supporto' AND c.comment_content LIKE '%errore%' OR '%bug%' OR '%crash%'",
                new DBConnectionInfo("jdbc:mysql://localhost:3306/wordpress?useSSL=false", "root", "password"),
                QueryType.DATA_BASE_TABLE_MONITOR,
                COperatorsEnum.GREATER_EQUALS,
                BigInteger.valueOf(1)
        ));

        this.queryService.create(new DBScheduledCountQuery(
                "Rapporto ultimo minuto: sono stati rilevati 1 o più commenti sotto il post denominato 'Supporto' riguardante il sistema 'ISSSR blog demo' che riporta le parole errore, bug o crash",
                TicketPriority.HIGH,
                false,
                false,
                true,
                this.userController.findByEmail(ADMIN_EMAIL).get().getEmail(),
                "*/10 * * * * ?",
                "SELECT count(*) FROM wp_comments AS c JOIN wp_posts AS p ON p.ID = c.comment_post_ID WHERE p.post_title = 'Supporto'  AND c.comment_content LIKE 'ISSSR blog demo%' AND c.comment_content LIKE '%errore%' OR '%bug%' OR '%crash%'",
                new DBConnectionInfo("jdbc:mysql://localhost:3306/wordpress?useSSL=false", "root", "password"),
                QueryType.DATA_BASE_TABLE_MONITOR,
                COperatorsEnum.GREATER_EQUALS,
                BigInteger.valueOf(1)
        ));

        this.queryService.create(new DBScheduledCountQuery(
                "Rapporto ultimi due minuti: è stata rilevato rilevato un incremento del 20% sul numero di ticket aperti con categoria bug",
                TicketPriority.HIGH,
                false,
                false,
                true,
                this.userController.findByEmail(ADMIN_EMAIL).get().getEmail(),
                "0 */2 * * * ?",
                "SELECT count(*) FROM ts_ticket AS t JOIN ts_ticketcategory AS tc ON t.category_id = tc.id WHERE tc.name = 'BUG'",
                new DBConnectionInfo(null, null, null),
                QueryType.DATA_BASE_TABLE_MONITOR,
                COperatorsEnum.PERCENT_GROWTH,
                BigInteger.valueOf(20)
        ));
    }

    private void demoGenerateTickets() {
        try {
            this.ticketController.insertTicket(new Ticket(
                    TicketStatus.VALIDATION,
                    TicketSource.CLIENT,
                    Instant.now(),
                    TicketCategory.BUG,
                    "Found a bug!",
                    "Hi,\nI'm Andrea, I would like you to inform that the system that you created is beautiful but there are some problems, you should fix all problems.\nSee you soon,\nAndrea",
                    userController.findByEmail("andrea.silvi94@gmail.com").get(),
                    targetController.getByName("ISSSR"),
                    TicketPriority.HIGH,
                    Visibility.PRIVATE
            ));
        } catch (EntityNotFoundException e) {
            e.printStackTrace();
        }

        try {
            this.ticketController.insertTicket(new Ticket(
                    TicketStatus.VALIDATION,
                    TicketSource.CLIENT,
                    Instant.now(),
                    TicketCategory.ERROR,
                    "Error found inside form",
                    "Hi,\nI'm Alessio, thanks for your amazing system, I'm experiencing some issues with your for during ticket creation.\nSee you soon,\nAlessio",
                    userController.findByEmail("alessio.vintari@gmail.com").get(),
                    targetController.getByName("ISSSR"),
                    TicketPriority.HIGH,
                    Visibility.PRIVATE
            ));
        } catch (EntityNotFoundException e) {
            e.printStackTrace();
        }

        try {
            this.ticketController.insertTicket(new Ticket(
                    TicketStatus.VALIDATION,
                    TicketSource.CLIENT,
                    Instant.now(),
                    TicketCategory.FAILURE,
                    "Failure of browser",
                    "Hi,\nI'm Luca, I would like you to inform that my browser crash instantly. Maybe the problem is my computer, I'm using Windows Vista and Internet explorer!\nSee you soon,\nLuca",
                    userController.findByEmail("luca.menzolini@gmail.com").get(),
                    targetController.getByName("ISSSR"),
                    TicketPriority.HIGH,
                    Visibility.PRIVATE
            ));
        } catch (EntityNotFoundException e) {
            e.printStackTrace();
        }
    }
}