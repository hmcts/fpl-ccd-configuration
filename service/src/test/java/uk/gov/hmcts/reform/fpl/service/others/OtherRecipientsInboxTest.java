package uk.gov.hmcts.reform.fpl.service.others;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeRole;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.Others;
import uk.gov.hmcts.reform.fpl.model.Recipient;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.EMAIL;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.POST;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testOther;

class OtherRecipientsInboxTest {

    private static final String REPRESENTING_OTHER_1_EMAIL = "representingOther1@test.com";
    public static final String REPRESENTING_OTHER_2_EMAIL = "representingOther2@test.com";
    public static final String REPRESENTING_RESPONDENT_1_EMAIL = "representingRespondent@test.com";
    private static final UUID REPRESENTATIVE_ID_1 = UUID.randomUUID();
    private static final UUID REPRESENTATIVE_ID_2 = UUID.randomUUID();
    private final OtherRecipientsInbox underTest = new OtherRecipientsInbox();

    @Test
    void testShouldReturnAllOtherRecipientsWhoAreRepresentedByEmail() {
        Other firstOther = testOther("First other");
        firstOther.addRepresentative(REPRESENTATIVE_ID_1);

        Other secondOther = testOther("Second other");
        secondOther.addRepresentative(REPRESENTATIVE_ID_2);

        Element<Representative> representingOther1 = element(REPRESENTATIVE_ID_1, Representative.builder()
            .servingPreferences(EMAIL)
            .role(RepresentativeRole.REPRESENTING_OTHER_PERSON_1)
            .email(REPRESENTING_OTHER_1_EMAIL)
            .build());

        Element<Representative> representingOther2 = element(REPRESENTATIVE_ID_2, Representative.builder()
            .servingPreferences(EMAIL)
            .role(RepresentativeRole.REPRESENTING_OTHER_PERSON_2)
            .email(REPRESENTING_OTHER_2_EMAIL)
            .build());

        CaseData caseData = CaseData.builder()
            .others(Others.builder()
                .firstOther(firstOther)
            .additionalOthers(List.of(element(secondOther), element(testOther("Third other"))))
            .build())
            .representatives(List.of(representingOther1, representingOther2))
            .build();

        Set<Element<Representative>> allRecipients = underTest.getAllRecipients(
            EMAIL, caseData, Function.identity()
        );
        assertThat(allRecipients).contains(representingOther1, representingOther2);
    }

    @Test
    void testShouldReturnAllOtherNonSelectedRecipientsWhoAreRepresentedByEmail() {
        Other firstOther = testOther("First other");
        firstOther.addRepresentative(REPRESENTATIVE_ID_1);

        Other secondOther = testOther("Second other");
        secondOther.addRepresentative(REPRESENTATIVE_ID_2);

        Element<Representative> representingOther1 = element(REPRESENTATIVE_ID_1, Representative.builder()
            .servingPreferences(EMAIL)
            .role(RepresentativeRole.REPRESENTING_OTHER_PERSON_1)
            .email(REPRESENTING_OTHER_1_EMAIL)
            .build());

        Element<Representative> representingOther2 = element(REPRESENTATIVE_ID_2, Representative.builder()
            .servingPreferences(EMAIL)
            .role(RepresentativeRole.REPRESENTING_OTHER_PERSON_2)
            .email(REPRESENTING_OTHER_2_EMAIL)
            .build());

        CaseData caseData = CaseData.builder()
            .others(Others.builder()
                .firstOther(firstOther)
            .additionalOthers(List.of(element(secondOther), element(testOther("Third other"))))
            .build())
            .representatives(List.of(representingOther1, representingOther2))
            .build();

        List<Element<Other>> othersSelected = List.of(element(firstOther));

        Set<Element<Representative>> nonSelectedRecipients = underTest.getNonSelectedRecipients(
            EMAIL, caseData, othersSelected, Function.identity()
        );
        assertThat(nonSelectedRecipients).containsOnly(representingOther2);
    }

