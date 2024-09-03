package durel.services.dataManagement.fileTypeSpecifications;

import durel.domain.model.Lemma;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * The abstract class DefaultFileType represents a default file type for data import and export.
 *
 * @param <T> The type of object to be read from or written to a file
 */
public abstract class DefaultFileType<T,U> {
        
    String DELIMITER = "\t";

    String NEW_LINE = "\n";

    protected final Logger logger;
    protected final String fileName;
    protected final int columnNumber;
    protected final String[] contentColumns;
    protected final String[] systemColumns;

    protected DefaultFileType(Logger logger, String fileName, int columnNumber, String[] contentColumns, String[] systemColumns) {
        this.logger = logger;
        this.fileName = fileName;
        this.columnNumber = columnNumber;
        this.contentColumns = contentColumns;
        this.systemColumns = systemColumns;
    }

    // METHODS --------------------------------------------------------------------------------------

    protected int getColumnNumber() {
        return columnNumber;
    }

    protected String getFileName() {
        return fileName;
    }

    protected String[] getColumns() {
        return contentColumns;
    }

    protected String[] getSystemColumns() {
        return systemColumns;
    }

    /**
     * Retrieves the content header for a file.
     * The method combines the content columns using a delimiter and adds a new line at the end.
     *
     * @return The content header as a string.
     */
    protected String getContentHeader() {
        return String.join(DELIMITER, contentColumns) + NEW_LINE;
    }

    /**
     * Retrieves the full header for a file.
     * The method combines the content columns and system columns using a delimiter, and adds a new line at the end.
     *
     * @return the full header as a string
     */
    protected String getFullHeader() {
        return String.join(DELIMITER, contentColumns) + DELIMITER + String.join(DELIMITER, systemColumns) + NEW_LINE;
    }


    /**
     * Converts the given object and word into a file string representation.
     * The method retrieves the content data using the getDataContent method and combines it into a single line using the createLine method.
     * If any data is missing or if the word is null, an empty string is returned.
     *
     * @param t    The object for which the content data will be retrieved.
     * @param lemma The word to include in the content data.
     * @return A string representing the object and word data in a file format.
     */
    private String dataToString(T t, Lemma lemma) {
        try {
            String[] content = getDataContent(t, lemma);
            return createLine(content);
        } catch (NullPointerException e) {
            logger.error("Unexpected error while creating file string from data. Incomplete data or null word.");
            return "";
        }
    }

    /**
     * Combines the elements of the input array into a single line string using a delimiter and adds a new line character at the end.
     *
     * @param content The array of strings to be combined into a single line.
     * @return The combined string with a new line character at the end.
     */
    protected String createLine(String [] content) {
        return String.join(DELIMITER, content) + NEW_LINE;
    }

    /**
     * Converts a set of objects to a list of file strings.
     *
     * @param tList The set of objects to convert.
     * @param lemma  The word to include in the content data.
     * @return A list of strings representing the object and word data in a file format.
     */
    protected List<String> dataSetToStringList(Set<T> tList, Lemma lemma) {
        List<String> fileStringList = new ArrayList<>();
        for (T t : tList) {
            String fileString = dataToString(t, lemma);
            fileStringList.add(fileString);
        }
        return fileStringList;
    }

    /**
     * Retrieves the content data of an object and a word and returns it as an array of strings.
     *
     * @param t    The object for which the content data will be retrieved.
     * @param lemma The word to include in the content data.
     * @return An array of strings containing the content data.
     * @throws NullPointerException If the object or word is null.
     */
    protected String[] getDataContent(T t, Lemma lemma) throws NullPointerException {
        validateData(t, lemma);
        return null;
    }
    
    /**
     * Validates the data of an object and a word.
     *
     * @param t    The object for which the data will be validated.
     * @param lemma The word to include in the validation.
     * @throws NullPointerException If the object or word is null.
     */
    abstract void validateData(T t, Lemma lemma) throws NullPointerException;

    abstract U stringToData(List<String> line);
}
