package uk.gov.hmcts.reform.fpl.service.email.content;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.notify.submittedcase.RespondentSolicitorTemplate;
import uk.gov.hmcts.reform.fpl.service.email.content.base.SharedNotifyContentProvider;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))

public class RespondentSolicitorContentProvider extends SharedNotifyContentProvider {

    public RespondentSolicitorTemplate buildNotifyRespondentSolicitorTemplate(CaseData caseData, RespondentSolicitor representative) {
        return RespondentSolicitorTemplate.builder()
            .representativeName(representative.getFirstName() + " " + representative.getLastName())
            .localAuthority(caseData.getCaseLocalAuthorityName())
            .build();
    }
}
