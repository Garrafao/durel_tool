package durel.services.dataManagement.upload;

import durel.services.dataManagement.uploadData.UseData;
import durel.domain.model.Project;
import durel.domain.model.Lemma;
import durel.services.dataManagement.fileTypeSpecifications.UseFileType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * This class is responsible for loading and parsing UseData from files.
 */
@Service
public class UseFileUploadService extends UseFileType implements DefaultFileUpload<UseData> {

    /**
     * @see Logger
     * @see LoggerFactory
     */
    private static final Logger logger = LoggerFactory.getLogger(UseFileUploadService.class);

    private static final Pattern pattern = Pattern.compile("^([0-9]+):([0-9]+)$");

    @Qualifier("lightTaskExecutor")
    ThreadPoolTaskExecutor taskExecutor;

    @Autowired
    public UseFileUploadService(ThreadPoolTaskExecutor taskExecutor) {
        this.taskExecutor = taskExecutor;
    }

    @Override
    public int getColumnNumber() {
        return super.getColumnNumber();
    }

    @Override
    public String[] getColumns() {
        return super.getColumns();
    }

    @Override
    public UseData getData(List<String> dataLine) {
        return super.stringToData(dataLine);
    }

    @Override
    public ThreadPoolTaskExecutor getExecutor() {
        return taskExecutor;
    }
    
    public List<List<UseData>> parseAndCheckFilesMultithreading(List<String> paths, boolean multipleAllowed) throws IOException {
        List<List<UseData>> processedUseData = parseAndCheckFilesMultithreading(paths, new ArrayList<>(), multipleAllowed);
        Set<String> lemmas = new HashSet<>();
        for (List<UseData> word:processedUseData) {
            String lemma = word.get(0).getLemma();
            if (lemmas.contains(lemma)) {
                throw new IOException("Lemma " + lemma + " occurs in two files");
            }
            lemmas.add(lemma);
        }
        return processedUseData;
    }

    /**
     * Performs data type specific checks on the given UseData object. For UseData these checks are index checks.
     *
     * @param lineNumber the line number where the UseData object is found
     * @param data the UseData object to perform checks on
     * @param dataSet a set of UseData objects
     * @throws IOException if any data type specific checks fail
     */
    @Override
    public void doDataTypeSpecificChecks(int lineNumber, UseData data, Set<UseData> dataSet) throws IOException {
        // Checking the indexes.
        if(isIndexNotValid(data.getSentenceIndex()) || isIndexNotValid(data.getTokenIndex())) {
            throw new IOException("An index in line " + lineNumber + " could not be parsed.");
        }
        int sentenceIndexStartingPosition = parseIndex(data.getSentenceIndex(), 0);
        int sentenceIndexEndPosition = parseIndex(data.getSentenceIndex(), 1);
        int wordIndexStartingPosition = parseIndex(data.getTokenIndex(), 0);
        int wordIndexEndPosition = parseIndex(data.getTokenIndex(), 1);
        // Checking the index positions against the length of the whole context
        if(data.getContext().length() < sentenceIndexEndPosition || data.getContext().length() < wordIndexEndPosition) {
            throw new IOException("An index in line " + lineNumber + " exceeds the context length.");
        }
        // Comparing index postions against each other
        if(sentenceIndexEndPosition < sentenceIndexStartingPosition
                || wordIndexEndPosition < wordIndexStartingPosition
                || wordIndexStartingPosition < sentenceIndexStartingPosition
                || sentenceIndexEndPosition < wordIndexEndPosition ) {
            throw new IOException("An index in line " + lineNumber + " does not seem to be correct.");
        }
    }

    /**
     * Checks if the given index string is valid.
     *
     * @param index the index string to check
     * @return true if the index string is not valid, false otherwise
     */
    private boolean isIndexNotValid(String index) {
        return !pattern.matcher(index).find();
    }

    /**
     * Parses the index string and returns the specified position.
     *
     * @param index the index string to parse
     * @param pos   the position of the index value to return
     * @return the parsed index value at the specified position
     */
    private int parseIndex(String index, int pos) {
        return Integer.parseInt(index.split(":")[pos]);
    }

    /**
     * Validates identifiers by checking if the identifier already exists in the set of identifiers.
     *
     * @param lineNumber       the line number where the identifier is found
     * @param use              the UseData object containing the identifier
     * @param usesIdentifiers  the set of existing identifiers
     * @throws IOException if the identifier already exists in the set of identifiers
     */
    @Override
    public void validateIdentifiers(int lineNumber, UseData use, Set<String> usesIdentifiers) throws IOException {
        if (usesIdentifiers.contains(use.getIdentifier())) {
            throw new IOException("Identifier " + use.getIdentifier() + " already exists in the set of identifiers.");
        }
        usesIdentifiers.add(use.getIdentifier());
    }

    /**
     * Checks if the given words are already present in the project.
     *
     * @param words   A list of lists containing UseData.
     *                Each inner list represents a set of UseData objects for a single word.
     * @param project The project in which to check for word existence.
     * @throws IOException If an empty word list is found or if a word in the list already exists in the project.
     */
    public void checkWordsInProject(List<List<UseData>> words, Project project) throws IOException {
        Set<String> lemmas = new HashSet<>();
        if (project.getLemmas() != null) {
            for (Lemma lemma : project.getLemmas()) {
                lemmas.add(lemma.getLemma());
            }
        }
        for (List<UseData> word : words) {
            if (word.isEmpty()) {
                throw new IOException("Empty word list found.");
            }
            if (lemmas.contains(word.get(0).getLemma())) {
                throw new IOException("Project already contains word " + word.get(0).getLemma());
            }
        }
    }
}
