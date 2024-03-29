package uk.gov.hmcts.reform.fpl.service.email.content;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.fpl.exceptions.LocalAuthorityNotFound;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.CaseTransferredNotifyData;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;
import uk.gov.hmcts.reform.fpl.model.notify.SharedLocalAuthorityChangedNotifyData;
import uk.gov.hmcts.reform.fpl.service.CaseUrlService;
import uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.ccd.model.OrganisationPolicy.organisationPolicy;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.LASHARED;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.LASOLICITOR;

class LocalAuthorityChangedContentProviderTest extends AbstractEmailContentProviderTest {

    private final OrganisationPolicy designated = organisationPolicy("ORG1", "Designated", LASOLICITOR);
    private final OrganisationPolicy secondary = organisationPolicy("ORG2", "Secondary", LASHARED);

    @Mock
    private CaseUrlService caseUrlService;

    @Mock
    private EmailNotificationHelper notificationHelper;

    @InjectMocks
    private LocalAuthorityChangedContentProvider underTest;

    @Test
    void shouldGetNotifyDataForAddedLocalAuthority() {

        final CaseData caseData = CaseData.builder()
            .id(10L)
            .caseName("Case name")
            .localAuthorityPolicy(designated)
            .sharedLocalAuthorityPolicy(secondary)
            .build();

        final NotifyData actualData = underTest.getNotifyDataForAddedLocalAuthority(caseData);

        final NotifyData expectedData = SharedLocalAuthorityChangedNotifyData.builder()
            .caseName("Case name")
            .ccdNumber("10")
            .secondaryLocalAuthority("Secondary")
            .designatedLocalAuthority("Designated")
            .build();

        assertThat(actualData).isEqualTo(expectedData);

        verifyNoInteractions(notificationHelper);
    }

    @Test
    void shouldGetNotifyDataForRemovedLocalAuthority() {

        final CaseData caseDataBefore = CaseData.builder()
            .id(10L)
            .caseName("Case name")
            .localAuthorityPolicy(designated)
            .sharedLocalAuthorityPolicy(secondary)
            .build();

        final CaseData caseData = caseDataBefore.toBuilder()
            .sharedLocalAuthorityPolicy(null)
            .build();

        final NotifyData actualData = underTest.getNotifyDataForRemovedLocalAuthority(caseData, caseDataBefore);

        final NotifyData expectedData = SharedLocalAuthorityChangedNotifyData.builder()
            .caseName("Case name")
            .ccdNumber("10")
            .secondaryLocalAuthority("Secondary")
            .designatedLocalAuthority("Designated")
            .build();

        assertThat(actualData).isEqualTo(expectedData);

        verifyNoInteractions(notificationHelper);
    }

    @Test
    void shouldGetNotifyDataForDesignatedLocalAuthority() {

        final CaseData caseData = CaseData.builder()
            .id(10L)
            .caseName("Case name")
            .localAuthorityPolicy(designated)
            .sharedLocalAuthorityPolicy(secondary)
            .build();

        final NotifyData expectedData = SharedLocalAuthorityChangedNotifyData.builder()
            .caseName("Case name")
            .ccdNumber("10")
            .secondaryLocalAuthority("Secondary")
            .designatedLocalAuthority("Designated")
            .childLastName("Smith")
            .build();

        when(notificationHelper.getEldestChildLastName(caseData)).thenReturn("Smith");

        final NotifyData actualData = underTest.getNotifyDataForDesignatedLocalAuthority(caseData);

        assertThat(actualData).isEqualTo(expectedData);

        verify(notificationHelper).getEldestChildLastName(caseData);
    }

    @Test
    void shouldThrowsExceptionWhenNoSecondaryLocalAuthority() {

        final CaseData caseData = CaseData.builder()
            .id(10L)
            .caseName("Case name")
            .localAuthorityPolicy(designated)
            .build();

        assertThatThrownBy(() -> underTest.getNotifyDataForAddedLocalAuthority(caseData))
            .isInstanceOf(LocalAuthorityNotFound.class);

        verifyNoInteractions(notificationHelper);
    }

    @Test
    void shouldGetCaseTransferNotifyData() {
        final CaseData caseDataBefore = CaseData.builder()
            .id(10L)
            .caseName("Case name")
            .localAuthorityPolicy(designated)
            .build();

        final CaseData caseData = caseDataBefore.toBuilder()
            .localAuthorityPolicy(secondary)
            .build();

        when(notificationHelper.getEldestChildLastName(caseData)).thenReturn("Smith");
        when(caseUrlService.getCaseUrl(10L)).thenReturn("http://case.url");

        final NotifyData actualData = underTest.getCaseTransferredNotifyData(caseData, caseDataBefore);

        final NotifyData expectedData = CaseTransferredNotifyData.builder()
            .caseName("Case name")
            .ccdNumber("10")
            .caseUrl("http://case.url")
            .prevDesignatedLocalAuthority("Designated")
            .newDesignatedLocalAuthority("Secondary")
            .childLastName("Smith")
            .build();

        assertThat(actualData).isEqualTo(expectedData);
    }

}
