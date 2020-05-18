package uk.gov.hmcts.reform.fpl.service.email.content;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.submittedcase.SubmitCaseCafcassTemplate;
import uk.gov.hmcts.reform.fpl.service.email.content.base.SharedNotifyContentProvider;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CafcassEmailContentProvider extends SharedNotifyContentProvider {
    private final LocalAuthorityNameLookupConfiguration localAuthorityNameLookupConfiguration;
    private final CafcassLookupConfiguration cafcassLookupConfiguration;
    private final ObjectMapper mapper;

    public SubmitCaseCafcassTemplate buildCafcassSubmissionNotification(CaseDetails caseDetails,
                                                                        String localAuthorityCode) {

        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        SubmitCaseCafcassTemplate template = buildNotifyTemplate(new SubmitCaseCafcassTemplate(),
            caseDetails.getId(), caseData.getOrders(), caseData.getHearing(), caseData.getRespondents1());

        template.setCafcass(cafcassLookupConfiguration.getCafcass(localAuthorityCode).getName());
        template.setLocalAuthority(localAuthorityNameLookupConfiguration.getLocalAuthorityName(localAuthorityCode));

        return template;
    }
}
