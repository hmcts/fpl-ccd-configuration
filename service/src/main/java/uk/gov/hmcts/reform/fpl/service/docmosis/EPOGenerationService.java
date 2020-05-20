package uk.gov.hmcts.reform.fpl.service.docmosis;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisGeneratedOrder;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisGeneratedOrder.DocmosisGeneratedOrderBuilder;
import uk.gov.hmcts.reform.fpl.model.emergencyprotectionorder.EPOChildren;
import uk.gov.hmcts.reform.fpl.service.CaseDataExtractionService;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.time.LocalDateTime;
import java.util.Optional;

import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE_TIME_AT;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;

@Service
public class EPOGenerationService extends GeneratedOrderTemplateDataGeneration {

    private final Time time;

    public EPOGenerationService(CaseDataExtractionService caseDataExtractionService,
        LocalAuthorityNameLookupConfiguration localAuthorityNameLookupConfiguration, Time time) {
        super(caseDataExtractionService, localAuthorityNameLookupConfiguration);
        this.time = time;
    }

    @Override
    DocmosisGeneratedOrderBuilder populateCustomOrderFields(CaseData caseData) {
        return DocmosisGeneratedOrder.builder()
            .localAuthorityName(getLocalAuthorityName(caseData.getCaseLocalAuthority()))
            .childrenDescription(getChildrenDescription(caseData.getEpoChildren()))
            .epoType(caseData.getEpoType())
            .includePhrase(caseData.getEpoPhrase().getIncludePhrase())
            .removalAddress(getFormattedRemovalAddress(caseData))
            .epoStartDateTime(formatEPODateTime(time.now()))
            .epoEndDateTime(formatEPODateTime(caseData.getEpoEndDate()));
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
