# Section 1: Architecture summary
This application relies on two technologies for the user interfaces. It can depend on Thymeleaf templates and Spring MVC to handle the Admin and Doctor dashboards, and REST APIs to serve appointments, patient dashboard and records. These requests are handled by the respective controllers, which handle these requests by a common service layer. This layer interacts with two databases: MySQL via Spring Data JPA that stores Patient, Doctor, Admin and Appointment data using JPA entities; and MongoDB to store prescriptions in a document based model using Spring Data MongoDB.

![schema-architecture](/images/schema-architecture.png)

# Section 2: Numbered flow of data and control
### An example of how a request is processed
1. User accesses AdminDashboard or Appointment pages.
2. Depending on what the User does, clicking a button or submitting a form, and whether it is a request for server-rendered views or from the API consumer the appropriate controller is used.
3. The respective controller calls the service layer which handles the business rules, validations, and performs data access operations by communicating with the repository layer. 
4. These data access operations are passed on to the repositories, either MySQL (managed by Sprind Data JPA) or MongoDB (managed by Spring Data MongoDB), depending on the model.
5. The repository interface interacts with the database itself and the information is persisted or retrieved.
6. The retrieved data is mapped into Java classes, annotated with @Entity for MySQL or @Document for MongoDB.
7. And finally the models are passed from the controller to the user interface layer, where they are rendered as dynamic HTML (in case of Thymeleaf templates) or serialized into JSON and sent to the client (in case of REST API).