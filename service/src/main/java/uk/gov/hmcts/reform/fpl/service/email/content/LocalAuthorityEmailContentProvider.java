package uk.gov.hmcts.reform.fpl.service.email.content;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.config.HmctsCourtLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.MapperService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
public class LocalAuthorityEmailContentProvider extends AbstractEmailContentProvider {

    private final LocalAuthorityNameLookupConfiguration localAuthorityNameLookupConfiguration;
    private final HmctsCourtLookupConfiguration hmctsCourtLookupConfiguration;
    private final ObjectMapper objectMapper;
    private final MapperService service;

    @Autowired
    public LocalAuthorityEmailContentProvider(LocalAuthorityNameLookupConfiguration localAuthorityNameLookupConfiguration,
                                              HmctsCourtLookupConfiguration hmctsCourtLookupConfiguration,
                                              @Value("${ccd.ui.base.url}") String uiBaseUrl, ObjectMapper objectMapper, MapperService service) {
        super(uiBaseUrl);
        this.localAuthorityNameLookupConfiguration = localAuthorityNameLookupConfiguration;
        this.hmctsCourtLookupConfiguration = hmctsCourtLookupConfiguration;
        this.objectMapper = objectMapper;
        this.service = service;
    }

    public Map<String, Object> buildLocalAuthoritySDOSubmissionNotification(CaseDetails caseDetails, String localAuthorityCode) {
        CaseData caseData = service.mapObject(caseDetails.getData(), CaseData.class);
        String leadRespondentsName = caseData.getRespondents1().get(0).getValue().getParty().getLastName();
        String leadRespondentsNameCapitalized = leadRespondentsName.substring(0, 1).toUpperCase() + leadRespondentsName.substring(1).toLowerCase();
        String familyManCaseNumber = caseData.getFamilyManCaseNumber();
        LocalDate hearingDate = caseData.getHearingDetails().get(0).getValue().getDate();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd LLLL yyyy");
        String formattedHearingDate = hearingDate.format(formatter);

        return super.getCasePersonalisationBuilder(caseDetails)
            .put("cafcass", localAuthorityNameLookupConfiguration.getLocalAuthorityName(localAuthorityCode))
            .put("familyManCaseNumber", familyManCaseNumber)
            .put("leadRespondentsName", leadRespondentsNameCapitalized)
            .put("hearingDate",formattedHearingDate)
            .build();
    }
}
