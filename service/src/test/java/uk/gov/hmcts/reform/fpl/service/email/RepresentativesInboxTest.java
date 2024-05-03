package uk.gov.hmcts.reform.fpl.service.email;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.fpl.enums.CaseRole;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeRole;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.Colleague;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static com.google.common.collect.Sets.newHashSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeRole.Type.CAFCASS;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeRole.Type.LASOLICITOR;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeRole.Type.RESPONDENT;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.EMAIL;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.POST;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

class RepresentativesInboxTest {

    private static final String ORGANISATION_ID = "ORGANISATION_ID";
    private static final String EMAIL_1 = "email1";
    private static final String EMAIL_2 = "email2";
    private static final String EMAIL_3 = "email3";
    private static final String EMAIL_4 = "email4";
    private static final String EMAIL_5 = "email5";
    private static final String EMAIL_6 = "email6";
    private static final String EMAIL_7 = "email7";
    private static final String EMAIL_8 = "email8";
    private static final String EMAIL_9 = "email9";
    private static final String EMAIL_10 = "email10";
    private static final Respondent UNREGISTERED_RESPONDENT = Respondent.builder()
        .solicitor(RespondentSolicitor.builder()
            .email(EMAIL_1)
            .organisation(Organisation.builder()
                .organisationID(null)
                .build())
            .build())
        .build();
    private static final Respondent REGISTERED_RESPONDENT = Respondent.builder()
        .solicitor(RespondentSolicitor.builder()
            .email(EMAIL_2)
            .organisation(Organisation.builder()
                .organisationID(ORGANISATION_ID)
                .build())
            .build())
        .build();
    private static final Element<Colleague> SOLICITOR_COLLEAGUE = element(Colleague.builder()
        .fullName("Jack Frost")
        .email(EMAIL_8)
        .build());
    private static final Element<Colleague> SOLICITOR_2_COLLEAGUE = element(Colleague.builder()
        .fullName("Billy Frost")
        .email(EMAIL_10)
        .build());
    private static final Respondent REGISTERED_RESPONDENT_WITH_COLLEAGUES = Respondent.builder()
        .solicitor(RespondentSolicitor.builder()
            .email(EMAIL_2)
            .organisation(Organisation.builder()
                .organisationID(ORGANISATION_ID)
                .build())
            .colleaguesToBeNotified(List.of(SOLICITOR_COLLEAGUE))
            .build())
        .build();
    private static final Representative EMAIL_REP = Representative.builder()
        .email(EMAIL_3)
        .servingPreferences(EMAIL)
        .build();
    private static final Representative EMAIL_REP2 = Representative.builder()
        .email(EMAIL_7)
        .servingPreferences(EMAIL)
        .build();
    private static final Representative DIGITAL_REP = Representative.builder()
        .email(EMAIL_4)
        .servingPreferences(DIGITAL_SERVICE)
        .build();
    private static final Child UNREGISTERED_CHILD = Child.builder()
        .solicitor(RespondentSolicitor.builder()
            .email(EMAIL_5)
            .organisation(Organisation.builder()
                .organisationID(null)
                .build())
            .build())
        .build();
    private static final Child REGISTERED_CHILD = Child.builder()
        .solicitor(RespondentSolicitor.builder()
            .email(EMAIL_6)
            .organisation(Organisation.builder()
                .organisationID(ORGANISATION_ID)
                .build())
            .build())
        .build();
    private static final Child REGISTERED_CHILD_WITH_COLLEAGUES = Child.builder()
        .solicitor(RespondentSolicitor.builder()
            .email(EMAIL_6)
            .organisation(Organisation.builder()
                .organisationID(ORGANISATION_ID)
                .build())
            .colleaguesToBeNotified(List.of(SOLICITOR_COLLEAGUE))
            .build())
        .build();
    private static final Child REGISTERED_CHILD_2_WITH_COLLEAGUES = Child.builder()
        .solicitor(RespondentSolicitor.builder()
            .email(EMAIL_9)
            .organisation(Organisation.builder()
                .organisationID(ORGANISATION_ID)
                .build())
            .colleaguesToBeNotified(List.of(SOLICITOR_2_COLLEAGUE))
            .build())
        .build();

    private static final Representative CARCASS_REP = Representative.builder()
        .email(EMAIL_1)
        .servingPreferences(DIGITAL_SERVICE)
        .role(RepresentativeRole.CAFCASS_GUARDIAN)
        .build();

