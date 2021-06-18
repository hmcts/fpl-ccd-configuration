package uk.gov.hmcts.reform.fpl.service.email.content;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.notify.noticeofchange.NoticeOfChangeRespondentSolicitorTemplate;
import uk.gov.hmcts.reform.fpl.service.email.content.base.AbstractEmailContentProvider;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Component
public class NoticeOfChangeContentProvider extends AbstractEmailContentProvider {

    public NoticeOfChangeRespondentSolicitorTemplate buildNoticeOfChangeRespondentSolicitorTemplate(
        CaseData caseData, RespondentSolicitor solicitor) {

        return NoticeOfChangeRespondentSolicitorTemplate.builder()
            .salutation(getSalutation(solicitor))
            .caseName(caseData.getCaseName())
            .ccdNumber(caseData.getId().toString())
            .caseUrl(getCaseUrl(caseData.getId()))
            .build();
    }

    private String getSalutation(RespondentSolicitor solicitor) {
        final String representativeName = solicitor.getFullName();
        return isBlank(representativeName) ? EMPTY : "Dear " + representativeName;
    }
}
