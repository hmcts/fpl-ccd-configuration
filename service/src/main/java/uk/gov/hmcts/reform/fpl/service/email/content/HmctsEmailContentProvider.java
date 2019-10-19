package uk.gov.hmcts.reform.fpl.service.email.content;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.config.HmctsCourtLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.DateFormatterService;

import java.time.format.FormatStyle;
import java.util.Map;
import java.util.Objects;

@Service
public class HmctsEmailContentProvider extends AbstractEmailContentProvider {

    private final LocalAuthorityNameLookupConfiguration localAuthorityNameLookupConfiguration;
    private final HmctsCourtLookupConfiguration hmctsCourtLookupConfiguration;
    private final ObjectMapper mapper;
    private final DateFormatterService dateFormatterService;

    @Autowired
    public HmctsEmailContentProvider(LocalAuthorityNameLookupConfiguration localAuthorityNameLookupConfiguration,
                                     HmctsCourtLookupConfiguration hmctsCourtLookupConfiguration,
                                     ObjectMapper mapper,
                                     DateFormatterService dateFormatterService,
                                     @Value("${ccd.ui.base.url}") String uiBaseUrl) {
        super(uiBaseUrl);
        this.localAuthorityNameLookupConfiguration = localAuthorityNameLookupConfiguration;
        this.hmctsCourtLookupConfiguration = hmctsCourtLookupConfiguration;
        this.mapper = mapper;
        this.dateFormatterService = dateFormatterService;
    }

    public Map<String, Object> buildHmctsSubmissionNotification(CaseDetails caseDetails, String localAuthorityCode) {
        return super.getCasePersonalisationBuilder(caseDetails)
            .put("court", hmctsCourtLookupConfiguration.getCourt(localAuthorityCode).getName())
            .put("localAuthority", localAuthorityNameLookupConfiguration.getLocalAuthorityName(localAuthorityCode))
            .build();
    }

    public Map<String, Object> buildC2UploadNotification(final CaseDetails caseDetails,
                                                         final String localAuthorityCode) {
        // Validation within our frontend ensures that the following data is present
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);
        return Map.of(
            "court", hmctsCourtLookupConfiguration.getCourt(localAuthorityCode).getName(),
            "lastNameOfRespondent", caseData.getRespondents1()
                .stream()
                .filter(Objects::nonNull)
                .findFirst()
                .get().getValue().getLastName(),
            "familymanID", caseData.getFamilyManCaseNumber(),
            "hearingDate", dateFormatterService.formatLocalDateToString(
                caseData.getHearingDetails()
                .stream()
                .filter(Objects::nonNull)
                .findFirst()
                .get().getValue().getDate(), FormatStyle.MEDIUM)
        );
    }
}
