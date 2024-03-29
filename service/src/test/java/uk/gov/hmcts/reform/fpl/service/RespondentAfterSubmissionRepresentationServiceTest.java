package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.model.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.noc.ChangeOfRepresentation;
import uk.gov.hmcts.reform.fpl.model.representative.ChangeOfRepresentationRequest;
import uk.gov.hmcts.reform.fpl.service.noc.NoticeOfChangeFieldPopulator;
import uk.gov.hmcts.reform.fpl.service.representative.ChangeOfRepresentationService;
import uk.gov.hmcts.reform.fpl.utils.TestDataHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.SolicitorRole.Representing.RESPONDENT;
import static uk.gov.hmcts.reform.fpl.enums.SolicitorRole.SOLICITORA;
import static uk.gov.hmcts.reform.fpl.enums.SolicitorRole.SOLICITORB;
import static uk.gov.hmcts.reform.fpl.model.noc.ChangeOfRepresentationMethod.RESPONDENTS_EVENT;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

class RespondentAfterSubmissionRepresentationServiceTest {

    private static final Map<String, Object> NOC_FIELDS = Map.of("some rubbish", "some rubbish value");
    private static final List<Element<Respondent>> RESPONDENTS_AFTER = List.of(element(mock(Respondent.class)));
    private static final List<Element<Respondent>> RESPONDENTS_BEFORE = List.of(element(mock(Respondent.class)));
    private static final List<Element<ChangeOfRepresentation>> CHANGE_OF_REPRESENTATION_BEFORE = List.of(element(mock(
        ChangeOfRepresentation.class)));
    private static final List<Element<ChangeOfRepresentation>> CHANGE_OF_REPRESENTATION = List.of(element(mock(
        ChangeOfRepresentation.class)));
    private static final List<Element<ChangeOfRepresentation>> ANOTHER_CHANGE_OF_REPRESENTATION = List.of(element(mock(
        ChangeOfRepresentation.class)));
    private static final RespondentSolicitor A_SOLICITOR = mock(RespondentSolicitor.class);
    private static final RespondentSolicitor A_SOLICITOR_2 = mock(RespondentSolicitor.class);
    private static final RespondentSolicitor ANOTHER_SOLICITOR = mock(RespondentSolicitor.class);
    private static final RespondentSolicitor ANOTHER_SOLICITOR_2 = mock(RespondentSolicitor.class);

    private final RespondentService respondentService = mock(RespondentService.class);
    private final NoticeOfChangeFieldPopulator nocFieldPopulator = mock(NoticeOfChangeFieldPopulator.class);
    private final ChangeOfRepresentationService changeOfRepresentationService =
        mock(ChangeOfRepresentationService.class);

