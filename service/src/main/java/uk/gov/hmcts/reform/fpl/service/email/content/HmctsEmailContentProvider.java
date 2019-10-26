package uk.gov.hmcts.reform.fpl.service.email.content;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.config.HmctsCourtLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;

import java.util.Map;

@Service
public class HmctsEmailContentProvider extends AbstractEmailContentProvider {

    private final LocalAuthorityNameLookupConfiguration localAuthorityNameLookupConfiguration;
    private final HmctsCourtLookupConfiguration hmctsCourtLookupConfiguration;

    @Autowired
    public HmctsEmailContentProvider(LocalAuthorityNameLookupConfiguration localAuthorityNameLookupConfiguration,
                                     HmctsCourtLookupConfiguration hmctsCourtLookupConfiguration,
                                     @Value("${ccd.ui.base.url}") String uiBaseUrl) {
        super(uiBaseUrl);
        this.localAuthorityNameLookupConfiguration = localAuthorityNameLookupConfiguration;
        this.hmctsCourtLookupConfiguration = hmctsCourtLookupConfiguration;
    }

    public Map<String, Object> buildC21OrderNotification(final CaseDetails caseDetails,
                                                         final String localAuthorityCode) {
        // Validation within our frontend ensures that the following data is present
        CaseData caseData = mapper.getObjectMapper().convertValue(caseDetails.getData(), CaseData.class);

        List<Map<String, Object>> respondents1 =
            (ObjectUtils.isEmpty(caseDetails.getData().get("respondents1"))
                ? Collections.emptyList() : mapper.getObjectMapper().convertValue(
                caseDetails.getData().get("respondents1"), new TypeReference<>() {}));

        List<Respondent> respondents = (CollectionUtils.isEmpty(respondents1)
            ? Collections.emptyList() : respondents1.stream().map(
            respondent -> mapper.getObjectMapper().convertValue(
                respondent.get("value"), Respondent.class)).collect(toList()));

        return super.getCasePersonalisationBuilder(caseDetails)
            .put("court", hmctsCourtLookupConfiguration.getCourt(localAuthorityCode).getName())
            .put("lastNameOfRespondent", getRespondent1Lastname(respondents))
            .put("familyManCaseNumber", StringUtils.defaultIfBlank(caseData.getFamilyManCaseNumber(), ""))
            .put("hearingDate", dateFormatterService.formatLocalDateToString(
                getHearingBookingDate(caseData), FormatStyle.MEDIUM))
            .build();
    }

    public Map<String, Object> buildHmctsSubmissionNotification(CaseDetails caseDetails, String localAuthorityCode) {
        return super.getCasePersonalisationBuilder(caseDetails)
            .put("court", hmctsCourtLookupConfiguration.getCourt(localAuthorityCode).getName())
            .put("localAuthority", localAuthorityNameLookupConfiguration.getLocalAuthorityName(localAuthorityCode))
            .build();
    }
}
