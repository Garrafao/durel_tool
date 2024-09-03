package durel;

import durel.domain.model.User;
import durel.domain.repository.UserDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Handles successful authentication by redirecting the user to a particular page.
 */
@Component
public class LoginSuccessHandler implements AuthenticationSuccessHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoginSuccessHandler.class);

    private final UserDAO userDAO;

    @Autowired
    public LoginSuccessHandler(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication) throws IOException, ServletException {
        AuthenticationSuccessHandler.super.onAuthenticationSuccess(request, response, chain, authentication);
    }

    /**
     * Handles successful authentication by redirecting the user to a particular page.
     *
     * @param request       the HttpServletRequest object
     * @param response      the HttpServletResponse object
     * @param authentication   the Authentication object representing the authenticated user
     * @throws IOException if an I/O error occurs during the redirection
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        try {
            UserDetails principal = (UserDetails) authentication.getPrincipal();
            if (principal != null) {
                String name = principal.getUsername();
                redirectBasedOnAnnotatorTutorialStatus(name, response);
                if (!name.equals("AnnotatorServer")) LOGGER.info("Authentication was successful for user: {}", name);
            } else {
                response.sendRedirect("/tutorial");
                LOGGER.warn("Principal was null after authentication.");
            }
        } catch (IOException e) {
            LOGGER.error("An error occurred while trying to handle successful authentication", e);
            throw e;
        }
    }

    /**
     * Redirects the user based on the user's tutorial status.
     *
     * @param username  the username of the user
     * @param response  the HttpServletResponse object for sending the redirect response
     * @throws IOException if an I/O error occurs during the redirection
     */
    private void redirectBasedOnAnnotatorTutorialStatus(String username, HttpServletResponse response) throws IOException {
        User user = userDAO.findByUsername(username);
        if (user != null && user.getTutorial() != null) {
            response.sendRedirect("/annotation");
        } else {
            response.sendRedirect("/tutorial");
        }
    }
}
