package uk.gov.hmcts.reform.fpl.service.email.content;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.DateFormatterService;
import uk.gov.hmcts.reform.fpl.service.HearingBookingService;

import java.util.Map;

import static java.util.Objects.isNull;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;

@Service
public class PartyAddedToCaseThroughDigitalServiceContentProvider extends AbstractEmailContentProvider {

    private final ObjectMapper objectMapper;

    @Autowired
    public PartyAddedToCaseThroughDigitalServiceContentProvider(@Value("${ccd.ui.base.url}") String uiBaseUrl,
                                                                ObjectMapper objectMapper,
                                                                DateFormatterService dateFormatterService,
                                                                HearingBookingService hearingBookingService) {
        super(uiBaseUrl,dateFormatterService,hearingBookingService);
        this.objectMapper = objectMapper;
    }

    public Map<String, Object> buildPartyAddedToCaseNotification(final CaseDetails caseDetails) {
        CaseData caseData = objectMapper.convertValue(caseDetails.getData(), CaseData.class);
        return Map.of(
            "firstRespondentLastName", isNull(caseData.getRespondents1()) ? ""
                : caseData.getRespondents1()
                .get(0).getValue().getParty().getLastName(),
            "familyManCaseNumber", isNull(caseData.getFamilyManCaseNumber()) ? ""
                : caseData.getFamilyManCaseNumber(),
            "caseUrl", uiBaseUrl + "/case/" + JURISDICTION + "/" + CASE_TYPE + "/" + caseDetails.getId());
    }
}
