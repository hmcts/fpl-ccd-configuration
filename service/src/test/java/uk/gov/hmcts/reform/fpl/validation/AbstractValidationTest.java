package uk.gov.hmcts.reform.fpl.validation;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.List;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import static java.util.stream.Collectors.toList;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = LocalValidatorFactoryBean.class)
public abstract class AbstractValidationTest {

    @Autowired
    private Validator validator;

    public <T> List<String> validate(T object, Class... groups) {
        return validator.validate(object, groups).stream().map(ConstraintViolation::getMessage).collect(toList());
    }

}
