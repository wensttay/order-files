package com.github.wensttay.orderfiles.v2;

import com.drew.imaging.ImageMetadataReader;
import com.drew.lang.GeoLocation;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.GpsDirectory;
import com.github.wensttay.orderfiles.v2.model.FileData;
import com.github.wensttay.orderfiles.v2.model.FileGeoLocation;

import java.io.File;
import java.util.Date;

public class MetadataExtractor {

    public static FileData fromFile(
            final File file
    ) {
        Date dateTaken = new Date(file.lastModified());

        try {
            final Metadata metadata = ImageMetadataReader.readMetadata(file);

            final ExifSubIFDDirectory directory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
            final GpsDirectory gpsDirectory = metadata.getFirstDirectoryOfType(GpsDirectory.class);

            if (directory != null && directory.getDateOriginal() != null) {
                dateTaken = directory.getDateOriginal();
            }

            if (gpsDirectory != null) {
                final GeoLocation geoLocation = gpsDirectory.getGeoLocation();
                if (geoLocation != null) {
                    return new FileData(file, dateTaken,
                            new FileGeoLocation(geoLocation.getLatitude(), geoLocation.getLongitude()));
                }
            }

            return new FileData(file, dateTaken, null);
        } catch (Exception e) {
            return new FileData(file, dateTaken, null);
        }
    }
}
