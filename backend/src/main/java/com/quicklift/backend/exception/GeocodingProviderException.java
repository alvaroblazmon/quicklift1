package com.quicklift.backend.exception;

public class GeocodingProviderException extends GeocodingException {
    public GeocodingProviderException(String message) {
        super(message);
    }

    public GeocodingProviderException(String message, Throwable cause) {
        super(message, cause);
    }
}
