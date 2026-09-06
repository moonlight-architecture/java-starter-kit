package com.servicecops.project.mail;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

/**
 * HTML mailer. Only registered when {@code jet.mail.enabled=true}.
 *
 * <p>Use {@link #sendHtml(String, String, String)} for raw HTML, or
 * {@link #sendTemplate(String, String, String, Map)} with a Thymeleaf
 * template under {@code src/main/resources/templates/} (see {@code email-template.html}).
 */
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "jet.mail", name = "enabled", havingValue = "true")
public class MailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username:}")
    private String from;

    public void sendHtml(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            if (from != null && !from.isBlank()) {
                helper.setFrom(from);
            }
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new IllegalStateException("Failed to send mail to " + to, e);
        }
    }

    public void sendTemplate(String to, String subject, String templateName, Map<String, Object> model) {
        Context context = new Context();
        if (model != null) {
            context.setVariables(model);
        }
        sendHtml(to, subject, templateEngine.process(templateName, context));
    }
}
