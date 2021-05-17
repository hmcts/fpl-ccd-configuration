package uk.gov.hmcts.reform.fpl.service.email.content.respondentsolicitor;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.notify.respondentsolicitor.RegisteredRespondentSolicitorTemplate;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class RegisteredRespondentSolicitorContentProvider {

    private final LocalAuthorityNameLookupConfiguration localAuthorityNameLookup;

    public RegisteredRespondentSolicitorTemplate buildRespondentSolicitorSubmissionNotification(
        CaseData caseData, RespondentSolicitor representative) {

        return RegisteredRespondentSolicitorTemplate.builder()
            .salutation(getSalutation(representative))
            .localAuthority(localAuthorityNameLookup.getLocalAuthorityName(caseData.getCaseLocalAuthority()))
            .build();
    }

    private String getSalutation(RespondentSolicitor representative) {
        final String representativeName = representative.getFullName();
        return isBlank(representativeName) ? EMPTY : "Dear " + representativeName;
    }
}
