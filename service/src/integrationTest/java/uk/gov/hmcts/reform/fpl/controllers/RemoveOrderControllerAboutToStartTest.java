package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.enums.HearingOrderType;
import uk.gov.hmcts.reform.fpl.enums.OtherApplicationType;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.StandardDirectionOrder;
import uk.gov.hmcts.reform.fpl.model.common.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.OtherApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrdersBundle;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static com.google.common.collect.Lists.newArrayList;
import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.APPROVED;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.DRAFT;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.SEND_TO_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.HearingOrderType.AGREED_CMO;
import static uk.gov.hmcts.reform.fpl.enums.HearingOrderType.C21;
import static uk.gov.hmcts.reform.fpl.enums.HearingOrderType.DRAFT_CMO;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.SEALED;
import static uk.gov.hmcts.reform.fpl.enums.OtherApplicationType.C1_WITH_SUPPLEMENT;
import static uk.gov.hmcts.reform.fpl.enums.State.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.enums.State.CLOSED;
import static uk.gov.hmcts.reform.fpl.enums.State.FINAL_HEARING;
import static uk.gov.hmcts.reform.fpl.enums.State.GATEKEEPING;
import static uk.gov.hmcts.reform.fpl.enums.State.SUBMITTED;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@WebMvcTest(RemoveOrderController.class)
@OverrideAutoConfiguration(enabled = true)
class RemoveOrderControllerAboutToStartTest extends AbstractCallbackTest {
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

        List<Element<HearingOrder>> sealedCaseManagementOrders = List.of(
            element(buildPastHearingOrder(AGREED_CMO).toBuilder()
                .status(APPROVED)
                .dateIssued(dateNow())
                .build()));

        Element<HearingOrder> draftCMOOne = element(UUID.randomUUID(), buildPastHearingOrder(DRAFT_CMO));
        Element<HearingOrder> draftCMOTwo = element(UUID.randomUUID(), buildPastHearingOrder(DRAFT_CMO));
        Element<HearingOrder> draftCMOThree = element(UUID.randomUUID(), buildPastHearingOrder(DRAFT_CMO));
        Element<HearingOrder> agreedCMO = element(UUID.randomUUID(), buildPastHearingOrder(AGREED_CMO));
        Element<HearingOrder> draftOrderOne = element(UUID.randomUUID(), buildPastHearingOrder(C21));
        Element<HearingOrder> draftOrderTwo = element(UUID.randomUUID(), buildPastHearingOrder(C21));

        CaseData caseData = CaseData.builder()
            .state(state)
            .orderCollection(generatedOrders)
            .sealedCMOs(sealedCaseManagementOrders)
            .draftUploadedCMOs(newArrayList(draftCMOOne, draftCMOThree))
            .hearingOrdersBundlesDrafts(newArrayList(
                element(HearingOrdersBundle.builder()
                    .orders(newArrayList(draftCMOOne, draftOrderOne))
                    .build()),
                element(HearingOrdersBundle.builder()
                    .orders(newArrayList(draftCMOTwo))
                    .build()),
                element(HearingOrdersBundle.builder()
                    .orders(newArrayList(agreedCMO, draftOrderTwo))
                    .build())
            ))
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
                buildListElement(generatedOrders.get(3).getId(), "order 4 - 12 August 2022"),
                buildListElement(generatedOrders.get(4).getId(), "order 5 - 12 September 2018"),
                buildListElement(sealedCaseManagementOrders.get(0).getId(),
                    String.format("Sealed case management order issued on %s",
                        formatLocalDateToString(dateNow(), "d MMMM yyyy"))),
                buildListElement(draftCMOOne.getId(), format("Draft case management order sent on %s",
                    formatLocalDateToString(dateNow().minusDays(1), "d MMMM yyyy"))),
                buildListElement(draftOrderOne.getId(), format("Draft order sent on %s",
                    formatLocalDateToString(dateNow().minusDays(1), "d MMMM yyyy"))),
                buildListElement(draftCMOTwo.getId(), format("Draft case management order sent on %s",
                    formatLocalDateToString(dateNow().minusDays(1), "d MMMM yyyy"))),
                buildListElement(agreedCMO.getId(), format("Agreed case management order sent on %s",
                    formatLocalDateToString(dateNow().minusDays(1), "d MMMM yyyy"))),
                buildListElement(draftOrderTwo.getId(), format("Draft order sent on %s",
                    formatLocalDateToString(dateNow().minusDays(1), "d MMMM yyyy"))),
                buildListElement(draftCMOThree.getId(), format("Draft case management order sent on %s",
                    formatLocalDateToString(dateNow().minusDays(1), "d MMMM yyyy")))))
            .build();

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

    @Test
    void shouldBuildListOfApplications() {
        List<Element<AdditionalApplicationsBundle>> applications = List.of(
            element(buildCombinedApplication(C1_WITH_SUPPLEMENT, "6 May 2020")));
        CaseData caseData = CaseData.builder().additionalApplicationsBundle(applications).build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToStartEvent(asCaseDetails(caseData));
        DynamicList builtDynamicList = mapper.convertValue(
            response.getData().get("removableApplicationList"), DynamicList.class
        );

        DynamicList expectedList = DynamicList.builder()
            .value(DynamicListElement.EMPTY)
            .listItems(List.of(buildListElement(applications.get(0).getId(), "C2, C1, 6 May 2020"))).build();

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

    private HearingOrder buildPastHearingOrder(HearingOrderType type) {
        return HearingOrder.builder()
            .type(type)
            .status((type == AGREED_CMO || type == C21) ? SEND_TO_JUDGE : DRAFT)
            .dateSent(dateNow().minusDays(1))
            .dateIssued(dateNow())
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

    private AdditionalApplicationsBundle buildCombinedApplication(OtherApplicationType type, String date) {
        return AdditionalApplicationsBundle.builder()
            .uploadedDateTime(date)
            .c2DocumentBundle(C2DocumentBundle.builder()
                .uploadedDateTime(date)
                .build())
            .otherApplicationsBundle(OtherApplicationsBundle.builder()
                .applicationType(type)
                .uploadedDateTime(date)
                .build())
            .build();
    }
}
