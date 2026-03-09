package com.quicklift.backend.model;

import java.math.BigDecimal;

public enum VehicleType {
    SEDAN(new BigDecimal("1.00")),
    SUV(new BigDecimal("1.25")),
    LUXURY(new BigDecimal("1.60")),
    MOTORCYCLE(new BigDecimal("0.70")),
    VAN(new BigDecimal("1.40"));

    private final BigDecimal multiplier;

    VehicleType(BigDecimal multiplier) {
        this.multiplier = multiplier;
    }

    public BigDecimal getMultiplier() {
        return multiplier;
    }
}
