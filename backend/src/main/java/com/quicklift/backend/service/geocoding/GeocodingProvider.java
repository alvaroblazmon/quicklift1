package com.quicklift.backend.service.geocoding;

public interface GeocodingProvider {
    GeocodingResult resolve(String address);
}
