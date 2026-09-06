package com.servicecops.project.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.mail.autoconfigure.MailSenderAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Brings in Spring Boot's {@code JavaMailSender} only when
 * {@code jet.mail.enabled=true}. The application excludes mail auto-config
 * by default so SMTP is not initialized at startup.
 */
@Configuration
@ConditionalOnProperty(prefix = "jet.mail", name = "enabled", havingValue = "true")
@Import(MailSenderAutoConfiguration.class)
public class MailConfiguration {
}
