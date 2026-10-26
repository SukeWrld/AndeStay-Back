# AndeStay-Back
BFF en Spring Boot con Spring Security y validación de JWT Azure AD

## RabbitMQ (servicio de reservas)

El servicio se conecta al broker remoto de CloudAMQP mediante su URL AMQPS.
Configura la variable de entorno `CLOUDAMQP_URL` con la URL de conexión que
entrega CloudAMQP en el entorno donde despliegues la aplicación. Guárdala como
secreto del entorno de despliegue; no la subas al repositorio ni la compartas,
porque incluye credenciales.

Inicia el servicio desde la carpeta `ms-andesstay-reservations` con
`.\mvnw.cmd spring-boot:run`. La variable `CLOUDAMQP_URL` debe estar configurada
antes de iniciarlo.
El servicio publica mensajes JSON en el exchange durable `andesstay.reservas`:

| Acción | Routing key | Tipo de evento |
| --- | --- | --- |
| Se crea una reserva | `reserva.creada` | `RESERVA_CREADA` |
| Cambia el estado de una reserva | `reserva.estado-cambiado` | `RESERVA_ESTADO_CAMBIADO` |

Ambos tipos quedan enlazados a la cola durable `andesstay.reservas.events`. El
payload incluye un identificador único de evento, el ID de la reserva, unidad,
estado anterior/nuevo y fechas; no incluye datos personales del huésped.