    @Test
    void testShouldReturnEmptySetWhenNoRepresentedOthers() {
        Other firstOther = testOther("First other");
        Other secondOther = testOther("Second other");

        CaseData caseData = CaseData.builder()
            .others(Others.builder()
                .firstOther(firstOther)
                .additionalOthers(List.of(element(firstOther),
                    element(secondOther),
                    element(testOther("Third other"))))
                .build())
            .build();

        List<Element<Other>> othersSelected = List.of(element(firstOther));

        Set<Element<Representative>> nonSelectedRecipients = underTest.getNonSelectedRecipients(
            EMAIL, caseData, othersSelected, Function.identity()
        );
        assertThat(nonSelectedRecipients).isEmpty();
    }

    @Test
    void testShouldReturnAllOtherNonSelectedRecipientsWhoAreRepresentedByPost() {
        Other firstOther = testOther("First other");
        firstOther.addRepresentative(REPRESENTATIVE_ID_1);

        Other secondOther = testOther("Second other");
        secondOther.addRepresentative(REPRESENTATIVE_ID_2);

        Element<Representative> representingOther1 = element(REPRESENTATIVE_ID_1, Representative.builder()
            .servingPreferences(EMAIL)
            .role(RepresentativeRole.REPRESENTING_OTHER_PERSON_1)
            .email(REPRESENTING_OTHER_1_EMAIL)
            .build());

        Element<Representative> representingOther2 = element(REPRESENTATIVE_ID_2, Representative.builder()
            .servingPreferences(POST)
            .role(RepresentativeRole.REPRESENTING_OTHER_PERSON_2)
            .email(REPRESENTING_OTHER_2_EMAIL)
            .build());

        CaseData caseData = CaseData.builder()
            .others(Others.builder()
                .firstOther(firstOther)
                .additionalOthers(List.of(element(secondOther),
                    element(testOther("Third other"))))
                .build())
            .representatives(List.of(representingOther1,
                representingOther2))
            .build();

        List<Element<Other>> othersSelected = List.of(element(firstOther));

        Set<Element<Representative>> nonSelectedRecipients = underTest.getNonSelectedRecipients(
            POST, caseData, othersSelected, Function.identity()
        );
        assertThat(nonSelectedRecipients).containsOnly(representingOther2);
    }

    @Test
    void testNotShouldReturnNonSelectedRepresentativesNotRepresentingOther() {
        Respondent respondent = Respondent.builder()
            .representedBy(List.of(element(REPRESENTATIVE_ID_1)))
            .party(RespondentParty.builder()
            .firstName("Respondent 1")
                .build())
            .build();

        Element<Representative> representingRespondent = element(REPRESENTATIVE_ID_1, Representative.builder()
            .servingPreferences(EMAIL)
            .role(RepresentativeRole.REPRESENTING_RESPONDENT_1)
            .email(REPRESENTING_RESPONDENT_1_EMAIL)
            .build());

        CaseData caseData = CaseData.builder()
            .respondents1(List.of(element(respondent)))
            .representatives(List.of(representingRespondent))
            .build();

        List<Element<Other>> othersSelected = emptyList();

        Set<Element<Representative>> nonSelectedRecipients = underTest.getNonSelectedRecipients(
            EMAIL, caseData, othersSelected, Function.identity()
        );
        assertThat(nonSelectedRecipients).isEmpty();
    }

    @Test
    void testShouldReturnSelectedRecipientsWithNoRepresentationWhenAddressPresent() {
        Other firstOther = testOther("First other");
        firstOther.addRepresentative(REPRESENTATIVE_ID_1);

        Other secondOther = testOther("Second other");

        List<Element<Other>> othersSelected = List.of(element(firstOther), element(secondOther));

        Set<Recipient> recipients = underTest.getSelectedRecipientsWithNoRepresentation(othersSelected);

        assertThat(recipients).containsOnly(secondOther.toParty());
    }

    @Test
    void testShouldReturnEmptyListWhenSelectedOthersHaveNoAddressPresent() {
        Other firstOther = Other.builder()
            .name("First other")
            .build();

        List<Element<Other>> othersSelected = List.of(element(firstOther));

        Set<Recipient> recipients = underTest.getSelectedRecipientsWithNoRepresentation(othersSelected);

        assertThat(recipients).isEmpty();
    }

    @Test
    void testShouldReturnEmptyListWhenNoSelectedOthers() {
        List<Element<Other>> othersSelected = emptyList();

        Set<Recipient> recipients = underTest.getSelectedRecipientsWithNoRepresentation(othersSelected);

        assertThat(recipients).isEmpty();
    }
}
