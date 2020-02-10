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

import static net.logstash.logback.encoder.org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static uk.gov.hmcts.reform.fpl.utils.PeopleInCaseHelper.getFirstRespondentLastName;

@Service
public class PartyAddedToCaseByEmailContentProvider extends AbstractEmailContentProvider {

    private final ObjectMapper objectMapper;

    @Autowired
    public PartyAddedToCaseByEmailContentProvider(@Value("${ccd.ui.base.url}") String uiBaseUrl,
                                                     ObjectMapper objectMapper,
                                                     DateFormatterService dateFormatterService,
                                                     HearingBookingService hearingBookingService) {
        super(uiBaseUrl,dateFormatterService,hearingBookingService);
        this.objectMapper = objectMapper;
    }

    public Map<String, Object> buildPartyAddedToCaseNotification(final CaseDetails caseDetails) {
        CaseData caseData = objectMapper.convertValue(caseDetails.getData(), CaseData.class);
        return Map.of(
            "firstRespondentLastName", getFirstRespondentLastName(caseData.getRespondents1()),
            "familyManCaseNumber", defaultIfNull(caseData.getFamilyManCaseNumber(), "")
        );
    }
}
