package uk.gov.hmcts.reform.fpl.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import static org.apache.logging.log4j.util.Strings.isEmpty;

@Service
public class ValidateEmailService {
    private static final String ERROR_MESSAGE = "Enter an email address in the correct format,"
        + " for example name@example.com";
    private static final List<String> OPTIONAL_KEYS = List.of("Applicant", "Representative");

    public List<String> validate(List<String> emailAddresses, String key) {
        List<String> validationErrors = new ArrayList<>();
        int index = 1;

        for (String email : emailAddresses) {
            Optional<String> validationMessage = Optional.empty();

            if (isEmpty(email)) {
                if (!emailIsOptional(key)) {
                    validationMessage = validate(email);
                }
            } else {
                validationMessage = validate(email);
            }

            if (validationMessage.isPresent()) {
                validationErrors.add(String.format("%s %s: %s", key, index, validationMessage.get()));
            }

            index++;
        }

        return validationErrors;
    }

    public Optional<String> validate(String email, String errorMessage) {
        return isValid(email) ? Optional.empty() : Optional.ofNullable(errorMessage);
    }

    public Optional<String> validate(String email) {
        return isValid(email) ? Optional.empty() : Optional.ofNullable(ERROR_MESSAGE);
    }

    private boolean emailIsOptional(String key) {
        return OPTIONAL_KEYS.contains(key);
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
