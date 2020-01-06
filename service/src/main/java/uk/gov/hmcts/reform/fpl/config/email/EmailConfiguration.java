package uk.gov.hmcts.reform.fpl.config.email;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

import static org.apache.commons.lang3.StringUtils.join;

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
        final String mailSmtpStarttlsEnableKey = "smtp.starttls.enable";
        final String mailSmtpSslTrustKey = "smtp.ssl.trust";

        JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
        javaMailSender.setHost(host);
        javaMailSender.setPort(port);

        Properties properties = new Properties();
        properties.setProperty("mail.transport.protocol", "smtp");
        properties.setProperty(join("mail.", mailSmtpStarttlsEnableKey),
            smtpPropertiesConfiguration.getStarttlsEnable());
        properties.setProperty(join("mail.", mailSmtpSslTrustKey),
            smtpPropertiesConfiguration.getSslTrust());

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
