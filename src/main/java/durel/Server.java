package durel;

import durel.services.user.UserService;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.HashMap;
import java.util.Map;

/**
 * The Server class represents the main entry point for the server application.
 * It runs a Spring Boot application and handles command line arguments on startup.
 */
@SpringBootApplication(exclude = { SecurityAutoConfiguration.class })
@EntityScan(basePackages = {"durel.domain.model"})
@EnableJpaRepositories(basePackages = { "durel.domain.repository"})
@Slf4j
public class Server implements ApplicationRunner {

    private final UserService adminService;

    private ArgumentParser argumentParser;

    @Autowired
    public Server(UserService adminService) {
        this.adminService = adminService;
        initializeCommandLineArgumentParser();

    }

    /**
     * Initializes the command line argument parser for the server application.
     * The parser is configured to handle command line arguments and options specific to the application.
     */
    public void initializeCommandLineArgumentParser() {
        argumentParser = ArgumentParsers.newFor("dbutilities").build().defaultHelp(true)
                .description("durel.Server and database utilities.");
        argumentParser.addArgument("-u","--add-admin-user")
                .action(Arguments.storeTrue()) // Store Bool
                .type(Boolean.class)
                .help("Adds an admin user to the database");
    }

    /**
     * The server app starts here. It triggers the function run(), when the server is up and running.
     *
     * @param args The parameters to the starting system, such as a new tutorial or a new admin. Will be parsed later.
     */
    public static void main(final String... args) {
        SpringApplication.run(Server.class, args);
    }

    /**
     * Executes the startup options based on the command line arguments.
     *
     * @param args The command line arguments.
     */
    @Override
    public void run(final ApplicationArguments args) {
        // Retrieve and parse args in a map. Args can be used to define startup options.
        Map<String, Object> arguments = parse(args.getSourceArgs());
        if (arguments != null) {
            // Option to add new admin users.
            if ((boolean)arguments.getOrDefault("add_admin_user", false)) {
                adminService.registerAdminUser();
            }
            // Other startup options could be added here. I have tried to minimize this, though.
        }
    }

    /**
     * Parse the given arguments and return a map of parsed attributes.
     *
     * @param args The command line arguments to parse.
     * @return A map of parsed attributes.
     */
    public Map<String, Object> parse (String ... args)  {
        try {
            Namespace arguments = argumentParser.parseArgs(args);
            return arguments.getAttrs();
        } catch (ArgumentParserException e) {
            log.error("Error parsing arguments: {}", e.getMessage());
            return new HashMap<>();
        }
    }
}

