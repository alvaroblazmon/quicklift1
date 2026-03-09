package com.quicklift.backend.service;

import com.quicklift.backend.dto.TripRequest;
import com.quicklift.backend.model.VehicleType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FareServiceTest {

    private FareService fareService;

    @BeforeEach
    void setUp() {
        fareService = new FareService();
    }

    @Test
    void calculateDistance_sameCoordinates_returnsZero() {
        double distance = fareService.calculateDistance(40.4168, -3.7038, 40.4168, -3.7038);

        assertEquals(0.0, distance, 1e-9);
    }

    @Test
    void calculateDistance_madridToBarcelona_returnsExpectedApproximateDistance() {
        double distance = fareService.calculateDistance(40.4168, -3.7038, 41.3874, 2.1686);

        assertEquals(505.10, distance, 1.0);
    }

    @Test
    void calculateFare_withDistanceAndTolls_returnsRoundedTotal() {
        BigDecimal fare = fareService.calculateFare(10.0, new BigDecimal("5.55"));

        assertEquals(new BigDecimal("115.55"), fare);
    }

    @Test
    void calculateFare_tripRequestWithNullTolls_usesZeroTolls() {
        TripRequest request = new TripRequest();
        request.setPickupLatitude(new BigDecimal("40.4168"));
        request.setPickupLongitude(new BigDecimal("-3.7038"));
        request.setDestinationLatitude(new BigDecimal("40.4170"));
        request.setDestinationLongitude(new BigDecimal("-3.7000"));
        request.setTolls(null);

        BigDecimal fare = fareService.calculateFare(request);

        assertEquals(new BigDecimal("3.55"), fare);
    }

    @Test
    void calculateFare_tripRequestWithMissingCoordinates_throwsIllegalArgumentException() {
        TripRequest request = new TripRequest();
        request.setPickupLatitude(new BigDecimal("40.4168"));
        request.setPickupLongitude(new BigDecimal("-3.7038"));
        request.setDestinationLatitude(new BigDecimal("40.4170"));
        request.setDestinationLongitude(null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> fareService.calculateFare(request));

        assertEquals("Pickup/destination coordinates are required to estimate fare.", exception.getMessage());
    }

    @Test
    void calculateFare_tripRequestWithVan_appliesOnePointFiveMultiplier() {
        TripRequest request = new TripRequest();
        request.setPickupLatitude(new BigDecimal("40.4168"));
        request.setPickupLongitude(new BigDecimal("-3.7038"));
        request.setDestinationLatitude(new BigDecimal("40.4170"));
        request.setDestinationLongitude(new BigDecimal("-3.7000"));
        request.setTolls(BigDecimal.ZERO);
        request.setVehicleType(VehicleType.VAN);

        BigDecimal fare = fareService.calculateFare(request);

        assertEquals(new BigDecimal("5.33"), fare);
    }
}
