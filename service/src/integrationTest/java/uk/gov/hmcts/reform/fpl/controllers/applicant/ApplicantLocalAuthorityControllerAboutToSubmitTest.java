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
import uk.gov.hmcts.reform.fpl.model.event.LocalAuthorityEventData;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.ccd.model.OrganisationPolicy.organisationPolicy;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.LASOLICITOR;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
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
        final Colleague newColleague = Colleague.builder()
            .role(ColleagueRole.SOCIAL_WORKER)
            .fullName("Emma Smith")
            .build();

        final LocalAuthority newLocalAuthority = LocalAuthority.builder()
            .name("ORG")
            .email("org@test.com")
            .build();

        final CaseData caseData = CaseData.builder()
            .localAuthorityEventData(LocalAuthorityEventData.builder()
                .localAuthority(newLocalAuthority)
                .applicantContact(newColleague)
                .build())
            .build();

        final CaseData updatedCaseData = extractCaseData(postAboutToSubmitEvent(caseData));

        final Colleague expectedMainContact = Colleague.builder()
            .role(ColleagueRole.SOCIAL_WORKER)
            .fullName("Emma Smith")
            .mainContact(YES.getValue())
            .notificationRecipient(YES.getValue())
            .build();

        final LocalAuthority expectedLocalAuthority = LocalAuthority.builder()
            .name("ORG")
            .email("org@test.com")
            .designated("Yes")
            .build();

        assertThat(updatedCaseData.getLocalAuthorities().size()).isEqualTo(1);
        final LocalAuthority actualLocalAuthority = updatedCaseData.getLocalAuthorities().get(0).getValue();
        assertThat(actualLocalAuthority.getName()).isEqualTo(expectedLocalAuthority.getName());
        assertThat(actualLocalAuthority.getEmail()).isEqualTo(expectedLocalAuthority.getEmail());
        assertThat(actualLocalAuthority.getDesignated()).isEqualTo(expectedLocalAuthority.getDesignated());

        assertThat(actualLocalAuthority.getColleagues().size()).isEqualTo(1);
        final Element<Colleague> actualColleague = actualLocalAuthority.getColleagues().get(0);
        assertThat(actualColleague.getId()).isNotNull();
        assertThat(actualColleague.getValue()).isEqualTo(expectedMainContact);
    }

    @Test
    void shouldUpdateExistingLocalAuthority() {
        final UUID existingMainContactUUID = UUID.randomUUID();

        final List<Element<Colleague>> existingColleagues = List.of(
            element(existingMainContactUUID, Colleague.builder()
                .role(ColleagueRole.SOCIAL_WORKER)
                .fullName("Emma Smith")
                .mainContact(YES.getValue())
                .build()));

        final LocalAuthority existingLocalAuthority = LocalAuthority.builder()
            .id("ORG")
            .name("ORG name")
            .email("org@test.com")
            .colleagues(existingColleagues)
            .build();

        final Colleague updatedContact = Colleague.builder()
            .role(ColleagueRole.SOCIAL_WORKER)
            .fullName("Gregory White")
            .mainContact(YES.getValue())
            .notificationRecipient(YES.getValue())
            .build();

        final CaseData caseData = CaseData.builder()
            .localAuthorityPolicy(organisationPolicy("ORG", "ORG name", LASOLICITOR))
            .localAuthorities(wrapElements(existingLocalAuthority))
            .localAuthorityEventData(LocalAuthorityEventData.builder()
                .localAuthority(existingLocalAuthority)
                .applicantContact(updatedContact)
                .applicantContactOthers(List.of())
                .build())
            .build();

        final CaseData updatedCaseData = extractCaseData(postAboutToSubmitEvent(caseData));

        final LocalAuthority expectedLocalAuthority = LocalAuthority.builder()
            .id("ORG")
            .name("ORG name")
            .email("org@test.com")
            .designated(YES.getValue())
            .colleagues(List.of(element(existingMainContactUUID, updatedContact)))
            .build();

        assertThat(updatedCaseData.getLocalAuthorities())
            .extracting(Element::getValue)
            .containsExactly(expectedLocalAuthority);
    }

    @Test
    void shouldAddNewOtherContactToLocalAuthority() {
        final UUID existingMainContactUUID = UUID.randomUUID();
        final Colleague existingMainContact = Colleague.builder()
            .role(ColleagueRole.SOCIAL_WORKER)
            .fullName("Emma Smith")
            .mainContact(YES.getValue())
            .notificationRecipient(YES.getValue())
            .build();
        final Element<Colleague> existingMainContactElement = element(existingMainContactUUID, existingMainContact);

        final LocalAuthority existingLocalAuthority = LocalAuthority.builder()
            .id("ORG")
            .name("ORG name")
            .email("org@test.com")
            .colleagues(List.of(existingMainContactElement))
            .build();

        final Element<Colleague> newOtherContact = element(Colleague.builder()
            .role(ColleagueRole.SOCIAL_WORKER)
            .fullName("Gregory White")
            .mainContact(NO.getValue())
            .notificationRecipient(YES.getValue())
            .build());

        final CaseData caseData = CaseData.builder()
            .localAuthorityPolicy(organisationPolicy("ORG", "ORG name", LASOLICITOR))
            .localAuthorities(wrapElements(existingLocalAuthority))
            .localAuthorityEventData(LocalAuthorityEventData.builder()
                .localAuthority(existingLocalAuthority)
                .applicantContact(existingMainContact)
                .applicantContactOthers(List.of(newOtherContact))
                .build())
            .build();

        final CaseData updatedCaseData = extractCaseData(postAboutToSubmitEvent(caseData));

        final LocalAuthority expectedLocalAuthority = LocalAuthority.builder()
            .id("ORG")
            .name("ORG name")
            .email("org@test.com")
            .designated(YES.getValue())
            .colleagues(List.of(existingMainContactElement, newOtherContact))
            .build();

        assertThat(updatedCaseData.getLocalAuthorities())
            .extracting(Element::getValue)
            .containsExactly(expectedLocalAuthority);
    }
}
