package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.Others;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.order.selector.Selector;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.EMAIL;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.POST;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testAddress;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testOther;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testRespondent;

@ExtendWith(MockitoExtension.class)
class PeopleInCaseServiceTest {

    private static final String EMAIL_1 = "email1@test.com";
    private static final String EMAIL_2 = "email2@test.com";
    private static final String EMAIL_3 = "email3@test.com";

    private static final List<Element<Other>> SELECTED_OTHERS = wrapElements(
        testOther("First other"), testOther("Second other"));

    private static final Element<Respondent> FIRST_RESPONDENT = testRespondent("First", "Respondent");
    public static final Element<Respondent> SECOND_RESPONDENT = testRespondent("Second", "Respondent");

    private static final List<Element<Respondent>> SELECTED_RESPONDENTS
        = List.of(FIRST_RESPONDENT, SECOND_RESPONDENT);

    private static final Element<Representative> EMAIL_REP = element(Representative.builder()
        .email(EMAIL_1)
        .servingPreferences(EMAIL)
        .build());

    private static final Element<Representative> EMAIL_REP2 = element(Representative.builder()
        .email(EMAIL_2)
        .servingPreferences(EMAIL)
        .build());
    private static final Element<Representative> DIGITAL_REP = element(Representative.builder()
        .email(EMAIL_3)
        .servingPreferences(DIGITAL_SERVICE)
        .build());
    private static final Element<Representative> POST_REP = element(Representative.builder()
        .address(testAddress())
        .servingPreferences(POST)
        .build());

    private PeopleInCaseService underTest = new PeopleInCaseService();

    @Test
    void shouldBuildExpectedLabelWhenRespondentsAndOthersExistInList() {
        List<Element<Respondent>> respondents = wrapElements(
            Respondent.builder().party(RespondentParty.builder().firstName("John").lastName("Smith").build()).build(),
            Respondent.builder().party(RespondentParty.builder().firstName("Tim").lastName("Jones").build()).build());

        List<Element<Other>> others = wrapElements(
            Other.builder().firstName("James Daniels").build(),
            Other.builder().firstName("Bob Martyn").build());

        String expectedLabel = "Person 1: Respondent 1 - John Smith\nPerson 2: Respondent 2 - Tim Jones\n"
            + "Person 3: Other 1 - James Daniels\nPerson 4: Other 2 - Bob Martyn\n";

        String actual = underTest.buildPeopleInCaseLabel(respondents, others);
        assertThat(actual).isEqualTo(expectedLabel);
    }

    @Test
    void shouldBuildExpectedLabelWhenRespondentsAndAdditionalOthersExistInList() {
        List<Element<Respondent>> respondents = wrapElements(
            Respondent.builder().party(RespondentParty.builder().firstName("John").lastName("Smith").build()).build());

        List<Element<Other>> others = wrapElements(Other.builder().firstName("Bob Martyn").build());

        String expectedLabel = "Person 1: Respondent 1 - John Smith\nPerson 2: Other 1 - Bob Martyn\n";

        String actual = underTest.buildPeopleInCaseLabel(respondents, others);
        assertThat(actual).isEqualTo(expectedLabel);
    }

    @Test
    void shouldBuildExpectedLabelWhenOthersAreEmpty() {
        List<Element<Respondent>> respondents = wrapElements(Respondent.builder()
            .party(RespondentParty.builder().firstName("John").lastName("Smith").build()).build());
        String expectedLabel = "Person 1: Respondent 1 - John Smith\n";

        String actual = underTest.buildPeopleInCaseLabel(respondents, List.of());
        assertThat(actual).isEqualTo(expectedLabel);
    }

    @Test
    void shouldBuildExpectedLabelWhenRespondentsAreEmpty() {
        List<Element<Other>> others = wrapElements(Other.builder().firstName("James Daniels").build());

        String expectedOthersLabel = "Person 1: Other 1 - James Daniels\n";

        String actual = underTest.buildPeopleInCaseLabel(List.of(), others);

        assertThat(actual).isEqualTo(expectedOthersLabel);
    }

