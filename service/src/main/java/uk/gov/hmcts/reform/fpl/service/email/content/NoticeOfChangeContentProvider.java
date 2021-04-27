package uk.gov.hmcts.reform.fpl.service.email.content;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.notify.noticeofchange.RespondentSolicitorNoticeOfChangeTemplate;
import uk.gov.hmcts.reform.fpl.service.email.content.base.SharedNotifyContentProvider;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class NoticeOfChangeContentProvider extends SharedNotifyContentProvider {

    public RespondentSolicitorNoticeOfChangeTemplate buildRespondentSolicitorAccessGrantedNotification(
        CaseData caseData, RespondentSolicitor solicitor) {

        return RespondentSolicitorNoticeOfChangeTemplate.builder()
            .salutation(getSalutation(solicitor))
            .caseName(caseData.getCaseName())
            .ccdNumber(caseData.getId().toString())
            .caseUrl(getCaseUrl(caseData.getId()))
            .build();
    }

    public RespondentSolicitorNoticeOfChangeTemplate buildRespondentSolicitorAccessRevokedNotification(
        CaseData caseData, RespondentSolicitor solicitor) {

        return RespondentSolicitorNoticeOfChangeTemplate.builder()
            .salutation(getSalutation(solicitor))
            .caseName(caseData.getCaseName())
            .ccdNumber(caseData.getId().toString())
            .build();
    }

    private String getSalutation(RespondentSolicitor solicitor) {
        final String representativeName = solicitor.getFullName();
        return isBlank(representativeName) ? EMPTY : "Dear " + representativeName;
    }
}
