package uk.gov.hmcts.reform.fpl.service.email.content;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.DateFormatterService;
import uk.gov.hmcts.reform.fpl.service.HearingBookingService;
import uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper;

import java.util.Map;

import static java.util.Objects.isNull;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;

@Service
public class PartyAddedToCaseEmailContentProvider extends AbstractEmailContentProvider {

    private final ObjectMapper objectMapper;

    @Autowired
    protected PartyAddedToCaseEmailContentProvider(@Value("${ccd.ui.base.url}") String uiBaseUrl,
                                                   ObjectMapper objectMapper,
                                                   DateFormatterService dateFormatterService,
                                                   HearingBookingService hearingBookingService) {
        super(uiBaseUrl,dateFormatterService,hearingBookingService);
        this.objectMapper = objectMapper;
    }

    public Map<String, Object> buildPartyAddedToCaseNotification(final CaseDetails caseDetails) {
        CaseData caseData = objectMapper.convertValue(caseDetails.getData(), CaseData.class);
        return Map.of(
            "firstRespondentLastName", caseData.getRespondents1().get(0).getValue().getParty().getLastName(),
            "familyManCaseNumber", isNull(caseData.getFamilyManCaseNumber()) ? "" : caseData.getFamilyManCaseNumber()
        );
    }
}
