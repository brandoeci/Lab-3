package wellness.gateway;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import wellness.*;
import wellness.medical.EmptyRequest;
import wellness.medical.MedicalServiceGrpc;
import wellness.medical.Specialty;
import wellness.medical.SpecialtyList;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class WellnessGateway {

    private static AppointmentServiceGrpc.AppointmentServiceBlockingStub appointmentStub;
    private static MedicalServiceGrpc.MedicalServiceBlockingStub medicalStub;

    // Simulacion en memoria de GymService y RecreationService
    // (no implementados como microservicios independientes en este ejercicio)
    private static Map<String, String> gymReservations = new HashMap<>();
    private static Map<String, String> recreationReservations = new HashMap<>();

    public static void main(String[] args) {

        ManagedChannel appointmentChannel = ManagedChannelBuilder
                .forAddress("localhost", 50060)
                .usePlaintext()
                .build();
        appointmentStub = AppointmentServiceGrpc.newBlockingStub(appointmentChannel);

        ManagedChannel medicalChannel = ManagedChannelBuilder
                .forAddress("localhost", 50061)
                .usePlaintext()
                .build();
        medicalStub = MedicalServiceGrpc.newBlockingStub(medicalChannel);

        Scanner scanner = new Scanner(System.in);
        boolean salir = false;

        while (!salir) {
            System.out.println("\n--- WellnessGateway ---");
            System.out.println("1. requestAppointment");
            System.out.println("2. getStudentWellnessSummary");
            System.out.println("3. reserveGymSession");
            System.out.println("4. reserveRecreationResource");
            System.out.println("5. Salir");
            System.out.print("Seleccione una opcion: ");
            String opcion = scanner.nextLine();

            switch (opcion) {
                case "1":
                    requestAppointment(scanner);
                    break;
                case "2":
                    getStudentWellnessSummary(scanner);
                    break;
                case "3":
                    reserveGymSession(scanner);
                    break;
                case "4":
                    reserveRecreationResource(scanner);
                    break;
                case "5":
                    salir = true;
                    break;
                default:
                    System.out.println("Opcion invalida");
            }
        }

        appointmentChannel.shutdown();
        medicalChannel.shutdown();
        System.out.println("Gateway finalizado.");
    }

    // Operacion 1: requestAppointment(studentId, serviceType)
    private static void requestAppointment(Scanner scanner) {
        System.out.print("studentId: ");
        String studentId = scanner.nextLine();
        System.out.print("serviceType (MEDICINE, PSYCHOLOGY, DENTISTRY): ");
        String tipo = scanner.nextLine().toUpperCase();

        ServiceType serviceType;
        try {
            serviceType = ServiceType.valueOf(tipo);
        } catch (IllegalArgumentException e) {
            System.out.println("ERROR: tipo de servicio invalido");
            return;
        }

        AppointmentRequest request = AppointmentRequest.newBuilder()
                .setStudentId(studentId)
                .setServiceType(serviceType)
                .setDate("2026-07-01")
                .build();

        AppointmentResponse response = appointmentStub.requestAppointment(request);
        System.out.println("Cita creada: " + response.getAppointmentId()
                + " - Estado: " + response.getStatus());
    }

    // Operacion 2: getStudentWellnessSummary(studentId)
    // Combina informacion de AppointmentService y MedicalService en una sola respuesta
    private static void getStudentWellnessSummary(Scanner scanner) {
        System.out.print("studentId: ");
        String studentId = scanner.nextLine();

        StudentRequest studentRequest = StudentRequest.newBuilder()
                .setStudentId(studentId)
                .build();

        AppointmentList appointments = appointmentStub.getAppointments(studentRequest);

        SpecialtyList specialties = medicalStub.getSpecialties(EmptyRequest.newBuilder().build());

        System.out.println("\n=== Resumen de bienestar para " + studentId + " ===");

        System.out.println("Citas:");
        if (appointments.getAppointmentsList().isEmpty()) {
            System.out.println(" (sin citas registradas)");
        } else {
            for (Appointment a : appointments.getAppointmentsList()) {
                System.out.println(" - " + a.getAppointmentId() + " | "
                        + a.getServiceType() + " | " + a.getDate()
                        + " | " + a.getStatus());
            }
        }

        System.out.println("Especialidades disponibles en bienestar:");
        for (Specialty s : specialties.getSpecialtiesList()) {
            System.out.println(" - " + s.getName()
                    + (s.getAvailable() ? " (DISPONIBLE)" : " (NO DISPONIBLE)"));
        }

        String gym = gymReservations.get(studentId);
        System.out.println("Reserva de gimnasio: " + (gym != null ? gym : "ninguna"));

        String recreation = recreationReservations.get(studentId);
        System.out.println("Reserva de recurso recreativo: " + (recreation != null ? recreation : "ninguna"));
    }

    // Operacion 3: reserveGymSession(studentId, timeSlot)
    // GymService no esta implementado como microservicio independiente;
    // el Gateway simula la reserva en memoria para efectos de este ejercicio.
    private static void reserveGymSession(Scanner scanner) {
        System.out.print("studentId: ");
        String studentId = scanner.nextLine();
        System.out.print("timeSlot (ej. 2026-07-01 18:00): ");
        String timeSlot = scanner.nextLine();

        gymReservations.put(studentId, timeSlot);
        System.out.println("RESERVA_GIMNASIO_EXITOSA para " + studentId + " en " + timeSlot);
    }

    // Operacion 4: reserveRecreationResource(studentId, resourceId)
    // RecreationService no esta implementado como microservicio independiente;
    // el Gateway simula la reserva en memoria para efectos de este ejercicio.
    private static void reserveRecreationResource(Scanner scanner) {
        System.out.print("studentId: ");
        String studentId = scanner.nextLine();
        System.out.print("resourceId (ej. CANCHA_FUTBOL): ");
        String resourceId = scanner.nextLine();

        recreationReservations.put(studentId, resourceId);
        System.out.println("RESERVA_RECURSO_EXITOSA para " + studentId + " - " + resourceId);
    }
}