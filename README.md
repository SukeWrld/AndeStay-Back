# AndeStay-Back
BFF en Spring Boot con Spring Security y validación de JWT Azure AD
<<<<<<< HEAD

## RabbitMQ (servicio de reservas)

Desde la carpeta `ms-andesstay-reservations`, levanta RabbitMQ con Docker Desktop:

```powershell
docker compose up -d
```

La consola de administración queda disponible en `http://localhost:15672`. En un
entorno local, las credenciales predeterminadas son `andesstay` / `andesstay_local`;
se pueden reemplazar con las variables `RABBITMQ_USERNAME` y `RABBITMQ_PASSWORD`.

Inicia el servicio desde esa misma carpeta con `.\mvnw.cmd spring-boot:run`.
El servicio publica mensajes JSON en el exchange durable `andesstay.reservas`:

| Acción | Routing key | Tipo de evento |
| --- | --- | --- |
| Se crea una reserva | `reserva.creada` | `RESERVA_CREADA` |
| Cambia el estado de una reserva | `reserva.estado-cambiado` | `RESERVA_ESTADO_CAMBIADO` |

Ambos tipos quedan enlazados a la cola durable `andesstay.reservas.events`. El
payload incluye un identificador único de evento, el ID de la reserva, unidad,
estado anterior/nuevo y fechas; no incluye datos personales del huésped.
=======
>>>>>>> 5b7408ccd899582627fe1c2b7b5edf6a105b4576
