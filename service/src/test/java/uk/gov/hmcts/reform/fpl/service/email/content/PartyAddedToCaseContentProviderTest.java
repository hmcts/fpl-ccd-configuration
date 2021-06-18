package uk.gov.hmcts.reform.fpl.service.email.content;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.notify.PartyAddedNotifyData;
import uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.EMAIL;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ContextConfiguration(classes = {PartyAddedToCaseContentProvider.class})
class PartyAddedToCaseContentProviderTest extends AbstractEmailContentProviderTest {

    private static final List<Element<Child>> CHILDREN = wrapElements(mock(Child.class));
    private static final String RESPONDENT_LAST_NAME = "Fulgrim";
    private static final String CHILD_LAST_NAME = "Perturabo";
    private static final CaseData CASE_DATA = CaseData.builder()
        .id(Long.valueOf(CASE_REFERENCE))
        .familyManCaseNumber(CASE_REFERENCE)
        .children1(CHILDREN)
        .respondents1(wrapElements(Respondent.builder()
            .party(RespondentParty.builder().lastName(RESPONDENT_LAST_NAME).build())
            .build()))
        .build();

    @MockBean
    private EmailNotificationHelper helper;

    @Autowired
    private PartyAddedToCaseContentProvider underTest;

    @BeforeEach
    void setUp() {
        when(helper.getEldestChildLastName(CHILDREN)).thenReturn(CHILD_LAST_NAME);
    }

    @Test
    void shouldGetPartyAddedToCaseByEmailNotificationParameters() {
        final PartyAddedNotifyData expectedParameters = PartyAddedNotifyData.builder()
            .firstRespondentLastName(RESPONDENT_LAST_NAME)
            .familyManCaseNumber(CASE_REFERENCE)
            .childLastName(CHILD_LAST_NAME)
            .build();

        final PartyAddedNotifyData actualParameters = underTest.getPartyAddedToCaseNotificationParameters(
            CASE_DATA, EMAIL
        );

        assertThat(actualParameters).isEqualTo(expectedParameters);
    }

    @Test
    void shouldGetPartyAddedToCaseThroughDigitalServiceNotificationParameters() {
        final PartyAddedNotifyData expectedParameters = PartyAddedNotifyData.builder()
            .firstRespondentLastName(RESPONDENT_LAST_NAME)
            .familyManCaseNumber(CASE_REFERENCE)
            .caseUrl(caseUrl(CASE_REFERENCE))
            .childLastName(CHILD_LAST_NAME)
            .build();

        final PartyAddedNotifyData actualParameters = underTest.getPartyAddedToCaseNotificationParameters(
            CASE_DATA, DIGITAL_SERVICE
        );

        assertThat(actualParameters).isEqualTo(expectedParameters);
    }
}
