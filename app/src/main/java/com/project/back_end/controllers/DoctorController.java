package com.project.back_end.controllers;

import com.project.back_end.DTO.Login;
import com.project.back_end.models.Doctor;
import com.project.back_end.services.DoctorService;
import com.project.back_end.services.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("${api.path}doctor")
public class DoctorController {

    @Autowired
    DoctorService doctorService;
    @Autowired
    Service service;

    @GetMapping("/availability/{user}/{doctorId}/{date}/{token}")
    public ResponseEntity<?> getDoctorAvailability(
            @PathVariable String user,
            @PathVariable Long doctorId,
            @PathVariable LocalDate date,
            @PathVariable String token
            ) {

        ResponseEntity<Map<String, String>> validateTokenResult = service.validateToken(token, user);
        if(validateTokenResult.getStatusCode().equals(HttpStatus.UNAUTHORIZED)) {
            return validateTokenResult;
        }

        return new ResponseEntity<>(Map.of("availability", doctorService.getDoctorAvailability(doctorId, date)),
                HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<?> getDoctor() {
        return new ResponseEntity<>(Map.of("doctors", doctorService.getDoctors()), HttpStatus.OK);
    }

    @PostMapping("/{token}")
    public ResponseEntity<?> saveDoctor(@RequestBody Doctor doctor, @PathVariable String token) {
        ResponseEntity<Map<String, String>> validateTokenResult = service.validateToken(token, "admin");
        if(validateTokenResult.getStatusCode().equals(HttpStatus.UNAUTHORIZED)) {
            return validateTokenResult;
        }

        int saveDoctorResult = doctorService.saveDoctor(doctor);
        switch (saveDoctorResult) {
            case -1:
                return new ResponseEntity<>(Map.of("conflict", "Doctor already exists"), HttpStatus.CONFLICT);
            case 1:
                return new ResponseEntity<>(Map.of("success", "Doctor added to db"), HttpStatus.CREATED);
            default:
                return new ResponseEntity<>(Map.of("internal error", "Some internal error occurred"),
                        HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> doctorLogin(@RequestBody Login login) {
        return doctorService.validateDoctor(login);
    }

    @PutMapping("/{token}")
    public ResponseEntity<?> updateDoctor(@RequestBody Doctor doctor, @PathVariable String token) {
        ResponseEntity<Map<String, String>> validateTokenResult = service.validateToken(token, "admin");
        if(validateTokenResult.getStatusCode().equals(HttpStatus.UNAUTHORIZED)) {
            return validateTokenResult;
        }

        int updateDoctorResult = doctorService.updateDoctor(doctor);
        switch (updateDoctorResult) {
            case -1:
                return new ResponseEntity<>(Map.of("Not found", "Doctor does not exist"), HttpStatus.NOT_FOUND);
            case 1:
                return new ResponseEntity<>(Map.of("success", "Doctor successfully updated"), HttpStatus.OK);
            default:
                return new ResponseEntity<>(Map.of("internal error", "Some internal error occurred"),
                        HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{id}/{token}")
    public ResponseEntity<?> deleteDoctor(@PathVariable Long id, @PathVariable String token) {
        ResponseEntity<Map<String, String>> validateTokenResult = service.validateToken(token, "admin");
        if(validateTokenResult.getStatusCode().equals(HttpStatus.UNAUTHORIZED)) {
            return validateTokenResult;
        }

        int deleteDoctorResult = doctorService.deleteDoctor(id);
        return switch (deleteDoctorResult) {
            case -1 -> new ResponseEntity<>(Map.of("Not found", "Doctor does not exist"), HttpStatus.NOT_FOUND);
            case 1 -> new ResponseEntity<>(Map.of("success", "Doctor was deleted successfully"), HttpStatus.OK);
            default -> new ResponseEntity<>(Map.of("internal error", "Some internal error occurred"),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        };
    }

    @GetMapping("/filter/{name}/{time}/{specialty}")
    public ResponseEntity<?> filter(@PathVariable(required = false) String name,
                                    @PathVariable(required = false) String date,
                                    @PathVariable(required = false) String specialty) {
            return new ResponseEntity<>(
                    Map.of("doctors", service.filterDoctor(name, specialty, date)),
                    HttpStatus.OK);
    }
}
