package durel.services.user;

import durel.domain.model.User;
import durel.domain.repository.UserDAO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Service class that implements the Spring Security UserDetailsService interface.
 * This class is responsible for retrieving user details from the database based on the username.
 * UserDetails are used in the authentication / security filter chain.
 */
@Service
@Slf4j
public class UserDetailsService implements org.springframework.security.core.userdetails.UserDetailsService {

    private static final String ROLE_USER = "ROLE_USER";
    private static final String ROLE_ADMIN = "ROLE_ADMIN";
    private static final String ROLE_CANNOTATOR = "ROLE_CANNOTATOR";

    private final UserDAO userDAO;

    public UserDetailsService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    /**
     * Retrieves user details from the database based on the username.
     *
     * @param username The username of the user.
     * @return The UserDetails object containing the user's details.
     * @throws UsernameNotFoundException If the user is not found by the given username.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userDAO.findByUsername(username);
        if (user != null) {
            String uname = user.getUsername();
            String pass = user.getPassword();
            User.Role role = user.getUserRole();

            List<SimpleGrantedAuthority> authList = getAuthorities(role);

            return new org.springframework.security.core.userdetails.User(uname, pass, authList);
        }
        throw new UsernameNotFoundException("User not found with username: " + username);
    }

    /**
     * Sets rights for every user role.
     * @return list of authorized roles
     */
    private List<SimpleGrantedAuthority> getAuthorities(User.Role role) {
        List<SimpleGrantedAuthority> authList = new ArrayList<>();
        authList.add(new SimpleGrantedAuthority(ROLE_USER));

        //you can also add different roles here
        //for example, the user is also an admin of the site, then you can add ROLE_ADMIN
        //so that he can view pages that are ROLE_ADMIN specific
        if (role.equals(User.Role.ADMIN)) {
            authList.add(new SimpleGrantedAuthority(ROLE_ADMIN));
        }
        if (role.equals(User.Role.CANNOTATOR)) {
            authList.add(new SimpleGrantedAuthority(ROLE_CANNOTATOR));
        }
        return authList;
    }
}
