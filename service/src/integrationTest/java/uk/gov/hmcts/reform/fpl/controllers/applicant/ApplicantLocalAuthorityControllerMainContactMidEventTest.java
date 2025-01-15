package uk.gov.hmcts.reform.fpl.controllers.applicant;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.fpl.controllers.AbstractCallbackTest;
import uk.gov.hmcts.reform.fpl.controllers.ApplicantLocalAuthorityController;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Colleague;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.event.LocalAuthorityEventData;
import uk.gov.hmcts.reform.fpl.utils.TestDataHelper;

import java.util.List;

import static org.apache.commons.lang3.tuple.Pair.of;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@WebMvcTest(ApplicantLocalAuthorityController.class)
@OverrideAutoConfiguration(enabled = true)
class ApplicantLocalAuthorityControllerMainContactMidEventTest extends AbstractCallbackTest {

    ApplicantLocalAuthorityControllerMainContactMidEventTest() {
        super("enter-local-authority");
    }

    @Test
    void shouldMarkSelectedColleagueAsMainContact() {

        final int selectedColleagueIndex = 1;

        final Element<Colleague> colleague1 = element(Colleague.builder()
            .fullName("Colleague 1")
            .build());

        final Element<Colleague> colleague2 = element(Colleague.builder()
            .fullName("Colleague 2")
            .build());

        final Element<Colleague> colleague3 = element(Colleague.builder()
            .fullName("Colleague 3")
            .build());

        final DynamicList listOfColleagues = TestDataHelper.buildDynamicList(selectedColleagueIndex,
            of(colleague1.getId(), colleague1.getValue().getFullName()),
            of(colleague2.getId(), colleague2.getValue().getFullName()),
            of(colleague3.getId(), colleague3.getValue().getFullName()));

        final LocalAuthorityEventData eventData = LocalAuthorityEventData.builder()
            .applicantContactOthers(List.of(colleague1, colleague2, colleague3))
            .localAuthorityColleaguesList(listOfColleagues)
            .build();

        final CaseData caseData = CaseData.builder()
            .localAuthorityEventData(eventData)
            .build();

        final CaseData updatedCaseData = extractCaseData(postMidEvent(caseData, "main-contact"));

        assertThat(updatedCaseData.getLocalAuthorityEventData().getApplicantContactOthers())
            .extracting(Element::getValue)
            .containsExactly(
                Colleague.builder()
                    .fullName("Colleague 1")
                    .mainContact("No")
                    .build(),
                Colleague.builder()
                    .fullName("Colleague 2")
                    .mainContact("Yes")
                    .build(),
                Colleague.builder()
                    .fullName("Colleague 3")
                    .mainContact("No")
                    .build());
    }

    @Test
    void shouldChangeMainContactBasedOnUserSelection() {

        final int selectedColleagueIndex = 1;

        final Element<Colleague> colleague1 = element(Colleague.builder()
            .fullName("Colleague 1")
            .mainContact("Yes")
            .build());

        final Element<Colleague> colleague2 = element(Colleague.builder()
            .fullName("Colleague 2")
            .mainContact("No")
            .build());

        final DynamicList listOfColleagues = TestDataHelper.buildDynamicList(selectedColleagueIndex,
            of(colleague1.getId(), colleague1.getValue().getFullName()),
            of(colleague2.getId(), colleague2.getValue().getFullName()));

        final LocalAuthorityEventData eventData = LocalAuthorityEventData.builder()
            .applicantContactOthers(List.of(colleague1, colleague2))
            .localAuthorityColleaguesList(listOfColleagues)
            .build();

        final CaseData caseData = CaseData.builder()
            .localAuthorityEventData(eventData)
            .build();

        final CaseData updatedCaseData = extractCaseData(postMidEvent(caseData, "main-contact"));

        assertThat(updatedCaseData.getLocalAuthorityEventData().getApplicantContactOthers())
            .extracting(Element::getValue)
            .containsExactly(
                Colleague.builder()
                    .fullName("Colleague 1")
                    .mainContact("No")
                    .build(),
                Colleague.builder()
                    .fullName("Colleague 2")
                    .mainContact("Yes")
                    .build());
    }
}
