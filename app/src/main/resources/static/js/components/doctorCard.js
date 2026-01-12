import { deleteDoctor } from '../services/doctorServices.js';
import { showBookingOverlay } from '../loggedPatient.js';
import { fetchPatientDetails } from '../services/patientServices.js';

export function createDoctorCard(doctor) {

    const role = localStorage.getItem("userRole");
    const token = localStorage.getItem("token");

    const card = document.createElement("div");
    card.classList.add("doctor-card"); // <div class="doctor-card">

    const infoDiv = document.createElement("div");
    infoDiv.classList.add("doctor-info"); // <div class="doctor-info">

    const name = document.createElement("h3");
    name.textContent = doctor.name; // <h3>doctor.name</>

    const specialty = document.createElement("h3");
    specialty.textContent = doctor.specialty; // <h3>doctor.specialty</>

    const email = document.createElement("h3");
    email.textContent = doctor.email; // <h3>doctor.email</>

    const timeList = document.createElement("ul");
    timeList.classList.add("availability-list"); // <ul class="availability-list>
    doctor.availableTimes?.forEach(time => {
        const li = document.createElement("li");
        li.textContent = time; //<li>time</li>
        timeList.appendChild(li);
    });

    infoDiv.appendChild(name);
    infoDiv.appendChild(specialization);
    infoDiv.appendChild(email);
    infoDiv.appendChild(timeList);

    const actionsDiv = document.createElement("div");
    actionsDiv.classList.add("card-actions");

    const actions = document.createElement("div");
    actions.classList.add("doctor-actions");

    if(role === "admin") {
        const removeBtn = document.createElement("button");
        removeBtn.textContent = "Delete";

        removeBtn.addEventListener("click", async () => {
          if (!token) {
              alert("Admin session expired. Please log in again.");
              window.location.href = "/";
              return;
          }
          // 1. Confirm deletion
          if(!confirm(`Are you sure you want to delete Dr. ${doctor.name}?`))
                return;
          // 2. Get token from localStorage
          // 3. Call API to delete
          try {
                const success = await deleteDoctor(doctor.id, token);
                // 4. On success: remove the card from the DOM
                if(success) {
                    alert("Doctor deleted successfully");
                    card.remove();
                } else {
                    alert("Failed to delete doctor");
                }
          } catch (error) {
                console.error(error);
                alert("Error deleting doctor");
          }
        });

        actions.appendChild(removeBtn);
    } else if(role === "patient") {
        const bookNow = document.createElement("button");
        bookNow.textContent = "Book Now";
        bookNow.addEventListener("click", () => {
            alert("Patient needs to login first.");
        });
    } else if(role === "loggedPatient") {
        const bookNow = document.createElement("button");
        bookNow.textContent = "Book Now";
        bookNow.addEventListener("click", async (e) => {
            if (!token) {
                alert("Patient session expired. Please log in again.");
                window.location.href = "/pages/patientDashboard.html";
                return;
            }

            try {
                const patientData = await getPatientData(token);
                showBookingOverlay(e, doctor, patientData);
            } catch (error) {
                console.error(error);
                alert("Error fetching patient details");
            }
        });
    }

    card.appendChild(infoDiv);
    card.appendChild(actionsDiv);

    return card;
}

/*
Import the overlay function for booking appointments from loggedPatient.js

  Import the deleteDoctor API function to remove doctors (admin role) from docotrServices.js

  Import function to fetch patient details (used during booking) from patientServices.js

  Function to create and return a DOM element for a single doctor card
    Create the main container for the doctor card
    Retrieve the current user role from localStorage
    Create a div to hold doctor information
    Create and set the doctor’s name
    Create and set the doctor's specialization
    Create and set the doctor's email
    Create and list available appointment times
    Append all info elements to the doctor info container
    Create a container for card action buttons
    === ADMIN ROLE ACTIONS ===
      Create a delete button
      Add click handler for delete button
     Get the admin token from localStorage
        Call API to delete the doctor
        Show result and remove card if successful
      Add delete button to actions container
   
    === PATIENT (NOT LOGGED-IN) ROLE ACTIONS ===
      Create a book now button
      Alert patient to log in before booking
      Add button to actions container
  
    === LOGGED-IN PATIENT ROLE ACTIONS === 
      Create a book now button
      Handle booking logic for logged-in patient   
        Redirect if token not available
        Fetch patient data with token
        Show booking overlay UI with doctor and patient info
      Add button to actions container
   
  Append doctor info and action buttons to the car
  Return the complete doctor card element
*/
