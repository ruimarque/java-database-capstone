package com.project.back_end.controllers;

import com.project.back_end.models.Prescription;
import com.project.back_end.services.PrescriptionService;
import com.project.back_end.services.Service;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("${api.path}" + "prescription")
public class PrescriptionController {

    @Autowired
    PrescriptionService prescriptionService;
    @Autowired
    Service service;

    @PostMapping("/{token}")
    public ResponseEntity<?> savePrescription(@RequestBody Prescription prescription, @PathVariable String token) {
        ResponseEntity<Map<String, String>> validateTokenResult = service.validateToken(token, "doctor");
        if(validateTokenResult.getStatusCode().equals(HttpStatus.UNAUTHORIZED)) {
            return validateTokenResult;
        }

        return prescriptionService.savePrescription(prescription);
    }

    @GetMapping("/{appointmentId}/{token}")
    public ResponseEntity<?> getPrescription(@PathVariable Long appointmentId, @PathVariable String token) {
        ResponseEntity<Map<String, String>> validateTokenResult = service.validateToken(token, "doctor");
        if(validateTokenResult.getStatusCode().equals(HttpStatus.UNAUTHORIZED)) {
            return validateTokenResult;
        }

        return prescriptionService.getPrescription(appointmentId);
    }
}
