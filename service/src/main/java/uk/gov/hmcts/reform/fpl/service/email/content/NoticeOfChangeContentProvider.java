package uk.gov.hmcts.reform.fpl.service.email.content;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.interfaces.WithSolicitor;
import uk.gov.hmcts.reform.fpl.model.notify.noticeofchange.NoticeOfChangeRespondentSolicitorTemplate;
import uk.gov.hmcts.reform.fpl.service.email.content.base.AbstractEmailContentProvider;
import uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class NoticeOfChangeContentProvider extends AbstractEmailContentProvider {
    private final EmailNotificationHelper helper;

    public NoticeOfChangeRespondentSolicitorTemplate buildNoticeOfChangeRespondentSolicitorTemplate(
        CaseData caseData, WithSolicitor party) {

        return NoticeOfChangeRespondentSolicitorTemplate.builder()
            .salutation(isNull(party.getSolicitor()) ? EMPTY : getSalutation(party.getSolicitor()))
            .caseName(caseData.getCaseName())
            .ccdNumber(caseData.getId().toString())
            .caseUrl(getCaseUrl(caseData.getId()))
            .clientFullName(isNull(party.toParty()) ? EMPTY : party.toParty().getFullName())
            .childLastName(helper.getEldestChildLastName(caseData.getChildren1()))
            .build();
    }

    public NoticeOfChangeRespondentSolicitorTemplate buildNoticeOfChangeThirdPartySolicitorTemplate(CaseData caseData) {

        return NoticeOfChangeRespondentSolicitorTemplate.builder()
            .salutation(EMPTY)
            .caseName(caseData.getCaseName())
            .ccdNumber(caseData.getId().toString())
            .caseUrl(getCaseUrl(caseData.getId()))
            .clientFullName(EMPTY)
            .childLastName(helper.getEldestChildLastName(caseData.getChildren1()))
            .build();

    }

    private String getSalutation(RespondentSolicitor solicitor) {
        final String representativeName = solicitor.getFullName();
        return isBlank(representativeName) ? EMPTY : "Dear " + representativeName;
    }
}
