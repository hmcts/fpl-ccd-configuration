package uk.gov.hmcts.reform.fpl.validators;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateOfBirthValidator {

    public static boolean dateOfBirthIsInFuture(final String dob) {
        try {
            final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date dateOfBirth = sdf.parse(dob);
            if (dateOfBirth.after(new Date())) {
                return true;
            }
        } catch (Exception ex) {
            //ignore
        }
        return false;
    }
}
