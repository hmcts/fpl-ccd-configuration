package uk.gov.hmcts.reform.fpl.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.String.format;
import static java.net.IDN.toASCII;
import static java.net.IDN.toUnicode;
import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static java.util.regex.Pattern.compile;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.split;

@Slf4j
@Service
public class ValidateEmailService {

    private static final int EMAIL_MAX_LENGTH = 320;
    private static final int HOST_MAX_LENGTH = 253;
    private static final int HOST_PART_MAX_LENGTH = 63;


    private static final String ERROR_MESSAGE = "Enter an email address in the correct format,"
        + " for example name@example.com";
    private static final List<String> OPTIONAL_KEYS = List.of("Applicant", "Representative");

    private static final String LOCAL_CHARS = "a-zA-Z0-9.!#$%&'*+/=?^_`{|}~\\-";

    private static final Pattern HOSTNAME_PATTERN = compile(format(
        "^(xn-|[a-z0-9]{1,%1$d})(-[a-z0-9]{1,%1$d}){0,%1$d}$", HOST_PART_MAX_LENGTH), CASE_INSENSITIVE);
    private static final Pattern TLD_PATTERN = compile(format(
        "^([a-z]{2,%1$d}|xn--([a-z0-9]{1,%1$d}-){0,%1$d}[a-z0-9]{1,%1$d})$", HOST_PART_MAX_LENGTH), CASE_INSENSITIVE);
    private static final Pattern EMAIL_PATTERN = compile(format(
        "^[%s]{1,%2$d}@([^.@][^@\\s]{2,%2$d})$", LOCAL_CHARS, EMAIL_MAX_LENGTH));

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
                validationErrors.add(format("%s %s: %s", key, index, validationMessage.get()));
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

    /*
    Mimic gov notify validation
    see https://github.com/alphagov/notifications-utils/blob/master/notifications_utils/recipients.py#L494-L534
     */
    public boolean isValid(String email) {

        if (isEmpty(email)) {
            log.warn("Email is null or empty");
            return false;
        }

        final String emailAddress = StringUtils.trim(email);

        if (emailAddress.length() > EMAIL_MAX_LENGTH) {
            log.warn("Email is longer than {} characters", EMAIL_MAX_LENGTH);
            return false;
        }

        if (emailAddress.contains("..")) {
            log.warn("Email contains ..");
            return false;
        }

        final Matcher emailMatcher = EMAIL_PATTERN.matcher(emailAddress);

        if (!emailMatcher.matches()) {
            log.warn("Email does not match pattern");
            return false;
        }

        String hostname = emailMatcher.group(1);

        try {
            hostname = toASCII(toUnicode(hostname));
        } catch (Exception e) {
            log.warn("Email hostname can not be converted to ascii");
            return false;
        }

        final String[] hostParts = split(hostname, ".");

        if (hostname.length() > HOST_MAX_LENGTH) {
            log.warn("Email hostname is longer than {} characters", HOST_MAX_LENGTH);
            return false;
        }

        if (hostParts.length < 2) {
            log.warn("Email hostname parts is {}", hostParts.length);
            return false;
        }

        for (String hostPart : hostParts) {
            if (hostPart.length() > HOST_PART_MAX_LENGTH) {
                log.warn("Email hostname part is longer than {}", HOST_PART_MAX_LENGTH);
                return false;
            }

            if (!HOSTNAME_PATTERN.matcher(hostPart).matches()) {
                log.warn("Email hostname part does not match pattern");
                return false;
            }
        }

        if (!TLD_PATTERN.matcher(hostParts[hostParts.length - 1]).matches()) {
            log.warn("Email top level domain does not match pattern");
            return false;
        }

        return true;
    }
}
