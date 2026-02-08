package com.project.back_end.services;

import com.project.back_end.models.Prescription;
import com.project.back_end.repo.PrescriptionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class PrescriptionService {

    private PrescriptionRepository prescriptionRepository;

    public PrescriptionService(PrescriptionRepository prescriptionRepository) {
        this.prescriptionRepository = prescriptionRepository;
    }

    public ResponseEntity<Map<String, String>> savePrescription(Prescription prescription){
        List<Prescription> existingPrescriptionsForAppointment =
                prescriptionRepository.findByAppointmentId(prescription.getAppointmentId());

        if(!existingPrescriptionsForAppointment.isEmpty()) {
            return new ResponseEntity<>(Map.of("message", "Prescription already exists"), HttpStatus.BAD_REQUEST);
        }

        try {
            prescriptionRepository.save(prescription);
            return new ResponseEntity<>(Map.of("message", "Prescription saved"), HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(Map.of("message", "Couldn't create prescription: " + e.getMessage()),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<Map<String, Object>> getPrescription(Long appointmentId) {
        try {
            List<Prescription> prescriptions = prescriptionRepository.findByAppointmentId(appointmentId);
            if(prescriptions.isEmpty()) {
                return new ResponseEntity<>(Map.of("message", "There are no prescriptions for this appointment"),
                        HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(Map.of("prescriptions", prescriptions), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(Map.of("message", "Error while fetching prescriptions: " + e.getMessage()),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
