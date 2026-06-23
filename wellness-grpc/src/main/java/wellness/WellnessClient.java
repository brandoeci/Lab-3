package wellness;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import wellness.medical.EmptyRequest;
import wellness.medical.MedicalServiceGrpc;
import wellness.medical.Specialty;
import wellness.medical.SpecialtyList;

public class WellnessClient {

    public static void main(String[] args) {

        // Canal hacia AppointmentService (puerto 50060)
        ManagedChannel appointmentChannel = ManagedChannelBuilder
                .forAddress("localhost", 50060)
                .usePlaintext()
                .build();

        AppointmentServiceGrpc.AppointmentServiceBlockingStub appointmentStub =
                AppointmentServiceGrpc.newBlockingStub(appointmentChannel);

        // Canal hacia MedicalService (puerto 50061)
        ManagedChannel medicalChannel = ManagedChannelBuilder
                .forAddress("localhost", 50061)
                .usePlaintext()
                .build();

        MedicalServiceGrpc.MedicalServiceBlockingStub medicalStub =
                MedicalServiceGrpc.newBlockingStub(medicalChannel);

        // 1. Consultar especialidades disponibles en MedicalService
        SpecialtyList specialties = medicalStub.getSpecialties(EmptyRequest.newBuilder().build());
        System.out.println("Especialidades disponibles:");
        for (Specialty s : specialties.getSpecialtiesList()) {
            System.out.println(" - " + s.getCode() + ": " + s.getName()
                    + " (" + (s.getAvailable() ? "DISPONIBLE" : "NO DISPONIBLE") + ")");
        }

        // 2. Solicitar una cita usando AppointmentService
        AppointmentRequest request = AppointmentRequest.newBuilder()
                .setStudentId("EST002")
                .setStudentName("Maria Lopez")
                .setInstitutionalEmail("maria.lopez@mail.escuelaing.edu.co")
                .setServiceType(ServiceType.MEDICINE)
                .setDate("2026-06-30")
                .build();

        AppointmentResponse response = appointmentStub.requestAppointment(request);
        System.out.println("\nCita creada: " + response.getAppointmentId()
                + " - Estado: " + response.getStatus());

        appointmentChannel.shutdown();
        medicalChannel.shutdown();
    }
}