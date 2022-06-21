package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.enums.UserRole;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.LocalAuthority;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.Others;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.Telephone;
import uk.gov.hmcts.reform.fpl.model.event.OtherToRespondentEventData;
import uk.gov.hmcts.reform.fpl.request.RequestData;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_INBOX;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_NAME;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.buildDynamicListFromOthers;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElementsWithRandomUUID;

@WebMvcTest(RespondentController.class)
@OverrideAutoConfiguration(enabled = true)
class RespondentControllerChangeFromOtherAboutToSubmitTest extends AbstractCallbackTest {

    @MockBean
    private RequestData requestData;

    RespondentControllerChangeFromOtherAboutToSubmitTest() {
        super("enter-respondents/change-from-other");
    }

    @BeforeEach
    void before() {
        given(requestData.userRoles()).willReturn(Set.of(UserRole.HMCTS_ADMIN.getRoleName()));
    }

    private static Respondent respondent(LocalDate dateOfBirth) {
        return respondent(dateOfBirth, true);
    }

    private static Respondent respondent(LocalDate dateOfBirth, boolean isLegalRepAnswered) {
        return Respondent.builder()
            .party(RespondentParty.builder()
                .dateOfBirth(dateOfBirth)
                .build())
            .legalRepresentation(isLegalRepAnswered ? "No" : null)
            .build();
    }

    private static Stream<Arguments> shouldTransformedOtherToRespondentSource() {
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
                    .addressKnow("Yes")
                    .dateOfBirth(LocalDate.of(2005, Month.JUNE, 4))
                    .firstName("Kyle Stafford")
                    .placeOfBirth("Newry")
                    .gender("Male")
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
                    .addressKnow("Yes")
                    .dateOfBirth(LocalDate.of(2002, Month.FEBRUARY, 5))
                    .firstName("Sarah Simpson")
                    .placeOfBirth("Craigavon")
                    .gender("Female")
                    .telephoneNumber(Telephone.builder()
                        .telephoneNumber("02838882404")
                        .build())
                    .build())
                .build())
        );
    }

    @Test
    void shouldDecreaseOtherCountAndIncreaseRespondentsCount() {
        Other targetOther = Other.builder().name("Marco One").build();
        Others others = Others.builder()
            .firstOther(targetOther)
            .build();
        CaseData caseData = CaseData.builder()
            .localAuthorities(wrapElements(LocalAuthority.builder()
                .name(LOCAL_AUTHORITY_1_NAME)
                .email(LOCAL_AUTHORITY_1_INBOX)
                .designated(YesNo.YES.getValue())
                .build()))
            .others(others)
            .respondents1(wrapElementsWithRandomUUID(respondent(dateNow())))
            .otherToRespondentEventData(OtherToRespondentEventData.builder()
                .transformedRespondent(Respondent.builder()
                    .party(RespondentParty.builder()
                        .firstName("Marco One")
                        .build())
                    .legalRepresentation("No")
                    .build())
                .othersList(buildDynamicListFromOthers(others, 0))
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(caseData);

        CaseData responseCaseData = extractCaseData(callbackResponse);
        assertThat(responseCaseData.getAllOthers()).hasSize(0);
        assertThat(responseCaseData.getAllRespondents()).hasSize(2);
    }

    @Test
    void shouldConvertSoleOtherPersonToRespondent() {
        Others others = Others.builder()
            .firstOther(Other.builder().name("Marco One").build())
            .build();
        CaseData caseData = CaseData.builder()
            .localAuthorities(wrapElements(LocalAuthority.builder()
                .name(LOCAL_AUTHORITY_1_NAME)
                .email(LOCAL_AUTHORITY_1_INBOX)
                .designated(YesNo.YES.getValue())
                .build()))
            .others(others)
            .respondents1(wrapElementsWithRandomUUID(respondent(dateNow())))
            .otherToRespondentEventData(OtherToRespondentEventData.builder()
                .transformedRespondent(Respondent.builder()
                    .party(RespondentParty.builder()
                        .firstName("Marco One")
                        .build())
                    .legalRepresentation("No")
                    .build())
                .othersList(buildDynamicListFromOthers(others, 0))
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(caseData);

        CaseData responseCaseData = extractCaseData(callbackResponse);
        assertThat(responseCaseData.getAllOthers()).hasSize(0);
        assertThat(responseCaseData.getAllRespondents()).hasSize(2);
        assertThat(responseCaseData.findRespondent(1)).contains(Respondent.builder()
            .party(RespondentParty.builder()
                .firstName("Marco One")
                .build())
            .legalRepresentation("No")
            .build());
    }

    public static Stream<Arguments> convertAdditionalOthersToRespondentParam() {
        Stream.Builder<Arguments> builder = Stream.builder();
        for (int i = 1; i < 10; i++) { // selectedAdditionalOtherSeq
            for (int j = 1; j < 9; j++) { // numberOfAdditionalOther
                for (int k = 1; k < 9; k++) { // numberOfRespondent
                    if (i <= j) {
                        builder.add(Arguments.of(i, j, k));
                    }
                }
            }
        }
        return builder.build();
    }

    @ParameterizedTest
    @MethodSource("convertAdditionalOthersToRespondentParam")
    void shouldConvertAdditionalOthersToRespondent(int selectedAdditionalOtherSeq, int numberOfAdditionalOther,
                                                   int numberOfRespondent) {
        List<Element<Other>> additionalOthers = new ArrayList<>();
        for (int i = 0; i < numberOfAdditionalOther; i++) {
            additionalOthers.add(element(Other.builder().name(String.format("Marco %s", i + 1)).build()));
        }
        Others others = Others.builder()
            .firstOther(Other.builder().name("Marco One").build())
            .additionalOthers(additionalOthers)
            .build();

        List<Respondent> respondents = new ArrayList<>();
        for (int j = 0; j < numberOfRespondent; j++) {
            respondents.add(respondent(dateNow()));
        }

        CaseData caseData = CaseData.builder()
            .localAuthorities(wrapElements(LocalAuthority.builder()
                .name(LOCAL_AUTHORITY_1_NAME)
                .email(LOCAL_AUTHORITY_1_INBOX)
                .designated(YesNo.YES.getValue())
                .build()))
            .others(others)
            .respondents1(wrapElementsWithRandomUUID(respondents))
            .otherToRespondentEventData(OtherToRespondentEventData.builder()
                .transformedRespondent(Respondent.builder()
                    .party(RespondentParty.builder()
                        .firstName(String.format("Marco %s", selectedAdditionalOtherSeq + 2))
                        .build())
                    .legalRepresentation("No")
                    .build())
                .othersList(buildDynamicListFromOthers(others, selectedAdditionalOtherSeq))
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(caseData);

        CaseData responseCaseData = extractCaseData(callbackResponse);
        assertThat(responseCaseData.getAllOthers()).hasSize(numberOfAdditionalOther);
        assertThat(responseCaseData.getAllRespondents()).hasSize(numberOfRespondent + 1);
        assertThat(responseCaseData.findRespondent(numberOfRespondent)).contains(Respondent.builder()
            .party(RespondentParty.builder()
                .firstName(String.format("Marco %s", selectedAdditionalOtherSeq + 2))
                .build())
            .legalRepresentation("No")
            .build());
    }

}
