package com.quicklift.backend.exception;

public class GeocodingResolutionException extends GeocodingException {
    public GeocodingResolutionException(String message) {
        super(message);
    }

    public GeocodingResolutionException(String message, Throwable cause) {
        super(message, cause);
    }
}