    private static final Representative RESPONDENT_REP = Representative.builder()
        .email(EMAIL_2)
        .servingPreferences(DIGITAL_SERVICE)
        .role(RepresentativeRole.REPRESENTING_RESPONDENT_1)
        .build();

    private final RepresentativesInbox underTest = new RepresentativesInbox();

    @Test
    void testRepresentativesByPOSTIsNotAccepted() {
        CaseData caseData = CaseData.builder().build();

        assertThatThrownBy(() -> underTest.getEmailsByPreference(caseData, POST))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Preference should not be POST");
    }

    @Test
    void testNullRepresentatives() {
        CaseData caseData = CaseData.builder().representatives(null).build();

        Set<String> actual = underTest.getEmailsByPreference(caseData, EMAIL);

        assertThat(actual).isEqualTo(Set.of());
    }

    @Test
    void testNoRepresentatives() {
        CaseData caseData = CaseData.builder().representatives(wrapElements()).build();

        Set<String> actual = underTest.getEmailsByPreference(caseData, EMAIL);

        assertThat(actual).isEqualTo(Set.of());
    }

    @Test
    void testFilterOnlyEmailRepresentatives() {
        CaseData caseData = CaseData.builder().representatives(wrapElements(EMAIL_REP, DIGITAL_REP)).build();

        Set<String> actual = underTest.getEmailsByPreference(caseData, EMAIL);

        assertThat(actual).isEqualTo(Set.of(EMAIL_3));
    }

    @Test
    void testFilterOnlyDigitalRepresentatives() {
        CaseData caseData = CaseData.builder().representatives(wrapElements(EMAIL_REP, DIGITAL_REP)).build();

        Set<String> actual = underTest.getEmailsByPreference(caseData, DIGITAL_SERVICE);

        assertThat(actual).isEqualTo(Set.of(EMAIL_4));
    }

    @Test
    void testFilterOnlyEmailRepresentativesFromRegisteredRespondentSolicitors() {
        CaseData caseData = CaseData.builder().respondents1(wrapElements(REGISTERED_RESPONDENT)).build();

        Set<String> actual = underTest.getEmailsByPreference(caseData, EMAIL);

        assertThat(actual).isEqualTo(Set.of());
    }

    @Test
    void testFilterOnlyEmailRepresentativesFromRegisteredChildSolicitors() {
        CaseData caseData = CaseData.builder().children1(wrapElements(REGISTERED_CHILD)).build();

        Set<String> actual = underTest.getEmailsByPreference(caseData, EMAIL);

        assertThat(actual).isEqualTo(Set.of());
    }

    @Test
    void testFilterOnlyEmailRepresentativesFromRespondentSolicitorsUnregistered() {
        CaseData caseData = CaseData.builder().respondents1(wrapElements(UNREGISTERED_RESPONDENT)).build();

        Set<String> actual = underTest.getEmailsByPreference(caseData, EMAIL);

        assertThat(actual).isEqualTo(Set.of(EMAIL_1));
    }

    @Test
    void testFilterOnlyEmailRepresentativesFromChildSolicitorsUnregistered() {
        CaseData caseData = CaseData.builder().children1(wrapElements(UNREGISTERED_CHILD)).build();

        Set<String> actual = underTest.getEmailsByPreference(caseData, EMAIL);

        assertThat(actual).isEqualTo(Set.of(EMAIL_5));
    }

    @Test
    void testFilterOnlyDigitalRepresentativesFromRegisteredRespondentSolicitors() {
        CaseData caseData = CaseData.builder().respondents1(wrapElements(REGISTERED_RESPONDENT)).build();

        Set<String> actual = underTest.getEmailsByPreference(caseData, DIGITAL_SERVICE);

        assertThat(actual).isEqualTo(Set.of(EMAIL_2));
    }

    @Test
    void testFilterOnlyDigitalRepresentativesFromRegisteredChildSolicitors() {
        CaseData caseData = CaseData.builder().children1(wrapElements(REGISTERED_CHILD)).build();

        Set<String> actual = underTest.getEmailsByPreference(caseData, DIGITAL_SERVICE);

        assertThat(actual).isEqualTo(Set.of(EMAIL_6));
    }

    @Test
    void testFilterOnlyDigitalRepresentativesFromRespondentSolicitorsUnregistered() {
        CaseData caseData = CaseData.builder().respondents1(wrapElements(UNREGISTERED_RESPONDENT)).build();

        Set<String> actual = underTest.getEmailsByPreference(caseData, DIGITAL_SERVICE);

        assertThat(actual).isEqualTo(Set.of());
    }

