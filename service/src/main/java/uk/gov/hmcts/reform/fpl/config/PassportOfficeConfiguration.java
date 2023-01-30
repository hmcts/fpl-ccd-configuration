package uk.gov.hmcts.reform.fpl.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class PassportOfficeConfiguration {
    private final String address;
    private final String email;

    public PassportOfficeConfiguration(@Value("${contacts.passport_office.address}") String address,
                                       @Value("${contacts.passport_office.email}") String email) {
        this.address = address;
        this.email = email;
    }
}
