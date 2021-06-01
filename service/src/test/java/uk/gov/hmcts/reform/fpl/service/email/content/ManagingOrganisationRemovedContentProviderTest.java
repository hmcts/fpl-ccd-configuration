package uk.gov.hmcts.reform.fpl.service.email.content;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.ManagingOrganisationRemovedNotifyData;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;
import uk.gov.hmcts.reform.rd.model.Organisation;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.apache.commons.lang3.RandomUtils.nextLong;
import static org.assertj.core.api.Assertions.assertThat;

class ManagingOrganisationRemovedContentProviderTest {

    private ManagingOrganisationRemovedContentProvider underTest = new ManagingOrganisationRemovedContentProvider();

    @Test
    void shouldBuildEmailData() {

        final Organisation organisation = Organisation.builder()
            .name(randomAlphanumeric(10))
            .build();

        final CaseData caseData = CaseData.builder()
            .id(nextLong())
            .caseName(randomAlphanumeric(10))
            .caseLocalAuthorityName(randomAlphanumeric(10))
            .build();

        final NotifyData expectedEmailData = ManagingOrganisationRemovedNotifyData.builder()
            .caseNumber(caseData.getId())
            .caseName(caseData.getCaseName())
            .localAuthorityName(caseData.getCaseLocalAuthorityName())
            .managingOrganisationName(organisation.getName())
            .build();

        final NotifyData actualEmailData = underTest.getEmailData(organisation, caseData);

        assertThat(actualEmailData).isEqualTo(expectedEmailData);
    }
}
