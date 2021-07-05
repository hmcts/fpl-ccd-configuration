package uk.gov.hmcts.reform.fpl.service.children;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.components.NoticeOfChangeAnswersConverter;
import uk.gov.hmcts.reform.fpl.components.OptionCountBuilder;
import uk.gov.hmcts.reform.fpl.model.Applicant;
import uk.gov.hmcts.reform.fpl.model.ApplicantParty;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.children.ChildRepresentationDetails;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.event.ChildrenEventData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;


class ChildRepresentationServiceTest {

    private static final String CODED_OPTION_COUNT = "0";
    private static final Map<String, Object> SERIALISED_REP_CHILDREN = Map.of(
        "someKey1", "someValue1",
        "someKey2", "someValue2"
    );
    private static final RespondentSolicitor CHILD_REPRESENTATIVE = mock(RespondentSolicitor.class);
    private static final RespondentSolicitor MAIN_CHILD_REPRESENTATIVE = mock(RespondentSolicitor.class);

    private static final UUID CHILD_UUID_1 = UUID.randomUUID();
    private static final UUID CHILD_UUID_2 = UUID.randomUUID();
    private static final String ORGANISATION_NAME = "Test organisation";
    private static final Applicant FIRST_APPLICANT = Applicant.builder()
        .party(ApplicantParty.builder()
            .organisationName(ORGANISATION_NAME)
            .build())
        .build();

    private final RespondentSolicitor mainRepresentative = mock(RespondentSolicitor.class);

    private final OptionCountBuilder optionCountBuilder = mock(OptionCountBuilder.class);
    private final ChildRepresentationDetailsFlattener flattener = mock(ChildRepresentationDetailsFlattener.class);
    private final NoticeOfChangeAnswersConverter noticeOfChangeAnswersConverter =
        mock(NoticeOfChangeAnswersConverter.class);

    private final ChildRepresentationService underTest = new ChildRepresentationService(
        optionCountBuilder, flattener
    );

    @Nested
    class PopulateRepresentationDetails {

        @Test
        void testWhenChildrenIfHaveRepresentation() {

            List<Element<Child>> children = wrapElements(Child.builder().build());
            when(optionCountBuilder.generateCode(children)).thenReturn(CODED_OPTION_COUNT);
            when(flattener.serialise(children, mainRepresentative)).thenReturn(SERIALISED_REP_CHILDREN);

            Map<String, Object> actual = underTest.populateRepresentationDetails(CaseData.builder()
                .childrenEventData(ChildrenEventData.builder()
                    .childrenMainRepresentative(mainRepresentative)
                    .childrenHaveRepresentation(YES.getValue())
                    .build())
                .children1(children)
                .build());

            Map<String, Object> expected = new HashMap<>();
            expected.put("optionCount", CODED_OPTION_COUNT);
            expected.putAll(SERIALISED_REP_CHILDREN);

            assertThat(actual).isEqualTo(expected);
        }

        @Test
        void testWhenChildrenIfDoesNotHaveRepresentation() {

            when(optionCountBuilder.generateCode(null)).thenReturn(CODED_OPTION_COUNT);
            when(flattener.serialise(null, null)).thenReturn(SERIALISED_REP_CHILDREN);

            Map<String, Object> actual = underTest.populateRepresentationDetails(CaseData.builder()
                .childrenEventData(ChildrenEventData.builder()
                    .childrenHaveRepresentation(NO.getValue())
                    .build())
                .children1(wrapElements(Child.builder().build()))
                .build());

            Map<String, Object> expected = new HashMap<>();
            expected.put("optionCount", null);
            expected.putAll(SERIALISED_REP_CHILDREN);

            assertThat(actual).isEqualTo(expected);
        }
    }

    @Nested
    class FinaliseRepresentationDetails {

        @Test
        void testIfMainSolicitorNotPresent() {
            Map<String, Object> actual = underTest.finaliseRepresentationDetails(CaseData.builder()
                .children1(List.of(element(CHILD_UUID_1, Child.builder()
                    .solicitor(CHILD_REPRESENTATIVE)
                    .build())))
                .childrenEventData(ChildrenEventData.builder()
                    .childrenHaveRepresentation(NO.getValue())
                    .build())
                .build());

            assertThat(actual).isEqualTo(Map.of(
                "children1", List.of(element(CHILD_UUID_1, Child.builder()
                    .solicitor(null)
                    .build()))
            ));
        }

