package com.quicklift.backend.service;

import com.quicklift.backend.dto.TripRequest;
import com.quicklift.backend.model.VehicleType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.stream.Stream;

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
    void calculateDistance_betweenMadridAndBarcelona_returnsExpectedRange() {
        double distance = fareService.calculateDistance(40.4168, -3.7038, 41.3874, 2.1686);

        assertEquals(505, distance, 10);
    }

    @Test
    void calculateFare_withDistanceAndTolls_returnsRoundedAmount() {
        BigDecimal fare = fareService.calculateFare(10.0, new BigDecimal("5.55"));

        assertEquals(new BigDecimal("115.55"), fare);
    }

    @Test
    void calculateFare_withTripRequestAndNoTolls_usesZeroTolls() {
        TripRequest request = new TripRequest();
        request.setPickupLatitude(new BigDecimal("40.4168"));
        request.setPickupLongitude(new BigDecimal("-3.7038"));
        request.setDestinationLatitude(new BigDecimal("40.4180"));
        request.setDestinationLongitude(new BigDecimal("-3.7000"));

        BigDecimal fare = fareService.calculateFare(request);

        assertEquals(new BigDecimal("3.83"), fare);
    }

    @Test
    void calculateFare_withTripRequestAndTolls_includesTolls() {
        TripRequest request = new TripRequest();
        request.setPickupLatitude(new BigDecimal("40.4168"));
        request.setPickupLongitude(new BigDecimal("-3.7038"));
        request.setDestinationLatitude(new BigDecimal("40.4180"));
        request.setDestinationLongitude(new BigDecimal("-3.7000"));
        request.setTolls(new BigDecimal("2.00"));

        BigDecimal fare = fareService.calculateFare(request);

        assertEquals(new BigDecimal("5.83"), fare);
    }

    @ParameterizedTest
    @MethodSource("vehicleMultiplierCases")
    void calculateFare_withTripRequestAndVehicleType_appliesExpectedMultiplier(VehicleType vehicleType, BigDecimal expectedFare) {
        TripRequest request = buildTripRequestWithTolls();
        request.setVehicleType(vehicleType);

        BigDecimal fare = fareService.calculateFare(request);

        assertEquals(expectedFare, fare);
    }

    @Test
    void calculateFare_withMissingCoordinates_throwsIllegalArgumentException() {
        TripRequest request = new TripRequest();
        request.setPickupLatitude(new BigDecimal("40.4168"));
        request.setPickupLongitude(new BigDecimal("-3.7038"));
        request.setDestinationLatitude(new BigDecimal("40.4180"));
        request.setDestinationLongitude(null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> fareService.calculateFare(request));

        assertEquals("Pickup/destination coordinates are required to estimate fare.", exception.getMessage());
    }

    private static Stream<Arguments> vehicleMultiplierCases() {
        return Stream.of(
                Arguments.of(VehicleType.SEDAN, new BigDecimal("5.83")),
                Arguments.of(VehicleType.SUV, new BigDecimal("7.29")),
                Arguments.of(VehicleType.LUXURY, new BigDecimal("9.33")),
                Arguments.of(VehicleType.MOTORCYCLE, new BigDecimal("4.08")),
                Arguments.of(VehicleType.VAN, new BigDecimal("8.16")),
                Arguments.of(null, new BigDecimal("5.83"))
        );
    }

    private TripRequest buildTripRequestWithTolls() {
        TripRequest request = new TripRequest();
        request.setPickupLatitude(new BigDecimal("40.4168"));
        request.setPickupLongitude(new BigDecimal("-3.7038"));
        request.setDestinationLatitude(new BigDecimal("40.4180"));
        request.setDestinationLongitude(new BigDecimal("-3.7000"));
        request.setTolls(new BigDecimal("2.00"));
        return request;
    }
}
