# Taller Integrador ARSW 2026-I — Evolución de Arquitecturas Distribuidas

**Autor:** Diego Ortiz
**Curso:** Arquitecturas de Software (ARSW) — Escuela Colombiana de Ingeniería Julio Garavito
**Caso evolucionado:** Sistema de Gestión de Salones / Inventario de Laboratorios / Bienestar Universitario

---

## Estructura del repositorio

```
CallReturnLab3/
├── salontcp/          → Parte I:  Sockets TCP (Gestión de Salones)
├── salonhttp/         → Parte II: HTTP (Gestión de Salones)
├── lab-rmi/           → Parte III: RMI (Inventario de Laboratorios)
└── wellness-grpc/     → Parte IV, V y VI: gRPC, Microservicios, API Gateway
    ├── src/main/proto/
    │   ├── appointment.proto
    │   └── medical.proto
    └── src/main/java/wellness/
        ├── AppointmentGrpcServer.java
        ├── AppointmentGrpcClient.java
        ├── WellnessClient.java
        ├── medical/MedicalGrpcServer.java
        └── gateway/WellnessGateway.java
```

El punto VII (ECICIENCIA) es un ejercicio solo de diseño, no requiere código — su documentación está al final de este README.

---

## Idea general del taller

Todo el taller toma un mismo problema (gestionar algo: salones, equipos, citas) y lo resuelve seis veces, cada vez con una tecnología de comunicación distinta. La intención no es que cada parte sea un ejercicio aislado, sino mostrar cómo cada estilo arquitectónico resuelve una limitación del anterior, pero introduce una limitación nueva.

---

## Parte I — Sockets TCP (`salontcp/`)

### Qué hace
Un servidor TCP que mantiene 4 salones (`E301`–`E304`) en memoria, cada uno disponible o reservado. El cliente envía comandos de texto plano y el servidor responde también en texto plano.

### Protocolo diseñado
```
CONSULTAR_SALON,E303   →  SALON_DISPONIBLE / SALON_RESERVADO
RESERVAR_SALON,E303    →  RESERVA_EXITOSA / SALON_RESERVADO
LIBERAR_SALON,E303     →  LIBERACION_EXITOSA / SALON_DISPONIBLE
(código inexistente)   →  ERROR_SALON_NO_EXISTE
(comando mal formado)  →  ERROR_OPERACION_INVALIDA
```

### Cómo correrlo
```
cd salontcp
javac *.java
java SalonServer        # terminal 1, puerto 36000
java SalonClient        # terminal 2
```

### Decisión de diseño clave
El repositorio (`SalonRepository`) usa métodos `synchronized` para que, si dos clientes intentan reservar el mismo salón al mismo tiempo, solo uno gane la reserva — sin esto habría una condición de carrera.

### Reflexiones
- **¿Qué tan fácil es agregar una operación nueva?** Hay que tocar tanto el cliente como el servidor y ponerse de acuerdo manualmente en el nuevo formato de texto — no hay nada que valide o documente el protocolo automáticamente.
- **¿Qué pasa si dos clientes reservan a la vez?** Sin sincronización habría una condición de carrera (ambos podrían "ganar" la reserva). Por eso el repositorio usa `synchronized`.
- **¿Dónde vive el contrato?** En ninguna parte formal — son solo convenciones de texto que están en la cabeza del programador.

---

## Parte II — HTTP (`salonhttp/`)

### Qué hace
El mismo dominio de salones, pero expuesto como rutas HTTP usando `com.sun.net.httpserver.HttpServer` (sin frameworks como Spring).

### Rutas implementadas
```
GET  /rooms                      → lista todos los salones
GET  /rooms?id=E303               → consulta uno específico
POST /rooms/reserve?id=E303       → reserva
POST /rooms/release?id=E303       → libera
```

### Cómo correrlo
```
cd salonhttp
javac *.java
java SalonHttpServer     # puerto 8081
```

Probar con navegador (las rutas GET):
```
http://localhost:8081/rooms
http://localhost:8081/rooms?id=E303
```

Probar con curl (las rutas POST):
```
curl -X POST http://localhost:8081/rooms/reserve?id=E303
curl -X POST http://localhost:8081/rooms/release?id=E303
```

