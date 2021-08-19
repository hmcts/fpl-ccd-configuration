package uk.gov.hmcts.reform.fpl.service.legalcounsel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.model.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.fpl.enums.SolicitorRole.Representing;
import uk.gov.hmcts.reform.fpl.events.legalcounsel.LegalCounsellorRemoved;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.LegalCounsellor;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.UnregisteredOrganisation;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.service.OrganisationService;
import uk.gov.hmcts.reform.fpl.service.UserService;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.RespondentsTestHelper.respondentWithSolicitor;

public class RepresentableLegalCounselUpdaterTest {
    private static final Representing REPRESENTING = Representing.RESPONDENT;
    private static final String ORG_1_ID = "123";
    private static final String ORG_2_ID = "456";
    private static final String ORG_1_NAME = "Org name";
    private static final String ORG_2_NAME = "Org name2";

    private final UnregisteredOrganisation unregisteredOrganisation = mock(UnregisteredOrganisation.class);
    private final uk.gov.hmcts.reform.rd.model.Organisation prdOrg1 = mock(
        uk.gov.hmcts.reform.rd.model.Organisation.class
    );
    private final uk.gov.hmcts.reform.rd.model.Organisation prdOrg2 = mock(
        uk.gov.hmcts.reform.rd.model.Organisation.class
    );
    private final Organisation organisation1 = mock(Organisation.class);
    private final Organisation organisation2 = mock(Organisation.class);
    private final LegalCounsellor legalCounsellor1 = mock(LegalCounsellor.class);
    private final LegalCounsellor legalCounsellor2 = mock(LegalCounsellor.class);

    private final RespondentSolicitor solicitor1 = mock(RespondentSolicitor.class);
    private final RespondentSolicitor solicitor2 = mock(RespondentSolicitor.class);
    private final Respondent respondent1 = mock(Respondent.class);
    private final Respondent respondent2 = mock(Respondent.class);

    private final CaseData caseData = mock(CaseData.class);
    private final CaseData caseDataBefore = mock(CaseData.class);

    private final OrganisationService orgService = mock(OrganisationService.class);
    private final UserService user = mock(UserService.class);

    private final RepresentableLegalCounselUpdater underTest = new RepresentableLegalCounselUpdater(orgService, user);

    @BeforeEach
    void setUp() {
        when(legalCounsellor1.getOrganisation()).thenReturn(organisation1);
        when(organisation1.getOrganisationID()).thenReturn(ORG_1_ID);
        when(organisation2.getOrganisationID()).thenReturn(ORG_2_ID);
    }

