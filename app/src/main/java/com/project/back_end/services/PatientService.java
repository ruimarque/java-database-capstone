package com.project.back_end.services;

import com.project.back_end.DTO.AppointmentDTO;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Doctor;
import com.project.back_end.models.Patient;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.PatientRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PatientService {

    private PatientRepository patientRepository;
    private AppointmentRepository appointmentRepository;
    private TokenService tokenService;

    public PatientService(PatientRepository patientRepository,
                          AppointmentRepository appointmentRepository,
                          TokenService tokenService) {
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
        this.tokenService = tokenService;
    }

    public int createPatient(Patient patient) {
        try {
            patientRepository.save(patient);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    public ResponseEntity<Map<String, Object>> getPatientAppointment(Long id, String token) {
        // get patient email from token
        String email = tokenService.extractEmail(token);
        // find patient with email
        Patient patient = patientRepository.findByEmail(email);
        if(patient == null) {
            return new ResponseEntity<>(Map.of("message", "Patient not found"), HttpStatus.NOT_FOUND);
        }
        // check if ids match
        if(!patient.getId().equals(id)) {
            return new ResponseEntity<>(Map.of("message", "Patient is not authorized to do that operation"),
                    HttpStatus.UNAUTHORIZED);
        }
        List<Appointment> patientAppointments = appointmentRepository.findByPatientId(id);
        if(patientAppointments.isEmpty()) {
            return new ResponseEntity<>(Map.of("appointments", List.<AppointmentDTO>of()), HttpStatus.OK);
        } else {
            List<AppointmentDTO> patientAppointmentsDTO = patientAppointments.stream().map(this::convertToDTO)
                    .toList();
            return new ResponseEntity<>(Map.of("appointments", patientAppointmentsDTO), HttpStatus.OK);
        }
    }

    public ResponseEntity<Map<String, Object>> filterByCondition(String condition, Long id) {
        // filter by condition (past or future) and patient id
        int status = condition.equalsIgnoreCase("past") ? 1
                : condition.equalsIgnoreCase("future") ? 0 : -1;
        if(status == -1) {
            return new ResponseEntity<>(Map.of("message", "Invalid condition, has to be past or future"),
                    HttpStatus.BAD_REQUEST);
        }
        if(!patientRepository.existsById(id)) {
            return new ResponseEntity<>(Map.of("message", "Patient does not exist"),
                    HttpStatus.NOT_FOUND);
        }
        List<Appointment> patientAppointments =
                appointmentRepository.findByPatientIdAndStatusOrderByAppointmentTimeAsc(id, status);
        if(patientAppointments.isEmpty()) {
            return new ResponseEntity<>(Map.of("appointments", List.<AppointmentDTO>of()), HttpStatus.OK);
        } else {
            List<AppointmentDTO> patientAppointmentsDTO = patientAppointments.stream().map(this::convertToDTO)
                    .toList();
            return new ResponseEntity<>(Map.of("appointments", patientAppointmentsDTO), HttpStatus.OK);
        }
    }

    public ResponseEntity<Map<String, Object>> filterByDoctor(String name, Long patientId) {
        if(!patientRepository.existsById(patientId)) {
            return new ResponseEntity<>(Map.of("message", "Patient does not exist"),
                    HttpStatus.NOT_FOUND);
        }
        List<Appointment> patientAppointments =
                appointmentRepository.filterByDoctorNameAndPatientId(name, patientId);
        if(patientAppointments.isEmpty()) {
            return new ResponseEntity<>(Map.of("appointments", List.<AppointmentDTO>of()), HttpStatus.OK);
        } else {
            List<AppointmentDTO> patientAppointmentsDTO = patientAppointments.stream().map(this::convertToDTO)
                    .toList();
            return new ResponseEntity<>(Map.of("appointments", patientAppointmentsDTO), HttpStatus.OK);
        }
    }

    public ResponseEntity<Map<String, Object>> filterByDoctorAndCondition(String condition, String name, Long patientId) {
        int status = condition.equalsIgnoreCase("past") ? 1
                : condition.equalsIgnoreCase("future") ? 0 : -1;
        if(status == -1) {
            return new ResponseEntity<>(Map.of("message", "Invalid condition, has to be past or future"),
                    HttpStatus.BAD_REQUEST);
        }
        if(!patientRepository.existsById(patientId)) {
            return new ResponseEntity<>(Map.of("message", "Patient does not exist"),
                    HttpStatus.NOT_FOUND);
        }
        List<Appointment> patientAppointments =
                appointmentRepository.filterByDoctorNameAndPatientIdAndStatus(name, patientId, status);
        if(patientAppointments.isEmpty()) {
            return new ResponseEntity<>(Map.of("appointments", List.<AppointmentDTO>of()), HttpStatus.OK);
        } else {
            List<AppointmentDTO> patientAppointmentsDTO = patientAppointments.stream().map(this::convertToDTO)
                    .toList();
            return new ResponseEntity<>(Map.of("appointments", patientAppointmentsDTO), HttpStatus.OK);
        }
    }

    public ResponseEntity<Map<String, Object>> getPatientDetails(String token) {
        String email = tokenService.extractEmail(token);
        Patient patient = patientRepository.findByEmail(email);
        if(patient == null) {
            return new ResponseEntity<>(Map.of("message", "Patient not found"), HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(Map.of("details", patient), HttpStatus.OK);
    }

    private AppointmentDTO convertToDTO(Appointment appointment) {
        Doctor doctor = appointment.getDoctor();
        Patient patient = appointment.getPatient();

        return new AppointmentDTO(appointment.getId(), doctor.getId(), doctor.getName(), patient.getId(),
                patient.getName(), patient.getEmail(), patient.getPhone(), patient.getAddress(),
                appointment.getAppointmentTime(), appointment.getStatus());
    }
}
