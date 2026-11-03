package com.github.wensttay.orderfiles.v2;

import com.github.wensttay.orderfiles.v2.model.FileData;
import com.github.wensttay.orderfiles.v2.model.FileGroup;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.StructuredTaskScope.Subtask;
import java.util.stream.Collectors;

public class OrderFilesService {

    private final SortParams sortParams;
    private List<FileData> delayedFileDatas = new ArrayList<>();

    public static final Set<String> IMAGE_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "gif", "bmp", "webp", "heic", "heif", "tiff"
    );

    public static final Set<String> VIDEO_EXTENSIONS = Set.of(
            "mp4", "mov", "m4v", "avi", "mkv", "wmv", "flv", "3gp"
    );

    public OrderFilesService(
            final SortParams sortParams
    ) {
        validParams(sortParams);
        this.sortParams = sortParams;
    }

    public void orderMedia() {
        final List<File> fileList = retrieveAllFiles(sortParams.getOriginFile());
        final List<FileData> mFileList = extractMetadataFiles(fileList);
        orderMedia(mFileList);
    }

    private void orderMedia(
            final List<FileData> mFileList
    ) {
        final List<FileGroup> fileGroupList = mapToGroups(mFileList);
        createFileGroups(fileGroupList);
    }

    private void createFileGroups(
            final List<FileGroup> fileGroupList
    ) {
        fileGroupList.forEach((fileGroup) ->
                createDirectoryAndCopyFiles(fileGroup.getDirectory(), fileGroup.getFileDataList()));
    }

    private void createDirectoryAndCopyFiles(
            final FileData directory,
            final List<FileData> fileDataList
    ) {
        try (var scope = StructuredTaskScope.open(StructuredTaskScope.Joiner.allSuccessfulOrThrow())) {
            fileDataList.forEach(fileToCopy ->
                    scope.fork(() -> copyToDirectory(directory.getFile(), fileToCopy))
            );

            try {
                scope.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        processDelayedFiles();
    }

    private void processDelayedFiles() {
        if (delayedFileDatas != null && !delayedFileDatas.isEmpty()) {
            final ArrayList<FileData> delayed = new ArrayList<>();
            delayed.addAll(delayedFileDatas);
            delayedFileDatas = new ArrayList<>();
            orderMedia(delayed);
        }
    }

    private void copyToDirectory(
            final File directory,
            final FileData fileDataToCopy
    ) {
        try {
            final Path directoryPath = directory.toPath();

            if (Files.notExists(directoryPath)) {
                Files.createDirectories(directoryPath);
            }

            final File fileToCopy = fileDataToCopy.getFile();
            final String fileTargetName = fileDataToCopy.getFile().getName();

            File checkNewFile = new File(directory.getPath() + File.separator + fileTargetName);

            if (isDuplicated(fileToCopy, checkNewFile)) {
                System.out.println("Skyping duplicated file: " + fileToCopy.getPath());
                return;
            } else if (isExists(checkNewFile)) {
                checkNewFile = getNewDuplicateFile(directory, fileTargetName);
            }

            Files.copy(fileToCopy.toPath(), checkNewFile.toPath());

            String fileToCopyPath = fileToCopy.getPath();
            int smartNameBreak = fileToCopyPath.lastIndexOf(File.separator) / 2;
            String from = fileToCopyPath.substring(0, 3) + "..." + fileToCopyPath.substring(smartNameBreak, fileToCopyPath.length());

            String checkNewFilePath = checkNewFile.getPath();
            smartNameBreak = checkNewFilePath.lastIndexOf(File.separator) / 2;
            String to = checkNewFilePath.substring(0, 3) + "..." + checkNewFilePath.substring(smartNameBreak, checkNewFilePath.length());

            System.out.println("Copy From: " + from + " >>> To: " + to);

        } catch (FileSystemException ex) {
            if (Files.exists(Path.of(ex.getFile()))) {
                System.out.println("Dublicated, will be delayed: " + ex.getFile());
                delayedFileDatas.add(fileDataToCopy);
            } else {
                throw new RuntimeException(ex);
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private boolean isDuplicated(File oldFile, File newFile) {
        if (oldFile == null || newFile == null) {
            return false;
        }

        if (!oldFile.exists() || !newFile.exists()) {
            return false;
        }

        if (oldFile.equals(newFile)) {
            return true;
        }

        final String oldFileName = oldFile.getName();
        final String newFileName = newFile.getName();

        if (!oldFileName.equals(newFileName)) {
            return false;
        }

        if (oldFile.length() != newFile.length()) {
            return false;
        }

        try {
            final String oldFileType = Files.probeContentType(oldFile.toPath());
            final String newFileType = Files.probeContentType(newFile.toPath());

            if (oldFileType != null && !oldFileType.equals(newFileType)) {
                return false;
            }

            if (Files.mismatch(oldFile.toPath(), newFile.toPath()) == -1) {
                return true;
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        return false;
    }

    private static boolean isExists(File checkNewFile) {
        return checkNewFile.exists();
    }

    private File getNewDuplicateFile(File dir, String nameFileToCopy) {
        int count = 2;

        while (true) {
            String fileNameWithOutFormat = "";
            String fileFormat = "";

            int pos = nameFileToCopy.lastIndexOf(".");

            if (pos > 0) {
                fileNameWithOutFormat = nameFileToCopy.substring(0, pos);
                fileFormat = nameFileToCopy.substring(pos);
            } else {
                fileNameWithOutFormat = nameFileToCopy;
            }

            File newDuplicateFile = new File(dir.getPath() + File.separator
                    + fileNameWithOutFormat + " (" + count + ")" + fileFormat);

            if (!newDuplicateFile.exists()) {
                return newDuplicateFile;
            } else {
                count++;
            }
        }
    }

//    private void createSymbolicLink(File directory, FileData fileToCopy) {
//        final Path filePath = fileToCopy.getFile().toPath();
//        Path symbolicLinkPath = getSymbolicLinkPath(directory, fileToCopy);
//        try {
//            Files.createSymbolicLink(filePath, symbolicLinkPath);
//            System.out.println("Arquivo copiado para: " + symbolicLinkPath);
//        } catch (FileAlreadyExistsException e) {
//            System.out.println(symbolicLinkPath);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    private Path getSymbolicLinkPath(
//            final File directory,
//            final FileData fileToCopy
//    ) {
//        int count = 0;
//        Path symbolicLinkPath = Path.of(directory.toPath().toString(), fileToCopy.getFile().getName());
//        while (Files.exists(symbolicLinkPath)) {
//            symbolicLinkPath = Path.of(directory.toPath().toString(), fileToCopy.getFile().getName() + " (" + count++ + ")");
//        }
//        return symbolicLinkPath;
//    }

    private List<FileGroup> mapToGroups(
            final List<FileData> mFileList
    ) {
        final List<FileGroup> result = new ArrayList<>();

        mFileList.forEach(fileData -> {
            final Optional<FileGroup> group = result.stream()
                    .filter(fileGroup -> fileGroup.equals(fileData))
                    .findFirst();

            if (group.isPresent()) {
                group.get().add(fileData);
            } else {
                result.add(fileData.toFileGroup(sortParams.getTargetFile()));
            }
        });


        return result;
    }

    private static List<FileData> extractMetadataFiles(
            final List<File> fileList
    ) {
        try (var scope = StructuredTaskScope.open(StructuredTaskScope.Joiner.allSuccessfulOrThrow())) {
            final List<Subtask<FileData>> tasks = fileList.stream()
                    .map(file -> scope.fork(() -> MetadataExtractor.fromFile(file)))
                    .toList();

            scope.join();

            return tasks.stream()
                    .map(OrderFilesService::resultFromTask)
                    .toList();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Metadata extraction interrupted", e);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to read directory metadata", e);
        }
    }

    private static FileData resultFromTask(final Subtask<FileData> task) {
        try {
            return task.get();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to read directory metadata", e);
        }
    }

    private List<File> retrieveAllFiles(
        final File directory
    ) {
        final File[] files = directory.listFiles();

        if (files == null) {
            return Collections.emptyList();
        }

        final List<File> fileList = Arrays.stream(files)
                .filter(File::isFile)
                .filter(this::isImageOrVideo)
                .collect(Collectors.toList());

        final List<File> subDiretoryList = Arrays.stream(files).filter(File::isDirectory).toList();

        if (!subDiretoryList.isEmpty()) {
            for (File subDirectory : subDiretoryList) {
                fileList.addAll(retrieveAllFiles(subDirectory));
            }
        }

        return fileList;
    }

    private boolean isImageOrVideo(final File file) {
        try {
            final String mime = Files.probeContentType(file.toPath());
            if (mime != null) {
                return mime.startsWith("image/") || mime.startsWith("video/");
            }
        } catch (IOException ignored) {
        }

        final String ext = extracExt(file);

        return IMAGE_EXTENSIONS.contains(ext) || VIDEO_EXTENSIONS.contains(ext);
    }

    private static String extracExt(File file) {
        final String name = file.getName().toLowerCase(Locale.ROOT);
        final int pos = name.lastIndexOf('.');
        final String ext = pos > 0 ? name.substring(pos + 1) : "";
        return ext;
    }

    /**
     * Ensure the provided set contains all known image and video directory extensions.
     * Any missing extension will be added to the provided set.
     * Note: the provided set must be mutable (e.g., HashSet); otherwise an UnsupportedOperationException will be thrown.
     */
    public static void ensureContainsAllImageAndVideoFormats(final Set<String> set) {
        if (set == null) {
            return;
        }

        // add missing image extensions
        for (String ext : IMAGE_EXTENSIONS) {
            if (!set.contains(ext)) {
                set.add(ext);
            }
        }

        // add missing video extensions
        for (String ext : VIDEO_EXTENSIONS) {
            if (!set.contains(ext)) {
                set.add(ext);
            }
        }
    }

    private void validParams(
        final SortParams sortParams
    ) {
        final File originFile = sortParams.getOriginFile();
        final File targetFile = sortParams.getTargetFile();

        if (!originFile.exists()) {
            throw new IllegalArgumentException("Origin directory do not exist");
        }

        if (!originFile.isDirectory()) {
            throw new IllegalArgumentException("Origin directory is not a directory");
        }

        if (targetFile.exists() && !targetFile.isDirectory()) {
            throw new IllegalArgumentException("Target directory is not a directory");
        }
    }

}
