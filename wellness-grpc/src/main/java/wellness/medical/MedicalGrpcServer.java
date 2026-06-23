package wellness.medical;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MedicalGrpcServer {

    public static void main(String[] args) throws Exception {
        Server server = ServerBuilder.forPort(50061)
                .addService(new MedicalServiceImpl())
                .build();

        server.start();
        System.out.println("MedicalService gRPC iniciado en puerto 50061...");
        server.awaitTermination();
    }

    static class MedicalServiceImpl extends MedicalServiceGrpc.MedicalServiceImplBase {

        private Map<String, Specialty> specialties = new HashMap<>();

        public MedicalServiceImpl() {
            specialties.put("MED", Specialty.newBuilder()
                    .setCode("MED").setName("Medicina General").setAvailable(true).build());
            specialties.put("PSI", Specialty.newBuilder()
                    .setCode("PSI").setName("Psicologia").setAvailable(true).build());
            specialties.put("ODO", Specialty.newBuilder()
                    .setCode("ODO").setName("Odontologia").setAvailable(false).build());
        }

        @Override
        public void getSpecialties(EmptyRequest request,
                                   StreamObserver<SpecialtyList> responseObserver) {

            List<Specialty> result = new ArrayList<>(specialties.values());

            SpecialtyList list = SpecialtyList.newBuilder()
                    .addAllSpecialties(result)
                    .build();

            responseObserver.onNext(list);
            responseObserver.onCompleted();
        }

        @Override
        public void getSpecialty(SpecialtyRequest request,
                                 StreamObserver<Specialty> responseObserver) {

            Specialty specialty = specialties.get(request.getCode());

            if (specialty == null) {
                specialty = Specialty.newBuilder()
                        .setCode(request.getCode())
                        .setName("NO_ENCONTRADA")
                        .setAvailable(false)
                        .build();
            }

            responseObserver.onNext(specialty);
            responseObserver.onCompleted();
        }
    }
}