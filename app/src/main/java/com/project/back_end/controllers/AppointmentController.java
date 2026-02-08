package com.project.back_end.controllers;

import com.project.back_end.models.Appointment;
import com.project.back_end.services.AppointmentService;
import com.project.back_end.services.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.Serial;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/appointments")
public class AppointmentController {

    @Autowired
    AppointmentService appointmentService;
    @Autowired
    Service service;

    @GetMapping("/{date}/{patientName}/{token}")
    public ResponseEntity<?> getAppointments(
            @PathVariable LocalDate date,
            @PathVariable String patientName,
            @PathVariable String token) {

        ResponseEntity<Map<String, String>> validateTokenResult = service.validateToken(token, "doctor");
        if(validateTokenResult.getStatusCode().equals(HttpStatus.UNAUTHORIZED)) {
            return validateTokenResult;
        }

        return new ResponseEntity<>(
                Map.of("appointments", appointmentService.getAppointment(patientName, date, token)),
                HttpStatus.OK);
    }

    //@PostMapping("/book/{token}")
    @PostMapping("/{token}")
    public ResponseEntity<?> bookAppointment(@RequestBody Appointment appointment, @PathVariable String token) {

        ResponseEntity<Map<String, String>> validateTokenResult = service.validateToken(token, "patient");
        if(validateTokenResult.getStatusCode().equals(HttpStatus.UNAUTHORIZED)) {
            return validateTokenResult;
        }
        if(service.validateAppointment(appointment) != 1) {
            return new ResponseEntity<>(Map.of("error", "Time slot is already taken or doctor doesn't exist"),
                    HttpStatus.BAD_REQUEST);
        }

        if(appointmentService.bookAppointment(appointment) == 1) {
            return new ResponseEntity<>(Map.of("message", "Appointment created successfully"), HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>(Map.of("error", "Could not create appointment"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{token}")
    public ResponseEntity<?> updateAppointment(@RequestBody Appointment appointment, @PathVariable String token) {
        ResponseEntity<Map<String, String>> validateTokenResult = service.validateToken(token, "patient");
        if(validateTokenResult.getStatusCode().equals(HttpStatus.UNAUTHORIZED)) {
            return validateTokenResult;
        }

        if(service.validateAppointment(appointment) != 1) {
            return new ResponseEntity<>(Map.of("error", "Time slot is already taken or doctor doesn't exist"),
                    HttpStatus.BAD_REQUEST);
        }

        return appointmentService.updateAppointment(appointment);
    }

    @DeleteMapping("/{id}/{token}")
    public ResponseEntity<?> cancelAppointment(@PathVariable Long id, @PathVariable String token) {
        ResponseEntity<Map<String, String>> validateTokenResult = service.validateToken(token, "patient");
        if(validateTokenResult.getStatusCode().equals(HttpStatus.UNAUTHORIZED)) {
            return validateTokenResult;
        }

        return appointmentService.cancelAppointment(id, token);
    }
}
