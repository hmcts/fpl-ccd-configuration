package uk.gov.hmcts.reform.fpl.model;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface FieldsGroup {

    String[] value() default {};
}
