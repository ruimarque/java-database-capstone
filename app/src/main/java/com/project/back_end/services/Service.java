package com.project.back_end.services;

import com.project.back_end.DTO.Login;
import com.project.back_end.models.Admin;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Doctor;
import com.project.back_end.models.Patient;
import com.project.back_end.repo.AdminRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;
import org.antlr.v4.runtime.Token;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@org.springframework.stereotype.Service
public class Service {

    private final TokenService tokenService;
    private final AdminRepository adminRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final DoctorService doctorService;
    private final PatientService patientService;

    public Service(TokenService tokenService,
                   AdminRepository adminRepository,
                   DoctorRepository doctorRepository,
                   PatientRepository patientRepository,
                   DoctorService doctorService,
                   PatientService patientService) {
        this.tokenService = tokenService;
        this.adminRepository = adminRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.doctorService = doctorService;
        this.patientService = patientService;
    }

    public ResponseEntity<Map<String, String>> validateToken(String token, String user) {
        boolean valid = tokenService.validateToken(token, user);
        if(!valid) {
            return new ResponseEntity<>(Map.of("message", "Token is invalid or expired"), HttpStatus.UNAUTHORIZED);
        }

        return new ResponseEntity<>(Map.of("message", "Token is valid"), HttpStatus.OK);
    }

    public ResponseEntity<Map<String, String>> validateAdmin(Admin receivedAdmin) {
        try {
            Admin existingAdmin = adminRepository.findByUsername(receivedAdmin.getUsername());
            // check if admin exists
            if(existingAdmin == null) {
                return new ResponseEntity<>(Map.of("message", "Invalid username"), HttpStatus.UNAUTHORIZED);
            }
            // check if passwords match
            if(!existingAdmin.getPassword().equals(receivedAdmin.getPassword())) {
                return new ResponseEntity<>(Map.of("message", "Invalid password"), HttpStatus.UNAUTHORIZED);
            }

            return new ResponseEntity<>(Map.of("token", tokenService.generateToken(existingAdmin.getUsername())),
                    HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(Map.of("message", "Login failed due to an internal error"),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<Map<String, Object>> filterDoctor(String name, String specialty, String time) {
        if(name != null && specialty != null && time != null) {
            return new ResponseEntity<>(doctorService.filterDoctorsByNameSpecialtyandTime(name, specialty, time),
                    HttpStatus.OK);
        } else if(name != null && specialty != null) {
            return new ResponseEntity<>(doctorService.filterDoctorByNameAndSpecialty(name, specialty),
                    HttpStatus.OK);
        } else if(specialty != null && time != null) {
            return new ResponseEntity<>(doctorService.filterDoctorByTimeAndSpecialty(specialty, time),
                    HttpStatus.OK);
        } else if(name != null && time != null) {
            return new ResponseEntity<>(doctorService.filterDoctorByNameAndTime(name, time), HttpStatus.OK);
        } else if(name != null) {
            return new ResponseEntity<>(doctorService.findDoctorsByName(name), HttpStatus.OK);
        } else if(specialty != null) {
            return new ResponseEntity<>(doctorService.filterDoctorBySpecialty(specialty), HttpStatus.OK);
        } else if(time != null) {
            return new ResponseEntity<>(doctorService.filterDoctorsByTime(time), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(Map.of("doctors", doctorService.getDoctors()), HttpStatus.OK);
        }
    }

    public int validateAppointment(Appointment appointment) {
        // try to get the doctor
        Optional<Doctor> checkDoctor = doctorRepository.findById(appointment.getDoctor().getId());
        if(checkDoctor.isEmpty()) { // doctor does not exist
            return -1;
        }

        List<String> doctorAvailability =
                doctorService.getDoctorAvailability(appointment.getDoctor().getId(), appointment.getAppointmentDate());

        return doctorAvailability.contains(appointment.getAppointmentTimeOnly().toString()) ? 1 : 0;
    }

    public boolean validatePatient(Patient patient) {
        return patientRepository.findByEmailOrPhone(patient.getEmail(), patient.getPhone()) == null;
    }

    public ResponseEntity<Map<String, String>> validatePatientLogin(Login login) {
        try {
            // check if patient exists
            Patient patient = patientRepository.findByEmail(login.getEmail());
            if (patient == null) {
                return new ResponseEntity<>(Map.of("message", "Invalid email"), HttpStatus.UNAUTHORIZED);
            }
            // check if password matches
            if (!patient.getPassword().equals(login.getPassword())) {
                return new ResponseEntity<>(Map.of("message", "Invalid password"), HttpStatus.UNAUTHORIZED);
            }
            // generate token
            return new ResponseEntity<>(Map.of("token", tokenService.generateToken(patient.getEmail())),
                    HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(Map.of("message", "Login failed due to an internal error"),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<Map<String, Object>> filterPatient(String condition, String name, String token) {
        try {
            String patientEmail = tokenService.extractEmail(token);
            Patient patient = patientRepository.findByEmail(patientEmail);
            if(patient == null) {
                return new ResponseEntity<>(Map.of("message", "Patient does not exist"), HttpStatus.NOT_FOUND);
            }
            Long patientId = patient.getId();
            if(condition != null && name != null) {
                return patientService.filterByDoctorAndCondition(condition, name, patientId);
            } else if(condition != null) {
                return patientService.filterByCondition(condition, patientId);
            } else if(name != null) {
                return patientService.filterByDoctor(name, patientId);
            } else {
                return patientService.getPatientAppointment(patientId, token);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(Map.of("message", "Internal server error"),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
