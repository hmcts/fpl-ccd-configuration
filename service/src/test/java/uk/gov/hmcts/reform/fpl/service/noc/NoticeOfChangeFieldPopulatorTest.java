package uk.gov.hmcts.reform.fpl.service.noc;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.fpl.components.NoticeOfChangeAnswersConverter;
import uk.gov.hmcts.reform.fpl.components.RespondentPolicyConverter;
import uk.gov.hmcts.reform.fpl.model.Applicant;
import uk.gov.hmcts.reform.fpl.model.ApplicantParty;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.LocalAuthority;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.interfaces.WithSolicitor;
import uk.gov.hmcts.reform.fpl.model.noticeofchange.NoticeOfChangeAnswers;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.SolicitorRole.CHILDSOLICITORA;
import static uk.gov.hmcts.reform.fpl.enums.SolicitorRole.CHILDSOLICITORB;
import static uk.gov.hmcts.reform.fpl.enums.SolicitorRole.CHILDSOLICITORC;
import static uk.gov.hmcts.reform.fpl.enums.SolicitorRole.CHILDSOLICITORD;
import static uk.gov.hmcts.reform.fpl.enums.SolicitorRole.CHILDSOLICITORE;
import static uk.gov.hmcts.reform.fpl.enums.SolicitorRole.CHILDSOLICITORF;
import static uk.gov.hmcts.reform.fpl.enums.SolicitorRole.CHILDSOLICITORG;
import static uk.gov.hmcts.reform.fpl.enums.SolicitorRole.CHILDSOLICITORH;
import static uk.gov.hmcts.reform.fpl.enums.SolicitorRole.CHILDSOLICITORI;
import static uk.gov.hmcts.reform.fpl.enums.SolicitorRole.CHILDSOLICITORJ;
import static uk.gov.hmcts.reform.fpl.enums.SolicitorRole.CHILDSOLICITORK;
import static uk.gov.hmcts.reform.fpl.enums.SolicitorRole.CHILDSOLICITORL;
import static uk.gov.hmcts.reform.fpl.enums.SolicitorRole.CHILDSOLICITORM;
import static uk.gov.hmcts.reform.fpl.enums.SolicitorRole.CHILDSOLICITORN;
import static uk.gov.hmcts.reform.fpl.enums.SolicitorRole.CHILDSOLICITORO;
import static uk.gov.hmcts.reform.fpl.enums.SolicitorRole.Representing;
import static uk.gov.hmcts.reform.fpl.enums.SolicitorRole.SOLICITORA;
import static uk.gov.hmcts.reform.fpl.enums.SolicitorRole.SOLICITORB;
import static uk.gov.hmcts.reform.fpl.enums.SolicitorRole.SOLICITORC;
import static uk.gov.hmcts.reform.fpl.enums.SolicitorRole.SOLICITORD;
import static uk.gov.hmcts.reform.fpl.enums.SolicitorRole.SOLICITORE;
import static uk.gov.hmcts.reform.fpl.enums.SolicitorRole.SOLICITORF;
import static uk.gov.hmcts.reform.fpl.enums.SolicitorRole.SOLICITORG;
import static uk.gov.hmcts.reform.fpl.enums.SolicitorRole.SOLICITORH;
import static uk.gov.hmcts.reform.fpl.enums.SolicitorRole.SOLICITORI;
import static uk.gov.hmcts.reform.fpl.enums.SolicitorRole.SOLICITORJ;
import static uk.gov.hmcts.reform.fpl.service.noc.NoticeOfChangeFieldPopulator.NoticeOfChangeAnswersPopulationStrategy.BLANK;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

class NoticeOfChangeFieldPopulatorTest {
    private static final UUID ELEMENT_1_ID = UUID.randomUUID();
    private static final UUID ELEMENT_2_ID = UUID.randomUUID();