### Reflexiones
- **¿Qué ventaja tiene HTTP sobre un protocolo manual?** Cualquier cliente (navegador, curl, Postman, otro lenguaje) puede consumirlo porque el protocolo ya es un estándar universal — no hay que escribir un cliente Java a medida como en TCP.
- **¿Qué limita construirlo sin framework?** Hay que parsear manualmente query strings, validar métodos HTTP y manejar códigos de estado a mano — tareas que un framework (Spring, Express) resolvería solo.
- **¿Cómo cambiaría con JSON en vez de HTML?** En vez de construir strings `<html>...`, se devolverían objetos como `{"codigo":"E303","estado":"DISPONIBLE"}`, mucho más apropiado para que otro programa lo consuma, normalmente usando una librería como Gson.

---

## Parte III — RMI (`lab-rmi/`)

### Qué hace
Sistema de inventario de equipos de laboratorio, donde el cliente invoca métodos remotos directamente sobre un objeto servidor — sin diseñar ningún protocolo de texto.

### Interfaz remota (el contrato)
```java
List<String> consultarEquipos()
String consultarEquipo(String codigo)
boolean reservarEquipo(String codigo)
boolean liberarEquipo(String codigo)
```

### Cómo correrlo
```
cd lab-rmi
javac *.java
java LaboratorioRmiServer    # terminal 1, puerto 24000
java LaboratorioRmiClient    # terminal 2, menú interactivo
```

### Reflexiones
- **¿Qué cambió al pasar de HTTP a RMI?** Ya no hay protocolo textual que diseñar: el cliente invoca métodos Java normales y RMI se encarga de serializar la llamada, transportarla y devolver el resultado tipado. Se siente como una llamada local.
- **¿Dónde está el contrato?** En la interfaz `LaboratorioService.java` — el compilador obliga a que cliente y servidor la respeten, a diferencia de TCP/HTTP donde el contrato vivía en convenciones externas.
- **¿Qué pasa si el cliente no es Java?** No funciona. RMI depende de la serialización nativa de objetos de Java y de los stubs generados por la JVM — un cliente en Python o C# no puede hacer `lookup()` en el RMI Registry.

---

## Parte IV — gRPC (`wellness-grpc/`)

### Qué hace
Servicio de citas de bienestar universitario (`AppointmentService`) usando contratos definidos en `.proto` y Protocol Buffers. Entidades: `Student`, `Appointment`, `ServiceType` (MEDICINE/PSYCHOLOGY/DENTISTRY), `Status` (REQUESTED/CANCELLED/ATTENDED).

### Contrato (`appointment.proto`)
```protobuf
service AppointmentService {
    rpc RequestAppointment (AppointmentRequest) returns (AppointmentResponse);
    rpc CancelAppointment (CancelRequest) returns (CancelResponse);
    rpc GetAppointments (StudentRequest) returns (AppointmentList);
}
```

### Cómo correrlo
```
cd wellness-grpc
mvn clean compile
mvn exec:java "-Dexec.mainClass=wellness.AppointmentGrpcServer"   # terminal 1, puerto 50060
mvn exec:java "-Dexec.mainClass=wellness.AppointmentGrpcClient"   # terminal 2
```

El cliente ejecuta automáticamente: solicita una cita, consulta las citas del estudiante, cancela la cita y vuelve a consultar para confirmar que ya no aparece como activa.

### Reflexiones
- **¿Por qué el `.proto` es un contrato?** Define explícitamente qué servicios, métodos y mensajes existen, y el compilador obliga a respetarlo — no son convenciones informales como en TCP.
- **¿Qué tan fácil es crear un cliente en otro lenguaje?** Muy fácil: el mismo `.proto` se compila con `protoc` hacia Python, Go, C#, etc. — a diferencia de RMI, gRPC es multiplataforma por diseño.
- **¿Diferencias RMI vs gRPC?** RMI depende totalmente de la JVM y de serialización Java; gRPC usa Protocol Buffers (binario, eficiente, agnóstico al lenguaje) y corre sobre HTTP/2, lo que habilita streaming bidireccional que RMI no tiene.

---

## Parte V — Microservicios (`wellness-grpc/`)

### Qué hace
Separa el dominio de bienestar en dos servicios independientes, cada uno en su propio puerto:

| Servicio | Responsabilidad | Puerto |
|---|---|---|
| `AppointmentService` | Gestionar citas y turnos | 50060 |
| `MedicalService` | Consultar especialidades médicas disponibles | 50061 |

`GymService` y `RecreationService` quedaron diseñados conceptualmente (no implementados), lo cual cumple el mínimo del ejercicio (dos servicios reales).

### Cómo correrlo
```
mvn exec:java "-Dexec.mainClass=wellness.AppointmentGrpcServer"          # terminal 1
mvn exec:java "-Dexec.mainClass=wellness.medical.MedicalGrpcServer"      # terminal 2
mvn exec:java "-Dexec.mainClass=wellness.WellnessClient"                 # terminal 3
```

