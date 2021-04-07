package uk.gov.hmcts.reform.fpl.components;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.fpl.enums.SolicitorRole;
import uk.gov.hmcts.reform.fpl.model.Applicant;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.noticeofchange.NoticeOfChangeAnswers;
import uk.gov.hmcts.reform.fpl.model.noticeofchange.NoticeOfChangeRespondent;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class NoticeOfChangeRespondentConverter {
    public NoticeOfChangeRespondent convert(Element<Respondent> respondentElement,
                                            Applicant applicant,
                                            SolicitorRole solicitorRole) {
        RespondentParty respondentParty = respondentElement.getValue().getParty();

        return NoticeOfChangeRespondent.builder()
            .noticeOfChangeAnswers(NoticeOfChangeAnswers.builder()
                .respondentFirstName(respondentParty.getFirstName())
                .respondentLastName(respondentParty.getLastName())
                .respondentDOB(respondentParty.getDateOfBirth())
                .applicantName(applicant.getParty().getOrganisationName())
                .build())
            .respondentId(respondentElement.getId())
            .organisationPolicy(setOrganisationPolicy(respondentElement.getValue().getSolicitor(), solicitorRole))
            .build();
    }

    private OrganisationPolicy setOrganisationPolicy(RespondentSolicitor respondentSolicitor,
                                                     SolicitorRole solicitorRole) {
        OrganisationPolicy.OrganisationPolicyBuilder organisationPolicy = OrganisationPolicy.builder();

        if (hasOrganisation(respondentSolicitor)) {
            organisationPolicy.organisation(respondentSolicitor.getOrganisation());
        }

        organisationPolicy.orgPolicyCaseAssignedRole(solicitorRole.getCaseRoleLabel());
        return organisationPolicy.build();
    }

    private boolean hasOrganisation(RespondentSolicitor respondentSolicitor) {
        return isNotEmpty(respondentSolicitor) && isNotEmpty(respondentSolicitor.getOrganisation());
    }
}