    @Test
    void testFilterOnlyDigitalRepresentativesFromChildSolicitorsUnregistered() {
        CaseData caseData = CaseData.builder().children1(wrapElements(UNREGISTERED_CHILD)).build();

        Set<String> actual = underTest.getEmailsByPreference(caseData, DIGITAL_SERVICE);

        assertThat(actual).isEqualTo(Set.of());
    }

    @Test
    void testFilterOnlyEmailRepresentativesFromRespondentSolicitorsWhenUnregistered() {
        CaseData caseData = CaseData.builder().respondents1(wrapElements(UNREGISTERED_RESPONDENT)).build();

        Set<String> actual = underTest.getEmailsByPreference(caseData, EMAIL);

        assertThat(actual).isEqualTo(Set.of(EMAIL_1));
    }

    @Test
    void testFilterOnlyEmailRepresentativesFromChildrenSolicitorsWhenUnregistered() {
        CaseData caseData = CaseData.builder().children1(wrapElements(UNREGISTERED_CHILD)).build();

        Set<String> actual = underTest.getEmailsByPreference(caseData, EMAIL);

        assertThat(actual).isEqualTo(Set.of(EMAIL_5));
    }

    @Test
    void testFilterOnlyEmailRepresentativesFromRespondentSolicitorsWhenNoOrganisation() {
        CaseData caseData = CaseData.builder().respondents1(wrapElements(
            Respondent.builder()
                .solicitor(RespondentSolicitor.builder()
                    .email(EMAIL_1)
                    .organisation(null)
                    .build())
                .build()
        )).build();

        Set<String> actual = underTest.getEmailsByPreference(caseData, EMAIL);

        assertThat(actual).isEqualTo(Set.of(EMAIL_1));
    }

    @Test
    void testFilterOnlyEmailRepresentativesFromChildSolicitorsWhenNoOrganisation() {
        CaseData caseData = CaseData.builder().children1(wrapElements(
            Child.builder()
                .solicitor(RespondentSolicitor.builder()
                    .email(EMAIL_5)
                    .organisation(null)
                    .build())
                .build()
        )).build();

        Set<String> actual = underTest.getEmailsByPreference(caseData, EMAIL);

        assertThat(actual).isEqualTo(Set.of(EMAIL_5));
    }

    @Test
    void testFilterOnlyEmailRepresentativesFromRespondentSolicitorsWhenNoSolicitor() {
        CaseData caseData = CaseData.builder().respondents1(wrapElements(
            Respondent.builder()
                .solicitor(null)
                .build()
        )).build();

        Set<String> actual = underTest.getEmailsByPreference(caseData, EMAIL);

        assertThat(actual).isEqualTo(Set.of());
    }

    @Test
    void testFilterOnlyEmailRepresentativesFromChildrenSolicitorsWhenNoSolicitor() {
        CaseData caseData = CaseData.builder().children1(wrapElements(
            Child.builder()
                .solicitor(null)
                .build()
        )).build();

        Set<String> actual = underTest.getEmailsByPreference(caseData, EMAIL);

        assertThat(actual).isEqualTo(Set.of());
    }

    @Test
    void testFilterOnlyEmailRepresentativesRemoveDuplicates() {
        CaseData caseData = CaseData.builder().representatives(wrapElements(EMAIL_REP, EMAIL_REP)).build();

        Set<String> actual = underTest.getEmailsByPreference(caseData, EMAIL);

        assertThat(actual).isEqualTo(Set.of(
            EMAIL_3
        ));
    }

    @Test
    void testFilterOnlyEmailRepresentativesRemoveNulls() {
        CaseData caseData = CaseData.builder()
            .representatives(wrapElements(
                Representative.builder()
                    .email(null)
                    .servingPreferences(EMAIL)
                    .build()
            )).build();

        Set<String> actual = underTest.getEmailsByPreference(caseData, EMAIL);

        assertThat(actual).isEqualTo(Set.of());
    }

    @Test
    void testFilterOnlyEmailRepresentativesRemoveEmpty() {
        CaseData caseData = CaseData.builder()
            .representatives(wrapElements(
                Representative.builder()
                    .email("")
                    .servingPreferences(EMAIL)
                    .build()
            ))
            .build();

        Set<String> actual = underTest.getEmailsByPreference(caseData, EMAIL);

        assertThat(actual).isEqualTo(Set.of());
    }

