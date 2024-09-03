package durel.services.dataManagement.upload;

import durel.services.dataManagement.uploadData.InstanceData;
import durel.services.dataManagement.fileTypeSpecifications.InstanceFileType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * This class implements the DefaultFileLoaderService interface to load and process instance data from a file.
 */
@Service
public class InstancesFileUploadService extends InstanceFileType implements DefaultFileUpload<InstanceData> {

    @Qualifier("lightTaskExecutor")
    ThreadPoolTaskExecutor taskExecutor;

    @Autowired
    public InstancesFileUploadService(ThreadPoolTaskExecutor taskExecutor) {
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
    public InstanceData getData(List<String> dataLine) {
        return super.stringToData(dataLine);
    }

    @Override
    public ThreadPoolTaskExecutor getExecutor() {
        return taskExecutor;
    }


    @Override
    public void doDataTypeSpecificChecks(int lineNumber, InstanceData data, Set<InstanceData> dataSet) throws IOException {
        try {
            return;
        } catch (Exception e) {
            throw new IOException("There are currently no instance specific checks, so this shouldn't come up.");
        }
    }

    @Override
    public void validateIdentifiers(int lineNumber, InstanceData data, Set<String> usesIdentifiers) throws IOException {
        if (!usesIdentifiers.contains(data.getIdentifierOne()) || !usesIdentifiers.contains(data.getIdentifierTwo())) {
            throw new IOException("An identifier in line " + lineNumber + " could not be linked to any identifier in the uses files.");
        }
    }
}