        @Test
        void testIfMainSolicitorIsPresentAndAllChildrenUseMainSolicitor() {
            Map<String, Object> actual = underTest.finaliseRepresentationDetails(CaseData.builder()
                .children1(List.of(element(CHILD_UUID_1, Child.builder()
                    .solicitor(CHILD_REPRESENTATIVE)
                    .build())))
                .childrenEventData(ChildrenEventData.builder()
                    .childrenHaveRepresentation(YES.getValue())
                    .childrenMainRepresentative(MAIN_CHILD_REPRESENTATIVE)
                    .childrenHaveSameRepresentation(YES.getValue())
                    .build())
                .build());

            assertThat(actual).isEqualTo(Map.of(
                "children1", List.of(element(CHILD_UUID_1, Child.builder()
                    .solicitor(MAIN_CHILD_REPRESENTATIVE)
                    .build()))
            ));
        }

        @Test
        void testIfMainSolicitorIsPresentAndChildDoNotUseMainSolicitor() {
            Map<String, Object> actual = underTest.finaliseRepresentationDetails(CaseData.builder()
                .children1(List.of(element(CHILD_UUID_1, Child.builder()
                    .build())))
                .childrenEventData(ChildrenEventData.builder()
                    .childrenHaveRepresentation(YES.getValue())
                    .childrenMainRepresentative(MAIN_CHILD_REPRESENTATIVE)
                    .childrenHaveSameRepresentation(NO.getValue())
                    .childRepresentationDetails0(ChildRepresentationDetails.builder()
                        .useMainSolicitor(NO.getValue())
                        .solicitor(CHILD_REPRESENTATIVE)
                        .build())
                    .build())
                .build());

            assertThat(actual).isEqualTo(Map.of(
                "children1", List.of(element(CHILD_UUID_1, Child.builder()
                    .solicitor(CHILD_REPRESENTATIVE)
                    .build()))
            ));
        }

        @Test
        void testIfMainSolicitorIsPresentAndChildUseMainSolicitor() {
            Map<String, Object> actual = underTest.finaliseRepresentationDetails(CaseData.builder()
                .children1(List.of(element(CHILD_UUID_1, Child.builder()
                    .build())))
                .childrenEventData(ChildrenEventData.builder()
                    .childrenHaveRepresentation(YES.getValue())
                    .childrenMainRepresentative(MAIN_CHILD_REPRESENTATIVE)
                    .childrenHaveSameRepresentation(NO.getValue())
                    .childRepresentationDetails0(ChildRepresentationDetails.builder()
                        .useMainSolicitor(YES.getValue())
                        .build())
                    .build())
                .build());

            assertThat(actual).isEqualTo(Map.of(
                "children1", List.of(element(CHILD_UUID_1, Child.builder()
                    .solicitor(MAIN_CHILD_REPRESENTATIVE)
                    .build()))
            ));
        }

        @Test
        void testIfMainSolicitorIsPresentAndMultipleChildrenUseMixedSolicitors() {
            Map<String, Object> actual = underTest.finaliseRepresentationDetails(CaseData.builder()
                .children1(List.of(
                    element(CHILD_UUID_1, Child.builder().build()),
                    element(CHILD_UUID_2, Child.builder().build())
                ))
                .childrenEventData(ChildrenEventData.builder()
                    .childrenHaveRepresentation(YES.getValue())
                    .childrenMainRepresentative(MAIN_CHILD_REPRESENTATIVE)
                    .childrenHaveSameRepresentation(NO.getValue())
                    .childRepresentationDetails0(ChildRepresentationDetails.builder()
                        .useMainSolicitor(YES.getValue())
                        .build())
                    .childRepresentationDetails1(ChildRepresentationDetails.builder()
                        .useMainSolicitor(NO.getValue())
                        .solicitor(CHILD_REPRESENTATIVE)
                        .build())

                    .build())

                .build());

            assertThat(actual).isEqualTo(Map.of(
                "children1", List.of(
                    element(CHILD_UUID_1, Child.builder()
                        .solicitor(MAIN_CHILD_REPRESENTATIVE)
                        .build()),
                    element(CHILD_UUID_2, Child.builder()
                        .solicitor(CHILD_REPRESENTATIVE)
                        .build())
                )
            ));
        }

    }

