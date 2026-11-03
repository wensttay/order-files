package com.github.wensttay.orderfiles.v2.model;

import com.github.wensttay.orderfiles.v1.enums.DefaultFolderStructures;

import java.io.File;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class FileData {
    private final File file;
    private final Date dateTaken;
    private final FileGeoLocation fileGeoLocation;

    public FileData(File file, Date dateTaken, FileGeoLocation fileGeoLocation) {
        this.fileGeoLocation = fileGeoLocation;
        this.dateTaken = dateTaken;
        this.file = file;
    }

    public File getFile() {
        return file;
    }

    public Date getDateTaken() {
        return dateTaken;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof FileData other)) {
            return false;
        }

        return isOnSameCalendarDay(other) && isNear(other);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dateTaken, fileGeoLocation);
    }


    public FileGroup toFileGroup(
            final File rootDirectory
    ) {
        if (dateTaken == null) {
            return toFileGroup(unknowPath(rootDirectory));
        }

        final Optional<String> geoLocation = getGeoLocationDirectoryName();
        String path = rootDirectory.getAbsolutePath() + File.separator + getDateDirectoryName();

        if (geoLocation.isPresent()) {
            path += File.separator + geoLocation.get();
        }

        return toFileGroup(path);
    }

    private FileGroup toFileGroup(
            final String filePath
    ) {
        final List<FileData> list = new ArrayList<>();
        list.add(this);
        return new FileGroup(
                new FileData(new File(filePath), this.dateTaken, this.fileGeoLocation), list
        );
    }

    private String unknowPath(File rootDirectory) {
        return rootDirectory.getAbsolutePath() + File.separator + "UNKNOW";
    }

    public String getDateDirectoryName() {
        return DefaultFolderStructures.SIMPLE.getSimpleDateFormat().format(dateTaken);
    }

    private Optional<String> getGeoLocationDirectoryName() {
        if (fileGeoLocation != null) {
            return Optional.of(fileGeoLocation.latitude() + "_" + fileGeoLocation.longitude());
        }

        return Optional.empty();
    }

    private boolean isOnSameCalendarDay(
            final FileData second
    ) {
        if (this.dateTaken == null || second.dateTaken == null) {
            return this == second;
        }

        final LocalDate firstDate = this.dateTaken.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        final LocalDate secondDate = second.dateTaken.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        return firstDate.equals(secondDate);
    }

    private boolean isNear(
            final FileData second
    ) {
        if (this.fileGeoLocation == null && second.fileGeoLocation == null) {
            return true;
        }

        if (this.fileGeoLocation == null || second.fileGeoLocation == null) {
            return false;
        }

        return this.fileGeoLocation.isNear(second.fileGeoLocation);
    }

}
