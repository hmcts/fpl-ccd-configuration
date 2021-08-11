package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityEmailLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityIdLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.LegalRepresentative;
import uk.gov.hmcts.reform.fpl.model.LocalAuthority;
import uk.gov.hmcts.reform.fpl.model.Solicitor;
import uk.gov.hmcts.reform.fpl.model.notify.RecipientsRequest;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class LocalAuthorityRecipientsService {
    private final FeatureToggleService featureToggles;
    private final LocalAuthorityIdLookupConfiguration localAuthorityIds;
    private final LocalAuthorityEmailLookupConfiguration localAuthorityInboxes;

    @Value("${fpl.local_authority_fallback_inbox}")
    private String fallbackInbox;

    public Set<String> getRecipients(RecipientsRequest request) {
        final Set<String> recipients = new HashSet<>();

        if (!request.isDesignatedLocalAuthorityExcluded()) {
            recipients.addAll(getDesignatedLocalAuthorityContacts(request));
        }

        if (!request.isSecondaryLocalAuthorityExcluded()) {
            recipients.addAll(getSecondaryLocalAuthorityContacts(request));
        }

        if (!request.isLegalRepresentativesExcluded()) {
            recipients.addAll(getLegalRepresentatives(request));
        }

        if (recipients.isEmpty()) {
            recipients.addAll(getFallbackInbox());
        }

        return recipients.stream()
            .filter(StringUtils::isNotBlank)
            .collect(Collectors.toSet());
    }

    private List<String> getDesignatedLocalAuthorityContacts(RecipientsRequest request) {
        final List<String> recipients = new ArrayList<>();

        if (isNotEmpty(request.getCaseData().getLocalAuthorities())) {
            final Optional<LocalAuthority> localAuthority = getDesignatedLocalAuthority(request.getCaseData());

            localAuthority.map(LocalAuthority::getEmail).ifPresent(recipients::add);

            if (featureToggles.emailsToSolicitorEnabled(request.getCaseData().getCaseLocalAuthority())) {
                localAuthority.map(LocalAuthority::getContactEmails).ifPresent(recipients::addAll);
            }

        } else {
            localAuthorityInboxes.getSharedInbox(request.getCaseData().getCaseLocalAuthority())
                .ifPresent(recipients::add);

            ofNullable(request.getCaseData().getSolicitor())
                .map(Solicitor::getEmail)
                .filter(StringUtils::isNotBlank)
                .ifPresent(recipients::add);
        }

        return recipients;
    }

    private List<String> getSecondaryLocalAuthorityContacts(RecipientsRequest request) {
        final List<String> recipients = new ArrayList<>();

        getSecondaryLocalAuthority(request.getCaseData()).ifPresent(la -> {
            final String localAuthorityCode = localAuthorityIds.getLocalAuthorityCode(la.getId()).orElse(null);

            ofNullable(la.getEmail()).ifPresent(recipients::add);

            if (featureToggles.emailsToSolicitorEnabled(localAuthorityCode)) {
                recipients.addAll(la.getContactEmails());
            }
        });

        return recipients;
    }

    private List<String> getLegalRepresentatives(RecipientsRequest request) {

        return unwrapElements(request.getCaseData().getLegalRepresentatives()).stream()
            .map(LegalRepresentative::getEmail)
            .collect(Collectors.toList());
    }

    private List<String> getFallbackInbox() {
        return List.of(fallbackInbox);
    }

    private Optional<LocalAuthority> getDesignatedLocalAuthority(CaseData caseData) {
        return unwrapElements(caseData.getLocalAuthorities()).stream()
            .filter(la -> YesNo.YES.getValue().equals(la.getDesignated()))
            .findFirst();
    }

    private Optional<LocalAuthority> getSecondaryLocalAuthority(CaseData caseData) {
        return unwrapElements(caseData.getLocalAuthorities()).stream()
            .filter(la -> !YesNo.YES.getValue().equals(la.getDesignated()))
            .findFirst();
    }
}
