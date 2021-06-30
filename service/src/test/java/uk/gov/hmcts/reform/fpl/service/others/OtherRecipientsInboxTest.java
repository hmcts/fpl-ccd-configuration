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

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

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

    @SuppressWarnings("unchecked")
    @Test
    void testShouldReturnAllOtherNonSelectedRecipientEmailsWhoAreRepresented() {
        Other firstOther = testOther("First other");
        firstOther.addRepresentative(REPRESENTATIVE_ID_1);

        Other secondOther = testOther("Second other");
        secondOther.addRepresentative(REPRESENTATIVE_ID_2);

        Representative representingOther1 = Representative.builder()
            .servingPreferences(EMAIL)
            .role(RepresentativeRole.REPRESENTING_OTHER_PERSON_1)
            .email(REPRESENTING_OTHER_1_EMAIL)
            .build();

        Representative representingOther2 = Representative.builder()
            .servingPreferences(EMAIL)
            .role(RepresentativeRole.REPRESENTING_OTHER_PERSON_2)
            .email(REPRESENTING_OTHER_2_EMAIL)
            .build();

        CaseData caseData = CaseData.builder()
            .others(Others.builder()
                .firstOther(firstOther)
            .additionalOthers(List.of(element(secondOther), element(testOther("Third other"))))
            .build())
            .representatives(List.of(element(REPRESENTATIVE_ID_1, representingOther1),
                element(REPRESENTATIVE_ID_2, representingOther2)))
            .build();

        List<Element<Other>> othersSelected = List.of(element(firstOther));

        Set<String> nonSelectedRecipients = (Set<String>) underTest.getNonSelectedRecipients(EMAIL, caseData,
            othersSelected,
            element -> element.getValue().getEmail());
        assertThat(nonSelectedRecipients).containsOnly(REPRESENTING_OTHER_2_EMAIL);
    }

    @SuppressWarnings("unchecked")
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

        Set<Recipient> nonSelectedRecipients = (Set<Recipient>) underTest.getNonSelectedRecipients(EMAIL, caseData,
            othersSelected, Function.identity());
        assertThat(nonSelectedRecipients).isEmpty();
    }

    @SuppressWarnings("unchecked")
    @Test
    void testShouldReturnAllOtherNonSelectedRecipientsWhoAreRepresented() {
        Other firstOther = testOther("First other");
        firstOther.addRepresentative(REPRESENTATIVE_ID_1);

        Other secondOther = testOther("Second other");
        secondOther.addRepresentative(REPRESENTATIVE_ID_2);

        Representative representingOther1 = Representative.builder()
            .servingPreferences(EMAIL)
            .role(RepresentativeRole.REPRESENTING_OTHER_PERSON_1)
            .email(REPRESENTING_OTHER_1_EMAIL)
            .build();

        Representative representingOther2 = Representative.builder()
            .servingPreferences(POST)
            .role(RepresentativeRole.REPRESENTING_OTHER_PERSON_2)
            .email(REPRESENTING_OTHER_2_EMAIL)
            .build();

        CaseData caseData = CaseData.builder()
            .others(Others.builder()
                .firstOther(firstOther)
                .additionalOthers(List.of(element(secondOther),
                    element(testOther("Third other"))))
                .build())
            .representatives(List.of(element(REPRESENTATIVE_ID_1, representingOther1),
                element(REPRESENTATIVE_ID_2, representingOther2)))
            .build();

        List<Element<Other>> othersSelected = List.of(element(firstOther));

        Representative expectedRecipient = Representative.builder()
            .email(REPRESENTING_OTHER_2_EMAIL)
            .role(RepresentativeRole.REPRESENTING_OTHER_PERSON_2)
            .servingPreferences(POST)
            .build();

        Set<Recipient> nonSelectedRecipients = (Set<Recipient>) underTest.getNonSelectedRecipients(POST, caseData,
            othersSelected,
            element -> element.getValue());
        assertThat(nonSelectedRecipients).containsOnly(expectedRecipient);
    }

    @SuppressWarnings("unchecked")
    @Test
    void testNotShouldReturnNonSelectedRepresentativesNotRepresentingOther() {
        Respondent respondent = Respondent.builder()
            .representedBy(List.of(element(REPRESENTATIVE_ID_1)))
            .party(RespondentParty.builder()
            .firstName("Respondent 1")
                .build())
            .build();

        Representative representingRespondent = Representative.builder()
            .servingPreferences(EMAIL)
            .role(RepresentativeRole.REPRESENTING_RESPONDENT_1)
            .email(REPRESENTING_RESPONDENT_1_EMAIL)
            .build();

        CaseData caseData = CaseData.builder()
            .respondents1(List.of(element(respondent)))
            .representatives(List.of(element(REPRESENTATIVE_ID_1, representingRespondent)))
            .build();

        List<Element<Other>> othersSelected = Collections.emptyList();

        Set<String> nonSelectedRecipients = (Set<String>) underTest.getNonSelectedRecipients(EMAIL, caseData,
            othersSelected,
            element -> element.getValue().getEmail());
        assertThat(nonSelectedRecipients).isEmpty();
    }
}
