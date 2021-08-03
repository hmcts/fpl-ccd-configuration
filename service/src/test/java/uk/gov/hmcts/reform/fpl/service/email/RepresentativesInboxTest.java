package uk.gov.hmcts.reform.fpl.service.email;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.Others;
import uk.gov.hmcts.reform.fpl.model.Recipient;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.EMAIL;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.POST;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testAddress;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testOther;

class RepresentativesInboxTest {

    private static final String ORGANISATION_ID = "ORGANISATION_ID";

    private static final String EMAIL_1 = "email1";
    private static final String EMAIL_2 = "email2";
    private static final String EMAIL_3 = "email3";
    private static final String EMAIL_4 = "email4";
    private static final String EMAIL_5 = "email5";
    private static final String EMAIL_6 = "email6";
    private static final String EMAIL_7 = "email7";

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

    private final RepresentativesInbox underTest = new RepresentativesInbox();
    public static final UUID EMAIL_REP_UUID = UUID.randomUUID();
    public static final UUID EMAIL_REP2_UUID = UUID.randomUUID();
    public static final UUID DIGITAL_REP_UUID = UUID.randomUUID();

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

    @ParameterizedTest
    @MethodSource("representativesData")
    @SuppressWarnings("unchecked")
    void shouldReturnNonSelectedRepresentativesByServingPreference(
        CaseData caseData,
        List<Element<Respondent>> selectedRespondents,
        RepresentativeServingPreferences servingPreferences,
        Set<Element<Representative>> expectedRepresentatives) {

        Set<Element<Representative>> unselectedRecipients
            = (Set<Element<Representative>>) underTest.getNonSelectedRespondentsRecipients(
            servingPreferences, caseData, selectedRespondents, Function.identity());

        assertThat(unselectedRecipients).isEqualTo(expectedRepresentatives);
    }

    @ParameterizedTest
    @MethodSource("unrepresentedRepresentativesData")
    void shouldReturnUnrepresentedRepresentatives(List<Element<Respondent>> selectedRespondents,
                                                  Set<Recipient> expectedRecipients) {
        Set<Recipient> unselectedRecipients
            = underTest.getSelectedRecipientsWithNoRepresentation(selectedRespondents);

        assertThat(unselectedRecipients).isEqualTo(expectedRecipients);
    }

    private static Stream<Arguments> unrepresentedRepresentativesData() {
        List<Element<Representative>> representatives = List.of(
            element(EMAIL_REP_UUID, EMAIL_REP),
            element(DIGITAL_REP_UUID, DIGITAL_REP));

        Respondent respondentWithEmailRep = Respondent.builder()
            .representedBy(wrapElements(EMAIL_REP_UUID)).build();
        Respondent respondentWithDigitalRep = Respondent.builder()
            .representedBy(wrapElements(DIGITAL_REP_UUID)).build();
        Respondent respondentWithValidAddress = Respondent.builder()
            .party(RespondentParty.builder().address(testAddress()).build())
            .representedBy(List.of()).build();
        Respondent respondentWithIncompleteAddress = Respondent.builder()
            .party(RespondentParty.builder().address(Address.builder().build()).build())
            .representedBy(List.of()).build();
        Respondent respondentWithoutAddress = Respondent.builder().party(RespondentParty.builder().build()).build();

        List<Element<Respondent>> respondents = wrapElements(respondentWithEmailRep, respondentWithDigitalRep,
            respondentWithValidAddress, respondentWithIncompleteAddress, respondentWithoutAddress);

        return Stream.of(
            Arguments.of(
                List.of(respondents.get(0), respondents.get(2)),
                Set.of(respondentWithValidAddress)
            ),
            Arguments.of(
                List.of(respondents.get(2), respondents.get(3), respondents.get(4)),
                Set.of(respondentWithValidAddress)),
            Arguments.of(List.of(respondents.get(0), respondents.get(1)), Set.of()),
            Arguments.of(List.of(respondents.get(3), respondents.get(4)), Set.of())
        );
    }

    private static Stream<Arguments> representativesData() {
        List<Element<Representative>> representatives = List.of(
            element(EMAIL_REP_UUID, EMAIL_REP),
            element(EMAIL_REP2_UUID, EMAIL_REP2),
            element(DIGITAL_REP_UUID, DIGITAL_REP));

        Respondent respondentWithEmailRep = Respondent.builder()
            .representedBy(wrapElements(EMAIL_REP_UUID)).build();
        Respondent respondentWithDigitalRep = Respondent.builder()
            .representedBy(wrapElements(DIGITAL_REP_UUID)).build();
        Respondent unrepresentedRespondent = Respondent.builder()
            .representedBy(List.of()).build();

        Other firstOther = testOther("other1");
        firstOther.addRepresentative(EMAIL_REP2_UUID);

        List<Element<Respondent>> respondents = wrapElements(
            respondentWithEmailRep, respondentWithDigitalRep, unrepresentedRespondent);

        CaseData caseData = CaseData.builder()
            .respondents1(respondents)
            .representatives(representatives)
            .others(Others.builder().firstOther(firstOther).build())
            .build();

        return Stream.of(
            Arguments.of(
                caseData, List.of(respondents.get(0), respondents.get(2)), EMAIL, Set.of()),
            Arguments.of(caseData, List.of(respondents.get(1)), EMAIL, Set.of(representatives.get(0))),
            Arguments.of(
                caseData, List.of(respondents.get(2)), EMAIL, Set.of(representatives.get(0))),
            Arguments.of(caseData, respondents, EMAIL, Set.of()),
            Arguments.of(caseData, respondents, DIGITAL_SERVICE, Set.of()),
            Arguments.of(caseData, List.of(respondents.get(1), respondents.get(2)), DIGITAL_SERVICE, Set.of()),
            Arguments.of(
                caseData, List.of(), DIGITAL_SERVICE, Set.of(representatives.get(2)))
        );
    }
}
