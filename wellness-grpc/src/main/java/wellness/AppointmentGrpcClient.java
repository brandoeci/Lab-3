package wellness;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class AppointmentGrpcClient {

    public static void main(String[] args) {
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("localhost", 50060)
                .usePlaintext()
                .build();

        AppointmentServiceGrpc.AppointmentServiceBlockingStub stub =
                AppointmentServiceGrpc.newBlockingStub(channel);

        // 1. Solicitar una cita
        AppointmentRequest request = AppointmentRequest.newBuilder()
                .setStudentId("EST001")
                .setStudentName("Diego Ortiz")
                .setInstitutionalEmail("diego.ortiz@mail.escuelaing.edu.co")
                .setServiceType(ServiceType.PSYCHOLOGY)
                .setDate("2026-06-25")
                .build();

        AppointmentResponse response = stub.requestAppointment(request);
        System.out.println("Cita creada: " + response.getAppointmentId()
                + " - Estado: " + response.getStatus()
                + " - Exito: " + response.getSuccess());

        // 2. Consultar citas del estudiante
        StudentRequest studentRequest = StudentRequest.newBuilder()
                .setStudentId("EST001")
                .build();

        AppointmentList list = stub.getAppointments(studentRequest);
        System.out.println("\nCitas del estudiante EST001:");
        for (Appointment a : list.getAppointmentsList()) {
            System.out.println(" - " + a.getAppointmentId() + " | "
                    + a.getServiceType() + " | " + a.getDate()
                    + " | " + a.getStatus());
        }

        // 3. Cancelar la cita
        CancelRequest cancelRequest = CancelRequest.newBuilder()
                .setAppointmentId(response.getAppointmentId())
                .build();

        CancelResponse cancelResponse = stub.cancelAppointment(cancelRequest);
        System.out.println("\nCancelacion exitosa: " + cancelResponse.getSuccess());

        // 4. Confirmar que ya no aparece como activa
        AppointmentList listAfterCancel = stub.getAppointments(studentRequest);
        System.out.println("\nCitas del estudiante EST001 despues de cancelar:");
        for (Appointment a : listAfterCancel.getAppointmentsList()) {
            System.out.println(" - " + a.getAppointmentId() + " | "
                    + a.getServiceType() + " | " + a.getDate()
                    + " | " + a.getStatus());
        }

        channel.shutdown();
    }
}