package uk.gov.hmcts.reform.fpl.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
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
            String validationMessage = validate(email);

            if (!validationMessage.isBlank()) {
                validationErrors.add(String.format("%s %s: %s", key, index, validationMessage));
            }

            index++;
        }

        return validationErrors;
    }

    public String validate(String email) {
        if (!isValidInternetAddress(email)) {
            return ERROR_MESSAGE;
        }

        return "";
    }

    public String validate(String email, String errorMessage) {
        if (!isValidInternetAddress(email)) {
            return errorMessage;
        }

        return "";
    }

    public boolean isValidInternetAddress(String email) {
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