`WellnessClient` abre dos canales gRPC distintos (uno por puerto) y consume ambos servicios directamente, evidenciando el problema que resuelve el siguiente punto (el Gateway).

### Reflexiones
- **¿Por qué separar esos servicios y no otros?** Tienen responsabilidades de dominio claramente distintas: gestionar citas (con ciclo de vida REQUESTED/CANCELLED/ATTENDED) es un problema distinto a mantener un catálogo de especialidades.
- **¿Qué datos pertenecen a cada uno?** `AppointmentService` es dueño de las citas; `MedicalService` es dueño del catálogo de especialidades — ninguno necesita conocer la estructura interna del otro.
- **¿Qué riesgo aparece si el cliente conoce todos los servicios?** Queda acoplado a direcciones y puertos individuales (se ve en `WellnessClient`, que abre dos canales). Si un servicio cambia de puerto o se agrega un tercero, hay que modificar el cliente — justo lo que resuelve el Gateway.

---

## Parte VI — API Gateway (`wellness-grpc/`)

### Qué hace
`WellnessGateway` centraliza el acceso a los dos microservicios anteriores detrás de un menú de consola, simulando además `GymService` y `RecreationService` en memoria (ya que no están implementados como microservicios reales).

### Operaciones expuestas
```
requestAppointment(studentId, serviceType)
getStudentWellnessSummary(studentId)     ← combina datos de AppointmentService + MedicalService
reserveGymSession(studentId, timeSlot)
reserveRecreationResource(studentId, resourceId)
```

### Cómo correrlo
Necesita los dos microservicios corriendo (igual que en la Parte V), más:
```
mvn exec:java "-Dexec.mainClass=wellness.gateway.WellnessGateway"   # terminal 3
```

### Reflexiones
- **¿Qué simplifica el Gateway para el cliente?** El cliente final solo habla con el Gateway — no necesita saber que existen varios microservicios en puertos distintos, ni sus contratos individuales.
- **¿Qué complejidad agrega?** Se vuelve un punto único de fallo: si el Gateway se cae, el cliente pierde acceso a todo el sistema, aunque los microservicios individuales sigan funcionando.
- **¿Qué pasa si el Gateway acumula demasiada lógica de negocio?** Deja de ser un simple punto de entrada y se convierte en otro monolito disfrazado, perdiendo la separación de responsabilidades que se buscaba con los microservicios.

---

## Parte VII — Ejercicio Integrador: Plataforma ECICIENCIA (solo diseño)

Este punto no requiere código implementado, solo el diseño justificado.

### 1. Microservicios identificados

| Servicio | Responsabilidad | Puerto sugerido |
|---|---|---|
| `RegistrationService` | Registrar asistentes (datos, tipo de asistente) | 50070 |
| `AgendaService` | Consultar agenda y filtrar por franja horaria | 50071 |
| `WorkshopService` | Reserva de cupos en talleres y control de aforo | 50072 |
| `AttendanceService` | Registrar asistencia (check-in) a actividades | 50073 |

### 2. Justificación de la separación
- **RegistrationService**: dueño del ciclo de vida del asistente, dominio independiente de cualquier actividad.
- **AgendaService**: de solo lectura desde la perspectiva del asistente, cambia con poca frecuencia.
- **WorkshopService**: requiere consistencia fuerte (evitar sobrecupo bajo concurrencia) — se aísla para que un error ahí no afecte a los demás.
- **AttendanceService**: alta frecuencia de escritura en ráfagas el día del evento (check-ins masivos), un patrón de carga muy distinto al de los otros tres.

### 3. Contrato gRPC principal — `workshop.proto`
```protobuf
syntax = "proto3";

option java_multiple_files = true;
option java_package = "eciencia.workshop";
option java_outer_classname = "WorkshopProto";

service WorkshopService {
    rpc GetWorkshops (EmptyRequest) returns (WorkshopList);
    rpc ReserveSlot (ReserveRequest) returns (ReserveResponse);
    rpc GetWorkshopCapacity (WorkshopRequest) returns (CapacityResponse);
}

message EmptyRequest {
}

message WorkshopRequest {
    string workshopId = 1;
}

message Workshop {
    string workshopId = 1;
    string title = 2;
    string timeSlot = 3;
    int32 capacity = 4;
    int32 reserved = 5;
}

message WorkshopList {
    repeated Workshop workshops = 1;
}

message ReserveRequest {
    string workshopId = 1;
    string attendeeId = 2;
}

message ReserveResponse {
    bool success = 1;
    string message = 2;
}

message CapacityResponse {
    int32 capacity = 1;
    int32 reserved = 2;
    int32 available = 3;
}
```

