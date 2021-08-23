package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityEmailLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.LegalRepresentative;
import uk.gov.hmcts.reform.fpl.model.notify.LocalAuthorityInboxRecipientsRequest;

import java.util.HashSet;
import java.util.Set;

import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class InboxLookupService {
    private final LocalAuthorityEmailLookupConfiguration localAuthorityEmailLookupConfiguration;
    private final FeatureToggleService featureToggleService;
    private final ApplicantLocalAuthorityService localAuthorityService;

    @Value("${fpl.local_authority_fallback_inbox}")
    private String fallbackInbox;

    public Set<String> getRecipients(LocalAuthorityInboxRecipientsRequest request) {
        CaseData caseData = request.getCaseData();
        Set<String> recipients = new HashSet<>();
        localAuthorityEmailLookupConfiguration.getLocalAuthority(caseData.getCaseLocalAuthority())
            .filter(StringUtils::isNotBlank)
            .ifPresent(recipients::add);

        if (recipients.isEmpty() || featureToggleService.isSendLAEmailsToSolicitorEnabled(
            caseData.getCaseLocalAuthority())) {
            recipients.addAll(localAuthorityService.getContactsEmails(caseData));
        }

        if (!request.isExcludeLegalRepresentatives()) {
            unwrapElements(caseData.getLegalRepresentatives()).stream().map(
                LegalRepresentative::getEmail
            ).forEach(recipients::add);
        }

        if (recipients.isEmpty()) {
            recipients.add(fallbackInbox);
        }

        return recipients;
    }
}
