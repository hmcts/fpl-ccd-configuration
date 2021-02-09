package uk.gov.hmcts.reform.fpl.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

@Service
public class ValidateEmailService {
    private static final String ERROR_MESSAGE = "Enter an email address in the correct format,"
        + " for example name@example.com";

    public List<String> validate(List<String> emailAddresses, String key) {
        List<String> validationErrors = new ArrayList<>();
        int index = 1;

        for (String email : emailAddresses) {
            Optional validationMessage = validate(email);

            if (validationMessage.isPresent()) {
                validationErrors.add(String.format("%s %s: %s", key, index, validationMessage.get()));
            }

            index++;
        }

        return validationErrors;
    }

    public Optional<String> validate(String email) {
        return isValid(email) ? Optional.empty() : Optional.ofNullable(ERROR_MESSAGE);
    }

    public Optional<String> validate(String email, String errorMessage) {
        return isValid(email) ? Optional.empty() : Optional.ofNullable(errorMessage);
    }

    public boolean isValid(String email) {
        InternetAddress internetAddress = new InternetAddress();
        internetAddress.setAddress(email);

        try {
            internetAddress.validate();
            return true;
        } catch (AddressException addressException) {
            return false;
        }
    }
}
