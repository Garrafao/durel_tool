package durel.interfaces;

import durel.services.LanguageService;
import durel.services.user.UserService;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class ControllerAdvisor {

    private final LanguageService languageService;

    public ControllerAdvisor(LanguageService languageService, UserService userService) {
        this.languageService = languageService;
    }

    @ModelAttribute
    public void locales(Model model) {
        model.addAttribute("locales", languageService.getLocales());
    }

    @ModelAttribute
    public void currentLocale(Model model) {
        model.addAttribute("locale", languageService.getCurrentLocale());
    }
}
