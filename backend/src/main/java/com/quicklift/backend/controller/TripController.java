package com.quicklift.backend.controller;

import com.quicklift.backend.dto.TripRequest;
import com.quicklift.backend.dto.FareResponse;
import com.quicklift.backend.exception.GeocodingException;
import com.quicklift.backend.model.Trip;
import com.quicklift.backend.model.TripStatus;
import com.quicklift.backend.model.User;
import com.quicklift.backend.service.FareService;
import com.quicklift.backend.service.TripCoordinateResolverService;
import com.quicklift.backend.service.TripService;
import com.quicklift.backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/trips")
public class TripController {

    @Autowired
    private TripService tripService;

    @Autowired
    private UserService userService;

    @Autowired
    private FareService fareService;

    @Autowired
    private TripCoordinateResolverService tripCoordinateResolverService;

    @PostMapping("/estimate")
    public ResponseEntity<?> estimateFare(@Valid @RequestBody TripRequest tripRequest) {
        try {
            TripRequest resolvedRequest = tripCoordinateResolverService.resolveCoordinates(tripRequest);
            double distance = fareService.calculateDistance(
                resolvedRequest.getPickupLatitude().doubleValue(),
                resolvedRequest.getPickupLongitude().doubleValue(),
                resolvedRequest.getDestinationLatitude().doubleValue(),
                resolvedRequest.getDestinationLongitude().doubleValue()
            );
            BigDecimal estimatedFare = fareService.calculateFare(resolvedRequest);
            return ResponseEntity.ok(new FareResponse(estimatedFare, distance));
        } catch (GeocodingException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("An error occurred while estimating the fare.");
        }
    }

    @PostMapping("/book")
    public ResponseEntity<?> bookTrip(@Valid @RequestBody TripRequest tripRequest) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            Optional<User> user = userService.findByUsername(username);
            if (user.isEmpty()) {
                return ResponseEntity.badRequest().body("User not found");
            }

            BigDecimal fare = fareService.calculateFare(tripRequest);

            Trip trip = new Trip();
            trip.setUser(user.get());
            trip.setPickupLocation(tripRequest.getPickupLocation());
            trip.setDestination(tripRequest.getDestination());
            trip.setRequestedVehicleType(tripRequest.getVehicleType());
            trip.setPickupLatitude(tripRequest.getPickupLatitude());
            trip.setPickupLongitude(tripRequest.getPickupLongitude());
            trip.setDestinationLatitude(tripRequest.getDestinationLatitude());
            trip.setDestinationLongitude(tripRequest.getDestinationLongitude());
            trip.setNotes(tripRequest.getNotes());
            trip.setFare(fare);
            trip.setPaymentMethod(tripRequest.getPaymentMethod());

            Trip createdTrip = tripService.createTripAndAssignDriver(trip);
            return ResponseEntity.ok(createdTrip);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/my-trips")
    public ResponseEntity<?> getMyTrips() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        Optional<User> user = userService.findByUsername(username);
        if (user.isEmpty()) {
            return ResponseEntity.badRequest().body("User not found");
        }

        List<Trip> trips = tripService.findByUserId(user.get().getId());
        return ResponseEntity.ok(trips);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getTripById(@PathVariable Long id) {
        Optional<Trip> trip = tripService.findById(id);
        if (trip.isPresent()) {
            return ResponseEntity.ok(trip.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<?> cancelTrip(@PathVariable Long id) {
        try {
            Trip cancelledTrip = tripService.cancelTrip(id);
            return ResponseEntity.ok(cancelledTrip);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/rate")
    public ResponseEntity<?> rateTrip(@PathVariable Long id, 
                                     @RequestParam BigDecimal rating,
                                     @RequestParam(required = false) String review) {
        try {
            Trip ratedTrip = tripService.rateTrip(id, rating, review);
            return ResponseEntity.ok(ratedTrip);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<?> getTripsByStatus(@PathVariable TripStatus status) {
        List<Trip> trips = tripService.findByStatus(status);
        return ResponseEntity.ok(trips);
    }

    @GetMapping("/driver/{driverId}")
    public ResponseEntity<?> getTripsByDriver(@PathVariable Long driverId) {
        List<Trip> trips = tripService.findByDriverId(driverId);
        return ResponseEntity.ok(trips);
    }

    @PostMapping("/request-to-driver")
    public ResponseEntity<?> requestToDriver(@RequestBody TripRequest tripRequest, @RequestParam Long driverId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            Optional<User> user = userService.findByUsername(username);
            if (user.isEmpty()) {
                return ResponseEntity.badRequest().body("User not found");
            }
            Trip trip = tripService.createTripForDriver(tripRequest, user.get(), driverId);
            return ResponseEntity.ok(trip);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/pay")
    public ResponseEntity<?> payForTrip(@PathVariable Long id) {
        try {
            Trip paidTrip = tripService.payForTrip(id);
            return ResponseEntity.ok(paidTrip);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/payment-method")
    public ResponseEntity<?> updatePaymentMethod(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            String paymentMethod = body.get("paymentMethod");
            Trip trip = tripService.findById(id).orElseThrow(() -> new RuntimeException("Trip not found"));
            trip.setPaymentMethod(paymentMethod);
            Trip updatedTrip = tripService.save(trip);
            return ResponseEntity.ok(updatedTrip);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
} 
