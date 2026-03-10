package com.quicklift.backend.service;

import com.quicklift.backend.dto.TripRequest;
import com.quicklift.backend.model.VehicleType;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumMap;
import java.util.Map;

@Service
public class FareService {

    private static final double EARTH_RADIUS_KM = 6371;
    
    // Per-kilometer rate in rupees
    private static final BigDecimal RATE_PER_KM = new BigDecimal("11.00");
    private static final Map<VehicleType, BigDecimal> VEHICLE_MULTIPLIERS = new EnumMap<>(VehicleType.class);

    static {
        VEHICLE_MULTIPLIERS.put(VehicleType.SEDAN, new BigDecimal("1.0"));
        VEHICLE_MULTIPLIERS.put(VehicleType.SUV, new BigDecimal("1.25"));
        VEHICLE_MULTIPLIERS.put(VehicleType.LUXURY, new BigDecimal("1.6"));
        VEHICLE_MULTIPLIERS.put(VehicleType.MOTORCYCLE, new BigDecimal("0.7"));
        VEHICLE_MULTIPLIERS.put(VehicleType.VAN, new BigDecimal("1.4"));
    }

    public double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }

    public BigDecimal calculateFare(TripRequest request) {
        if (request.getPickupLatitude() == null || request.getPickupLongitude() == null ||
            request.getDestinationLatitude() == null || request.getDestinationLongitude() == null) {
            // Cannot calculate fare without coordinates, return a default or throw exception
            // For now, let's return a default value or handle as an error.
            // Returning zero or a default could be misleading. Let's throw an exception.
            throw new IllegalArgumentException("Pickup/destination coordinates are required to estimate fare.");
        }

        double distance = calculateDistance(
            request.getPickupLatitude().doubleValue(),
            request.getPickupLongitude().doubleValue(),
            request.getDestinationLatitude().doubleValue(),
            request.getDestinationLongitude().doubleValue()
        );

        BigDecimal tolls = request.getTolls() != null ? request.getTolls() : BigDecimal.ZERO;
        return calculateFare(distance, tolls, request.getVehicleType());
    }

    public BigDecimal calculateFare(double distance, BigDecimal tolls) {
        return calculateFare(distance, tolls, null);
    }

    public BigDecimal calculateFare(double distance, BigDecimal tolls, VehicleType vehicleType) {
        BigDecimal distanceCost = BigDecimal.valueOf(distance).multiply(RATE_PER_KM);
        BigDecimal baseFare = distanceCost.add(tolls);
        BigDecimal multiplier = VEHICLE_MULTIPLIERS.getOrDefault(vehicleType, BigDecimal.ONE);
        return baseFare.multiply(multiplier).setScale(2, RoundingMode.HALF_UP);
    }
}