    private static final Respondent RESPONDENT_1 = mock(Respondent.class);
    private static final Respondent RESPONDENT_2 = mock(Respondent.class);
    private static final Element<Respondent> RESPONDENT_1_ELEMENT = element(ELEMENT_1_ID, RESPONDENT_1);
    private static final Element<Respondent> RESPONDENT_2_ELEMENT = element(ELEMENT_2_ID, RESPONDENT_2);
    private static final Element<WithSolicitor> RESPONDENT_1_ELEMENT_INT = element(ELEMENT_1_ID, RESPONDENT_1);
    private static final Element<WithSolicitor> RESPONDENT_2_ELEMENT_INT = element(ELEMENT_2_ID, RESPONDENT_2);

    private static final Child CHILD_1 = mock(Child.class);
    private static final Child CHILD_2 = mock(Child.class);
    private static final Element<Child> CHILD_1_ELEMENT = element(ELEMENT_1_ID, CHILD_1);
    private static final Element<Child> CHILD_2_ELEMENT = element(ELEMENT_2_ID, CHILD_2);
    private static final Element<WithSolicitor> CHILD_1_ELEMENT_INT = element(ELEMENT_1_ID, CHILD_1);
    private static final Element<WithSolicitor> CHILD_2_ELEMENT_INT = element(ELEMENT_2_ID, CHILD_2);

    private static final OrganisationPolicy ORG_POLICY_A = mock(OrganisationPolicy.class);
    private static final OrganisationPolicy ORG_POLICY_B = mock(OrganisationPolicy.class);
    private static final OrganisationPolicy ORG_POLICY_C = mock(OrganisationPolicy.class);
    private static final OrganisationPolicy ORG_POLICY_D = mock(OrganisationPolicy.class);
    private static final OrganisationPolicy ORG_POLICY_E = mock(OrganisationPolicy.class);
    private static final OrganisationPolicy ORG_POLICY_F = mock(OrganisationPolicy.class);
    private static final OrganisationPolicy ORG_POLICY_G = mock(OrganisationPolicy.class);
    private static final OrganisationPolicy ORG_POLICY_H = mock(OrganisationPolicy.class);
    private static final OrganisationPolicy ORG_POLICY_I = mock(OrganisationPolicy.class);
    private static final OrganisationPolicy ORG_POLICY_J = mock(OrganisationPolicy.class);
    private static final OrganisationPolicy ORG_POLICY_K = mock(OrganisationPolicy.class);
    private static final OrganisationPolicy ORG_POLICY_L = mock(OrganisationPolicy.class);
    private static final OrganisationPolicy ORG_POLICY_M = mock(OrganisationPolicy.class);
    private static final OrganisationPolicy ORG_POLICY_N = mock(OrganisationPolicy.class);
    private static final OrganisationPolicy ORG_POLICY_O = mock(OrganisationPolicy.class);

    private static final NoticeOfChangeAnswers ANSWERS_1 = mock(NoticeOfChangeAnswers.class);
    private static final NoticeOfChangeAnswers ANSWERS_2 = mock(NoticeOfChangeAnswers.class);
    private static final NoticeOfChangeAnswers BLANK_ANSWERS = NoticeOfChangeAnswers.builder().build();
    private static final NoticeOfChangeAnswers THIRD_PARTY_APPLICANT_ANSWERS = NoticeOfChangeAnswers.builder()
        .respondentFirstName("Bilbo")
        .respondentLastName("Baggins")
        .build();

    private static final Applicant APPLICANT = mock(Applicant.class);
    private static final String APPLICANT_NAME = "applicant";
    public static final ApplicantParty APPLICANT_PARTY = mock(ApplicantParty.class);
    public static final LocalAuthority LOCAL_AUTHORITY = mock(LocalAuthority.class);

    private final CaseData caseData = mock(CaseData.class);

    private final NoticeOfChangeAnswersConverter answersConverter = mock(NoticeOfChangeAnswersConverter.class);
    private final RespondentPolicyConverter policyConverter = mock(RespondentPolicyConverter.class);

    private final NoticeOfChangeFieldPopulator underTest = new NoticeOfChangeFieldPopulator(
        answersConverter, policyConverter
    );

