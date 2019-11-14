package uk.gov.hmcts.reform.fpl.validation.interfaces;

import javax.validation.Payload;

public @interface HasStartDateAfterEndDate {
    String message() default "The start date cannot be after the end date";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
