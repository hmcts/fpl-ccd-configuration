package uk.gov.hmcts.reform.fpl.service.email.content;

import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.LegalRepresentative;
import uk.gov.hmcts.reform.fpl.service.email.content.base.AbstractEmailContentProvider;

import java.util.Map;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static uk.gov.hmcts.reform.fpl.utils.PeopleInCaseHelper.getFirstRespondentLastName;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class LegalRepresentativeAddedContentProvider extends AbstractEmailContentProvider {

    private final LocalAuthorityNameLookupConfiguration nameLookupConfiguration;

    public Map<String, Object> getParameters(LegalRepresentative legalRepresentative,
                                             CaseData caseData) {
        //TODO: add template
        ImmutableMap.Builder<String, Object> notificationParams =
            ImmutableMap.<String, Object>builder()
                .put("repName", legalRepresentative.getFullName())
                .put("localAuthority", nameLookupConfiguration.getLocalAuthorityName(caseData.getCaseLocalAuthority()))
                .put("firstRespondentLastName", getFirstRespondentLastName(caseData.getRespondents1()))
                .put("familyManCaseNumber", defaultIfNull(caseData.getFamilyManCaseNumber(), ""))
                .put("caseUrl", getCaseUrl(caseData.getId()));

        return notificationParams.build();
    }

}
