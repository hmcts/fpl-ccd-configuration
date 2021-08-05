package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.model.Address;
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
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.EMAIL;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.POST;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
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

    @Mock
    private OthersService othersService;
    @Mock
    private RespondentService respondentService;
    @InjectMocks
    private PeopleInCaseService underTest;

    @Test
    void shouldBuildExpectedLabelWhenRespondentsAndOthersExistInList() {
        List<Element<Respondent>> respondents = wrapElements(
            Respondent.builder().party(RespondentParty.builder().firstName("John").lastName("Smith").build()).build(),
            Respondent.builder().party(RespondentParty.builder().firstName("Tim").lastName("Jones").build()).build());

        Others others = Others.builder()
            .firstOther(Other.builder().name("James Daniels").build())
            .additionalOthers(wrapElements(Other.builder().name("Bob Martyn").build()))
            .build();

        String expectedRespondentsLabel = "Respondent 1 - John Smith\nRespondent 2 - Tim Jones\n";
        given(respondentService.buildRespondentLabel(respondents)).willReturn(expectedRespondentsLabel);

        String expectedOthersLabel = "Person 1 - James Daniels\nOther person 1 - Bob Martyn\n";
        given(othersService.buildOthersLabel(others)).willReturn(expectedOthersLabel);

        String actual = underTest.buildPeopleInCaseLabel(respondents, others);
        assertThat(actual).isEqualTo(String.join("", expectedRespondentsLabel, expectedOthersLabel));
    }

    @Test
    void shouldBuildExpectedLabelWhenOthersAreEmpty() {
        List<Element<Respondent>> respondents = wrapElements(Respondent.builder()
            .party(RespondentParty.builder().firstName("John").lastName("Smith").build()).build());

        String expectedRespondentsLabel = "Respondent 1 - John Smith\n";
        given(respondentService.buildRespondentLabel(respondents)).willReturn(expectedRespondentsLabel);

        String actual = underTest.buildPeopleInCaseLabel(respondents, Others.builder().build());
        assertThat(actual).isEqualTo(expectedRespondentsLabel);
    }

    @Test
    void shouldBuildExpectedLabelWhenRespondentsAreEmpty() {
        Others others = Others.builder()
            .firstOther(Other.builder().name("James Daniels").build())
            .build();

        String expectedOthersLabel = "Person 1 - James Daniels\n";
        given(othersService.buildOthersLabel(others)).willReturn(expectedOthersLabel);

        String actual = underTest.buildPeopleInCaseLabel(List.of(), others);

        assertThat(actual).isEqualTo(expectedOthersLabel);
    }

    @Test
    void shouldReturnExpectedMessageWhenRespondentsAndOthersAreEmpty() {
        String actual = underTest.buildPeopleInCaseLabel(List.of(), Others.builder().build());

        assertThat(actual).isEqualTo("No respondents and others on the case");
    }

    @Test
    void shouldReturnAllRespondentsAndOthersWhenSelectedAll() {
        List<Element<Other>> selectedOthers = underTest.getSelectedOthers(
            SELECTED_RESPONDENTS, SELECTED_OTHERS, Selector.builder().build(), "Yes");

        assertThat(selectedOthers).isEqualTo(SELECTED_OTHERS);
    }

    @Test
    void shouldReturnSelectedOthers() {
        List<Element<Other>> selectedOthers = underTest.getSelectedOthers(
            SELECTED_RESPONDENTS, SELECTED_OTHERS, Selector.builder().selected(List.of(0, 2)).build(), "No");

        assertThat(selectedOthers).isEqualTo(List.of(SELECTED_OTHERS.get(0)));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldReturnSelectedOthersWhenRespondentsAreNullEmpty(List<Element<Respondent>> respondents) {
        List<Element<Other>> selectedOthers = underTest.getSelectedOthers(
            respondents, SELECTED_OTHERS, Selector.builder().selected(List.of(0, 2)).build(), "No");

        assertThat(selectedOthers).isEqualTo(List.of(SELECTED_OTHERS.get(0)));
    }

    @Test
    void shouldReturnEmptyWhenNoneOfTheOthersAreSelected() {
        List<Element<Other>> selectedOthers = underTest.getSelectedOthers(
            SELECTED_RESPONDENTS, SELECTED_OTHERS, Selector.builder().selected(List.of()).build(), "No");

        assertThat(selectedOthers).isEmpty();
    }

    @Test
    void shouldReturnEmptyWhenSelectorIsNull() {
        List<Element<Other>> selectedOthers = underTest.getSelectedOthers(
            SELECTED_RESPONDENTS, SELECTED_OTHERS, null, "No");

        assertThat(selectedOthers).isEmpty();
    }

    @Test
    void shouldReturnAllRespondentsWhenSelectedAllPeopleInTheCase() {
        List<Element<Respondent>> selectedRespondents = underTest.getSelectedRespondents(
            SELECTED_RESPONDENTS, Selector.builder().build(), "Yes");

        assertThat(selectedRespondents).isEqualTo(SELECTED_RESPONDENTS);
    }

    @Test
    void shouldReturnSelectedRespondents() {
        List<Element<Respondent>> selectedRespondents = underTest.getSelectedRespondents(
            SELECTED_RESPONDENTS, Selector.builder().selected(List.of(0, 2)).build(), "No");

        assertThat(selectedRespondents).isEqualTo(List.of(SELECTED_RESPONDENTS.get(0)));
    }

    @Test
    void shouldReturnEmptyWhenNoRespondentsAreSelected() {
        List<Element<Respondent>> selectedRespondents = underTest.getSelectedRespondents(
            SELECTED_RESPONDENTS, Selector.builder().selected(List.of(2, 3)).build(), "No");

        assertThat(selectedRespondents).isEmpty();
    }

    @Test
    void shouldReturnEmptyWhenSelectedItemsAreEmpty() {
        List<Element<Respondent>> selectedRespondents = underTest.getSelectedRespondents(
            SELECTED_RESPONDENTS, Selector.builder().selected(List.of()).build(), "No");

        assertThat(selectedRespondents).isEmpty();
    }

    @Test
    void shouldReturnEmptyRespondentsListWhenSelectorIsNull() {
        List<Element<Respondent>> selectedRespondents = underTest.getSelectedRespondents(
            SELECTED_RESPONDENTS, null, "No");

        assertThat(selectedRespondents).isEmpty();
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
        Element<Respondent> unrepresentedRespondentWithAddress = buildRespondentWithRepresentative(
            "Third", "Respondent", true, List.of());
        Element<Respondent> unrepresentedRespondent = buildRespondentWithRepresentative(
            "Fourth", "Respondent", false, List.of());
        Element<Representative> repWithInvalidAddress = element(Representative.builder().servingPreferences(POST)
            .address(Address.builder().build()).build());
        Element<Respondent> respondentWithPostRep = buildRespondentWithRepresentative(
            "Fifth", "Respondent", false, wrapElements(repWithInvalidAddress.getId()));
        Element<Representative> repWithInvalidEmail = element(Representative.builder()
            .servingPreferences(EMAIL).email("").build());
        Element<Respondent> respondentWithInvalidEmailRep = buildRespondentWithRepresentative(
            "Sixth", "Respondent", false, wrapElements(repWithInvalidEmail.getId()));

        Element<Other> firstOther = element(Other.builder().name("First Other").build());
        firstOther.getValue().addRepresentative(DIGITAL_REP.getId());
        Element<Other> other2 = element(Other.builder().name("Second Other").address(testAddress()).build());
        firstOther.getValue().addRepresentative(POST_REP.getId());
        Element<Other> unrepresentedOtherWithAddress = element(
            Other.builder().name("Third Other").address(testAddress()).build());
        Element<Other> unrepresentedOtherWithoutAddress = element(Other.builder().name("Fourth Other").build());

        List<Element<Representative>> representatives = List.of(
            EMAIL_REP, EMAIL_REP2, DIGITAL_REP, POST_REP, repWithInvalidAddress, repWithInvalidEmail);

        return Stream.of(
            Arguments.of(representatives,
                List.of(respondentWithEmailRep, respondentWithDigitalRep, unrepresentedRespondent),
                List.of(),
                "First Respondent, Second Respondent"),
            Arguments.of(representatives,
                List.of(respondentWithEmailRep, respondentWithDigitalRep, unrepresentedRespondent),
                List.of(firstOther, unrepresentedOtherWithoutAddress),
                "First Respondent, Second Respondent, First Other"),
            Arguments.of(representatives,
                List.of(unrepresentedRespondentWithAddress),
                List.of(other2, unrepresentedOtherWithAddress),
                "Third Respondent, Second Other, Third Other"),
            Arguments.of(representatives,
                List.of(unrepresentedRespondent),
                List.of(firstOther, other2),
                "First Other, Second Other"),
            Arguments.of(representatives, List.of(), List.of(), ""),
            Arguments.of(representatives,
                List.of(respondentWithPostRep, respondentWithInvalidEmailRep),
                List.of(), "")
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
