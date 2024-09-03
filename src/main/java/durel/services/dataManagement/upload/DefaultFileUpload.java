package durel.services.dataManagement.upload;

import durel.services.dataManagement.uploadData.UploadData;
import durel.services.dataManagement.uploadData.UseData;
import durel.utils.FileManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public interface DefaultFileUpload<T extends UploadData> {
    Logger logger = LoggerFactory.getLogger(DefaultFileUpload.class);

    Map<String, String> MAPPING = Map.ofEntries(
            Map.entry("aͤ", "ä"), Map.entry("oͤ", "ö"), Map.entry("uͤ", "ü"),
            Map.entry("Aͤ", "Ä"), Map.entry("Oͤ", "Ö"), Map.entry("Uͤ", "Ü"),
            Map.entry("ſ", "s"), Map.entry("ꝛ", "r"), Map.entry("m̃", "mm"),
            Map.entry("æ", "ae"), Map.entry("Æ", "Ae"));
    int RUNNING_TIME = 2;

    // METHODS TO BE IMPLEMENTED IN THE CLASSES -------------------------------------------------------

    /*
    These methods are used in file2Data and have to be specified for different data types.
     */
    T getData(List<String> dataLine) throws IOException;

    void doDataTypeSpecificChecks(int lineNumber, T data, Set<T> dataSet) throws IOException;

    void validateIdentifiers(int lineNumber, T data, Set<String> usesIdentifiers) throws IOException;

    /*
    Get the data type specific static variables.
     */
    int getColumnNumber();
    String[] getColumns();
    ThreadPoolTaskExecutor getExecutor();

    // MULTITHREADING TASK SETUP ----------------------------------------------------------------------

    default List<List<T>> parseAndCheckFilesMultithreading(List<String> paths, List<List<UseData>> uses,
                                                           boolean multipleAllowed) throws IOException {
        File[] files = FileManager.loadFiles(paths);
        ThreadPoolTaskExecutor executor = getExecutor();

        List<List<T>> parsedData;
        try {
            List<Future<List<T>>> futures = prepareTaskList(executor, uses, files, multipleAllowed);
            parsedData = executeTasks(futures);
        } catch (InterruptedException e) {
            throw new IOException(e.getMessage());
        }
        return parsedData;
    }

    default List<Future<List<T>>> prepareTaskList(ThreadPoolTaskExecutor executor,
                                                  List<List<UseData>> uses, File[] files,
                                                  boolean multipleAllowed) throws InterruptedException {
        List<Callable<List<T>>> tasks = new ArrayList<>();
        for (int i = 0; i < files.length; i++) {
            int finalI = i;
            Callable<List<T>> task = () -> parseAndCheckFiles(uses, files, finalI, multipleAllowed);
            tasks.add(task);
        }
        return tasks.stream()
                .map(executor::submit)
                .collect(Collectors.toList());
    }

    static <T> List<List<T>> executeTasks(List<Future<List<T>>> futures) throws InterruptedException {
        List<List<T>> parsedData = new ArrayList<>();
        for (Future<List<T>> future : futures) {
            try {
                parsedData.add(future.get(RUNNING_TIME, TimeUnit.MINUTES));
            } catch (ExecutionException | TimeoutException | InterruptedException e) {
                throw new InterruptedException(e.getCause().getMessage());
            }
        }
        return parsedData;
    }

    // PARSING AND CHECKING LOGIC ---------------------------------------------------------------------

    /**
     * Parses and checks files to retrieve a list of data objects of type T.
     *
     * @param uses  the list of lists of UseData objects from which to extract the identifiers
     * @param files the array of files to parse and check
     * @param index the index of the file to process
     * @return a list of data objects of type T
     * @throws IOException if an error occurs while parsing the files or processing the data
     */
    default List<T> parseAndCheckFiles(List<List<UseData>> uses, File[] files, int index, boolean multipleAllowed) throws IOException {
        Set<String> usesIdentifiers = (uses != null && uses.size() > index) ?
                createIdentifierSet(uses.get(index)) :  // For AnnotationData and InstanceData we extract the Identifiers of the corresponding uses
                new HashSet<>();    // For UseData we are extracting the usesIdentifiers on the go, therefore we pass an empty HashSet
        List<List<String>> csvRaw = FileManager.parseFile(files[index]);
        try {
            checkHeaderColumnNamesAndOrder(csvRaw);
            List<T> data = file2Data(csvRaw, usesIdentifiers, multipleAllowed);
            files[index].deleteOnExit();
            return data;
        } catch (IOException | UsernameNotFoundException e) {
            logger.info("Error parseAndCheckFiles method for word {}/{}", index, files.length);
            String fileName = files[index].getName().split(".csv")[0];
            throw new IOException("File " + fileName + ": " + e.getMessage());
        }
    }

    // 1
    /**
     * Creates a set of identifiers from a list of UseData objects.
     *
     * @param useDataList the list of UseData objects from which to extract the identifiers
     * @return a Set of strings containing the identifiers
     */
    static Set<String> createIdentifierSet(List<UseData> useDataList) {
        return useDataList.parallelStream()
                .map(UseData::getIdentifier)
                .collect(Collectors.toSet());
    }

    // 2
    /**
     * Checks the column's names and order in a CSV raw data.
     *
     * @param csvRaw the raw CSV data containing the header
     * @throws IOException if the number of columns in the header does not match the expected number of columns defined by the data type,
     *                     or if the column names are not in the expected order
     */
    default void checkHeaderColumnNamesAndOrder(List<List<String>> csvRaw) throws IOException {
        List<String> headersList = csvRaw.get(0);
        if (headersList.size() < getColumnNumber()) {
            throw new IOException("Wrong number of columns in file. Expected: "
                    + Arrays.toString(getColumns()) + ", Found: " + headersList.size() + ".");
        }
        for (int i = 0; i < getColumnNumber(); i++) {
            if (!headersList.get(i).equals(getColumns()[i])) {
                int iPlus1 = i+1;
                throw new IOException("The file has wrong column order. Expected '"
                        + getColumns()[i] + "' at position " + iPlus1 + ", but found '" + headersList.get(i) + "'.");
            }
        }
    }

    // 3
    /**
     * Converts raw CSV data to a list of data objects of type T.
     *
     * @param dataRaw         the raw CSV data to convert
     * @param usesIdentifiers a set of identifiers to validate against
     * @return a list of data objects of type T
     * @throws IOException if an error occurs while processing the CSV data
     */
    default List<T> file2Data(List<List<String>> dataRaw, Set<String> usesIdentifiers, boolean multipleAllowed) throws IOException {
        Set<T> dataSet = new HashSet<>();
        Set<String> lemmaSet = new HashSet<>();
        // Starting at 1, so we skip the header
        for (int i = 1; i < dataRaw.size(); i++) {
            checkColumns(i, dataRaw, getColumnNumber());
            T data = handleDataExtraction(i, dataRaw);
            doDataTypeSpecificChecks(i, data, dataSet);
            validateIdentifiers(i, data, usesIdentifiers);
            dataSet.add(data);
            if (!multipleAllowed) {
                lemmaSet.add(data.getLemma());
                validateLemmas(lemmaSet);
            }
        }
        return new ArrayList<>(dataSet);
    }

    // 3a
    static void checkColumns(int lineNumber, List<List<String>> dataRaw, int columnNumber) throws IOException {
        if (dataRaw.get(lineNumber).size() < columnNumber) {
            throw new IOException("There is the wrong number of entries in line " + lineNumber + ".");
        }
    }

    // 3b
    default T handleDataExtraction(int lineNumber, List<List<String>> dataRaw) throws IOException {
        try {
            return getData(dataRaw.get(lineNumber));
        } catch (IndexOutOfBoundsException e) {
            throw new IOException("Incorrect line number " + lineNumber + ".", e);
        } catch (Exception e) {
            throw new IOException("An error occurred while processing line " + lineNumber + ".", e);
        }
    }

    // 3c-3d implemented in classes
    // 3e
    static void validateLemmas(Set<String> lemmaSet) throws IOException {
        if (lemmaSet.size() != 1) {
            throw new IOException("There are multiple lemmas.");
        }
    }

    // UTILITY FUNCTIONS -----------------------------------------------------------------------------------

    static String normalize(String str) {
        for (Map.Entry<String, String> entry : MAPPING.entrySet()) {
            str = str.replace(entry.getKey(), entry.getValue());
        }
        return str;
    }

    static String extractData(List<String> dataLine, int index) throws IllegalArgumentException {
        if (dataLine != null && index < dataLine.size()) {
            return dataLine.get(index);
        }
        throw new IllegalArgumentException("Invalid index: " + index);
    }
}
