package durel.interfaces;

import durel.dto.requests.registration.RegistrationRequest;
import durel.dto.requests.registration.RegistrationRequestValidator;
import durel.services.user.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;

@Controller
@Slf4j
public class RegistrationController {

    private final UserService userService;

    private final RegistrationRequestValidator registrationRequestValidator;

    @Autowired
    public RegistrationController(UserService userService, RegistrationRequestValidator registrationRequestValidator) {
        this.userService = userService;
        this.registrationRequestValidator = registrationRequestValidator;
    }

    @InitBinder("registrationForm")
    public void initBinder(WebDataBinder binder) {
        binder.addValidators(registrationRequestValidator);
    }

    /**
     * Retrieves the registration page.
     *
     * @param model the model object to hold data to be rendered in the view
     * @return the view name for the registration page
     */
    @GetMapping("/register")
    public String showRegistration(Model model){
        model.addAttribute("registrationForm", new RegistrationRequest());
        return "pages/register";
    }

    @PostMapping("/register")
    public String userRegistration(final @Valid @ModelAttribute("registrationForm") RegistrationRequest registrationRequest, final BindingResult bindingResult) {
        if (!bindingResult.hasErrors()) {
            boolean registrationSuccessful = userService.registerUser(registrationRequest);
            if (registrationSuccessful) {
                return "redirect:/tutorial";
            }
        }
        return "pages/register";
    }
}
