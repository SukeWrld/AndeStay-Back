# AndeStay-Back

Backend de AndeStay, con servicios Spring Boot y un BFF.

## Infraestructura local

La infraestructura se divide en tres archivos Compose:

- `infra/apps/compose.yml`: BFF y los cinco servicios de dominio. Solo publica
  el BFF en `http://localhost:8080`; los servicios de dominio quedan en la red
  interna.
- `infra/mq/compose.yml`: clúster RabbitMQ de dos nodos y Management UI en
  `http://localhost:15672`.
- `infra/kafka/compose.yml`: clúster ZooKeeper de tres nodos, Kafka de tres
  brokers y Kafka UI en `http://localhost:8086`.

El Compose de aplicaciones incluye MongoDB local persistente en
`localhost:27017`. Reservations, Catalog, Audit y Report usan la base
`andesstay`; configura `MONGODB_URI` si prefieres usar MongoDB Atlas. Esta
decisión mantiene MongoDB, aunque la guía del caso originalmente especifica
Oracle para algunos servicios.

Desde la raíz del repositorio, crea la red compartida una vez y levanta los
componentes en este orden:

```powershell
docker network create andesstay-network
docker compose -f infra/mq/compose.yml up -d
docker compose -f infra/kafka/compose.yml up -d
docker compose -f infra/apps/compose.yml up --build -d
```

RabbitMQ publica AMQP en el puerto `5672`. Kafka publica los brokers para
clientes del host en `localhost:9092`, `localhost:9093` y `localhost:9094`;
los servicios dentro de Docker usan `kafka1:29092`, `kafka2:29092` y
`kafka3:29092`. La topología crea los tópicos `reservations.events` y
`audit.timeline` con tres particiones y factor de réplica tres.

Los valores por defecto de RabbitMQ y la instancia MongoDB local sin
autenticación son solo para desarrollo. Cambia la URI o habilita autenticación
antes de usar MongoDB en un entorno compartido. RabbitMQ se puede cambiar con `RABBITMQ_USERNAME`,
`RABBITMQ_PASSWORD` y `RABBITMQ_ERLANG_COOKIE`. Cambia esos valores antes de
desplegar en un entorno compartido y no guardes credenciales en el repositorio.
Si una contraseña contiene caracteres especiales, codifícala para usarla en
`RABBITMQ_URL`.

El servicio Notify consume los comandos `q.cmd.email` y `q.cmd.housekeeping`.
Configura `SMTP_HOST`, `SMTP_PORT`, `SMTP_USERNAME`, `SMTP_PASSWORD` y
`ANDESSTAY_MAIL_FROM` para correo. Configura
`ANDESSTAY_HOUSEKEEPING_WEBHOOK_URL` para entregar tickets a housekeeping.
Los comandos fallidos se reintentan y después pasan a la DLQ correspondiente.
El comando de voucher se conserva en su cola, pero se envía a DLQ hasta que se
configure un proveedor de generación PDF.

Audit y Report consumen `reservations.events` desde grupos Kafka independientes.
Audit publica los eventos persistidos en `audit.timeline`; los fallos de
consumidor se deben revisar en los tópicos DLT.

El servicio Reservations publica eventos JSON en el exchange durable
`andesstay.reservas` con las routing keys `reserva.creada` y
`reserva.estado-cambiado`; su URL RabbitMQ se configura con `RABBITMQ_URL`.
`RABBITMQ_URL` acepta `amqp://` para el clúster local y `amqps://` para un
broker remoto.

Con `AZURE_AD_ISSUER_URI` vacío, los servicios quedan abiertos para desarrollo
local. Para protegerlos configura el issuer de Azure AD y
`AZURE_AD_API_AUDIENCE` con el Application ID URI de la API. Se validan emisor,
firma, expiración y audiencia; si se configura el issuer sin audiencia, las
aplicaciones no inician. Los tokens deben incluir el claim `roles` con los
roles `Admin`, `Operador del dominio`, `Cliente del dominio` o `Auditor`. El
BFF publica las rutas de Reservas, Catálogo, Auditoría y Reportería en
`http://localhost:8080` y reenvía el bearer token a cada servicio.

Para apagar cada grupo:

```powershell
docker compose -f infra/apps/compose.yml down
docker compose -f infra/kafka/compose.yml down
docker compose -f infra/mq/compose.yml down
```

El stack completo usa dos nodos RabbitMQ, tres nodos ZooKeeper, tres brokers
Kafka y seis aplicaciones. Asigna memoria suficiente a Docker Desktop antes de
levantar todos los grupos. No se pudo comprobar el arranque en este entorno
porque Docker Desktop no estaba ejecutando.
