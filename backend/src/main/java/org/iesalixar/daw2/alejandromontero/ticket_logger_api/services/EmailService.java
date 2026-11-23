package org.iesalixar.daw2.alejandromontero.ticket_logger_api.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.iesalixar.daw2.alejandromontero.ticket_logger_api.dtos.LineaTicketResponseDTO;
import org.iesalixar.daw2.alejandromontero.ticket_logger_api.dtos.TicketResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${app.backend.base-url:http://localhost:8080}")
    private String backendBaseUrl;

    public void sendHtmlEmail(String to, String subject, String htmlBody) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlBody, true); // El 'true' indica que es HTML

        mailSender.send(message);
    }

    public void sendTwoFactorCodeEmail(String email, String code) throws MessagingException {
        String htmlContent = String.format(
                "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; border: 1px solid #e0e0e0; border-radius: 8px; overflow: hidden;'>" +
                        "<div style='background-color: #cc0000; padding: 20px; text-align: center; color: white;'>" +
                        "<h1>Verificacion de Seguridad</h1>" +
                        "</div>" +
                        "<div style='padding: 30px; line-height: 1.6; color: #333;'>" +
                        "<p>Has solicitado un codigo de acceso para tu cuenta. Por favor, utiliza el siguiente codigo para completar el proceso:</p>" +
                        "<div style='background-color: #f4f4f4; padding: 20px; text-align: center; border-radius: 5px; margin: 20px 0;'>" +
                        "<span style='font-size: 32px; font-weight: bold; letter-spacing: 5px; color: #cc0000;'>%s</span>" +
                        "</div>" +
                        "<p style='font-size: 12px; color: #777;'>Este codigo expirara en 1 minuto. Si no solicitaste este correo, puedes ignorarlo con seguridad.</p>" +
                        "</div>" +
                        "<div style='background-color: #f9f9f9; padding: 15px; text-align: center; font-size: 12px; color: #999;'>" +
                        "© 2026 GoGaming.es" +
                        "</div>" +
                        "</div>",
                code
        );

        sendHtmlEmail(email, "Tu Codigo de Verificacion: " + code, htmlContent);
    }

    public void sendPaymentConfirmationEmail(String email, String firstName, TicketResponseDTO ticket) throws MessagingException {
        String customerName = (firstName == null || firstName.isBlank()) ? "cliente" : firstName;

        StringBuilder rows = new StringBuilder();
        for (LineaTicketResponseDTO linea : ticket.getLineas()) {
            String imageUrl = resolveProductImageUrl(linea.getProductImage());
            rows.append("<tr>")
                    .append("<td style='padding: 8px; border-bottom: 1px solid #eee;'>")
                    .append("<div style='display: flex; align-items: center; gap: 10px;'>")
                    .append("<img src='").append(imageUrl).append("' alt='").append(linea.getProductName()).append("' style='width: 56px; height: 56px; object-fit: cover; border-radius: 6px; border: 1px solid #ddd;' />")
                    .append("<span>").append(linea.getProductName()).append("</span>")
                    .append("</div>")
                    .append("</td>")
                    .append("<td style='padding: 8px; border-bottom: 1px solid #eee; text-align: center;'>").append(linea.getCantidad()).append("</td>")
                    .append("<td style='padding: 8px; border-bottom: 1px solid #eee; text-align: right;'>").append(linea.getPrecioUnitario()).append(" EUR</td>")
                    .append("<td style='padding: 8px; border-bottom: 1px solid #eee; text-align: right;'>").append(linea.getSubtotal()).append(" EUR</td>")
                    .append("</tr>");
        }

        String htmlContent = String.format(
                "<div style='font-family: Arial, sans-serif; max-width: 700px; margin: 0 auto; border: 1px solid #e0e0e0; border-radius: 8px; overflow: hidden;'>" +
                        "<div style='background-color: #cc0000; padding: 20px; text-align: center; color: white;'>" +
                        "<h1>Pago completado con exito</h1>" +
                        "</div>" +
                        "<div style='padding: 24px; color: #333; line-height: 1.6;'>" +
                        "<p>Hola %s, gracias por tu compra en GoGaming.</p>" +
                        "<p><strong>Numero de ticket:</strong> %s<br/>" +
                        "<strong>Fecha:</strong> %s<br/>" +
                        "<strong>Metodo de pago:</strong> %s</p>" +
                        "<table style='width: 100%%; border-collapse: collapse; margin-top: 12px;'>" +
                        "<thead><tr style='background: #f4f4f4;'>" +
                        "<th style='padding: 8px; text-align: left;'>Producto</th>" +
                        "<th style='padding: 8px; text-align: center;'>Cantidad</th>" +
                        "<th style='padding: 8px; text-align: right;'>Precio U.</th>" +
                        "<th style='padding: 8px; text-align: right;'>Subtotal</th>" +
                        "</tr></thead>" +
                        "<tbody>%s</tbody>" +
                        "</table>" +
                        "<div style='margin-top: 16px; text-align: right;'>" +
                        "<p style='margin: 4px 0 0 0; font-size: 18px; color: #cc0000;'><strong>Total:</strong> %s EUR</p>" +
                        "</div>" +
                        "</div>" +
                        "<div style='background-color: #f9f9f9; padding: 12px; text-align: center; font-size: 12px; color: #999;'>" +
                        "© 2026 GoGaming.es" +
                        "</div>" +
                        "</div>",
                customerName,
                ticket.getNumeroTicket(),
                ticket.getFecha(),
                ticket.getMetodoPago(),
                rows,
                ticket.getTotal()
        );

        sendHtmlEmail(email, "Confirmacion de compra - " + ticket.getNumeroTicket(), htmlContent);
    }

    private String resolveProductImageUrl(String rawImage) {
        if (rawImage == null || rawImage.isBlank()) {
            return "https://via.placeholder.com/56x56?text=IMG";
        }

        String cleanImage = rawImage.replace("\"", "").trim();
        if (cleanImage.startsWith("http://") || cleanImage.startsWith("https://")) {
            return cleanImage;
        }

        String encoded = URLEncoder.encode(cleanImage, StandardCharsets.UTF_8);
        return backendBaseUrl + "/api/v1/users/image/" + encoded;
    }
}