    @Test
    void generateRespondents() {
        when(caseData.getAllRespondents()).thenReturn(List.of(RESPONDENT_1_ELEMENT, RESPONDENT_2_ELEMENT));

        when(caseData.getLocalAuthorities()).thenReturn(null);
        when(caseData.getAllApplicants()).thenReturn(wrapElements(APPLICANT));
        when(APPLICANT.getParty()).thenReturn(APPLICANT_PARTY);
        when(APPLICANT_PARTY.getOrganisationName()).thenReturn(APPLICANT_NAME);

        when(policyConverter.generate(SOLICITORA, Optional.of(RESPONDENT_1_ELEMENT_INT)))
            .thenReturn(ORG_POLICY_A);
        when(policyConverter.generate(SOLICITORB, Optional.of(RESPONDENT_2_ELEMENT_INT)))
            .thenReturn(ORG_POLICY_B);

        when(policyConverter.generate(SOLICITORC, Optional.empty())).thenReturn(ORG_POLICY_C);
        when(policyConverter.generate(SOLICITORD, Optional.empty())).thenReturn(ORG_POLICY_D);
        when(policyConverter.generate(SOLICITORE, Optional.empty())).thenReturn(ORG_POLICY_E);
        when(policyConverter.generate(SOLICITORF, Optional.empty())).thenReturn(ORG_POLICY_F);
        when(policyConverter.generate(SOLICITORG, Optional.empty())).thenReturn(ORG_POLICY_G);
        when(policyConverter.generate(SOLICITORH, Optional.empty())).thenReturn(ORG_POLICY_H);
        when(policyConverter.generate(SOLICITORI, Optional.empty())).thenReturn(ORG_POLICY_I);
        when(policyConverter.generate(SOLICITORJ, Optional.empty())).thenReturn(ORG_POLICY_J);

        when(answersConverter.generateForSubmission(RESPONDENT_1_ELEMENT_INT)).thenReturn(ANSWERS_1);
        when(answersConverter.generateForSubmission(RESPONDENT_2_ELEMENT_INT)).thenReturn(ANSWERS_2);

        final Map<String, Object> data = underTest.generate(caseData, Representing.RESPONDENT);

        assertThat(data).isEqualTo(Map.ofEntries(
            entry("noticeOfChangeAnswers0", ANSWERS_1),
            entry("noticeOfChangeAnswers1", ANSWERS_2),
            entry("respondentPolicy0", ORG_POLICY_A),
            entry("respondentPolicy1", ORG_POLICY_B),
            entry("respondentPolicy2", ORG_POLICY_C),
            entry("respondentPolicy3", ORG_POLICY_D),
            entry("respondentPolicy4", ORG_POLICY_E),
            entry("respondentPolicy5", ORG_POLICY_F),
            entry("respondentPolicy6", ORG_POLICY_G),
            entry("respondentPolicy7", ORG_POLICY_H),
            entry("respondentPolicy8", ORG_POLICY_I),
            entry("respondentPolicy9", ORG_POLICY_J)
        ));

    }

