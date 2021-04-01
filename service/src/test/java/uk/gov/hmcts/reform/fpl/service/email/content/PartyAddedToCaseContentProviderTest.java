package uk.gov.hmcts.reform.fpl.service.email.content;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.PartyAddedNotifyData;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.EMAIL;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.caseData;

@ContextConfiguration(classes = {PartyAddedToCaseContentProvider.class})
class PartyAddedToCaseContentProviderTest extends AbstractEmailContentProviderTest {

    @Autowired
    private PartyAddedToCaseContentProvider partyAddedToCaseContentProvider;

    @Test
    void shouldGetPartyAddedToCaseByEmailNotificationParameters() {
        final CaseData caseData = caseData();

        final PartyAddedNotifyData expectedParameters = PartyAddedNotifyData.builder()
            .firstRespondentLastName("Smith")
            .familyManCaseNumber("12345L")
            .build();

        final PartyAddedNotifyData actualParameters = partyAddedToCaseContentProvider
            .getPartyAddedToCaseNotificationParameters(caseData, EMAIL);

        assertThat(actualParameters).isEqualTo(expectedParameters);
    }

    @Test
    void shouldGetPartyAddedToCaseThroughDigitalServiceNotificationParameters() {
        final CaseData caseData = caseData();
        final PartyAddedNotifyData expectedParameters = PartyAddedNotifyData.builder()
            .firstRespondentLastName("Smith")
            .familyManCaseNumber("12345L")
            .caseUrl(caseUrl(CASE_REFERENCE))
            .build();

        final PartyAddedNotifyData actualParameters = partyAddedToCaseContentProvider
            .getPartyAddedToCaseNotificationParameters(caseData, DIGITAL_SERVICE);

        assertThat(actualParameters).isEqualTo(expectedParameters);
    }
}