    /* Move to The appropriate test

       private static final List<Element<Applicant>> APPLICANTS = List.of(element(FIRST_APPLICANT));
    private static final OrganisationPolicy ORGANISATION_POLICY_A = mock(OrganisationPolicy.class);
    private static final OrganisationPolicy ORGANISATION_POLICY_B = mock(OrganisationPolicy.class);
    private static final OrganisationPolicy ORGANISATION_POLICY_C = mock(OrganisationPolicy.class);
    private static final OrganisationPolicy ORGANISATION_POLICY_D = mock(OrganisationPolicy.class);
    private static final OrganisationPolicy ORGANISATION_POLICY_E = mock(OrganisationPolicy.class);
    private static final OrganisationPolicy ORGANISATION_POLICY_F = mock(OrganisationPolicy.class);
    private static final OrganisationPolicy ORGANISATION_POLICY_G = mock(OrganisationPolicy.class);
    private static final OrganisationPolicy ORGANISATION_POLICY_H = mock(OrganisationPolicy.class);
    private static final OrganisationPolicy ORGANISATION_POLICY_I = mock(OrganisationPolicy.class);
    private static final OrganisationPolicy ORGANISATION_POLICY_J = mock(OrganisationPolicy.class);
    private static final OrganisationPolicy ORGANISATION_POLICY_K = mock(OrganisationPolicy.class);
    private static final OrganisationPolicy ORGANISATION_POLICY_L = mock(OrganisationPolicy.class);
    private static final OrganisationPolicy ORGANISATION_POLICY_M = mock(OrganisationPolicy.class);
    private static final OrganisationPolicy ORGANISATION_POLICY_N = mock(OrganisationPolicy.class);
    private static final OrganisationPolicy ORGANISATION_POLICY_O = mock(OrganisationPolicy.class);

    private static final Element<Child> CHILD_A = element(mock(Child.class));
    private static final Element<Child> CHILD_B = element(mock(Child.class));
    private static final Element<Child> CHILD_C = element(mock(Child.class));
    private static final Element<Child> CHILD_D = element(mock(Child.class));
    private static final Element<Child> CHILD_E = element(mock(Child.class));
    private static final Element<Child> CHILD_F = element(mock(Child.class));
    private static final Element<Child> CHILD_G = element(mock(Child.class));
    private static final Element<Child> CHILD_H = element(mock(Child.class));
    private static final Element<Child> CHILD_I = element(mock(Child.class));
    private static final Element<Child> CHILD_J = element(mock(Child.class));
    private static final Element<Child> CHILD_K = element(mock(Child.class));
    private static final Element<Child> CHILD_L = element(mock(Child.class));
    private static final Element<Child> CHILD_M = element(mock(Child.class));
    private static final Element<Child> CHILD_N = element(mock(Child.class));
    private static final Element<Child> CHILD_O = element(mock(Child.class));

    private static final NoticeOfChangeAnswers NOTICE_OF_CHANGE_ANSWERS_A = mock(NoticeOfChangeAnswers.class);
    private static final NoticeOfChangeAnswers NOTICE_OF_CHANGE_ANSWERS_B = mock(NoticeOfChangeAnswers.class);
    private static final NoticeOfChangeAnswers NOTICE_OF_CHANGE_ANSWERS_C = mock(NoticeOfChangeAnswers.class);
    private static final NoticeOfChangeAnswers NOTICE_OF_CHANGE_ANSWERS_D = mock(NoticeOfChangeAnswers.class);
    private static final NoticeOfChangeAnswers NOTICE_OF_CHANGE_ANSWERS_E = mock(NoticeOfChangeAnswers.class);
    private static final NoticeOfChangeAnswers NOTICE_OF_CHANGE_ANSWERS_F = mock(NoticeOfChangeAnswers.class);
    private static final NoticeOfChangeAnswers NOTICE_OF_CHANGE_ANSWERS_G = mock(NoticeOfChangeAnswers.class);
    private static final NoticeOfChangeAnswers NOTICE_OF_CHANGE_ANSWERS_H = mock(NoticeOfChangeAnswers.class);
    private static final NoticeOfChangeAnswers NOTICE_OF_CHANGE_ANSWERS_I = mock(NoticeOfChangeAnswers.class);
    private static final NoticeOfChangeAnswers NOTICE_OF_CHANGE_ANSWERS_J = mock(NoticeOfChangeAnswers.class);
    private static final NoticeOfChangeAnswers NOTICE_OF_CHANGE_ANSWERS_K = mock(NoticeOfChangeAnswers.class);
    private static final NoticeOfChangeAnswers NOTICE_OF_CHANGE_ANSWERS_L = mock(NoticeOfChangeAnswers.class);
    private static final NoticeOfChangeAnswers NOTICE_OF_CHANGE_ANSWERS_M = mock(NoticeOfChangeAnswers.class);
    private static final NoticeOfChangeAnswers NOTICE_OF_CHANGE_ANSWERS_N = mock(NoticeOfChangeAnswers.class);
    private static final NoticeOfChangeAnswers NOTICE_OF_CHANGE_ANSWERS_O = mock(NoticeOfChangeAnswers.class);

    @Nested
    class GenerateCaseAccessFields {

        @Test
        void testEmpty() {
            Map<Object, Object> expected = new HashMap<>();
            expected.put("childPolicy0", null);
            expected.put("childPolicy1", null);
            expected.put("childPolicy2", null);
            expected.put("childPolicy3", null);
            expected.put("childPolicy4", null);
            expected.put("childPolicy5", null);
            expected.put("childPolicy6", null);
            expected.put("childPolicy7", null);
            expected.put("childPolicy8", null);
            expected.put("childPolicy9", null);
            expected.put("childPolicy10", null);
            expected.put("childPolicy11", null);
            expected.put("childPolicy12", null);
            expected.put("childPolicy13", null);
            expected.put("childPolicy14", null);

            assertThat(underTest.generateCaseAccessFields(CaseData.builder()
                .applicants(APPLICANTS)
                .build())).isEqualTo(expected);
        }

        @Test
        void testSomeChild() {
            when(childSolicitorPolicyConverter.generate(SolicitorRole.CHILDSOLICITORA,
                Optional.of(CHILD_A)))
                .thenReturn(ORGANISATION_POLICY_A);
            when(noticeOfChangeAnswersConverter.generateForSubmission(CHILD_A, FIRST_APPLICANT))
                .thenReturn(NOTICE_OF_CHANGE_ANSWERS_A);




            Map<Object, Object> expected = new HashMap<>();
            expected.put("childPolicy0", ORGANISATION_POLICY_A);
            expected.put("childPolicy1", null);
            expected.put("childPolicy2", null);
            expected.put("childPolicy3", null);
            expected.put("childPolicy4", null);
            expected.put("childPolicy5", null);
            expected.put("childPolicy6", null);
            expected.put("childPolicy7", null);
            expected.put("childPolicy8", null);
            expected.put("childPolicy9", null);
            expected.put("childPolicy10", null);
            expected.put("childPolicy11", null);
            expected.put("childPolicy12", null);
            expected.put("childPolicy13", null);
            expected.put("childPolicy14", null);
            expected.put("noticeOfChangeChildAnswers0", NOTICE_OF_CHANGE_ANSWERS_A);

            assertThat(underTest.generateCaseAccessFields(CaseData.builder()
                .applicants(APPLICANTS)
                .children1(List.of(CHILD_A))
                .build())).isEqualTo(expected);
        }

        @Test
        void testSomeChildren() {
            when(childSolicitorPolicyConverter.generate(SolicitorRole.CHILDSOLICITORA,
                Optional.of(CHILD_A))).thenReturn(ORGANISATION_POLICY_A);
            when(childSolicitorPolicyConverter.generate(SolicitorRole.CHILDSOLICITORB,
                Optional.of(CHILD_B))).thenReturn(ORGANISATION_POLICY_B);
            when(childSolicitorPolicyConverter.generate(SolicitorRole.CHILDSOLICITORC,
                Optional.of(CHILD_C))).thenReturn(ORGANISATION_POLICY_C);
            when(childSolicitorPolicyConverter.generate(SolicitorRole.CHILDSOLICITORD,
                Optional.of(CHILD_D))).thenReturn(ORGANISATION_POLICY_D);
            when(childSolicitorPolicyConverter.generate(SolicitorRole.CHILDSOLICITORE,
                Optional.of(CHILD_E))).thenReturn(ORGANISATION_POLICY_E);
            when(childSolicitorPolicyConverter.generate(SolicitorRole.CHILDSOLICITORF,
                Optional.of(CHILD_F))).thenReturn(ORGANISATION_POLICY_F);
            when(childSolicitorPolicyConverter.generate(SolicitorRole.CHILDSOLICITORG,
                Optional.of(CHILD_G))).thenReturn(ORGANISATION_POLICY_G);
            when(childSolicitorPolicyConverter.generate(SolicitorRole.CHILDSOLICITORH,
                Optional.of(CHILD_H))).thenReturn(ORGANISATION_POLICY_H);
            when(childSolicitorPolicyConverter.generate(SolicitorRole.CHILDSOLICITORI,
                Optional.of(CHILD_I))).thenReturn(ORGANISATION_POLICY_I);
            when(childSolicitorPolicyConverter.generate(SolicitorRole.CHILDSOLICITORJ,
                Optional.of(CHILD_J))).thenReturn(ORGANISATION_POLICY_J);
            when(childSolicitorPolicyConverter.generate(SolicitorRole.CHILDSOLICITORK,
                Optional.of(CHILD_K))).thenReturn(ORGANISATION_POLICY_K);
            when(childSolicitorPolicyConverter.generate(SolicitorRole.CHILDSOLICITORL,
                Optional.of(CHILD_L))).thenReturn(ORGANISATION_POLICY_L);
            when(childSolicitorPolicyConverter.generate(SolicitorRole.CHILDSOLICITORM,
                Optional.of(CHILD_M))).thenReturn(ORGANISATION_POLICY_M);
            when(childSolicitorPolicyConverter.generate(SolicitorRole.CHILDSOLICITORN,
                Optional.of(CHILD_N))).thenReturn(ORGANISATION_POLICY_N);
            when(childSolicitorPolicyConverter.generate(SolicitorRole.CHILDSOLICITORO,
                Optional.of(CHILD_O))).thenReturn(ORGANISATION_POLICY_O);

            when(noticeOfChangeAnswersConverter.generateForSubmission(CHILD_A, FIRST_APPLICANT))
                .thenReturn(NOTICE_OF_CHANGE_ANSWERS_A);
            when(noticeOfChangeAnswersConverter.generateForSubmission(CHILD_B, FIRST_APPLICANT))
                .thenReturn(NOTICE_OF_CHANGE_ANSWERS_B);
            when(noticeOfChangeAnswersConverter.generateForSubmission(CHILD_C, FIRST_APPLICANT))
                .thenReturn(NOTICE_OF_CHANGE_ANSWERS_C);
            when(noticeOfChangeAnswersConverter.generateForSubmission(CHILD_D, FIRST_APPLICANT))
                .thenReturn(NOTICE_OF_CHANGE_ANSWERS_D);
            when(noticeOfChangeAnswersConverter.generateForSubmission(CHILD_E, FIRST_APPLICANT))
                .thenReturn(NOTICE_OF_CHANGE_ANSWERS_E);
            when(noticeOfChangeAnswersConverter.generateForSubmission(CHILD_F, FIRST_APPLICANT))
                .thenReturn(NOTICE_OF_CHANGE_ANSWERS_F);
            when(noticeOfChangeAnswersConverter.generateForSubmission(CHILD_G, FIRST_APPLICANT))
                .thenReturn(NOTICE_OF_CHANGE_ANSWERS_G);
            when(noticeOfChangeAnswersConverter.generateForSubmission(CHILD_H, FIRST_APPLICANT))
                .thenReturn(NOTICE_OF_CHANGE_ANSWERS_H);
            when(noticeOfChangeAnswersConverter.generateForSubmission(CHILD_I, FIRST_APPLICANT))
                .thenReturn(NOTICE_OF_CHANGE_ANSWERS_I);
            when(noticeOfChangeAnswersConverter.generateForSubmission(CHILD_J, FIRST_APPLICANT))
                .thenReturn(NOTICE_OF_CHANGE_ANSWERS_J);
            when(noticeOfChangeAnswersConverter.generateForSubmission(CHILD_K, FIRST_APPLICANT))
                .thenReturn(NOTICE_OF_CHANGE_ANSWERS_K);
            when(noticeOfChangeAnswersConverter.generateForSubmission(CHILD_L, FIRST_APPLICANT))
                .thenReturn(NOTICE_OF_CHANGE_ANSWERS_L);
            when(noticeOfChangeAnswersConverter.generateForSubmission(CHILD_M, FIRST_APPLICANT))
                .thenReturn(NOTICE_OF_CHANGE_ANSWERS_M);
            when(noticeOfChangeAnswersConverter.generateForSubmission(CHILD_N, FIRST_APPLICANT))
                .thenReturn(NOTICE_OF_CHANGE_ANSWERS_N);
            when(noticeOfChangeAnswersConverter.generateForSubmission(CHILD_O, FIRST_APPLICANT))
                .thenReturn(NOTICE_OF_CHANGE_ANSWERS_O);

            Map<Object, Object> expected = new HashMap<>();
            expected.put("childPolicy0", ORGANISATION_POLICY_A);
            expected.put("childPolicy1", ORGANISATION_POLICY_B);
            expected.put("childPolicy2", ORGANISATION_POLICY_C);
            expected.put("childPolicy3", ORGANISATION_POLICY_D);
            expected.put("childPolicy4", ORGANISATION_POLICY_E);
            expected.put("childPolicy5", ORGANISATION_POLICY_F);
            expected.put("childPolicy6", ORGANISATION_POLICY_G);
            expected.put("childPolicy7", ORGANISATION_POLICY_H);
            expected.put("childPolicy8", ORGANISATION_POLICY_I);
            expected.put("childPolicy9", ORGANISATION_POLICY_J);
            expected.put("childPolicy10", ORGANISATION_POLICY_K);
            expected.put("childPolicy11", ORGANISATION_POLICY_L);
            expected.put("childPolicy12", ORGANISATION_POLICY_M);
            expected.put("childPolicy13", ORGANISATION_POLICY_N);
            expected.put("childPolicy14", ORGANISATION_POLICY_O);
            expected.put("noticeOfChangeChildAnswers0", NOTICE_OF_CHANGE_ANSWERS_A);
            expected.put("noticeOfChangeChildAnswers1", NOTICE_OF_CHANGE_ANSWERS_B);
            expected.put("noticeOfChangeChildAnswers2", NOTICE_OF_CHANGE_ANSWERS_C);
            expected.put("noticeOfChangeChildAnswers3", NOTICE_OF_CHANGE_ANSWERS_D);
            expected.put("noticeOfChangeChildAnswers4", NOTICE_OF_CHANGE_ANSWERS_E);
            expected.put("noticeOfChangeChildAnswers5", NOTICE_OF_CHANGE_ANSWERS_F);
            expected.put("noticeOfChangeChildAnswers6", NOTICE_OF_CHANGE_ANSWERS_G);
            expected.put("noticeOfChangeChildAnswers7", NOTICE_OF_CHANGE_ANSWERS_H);
            expected.put("noticeOfChangeChildAnswers8", NOTICE_OF_CHANGE_ANSWERS_I);
            expected.put("noticeOfChangeChildAnswers9", NOTICE_OF_CHANGE_ANSWERS_J);
            expected.put("noticeOfChangeChildAnswers10", NOTICE_OF_CHANGE_ANSWERS_K);
            expected.put("noticeOfChangeChildAnswers11", NOTICE_OF_CHANGE_ANSWERS_L);
            expected.put("noticeOfChangeChildAnswers12", NOTICE_OF_CHANGE_ANSWERS_M);
            expected.put("noticeOfChangeChildAnswers13", NOTICE_OF_CHANGE_ANSWERS_N);
            expected.put("noticeOfChangeChildAnswers14", NOTICE_OF_CHANGE_ANSWERS_O);

            assertThat(underTest.generateCaseAccessFields(CaseData.builder()
                .applicants(APPLICANTS)
                .children1(List.of(
                    CHILD_A,
                    CHILD_B,
                    CHILD_C,
                    CHILD_D,
                    CHILD_E,
                    CHILD_F,
                    CHILD_G,
                    CHILD_H,
                    CHILD_I,
                    CHILD_J,
                    CHILD_K,
                    CHILD_L,
                    CHILD_M,
                    CHILD_N,
                    CHILD_O
                )).build())).isEqualTo(expected);
        }

    }
    */

}
