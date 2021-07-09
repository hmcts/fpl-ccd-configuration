package uk.gov.hmcts.reform.fpl.service.email.content.respondentsolicitor;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.notify.respondentsolicitor.RegisteredRespondentSolicitorTemplate;
import uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.logging.log4j.util.Strings.isBlank;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class RegisteredRespondentSolicitorContentProvider {

    private static final String MANAGE_ORG_URL = "https://manage-org.platform.hmcts.net";
    private final LocalAuthorityNameLookupConfiguration localAuthorityNameLookup;
    private final EmailNotificationHelper helper;

    public RegisteredRespondentSolicitorTemplate buildRespondentSolicitorSubmissionNotification(
        CaseData caseData, Respondent respondent) {

        String respondentName = isNull(respondent.getParty()) ? EMPTY : respondent.getParty().getFullName();

        return RegisteredRespondentSolicitorTemplate.builder()
            .salutation(getSalutation(respondent.getSolicitor()))
            .clientFullName(respondentName)
            .localAuthority(localAuthorityNameLookup.getLocalAuthorityName(caseData.getCaseLocalAuthority()))
            .ccdNumber(caseData.getId().toString())
            .caseName(caseData.getCaseName())
            .manageOrgLink(MANAGE_ORG_URL)
            .childLastName(helper.getEldestChildLastName(caseData.getChildren1()))
            .build();
    }

    private String getSalutation(RespondentSolicitor representative) {
        final String representativeName = representative.getFullName();
        return isBlank(representativeName) ? EMPTY : "Dear " + representativeName;
    }
}
