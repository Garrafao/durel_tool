package durel.services.user;

import durel.domain.model.Project;
import durel.dto.requests.registration.RegistrationRequest;
import durel.exceptions.UserAlreadyExistsException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import durel.domain.model.User;
import durel.domain.model.Tutorial;
import durel.domain.repository.UserDAO;

import javax.transaction.Transactional;
import java.io.Console;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service to manage users (Annotator objects).
 */
@Service
@Slf4j
public class UserService {

    // DAO
    private final UserDAO userDAO;

    // OTHER BEANS
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserDAO userDAO, PasswordEncoder passwordEncoder) {
        this.userDAO = userDAO;
        this.passwordEncoder = passwordEncoder;
    }

    // PROJECT ANNOTATORS ----------------------------------------------------------------------------------------------

    /**
     * Finds all active annotators of a given project, i.e., all users that have annotated the project.
     *
     * @param project The project for which to find active annotators.
     * @return A set of active annotators associated with the project.
     */
    public Set<User> findAllActiveAnnotatorsOfProject(Project project) {
        return userDAO.findDistinctByAnnotationSequences_Lemma_ProjectOrderByUsernameAsc(project);
    }

    // CONVERTING USERNAMES AND USERS ----------------------------------------------------------------------------------

    /**
     * Returns the Annotator object associated with the given username. Usernames are the unique IDs of Annotators.
     *
     * @param username the username of the Annotator to retrieve.
     * @return the Annotator object associated with the given username.
     * @throws UsernameNotFoundException if no Annotator is found with the specified username.
     */
    public User getUserByUsername(String username) throws UsernameNotFoundException {
        User annotator = userDAO.findByUsername(username);
        if(annotator == null) {
            throw new UsernameNotFoundException("User not found by name: " + username);
        }
        return annotator;
    }

    public List<String> getAllUsernames() {
        List<User> allUsers = userDAO.findAll();
        return listOfUsersToListOfUsernames(allUsers);
    }

    /**
     * Retrieves a list of all users except the user with the given username.
     *
     * @param username the username of the user which should be excluded from the list
     * @return a list of Annotator objects representing the other users
     */
    private List<User> getAllOtherUsers(String username) {
        return userDAO.findByUsernameNotLike(username);
    }

    /**
     * Retrieves all other usernames except the given username.
     *
     * @param username the username for which other usernames should be retrieved
     * @return a list containing all other usernames excluding the given username
     */
    public List<String> getAllOtherUsernames(String username) {
        List<User> allOtherUsers = getAllOtherUsers(username) ;
        return listOfUsersToListOfUsernames(allOtherUsers);
    }

    /**
     * Converts a string of usernames separated by comma into a list of users.
     *
     * @param usernames the string of usernames to convert like "username1, username2"
     * @return a list of Annotators representing the usernames
     */
    public List<User> stringOfUsernamesToListOfUsers(String usernames) {
        String[] annotatorsList = usernames.split(",");
        return listOfUsernamesToListOfUsers(List.of(annotatorsList));
    }

    /**
     * Converts a list of usernames to a list of corresponding users.
     *
     * @param usernames the list of usernames to convert
     * @return a list of users corresponding to the provided usernames
     */
    private List<User> listOfUsernamesToListOfUsers(List<String> usernames) {
        return usernames.stream().map(this::getUserByUsername).toList();
    }

    /**
     * Converts a list of usernames to a set of corresponding users.
     *
     * @param usernames the list of usernames to convert
     * @return a set of users corresponding to the provided usernames
     */
    public Set<User> listOfUsernamesToSetOfUsers(List<String> usernames) {
        return new HashSet<>(listOfUsernamesToListOfUsers(usernames));
    }

    /**
     * Converts a list of users to a list of their usernames.
     *
     * @param users the list of users to be converted
     * @return the list of usernames
     */
    private List<String> listOfUsersToListOfUsernames(List<User> users) {
        return users.stream().map(User::getUsername).collect(Collectors.toList());
    }

    /**
     * Converts a set of users to a list of their usernames.
     *
     * @param users the set of users to be converted
     * @return the list of usernames
     */
    public List<String> setOfUsersToListOfUsernames(Set<User> users) {
        return listOfUsersToListOfUsernames(new ArrayList<>(users));
    }

    // USER MANAGEMENT ------------------------------------------------------------------------------------------------

    /**
     * Registers a user based on the provided registration request. Cannot be used to register admin users.
     *
     * @param registrationRequest The registration request containing user details.
     *
     * @return true if the user is successfully registered, false otherwise.
     */
    public boolean registerUser(final RegistrationRequest registrationRequest) {
        boolean userRegistered;
        if (!registrationRequest.getRole().equals("admin")) {
            try {
                addUser(registrationRequest);
                userRegistered = true;
            } catch (UserAlreadyExistsException e) {
                userRegistered = false;
            }
        } else {
            userRegistered = false;
        }
        return userRegistered;
    }

    /**
     * Checks if a user with the given username exists.
     */
    public boolean checkIfUserExists(String username) {
        return userDAO.existsByUsername(username);
    }

    /**
     * Add a new user to the application.
     * Throws an exception if the user already exists.
     *
     * @param user The registration request containing the user information.
     * @throws UserAlreadyExistsException If a user with the same username already exists.
     */
    private void addUser(RegistrationRequest user) throws UserAlreadyExistsException {
        if(checkIfUserExists(user.getUsername())) {
            throw new UserAlreadyExistsException("User already exists for this username: " + user.getUsername());
        }
        User annotator = new User();
        annotator.setUsername(user.getUsername());
        annotator.setEmail(user.getEmail());
        annotator.setPassword(passwordEncoder.encode(user.getPassword()));
        annotator.setUserRole(mapRoleStringToRole(user.getRole()));
        userDAO.save(annotator);
    }

    /**
     * Maps a given role string to the corresponding Annotator.Role enum value.
     *
     * @param roleString the string representing the role
     * @return the Annotator.Role enum value mapped from the roleString
     */
    private User.Role mapRoleStringToRole(String roleString) {
        if (roleString.equals("admin")) {
            log.warn("Trying to assign admin role to user.");
        } else {
            log.info("Trying to assign role {}", roleString);
        }
        return switch (roleString) {
            case "admin" -> User.Role.ADMIN;
            case "annotator" -> User.Role.ANNOTATOR;
            case "researcher" -> User.Role.RESEARCHER;
            default -> User.Role.OTHER;
        };
    }

    // TUTORIAL ASSIGNMENT --------------------------------------------------------------------------------------------

    /**
     * Assigns a tutorial to a user.
     *
     * @param username the username of the user to assign the tutorial to
     * @param tutorial the tutorial to assign to the user
     * @param agreement the agreement score for the tutorial
     * @throws UsernameNotFoundException if the user with the specified username is not found
     */
    @Transactional
    public void assignTutorialToUser(String username, Tutorial tutorial, double agreement) throws UsernameNotFoundException {
        User annotator = getUserByUsername(username);
        annotator.setTutorial(tutorial);
        annotator.setTutorialAgreement(agreement);
        userDAO.save(annotator);
    }

    /**
     * Checks if a user has completed any tutorial.
     *
     * @param username the username of the user to check
     * @return true if the user has completed any tutorial, false otherwise
     */
    public boolean checkUserTutorial(String username) {
        try {
            return getUserByUsername(username).getTutorial() != null;
        } catch (UsernameNotFoundException e) {
            return false;
        }
    }

    // ADMIN USER FUNCTIONALITIES -------------------------------------------------------------------------------------

    /**
     * <b>This method requires command line access and should only be called on startup in the Server class.</b>
     * Adds a new admin user to the database.
     * <p>
     * This method prompts the user to enter a username, email, and password for the new admin user.
     * It creates a RegistrationRequest object with the entered data and adds the user to the database using the addUser method.
     * If the user already exists, it logs a message indicating that the user already exists.
     */
    public void registerAdminUser() {

        // Prompt the user safely for a password.
        Console console = System.console();
        if (console == null) {
            log.error("Couldn't get Console instance");
            return;
        }
        String username = new String(console.readPassword("Enter a username: "));
        char[] email = console.readPassword("Enter an email: ");
        char[] passwordArray = console.readPassword("Enter secret password for user '" + username + "': ");

        RegistrationRequest registrationRequest = new RegistrationRequest(username, new String(email),
                new String(passwordArray), new String(passwordArray), "admin", true, true);

        try {
            addUser(registrationRequest);
            log.info("Added a new ADMIN user to the database: {}", username);
        } catch (UserAlreadyExistsException e) {
            log.info("User already exists for this username: {}", username);
        }
    }

    /**
     * Determines whether the user with the provided username is an admin.
     *
     * @param username the username of the user to check
     * @return true if the user is an admin, false otherwise
     */
    public boolean isAdmin(String username) {
        return getUserByUsername(username).getUserRole().equals(User.Role.ADMIN) ;
    }
}
