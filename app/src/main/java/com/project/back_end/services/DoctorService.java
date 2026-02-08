package com.project.back_end.services;

import com.project.back_end.DTO.Login;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Doctor;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.DoctorRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DoctorService {

    private DoctorRepository doctorRepository;
    private AppointmentRepository appointmentRepository;
    private TokenService tokenService;

    public DoctorService(DoctorRepository doctorRepository,
                         AppointmentRepository appointmentRepository,
                         TokenService tokenService) {
        this.doctorRepository = doctorRepository;
        this.appointmentRepository = appointmentRepository;
        this.tokenService = tokenService;
    }

    public List<String> getDoctorAvailability(Long doctorId, LocalDate date) {
        Optional<Doctor> optionalDoctor = doctorRepository.findById(doctorId);
        if(optionalDoctor.isEmpty()) {
            return new ArrayList<>();
        }

        Doctor existingDoctor = optionalDoctor.get();
        List<String> allDoctorSlots = existingDoctor.getAvailableTimes();

        List<Appointment> bookedDoctorAppointments = appointmentRepository.findByDoctorIdAndAppointmentTimeBetween(
                doctorId,
                date.atStartOfDay(),
                date.plusDays(1).atStartOfDay());

        // Does it really need a set because of duplicate elements?
        // It is not supposed that each doctor will not have the same time slot in a day?
        Set<String> bookedDoctorSlots = bookedDoctorAppointments.stream()
                .map(app -> app.getAppointmentTimeOnly().toString()) // or format as needed
                .collect(Collectors.toSet());

        return allDoctorSlots.stream()
                .filter(slot -> !bookedDoctorSlots.contains(slot))
                .sorted()
                .collect(Collectors.toList());
    }

    @Transactional
    public int saveDoctor(Doctor doctor) {
        //if(doctorRepository.existsById(doctor.getId())) // does this do what I think it does?
        if(doctorRepository.findByEmail(doctor.getEmail()) != null) {
            return -1;
        }
        try {
            doctorRepository.save(doctor);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    @Transactional
    public int updateDoctor(Doctor doctor) {
        Optional<Doctor> optionalDoctor = doctorRepository.findById(doctor.getId());
        if(optionalDoctor.isEmpty()) {
            return -1;
        }

        Doctor existingDoctor = optionalDoctor.get();
        existingDoctor.setName(doctor.getName());
        existingDoctor.setEmail(doctor.getEmail());
        existingDoctor.setPhone(doctor.getPhone());
        existingDoctor.setSpecialty(doctor.getSpecialty());
        existingDoctor.setAvailableTimes(doctor.getAvailableTimes());

        doctorRepository.save(existingDoctor);
        return 1;
    }

    public List<Doctor> getDoctors() {
        return doctorRepository.findAll();
    }

    @Transactional
    public int deleteDoctor(Long id) {
        if(!doctorRepository.existsById(id)) {
            return -1;
        }

        try {
            appointmentRepository.deleteAllByDoctorId(id);
            doctorRepository.deleteById(id);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    public ResponseEntity<Map<String, String>> validateDoctor(Login login) {
        Doctor doctor = doctorRepository.findByEmail(login.getEmail());
        if(doctor == null || !doctor.getPassword().equals(login.getPassword())) {
            return new ResponseEntity<>(Map.of("message", "Invalid email or password"),
                    HttpStatus.UNAUTHORIZED);
        }

        return new ResponseEntity<>(Map.of("token", tokenService.generateToken(doctor.getEmail())),
                HttpStatus.OK);
    }

    public Map<String, Object> findDoctorsByName(String name) {
        return Map.of("doctors", doctorRepository.findByNameLike(name));
    }

    public Map<String, Object> filterDoctorsByNameSpecialtyandTime(String name, String specialty, String amOrPm) {
        List<Doctor> doctorsWithNameAndSpecialty =
                doctorRepository.findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase(name, specialty);

        if(doctorsWithNameAndSpecialty.isEmpty()) {
            return Map.of("doctors", doctorsWithNameAndSpecialty);
            //return Map.of("message", "There are no doctors with that name and/or specialty");
        }

        return Map.of("doctors", filterDoctorByTime(doctorsWithNameAndSpecialty, amOrPm)); //what if filter comes back empty?
    }

    public Map<String, Object> filterDoctorByNameAndTime(String name, String amOrPm) {
        List<Doctor> doctorsWithName = doctorRepository.findByNameLike(name);

        if(doctorsWithName.isEmpty()) {
            return Map.of("doctors", doctorsWithName);
            //return Map.of("message", "There are no doctors with that name");
        }

        return Map.of("doctors", filterDoctorByTime(doctorsWithName, amOrPm));
    }

    public Map<String, Object> filterDoctorByNameAndSpecialty(String name, String specialty) {
        return Map.of("doctors",
                doctorRepository.findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase(name, specialty));
    }

    public Map<String, Object> filterDoctorByTimeAndSpecialty(String specialty, String amOrPm) {
        return Map.of("doctors", filterDoctorByTime(doctorRepository.findBySpecialtyIgnoreCase(specialty), amOrPm));
    }

    public Map<String, Object> filterDoctorBySpecialty(String specialty) {
        return Map.of("doctors", doctorRepository.findBySpecialtyIgnoreCase(specialty));
    }

    public Map<String, Object> filterDoctorsByTime(String amOrPm) {
        return Map.of("doctors", filterDoctorByTime(doctorRepository.findAll(), amOrPm));
    }

    private List<Doctor> filterDoctorByTime(List<Doctor> doctors, String amOrPm) {
        return doctors.stream()
                .filter(
                        doctor -> doctor.getAvailableTimes().stream()
                        .anyMatch(
                                timeStr -> {
                                    LocalTime time = LocalTime.parse(timeStr);
                                    return amOrPm.equalsIgnoreCase("AM") ? time.isBefore(LocalTime.NOON)
                                            : time.isAfter(LocalTime.NOON);
                                }
                        )
                )
                .collect(Collectors.toList());
    }
}
