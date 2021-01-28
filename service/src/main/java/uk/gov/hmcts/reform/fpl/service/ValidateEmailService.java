package uk.gov.hmcts.reform.fpl.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

@Service
public class ValidateEmailService {
    private static final String ERROR_MESSAGE = "Enter a valid email address";

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

    private boolean isValidInternetAddress(String email) {
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