### 4. Descripción del API Gateway — `ECICIENCIAGateway`

**Responsabilidades:**
- Recibir solicitudes del cliente (app web/móvil del asistente)
- Mantener canales gRPC abiertos hacia los cuatro microservicios internos
- Combinar respuestas cuando una operación necesita datos de más de un servicio
- Ocultar del cliente los puertos y direcciones internas

**Operaciones expuestas:**
```
registerAttendee(name, email, attendeeType)
getAgenda(timeSlot)
reserveWorkshopSlot(attendeeId, workshopId)
getAttendeeSummary(attendeeId)   ← combina registro + reservas + asistencia
checkIn(attendeeId, activityId)
```

`getAttendeeSummary` es el caso más representativo: combina `RegistrationService`, `WorkshopService` y `AttendanceService` en una sola respuesta unificada.

### 5. Diagrama de arquitectura
```
Asistente (App / Web)
 |
 v
ECICIENCIAGateway
 |
 +---- RegistrationService   (puerto 50070) - registro de asistentes
 +---- AgendaService         (puerto 50071) - consulta de agenda
 +---- WorkshopService       (puerto 50072) - reservas y control de aforo
 +---- AttendanceService     (puerto 50073) - check-in de actividades
```

### 6. Por qué no un monolito
Un monolito concentraría responsabilidades con patrones de carga muy distintos: control de aforo (necesita consistencia fuerte ante concurrencia) y check-in (necesita alta disponibilidad en ráfagas el día del evento). Si todo viviera en un solo proceso, un pico de tráfico en check-ins podría degradar el control de aforo, o un bug en registro podría tumbar todo el sistema el día del evento. Separar en microservicios permite escalar, desplegar y depurar cada responsabilidad de forma independiente, limitando el alcance del daño si uno falla.

### 7. Reflexión final del taller

A lo largo del taller, el sistema evolucionó por seis estilos arquitectónicos, cada uno resolviendo una limitación del anterior.

Con sockets TCP, la comunicación quedó completamente en manos del desarrollador: formato del mensaje, parsing, validación y errores eran responsabilidad explícita del código, sin ningún estándar que lo respaldara — el contrato vivía solo en convenciones de texto.

Pasar a HTTP resolvió la interoperabilidad: cualquier cliente pudo consumir el servicio sin un cliente Java a medida, porque el protocolo ya era un estándar universal, aunque persistía el reto de construir rutas y manejar respuestas sin un framework.

RMI cambió el paradigma: el cliente invoca métodos remotos directamente y el contrato pasó a vivir en una interfaz Java formal, a costa de una dependencia total del ecosistema Java.

gRPC resolvió esa limitación usando Protocol Buffers: el contrato (`.proto`) se volvió independiente del lenguaje, permitiendo generar clientes en cualquier tecnología, manteniendo tipado fuerte y eficiencia.

Separar el sistema en microservicios permitió aislar responsabilidades en unidades pequeñas y cohesivas, desplegables de forma independiente, pero introdujo el riesgo de que el cliente terminara conociendo demasiados servicios y puertos.

Finalmente, el API Gateway centralizó ese acceso, ocultando al cliente la existencia de múltiples servicios, a costa de convertirse en un punto crítico de fallo y en un posible imán de lógica de negocio que debería vivir en los microservicios.

Ninguna arquitectura es universalmente superior: cada estilo resuelve un problema específico a costa de una nueva limitación, y la elección depende del contexto: tamaño del equipo, necesidad de interoperabilidad, nivel de control requerido y tolerancia a la complejidad operacional.

---

## Tabla comparativa de estilos

| Estilo | Ventaja principal | Limitación principal |
|---|---|---|
| Sockets TCP | Control total sobre la comunicación | Protocolo manual y bajo nivel |
| HTTP | Interoperabilidad, acceso desde cualquier cliente | Hay que diseñar rutas y respuestas a mano |
| RMI | Invocación remota de métodos en Java | Dependencia fuerte del ecosistema Java |
| gRPC | Contratos formales, eficiencia, multiplataforma | Requiere configuración de protobuf |
| Microservicios | Separación de responsabilidades y escalabilidad | Mayor complejidad operacional |
| API Gateway | Punto de entrada único para el cliente | Puede ser cuello de botella o punto único de fallo |
