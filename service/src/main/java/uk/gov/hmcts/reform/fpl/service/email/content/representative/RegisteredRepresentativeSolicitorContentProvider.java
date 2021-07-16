package uk.gov.hmcts.reform.fpl.service.email.content.representative;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.common.Party;
import uk.gov.hmcts.reform.fpl.model.interfaces.WithSolicitor;
import uk.gov.hmcts.reform.fpl.model.notify.representative.RegisteredRepresentativeSolicitorTemplate;
import uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.logging.log4j.util.Strings.isBlank;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class RegisteredRepresentativeSolicitorContentProvider {

    private static final String MANAGE_ORG_URL = "https://manage-org.platform.hmcts.net";
    private final LocalAuthorityNameLookupConfiguration localAuthorityNameLookup;
    private final EmailNotificationHelper helper;

    public <R extends WithSolicitor> RegisteredRepresentativeSolicitorTemplate buildContent(CaseData caseData,
                                                                                            R representable) {
        Party party = representable.toParty();

        return RegisteredRepresentativeSolicitorTemplate.builder()
            .salutation(getSalutation(representable.getSolicitor()))
            .clientFullName(isNull(party) ? EMPTY : party.getFullName())
            .localAuthority(localAuthorityNameLookup.getLocalAuthorityName(caseData.getCaseLocalAuthority()))
            .ccdNumber(caseData.getId().toString())
            .caseName(caseData.getCaseName())
            .manageOrgLink(MANAGE_ORG_URL)
            .childLastName(helper.getEldestChildLastName(caseData.getAllChildren()))
            .build();
    }

    private String getSalutation(RespondentSolicitor representative) {
        final String representativeName = representative.getFullName();
        return isBlank(representativeName) ? EMPTY : "Dear " + representativeName;
    }
}
