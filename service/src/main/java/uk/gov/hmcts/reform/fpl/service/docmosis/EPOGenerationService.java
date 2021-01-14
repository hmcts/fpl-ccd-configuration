package uk.gov.hmcts.reform.fpl.service.docmosis;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.EPOExclusionRequirementType;
import uk.gov.hmcts.reform.fpl.enums.EPOType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisGeneratedOrder;
import uk.gov.hmcts.reform.fpl.model.emergencyprotectionorder.EPOChildren;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper;

import java.time.LocalDateTime;
import java.util.Optional;

import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE_TIME_AT;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class EPOGenerationService extends GeneratedOrderTemplateDataGeneration {
    private final FeatureToggleService featureToggleService;

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
            .exclusionRequirement(featureToggleService.isEpoOrderTypeAndExclusionEnabled()
                ? buildExclusionRequirement(caseData) : null)
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

    private String buildExclusionRequirement(CaseData caseData) {
        if (caseData.getEpoType() == EPOType.REMOVE_TO_ACCOMMODATION
            || caseData.getEpoExclusionRequirementType() == EPOExclusionRequirementType.NO_TO_EXCLUSION) {
            return null;
        }
        String who = caseData.getEpoWhoIsExcluded();
        String address = caseData.getEpoRemovalAddress().getAddressAsString(", ");
        String when;
        if (caseData.getEpoExclusionRequirementType() == EPOExclusionRequirementType.STARTING_ON_SAME_DATE) {
            when = "forthwith";
        } else {
            when = "from " + DateFormatterHelper.formatLocalDateToString(caseData.getEpoExclusionStartDate(), DATE);
        }
        return String.format("The Court directs that %s be excluded from %s %s so that the child may continue to live "
                + "there, consent to the exclusion requirement having been given by %s.",
            who, address, when, who);
    }
}
