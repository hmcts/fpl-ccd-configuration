package uk.gov.hmcts.reform.fpl.controllers.applicant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.fpl.controllers.AbstractCallbackTest;
import uk.gov.hmcts.reform.fpl.controllers.ApplicantLocalAuthorityController;
import uk.gov.hmcts.reform.fpl.enums.ColleagueRole;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Colleague;
import uk.gov.hmcts.reform.fpl.model.LocalAuthority;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.event.LocalAuthorityEventData;
import uk.gov.hmcts.reform.fpl.utils.TestDataHelper;

import java.util.List;

import static org.apache.commons.lang3.tuple.Pair.of;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.ccd.model.OrganisationPolicy.organisationPolicy;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.LASOLICITOR;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@WebMvcTest(ApplicantLocalAuthorityController.class)
@OverrideAutoConfiguration(enabled = true)
class ApplicantLocalAuthorityControllerAboutToSubmitTest extends AbstractCallbackTest {

    ApplicantLocalAuthorityControllerAboutToSubmitTest() {
        super("enter-local-authority");
    }

    @BeforeEach
    void setup() {
        givenFplService();
    }

    @Test
    void shouldAddNewLocalAuthority() {

        final List<Element<Colleague>> newColleagues = wrapElements(Colleague.builder()
            .role(ColleagueRole.SOCIAL_WORKER)
            .fullName("Emma Smith")
            .build());

        final LocalAuthority newLocalAuthority = LocalAuthority.builder()
            .name("ORG")
            .email("org@test.com")
            .colleagues(newColleagues)
            .build();

        final CaseData caseData = CaseData.builder()
            .localAuthorityEventData(LocalAuthorityEventData.builder()
                .localAuthority(newLocalAuthority)
                .applicantContactOthers(newColleagues)
                .build())
            .build();

        final CaseData updatedCaseData = extractCaseData(postAboutToSubmitEvent(caseData));

        final LocalAuthority expectedLocalAuthority = LocalAuthority.builder()
            .name("ORG")
            .email("org@test.com")
            .designated("Yes")
            .colleagues(wrapElements(Colleague.builder()
                .role(ColleagueRole.SOCIAL_WORKER)
                .fullName("Emma Smith")
                .mainContact("Yes")
                .build()))
            .build();

        assertThat(updatedCaseData.getLocalAuthorities())
            .extracting(Element::getValue)
            .containsExactly(expectedLocalAuthority);
    }

    @Test
    void shouldUpdateExistingLocalAuthority() {

        final List<Element<Colleague>> existingColleagues = wrapElements(Colleague.builder()
            .role(ColleagueRole.SOCIAL_WORKER)
            .fullName("Emma Smith")
            .mainContact("Yes")
            .build());

        final LocalAuthority existingLocalAuthority = LocalAuthority.builder()
            .id("ORG")
            .name("ORG name")
            .email("org@test.com")
            .colleagues(existingColleagues)
            .build();

        final Element<Colleague> colleague1 = element(Colleague.builder()
            .role(ColleagueRole.SOCIAL_WORKER)
            .fullName("Emma Smith")
            .mainContact("No")
            .build());

        final Element<Colleague> colleague2 = element(Colleague.builder()
            .role(ColleagueRole.SOCIAL_WORKER)
            .fullName("Gregory White")
            .mainContact("Yes")
            .build());

        final DynamicList listOfColleagues = TestDataHelper.buildDynamicList(1,
            of(colleague1.getId(), colleague1.getValue().getFullName()),
            of(colleague2.getId(), colleague2.getValue().getFullName()));

        final List<Element<Colleague>> updatedColleagues = List.of(colleague1, colleague2);

        final LocalAuthority updatedLocalAuthority = LocalAuthority.builder()
            .id("ORG")
            .name("ORG name")
            .email("org@test.com")
            .designated("Yes")
            .colleagues(updatedColleagues)
            .build();

        final CaseData caseData = CaseData.builder()
            .localAuthorityPolicy(organisationPolicy("ORG", "ORG name", LASOLICITOR))
            .localAuthorities(wrapElements(existingLocalAuthority))
            .localAuthorityEventData(LocalAuthorityEventData.builder()
                .localAuthority(updatedLocalAuthority)
                .applicantContactOthers(updatedColleagues)
                .build())
            .build();

        final CaseData updatedCaseData = extractCaseData(postAboutToSubmitEvent(caseData));

        assertThat(updatedCaseData.getLocalAuthorities())
            .extracting(Element::getValue)
            .containsExactly(updatedLocalAuthority);
    }
}
