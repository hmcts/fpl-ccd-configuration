package uk.gov.hmcts.reform.fpl.service.email.content;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.notify.submittedcase.RespondentSolicitorTemplate;
import uk.gov.hmcts.reform.fpl.service.email.content.base.SharedNotifyContentProvider;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RespondentSolicitorContentProvider extends SharedNotifyContentProvider {

    private final LocalAuthorityNameLookupConfiguration localAuthorityNameLookup;

    public RespondentSolicitorTemplate buildRespondentSolicitorSubmissionNotification(
        CaseData caseData, RespondentSolicitor representative) {

        return RespondentSolicitorTemplate.builder()
            .representativeName(representative.getFirstName() + " " + representative.getLastName())
            .localAuthority(localAuthorityNameLookup.getLocalAuthorityName(caseData.getCaseLocalAuthority()))
            .build();
    }
}