    private final RespondentAfterSubmissionRepresentationService underTest =
        new RespondentAfterSubmissionRepresentationService(
            respondentService,
            nocFieldPopulator,
            changeOfRepresentationService
        );

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void testNoChanges() {

        CaseData caseDataAfter = CaseData.builder().respondents1(RESPONDENTS_AFTER).build();
        CaseData caseDataBefore = CaseData.builder().respondents1(RESPONDENTS_BEFORE).build();

        when(nocFieldPopulator.generate(caseDataAfter, RESPONDENT)).thenReturn(NOC_FIELDS);
        when(respondentService.getRepresentationChanges((List) RESPONDENTS_AFTER, (List) RESPONDENTS_BEFORE,
            RESPONDENT)).thenReturn(List.of());

        Map<String, Object> actual = underTest.updateRepresentation(caseDataAfter, caseDataBefore, RESPONDENT, true);

        Map<String, Object> expected = new HashMap<>(NOC_FIELDS);
        expected.put("changeOfRepresentatives", List.of());

        assertThat(actual).isEqualTo(
            expected
        );
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void testDoNotRecordChangeOfRepresentatives() {

        CaseData caseDataAfter = CaseData.builder().respondents1(RESPONDENTS_AFTER).build();
        CaseData caseDataBefore = CaseData.builder().respondents1(RESPONDENTS_BEFORE).build();

        when(nocFieldPopulator.generate(caseDataAfter, RESPONDENT)).thenReturn(NOC_FIELDS);

        Map<String, Object> actual = underTest.updateRepresentation(caseDataAfter, caseDataBefore, RESPONDENT, false);

        Map<String, Object> expected = new HashMap<>(NOC_FIELDS);
        assertThat(actual).isEqualTo(
            expected
        );
        verifyNoInteractions(respondentService);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void testNoChangesMaintainHistory() {

        CaseData caseDataAfter = CaseData.builder()
            .respondents1(RESPONDENTS_AFTER)
            .changeOfRepresentatives(CHANGE_OF_REPRESENTATION_BEFORE)
            .build();
        CaseData caseDataBefore = CaseData.builder()
            .respondents1(RESPONDENTS_BEFORE)
            .build();

        when(nocFieldPopulator.generate(caseDataAfter, RESPONDENT)).thenReturn(NOC_FIELDS);
        when(respondentService.getRepresentationChanges((List) RESPONDENTS_AFTER, (List) RESPONDENTS_BEFORE,
            RESPONDENT)).thenReturn(List.of());

        Map<String, Object> actual = underTest.updateRepresentation(caseDataAfter, caseDataBefore, RESPONDENT, true);

        Map<String, Object> expected = new HashMap<>(NOC_FIELDS);
        expected.put("changeOfRepresentatives", CHANGE_OF_REPRESENTATION_BEFORE);

        assertThat(actual).isEqualTo(
            expected
        );
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void testSingleRespondentChanged() {

        Respondent currentRespondent = Respondent.builder()
            .solicitor(A_SOLICITOR)
            .build();

        List<Element<Respondent>> respondentsAfter = List.of(element(currentRespondent));

        CaseData caseDataAfter = CaseData.builder()
            .respondents1(respondentsAfter)
            .changeOfRepresentatives(CHANGE_OF_REPRESENTATION_BEFORE)
            .build();

        List<Element<Respondent>> respondentsBefore = List.of(element(
            Respondent.builder()
                .solicitor(ANOTHER_SOLICITOR)
                .build()));
        CaseData caseDataBefore = CaseData.builder()
            .respondents1(respondentsBefore)
            .build();

        when(nocFieldPopulator.generate(caseDataAfter, RESPONDENT)).thenReturn(NOC_FIELDS);
        when(respondentService.getRepresentationChanges((List) respondentsAfter, (List) respondentsBefore, RESPONDENT))
            .thenReturn(List.of(
                ChangeOrganisationRequest.builder()
                    .caseRoleId(TestDataHelper.caseRoleDynamicList(SOLICITORA))
                    .build()
            ));
        when(changeOfRepresentationService.changeRepresentative(
            ChangeOfRepresentationRequest.builder()
                .current(CHANGE_OF_REPRESENTATION_BEFORE)
                .by("HMCTS")
                .method(RESPONDENTS_EVENT)
                .respondent(currentRespondent)
                .addedRepresentative(A_SOLICITOR)
                .removedRepresentative(ANOTHER_SOLICITOR)
                .build()
        )).thenReturn(CHANGE_OF_REPRESENTATION);


        Map<String, Object> actual = underTest.updateRepresentation(caseDataAfter, caseDataBefore, RESPONDENT, true);

        Map<String, Object> expected = new HashMap<>(NOC_FIELDS);
        expected.put("changeOfRepresentatives", CHANGE_OF_REPRESENTATION);

        assertThat(actual).isEqualTo(
            expected
        );
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void testSingleRespondentAdded() {

        Respondent currentRespondent = Respondent.builder()
            .solicitor(A_SOLICITOR)
            .build();

        List<Element<Respondent>> respondentsAfter = List.of(element(currentRespondent));

        CaseData caseDataAfter = CaseData.builder()
            .respondents1(respondentsAfter)
            .changeOfRepresentatives(CHANGE_OF_REPRESENTATION_BEFORE)
            .build();

        List<Element<Respondent>> respondentsBefore = List.of();

        CaseData caseDataBefore = CaseData.builder()
            .respondents1(respondentsBefore)
            .build();

        when(nocFieldPopulator.generate(caseDataAfter, RESPONDENT)).thenReturn(NOC_FIELDS);
        when(respondentService.getRepresentationChanges((List) respondentsAfter, (List) respondentsBefore, RESPONDENT))
            .thenReturn(List.of(
                ChangeOrganisationRequest.builder()
                    .caseRoleId(TestDataHelper.caseRoleDynamicList(SOLICITORA))
                    .build()
            ));
        when(changeOfRepresentationService.changeRepresentative(
            ChangeOfRepresentationRequest.builder()
                .current(CHANGE_OF_REPRESENTATION_BEFORE)
                .by("HMCTS")
                .method(RESPONDENTS_EVENT)
                .respondent(currentRespondent)
                .addedRepresentative(A_SOLICITOR)
                .removedRepresentative(null)
                .build()
        )).thenReturn(CHANGE_OF_REPRESENTATION);


        Map<String, Object> actual = underTest.updateRepresentation(caseDataAfter, caseDataBefore,
            RESPONDENT, true);

        Map<String, Object> expected = new HashMap<>(NOC_FIELDS);
        expected.put("changeOfRepresentatives", CHANGE_OF_REPRESENTATION);

        assertThat(actual).isEqualTo(
            expected
        );
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void testMultipleRespondentChanged() {

        Respondent beforeRespondent = Respondent.builder()
            .solicitor(A_SOLICITOR)
            .build();

        Respondent anotherBeforeRespondent = Respondent.builder()
            .solicitor(ANOTHER_SOLICITOR)
            .build();

        Respondent afterRespondent = Respondent.builder()
            .solicitor(A_SOLICITOR_2)
            .build();
        Respondent anotherAfterRespondent = Respondent.builder()
            .solicitor(ANOTHER_SOLICITOR_2)
            .build();

        List<Element<Respondent>> respondentsAfter = List.of(element(afterRespondent), element(anotherAfterRespondent));

        CaseData caseDataAfter = CaseData.builder()
            .respondents1(respondentsAfter)
            .changeOfRepresentatives(CHANGE_OF_REPRESENTATION_BEFORE)
            .build();

        List<Element<Respondent>> respondentsBefore = List.of(element(beforeRespondent),
            element(anotherBeforeRespondent));

        CaseData caseDataBefore = CaseData.builder()
            .respondents1(respondentsBefore)
            .build();

        when(nocFieldPopulator.generate(caseDataAfter, RESPONDENT)).thenReturn(NOC_FIELDS);
        when(respondentService.getRepresentationChanges((List) respondentsAfter, (List) respondentsBefore, RESPONDENT))
            .thenReturn(List.of(
                ChangeOrganisationRequest.builder()
                    .caseRoleId(TestDataHelper.caseRoleDynamicList(SOLICITORA))
                    .build(),
                ChangeOrganisationRequest.builder()
                    .caseRoleId(TestDataHelper.caseRoleDynamicList(SOLICITORB))
                    .build()
            ));
        when(changeOfRepresentationService.changeRepresentative(
            ChangeOfRepresentationRequest.builder()
                .current(CHANGE_OF_REPRESENTATION_BEFORE)
                .by("HMCTS")
                .method(RESPONDENTS_EVENT)
                .respondent(afterRespondent)
                .addedRepresentative(A_SOLICITOR_2)
                .removedRepresentative(A_SOLICITOR)
                .build()
        )).thenReturn(CHANGE_OF_REPRESENTATION);

        when(changeOfRepresentationService.changeRepresentative(
            ChangeOfRepresentationRequest.builder()
                .current(CHANGE_OF_REPRESENTATION)
                .by("HMCTS")
                .method(RESPONDENTS_EVENT)
                .respondent(anotherAfterRespondent)
                .addedRepresentative(ANOTHER_SOLICITOR_2)
                .removedRepresentative(ANOTHER_SOLICITOR)
                .build()
        )).thenReturn(ANOTHER_CHANGE_OF_REPRESENTATION);

        Map<String, Object> actual = underTest.updateRepresentation(caseDataAfter, caseDataBefore, RESPONDENT, true);

        Map<String, Object> expected = new HashMap<>(NOC_FIELDS);
        expected.put("changeOfRepresentatives", ANOTHER_CHANGE_OF_REPRESENTATION);

        assertThat(actual).isEqualTo(
            expected
        );
    }

}
