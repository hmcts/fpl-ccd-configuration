package uk.gov.hmcts.reform.fpl.service;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.OrderStatus;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.validation.groups.SealedSDOGroup;

import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.SEALED;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OrderValidationService {

    private final Validator validator;

    public List<String> validate(final CaseData caseData) {

        final OrderStatus orderStatus = caseData.getStandardDirectionOrder().getOrderStatus();

        if (SEALED.equals(orderStatus)) {
            return validator.validate(caseData, SealedSDOGroup.class)
                .stream()
                .map(ConstraintViolation::getMessage)
                .distinct()
                .collect(toList());
        }

        return emptyList();
    }
}
