package durel.services.dataManagement.download;

import durel.domain.model.Lemma;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * DefaultFileDownload is an interface that provides methods for downloading files.
 */
public interface DefaultFileDownload<T> {

    Logger logger = LoggerFactory.getLogger(DefaultFileDownload.class);

    String getFileName();
    String getHeader();
    Set<T> getDataForDownload(Lemma lemma);
    List<String> dataToListOfStrings(Lemma lemma);

    /**
     * Writes a Word object to a file.
     *
     * @param path the path of the file exclusion filename
     * @param lemma the Word to be written
     */
    default void writeFile(String path, Lemma lemma) {
        String filePath = path + getFileName();
        try (FileWriter fileWriter = createAndOpenFile(filePath)) {
            fileWriter.write(getHeader());
            List<String> lines = dataToListOfStrings(lemma);
            addLinesToFile(lines, fileWriter);
        } catch (IOException e) {
            logger.error("Error occurred while writing to the file: {}", filePath, e);
        }
    }

    /**
     * Adds lines to a file using a FileWriter.
     * For each line in the provided list, the data is written to the file using the given FileWriter.
     * If an IOException occurs while writing a line, an error message is logged.
     *
     * @param lines       the lines to be added to the file
     * @param fileWriter the FileWriter used to write data to the file
     */
    default void addLinesToFile(List<String> lines, FileWriter fileWriter) {
        for (String line : lines) {
            try {
                writeData(line, fileWriter);
            } catch (IOException e) {
                logger.error("Error when writing line to file: {}.", line);
            }
        }
    }

    /**
     * Creates a file at the specified path and opens it for writing.
     *
     * @param path the path of the file to be created and opened
     * @return a FileWriter object for the opened file
     * @throws IOException if an I/O error occurs while creating or opening the file
     */
    static FileWriter createAndOpenFile(String path) throws IOException {
        File file = new File(path);
        boolean created = file.createNewFile();
        if (!created) {
            throw new IOException("File at path " + path + " was not created");
        }
        return new FileWriter(path);
    }

    default void writeData(String line, FileWriter fileWriter) throws IOException {
        fileWriter.write(line);
    }
}