    @Test
    void shouldReturnExpectedMessageWhenRespondentsAndOthersAreEmpty() {
        String actual = underTest.buildPeopleInCaseLabel(List.of(), List.of());

        assertThat(actual).isEqualTo("No respondents and others on the case");
    }

    @Test
    void shouldReturnAllRespondentsAndOthersWhenSelectedAll() {
        CaseData caseData = CaseData.builder()
            .respondents1(SELECTED_RESPONDENTS)
            .others(Others.from(SELECTED_OTHERS))
            .personSelector(Selector.builder().build())
            .notifyApplicationsToAllOthers("Yes")
            .build();

        List<Element<Other>> selectedOthers = underTest.getSelectedOthers(caseData);
        assertThat(selectedOthers).hasSize(SELECTED_OTHERS.size());
        assertThat(unwrapElements(selectedOthers)).isEqualTo(unwrapElements(SELECTED_OTHERS));
    }

    @Test
    void shouldReturnSelectedOthers() {
        CaseData caseData = CaseData.builder()
            .respondents1(SELECTED_RESPONDENTS)
            .others(Others.from(SELECTED_OTHERS))
            .personSelector(Selector.builder().selected(List.of(0, 2)).build())
            .notifyApplicationsToAllOthers("No")
            .build();

        List<Element<Other>> selectedOthers = underTest.getSelectedOthers(caseData);
        assertThat(selectedOthers).hasSize(1);
        assertThat(unwrapElements(selectedOthers))
            .containsExactly(SELECTED_OTHERS.get(0).getValue());
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldReturnSelectedOthersWhenRespondentsAreNullEmpty(List<Element<Respondent>> respondents) {
        CaseData caseData = CaseData.builder()
            .respondents1(respondents)
            .others(Others.from(SELECTED_OTHERS))
            .personSelector(Selector.builder().selected(List.of(0, 2)).build())
            .notifyApplicationsToAllOthers("No")
            .build();

        List<Element<Other>> selectedOthers = underTest.getSelectedOthers(caseData);
        assertThat(selectedOthers).hasSize(1);
        assertThat(unwrapElements(selectedOthers)).containsExactly(SELECTED_OTHERS.get(0).getValue());
    }

    @Test
    void shouldReturnEmptyWhenNoneOfTheOthersAreSelected() {
        CaseData caseData = CaseData.builder()
            .respondents1(SELECTED_RESPONDENTS)
            .others(Others.from(SELECTED_OTHERS))
            .personSelector(Selector.builder().selected(List.of()).build())
            .notifyApplicationsToAllOthers("No")
            .build();

        assertThat(underTest.getSelectedOthers(caseData)).isEmpty();
    }

    @Test
    void shouldReturnEmptyWhenSelectorIsNull() {
        CaseData caseData = CaseData.builder()
            .respondents1(SELECTED_RESPONDENTS)
            .others(Others.from(SELECTED_OTHERS))
            .personSelector(null)
            .notifyApplicationsToAllOthers("No")
            .build();

        assertThat(underTest.getSelectedOthers(caseData)).isEmpty();
    }

    @Test
    void shouldReturnAllRespondentsWhenSelectedAllPeopleInTheCase() {
        CaseData caseData = CaseData.builder()
            .respondents1(SELECTED_RESPONDENTS)
            .personSelector(Selector.builder().build())
            .notifyApplicationsToAllOthers("Yes")
            .build();

        assertThat(underTest.getSelectedRespondents(caseData)).isEqualTo(SELECTED_RESPONDENTS);
    }

    @Test
    void shouldReturnSelectedRespondents() {
        CaseData caseData = CaseData.builder()
            .respondents1(SELECTED_RESPONDENTS)
            .personSelector(Selector.builder().selected(List.of(0, 2)).build())
            .notifyApplicationsToAllOthers("No")
            .build();

        assertThat(underTest.getSelectedRespondents(caseData)).isEqualTo(List.of(SELECTED_RESPONDENTS.get(0)));
    }

    @Test
    void shouldReturnEmptyWhenNoRespondentsAreSelected() {
        CaseData caseData = CaseData.builder()
            .respondents1(SELECTED_RESPONDENTS)
            .personSelector(Selector.builder().selected(List.of(2, 3)).build())
            .notifyApplicationsToAllOthers("No")
            .build();

        assertThat(underTest.getSelectedRespondents(caseData)).isEmpty();
    }

    @Test
    void shouldReturnEmptyWhenSelectedItemsAreEmpty() {
        CaseData caseData = CaseData.builder()
            .respondents1(SELECTED_RESPONDENTS)
            .personSelector(Selector.builder().selected(List.of()).build())
            .notifyApplicationsToAllOthers("No")
            .build();

        assertThat(underTest.getSelectedRespondents(caseData)).isEmpty();
    }

    @Test
    void shouldReturnEmptyRespondentsListWhenSelectorIsNull() {
        CaseData caseData = CaseData.builder()
            .respondents1(SELECTED_RESPONDENTS)
            .personSelector(null)
            .notifyApplicationsToAllOthers("No")
            .build();

        assertThat(underTest.getSelectedRespondents(caseData)).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("peopleNotifiedData")
    void shouldReturnNotifiedPeoplesNamesWhenSelectedRespondentsHaveRepresentatives(
        List<Element<Representative>> allRepresentatives,
        List<Element<Respondent>> selectedRespondents,
        List<Element<Other>> selectedOthers,
        String expected) {

        String peopleNotified = underTest.getPeopleNotified(allRepresentatives, selectedRespondents, selectedOthers);
        assertThat(peopleNotified).isEqualTo(expected);
    }

    private static Stream<Arguments> peopleNotifiedData() {
        Element<Respondent> respondentWithEmailRep = buildRespondentWithRepresentative(
            "First", "Respondent", false, wrapElements(EMAIL_REP.getId()));
        Element<Respondent> respondentWithDigitalRep = buildRespondentWithRepresentative(
            "Second", "Respondent", false, wrapElements(DIGITAL_REP.getId()));
        Element<Respondent> unrepresentedRespondent = buildRespondentWithRepresentative(
            "Third", "Respondent", true, List.of());

        Element<Other> firstOther = element(Other.builder().name("First Other").build());
        firstOther.getValue().addRepresentative(DIGITAL_REP.getId());
        Element<Other> other2 = element(Other.builder().name("Second Other").address(testAddress()).build());
        firstOther.getValue().addRepresentative(POST_REP.getId());
        Element<Other> unrepresentedOther = element(
            Other.builder().name("Third Other").address(testAddress()).build());

        List<Element<Representative>> representatives = List.of(
            EMAIL_REP, EMAIL_REP2, DIGITAL_REP, POST_REP);

        return Stream.of(
            Arguments.of(representatives,
                List.of(respondentWithEmailRep, respondentWithDigitalRep),
                List.of(),
                "First Respondent, Second Respondent"),
            Arguments.of(representatives,
                List.of(respondentWithEmailRep, respondentWithDigitalRep, unrepresentedRespondent),
                List.of(firstOther),
                "First Respondent, Second Respondent, Third Respondent, First Other"),
            Arguments.of(representatives,
                List.of(),
                List.of(other2, unrepresentedOther),
                "Second Other, Third Other"),
            Arguments.of(representatives, List.of(), List.of(), "")
        );
    }

    private static Element<Respondent> buildRespondentWithRepresentative(String firstName,
                                                                         String lastName,
                                                                         boolean addAddress,
                                                                         List<Element<UUID>> representativeIds) {
        return element(UUID.randomUUID(), Respondent.builder()
            .party(RespondentParty.builder()
                .firstName(firstName)
                .lastName(lastName)
                .address(addAddress ? testAddress() : null)
                .build())
            .representedBy(representativeIds).build());
    }
}
