package cl.duoc.andesstay.ms_andesstay_notify.service;

import jakarta.mail.Multipart;
import jakarta.mail.Part;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.javamail.JavaMailSender;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NotificationConsumerTest {

    @Test
    @SuppressWarnings("unchecked")
    void sendsReservationReceiptAsHtmlAndPlainText() throws Exception {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        ObjectProvider<JavaMailSender> mailSenderProvider = mock(ObjectProvider.class);
        when(mailSenderProvider.getIfAvailable()).thenReturn(mailSender);
        MimeMessage mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        NotificationConsumer consumer = new NotificationConsumer(
                new ObjectMapper(), mailSenderProvider, "sender@example.com", "");
        String command = """
                {"to":"guest@example.com","subject":"Comprobante de reserva AndeStay","body":"receipt",
                 "nombreHuesped":"Ada <Lovelace>","reservaId":"reserva-123","unidadId":"42",
                 "fechaEntrada":"2026-11-15","fechaSalida":"2026-11-18","cantidadPersonas":"2","estado":"CREADA"}
                """;

        consumer.sendEmail(new Message(command.getBytes(StandardCharsets.UTF_8), new MessageProperties()));

        verify(mailSender).send(mimeMessage);
        mimeMessage.saveChanges();
        assertTrue(mimeMessage.getContentType().startsWith("multipart/"));
        String text = findContent(mimeMessage, "text/plain");
        String html = findContent(mimeMessage, "text/html");
        assertTrue(text != null);
        assertTrue(html != null);
        assertTrue(text.contains("reserva-123"));
        assertTrue(html.contains("Ada &lt;Lovelace&gt;"));
        assertTrue(html.contains("Unidad #42"));
        assertTrue(html.contains("15 de noviembre de 2026"));
        assertTrue(html.contains("Tu reserva fue creada"));
        assertTrue(!html.contains("{{"));
    }

    private String findContent(Part part, String contentType) throws Exception {
        if (part.isMimeType(contentType)) {
            return (String) part.getContent();
        }
        Object content = part.getContent();
        if (content instanceof Multipart multipart) {
            for (int i = 0; i < multipart.getCount(); i++) {
                String found = findContent(multipart.getBodyPart(i), contentType);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }
}
