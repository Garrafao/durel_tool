package durel.interfaces;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * The UserDocumentationController is responsible for rendering the appropriate views for static documentation pages.
 */
@Controller
@Slf4j
public class UserDocumentationController {
    /**
     * This method returns the view name for the tutorial user documentation.
     *
     * @return the view name for the tutorial user documentation
     */
    @GetMapping("/docs/tutorial")
    public String showUserDocsTutorial() {
        return "user_documentation/tutorial-doc";
    }

    /**
     * Returns the view name for the user documentation about the annotation process.
     *
     * @return the view name for the user documentation about the annotation process
     */
    @GetMapping("/docs/annotation")
    public String showUserDocsAnnotation() {
        return "user_documentation/annotation-process-doc";
    }

    /**
     * This method returns the view name for the user documentation on how to upload projects.
     *
     * @return the view name for the user documentation on how to upload projects
     */
    @GetMapping("/docs/upload/file-type")
    public String showUserDocsUpload() {
        return "user_documentation/upload-doc";
    }

    /**
     * Returns the view name for the user documentation on how to use the LLM annotators.
     *
     * @return the view name for the user documentation on how to use the LLM annotators
     */
    @GetMapping("/docs/LLM-annotators")
    public String showUserDocsLLMAnnotators() {
        return "user_documentation/LLM-annotator-doc";
    }
}
