package com.project.back_end.services;

import com.project.back_end.models.Appointment;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class AppointmentService {

    private AppointmentRepository appointmentRepository;
    private PatientRepository patientRepository;
    private DoctorRepository doctorRepository;
    private TokenService tokenService;
    private com.project.back_end.services.Service service;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              PatientRepository patientRepository,
                              DoctorRepository doctorRepository,
                              TokenService tokenService,
                              com.project.back_end.services.Service service) {

        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.tokenService = tokenService;
        this.service = service;
    }

    @Transactional
    public int bookAppointment(Appointment appointment) {
        try {
            appointmentRepository.save(appointment);
            return 1;
        } catch (Exception e) {
            System.out.println("Error saving in the database: " + e.getMessage());
            return 0;
        }
    }

    @Transactional
    public ResponseEntity<Map<String, String>> updateAppointment(Appointment appointment) {
        //<String, String> map = new HashMap<String, String>();
        Optional<Appointment> optional = appointmentRepository.findById(appointment.getId());
        if(optional.isEmpty()) {
            //map.put("failure", "The appointment to update was not found");
            //return new ResponseEntity<Map<String, String>>(map, HttpStatus.NOT_FOUND);
            return new ResponseEntity<>(Map.of("message", "The appointment to update was not found"),
                    HttpStatus.NOT_FOUND);
        }

        if(service.validateAppointment(appointment) != 1) {
            //map.put("failure", "The inserted data is not valid");
            //return new ResponseEntity<Map<String, String>>(map, HttpStatus.BAD_REQUEST);
            return new ResponseEntity<>(Map.of("message", "The inserted data is not valid"),
                    HttpStatus.BAD_REQUEST);
        }

        Appointment existing = optional.get();
        existing.setDoctor(appointment.getDoctor());
        existing.setPatient(appointment.getPatient());
        existing.setAppointmentTime(appointment.getAppointmentTime());
        existing.setStatus(appointment.getStatus());

        appointmentRepository.save(existing);

        //map.put("success", "Appointment was updated successfully");
        //return new ResponseEntity<Map<String, String>>(map, HttpStatus.OK);
        return new ResponseEntity<>(Map.of("message", "Appointment was updated successfully"),
                HttpStatus.OK);
    }

    @Transactional
    public ResponseEntity<Map<String, String>> cancelAppointment(Long id, String token) {
        //HashMap<String, String> map = new HashMap<String, String>();
        Optional<Appointment> optional = appointmentRepository.findById(id);
        if(optional.isEmpty()) {
            //map.put("failure", "The appointment to delete was not found");
            //return new ResponseEntity<Map<String, String>>(map, HttpStatus.NOT_FOUND);
            return new ResponseEntity<>(Map.of("message", "The appointment to delete was not found"),
                    HttpStatus.NOT_FOUND);
        }

        if(!service.validateToken(token, "patient" /* patient id ?*/ ).getBody().get("message").equals("ALLOWED?")) {
            return new ResponseEntity<>(Map.of("message", "Unauthorized"),
                    HttpStatus.UNAUTHORIZED);
        }

        appointmentRepository.delete(optional.get());
        return new ResponseEntity<>(Map.of("message", "Successfully deleted the appointment"),
                HttpStatus.OK);
    }

    public Map<String, Object> getAppointment(String pname, LocalDate date, String token) {
        // get doctor id from token?
        Long doctorId = doctorRepository.findByEmail(tokenService.extractEmail(token)).getId();
        if(pname != null && !pname.isEmpty()) {
            return Map.of("appointments", appointmentRepository.findByDoctorIdAndPatient_NameContainingIgnoreCaseAndAppointmentTimeBetween(
                    doctorId, pname, date.atStartOfDay(), date.plusDays(1).atStartOfDay()));
        } else {
            return Map.of("appointments", appointmentRepository.findByDoctorIdAndAppointmentTimeBetween(
                    doctorId, date.atStartOfDay(), date.plusDays(1).atStartOfDay()));
        }
    }
}
