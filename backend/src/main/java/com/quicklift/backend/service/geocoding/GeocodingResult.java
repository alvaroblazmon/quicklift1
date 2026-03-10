package com.quicklift.backend.service.geocoding;

import java.math.BigDecimal;

public class GeocodingResult {
    private final BigDecimal latitude;
    private final BigDecimal longitude;

    public GeocodingResult(BigDecimal latitude, BigDecimal longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }
}
