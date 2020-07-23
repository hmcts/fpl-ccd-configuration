package uk.gov.hmcts.reform.fpl.service.validators;

import org.springframework.stereotype.Component;

@Component
public class OrdersNeededValidator extends PropertiesValidator {

    public OrdersNeededValidator() {
        super("orders");
    }
}
