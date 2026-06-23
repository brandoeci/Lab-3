package wellness;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AppointmentGrpcServer {

    public static void main(String[] args) throws Exception {
        Server server = ServerBuilder.forPort(50060)
                .addService(new AppointmentServiceImpl())
                .build();

        server.start();
        System.out.println("AppointmentService gRPC iniciado en puerto 50060...");
        server.awaitTermination();
    }

    static class AppointmentServiceImpl extends AppointmentServiceGrpc.AppointmentServiceImplBase {

        private Map<String, Appointment> appointments = new HashMap<>();

        @Override
        public void requestAppointment(AppointmentRequest request,
                                       StreamObserver<AppointmentResponse> responseObserver) {

            String appointmentId = UUID.randomUUID().toString().substring(0, 8);

            Appointment appointment = Appointment.newBuilder()
                    .setAppointmentId(appointmentId)
                    .setStudentId(request.getStudentId())
                    .setServiceType(request.getServiceType())
                    .setDate(request.getDate())
                    .setStatus(Status.REQUESTED)
                    .build();

            appointments.put(appointmentId, appointment);

            AppointmentResponse response = AppointmentResponse.newBuilder()
                    .setAppointmentId(appointmentId)
                    .setStatus(Status.REQUESTED)
                    .setSuccess(true)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }

        @Override
        public void cancelAppointment(CancelRequest request,
                                      StreamObserver<CancelResponse> responseObserver) {

            Appointment appointment = appointments.get(request.getAppointmentId());
            boolean success = false;

            if (appointment != null && appointment.getStatus() == Status.REQUESTED) {
                Appointment cancelled = appointment.toBuilder()
                        .setStatus(Status.CANCELLED)
                        .build();
                appointments.put(request.getAppointmentId(), cancelled);
                success = true;
            }

            CancelResponse response = CancelResponse.newBuilder()
                    .setSuccess(success)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }

        @Override
        public void getAppointments(StudentRequest request,
                                    StreamObserver<AppointmentList> responseObserver) {

            List<Appointment> result = new ArrayList<>();
            for (Appointment a : appointments.values()) {
                if (a.getStudentId().equals(request.getStudentId())) {
                    result.add(a);
                }
            }

            AppointmentList list = AppointmentList.newBuilder()
                    .addAllAppointments(result)
                    .build();

            responseObserver.onNext(list);
            responseObserver.onCompleted();
        }
    }
}