    @Test
    void generateChildren() {
        when(caseData.getAllChildren()).thenReturn(List.of(CHILD_1_ELEMENT, CHILD_2_ELEMENT));

        when(caseData.getLocalAuthorities()).thenReturn(wrapElements(LOCAL_AUTHORITY));
        when(LOCAL_AUTHORITY.getName()).thenReturn(APPLICANT_NAME);

        when(policyConverter.generate(CHILDSOLICITORA, Optional.of(CHILD_1_ELEMENT_INT))).thenReturn(ORG_POLICY_A);
        when(policyConverter.generate(CHILDSOLICITORB, Optional.of(CHILD_2_ELEMENT_INT))).thenReturn(ORG_POLICY_B);
        when(policyConverter.generate(CHILDSOLICITORC, Optional.empty())).thenReturn(ORG_POLICY_C);
        when(policyConverter.generate(CHILDSOLICITORD, Optional.empty())).thenReturn(ORG_POLICY_D);
        when(policyConverter.generate(CHILDSOLICITORE, Optional.empty())).thenReturn(ORG_POLICY_E);
        when(policyConverter.generate(CHILDSOLICITORF, Optional.empty())).thenReturn(ORG_POLICY_F);
        when(policyConverter.generate(CHILDSOLICITORG, Optional.empty())).thenReturn(ORG_POLICY_G);
        when(policyConverter.generate(CHILDSOLICITORH, Optional.empty())).thenReturn(ORG_POLICY_H);
        when(policyConverter.generate(CHILDSOLICITORI, Optional.empty())).thenReturn(ORG_POLICY_I);
        when(policyConverter.generate(CHILDSOLICITORJ, Optional.empty())).thenReturn(ORG_POLICY_J);
        when(policyConverter.generate(CHILDSOLICITORK, Optional.empty())).thenReturn(ORG_POLICY_K);
        when(policyConverter.generate(CHILDSOLICITORL, Optional.empty())).thenReturn(ORG_POLICY_L);
        when(policyConverter.generate(CHILDSOLICITORM, Optional.empty())).thenReturn(ORG_POLICY_M);
        when(policyConverter.generate(CHILDSOLICITORN, Optional.empty())).thenReturn(ORG_POLICY_N);
        when(policyConverter.generate(CHILDSOLICITORO, Optional.empty())).thenReturn(ORG_POLICY_O);

        final Map<String, Object> data = underTest.generate(caseData, Representing.CHILD, BLANK);

        assertThat(data).isEqualTo(Map.ofEntries(
            entry("noticeOfChangeChildAnswers0", BLANK_ANSWERS),
            entry("noticeOfChangeChildAnswers1", BLANK_ANSWERS),
            entry("noticeOfChangeChildAnswers2", BLANK_ANSWERS),
            entry("noticeOfChangeChildAnswers3", BLANK_ANSWERS),
            entry("noticeOfChangeChildAnswers4", BLANK_ANSWERS),
            entry("noticeOfChangeChildAnswers5", BLANK_ANSWERS),
            entry("noticeOfChangeChildAnswers6", BLANK_ANSWERS),
            entry("noticeOfChangeChildAnswers7", BLANK_ANSWERS),
            entry("noticeOfChangeChildAnswers8", BLANK_ANSWERS),
            entry("noticeOfChangeChildAnswers9", BLANK_ANSWERS),
            entry("noticeOfChangeChildAnswers10", BLANK_ANSWERS),
            entry("noticeOfChangeChildAnswers11", BLANK_ANSWERS),
            entry("noticeOfChangeChildAnswers12", BLANK_ANSWERS),
            entry("noticeOfChangeChildAnswers13", BLANK_ANSWERS),
            entry("noticeOfChangeChildAnswers14", BLANK_ANSWERS),
            entry("childPolicy0", ORG_POLICY_A),
            entry("childPolicy1", ORG_POLICY_B),
            entry("childPolicy2", ORG_POLICY_C),
            entry("childPolicy3", ORG_POLICY_D),
            entry("childPolicy4", ORG_POLICY_E),
            entry("childPolicy5", ORG_POLICY_F),
            entry("childPolicy6", ORG_POLICY_G),
            entry("childPolicy7", ORG_POLICY_H),
            entry("childPolicy8", ORG_POLICY_I),
            entry("childPolicy9", ORG_POLICY_J),
            entry("childPolicy10", ORG_POLICY_K),
            entry("childPolicy11", ORG_POLICY_L),
            entry("childPolicy12", ORG_POLICY_M),
            entry("childPolicy13", ORG_POLICY_N),
            entry("childPolicy14", ORG_POLICY_O)
        ));
    }

    @Test
    void generateApplicantAnswer() {
        Map<String, Object> nocAnswers = underTest.generateApplicantAnswer(caseData);

        assertThat(nocAnswers.get("noticeOfChangeAnswersThirdPartyRespondent")).isEqualTo(
            THIRD_PARTY_APPLICANT_ANSWERS);
    }
}
