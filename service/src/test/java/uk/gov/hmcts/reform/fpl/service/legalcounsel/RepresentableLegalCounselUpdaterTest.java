package uk.gov.hmcts.reform.fpl.service.legalcounsel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.LegalCounsellor;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.OrganisationService;
import uk.gov.hmcts.reform.fpl.service.UserService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.RespondentsTestHelper.respondentWithSolicitor;

public class RepresentableLegalCounselUpdaterTest {
    private final Organisation organisation1 = mock(Organisation.class);
    private final Organisation organisation2 = mock(Organisation.class);
    private final LegalCounsellor testLegalCounsellor = mock(LegalCounsellor.class);

    private final OrganisationService orgService = mock(OrganisationService.class);
    private final UserService user = mock(UserService.class);

    private final RepresentableLegalCounselUpdater underTest = new RepresentableLegalCounselUpdater(
        orgService, user
    );

    @BeforeEach
    void setUp() {
        when(testLegalCounsellor.getOrganisation()).thenReturn(organisation1);
        when(organisation1.getOrganisationID()).thenReturn("123");
        when(organisation2.getOrganisationID()).thenReturn("124");
    }

    @Test
    void shouldRemoveLegalCounselWhenSolicitorIsRemovedFromRepresentedParty() {
        List<Element<LegalCounsellor>> legalCounsel = wrapElements(testLegalCounsellor);
        Respondent respondent1 = respondentWithSolicitor().toBuilder()
            .legalCounsellors(legalCounsel)
            .build();
        Respondent respondent2 = respondentWithSolicitor().toBuilder()
            .legalCounsellors(legalCounsel)
            .build();

        List<Element<Respondent>> previousRespondents = wrapElements(respondent1, respondent2);
        List<Element<Respondent>> currentRespondents = wrapElements(
            respondent1.toBuilder().solicitor(null).build(),
            respondent2
        );

        List<Element<Respondent>> updated = underTest.updateLegalCounsel(
            previousRespondents, currentRespondents, List.of()
        );

        assertThat(updated).hasSize(2);
        assertThat(updated.get(0).getValue().getLegalCounsellors()).isEmpty();
        assertThat(updated.get(1).getValue().getLegalCounsellors()).isEqualTo(legalCounsel);
    }

    @Test
    void shouldNotRemoveLegalCounselWhenSolicitorOrganisationIdDoesNotChange() {
        List<Element<LegalCounsellor>> legalCounsel = wrapElements(testLegalCounsellor);
        Respondent respondentBefore = Respondent.builder()
            .solicitor(RespondentSolicitor.builder()
                .firstName("Ted")
                .organisation(organisation1)
                .build())
            .legalCounsellors(legalCounsel)
            .build();
        Respondent respondentAfter = Respondent.builder()
            .solicitor(RespondentSolicitor.builder()
                .firstName("John")
                .organisation(organisation1)
                .build())
            .legalCounsellors(legalCounsel)
            .build();

        List<Element<Respondent>> updated = underTest.updateLegalCounsel(
            wrapElements(respondentBefore), wrapElements(respondentAfter), List.of()
        );

        assertThat(updated).hasSize(1);
        assertThat(updated.get(0).getValue().getLegalCounsellors()).isEqualTo(legalCounsel);
    }

    @Test
    void shouldRemoveLegalCounselIfNewSolicitorOrganisationDoesNotHaveLegalCounselInCaseData() {
        List<Element<LegalCounsellor>> legalCounsel = wrapElements(testLegalCounsellor);
        Respondent respondentBefore = Respondent.builder()
            .solicitor(RespondentSolicitor.builder()
                .firstName("Ted")
                .organisation(organisation1)
                .build())
            .legalCounsellors(legalCounsel)
            .build();
        Respondent respondentAfter = Respondent.builder()
            .solicitor(RespondentSolicitor.builder()
                .firstName("Billy")
                .organisation(organisation2)
                .build())
            .legalCounsellors(legalCounsel)
            .build();

        List<Element<Respondent>> updated = underTest.updateLegalCounsel(
            wrapElements(respondentBefore), wrapElements(respondentAfter), List.of()
        );

        assertThat(updated).hasSize(1);
        assertThat(updated.get(0).getValue().getLegalCounsellors()).isEmpty();
    }

