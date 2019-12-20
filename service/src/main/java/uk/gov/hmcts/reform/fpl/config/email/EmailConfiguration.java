package uk.gov.hmcts.reform.fpl.config.email;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Map;
import java.util.Properties;

import static org.apache.commons.lang.ObjectUtils.defaultIfNull;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "spring.mail.properties")
public class EmailConfiguration {
    private String host;
    private int port;
    private String testConnection;
    private Map<String, String> mail;

    @Bean
    public JavaMailSender javaMailSender() {
        final String mailSmtpStarttlsEnableKey = "mail.smtp.starttls.enable";
        final String mailSmtpSslTrustKey = "mail.smtp.ssl.trust";

        JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
        javaMailSender.setHost(host);
        javaMailSender.setPort(port);

        Properties properties = new Properties();
        properties.setProperty("mail.transport.protocol", "smtp");
        properties.setProperty(mailSmtpStarttlsEnableKey,
            (String) defaultIfNull(mail.get(mailSmtpStarttlsEnableKey), ""));
        properties.put(mailSmtpSslTrustKey, defaultIfNull(mail.get(mailSmtpSslTrustKey), ""));

        javaMailSender.setJavaMailProperties(properties);
        return javaMailSender;
    }
}
