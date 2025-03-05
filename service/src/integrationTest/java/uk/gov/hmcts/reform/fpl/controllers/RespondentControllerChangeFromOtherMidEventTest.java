package uk.gov.hmcts.reform.fpl.controllers;

import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.IsAddressKnowType;
import uk.gov.hmcts.reform.fpl.enums.UserRole;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.Telephone;
import uk.gov.hmcts.reform.fpl.model.event.OtherToRespondentEventData;
import uk.gov.hmcts.reform.fpl.request.RequestData;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.controllers.ChangeFromOtherUtils.localAuthorities;
import static uk.gov.hmcts.reform.fpl.controllers.ChangeFromOtherUtils.otherToRespondentEventData;
import static uk.gov.hmcts.reform.fpl.controllers.ChangeFromOtherUtils.prepareConfidentialOthers;
import static uk.gov.hmcts.reform.fpl.controllers.ChangeFromOtherUtils.prepareConfidentialRespondentsFromRespondents1;
import static uk.gov.hmcts.reform.fpl.controllers.ChangeFromOtherUtils.prepareOthers;
import static uk.gov.hmcts.reform.fpl.controllers.ChangeFromOtherUtils.prepareRespondentsTestingData;
import static uk.gov.hmcts.reform.fpl.controllers.ChangeFromOtherUtils.prepareTransformedRespondentTestingData;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeRole.REPRESENTING_OTHER_PERSON_1;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeRole.REPRESENTING_PERSON_1;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.buildDynamicListFromOthers;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createOthers;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElementsWithRandomUUID;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElementsWithUUIDs;

@WebMvcTest(RespondentController.class)
@OverrideAutoConfiguration(enabled = true)
class RespondentControllerChangeFromOtherMidEventTest extends AbstractCallbackTest {

    @MockBean
    private RequestData requestData;
    private static final String CONFIRM_IF_LEGAL_REP_ERROR = "Confirm if respondent has legal representation";
    private static final String DOB_ERROR = "Date of birth for respondent cannot be in the future";
    private static final String MAX_RESPONDENTS_ERROR = "Maximum number of respondents is 10";

    RespondentControllerChangeFromOtherMidEventTest() {
        super("enter-respondents/change-from-other");
    }

    @BeforeEach
    void before() {
        given(requestData.userRoles()).willReturn(Set.of(UserRole.HMCTS_ADMIN.getRoleName()));
    }

    private Respondent respondent(LocalDate dateOfBirth) {
        return respondent(dateOfBirth, false);
    }

    private Respondent respondent(LocalDate dateOfBirth, boolean isLegalRepAnswered) {
        return Respondent.builder()
            .party(RespondentParty.builder()
                .dateOfBirth(dateOfBirth)
                .build())
            .legalRepresentation(isLegalRepAnswered ? "No" : null)
            .build();
    }

    private static Stream<Arguments> shouldPopulateTransformedRespondentSource() {
        return Stream.of(
            Arguments.of(0, Respondent.builder()
                .party(RespondentParty.builder()
                    .address(Address.builder()
                        .addressLine1("1 Some street")
                        .addressLine2("Some road")
                        .postTown("some town")
                        .postcode("BT66 7RR")
                        .county("Some county")
                        .country("UK")
                        .build())
                    .addressKnow(IsAddressKnowType.YES)
                    .dateOfBirth(LocalDate.of(2005, Month.JUNE, 4))
                    .firstName("Kyle Stafford")
                    .relationshipToChild("Child suffers from ADD")
                    .telephoneNumber(Telephone.builder()
                        .telephoneNumber("02838882404")
                        .build())
                    .build())
                .build()),
            Arguments.of(1, Respondent.builder()
                .party(RespondentParty.builder()
                    .address(Address.builder()
                        .addressLine1("1 Some street")
                        .addressLine2("Some road")
                        .postTown("some town")
                        .postcode("BT66 7RR")
                        .county("Some county")
                        .country("UK")
                        .build())
                    .addressKnow(IsAddressKnowType.YES)
                    .dateOfBirth(LocalDate.of(2002, Month.FEBRUARY, 5))
                    .firstName("Sarah Simpson")
                    .telephoneNumber(Telephone.builder()
                        .telephoneNumber("02838882404")
                        .build())
                    .build())
                .build())
        );
    }

    @ParameterizedTest
    @MethodSource("shouldPopulateTransformedRespondentSource")
    void shouldPopulateTransformedRespondent(int selected, Respondent expectedRespondent) {
        UUID otherPerson1Uuid = randomUUID();
        List<Element<Other>> others = createOthers(otherPerson1Uuid);
        CaseDetails caseDetails = CaseDetails.builder()
            .id(RandomUtils.nextLong())
            .data(Map.of(
                "othersList", buildDynamicListFromOthers(others, selected),
                "othersV2", others))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails,"enter-respondent");

