package cl.duoc.andesstay.ms_andesstay_notify.service;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class NotificationConsumer {
    private final ObjectMapper objectMapper;
    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final String sender;
    private final String housekeepingWebhookUrl;
    private final RestClient restClient = RestClient.create();

    public NotificationConsumer(ObjectMapper objectMapper, ObjectProvider<JavaMailSender> mailSenderProvider,
            @Value("${andesstay.notifications.from:}") String sender,
            @Value("${andesstay.notifications.housekeeping-webhook-url:}") String housekeepingWebhookUrl) {
        this.objectMapper = objectMapper;
        this.mailSenderProvider = mailSenderProvider;
        this.sender = sender;
        this.housekeepingWebhookUrl = housekeepingWebhookUrl;
    }

    @RabbitListener(queues = "q.cmd.email")
    public void sendEmail(Message message) throws Exception {
        JsonNode command = objectMapper.readTree(message.getBody());
        String to = required(command, "to");
        String subject = required(command, "subject");
        String body = required(command, "body");
        if (sender.isBlank()) {
            throw new IllegalStateException("ANDESSTAY_MAIL_FROM is required to send email commands");
        }
        InternetAddress email = new InternetAddress(to);
        email.validate();
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            throw new IllegalStateException("SMTP_HOST is required to send email commands");
        }

        MimeMessage mail = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mail, true, StandardCharsets.UTF_8.name());
        helper.setFrom(sender);
        helper.setTo(email.getAddress());
        helper.setSubject(subject);
        if (command.hasNonNull("reservaId")) {
            helper.setText(plainText(command), reservationHtml(command));
        } else {
            helper.setText(body);
        }
        mailSender.send(mail);
    }

    @RabbitListener(queues = "q.cmd.housekeeping")
    public void createHousekeepingTicket(Message message) throws Exception {
        if (housekeepingWebhookUrl.isBlank()) {
            throw new IllegalStateException("ANDESSTAY_HOUSEKEEPING_WEBHOOK_URL is required for housekeeping commands");
        }
        JsonNode command = objectMapper.readTree(message.getBody());
        required(command, "eventId");
        required(command, "reservaId");
        required(command, "unidadId");
        required(command, "fechaEntrada");
        required(command, "fechaSalida");
        restClient.post()
                .uri(URI.create(housekeepingWebhookUrl))
                .contentType(MediaType.APPLICATION_JSON)
                .body(new String(message.getBody(), StandardCharsets.UTF_8))
                .retrieve()
                .toBodilessEntity();
    }

    @RabbitListener(queues = "q.cmd.voucher")
    public void processVoucher(Message message) {
        throw new IllegalStateException("Voucher generation is not configured; refusing to discard the command");
    }

    private String plainText(JsonNode command) {
        String name = optional(command, "nombreHuesped", "Huesped AndeStay");
        String reservaId = optional(command, "reservaId", "No informado");
        String unidadId = optional(command, "unidadId", "No informada");
        String fechaEntrada = formatDate(optional(command, "fechaEntrada", "No informada"));
        String fechaSalida = formatDate(optional(command, "fechaSalida", "No informada"));
        String personas = optional(command, "cantidadPersonas", "No informado");
        String estado = optional(command, "estado", "CREADA");
        return "Hola, " + name + "\n\n"
                + "Comprobante informativo de tu reserva AndeStay\n"
                + "Codigo de reserva: " + reservaId + "\n"
                + "Alojamiento: Unidad #" + unidadId + "\n"
                + "Entrada: " + fechaEntrada + "\n"
                + "Salida: " + fechaSalida + "\n"
                + "Personas: " + personas + "\n"
                + "Estado: " + estadoLabel(estado) + "\n\n"
                + stateMessage(estado) + "\n\n"
                + "Este comprobante no es una boleta ni acredita un pago.";
    }

    private String reservationHtml(JsonNode command) {
        String name = escapeHtml(optional(command, "nombreHuesped", "Huesped AndeStay"));
        String reservaId = escapeHtml(optional(command, "reservaId", "No informado"));
        String unidadId = escapeHtml(optional(command, "unidadId", "No informada"));
        String fechaEntrada = escapeHtml(formatDate(optional(command, "fechaEntrada", "No informada")));
        String fechaSalida = escapeHtml(formatDate(optional(command, "fechaSalida", "No informada")));
        String personas = escapeHtml(optional(command, "cantidadPersonas", "No informado"));
        String estado = optional(command, "estado", "CREADA");
        String estadoLabel = escapeHtml(estadoLabel(estado));
        String mensajeEstado = escapeHtml(stateMessage(estado));

        return """
                <!doctype html>
                <html lang="es"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width, initial-scale=1"></head>
                <body style="margin:0;padding:0;background-color:#f4f1eb;font-family:Arial,Helvetica,sans-serif;color:#24332e;">
                  <table role="presentation" width="100%" border="0" cellspacing="0" cellpadding="0" style="width:100%;background-color:#f4f1eb;">
                    <tr><td align="center" style="padding:32px 12px;">
                      <table role="presentation" width="600" border="0" cellspacing="0" cellpadding="0" style="width:100%;max-width:600px;background-color:#ffffff;border:1px solid #e8e2d8;border-radius:16px;">
                        <tr><td style="padding:26px 32px;background-color:#173f35;color:#ffffff;font-size:22px;font-weight:bold;">AndeStay <span style="float:right;font-size:12px;font-weight:normal;color:#d9e7dc;">Comprobante de reserva</span></td></tr>
                        <tr><td style="padding:30px 32px 10px;font-size:15px;line-height:23px;color:#52635c;">Hola, {{name}}</td></tr>
                        <tr><td style="padding:0 32px 12px;font-size:27px;line-height:34px;font-weight:bold;color:#173f35;">Detalle de tu reserva</td></tr>
                        <tr><td style="padding:0 32px 22px;font-size:15px;line-height:24px;color:#52635c;">Este comprobante resume los datos registrados para tu estad&iacute;a.</td></tr>
                        <tr><td style="padding:0 32px 22px;"><table role="presentation" width="100%" border="0" cellspacing="0" cellpadding="0" style="background-color:#f2f6f1;border:1px solid #dce8dc;border-radius:12px;"><tr><td style="padding:16px 19px;"><div style="font-size:11px;letter-spacing:1px;text-transform:uppercase;color:#61736a;">Codigo de reserva</div><div style="padding-top:5px;font-size:22px;font-weight:bold;color:#173f35;word-break:break-word;">{{reservaId}}</div></td></tr></table></td></tr>
                        <tr><td style="padding:0 32px 10px;font-size:18px;font-weight:bold;">Tu estad&iacute;a</td></tr>
                        <tr><td style="padding:0 32px 22px;"><table role="presentation" width="100%" border="0" cellspacing="0" cellpadding="0" style="border:1px solid #e8e2d8;border-radius:12px;">
                          <tr><td style="padding:16px 19px;border-bottom:1px solid #eee9e1;"><div style="font-size:12px;color:#718078;">Alojamiento</div><div style="padding-top:5px;font-size:16px;font-weight:bold;">Unidad #{{unidadId}}</div></td></tr>
                          <tr><td style="padding:15px 19px;border-bottom:1px solid #eee9e1;"><table role="presentation" width="100%" border="0" cellspacing="0" cellpadding="0"><tr><td width="50%" valign="top" style="font-size:12px;color:#718078;">Entrada<div style="padding-top:5px;font-size:15px;font-weight:bold;color:#24332e;">{{fechaEntrada}}</div></td><td width="50%" valign="top" style="font-size:12px;color:#718078;">Salida<div style="padding-top:5px;font-size:15px;font-weight:bold;color:#24332e;">{{fechaSalida}}</div></td></tr></table></td></tr>
                          <tr><td style="padding:15px 19px;"><div style="font-size:12px;color:#718078;">Personas</div><div style="padding-top:5px;font-size:15px;">{{personas}}</div></td></tr>
                        </table></td></tr>
                        <tr><td style="padding:0 32px 22px;"><table role="presentation" width="100%" border="0" cellspacing="0" cellpadding="0" style="background-color:#fff8e9;border:1px solid #f0dfb9;border-radius:12px;"><tr><td style="padding:16px 19px;"><div style="font-size:12px;color:#78633d;">Estado de la reserva</div><div style="padding-top:5px;font-size:16px;font-weight:bold;color:#5e4a28;">{{estadoLabel}}</div><div style="padding-top:8px;font-size:13px;line-height:20px;color:#5e4a28;">{{mensajeEstado}}</div></td></tr></table></td></tr>
                        <tr><td style="padding:0 32px 24px;font-size:12px;line-height:19px;color:#718078;">Este es un comprobante informativo de reserva; no es una boleta tributaria ni una factura y no acredita la recepci&oacute;n o aprobaci&oacute;n de un pago.</td></tr>
                        <tr><td style="padding:16px 32px;background-color:#f8f6f2;border-top:1px solid #e8e2d8;font-size:11px;color:#718078;">AndeStay ? Comprobante de reserva</td></tr>
                      </table>
                    </td></tr>
                  </table>
                </body></html>
                """
                .replace("{{name}}", name)
                .replace("{{reservaId}}", reservaId)
                .replace("{{unidadId}}", unidadId)
                .replace("{{fechaEntrada}}", fechaEntrada)
                .replace("{{fechaSalida}}", fechaSalida)
                .replace("{{personas}}", personas)
                .replace("{{estadoLabel}}", estadoLabel)
                .replace("{{mensajeEstado}}", mensajeEstado);
    }

    private String optional(JsonNode command, String field, String fallback) {
        JsonNode value = command.get(field);
        return value == null || value.isNull() || value.asText().isBlank() ? fallback : value.asText();
    }

    private String formatDate(String value) {
        try {
            return LocalDate.parse(value)
                    .format(DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", Locale.forLanguageTag("es-CL")));
        } catch (DateTimeParseException exception) {
            return value;
        }
    }

    private String estadoLabel(String estado) {
        return switch (estado) {
            case "CREADA" -> "Creada";
            case "CONFIRMADA" -> "Confirmada";
            case "CHECKIN_PENDIENTE" -> "Check-in pendiente";
            case "EN_ESTADIA" -> "En estad&iacute;a";
            case "CHECKOUT" -> "Check-out";
            case "CANCELADA" -> "Cancelada";
            default -> estado.replace('_', ' ');
        };
    }

    private String stateMessage(String estado) {
        return switch (estado) {
            case "CREADA" -> "Tu reserva fue creada y qued? registrada. Esto no significa que el pago haya sido recibido o aprobado.";
            case "CONFIRMADA" -> "Tu reserva est? confirmada seg?n el estado registrado por AndeStay.";
            case "CANCELADA" -> "El estado de tu reserva fue actualizado a cancelada.";
            default -> "El estado actual de tu reserva fue actualizado por AndeStay.";
        };
    }

    private String escapeHtml(String value) {
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private String required(JsonNode command, String field) {
        if (!command.hasNonNull(field) || command.get(field).asText().isBlank()) {
            throw new IllegalArgumentException("Required notification field is missing: " + field);
        }
        return command.get(field).asText();
    }
}
