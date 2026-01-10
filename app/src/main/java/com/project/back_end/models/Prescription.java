package com.project.back_end.models;

import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "prescriptions")
public class Prescription {

    @Id
    private String id;

    @NotNull(message = "Patient name is required")
    @Size(min = 3, max = 100, message = "Patient name must be within 3-100 characters")
    private String patientName;

    @NotNull(message = "Appointment ID is required")
    private Long appointmentId;

    @NotNull(message = "Medication name is required")
    @Size(min = 3, max = 100, message = "Medication name must be within 3-100 characters")
    private String medication;

    @NotNull(message = "Dosage details are required")
    @Size(min = 3, max = 20, message = "Dosage details must be within 3-20 characters")
    private String dosage;

    @Size(max = 200, message = "Doctor notes must be at most 200 characters")
    private String doctorNotes;

    // Constructors
    public Prescription() {}
    public Prescription(String patientName, Long appointmentId, String medication, String dosage, String doctorNotes) {
        this.patientName = patientName;
        this.appointmentId = appointmentId;
        this.medication = medication;
        this.dosage = dosage;
        this.doctorNotes = doctorNotes;
    }

    // Getters and setters
    public String getId() {
        return id;
    }
    public String getPatientName() {
        return patientName;
    }
    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }
    public Long getAppointmentId() {
        return appointmentId;
    }
    public void setAppointmentId(Long appointmentId) {
        this.appointmentId = appointmentId;
    }
    public String getMedication() {
        return medication;
    }
    public void setMedication(String medication) {
        this.medication = medication;
    }
    public String getDosage() {
        return dosage;
    }
    public void setDosage(String dosage) {
        this.dosage = dosage;
    }
    public String getDoctorNotes() {
        return doctorNotes;
    }
    public void setDoctorNotes(String doctorNotes) {
        this.doctorNotes = doctorNotes;
    }
}
