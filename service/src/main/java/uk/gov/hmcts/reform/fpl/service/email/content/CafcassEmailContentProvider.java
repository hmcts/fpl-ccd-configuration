package uk.gov.hmcts.reform.fpl.service.email.content;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.MapperService;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Map;

@Service
public class CafcassEmailContentProvider extends AbstractEmailContentProvider {

    private final LocalAuthorityNameLookupConfiguration localAuthorityNameLookupConfiguration;
    private final CafcassLookupConfiguration cafcassLookupConfiguration;
    private final ObjectMapper objectMapper;
    private final MapperService service;

    @Autowired
    public CafcassEmailContentProvider(LocalAuthorityNameLookupConfiguration localAuthorityNameLookupConfiguration,
                                       CafcassLookupConfiguration cafcassLookupConfiguration,
                                       @Value("${ccd.ui.base.url}") String uiBaseUrl, ObjectMapper objectMapper, MapperService service) {
        super(uiBaseUrl);
        this.localAuthorityNameLookupConfiguration = localAuthorityNameLookupConfiguration;
        this.cafcassLookupConfiguration = cafcassLookupConfiguration;
        this.objectMapper = objectMapper;
        this.service = service;
    }

    public Map<String, Object> buildCafcassSubmissionNotification(CaseDetails caseDetails, String localAuthorityCode) {
        return super.getCasePersonalisationBuilder(caseDetails)
            .put("cafcass", cafcassLookupConfiguration.getCafcass(localAuthorityCode).getName())
            .put("localAuthority", localAuthorityNameLookupConfiguration.getLocalAuthorityName(localAuthorityCode))
            .build();
    }

    public Map<String, Object> buildCafcassSDOSubmissionNotification(CaseDetails caseDetails, String localAuthorityCode) {
        CaseData caseData = service.mapObject(caseDetails.getData(), CaseData.class);
        String leadRespondentsName = caseData.getRespondents1().get(0).getValue().getParty().getLastName();
        String leadRespondentsNameCapitalized = leadRespondentsName.substring(0, 1).toUpperCase() + leadRespondentsName.substring(1).toLowerCase();
        String familyManCaseNumber = caseData.getFamilyManCaseNumber();
        LocalDate hearingDate = caseData.getHearingDetails().get(0).getValue().getDate();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd LLLL yyyy");
        String formattedHearingDate = hearingDate.format(formatter);

        return super.getCasePersonalisationBuilder(caseDetails)
            .put("cafcass", cafcassLookupConfiguration.getCafcass(localAuthorityCode).getName())
            .put("familyManCaseNumber", familyManCaseNumber)
            .put("leadRespondentsName", leadRespondentsNameCapitalized)
            .put("hearingDate",formattedHearingDate)
            .build();
    }
}
