package com.project.back_end.controllers;

import com.project.back_end.DTO.Login;
import com.project.back_end.models.Patient;
import com.project.back_end.services.PatientService;
import com.project.back_end.services.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/patient")
public class PatientController {

    @Autowired
    PatientService patientService;
    @Autowired
    Service service;

    @GetMapping("/{token}")
    public ResponseEntity<?> getPatient(@PathVariable String token) {
        ResponseEntity<Map<String, String>> validateTokenResult = service.validateToken(token, "patient");
        if(validateTokenResult.getStatusCode().equals(HttpStatus.UNAUTHORIZED)) {
            return validateTokenResult;
        }

        return patientService.getPatientDetails(token);
    }

    @PostMapping()
    public ResponseEntity<?> createPatient(@RequestBody Patient patient) {
        if(!service.validatePatient(patient)) {
            return new ResponseEntity<>(Map.of("message", "Patient already exists"), HttpStatus.CONFLICT);
        }

        int createPatientResult = patientService.createPatient(patient);
        switch (createPatientResult) {
            case 1:
                return new ResponseEntity<>(Map.of("success", "Patient added to db"), HttpStatus.CREATED);
            default:
                return new ResponseEntity<>(Map.of("internal error", "Some internal error occurred"),
                        HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginPatient(@RequestBody Login login) {
        return service.validatePatientLogin(login);
    }


    // 6. Define the `getPatientAppointment` Method:
//    - Handles HTTP GET requests to fetch appointment details for a specific patient.
//    - Requires the patient ID, token, and user role as path variables.
//    - Validates the token using the shared service.
//    - If valid, retrieves the patient's appointment data from `PatientService`; otherwise, returns a validation error.
    @GetMapping("/{id}/{token}")
    public ResponseEntity<?> getPatientAppointment(@PathVariable Long id, @PathVariable String token) {
        ResponseEntity<Map<String, String>> validateTokenResult = service.validateToken(token, "patient");
        if(validateTokenResult.getStatusCode().equals(HttpStatus.UNAUTHORIZED)) {
            return validateTokenResult;
        }

        return patientService.getPatientAppointment(id, token);
    }

    @GetMapping("/filter/{condition}/{name}/{token}")
    public ResponseEntity<?> filterPatientAppointment(
            @PathVariable(required = false) String condition,
            @PathVariable(required = false) String name,
            @PathVariable String token) {

        ResponseEntity<Map<String, String>> validateTokenResult = service.validateToken(token, "patient");
        if(validateTokenResult.getStatusCode().equals(HttpStatus.UNAUTHORIZED)) {
            return validateTokenResult;
        }

        return service.filterPatient(condition, name, token);
    }
}


