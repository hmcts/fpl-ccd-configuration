package uk.gov.hmcts.reform.fpl.config.email;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Slf4j
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "spring.mail")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class EmailConfiguration {
    private String host;
    private int port;

    private final SmtpPropertiesConfiguration smtpPropertiesConfiguration;

    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
        javaMailSender.setHost(host);
        javaMailSender.setPort(port);

        Properties properties = new Properties();
        properties.setProperty("mail.transport.protocol", "smtp");
        properties.setProperty("mail.smtp.starttls.enable", smtpPropertiesConfiguration.getStarttlsEnable());
        properties.put("mail.smtp.ssl.trust", smtpPropertiesConfiguration.getSslTrust());
        properties.put("mail.debug", "true");

        log.error("'mail.smtp.ssl.trust' value is " + smtpPropertiesConfiguration.getSslTrust());
        javaMailSender.setJavaMailProperties(properties);
        return javaMailSender;
    }

    @Getter
    @Configuration
    static class SmtpPropertiesConfiguration {
        @Value("${spring.mail.properties.mail-smtp.starttls.enable}")
        private String starttlsEnable;

        @Value("${spring.mail.properties.mail-smtp.ssl.trust}")
        private String sslTrust;
    }
}
