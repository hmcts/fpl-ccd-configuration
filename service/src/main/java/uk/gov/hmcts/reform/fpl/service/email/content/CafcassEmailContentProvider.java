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

    public CafcassSubmissionTemplate buildCafcassSubmissionNotification(CaseDetails caseDetails,
                                                                        String localAuthorityCode) {

        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        CafcassSubmissionTemplate template = addCasePersonalisationBuilder(new CafcassSubmissionTemplate(),
            caseDetails.getId(),
            caseData.getOrders(),
            caseData.getHearing(),
            caseData.getRespondents1());

        template.setCafcass(cafcassLookupConfiguration.getCafcass(localAuthorityCode).getName());
        template.setLocalAuthority(localAuthorityNameLookupConfiguration.getLocalAuthorityName(localAuthorityCode));

        return template;
    }
}
