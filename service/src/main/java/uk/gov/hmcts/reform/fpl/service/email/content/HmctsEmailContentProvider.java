package uk.gov.hmcts.reform.fpl.service.email.content;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.LocalAuthority;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.notify.submittedcase.SubmitCaseHmctsTemplate;
import uk.gov.hmcts.reform.fpl.service.CourtService;
import uk.gov.hmcts.reform.fpl.service.email.content.base.SharedNotifyContentProvider;

import static java.util.Objects.nonNull;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class HmctsEmailContentProvider extends SharedNotifyContentProvider {
    private final LocalAuthorityNameLookupConfiguration laNameLookup;
    private final CourtService courtService;

    public SubmitCaseHmctsTemplate buildHmctsSubmissionNotification(CaseData caseData) {
        SubmitCaseHmctsTemplate template = buildNotifyTemplate(SubmitCaseHmctsTemplate.builder().build(), caseData);

        template.setCourt(courtService.getCourtName(caseData));
        template.setLocalAuthority(nonNull(caseData.getCaseLocalAuthority())
            ? laNameLookup.getLocalAuthorityName(caseData.getCaseLocalAuthority()) : getApplicantName(caseData));
        template.setDocumentLink(getDocumentUrl(caseData.getC110A().getSubmittedForm()));

        return template;
    }

    public String getApplicantName(CaseData caseData) {
        LocalAuthority applicant = caseData.getLocalAuthorities().stream()
            .map(Element::getValue)
            .findFirst()
            .orElse(null);

        return nonNull(applicant) ? applicant.getName() : null;
    }
}
