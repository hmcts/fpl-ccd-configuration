package uk.gov.hmcts.reform.fpl.controllers.applicant;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.controllers.AbstractCallbackTest;
import uk.gov.hmcts.reform.fpl.controllers.ApplicantLocalAuthorityController;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Colleague;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.event.LocalAuthorityEventData;
import uk.gov.hmcts.reform.fpl.utils.TestDataHelper;

import java.util.List;

import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.tuple.Pair.of;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@WebMvcTest(ApplicantLocalAuthorityController.class)
@OverrideAutoConfiguration(enabled = true)
class ApplicantLocalAuthorityControllerColleaguesMidEventTest extends AbstractCallbackTest {

    ApplicantLocalAuthorityControllerColleaguesMidEventTest() {
        super("enter-local-authority");
    }

    @Test
    void shouldValidateColleaguesEmails() {
        final Colleague colleague1 = Colleague.builder()
            .email("test")
            .build();

        final Colleague colleague2 = Colleague.builder()
            .email("test@test.com")
            .build();

        final Colleague colleague3 = Colleague.builder()
            .email("test@test")
            .build();

        final LocalAuthorityEventData eventData = LocalAuthorityEventData.builder()
            .applicantContactOthers(wrapElements(colleague1, colleague2, colleague3))
            .build();

        final CaseData caseData = CaseData.builder()
            .localAuthorityEventData(eventData)
            .build();

        final AboutToStartOrSubmitCallbackResponse response = postMidEvent(caseData, "colleagues");

        assertThat(response.getErrors()).containsExactly(
            "Colleague 1: Enter an email address in the correct format, for example name@example.com",
            "Colleague 3: Enter an email address in the correct format, for example name@example.com");
    }

    @Test
    void shouldPrepareDynamicListOfColleaguesWhenMultiplePresent() {
        final Element<Colleague> colleague1 = element(Colleague.builder()
            .fullName("Colleague 1")
            .email("test1@test.com")
            .build());

        final Element<Colleague> colleague2 = element(Colleague.builder()
            .fullName("Colleague 2")
            .email("test2@test.com")
            .build());

        final LocalAuthorityEventData eventData = LocalAuthorityEventData.builder()
            .applicantContactOthers(List.of(colleague1, colleague2))
            .build();

        final CaseData caseData = CaseData.builder()
            .localAuthorityEventData(eventData)
            .build();

        final CaseData updatedCaseData = extractCaseData(postMidEvent(caseData, "colleagues"));

        final DynamicList expectedListOfColleagues = TestDataHelper.buildDynamicList(
            of(colleague1.getId(), colleague1.getValue().getFullName()),
            of(colleague2.getId(), colleague2.getValue().getFullName()));

        assertThat(updatedCaseData.getLocalAuthorityEventData().getLocalAuthorityMainContactShown())
            .isEqualTo("Yes");

        assertThat(updatedCaseData.getLocalAuthorityEventData().getLocalAuthorityColleaguesList())
            .isEqualTo(expectedListOfColleagues);
    }

    @Test
    void shouldNotShowListOfColleaguesWhenOnlyOnePresent() {

        final Element<Colleague> colleague1 = element(Colleague.builder()
            .fullName("Colleague 1")
            .email("test1@test.com")
            .build());

        final LocalAuthorityEventData eventData = LocalAuthorityEventData.builder()
            .applicantContactOthers(List.of(colleague1))
            .build();

        final CaseData caseData = CaseData.builder()
            .localAuthorityEventData(eventData)
            .build();

        final CaseData updatedCaseData = extractCaseData(postMidEvent(caseData, "colleagues"));

        assertThat(updatedCaseData.getLocalAuthorityEventData().getLocalAuthorityMainContactShown())
            .isEqualTo("No");
    }

    @Test
    void shouldNotShowListOfColleaguesWhenNonePresent() {

        final LocalAuthorityEventData eventData = LocalAuthorityEventData.builder()
            .applicantContactOthers(emptyList())
            .build();

        final CaseData caseData = CaseData.builder()
            .localAuthorityEventData(eventData)
            .build();

        final CaseData updatedCaseData = extractCaseData(postMidEvent(caseData, "colleagues"));

        assertThat(updatedCaseData.getLocalAuthorityEventData().getLocalAuthorityMainContactShown())
            .isEqualTo("No");

    }
}
