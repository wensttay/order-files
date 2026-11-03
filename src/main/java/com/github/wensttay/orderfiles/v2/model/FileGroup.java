package com.github.wensttay.orderfiles.v2.model;

import java.util.List;
import java.util.Objects;

public class FileGroup {

    private final FileData directory;
    private final List<FileData> fileDataList;

    public FileGroup(
            FileData directory,
            List<FileData> fileDataList
    ) {
        this.directory = directory;
        this.fileDataList = fileDataList;
    }

    public FileData getDirectory() {
        return directory;
    }

    public List<FileData> getFileDataList() {
        return fileDataList;
    }

    public boolean equals(FileData o) {
        return Objects.equals(this.directory, o);
    }

    public void add(
            final FileData fileData
    ) {
        fileDataList.add(fileData);
    }

    private String getTargetFileName(
            final FileData fileDataToCopy
    ) {
        int count = 2;

        while (true) {
            String nameFileToCopy = fileDataToCopy.getFile().getName();
            String fileNameWithOutFormat = "";
            String fileFormat = "";

            int pos = nameFileToCopy.lastIndexOf(".");

            if (pos > 0) {
                fileNameWithOutFormat = nameFileToCopy.substring(0, pos);
                fileFormat = nameFileToCopy.substring(pos);
            }

            final String targetFileName = fileNameWithOutFormat + " (" + count + ")" + fileFormat;

            boolean noneMatch = fileDataList.stream()
                    .noneMatch(fileData ->
                            fileData.getFile().getName().equals(targetFileName));

            if (noneMatch) {
                System.out.println("New targetFileName: " + targetFileName);
                return targetFileName;
            } else {
                count++;
            }
        }
    }

}
