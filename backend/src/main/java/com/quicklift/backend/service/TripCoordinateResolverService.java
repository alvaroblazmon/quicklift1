package com.quicklift.backend.service;

import com.quicklift.backend.dto.TripRequest;
import com.quicklift.backend.exception.GeocodingProviderException;
import com.quicklift.backend.exception.GeocodingResolutionException;
import com.quicklift.backend.service.geocoding.GeocodingProvider;
import com.quicklift.backend.service.geocoding.GeocodingResult;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class TripCoordinateResolverService {

    private final ObjectProvider<GeocodingProvider> geocodingProvider;

    public TripCoordinateResolverService(ObjectProvider<GeocodingProvider> geocodingProvider) {
        this.geocodingProvider = geocodingProvider;
    }

    public TripRequest resolveCoordinates(TripRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Trip request is required.");
        }

        TripRequest resolved = copyRequest(request);

        if (requiresGeocoding(request.getPickupLatitude(), request.getPickupLongitude())) {
            GeocodingResult pickupResult = resolveAddress(request.getPickupLocation(), "pickup");
            resolved.setPickupLatitude(pickupResult.getLatitude());
            resolved.setPickupLongitude(pickupResult.getLongitude());
        }

        if (requiresGeocoding(request.getDestinationLatitude(), request.getDestinationLongitude())) {
            GeocodingResult destinationResult = resolveAddress(request.getDestination(), "destination");
            resolved.setDestinationLatitude(destinationResult.getLatitude());
            resolved.setDestinationLongitude(destinationResult.getLongitude());
        }

        return resolved;
    }

    private boolean requiresGeocoding(BigDecimal latitude, BigDecimal longitude) {
        return latitude == null || longitude == null;
    }

    private GeocodingResult resolveAddress(String address, String pointName) {
        if (address == null || address.isBlank()) {
            throw new GeocodingResolutionException("Unable to resolve " + pointName + " coordinates.");
        }

        GeocodingProvider provider = geocodingProvider.getIfAvailable();
        if (provider == null) {
            throw new GeocodingProviderException("Geocoding provider is not configured.");
        }

        GeocodingResult result;
        try {
            result = provider.resolve(address);
        } catch (GeocodingProviderException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new GeocodingProviderException("Geocoding provider failed to resolve address.", ex);
        }

        if (result == null || result.getLatitude() == null || result.getLongitude() == null) {
            throw new GeocodingResolutionException("Unable to resolve " + pointName + " coordinates.");
        }

        return result;
    }

    private TripRequest copyRequest(TripRequest source) {
        TripRequest copy = new TripRequest(source.getPickupLocation(), source.getDestination(), source.getVehicleType());
        copy.setPickupLatitude(source.getPickupLatitude());
        copy.setPickupLongitude(source.getPickupLongitude());
        copy.setDestinationLatitude(source.getDestinationLatitude());
        copy.setDestinationLongitude(source.getDestinationLongitude());
        copy.setNotes(source.getNotes());
        copy.setTolls(source.getTolls());
        copy.setPaymentMethod(source.getPaymentMethod());
        return copy;
    }
}