    @Test
    void testFilterOnlyDigitalMixedRepresentatives() {
        CaseData caseData = CaseData.builder()
            .respondents1(wrapElements(UNREGISTERED_RESPONDENT, REGISTERED_RESPONDENT))
            .children1(wrapElements(UNREGISTERED_CHILD, REGISTERED_CHILD))
            .representatives(wrapElements(EMAIL_REP, DIGITAL_REP))
            .build();

        Set<String> actual = underTest.getEmailsByPreference(caseData, DIGITAL_SERVICE);

        assertThat(actual).isEqualTo(Set.of(EMAIL_2, EMAIL_4, EMAIL_6));
    }

    @Test
    void testFilterOnlyEmailMixedRepresentatives() {
        CaseData caseData = CaseData.builder()
            .respondents1(wrapElements(UNREGISTERED_RESPONDENT, REGISTERED_RESPONDENT))
            .children1(wrapElements(UNREGISTERED_CHILD, REGISTERED_CHILD))
            .representatives(wrapElements(EMAIL_REP, DIGITAL_REP))
            .build();

        Set<String> actual = underTest.getEmailsByPreference(caseData, EMAIL);

        assertThat(actual).isEqualTo(Set.of(EMAIL_1, EMAIL_3, EMAIL_5));
    }

    @Test
    void testFilterRepresentativesByRole() {
        CaseData caseData = CaseData.builder().representatives(wrapElements(CARCASS_REP, RESPONDENT_REP)).build();
        List<RepresentativeRole.Type> roles = List.of(CAFCASS, RESPONDENT);

        Set<String> actual = underTest.getRepresentativeEmailsFilteredByRole(caseData, DIGITAL_SERVICE, roles);

        assertThat(actual).isEqualTo(newHashSet(EMAIL_1, EMAIL_2));
    }

    @Test
    void testFilterRepresentativesByRoleRemovesRepresentativesWithIncorrectRoles() {
        CaseData caseData = CaseData.builder().representatives(wrapElements(CARCASS_REP, RESPONDENT_REP)).build();
        List<RepresentativeRole.Type> roles = List.of(CAFCASS, LASOLICITOR);

        Set<String> actual = underTest.getRepresentativeEmailsFilteredByRole(caseData, DIGITAL_SERVICE, roles);

        assertThat(actual).isEqualTo(newHashSet(EMAIL_1));
    }

    @Test
    void shouldGetRespondentSolicitorColleagueEmailsAsWell() {
        CaseData caseData = CaseData.builder()
            .respondents1(List.of(element(REGISTERED_RESPONDENT_WITH_COLLEAGUES)))
            .build();

        HashSet<String> emails = underTest.getRespondentSolicitorEmails(caseData, DIGITAL_SERVICE);

        assertThat(emails).containsExactlyInAnyOrder(EMAIL_2, EMAIL_8);
    }

    @Test
    void shouldGetChildSolicitorColleagueEmailsAsWell() {
        CaseData caseData = CaseData.builder()
            .children1(List.of(element(REGISTERED_CHILD_WITH_COLLEAGUES)))
            .build();

        Set<String> emails = underTest.getChildrenSolicitorEmails(caseData, DIGITAL_SERVICE);

        assertThat(emails).containsExactlyInAnyOrder(EMAIL_6, EMAIL_8);
    }

    @Test
    void shouldGetChildSolicitorColleagueEmailsAsWellByCaseRole_CHILDSOLICITORA() {
        CaseData caseData = CaseData.builder()
            .children1(List.of(element(REGISTERED_CHILD_WITH_COLLEAGUES),
                element(REGISTERED_CHILD_2_WITH_COLLEAGUES)))
            .build();

        Set<String> emails = underTest.getChildrenSolicitorEmails(caseData, CaseRole.CHILDSOLICITORA,
            DIGITAL_SERVICE);

        assertThat(emails).containsExactlyInAnyOrder(EMAIL_6, EMAIL_8);
    }

    @Test
    void shouldGetChildSolicitorColleagueEmailsAsWellByCaseRole_CHILDSOLICITORB() {
        CaseData caseData = CaseData.builder()
            .children1(List.of(element(REGISTERED_CHILD_WITH_COLLEAGUES),
                element(REGISTERED_CHILD_2_WITH_COLLEAGUES)))
            .build();

        Set<String> emails = underTest.getChildrenSolicitorEmails(caseData, CaseRole.CHILDSOLICITORB,
            DIGITAL_SERVICE);

        assertThat(emails).containsExactlyInAnyOrder(EMAIL_9, EMAIL_10);
    }
}
