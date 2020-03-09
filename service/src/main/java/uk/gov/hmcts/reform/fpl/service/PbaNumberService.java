package uk.gov.hmcts.reform.fpl.service;

import com.google.common.collect.ImmutableList;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.Applicant;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.utils.PbaNumberHelper;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PbaNumberService {

    private static final String VALIDATION_ERROR_MESSAGE = "Payment by account (PBA) number must include 7 numbers";

    private final PbaNumberHelper pbaNumberHelper;

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
        return pbaNumberHelper.getNonEmptyPbaNumber(c2DocumentBundle)
            .map(pbaNumberHelper::setPrefix)
            .map(pbaNumber -> c2DocumentBundle.toBuilder().pbaNumber(pbaNumber).build())
            .orElse(c2DocumentBundle);
    }

    public List<String> validate(List<Element<Applicant>> applicantElementsList) {
        if (pbaNumberHelper.getNonEmptyPbaNumbers(applicantElementsList)
            .anyMatch(pbaNumberHelper::isInvalidPbaNumber)) {
            return List.of(VALIDATION_ERROR_MESSAGE);
        }
        return List.of();
    }

    public List<String> validate(C2DocumentBundle c2DocumentBundle) {
        if (pbaNumberHelper.getNonEmptyPbaNumber(c2DocumentBundle)
            .map(pbaNumberHelper::isInvalidPbaNumber)
            .orElse(false)) {
            return List.of(VALIDATION_ERROR_MESSAGE);
        }
        return List.of();
    }

    private boolean hasEmptyPbaNumber(Element<Applicant> applicantElement) {
        return StringUtils.isEmpty(applicantElement.getValue().getParty().getPbaNumber());
    }

    private Element<Applicant> updatePbaNumber(Element<Applicant> applicantElement) {
        var updatedPbaNumber = pbaNumberHelper.setPrefix(applicantElement.getValue().getParty().getPbaNumber());

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

