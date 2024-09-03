package durel.utils;

import org.springframework.util.ResourceUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * It is responsible for fetching, converting, and creating a zip file with all the data concerning a project.
 */
public class FileManager {

    public FileManager() {
        System.setProperty("sun.jnu.encoding", "UTF-8");
    }

    /**
     * Creates a directory in the given path.
     * @param pathString directory path.
     */
    public static void createDirectory(String pathString) {
        try {
            Path path = Paths.get(pathString);
            Files.createDirectories(path);
            System.out.println("Directory is created!");
        }
        catch (IOException e) {
            System.err.println("Failed to create directory!" + e.getMessage());
        }
    }


    /**
     * Loads files from a list of paths and returns an array of File objects.
     *
     * @param paths The list of paths to the files.
     * @return An array of File objects.
     */
    public static File[] loadFiles(List<String> paths) {
        return paths.stream()
                .map(FileManager::getFileSafely)
                .toArray(File[]::new);
    }

    private static File getFileSafely(String path) {
        try {
            return ResourceUtils.getFile("/" + path);
        } catch (FileNotFoundException e) {
            throw new UncheckedIOException(e.getMessage(), e);
        }
    }

    /**
     * Reads in File and creates List with List of Strings.
     * @return List out of Lists of Strings.
     */
    public static List<List<String>> parseFile(File file) throws IOException {
        List<List<String>> csvRaw;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            csvRaw = br.lines()
                    .map(line -> line.split("\t"))
                    .map(Arrays::asList)
                    .toList();
            if (csvRaw.isEmpty() || csvRaw.size() == 1) {
                throw new IOException("The file is emtpy.");
            }
        } catch (IOException e) {
            throw new IOException("Could not parse the file " + file.getName() + ". " + e.getMessage(), e);
        }
        return csvRaw;
    }

    public static List<String> temporarilyStoreFiles(MultipartFile[] files) throws IOException {
        List<String> paths = new ArrayList<>();
        for (MultipartFile file : files) {
            File folder = new File(System.getProperty("java.io.tmpdir"));
            String name = file.getOriginalFilename();
            if (name==null) break;
            File newFile = File.createTempFile(name, ".csv", folder);
            file.transferTo(newFile);
            paths.add(newFile.getPath());
        }
        return paths;
    }
}
