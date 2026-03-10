package com.quicklift.backend.service;

import com.quicklift.backend.dto.TripRequest;
import com.quicklift.backend.model.VehicleType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FareServiceTest {

    private final FareService fareService = new FareService();

    @ParameterizedTest
    @MethodSource("vehicleFareCases")
    void shouldApplyVehicleMultiplierToBaseFare(VehicleType vehicleType, String expectedFare) {
        BigDecimal fare = fareService.calculateFare(10.0, new BigDecimal("5.00"), vehicleType);

        assertThat(fare).isEqualByComparingTo(new BigDecimal(expectedFare));
    }

    @Test
    void shouldUseDefaultMultiplierWhenVehicleTypeIsMissing() {
        BigDecimal fare = fareService.calculateFare(10.0, new BigDecimal("5.00"), null);

        assertThat(fare).isEqualByComparingTo(new BigDecimal("115.00"));
    }

    @Test
    void shouldUseDefaultMultiplierInOverloadWithoutVehicleType() {
        BigDecimal fare = fareService.calculateFare(10.0, new BigDecimal("5.00"));

        assertThat(fare).isEqualByComparingTo(new BigDecimal("115.00"));
    }

    @Test
    void shouldThrowWhenCoordinatesAreMissing() {
        TripRequest request = new TripRequest();

        assertThatThrownBy(() -> fareService.calculateFare(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Pickup/destination coordinates are required to estimate fare.");
    }

    @Test
    void shouldUseDefaultMultiplierWhenTripRequestDoesNotIncludeVehicleType() {
        TripRequest request = new TripRequest();
        request.setPickupLatitude(new BigDecimal("40.4168"));
        request.setPickupLongitude(new BigDecimal("-3.7038"));
        request.setDestinationLatitude(new BigDecimal("40.4168"));
        request.setDestinationLongitude(new BigDecimal("-3.7038"));
        request.setTolls(new BigDecimal("10.00"));
        request.setVehicleType(null);

        BigDecimal fare = fareService.calculateFare(request);

        assertThat(fare).isEqualByComparingTo(new BigDecimal("10.00"));
    }

    private static Stream<Arguments> vehicleFareCases() {
        return Stream.of(
                Arguments.of(VehicleType.SEDAN, "115.00"),
                Arguments.of(VehicleType.SUV, "143.75"),
                Arguments.of(VehicleType.LUXURY, "184.00"),
                Arguments.of(VehicleType.MOTORCYCLE, "80.50"),
                Arguments.of(VehicleType.VAN, "161.00")
        );
    }
}
