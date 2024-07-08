package uk.gov.hmcts.reform.fpl.service.email.content;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.ReturnApplication;
import uk.gov.hmcts.reform.fpl.model.notify.returnedcase.ReturnedCaseTemplate;
import uk.gov.hmcts.reform.fpl.service.email.content.base.AbstractEmailContentProvider;
import uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static uk.gov.hmcts.reform.fpl.utils.PeopleInCaseHelper.getFirstRespondentFullName;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ReturnedCaseContentProvider extends AbstractEmailContentProvider {
    private final LocalAuthorityNameLookupConfiguration laLookup;
    private final EmailNotificationHelper helper;


    public ReturnedCaseTemplate parametersWithCaseUrl(CaseData caseData) {
        return templateData(caseData)
            .caseUrl(getCaseUrl(caseData.getId()))
            .build();
    }

    public ReturnedCaseTemplate parametersWithApplicationLink(CaseData caseData) {
        return templateData(caseData)
            .applicationDocumentUrl(linkToAttachedDocument(caseData.getC110A().getSubmittedForm()))
            .build();
    }

    private ReturnedCaseTemplate.ReturnedCaseTemplateBuilder templateData(CaseData caseData) {
        ReturnApplication returnApplication = caseData.getReturnApplication();

        return ReturnedCaseTemplate.builder()
            .localAuthority(caseData.getApplicantName().orElse("The applicant"))
            .respondentFullName(getFirstRespondentFullName(caseData.getRespondents1()))
            .lastName(helper.getEldestChildLastName(caseData.getAllChildren()))
            .familyManCaseNumber(defaultIfNull(caseData.getFamilyManCaseNumber(), ""))
            .returnedReasons(returnApplication.getFormattedReturnReasons())
            .returnedNote(returnApplication.getNote());
    }
}
