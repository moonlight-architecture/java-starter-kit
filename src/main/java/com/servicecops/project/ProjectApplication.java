package com.servicecops.project;

import com.servicecops.project.models.jpahelpers.repository.JetRepositoryImpl;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.mail.autoconfigure.MailSenderAutoConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(exclude = MailSenderAutoConfiguration.class)
@EnableJpaRepositories(
		basePackages = "com.servicecops.project.repositories",
		repositoryBaseClass = JetRepositoryImpl.class)
public class ProjectApplication {
	public static void main(String[] args) {
		SpringApplication.run(ProjectApplication.class, args);
	}
}