    @Test
    void shouldRemoveLegalCounselWhenSolicitorIsRemovedFromRepresentedParty() {
        List<Element<LegalCounsellor>> legalCounsel = wrapElements(legalCounsellor1);
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
        List<Element<LegalCounsellor>> legalCounsel = wrapElements(legalCounsellor1);
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
        List<Element<LegalCounsellor>> legalCounsel = wrapElements(legalCounsellor1);
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
        List<Element<LegalCounsellor>> legalCounsel1 = wrapElements(legalCounsellor1);
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

        List<Element<LegalCounsellor>> legalCounsel1 = wrapElements(legalCounsellor1);
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

        List<Element<LegalCounsellor>> legalCounsel1 = wrapElements(legalCounsellor1);
        List<Element<LegalCounsellor>> legalCounsel2 = wrapElements(legalCounsellor2);

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

    @Test
    void shouldUpdateLegalCounselInChildren() {
        Child childBefore = Child.builder()
            .solicitor(solicitor1)
            .legalCounsellors(wrapElements(legalCounsellor1))
            .build();

        Child childAfter = Child.builder()
            .solicitor(solicitor2)
            .legalCounsellors(wrapElements(legalCounsellor1))
            .build();

        Respondent respondent = Respondent.builder()
            .solicitor(solicitor1)
            .legalCounsellors(wrapElements(legalCounsellor2))
            .build();

        when(caseData.getAllChildren()).thenReturn(wrapElements(childAfter));
        when(caseDataBefore.getAllChildren()).thenReturn(wrapElements(childBefore));
        when(caseData.getAllRespondents()).thenReturn(wrapElements(respondent));

        when(caseData.getChangeOrganisationRequestField()).thenReturn(
            ChangeOrganisationRequest.builder()
                .caseRoleId(DynamicList.builder()
                    .value(DynamicListElement.builder().code("[CHILDSOLICITORA]").build())
                    .build())
            .build()
        );

        when(solicitor1.getOrganisation()).thenReturn(organisation1);
        when(solicitor2.getOrganisation()).thenReturn(organisation2);

        Map<String, Object> updated = underTest.updateLegalCounselFromNoC(caseData, caseDataBefore);

        Child expectedChild = Child.builder().solicitor(solicitor2).legalCounsellors(List.of()).build();
        assertThat(updated).isEqualTo(Map.of(
            "children1", wrapElements(expectedChild)
        ));
    }

    @Test
    void shouldUpdateLegalCounselInRespondents() {
        Child child = Child.builder()
            .solicitor(solicitor1)
            .legalCounsellors(wrapElements(legalCounsellor1))
            .build();

        Respondent respondentBefore = Respondent.builder()
            .solicitor(solicitor1)
            .legalCounsellors(wrapElements(legalCounsellor2))
            .build();

        Respondent respondentAfter = Respondent.builder()
            .solicitor(solicitor2)
            .legalCounsellors(wrapElements(legalCounsellor2))
            .build();

        when(caseData.getAllChildren()).thenReturn(wrapElements(child));
        when(caseData.getAllRespondents()).thenReturn(wrapElements(respondentAfter));
        when(caseDataBefore.getAllRespondents()).thenReturn(wrapElements(respondentBefore));

        when(solicitor1.getOrganisation()).thenReturn(organisation1);
        when(solicitor2.getOrganisation()).thenReturn(organisation2);

        when(caseData.getChangeOrganisationRequestField()).thenReturn(
            ChangeOrganisationRequest.builder()
                .caseRoleId(DynamicList.builder()
                    .value(DynamicListElement.builder().code("[SOLICITORA]").build())
                    .build())
                .build()
        );

        Map<String, Object> updated = underTest.updateLegalCounselFromNoC(caseData, caseDataBefore);

        Respondent expectedRespondent = Respondent.builder().solicitor(solicitor2).legalCounsellors(List.of()).build();
        assertThat(updated).isEqualTo(Map.of(
            "respondents1", wrapElements(expectedRespondent)
        ));
    }

    @Test
    void shouldNotBuildAnyEventsWhenNothingHasChanged() {
        when(caseData.getAllChildren()).thenReturn(List.of());
        when(caseDataBefore.getAllChildren()).thenReturn(List.of());

        Set<LegalCounsellorRemoved> events = underTest.buildEventsForAccessRemoval(
            caseData, caseDataBefore, REPRESENTING
        );

        assertThat(events).isEmpty();
    }

    @Test
    void shouldNotBuildAnyEventsWhenSolicitorHasChangedButNoLegalCounsellorsEverPresent() {
        when(caseData.getAllRespondents()).thenReturn(wrapElements(respondent1));
        when(caseDataBefore.getAllRespondents()).thenReturn(wrapElements(respondent2));

        when(respondent1.getSolicitor()).thenReturn(solicitor1);
        when(respondent2.getSolicitor()).thenReturn(solicitor2);
        when(respondent1.getLegalCounsellors()).thenReturn(null);
        when(respondent2.getLegalCounsellors()).thenReturn(null);

        when(solicitor1.getOrganisation()).thenReturn(organisation1);

        when(orgService.findOrganisation(ORG_1_ID)).thenReturn(Optional.of(prdOrg1));
        when(prdOrg1.getName()).thenReturn(ORG_1_NAME);

        Set<LegalCounsellorRemoved> events = underTest.buildEventsForAccessRemoval(
            caseData, caseDataBefore, REPRESENTING
        );

        assertThat(events).isEmpty();
    }

    @Test
    void shouldBuildEventsWhenSolicitorHasChangedAndLegalCounsellorsRemoved() {
        when(caseData.getAllRespondents()).thenReturn(wrapElements(respondent1));
        when(caseDataBefore.getAllRespondents()).thenReturn(wrapElements(respondent2));

        when(respondent1.getSolicitor()).thenReturn(solicitor1);
        when(respondent2.getSolicitor()).thenReturn(solicitor2);
        when(respondent1.getLegalCounsellors()).thenReturn(List.of());
        when(respondent2.getLegalCounsellors()).thenReturn(wrapElements(legalCounsellor1));

        when(solicitor1.getOrganisation()).thenReturn(organisation1);

        when(orgService.findOrganisation(ORG_1_ID)).thenReturn(Optional.of(prdOrg1));
        when(prdOrg1.getName()).thenReturn(ORG_1_NAME);

        Set<LegalCounsellorRemoved> events = underTest.buildEventsForAccessRemoval(
            caseData, caseDataBefore, REPRESENTING
        );

        assertThat(events).isEqualTo(Set.of(
            new LegalCounsellorRemoved(caseData, ORG_1_NAME, legalCounsellor1)
        ));
    }

    @Test
    void shouldBuildEventsWhenSolicitorHasBeenRemovedByHMCTS() {
        when(caseData.getAllRespondents()).thenReturn(wrapElements(respondent1));
        when(caseDataBefore.getAllRespondents()).thenReturn(wrapElements(respondent2));

        when(respondent1.getSolicitor()).thenReturn(null);
        when(respondent2.getSolicitor()).thenReturn(solicitor2);
        when(respondent1.getLegalCounsellors()).thenReturn(List.of());
        when(respondent2.getLegalCounsellors()).thenReturn(wrapElements(legalCounsellor1));

        when(user.isHmctsUser()).thenReturn(true);

        Set<LegalCounsellorRemoved> events = underTest.buildEventsForAccessRemoval(
            caseData, caseDataBefore, REPRESENTING
        );

        assertThat(events).isEqualTo(Set.of(
            new LegalCounsellorRemoved(caseData, "HMCTS", legalCounsellor1)
        ));
    }

    @Test
    void shouldBuildEventsWhenSolicitorHasBeenRemovedByLA() {
        when(caseData.getAllRespondents()).thenReturn(wrapElements(respondent1));
        when(caseDataBefore.getAllRespondents()).thenReturn(wrapElements(respondent2));

        when(respondent1.getSolicitor()).thenReturn(null);
        when(respondent2.getSolicitor()).thenReturn(solicitor2);
        when(respondent1.getLegalCounsellors()).thenReturn(List.of());
        when(respondent2.getLegalCounsellors()).thenReturn(wrapElements(legalCounsellor1));

        when(user.isHmctsUser()).thenReturn(false);
        when(orgService.findOrganisation()).thenReturn(Optional.of(prdOrg1));
        when(prdOrg1.getName()).thenReturn(ORG_1_NAME);

        Set<LegalCounsellorRemoved> events = underTest.buildEventsForAccessRemoval(
            caseData, caseDataBefore, REPRESENTING
        );

        assertThat(events).isEqualTo(Set.of(
            new LegalCounsellorRemoved(caseData, ORG_1_NAME, legalCounsellor1)
        ));
    }

    @Test
    void shouldBuildEventsWhenSolicitorHasBeenReplacedWithUnregisteredSolicitor() {
        when(caseData.getAllRespondents()).thenReturn(wrapElements(respondent1));
        when(caseDataBefore.getAllRespondents()).thenReturn(wrapElements(respondent2));

        when(respondent1.getSolicitor()).thenReturn(solicitor1);
        when(respondent2.getSolicitor()).thenReturn(solicitor2);
        when(respondent1.getLegalCounsellors()).thenReturn(List.of());
        when(respondent2.getLegalCounsellors()).thenReturn(wrapElements(legalCounsellor1));

        when(solicitor1.getUnregisteredOrganisation()).thenReturn(unregisteredOrganisation);
        when(unregisteredOrganisation.getName()).thenReturn(ORG_1_NAME);

        Set<LegalCounsellorRemoved> events = underTest.buildEventsForAccessRemoval(
            caseData, caseDataBefore, REPRESENTING
        );

        assertThat(events).isEqualTo(Set.of(
            new LegalCounsellorRemoved(caseData, ORG_1_NAME, legalCounsellor1)
        ));
    }

    @Test
    void shouldBuildEventsWhenMultipleSolicitorsHaveBeenRemoved() {
        Respondent respondent3 = mock(Respondent.class);
        Respondent respondent4 = mock(Respondent.class);
        RespondentSolicitor solicitor3 = mock(RespondentSolicitor.class);
        RespondentSolicitor solicitor4 = mock(RespondentSolicitor.class);
        LegalCounsellor legalCounsellor2 = mock(LegalCounsellor.class);

        when(caseData.getAllRespondents()).thenReturn(wrapElements(respondent1, respondent2));
        when(caseDataBefore.getAllRespondents()).thenReturn(wrapElements(respondent3, respondent4));

        when(respondent1.getSolicitor()).thenReturn(solicitor1);
        when(respondent2.getSolicitor()).thenReturn(solicitor2);
        when(respondent3.getSolicitor()).thenReturn(solicitor3);
        when(respondent4.getSolicitor()).thenReturn(solicitor4);
        when(respondent1.getLegalCounsellors()).thenReturn(List.of());
        when(respondent2.getLegalCounsellors()).thenReturn(List.of());
        when(respondent3.getLegalCounsellors()).thenReturn(wrapElements(legalCounsellor1));
        when(respondent4.getLegalCounsellors()).thenReturn(wrapElements(legalCounsellor2));

        when(solicitor1.getOrganisation()).thenReturn(organisation1);
        when(orgService.findOrganisation(ORG_1_ID)).thenReturn(Optional.of(prdOrg1));
        when(prdOrg1.getName()).thenReturn(ORG_1_NAME);
        when(solicitor2.getOrganisation()).thenReturn(organisation2);
        when(orgService.findOrganisation(ORG_2_ID)).thenReturn(Optional.of(prdOrg2));
        when(prdOrg2.getName()).thenReturn(ORG_2_NAME);

        Set<LegalCounsellorRemoved> events = underTest.buildEventsForAccessRemoval(
            caseData, caseDataBefore, REPRESENTING
        );

        assertThat(events).isEqualTo(Set.of(
            new LegalCounsellorRemoved(caseData, ORG_1_NAME, legalCounsellor1),
            new LegalCounsellorRemoved(caseData, ORG_2_NAME, legalCounsellor2)
        ));
    }

    @Test
    void shouldThrowExceptionWhenSolicitorHasBeenRemovedByUnexpectedUser() {
        when(caseData.getAllRespondents()).thenReturn(wrapElements(respondent1));
        when(caseDataBefore.getAllRespondents()).thenReturn(wrapElements(respondent2));

        when(respondent1.getSolicitor()).thenReturn(null);
        when(respondent2.getSolicitor()).thenReturn(solicitor2);

        when(respondent2.getLegalCounsellors()).thenReturn(wrapElements(legalCounsellor1));

        when(user.isHmctsUser()).thenReturn(false);
        when(orgService.findOrganisation()).thenReturn(Optional.empty());

        assertThatThrownBy(() -> underTest.buildEventsForAccessRemoval(caseData, caseDataBefore, REPRESENTING))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Solicitor was changed to null, the user was not HMCTS, the user does not have a organisation"
                        + " associated to them"
            );
    }
}