    @Test
    void shouldCopyLegalCounselOverIfSameOrganisationAlreadyHasLegalCounselInCaseData() {
        RespondentSolicitor solicitor1 = RespondentSolicitor.builder()
            .firstName("Bob")
            .organisation(organisation1)
            .build();
        List<Element<LegalCounsellor>> legalCounsel1 = wrapElements(testLegalCounsellor);
        Respondent respondent1Before = Respondent.builder().build();//No representation
        Respondent respondent2Before = Respondent.builder()
            .solicitor(solicitor1)
            .legalCounsellors(legalCounsel1)
            .build();
        Respondent respondent1After = Respondent.builder()
            .solicitor(solicitor1)//Now represented by someone who already has a legal counsel
            .build();
        Respondent respondent2After = Respondent.builder()//No change on this respondent
            .solicitor(solicitor1)
            .legalCounsellors(legalCounsel1)
            .build();

        List<Element<Respondent>> updated = underTest.updateLegalCounsel(
            wrapElements(respondent1Before, respondent2Before),
            wrapElements(respondent1After, respondent2After),
            List.of()
        );

        assertThat(updated).hasSize(2);
        assertThat(updated.get(0)
            .getValue()
            .getLegalCounsellors()).isEqualTo(legalCounsel1);//Legal counsel from the other organisation was copied over
        assertThat(updated.get(1).getValue().getLegalCounsellors()).isEqualTo(legalCounsel1);//Legal counsel kept intact
    }

    @Test
    void shouldCopyLegalCounselOverIfNewSolicitorOrganisationAlreadyHasLegalCounselInCaseData() {
        RespondentSolicitor solicitor1 = RespondentSolicitor.builder()
            .firstName("Bob")
            .organisation(organisation1)
            .build();

        RespondentSolicitor orgBSolicitor1 = RespondentSolicitor.builder()
            .firstName("Charles")
            .organisation(organisation2)
            .build();
        RespondentSolicitor orgBSolicitor2 = RespondentSolicitor.builder()
            .firstName("Duncan")
            .organisation(organisation2)
            .build();

        List<Element<LegalCounsellor>> legalCounsel1 = wrapElements(testLegalCounsellor);
        List<Element<LegalCounsellor>> legalCounsel2 = wrapElements(mock(LegalCounsellor.class));

        Respondent respondent1Before = Respondent.builder()
            .solicitor(solicitor1)
            .legalCounsellors(legalCounsel1)
            .build();
        Respondent respondent2Before = Respondent.builder()
            .solicitor(orgBSolicitor1) //Solicitor changed to an existing organisation
            .legalCounsellors(legalCounsel2)
            .build();

        Respondent respondent1After = Respondent.builder()
            .solicitor(orgBSolicitor2)
            .legalCounsellors(legalCounsel1) //Counsel is not changed on screen
            .build();
        Respondent respondent2After = Respondent.builder()
            .solicitor(orgBSolicitor1) //No change on this solicitor
            .legalCounsellors(legalCounsel2)
            .build();

        List<Element<Respondent>> updated = underTest.updateLegalCounsel(
            wrapElements(respondent1Before, respondent2Before),
            wrapElements(respondent1After, respondent2After),
            List.of()
        );

        assertThat(updated).hasSize(2);
        //Legal counsel from the other organisation was copied over
        assertThat(updated.get(0).getValue().getLegalCounsellors()).isEqualTo(legalCounsel2);
        //Legal counsel kept intact
        assertThat(updated.get(1).getValue().getLegalCounsellors()).isEqualTo(legalCounsel2);
    }

    @Test
    void shouldCopyLegalCounselOverIfNewSolicitorOrganisationAlreadyHasLegalCounselInExtraQueryList() {
        RespondentSolicitor solicitor1 = RespondentSolicitor.builder()
            .firstName("Bob")
            .organisation(organisation1)
            .build();
        RespondentSolicitor solicitor2 = RespondentSolicitor.builder()
            .firstName("Charles")
            .organisation(organisation2)
            .build();

        List<Element<LegalCounsellor>> legalCounsel1 = wrapElements(testLegalCounsellor);
        List<Element<LegalCounsellor>> legalCounsel2 = wrapElements(mock(LegalCounsellor.class));

        Child childBefore = Child.builder()
            .solicitor(solicitor1)
            .legalCounsellors(legalCounsel1)
            .build();
        Child childAfter = Child.builder()
            .solicitor(solicitor2)
            .legalCounsellors(legalCounsel1) //Counsel is not changed on screen
            .build();

        Respondent respondentBefore = Respondent.builder()
            .solicitor(solicitor2)
            .legalCounsellors(legalCounsel2)
            .build();

        List<Element<Child>> updated = underTest.updateLegalCounsel(
            wrapElements(childBefore), wrapElements(childAfter), wrapElements(respondentBefore)
        );

        assertThat(updated).hasSize(1);
        //Legal counsel from the other organisation was copied over
        assertThat(updated.get(0).getValue().getLegalCounsellors()).isEqualTo(legalCounsel2);
    }
}
