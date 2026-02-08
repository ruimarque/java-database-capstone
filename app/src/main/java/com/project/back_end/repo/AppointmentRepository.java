package com.project.back_end.repo;

import com.project.back_end.models.Appointment;
import jakarta.transaction.Transactional;
import org.springframework.cglib.core.Local;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {


    // does it need the @Query?
    @Query("SELECT a FROM Appointment a LEFT JOIN FETCH a.doctor d WHERE a.doctor.id = :doctorId " +
            "AND a.appointment_time BETWEEN :start AND :end")
    public List<Appointment> findByDoctorIdAndAppointmentTimeBetween(Long doctorId, LocalDateTime start,
        LocalDateTime end);

    //
    @Query("SELECT a FROM Appointment a LEFT JOIN FETCH a.patient p WHERE a.doctor.id = :doctorId " +
            "AND LOWER(p.name) LIKE LOWER(CONCAT('%', :patientName, '%')) AND a.appointment_time BETWEEN " +
            ":start AND :end")
    public List<Appointment> findByDoctorIdAndPatient_NameContainingIgnoreCaseAndAppointmentTimeBetween(
            Long doctorId, String patientName, LocalDateTime start, LocalDateTime end);

    @Modifying
    @Transactional
    public void deleteAllByDoctorId(Long doctorId);

    public List<Appointment> findByPatientId(Long patientId);

    public List<Appointment> findByPatientIdAndStatusOrderByAppointmentTimeAsc(Long patientId, int status);
    //public List<Appointment> findByPatient_IdAndStatusOrderByAppointmentTimeAsc(Long patientId, int status);

    @Query("SELECT a FROM Appointment a WHERE LOWER(a.doctor.name) LIKE LOWER(CONCAT('%', :doctorName, '%')) " +
            "AND a.patient.id = :patientId")
    public List<Appointment> filterByDoctorNameAndPatientId(String doctorName, Long patientId);

    @Query("SELECT a FROM Appointment a WHERE LOWER(d.name) LIKE LOWER(CONCAT('%', :doctorName, '%')) AND " +
            "a.patient.id = :patientId AND a.status = :status")
    public List<Appointment> filterByDoctorNameAndPatientIdAndStatus(String doctorName, Long patientId, int status);

    @Modifying
    @Transactional
    @Query("UPDATE Appointment a SET a.status = :status WHERE a.id = :id")
    void updateStatus(int status, Long id);
}
