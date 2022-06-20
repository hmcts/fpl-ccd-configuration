package uk.gov.hmcts.reform.fpl.controllers;

import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.Telephone;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.model.event.OtherToRespondentEventData;

import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createOthers;

@WebMvcTest(RespondentController.class)
@OverrideAutoConfiguration(enabled = true)
class RespondentControllerChangeFromOtherMidEventTest extends AbstractCallbackTest {

    RespondentControllerChangeFromOtherMidEventTest() {
        super("enter-respondents/change-from-other");
    }

    private DynamicList buildDynamicList(UUID otherPerson1Uuid, int selected) {
        List<DynamicListElement> listItems = List.of(
            DynamicListElement.builder().code(randomUUID()).label("Kyle Stafford").build(),
            DynamicListElement.builder().code(otherPerson1Uuid).label("Sarah Simpson").build()
        );
        return DynamicList.builder()
            .listItems(listItems)
            .value(listItems.get(selected))
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

    @ParameterizedTest
    @MethodSource("shouldPopulateTransformedRespondentSource")
    void shouldPopulateTransformedRespondent(int selected, Respondent expectedRespondent) {
        UUID otherPerson1Uuid = randomUUID();
        CaseDetails caseDetails = CaseDetails.builder()
            .id(RandomUtils.nextLong())
            .data(Map.of(
                "othersList", buildDynamicList(otherPerson1Uuid, selected),
                "others", createOthers(otherPerson1Uuid)))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails,"enter-respondent");

        assertThat(callbackResponse.getData()).containsKey("transformedRespondent");
        CaseData responseCaseData = extractCaseData(callbackResponse);
        assertThat(responseCaseData.getOtherToRespondentEventData().getTransformedRespondent()).isEqualTo(
            expectedRespondent);
    }
}
