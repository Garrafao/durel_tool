package durel.services.statistics;

import durel.domain.model.Lemma;
import durel.services.WordService;
import durel.services.dataManagement.download.DownloadProcessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Service
public class WUGsService {

    private final WordService wordService;

    private final DownloadProcessService downloadProcessService;

    @Autowired
    public WUGsService(WordService wordService, DownloadProcessService downloadProcessService) {
        this.wordService = wordService;
        this.downloadProcessService = downloadProcessService;
    }

    public List<String> executeWUGsPipeline(String algorithm, String position, String threshold, String project, String word) {
        System.out.println("The algorithm is: " + algorithm);
        System.out.println("The position is: " + position);
        System.out.println("The threshold is: " + threshold);

        // Fetch word from database.
        Lemma w = wordService.getLemmaObjectByProjectNameAndLemma(project, word);
        // Dump word into file, so that it can be read by the python routine that
        // generates the graph.
        String path = downloadProcessService.prepareFilesForVisualization(w);

        // Get the graph in HTML format
        CommandLineRunner commandLineRunner = new CommandLineRunner();
        return commandLineRunner.runVisualizationRoutine(path, word, algorithm, threshold, position);
    }

    public String createJSScript(String jsonPath) {
        return String.format("<script type=\"text/javascript\">%s</script>", CommandLineRunner.readLineByLineJava8(jsonPath + "stats.js")) + System.lineSeparator() +
                String.format("<script type=\"text/javascript\">%s</script>", CommandLineRunner.readLineByLineJava8(jsonPath + "stats_agreement.js")) + System.lineSeparator() +
                String.format("<script type=\"text/javascript\">%s</script>", CommandLineRunner.readLineByLineJava8(jsonPath + "stats_groupings.js")) + System.lineSeparator() +
                String.format("<script type=\"text/javascript\">%s</script>", CommandLineRunner.readLineByLineJava8(jsonPath + "stats_plotting.js")) + System.lineSeparator() +
                String.format("<script type=\"text/javascript\">%s</script>", CommandLineRunner.readLineByLineJava8(jsonPath + "data_joint.js"));
    }

    /**
     * It allows to run programs in the command-line.
     */
    public static class CommandLineRunner {

        public List<String> runVisualizationRoutine(String path, String word, String algorithm, String threshold, String position) {
            ProcessBuilder processBuilder = new ProcessBuilder("scripts/run_system2.sh", path, algorithm, threshold, position, "scripts/parameters_system2.sh") ;
            processBuilder.directory(new File("WUGs"));

            try {
                Process process = processBuilder.start();

                Reader rdr = new InputStreamReader(process.getErrorStream());
                StringBuilder sb = new StringBuilder();
                for(int i; (i = rdr.read()) !=-1;) {
                    sb.append((char)i);
                }
                String var = sb.toString();

                System.out.println(var);
                int ret = process.waitFor();
                System.out.printf("Program exited with code: %d", ret);

            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }


            System.out.println("Finished");

            String htmlPath = path+ "plots/interactive/full/colorful/weight/full/" + word;
            // String jsonPath = path+ "plots/interactive/full/colorful/weight/";
            List<String> jsonPathAndHtmlPage = new ArrayList<>();
            // jsonPathAndHtmlPage.add(htmlPath);
            // jsonPathAndHtmlPage.add(jsonPath);
            jsonPathAndHtmlPage.add(readLineByLineJava8(htmlPath + "_edges.txt"));
            jsonPathAndHtmlPage.add(readLineByLineJava8(htmlPath + "_nodes.txt"));
            //jsonPathAndHtmlPage.add(path);
            return jsonPathAndHtmlPage;
        }

        public static String readLineByLineJava8(String filePath)
        {
            StringBuilder contentBuilder = new StringBuilder();

            try (Stream<String> stream = Files.lines( Paths.get(filePath), StandardCharsets.UTF_8))
            {
                stream.forEach(s -> contentBuilder.append(s).append("\n"));
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

            return contentBuilder.toString();
        }

    }
}
