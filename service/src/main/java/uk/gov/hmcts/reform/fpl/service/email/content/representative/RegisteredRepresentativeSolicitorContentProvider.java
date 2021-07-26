package uk.gov.hmcts.reform.fpl.service.email.content.representative;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.common.Party;
import uk.gov.hmcts.reform.fpl.model.interfaces.WithSolicitor;
import uk.gov.hmcts.reform.fpl.model.notify.representative.RegisteredRepresentativeSolicitorTemplate;
import uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class RegisteredRepresentativeSolicitorContentProvider {

    private static final String MANAGE_ORG_URL = "https://manage-org.platform.hmcts.net";
    private final LocalAuthorityNameLookupConfiguration localAuthorityNameLookup;
    private final EmailNotificationHelper helper;

    public <R extends WithSolicitor> RegisteredRepresentativeSolicitorTemplate buildContent(CaseData caseData,
                                                                                            R representable) {
        return buildContent(caseData, representable.getSolicitor(), List.of(representable));
    }

    public <R extends WithSolicitor> RegisteredRepresentativeSolicitorTemplate buildContent(
        CaseData caseData, RespondentSolicitor solicitor, List<R> representables) {

        return RegisteredRepresentativeSolicitorTemplate.builder()
            .salutation(getSalutation(solicitor))
            .clientFullName(clientNames(representables))
            .localAuthority(localAuthorityNameLookup.getLocalAuthorityName(caseData.getCaseLocalAuthority()))
            .ccdNumber(caseData.getId().toString())
            .caseName(caseData.getCaseName())
            .manageOrgLink(MANAGE_ORG_URL)
            .childLastName(helper.getEldestChildLastName(caseData.getAllChildren()))
            .build();
    }

    private <R extends WithSolicitor> String clientNames(List<R> clients) {
        return clients.stream()
            .map(WithSolicitor::toParty)
            .filter(Objects::nonNull)
            .map(Party::getFullName)
            .filter(StringUtils::isNotBlank)
            .collect(Collectors.joining(", "));
    }

    private String getSalutation(RespondentSolicitor representative) {
        final String representativeName = representative.getFullName();
        return isBlank(representativeName) ? EMPTY : "Dear " + representativeName;
    }
}
