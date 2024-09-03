package durel.services.dataManagement.download;

import durel.domain.model.Project;
import durel.domain.model.Lemma;
import durel.utils.FileManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class DownloadProcessService {
    private static final int BUFFER_SIZE = 1024;
    private static final String DATAFOLDER_STRING = "data";
    private static final String TEMP_DIR = "/tmp/durel";
    private final Logger logger = LoggerFactory.getLogger(FileManager.class);
    UseFileDownloadService useFileDownloadService;
    InstanceFileDownloadService instancesFileDownloadService;
    AnnotationFileDownloadService annotationFileDownloadService;

    @Autowired
    public DownloadProcessService(UseFileDownloadService useFileDownloadService,
                                  InstanceFileDownloadService instancesFileDownloadService,
                                  AnnotationFileDownloadService annotationFileDownloadService) {
        this.useFileDownloadService = useFileDownloadService;
        this.instancesFileDownloadService = instancesFileDownloadService;
        this.annotationFileDownloadService = annotationFileDownloadService;
    }

    public String prepareFilesForVisualization(Lemma lemma) {
        String targetDir = generateDirectoryPath("visualization", lemma.getLemma(), System.currentTimeMillis());
        String dataDir = targetDir + DATAFOLDER_STRING + File.separator + lemma.getLemma() + File.separator;
        FileManager.createDirectory(dataDir);
        useFileDownloadService.writeFile(dataDir, lemma);
        annotationFileDownloadService.writeFile(dataDir, lemma);
        return targetDir;
    }

    public String downloadProject(Project project) {
        // Prepare for zip file.
        String baseDirectoryPath = generateDirectoryPath("download", project.getProjectName(), System.currentTimeMillis());
        String resourcesPath = baseDirectoryPath + DATAFOLDER_STRING + File.separator;
        FileManager.createDirectory(resourcesPath);
        Path input = Paths.get(resourcesPath);
        for (Lemma lemma : project.getLemmas()) {
            prepareWordFiles(resourcesPath, lemma);
        }
        String zipFilePath = baseDirectoryPath + project.getProjectName() + ".zip";
        Path output = Paths.get(zipFilePath);
        zipFolder(input, output);
        return zipFilePath;
    }

    private void prepareWordFiles(String resourcesPath, Lemma lemma) {
        useFileDownloadService.writeFile(resourcesPath  + lemma.getLemma() + "_", lemma);
        instancesFileDownloadService.writeFile(resourcesPath  + lemma.getLemma() + "_", lemma);
        annotationFileDownloadService.writeFile(resourcesPath  + lemma.getLemma() + "_", lemma);
    }

    public String downloadWUGs(String path) {
        Path input = Paths.get(path);
        Path output = Paths.get(path + "WUGs.zip");
        zipFolder(input, output);
        return path + "WUGs.zip";
    }

    /**
     * Zip a directory, including sub files and subdirectories
     */
    private void zipFolder(Path source, Path output){
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(output.toString()))) {
            Files.walkFileTree(source, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) {
                    if (!attributes.isSymbolicLink()) {
                        try {
                            zipFile(zos, source, file);
                            logger.info("Zip file : {}", file);
                        } catch (IOException e) {
                            logger.error(e.getMessage());
                        }
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) {
                    logger.error("Unable to zip : {}", file, exc);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    private void zipFile(ZipOutputStream zos, Path source, Path targetFile) throws IOException {
        try (FileInputStream fis = new FileInputStream(targetFile.toFile())) {
            zos.putNextEntry(createZipEntry(source, targetFile));
            byte[] buffer = new byte[BUFFER_SIZE];
            int len;
            while ((len = fis.read(buffer)) > 0) {
                zos.write(buffer, 0, len);
            }
            zos.closeEntry();
        }
    }

    private ZipEntry createZipEntry(Path source, Path targetFile) {
        Path fileToZip = source.relativize(targetFile);
        return new ZipEntry(fileToZip.toString());
    }

    private String generateDirectoryPath(String folderName, String itemName, long id) {
        return TEMP_DIR + File.separator + folderName + File.separator + itemName + id + File.separator;
    }
}
