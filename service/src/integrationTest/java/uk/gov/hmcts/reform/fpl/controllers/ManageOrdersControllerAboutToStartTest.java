package uk.gov.hmcts.reform.fpl.controllers;

import org.apache.commons.lang3.tuple.Pair;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.fpl.enums.CMOStatus;
import uk.gov.hmcts.reform.fpl.enums.OrderStatus;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.StandardDirectionOrder;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.model.order.OrderTempQuestions;
import uk.gov.hmcts.reform.fpl.model.order.UrgentHearingOrder;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static uk.gov.hmcts.reform.fpl.enums.State.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.assertions.DynamicListAssert.assertThat;

@WebMvcTest(ManageOrdersController.class)
@OverrideAutoConfiguration(enabled = true)
class ManageOrdersControllerAboutToStartTest extends AbstractCallbackTest {

    ManageOrdersControllerAboutToStartTest() {
        super("manage-orders");
    }

    @Test
    void shouldPopulateManageOrdersAmendmentList() {
        UUID orderId = UUID.randomUUID();
        UUID cmoId = UUID.randomUUID();
        UUID uhoId = UUID.fromString("5d05d011-5d01-5d01-5d01-5d05d05d05d0");
        UUID sdoId = UUID.fromString("11111111-1111-1111-1111-111111111111");

        CaseData caseData = CaseData.builder()
            .state(CASE_MANAGEMENT)
            .urgentHearingOrder(UrgentHearingOrder.builder().dateAdded(LocalDate.of(4, 4, 4)).build())
            .standardDirectionOrder(StandardDirectionOrder.builder()
                .dateOfUpload(LocalDate.of(2, 2, 2))
                .orderStatus(OrderStatus.SEALED)
                .build())
            .orderCollection(List.of(
                element(orderId, GeneratedOrder.builder()
                    .dateTimeIssued(LocalDateTime.of(1, 1, 1, 1, 1, 1))
                    .approvalDate(LocalDate.of(1, 1, 1))
                    .type("some type of order")
                    .build()
                )
            ))
            .sealedCMOs(List.of(
                element(cmoId, HearingOrder.builder()
                    .status(CMOStatus.APPROVED)
                    .dateIssued(LocalDate.of(3, 3, 3))
                    .build()
                )
            ))
            .build();

        CaseData responseData = extractCaseData(postAboutToStartEvent(asCaseDetails(caseData)));
        assertThat(responseData.getManageOrdersEventData().getManageOrdersAmendmentList())
            .hasSize(4)
            .hasNoSelectedValue()
            .hasElementsInOrder(
                Pair.of(uhoId, "Urgent hearing order - 4 April 0004"),
                Pair.of(cmoId, "Sealed case management order issued on 3 March 0003"),
                Pair.of(sdoId, "Gatekeeping order - 2 February 0002"),
                Pair.of(orderId, "some type of order - 1 January 0001")
            );
    }

    @Test
    void shouldRemoveTemporaryFields() {
        CaseData caseData = CaseData.builder()
            .manageOrdersEventData(
                ManageOrdersEventData.builder()
                    .orderTempQuestions(
                        OrderTempQuestions.builder()
                            .manageOrdersExclusionRequirementDetails("NO")
                            .manageOrdersVaryOrExtendSupervisionOrder("NO")
                            .manageOrdersExpiryDateWithEndOfProceedings("NO")
                            .build()
                    ).build()
            ).build();

        CaseData responseData = extractCaseData(postAboutToStartEvent(asCaseDetails(caseData)));

        Assertions.assertThat(responseData.getManageOrdersEventData().getOrderTempQuestions()).isNull();
    }
}