        assertThat(callbackResponse.getData()).containsKey("transformedRespondent");
        CaseData responseCaseData = extractCaseData(callbackResponse);
        assertThat(responseCaseData.getOtherToRespondentEventData().getTransformedRespondent().getParty())
            .isEqualTo(expectedRespondent.getParty());
    }

    @ParameterizedTest
    @MethodSource("shouldPopulateTransformedRespondentSource")
    void shouldPopulateTransformedRespondentWithRepresentative(int selected, Respondent expectedRespondent) {
        UUID otherPerson1UUID = randomUUID();
        UUID representativeUUID = randomUUID();

        List<Element<Other>> others = createOthers(otherPerson1UUID);
        others.get(selected).getValue().addRepresentative(representativeUUID);

        CaseDetails caseDetails = CaseDetails.builder()
            .id(RandomUtils.nextLong())
            .data(Map.of(
                "representatives", List.of(element(representativeUUID, Representative.builder()
                    .role(selected == 0 ? REPRESENTING_PERSON_1 : REPRESENTING_OTHER_PERSON_1)
                    .build())),
                "othersList", buildDynamicListFromOthers(others, selected),
                "othersV2", others))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails,"enter-respondent");

        assertThat(callbackResponse.getData()).containsKey("transformedRespondent");
        CaseData responseCaseData = extractCaseData(callbackResponse);
        expectedRespondent.addRepresentative(representativeUUID);
        Respondent transformedRespondent = responseCaseData.getOtherToRespondentEventData().getTransformedRespondent();
        assertThat(transformedRespondent.getParty()).isEqualTo(expectedRespondent.getParty());
        assertThat(transformedRespondent.getRepresentedBy()).hasSize(1);
        assertThat(unwrapElements(transformedRespondent.getRepresentedBy())).isEqualTo(List.of(representativeUUID));
    }

    @Test
    void shouldNotReturnErrorWhenExistingRespondentHavingConfidentialDetails() {
        List<Element<Other>> others = prepareOthers(1, null);

        List<Respondent> respondents = prepareRespondentsTestingData(1, true);
        Respondent transformedRespondent = prepareTransformedRespondentTestingData(true);

        List<Element<Respondent>> respondents1 = wrapElementsWithUUIDs(respondents);

        CaseData caseData = CaseData.builder()
            .confidentialRespondents(prepareConfidentialRespondentsFromRespondents1(respondents1))
            .confidentialOthers(prepareConfidentialOthers(others))
            .localAuthorities(localAuthorities())
            .respondents1(respondents1)
            .othersV2(others)
            .otherToRespondentEventData(otherToRespondentEventData(transformedRespondent, others, 0))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData);
        assertThat(callbackResponse.getErrors()).isEmpty();
    }

    @Test
    void shouldReturnMaximumRespondentErrorsWhenNumberOfRespondentsExceeds10() {
        CaseData caseData = CaseData.builder()
            .respondents1(wrapElements(
                respondent(dateNow()), respondent(dateNow()), respondent(dateNow()), respondent(dateNow()),
                respondent(dateNow()), respondent(dateNow()), respondent(dateNow()), respondent(dateNow()),
                respondent(dateNow()), respondent(dateNow())))
            .otherToRespondentEventData(OtherToRespondentEventData.builder()
                .transformedRespondent(respondent(dateNow()))
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData);
        assertThat(callbackResponse.getErrors()).contains(MAX_RESPONDENTS_ERROR);
    }

    @Test
    void shouldReturnDateOfBirthErrorsForRespondentWhenFutureDateOfBirth() {
        CaseData caseData = CaseData.builder()
            .respondents1(wrapElements(respondent(dateNow())))
            .otherToRespondentEventData(OtherToRespondentEventData.builder()
                .transformedRespondent(respondent(dateNow().plusDays(1)))
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData);
        assertThat(callbackResponse.getErrors()).contains(DOB_ERROR);
    }

    @Test
    void shouldReturnNoDateOfBirthErrorsForRespondentWhenValidDateOfBirth() {
        CaseData caseData = CaseData.builder()
            .respondents1(wrapElements(respondent(dateNow())))
            .otherToRespondentEventData(OtherToRespondentEventData.builder()
                .transformedRespondent(respondent(dateNow().minusDays(1)))
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData);
        assertThat(callbackResponse.getErrors()).doesNotContain(DOB_ERROR);
    }

    @Test
    void shouldReturnErrorWhenLegalRepNotAnswered() {
        CaseData caseData = CaseData.builder()
            .respondents1(wrapElementsWithRandomUUID(respondent(dateNow(), true)))
            .otherToRespondentEventData(OtherToRespondentEventData.builder()
                .transformedRespondent(respondent(dateNow().minusDays(1), false))
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData);
        assertThat(callbackResponse.getErrors()).contains(CONFIRM_IF_LEGAL_REP_ERROR);
    }

    @Test
    void shouldNotReturnErrorWhenLegalRepAnswered() {
        CaseData caseData = CaseData.builder()
            .respondents1(wrapElementsWithRandomUUID(respondent(dateNow(), true)))
            .otherToRespondentEventData(OtherToRespondentEventData.builder()
                .transformedRespondent(respondent(dateNow().minusDays(1), true))
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData);
        assertThat(callbackResponse.getErrors()).doesNotContain(CONFIRM_IF_LEGAL_REP_ERROR);
    }
}
