package uk.gov.hmcts.reform.fpl.service.email.content;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.EmailAddress;
import uk.gov.hmcts.reform.fpl.model.notify.orderremoval.OrderRemovalTemplate;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ContextConfiguration(classes = {OrderRemovalEmailContentProvider.class})
class OrderRemovalEmailContentProviderTest extends AbstractEmailContentProviderTest {

    private static final Long CASE_ID = 12345L;
    private static final String FAKE_URL = "http://fake-url/cases/case-details/12345";
    private static final String REMOVAL_REASON = "removal reason test";
    private static final String GATEKEEPER_EMAIL_ADDRESS = "FamilyPublicLaw+gatekeeper@gmail.com";

    @Autowired
    private OrderRemovalEmailContentProvider orderRemovalEmailContentProvider;

    @Test
    void shouldGetSDORemovedEmailNotificationParameters() {
        final CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .respondents1(wrapElements(Respondent.builder()
                .party(RespondentParty.builder().lastName("Smith").build())
                .build()))
            .gatekeeperEmails(singletonList(element(EmailAddress.builder().email(GATEKEEPER_EMAIL_ADDRESS).build())))
            .build();

        final OrderRemovalTemplate expectedTemplate = expectedTemplate();

        assertThat(orderRemovalEmailContentProvider.buildNotificationForOrderRemoval(caseData, REMOVAL_REASON))
            .usingRecursiveComparison()
            .isEqualTo(expectedTemplate);
    }

    @Test
    void shouldGetCMORemovedEmailNotificationParameters() {
        final CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
            .respondents1(wrapElements(Respondent.builder()
                .party(RespondentParty.builder().lastName("Smith").build())
                .build()))
            .build();

        final OrderRemovalTemplate expectedTemplate = expectedTemplate();

        assertThat(orderRemovalEmailContentProvider.buildNotificationForOrderRemoval(caseData, REMOVAL_REASON))
            .usingRecursiveComparison()
            .isEqualTo(expectedTemplate);
    }

    private OrderRemovalTemplate expectedTemplate() {
        return OrderRemovalTemplate.builder()
            .respondentLastName("Smith")
            .returnedNote(REMOVAL_REASON)
            .caseReference(String.valueOf(CASE_ID))
            .caseUrl(FAKE_URL)
            .build();
    }

}
