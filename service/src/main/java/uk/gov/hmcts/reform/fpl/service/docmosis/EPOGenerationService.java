package uk.gov.hmcts.reform.fpl.service.docmosis;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisGeneratedOrder;
import uk.gov.hmcts.reform.fpl.model.emergencyprotectionorder.EPOChildren;

import java.time.LocalDateTime;
import java.util.Optional;

import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE_TIME_AT;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class EPOGenerationService extends GeneratedOrderTemplateDataGeneration {

    @Override
    DocmosisGeneratedOrder populateCustomOrderFields(CaseData caseData) {
        return DocmosisGeneratedOrder.builder()
            .localAuthorityName(getLocalAuthorityName(caseData.getCaseLocalAuthority()))
            .childrenDescription(getChildrenDescription(caseData.getEpoChildren()))
            .epoType(caseData.getEpoType())
            .includePhrase(caseData.getEpoPhrase().getIncludePhrase())
            .removalAddress(getFormattedRemovalAddress(caseData))
            .epoStartDateTime(formatEPODateTime(caseData.getDateAndTimeOfIssue()))
            .epoEndDateTime(formatEPODateTime(caseData.getEpoEndDate()))
            .build();
    }

    private String getChildrenDescription(EPOChildren epoChildren) {
        if ("Yes".equals(epoChildren.getDescriptionNeeded())) {
            return epoChildren.getDescription();
        }
        return "";
    }

    private String getFormattedRemovalAddress(CaseData caseData) {
        return Optional.ofNullable(caseData.getEpoRemovalAddress())
            .map(address -> address.getAddressAsString(", ")).orElse("");
    }

    private String formatEPODateTime(LocalDateTime dateTime) {
        return formatLocalDateTimeBaseUsingFormat(dateTime, DATE_TIME_AT);
    }
}
