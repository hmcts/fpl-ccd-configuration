package uk.gov.hmcts.reform.fpl.service;

import com.google.common.collect.ImmutableList;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.Applicant;
import uk.gov.hmcts.reform.fpl.model.PBAPayment;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.utils.PbaNumberHelper;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.utils.PbaNumberHelper.getNonEmptyPbaNumber;
import static uk.gov.hmcts.reform.fpl.utils.PbaNumberHelper.getNonEmptyPbaNumbers;
import static uk.gov.hmcts.reform.fpl.utils.PbaNumberHelper.getPBAPaymentWithNonEmptyPbaNumber;
import static uk.gov.hmcts.reform.fpl.utils.PbaNumberHelper.isInvalidPbaNumber;
import static uk.gov.hmcts.reform.fpl.utils.PbaNumberHelper.setPrefix;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PbaNumberService {

    private static final String VALIDATION_ERROR_MESSAGE = "Payment by account (PBA) number must include 7 numbers";

    public List<Element<Applicant>> update(List<Element<Applicant>> applicantElementsList) {
        var applicantsPartitionedByEmptyPbaNumber = applicantElementsList.stream()
            .collect(Collectors.partitioningBy(this::hasEmptyPbaNumber));

        return ImmutableList.<Element<Applicant>>builder()
            .addAll(applicantsPartitionedByEmptyPbaNumber.get(true))
            .addAll(applicantsPartitionedByEmptyPbaNumber.get(false)
                .stream()
                .map(this::updatePbaNumber)
                .collect(toList()))
            .build();
    }

    public C2DocumentBundle update(C2DocumentBundle c2DocumentBundle) {
        return getNonEmptyPbaNumber(c2DocumentBundle)
            .map(PbaNumberHelper::setPrefix)
            .map(pbaNumber -> c2DocumentBundle.toBuilder().pbaNumber(pbaNumber).build())
            .orElse(c2DocumentBundle);
    }

    public String update(String pbaNumber) {
        if (!isEmpty(pbaNumber)) {
            return setPrefix(pbaNumber);
        }
        return null;
    }

    public PBAPayment updatePBAPayment(PBAPayment pbaPayment) {
        if (pbaPayment != null && !isEmpty(pbaPayment.getPbaNumber())) {
            return pbaPayment.toBuilder().pbaNumber(setPrefix(pbaPayment.getPbaNumber())).build();
        }
        return null;
    }

    public List<String> validate(PBAPayment pbaPayment) {
        if (getPBAPaymentWithNonEmptyPbaNumber(pbaPayment)
            .map(PbaNumberHelper::isInvalidPbaNumber)
            .orElse(false)) {
            return List.of(VALIDATION_ERROR_MESSAGE);
        }
        return List.of();
    }

    public List<String> validate(List<Element<Applicant>> applicantElementsList) {
        if (getNonEmptyPbaNumbers(applicantElementsList)
            .anyMatch(PbaNumberHelper::isInvalidPbaNumber)) {
            return List.of(VALIDATION_ERROR_MESSAGE);
        }
        return List.of();
    }

    public List<String> validate(C2DocumentBundle c2DocumentBundle) {
        if (getNonEmptyPbaNumber(c2DocumentBundle)
            .map(PbaNumberHelper::isInvalidPbaNumber)
            .orElse(false)) {
            return List.of(VALIDATION_ERROR_MESSAGE);
        }
        return List.of();
    }

    public List<String> validate(String pbaNumber) {
        if (!isEmpty(pbaNumber) && isInvalidPbaNumber(pbaNumber)) {
            return List.of(VALIDATION_ERROR_MESSAGE);
        }
        return List.of();
    }

    private boolean hasEmptyPbaNumber(Element<Applicant> applicantElement) {
        return StringUtils.isEmpty(applicantElement.getValue().getParty().getPbaNumber());
    }

    private Element<Applicant> updatePbaNumber(Element<Applicant> applicantElement) {
        var updatedPbaNumber = setPrefix(applicantElement.getValue().getParty().getPbaNumber());

        return Element.<Applicant>builder()
            .id(applicantElement.getId())
            .value(applicantElement.getValue().toBuilder()
                .party(applicantElement.getValue().getParty().toBuilder()
                    .pbaNumber(updatedPbaNumber)
                    .build())
                .build())
            .build();
    }
}

