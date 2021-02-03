package uk.gov.hmcts.reform.fpl.service.email.content;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.notify.cmo.RejectedOrdersTemplate;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.service.CaseConverter;
import uk.gov.hmcts.reform.fpl.service.email.content.cmo.ReviewDraftOrdersEmailContentProvider;
import uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.enums.TabUrlAnchor.ORDERS;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createRespondents;

@ContextConfiguration(classes = {ReviewDraftOrdersEmailContentProvider.class, EmailNotificationHelper.class,
    FixedTimeConfiguration.class, CaseConverter.class})
class ReviewDraftOrdersEmailContentProviderTest extends AbstractEmailContentProviderTest {

    @Autowired
    private ReviewDraftOrdersEmailContentProvider reviewDraftOrdersEmailContentProvider;

    private static final LocalDate SOME_DATE = LocalDate.of(2020, 2, 20);

    @Test
    void shouldBuildCMORejectedByJudgeNotificationExpectedParameters() {
        HearingBooking hearing = HearingBooking.builder()
            .type(CASE_MANAGEMENT)
            .startDate(LocalDateTime.of(SOME_DATE, LocalTime.of(0, 0)))
            .build();

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .familyManCaseNumber("SN2000")
            .respondents1(createRespondents())
            .build();

        List<HearingOrder> orders = List.of(HearingOrder.builder()
                .title("Order 1")
                .requestedChanges("Missing information about XYZ")
                .build(),
            HearingOrder.builder()
                .title("Order 2")
                .requestedChanges("Please change ABC")
                .build());

        RejectedOrdersTemplate expectedTemplate = RejectedOrdersTemplate.builder()
            .ordersAndRequestedChanges(List.of(
                "Order 1 - Missing information about XYZ",
                "Order 2 - Please change ABC"))
            .caseUrl(caseUrl(CASE_REFERENCE, ORDERS))
            .respondentLastName("Jones")
            .subjectLineWithHearingDate("Jones, SN2000, case management hearing, 20 February 2020")
            .build();

        assertThat(reviewDraftOrdersEmailContentProvider.buildOrdersRejectedContent(caseData, hearing, orders))
            .isEqualTo(expectedTemplate);
    }

}
