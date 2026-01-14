import { openModal } from './components/modals.js';
import { getDoctors, filterDoctors, saveDoctor } from './services/doctorServices.js';
import { createDoctorCard } from './components/doctorCard.js';

document.addEventListener("DOMContentLoaded", () => {
    const addBtn = document.getElementById("addDocBtn");
    if(addBtn) {
        addBtn.addEventListener("click", () => {
            openModal('addDoctor');
        });
    }

    loadDoctorCards();

    document.getElementById("searchBar").addEventListener("input", filterDoctorsOnChange);
    document.getElementById("timeFilter").addEventListener("change", filterDoctorsOnChange);
    document.getElementById("specialtyFilter").addEventListener("change", filterDoctorsOnChange);
});

async function loadDoctorCards() {

    try {
        const data = await getDoctors();
        renderDoctorCards(data);
    } catch(error) {
        console.error("Could not render doctor cards");
    }
}

async function renderDoctorCards(doctors) {

    const contentDiv = document.getElementById("content");
    contentDiv.innerHTML = "";

    doctors.forEach((doc) => {
        const card = createDoctorCard(doc);
        contentDiv.append(card);
    });
}

async function filterDoctorsOnChange() {

    const searchBar = document.getElementById("searchBar").value.trim();
    const filterTime = document.getElementById("filterTime").value;
    const specialtyTime = document.getElementById("specialtyTime").value;

    const name = searchBar.length > 0 ? searchBar : null;
    const time = filterTime.length > 0 ? timeFilter : null;
    const specialty = specialtyFilter.length > 0 ? specialtyFilter : null;

    try {
        const data = await filterDoctors(name, time, specialty);
        if(data.length > 0) {
            renderDoctorCards(data);
        } else {
            document.getElementById("content").innerHTML = "<p>No doctors found with the given filters.</p>";
            console.log("Nothing");
        }

    } catch(error) {
        console.error("Failed to filter doctors:", error);
        alert("❌ An error occurred while filtering doctors.");
    }
}

window.adminAddDoctor = async function() {

    const name = document.getElementById("doctorName")?.value.trim();
    const email = document.getElementById("doctorEmail")?.value.trim();
    const password = document.getElementById("doctorPassword")?.value.trim();
    const phone = document.getElementById("doctorPhone")?.value.trim();
    const specialty = document.getElementById("specialization")?.value.trim();
    const availability = Array.from(
      document.querySelectorAll('input[name="availability"]:checked')
    ).map(cb => cb.value);

    if (!name || !email || !phone || !password || !specialty || !availability) {
        alert("Please fill in all fields.");
        return;
    }

    const token = localStorage.getItem("token");
    if(!token) {
        alert("Session has expired. Please log in again");
        window.location.href = "/";
        return;
    }

    const doctor = {
        name,
        email,
        phone,
        password,
        specialty,
        availability
    };

    try {
        const result = await saveDoctor(doctor, token);

        if (result.success) {
            alert("Doctor added successfully!");
            closeModal("addDoctor");
            loadDoctorCards();
        } else {
            alert(`Failed to add doctor: ${result.message}`);
        }
    } catch (err) {
        console.error("Error adding doctor:", err);
        alert("An error occurred. Please try again.");
    }
}


/*
  This script handles the admin dashboard functionality for managing doctors:
  - Loads all doctor cards
  - Filters doctors by name, time, or specialty
  - Adds a new doctor via modal form


  Attach a click listener to the "Add Doctor" button
  When clicked, it opens a modal form using openModal('addDoctor')


  When the DOM is fully loaded:
    - Call loadDoctorCards() to fetch and display all doctors


  Function: loadDoctorCards
  Purpose: Fetch all doctors and display them as cards

    Call getDoctors() from the service layer
    Clear the current content area
    For each doctor returned:
    - Create a doctor card using createDoctorCard()
    - Append it to the content div

    Handle any fetch errors by logging them


  Attach 'input' and 'change' event listeners to the search bar and filter dropdowns
  On any input change, call filterDoctorsOnChange()


  Function: filterDoctorsOnChange
  Purpose: Filter doctors based on name, available time, and specialty

    Read values from the search bar and filters
    Normalize empty values to null
    Call filterDoctors(name, time, specialty) from the service

    If doctors are found:
    - Render them using createDoctorCard()
    If no doctors match the filter:
    - Show a message: "No doctors found with the given filters."

    Catch and display any errors with an alert


  Function: renderDoctorCards
  Purpose: A helper function to render a list of doctors passed to it

    Clear the content area
    Loop through the doctors and append each card to the content area


  Function: adminAddDoctor
  Purpose: Collect form data and add a new doctor to the system

    Collect input values from the modal form
    - Includes name, email, phone, password, specialty, and available times

    Retrieve the authentication token from localStorage
    - If no token is found, show an alert and stop execution

    Build a doctor object with the form values

    Call saveDoctor(doctor, token) from the service

    If save is successful:
    - Show a success message
    - Close the modal and reload the page

    If saving fails, show an error message
*/
