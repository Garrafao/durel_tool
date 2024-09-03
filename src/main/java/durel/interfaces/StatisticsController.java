package durel.interfaces;

import durel.dto.responses.statistics.AnnotationCounts;
import durel.services.ProjectService;
import durel.services.dataManagement.download.DownloadProcessService;
import durel.services.statistics.AgreementStatisticsService;
import durel.services.statistics.DatabaseCountsStatisticsService;
import durel.services.statistics.WUGsService;
import durel.services.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/statistics")
public class StatisticsController {

    private static final Logger logger = LoggerFactory.getLogger(StatisticsController.class);

    private final UserService userService;

    private final ProjectService projectService;

    private final DatabaseCountsStatisticsService databaseCountsStatisticsService;

    private final AgreementStatisticsService agreementStatisticsService;

    private final WUGsService WUGsService;

    private final DownloadProcessService downloadProcessService;

    @Autowired
    public StatisticsController(UserService userService, ProjectService projectService, DatabaseCountsStatisticsService databaseCountsStatisticsService, AgreementStatisticsService agreementStatisticsService, durel.services.statistics.WUGsService wuGsService, DownloadProcessService downloadProcessService) {
        this.userService = userService;
        this.projectService = projectService;
        this.databaseCountsStatisticsService = databaseCountsStatisticsService;
        this.agreementStatisticsService = agreementStatisticsService;
        this.WUGsService = wuGsService;
        this.downloadProcessService = downloadProcessService;
    }

    /**
     * Triggers the Graph visualization
     */
    @RequestMapping(value = "/WUG")
    public String getWUG(final @RequestParam(value = "project-select") String project,
                         final @RequestParam(value = "word-select") String word,
                         final @RequestParam(value = "algorithm") String algorithm,
                         final @RequestParam(value = "position") String position,
                         final @RequestParam(value = "threshold", required = false, defaultValue = "") String threshold,
                         final Model model) {
        List<String> jsonPathAndHtmlPage = WUGsService.executeWUGsPipeline(algorithm, position, threshold, project, word);
        //String scriptToLoad = WUGsService.createJSScript(jsonPathAndHtmlPage.get(0));
        //String html = jsonPathAndHtmlPage.get(1);
        //model.addAttribute("graphPath", jsonPathAndHtmlPage.get(0));
        // Return the html document.
        String nodes = jsonPathAndHtmlPage.get(1);
        String edges = jsonPathAndHtmlPage.get(0);
        System.out.println(nodes);
        System.out.println(edges);
        model.addAttribute("nodes", nodes);
        model.addAttribute("edges", edges);
        model.addAttribute("users", userService.getAllUsernames());
        return "graphs/new_template";
    }

    @GetMapping(value = "/WUGdownload", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Resource> downloadFiles(@RequestParam String project,
                                                  @RequestParam String word, @RequestParam String algorithm,
                                                  @RequestParam String position, @RequestParam String threshold) {

        List<String> jsonPathAndHtmlPage = WUGsService.executeWUGsPipeline(algorithm, position, threshold, project, word);

        // Set up headers of the response so that the browser can download the content.
        HttpHeaders header = new HttpHeaders();
        header.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=WUG_" + project + ".zip");
        header.add("Cache-Control", "no-cache, no-store, must-revalidate");
        header.add("Pragma", "no-cache");
        header.add("Expires", "0");

        String zipPath = downloadProcessService.downloadWUGs(jsonPathAndHtmlPage.get(2));
        // Set up the file to be transferred.
        File file = new File(zipPath);
        Path path = Paths.get(file.getAbsolutePath());
        //file.delete();
        ByteArrayResource resource = null;
        try {
            resource = new ByteArrayResource(Files.readAllBytes(path));
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }

        // Send the file to be downloader by the user.
        return ResponseEntity.ok()
                .headers(header)
                .contentLength(file.length())
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .body(resource);

    }

    @GetMapping("/agreementTable")
    @ResponseBody
    public double[][] getAgreementStatistics(final @RequestParam(value = "project-select") String projectSelect,
                                               final @RequestParam(value = "word-select") String wordSelect,
                                               final @RequestParam(value = "metric-select") String metricSelect,
                                               final @RequestParam String annotators, final Principal principal) {
        if (projectService.userIsOwnerOrAdmin(projectSelect, principal)) {
            return agreementStatisticsService.getAllAgreementStatistics(projectSelect, wordSelect, annotators, metricSelect);
        }
        return null;
    }

    @GetMapping("/database")
    @ResponseBody
    public List<AnnotationCounts> getDatabaseStatistics(@RequestParam String projectName, @RequestParam String annotators, final Principal principal) {
        if (projectService.userIsOwnerOrAdmin(projectName, principal)) {
            return databaseCountsStatisticsService.getAllAnnotationCounts(projectName, annotators);
        }
        return null;
    }
}
