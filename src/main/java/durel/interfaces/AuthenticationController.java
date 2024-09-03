package durel.interfaces;

import durel.dto.requests.security.AuthenticationRequest;
import durel.dto.responses.security.AuthenticationResponse;
import durel.services.user.UserDetailsService;
import durel.utils.JwtService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Controller class for handling authentication related operations. Authentication is only used for the computational
 * annotators. Therefore, these endpoints are only accessible for users with role CANNOTATOR.
 */
@Controller
@Slf4j
public class AuthenticationController {

    private final AuthenticationManager authenticationManager;

    private final UserDetailsService userDetailsService;

    private final JwtService jwtTokenUtil;

    @Autowired
    public AuthenticationController(AuthenticationManager authenticationManager, UserDetailsService userDetailsService, JwtService jwtTokenUtil){
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    /**
     * Creates an authentication token for the user based on the provided credentials.
     * This method is used to authenticate the user and generate a JWT token for further authentication.
     *
     * @param authenticationRequest The authentication request object containing the user's credentials.
     * @return ResponseEntity The response entity containing the authentication response with the JWT token.
     * If the authentication is successful, it returns a ResponseEntity with HTTP status 200 (OK) and the JWT token.
     * If the authentication fails (bad credentials), it returns a ResponseEntity with HTTP status 400 (Bad Request) and no token.
     */
    @PostMapping(value = "/authenticate")
    public ResponseEntity<AuthenticationResponse> createAuthenticationToken(@RequestBody AuthenticationRequest authenticationRequest){
        try{
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    authenticationRequest.getUsername(), authenticationRequest.getPassword()));
            final UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationRequest.getUsername());
            String jwt = jwtTokenUtil.generateToken(userDetails);
            return ResponseEntity.ok(new AuthenticationResponse(jwt));
        } catch (BadCredentialsException e){
            log.warn("Bad credentials: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}