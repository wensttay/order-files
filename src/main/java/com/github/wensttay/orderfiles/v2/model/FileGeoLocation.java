package com.github.wensttay.orderfiles.v2.model;

public record FileGeoLocation(
        Double latitude,
        Double longitude
) {
    private static final double ONE_KILOMETER_IN_METERS = 1000.0;

    public boolean isNear(final FileGeoLocation other) {
        return isNear(other, ONE_KILOMETER_IN_METERS);
    }

    public boolean isNear(final FileGeoLocation other, final double maxDistanceInMeters) {
        if (other == null) {
            return false;
        }

        final double latDistance = Math.toRadians(other.latitude - this.latitude);
        final double lonDistance = Math.toRadians(other.longitude - this.longitude);

        final double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(this.latitude)) * Math.cos(Math.toRadians(other.latitude))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        final double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        final double distanceInMeters = 6371000 * c;

        return distanceInMeters <= maxDistanceInMeters;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof FileGeoLocation other)) {
            return false;
        }

        return isNear(other, ONE_KILOMETER_IN_METERS);
    }

    @Override
    public int hashCode() {
        return 0;
    }
}
