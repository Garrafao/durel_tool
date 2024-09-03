package durel.interfaces;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
/**
 * The StaticPagesController is responsible for rendering the appropriate views for static pages in the application.
 * All pages can be accessed before login.
 * <p>
 * The class contains methods for handling requests to:
 * - the home page ("/")
 * - the annotationGuidelines page ("/annotationGuidelines")
 * - the login page ("/login")
 * - the about page ("/about")
 * - the privacy policy page ("/privacyPolicy")
 */
@Controller
@Slf4j
public class StaticPagesController {

    /**
     * Returns the view name for the home page ("/").
     *
     * @return the view name for the home page
     */
    @GetMapping("/")
    public String showHome() {
        return "pages/home";
    }

    /**
     * Retrieves the view name for the annotation guidelines page ("/annotationGuidelines").
     * The annotation guidelines page provides guidelines on how to use DURel annotations.
     *
     * @return the view name for the annotation guidelines page
     */
    @GetMapping("/annotationGuidelines")
    public String showAnnotationGuidelines() {
        return "pages/annotationGuidelines";
    }

    /**
     * Retrieves the view name for the login page ("/login").
     *
     * @return the view name for the login page
     */
    @GetMapping("/login")
    public String showLogin() {
        return "pages/login";
    }

    /**
     * Retrieves the view name for the about page ("/about").
     * The about page provides information about the application.
     *
     * @return the view name for the about page
     */
    @GetMapping("/about")
    public String showAbout() {
        return "pages/about";
    }

    /**
     * Retrieves the view name for the privacy policy page ("/privacyPolicy").
     * The privacy policy page provides information concerning the DSGVO.
     *
     * @return the view name for the privacy policy page
     */
    @GetMapping("/privacyPolicy")
    public String showPrivacyPolicy() {
        return "pages/privacyPolicy";
    }
}
