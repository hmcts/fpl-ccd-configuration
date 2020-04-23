package uk.gov.hmcts.reform.fpl.service.email.content;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.CafcassSubmissionTemplate;
import uk.gov.hmcts.reform.fpl.service.email.content.base.CasePersonalisedContentProvider;

import java.util.Map;

@Service
public class CafcassEmailContentProvider extends CasePersonalisedContentProvider {
    private final LocalAuthorityNameLookupConfiguration localAuthorityNameLookupConfiguration;
    private final CafcassLookupConfiguration cafcassLookupConfiguration;

    @Autowired
    protected CafcassEmailContentProvider(@Value("${ccd.ui.base.url}") String uiBaseUrl,
                                          ObjectMapper mapper,
                                          LocalAuthorityNameLookupConfiguration localAuthorityNameLookupConfiguration,
                                          CafcassLookupConfiguration cafcassLookupConfiguration) {
        super(uiBaseUrl, mapper);
        this.localAuthorityNameLookupConfiguration = localAuthorityNameLookupConfiguration;
        this.cafcassLookupConfiguration = cafcassLookupConfiguration;
    }

    public Map<String, Object> buildCafcassSubmissionNotification(CaseDetails caseDetails, String localAuthorityCode) {
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        mapper.convertValue(getCasePersonalisationBuilder(caseDetails.getId(), caseData), CafcassSubmissionTemplate.class);

        return super.getCasePersonalisationBuilder(caseDetails.getId(), caseData)

            .put("cafcass", cafcassLookupConfiguration.getCafcass(localAuthorityCode).getName())
            .put("localAuthority", localAuthorityNameLookupConfiguration.getLocalAuthorityName(localAuthorityCode))
            .build();
    }
}
