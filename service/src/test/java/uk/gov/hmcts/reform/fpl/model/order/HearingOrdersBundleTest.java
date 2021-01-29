package uk.gov.hmcts.reform.fpl.model.order;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.enums.HearingOrderType;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;
import uk.gov.hmcts.reform.fpl.utils.TestDataHelper;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.UUID;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.HearingOrderType.AGREED_CMO;
import static uk.gov.hmcts.reform.fpl.enums.HearingOrderType.C21;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HER_HONOUR_JUDGE;

class HearingOrdersBundleTest {

    @Nested
    class UpdateHearing {

        final LocalDateTime hearingDate = LocalDateTime.of(2021, Month.JANUARY, 1, 1, 0, 0);

        @Test
        void shouldAddHearingInformationIfNotPresent() {
            HearingOrdersBundle originalOrdersBundle = HearingOrdersBundle.builder().build();

            UUID hearingId = UUID.randomUUID();

            HearingBooking hearing = HearingBooking.builder()
                .startDate(hearingDate)
                .type(CASE_MANAGEMENT)
                .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                    .judgeLastName("Smith")
                    .judgeTitle(HER_HONOUR_JUDGE)
                    .build())
                .build();

            originalOrdersBundle.updateHearing(hearingId, hearing);

            HearingOrdersBundle expectedOrdersBundle = originalOrdersBundle.toBuilder()
                .hearingId(hearingId)
                .hearingName("Case management hearing, 1 January 2021")
                .judgeTitleAndName("Her Honour Judge Smith")
                .build();

            assertThat(originalOrdersBundle).isEqualTo(expectedOrdersBundle);
        }

        @Test
        void shouldUpdateExistingHearingInformation() {
            HearingOrdersBundle originalOrdersBundle = HearingOrdersBundle.builder()
                .hearingId(UUID.randomUUID())
                .hearingName("Test hearing")
                .judgeTitleAndName("Test judge")
                .build();

            UUID hearingId = UUID.randomUUID();

            HearingBooking hearing = HearingBooking.builder()
                .startDate(hearingDate)
                .type(CASE_MANAGEMENT)
                .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                    .judgeLastName("Smith")
                    .judgeTitle(HER_HONOUR_JUDGE)
                    .build())
                .build();

            originalOrdersBundle.updateHearing(hearingId, hearing);

            HearingOrdersBundle expectedOrdersBundle = originalOrdersBundle.toBuilder()
                .hearingId(hearingId)
                .hearingName("Case management hearing, 1 January 2021")
                .judgeTitleAndName("Her Honour Judge Smith")
                .build();

            assertThat(originalOrdersBundle).isEqualTo(expectedOrdersBundle);
        }

        @Test
        void shouldUpdatePartiallyHearingInformation() {
            HearingOrdersBundle originalOrdersBundle = HearingOrdersBundle.builder()
                .hearingId(UUID.randomUUID())
                .hearingName("Test hearing")
                .judgeTitleAndName("Test judge")
                .build();

            UUID hearingId = UUID.randomUUID();

            HearingBooking hearing = HearingBooking.builder()
                .startDate(hearingDate)
                .type(CASE_MANAGEMENT)
                .build();

            originalOrdersBundle.updateHearing(hearingId, hearing);

            HearingOrdersBundle expectedOrdersBundle = originalOrdersBundle.toBuilder()
                .hearingId(hearingId)
                .hearingName("Case management hearing, 1 January 2021")
                .judgeTitleAndName("")
                .build();

            assertThat(originalOrdersBundle).isEqualTo(expectedOrdersBundle);
        }

        @Test
        void shouldRemoveHearingInformation() {
            HearingOrdersBundle originalOrdersBundle = HearingOrdersBundle.builder()
                .hearingId(UUID.randomUUID())
                .hearingName("Test hearing")
                .judgeTitleAndName("Test judge")
                .build();

            originalOrdersBundle.updateHearing(null, null);

            HearingOrdersBundle expectedOrdersBundle = originalOrdersBundle.toBuilder()
                .hearingId(null)
                .hearingName("No hearing")
                .judgeTitleAndName(null)
                .build();

            assertThat(originalOrdersBundle).isEqualTo(expectedOrdersBundle);
        }
    }

    @Nested
    class UpdateOrders {

        @Test
        void shouldAddHearingInformationIfNotPresent() {
            HearingOrdersBundle originalOrdersBundle = HearingOrdersBundle.builder().build();

            List<Element<HearingOrder>> hearingOrders = newArrayList(hearingOrder(C21), hearingOrder(C21));

            originalOrdersBundle.updateOrders(hearingOrders, C21);

            HearingOrdersBundle expectedOrdersBundle = originalOrdersBundle.toBuilder()
                .orders(hearingOrders)
                .build();

            assertThat(originalOrdersBundle).isEqualTo(expectedOrdersBundle);
        }

        @Test
        void shouldReplaceC21() {
            Element<HearingOrder> originalHearingOrder1 = hearingOrder(AGREED_CMO);
            Element<HearingOrder> originalHearingOrder2 = hearingOrder(C21);
            Element<HearingOrder> newHearingOrder1 = hearingOrder(C21);
            Element<HearingOrder> newHearingOrder2 = hearingOrder(C21);

            HearingOrdersBundle originalOrdersBundle = HearingOrdersBundle.builder()
                .orders(newArrayList(originalHearingOrder1, originalHearingOrder2))
                .build();

            List<Element<HearingOrder>> hearingOrders = newArrayList(newHearingOrder1, newHearingOrder2);

            originalOrdersBundle.updateOrders(hearingOrders, C21);

            HearingOrdersBundle expectedOrdersBundle = originalOrdersBundle.toBuilder()
                .orders(newArrayList(originalHearingOrder1, newHearingOrder1, newHearingOrder2))
                .build();

            assertThat(originalOrdersBundle).isEqualTo(expectedOrdersBundle);
        }

        @Test
        void shouldReplaceCmo() {
            Element<HearingOrder> originalHearingOrder1 = hearingOrder(AGREED_CMO);
            Element<HearingOrder> originalHearingOrder2 = hearingOrder(C21);
            Element<HearingOrder> newHearingOrder1 = hearingOrder(AGREED_CMO);

            HearingOrdersBundle originalOrdersBundle = HearingOrdersBundle.builder()
                .orders(newArrayList(originalHearingOrder1, originalHearingOrder2))
                .build();

            List<Element<HearingOrder>> hearingOrders = newArrayList(newHearingOrder1);

            originalOrdersBundle.updateOrders(hearingOrders, AGREED_CMO);

            HearingOrdersBundle expectedOrdersBundle = originalOrdersBundle.toBuilder()
                .orders(newArrayList(newHearingOrder1, originalHearingOrder2))
                .build();

            assertThat(originalOrdersBundle).isEqualTo(expectedOrdersBundle);
        }

        private Element<HearingOrder> hearingOrder(HearingOrderType hearingOrderType) {
            return ElementUtils.element(HearingOrder.builder()
                .type(hearingOrderType)
                .order(TestDataHelper.testDocumentReference())
                .build());
        }
    }
}
