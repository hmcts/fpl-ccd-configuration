package uk.gov.hmcts.reform.fpl.service.email.content.cmo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.fpl.enums.TabUrlAnchor;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.notify.cmo.DraftOrdersUploadedTemplate;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.service.email.content.AbstractEmailContentProviderTest;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HER_HONOUR_JUDGE;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ContextConfiguration(classes = {DraftOrdersUploadedContentProvider.class})
class DraftOrdersUploadedContentProviderTest extends AbstractEmailContentProviderTest {

    private static final Long CASE_NUMBER = 12345L;

    @Autowired
    private DraftOrdersUploadedContentProvider contentProvider;

    private final CaseData caseData = CaseData.builder()
        .respondents1(wrapElements(
            Respondent.builder()
                .party(RespondentParty.builder()
                    .lastName("White")
                    .build())
                .build()))
        .familyManCaseNumber("FMN")
        .id(CASE_NUMBER)
        .build();

    private final JudgeAndLegalAdvisor judge = JudgeAndLegalAdvisor.builder()
        .judgeTitle(HER_HONOUR_JUDGE)
        .judgeLastName("Black")
        .build();

    @Test
    void shouldCreateCustomizationWithHearing() {

        final HearingBooking hearing = HearingBooking.builder()
            .type(CASE_MANAGEMENT)
            .startDate(LocalDateTime.of(2020, Month.FEBRUARY, 20, 0, 0, 0))
            .judgeAndLegalAdvisor(judge)
            .build();

        final List<HearingOrder> orders = orders("order 1", "order 2");

        DraftOrdersUploadedTemplate customization = contentProvider.buildContent(caseData, hearing, judge, orders);

        assertThat(customization.getJudgeName()).isEqualTo("Black");
        assertThat(customization.getJudgeTitle()).isEqualTo("Her Honour Judge");
        assertThat(customization.getRespondentLastName()).isEqualTo("White");
        assertThat(customization.getCaseUrl()).isEqualTo(caseUrl(CASE_NUMBER.toString(), TabUrlAnchor.DRAFT_ORDERS));
        assertThat(customization.getDraftOrders()).isEqualTo("order 1\norder 2");
        assertThat(customization.getSubjectLineWithHearingDate())
            .isEqualTo("White, FMN, case management hearing, 20 February 2020");
    }

    @Test
    void shouldCreateCustomizationWithoutHearing() {
        final List<HearingOrder> orders = orders("order 1");

        DraftOrdersUploadedTemplate customization = contentProvider.buildContent(caseData, null, judge, orders);

        assertThat(customization.getJudgeName()).isEqualTo("Black");
        assertThat(customization.getJudgeTitle()).isEqualTo("Her Honour Judge");
        assertThat(customization.getRespondentLastName()).isEqualTo("White");
        assertThat(customization.getCaseUrl()).isEqualTo(caseUrl(CASE_NUMBER.toString(), TabUrlAnchor.DRAFT_ORDERS));
        assertThat(customization.getDraftOrders()).isEqualTo("order 1");
        assertThat(customization.getSubjectLineWithHearingDate()).isEqualTo("White, FMN");
    }

    private List<HearingOrder> orders(String... titles) {
        return Stream.of(titles)
            .map(title -> HearingOrder.builder().title(title).build())
            .collect(Collectors.toList());
    }
}
