package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.enums.HearingOrderType;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.StandardDirectionOrder;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.APPROVED;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.DRAFT;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.SEALED;
import static uk.gov.hmcts.reform.fpl.enums.State.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.enums.State.CLOSED;
import static uk.gov.hmcts.reform.fpl.enums.State.FINAL_HEARING;
import static uk.gov.hmcts.reform.fpl.enums.State.GATEKEEPING;
import static uk.gov.hmcts.reform.fpl.enums.State.SUBMITTED;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@ActiveProfiles("integration-test")
@WebMvcTest(RemoveOrderController.class)
@OverrideAutoConfiguration(enabled = true)
class RemoveOrderControllerAboutToStartTest extends AbstractControllerTest {
    private static final UUID SDO_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    RemoveOrderControllerAboutToStartTest() {
        super("remove-order");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("generateAllAvailableStatesSource")
    void shouldAddAllNonSDRemovableOrdersToCaseDataInAllExpectedStates(State state) {
        List<Element<GeneratedOrder>> generatedOrders = List.of(
            element(buildOrder("order 1", "12 March 1234", "Blank order (C21)")),
            element(buildOrder("order 2", "28 July 2020", "Blank order (C21)")),
            element(buildOrder("order 3", "29 August 2021", "Interim supervision order")),
            element(buildOrder("order 4", "12 August 2022", "Interim care order")),
            element(buildOrder("order 5", "12 September 2018", "Another Order"))
        );

        List<Element<HearingOrder>> caseManagementOrders = List.of(
            element(HearingOrder.builder()
                .type(HearingOrderType.AGREED_CMO)
                .title("Agreed CMO discussed at hearing")
                .status(APPROVED)
                .dateIssued(dateNow())
                .build()),
            element(HearingOrder.builder()
                .type(HearingOrderType.DRAFT_CMO)
                .title("Draft CMO from advocates' meeting")
                .status(DRAFT)
                .dateIssued(dateNow())
                .build())
        );

        CaseData caseData = CaseData.builder()
            .state(state)
            .orderCollection(generatedOrders)
            .sealedCMOs(caseManagementOrders)
            .build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToStartEvent(asCaseDetails(caseData));
        DynamicList builtDynamicList = mapper.convertValue(
            response.getData().get("removableOrderList"), DynamicList.class
        );

        DynamicList expectedList = DynamicList.builder()
            .value(DynamicListElement.EMPTY)
            .listItems(List.of(
                buildListElement(generatedOrders.get(0).getId(), "order 1 - 12 March 1234"),
                buildListElement(generatedOrders.get(1).getId(), "order 2 - 28 July 2020"),
                buildListElement(generatedOrders.get(2).getId(), "order 3 - 29 August 2021"),
                buildListElement(generatedOrders.get(3).getId(), "order 4 - 12 August 2022")
            )).build();

        assertThat(builtDynamicList).isEqualTo(expectedList);
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(value = State.class, names = {"SUBMITTED", "GATEKEEPING", "CASE_MANAGEMENT", "CLOSED"})
    void shouldAddAllRemovableSDOrdersToCaseDataInAllExpectedStatesBesidesFinal(State state) {
        CaseData caseData = CaseData.builder()
            .state(state)
            .standardDirectionOrder(StandardDirectionOrder.builder()
                .orderStatus(SEALED)
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToStartEvent(asCaseDetails(caseData));
        DynamicList builtDynamicList = mapper.convertValue(
            response.getData().get("removableOrderList"), DynamicList.class
        );

        DynamicList expectedList = DynamicList.builder()
            .value(DynamicListElement.EMPTY)
            .listItems(List.of(
                buildListElement(SDO_ID, format("Gatekeeping order - %s",
                    formatLocalDateToString(dateNow(), "d MMMM yyyy")))
            )).build();

        assertThat(builtDynamicList).isEqualTo(expectedList);
    }

    @Test
    void shouldNotAddAnyRemovableSDOrderTypesToCaseDataWhenCaseIsInFinalHearingState() {
        CaseData caseData = CaseData.builder()
            .state(FINAL_HEARING)
            .standardDirectionOrder(StandardDirectionOrder.builder()
                .orderStatus(SEALED)
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToStartEvent(asCaseDetails(caseData));
        DynamicList builtDynamicList = mapper.convertValue(
            response.getData().get("removableOrderList"), DynamicList.class
        );

        DynamicList expectedList = DynamicList.builder()
            .value(DynamicListElement.EMPTY)
            .listItems(List.of()).build();

        assertThat(builtDynamicList).isEqualTo(expectedList);
    }

    private DynamicListElement buildListElement(UUID id, String label) {
        return DynamicListElement.builder()
            .code(id)
            .label(label)
            .build();
    }

    private GeneratedOrder buildOrder(String title, String date, String type) {
        return GeneratedOrder.builder()
            .type(type)
            .title(title)
            .dateOfIssue(date)
            .build();
    }

    private static Stream<Arguments> generateAllAvailableStatesSource() {
        return Stream.of(
            Arguments.of(SUBMITTED),
            Arguments.of(GATEKEEPING),
            Arguments.of(CASE_MANAGEMENT),
            Arguments.of(FINAL_HEARING),
            Arguments.of(CLOSED));
    }
}
