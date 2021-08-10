package uk.gov.hmcts.reform.fpl.service.email.content.cmo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle;
import uk.gov.hmcts.reform.fpl.enums.TabUrlAnchor;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.notify.cmo.DraftOrdersUploadedTemplate;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.service.email.content.AbstractEmailContentProviderTest;
import uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HER_HONOUR_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HIS_HONOUR_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.MAGISTRATES;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ContextConfiguration(classes = {DraftOrdersUploadedContentProvider.class})
class DraftOrdersUploadedContentProviderTest extends AbstractEmailContentProviderTest {

    private static final Long CASE_NUMBER = 12345L;
    private static CaseData caseData = CaseData.builder()
        .children1(wrapElements(mock(Child.class)))
        .respondents1(wrapElements(Respondent.builder()
            .party(RespondentParty.builder().lastName("White").build())
            .build()))
        .familyManCaseNumber("FMN")
        .id(CASE_NUMBER)
        .build();

    @MockBean
    private EmailNotificationHelper helper;
    @Autowired
    private DraftOrdersUploadedContentProvider underTest;

    @BeforeEach
    void setUp() {
        when(helper.getEldestChildLastName(caseData.getAllChildren())).thenReturn("White");
    }

    @Test
    void shouldCreateCustomizationWithHearing() {
        JudgeAndLegalAdvisor judge = judge(HER_HONOUR_JUDGE, "Black");
        List<HearingOrder> orders = orders("order 1", "order 2");
        HearingBooking hearing = HearingBooking.builder()
            .type(CASE_MANAGEMENT)
            .startDate(LocalDateTime.of(2020, Month.FEBRUARY, 20, 0, 0, 0))
            .judgeAndLegalAdvisor(judge)
            .build();


        DraftOrdersUploadedTemplate customization = underTest.buildContent(caseData, hearing, judge, orders);

        DraftOrdersUploadedTemplate expected = DraftOrdersUploadedTemplate.builder()
            .judgeName("Black")
            .judgeTitle("Her Honour Judge")
            .lastName("White")
            .caseUrl(caseUrl(CASE_NUMBER.toString(), TabUrlAnchor.DRAFT_ORDERS))
            .draftOrders("order 1\norder 2")
            .subjectLineWithHearingDate("White, FMN, case management hearing, 20 February 2020")
            .build();

        assertThat(customization).isEqualTo(expected);
    }

    @Test
    void shouldCreateCustomizationWithoutHearing() {
        List<HearingOrder> orders = orders("order 1");
        JudgeAndLegalAdvisor judge = judge(HIS_HONOUR_JUDGE, "White");

        DraftOrdersUploadedTemplate customization = underTest.buildContent(caseData, null, judge, orders);

        DraftOrdersUploadedTemplate expected = DraftOrdersUploadedTemplate.builder()
            .judgeName("White")
            .judgeTitle("His Honour Judge")
            .lastName("White")
            .caseUrl(caseUrl(CASE_NUMBER.toString(), TabUrlAnchor.DRAFT_ORDERS))
            .draftOrders("order 1")
            .subjectLineWithHearingDate("White, FMN")
            .build();

        assertThat(customization).isEqualTo(expected);
    }

    @Test
    void shouldCreateCustomizationWhenMagistrateWithoutName() {
        List<HearingOrder> orders = orders("order 1");
        JudgeAndLegalAdvisor judge = judge(MAGISTRATES, null);

        DraftOrdersUploadedTemplate customization = underTest.buildContent(caseData, null, judge, orders);

        DraftOrdersUploadedTemplate expected = DraftOrdersUploadedTemplate.builder()
            .judgeName("")
            .judgeTitle("Justice of the Peace")
            .lastName("White")
            .caseUrl(caseUrl(CASE_NUMBER.toString(), TabUrlAnchor.DRAFT_ORDERS))
            .draftOrders("order 1")
            .subjectLineWithHearingDate("White, FMN")
            .build();

        assertThat(customization).isEqualTo(expected);
    }

    @Test
    void shouldCreateCustomizationWhenMagistrateWithName() {
        List<HearingOrder> orders = orders("order 1");
        JudgeAndLegalAdvisor judge = judge(MAGISTRATES, "Smith");

        DraftOrdersUploadedTemplate customization = underTest.buildContent(caseData, null, judge, orders);

        DraftOrdersUploadedTemplate expected = DraftOrdersUploadedTemplate.builder()
            .judgeName("Smith (JP)")
            .judgeTitle("")
            .lastName("White")
            .caseUrl(caseUrl(CASE_NUMBER.toString(), TabUrlAnchor.DRAFT_ORDERS))
            .draftOrders("order 1")
            .subjectLineWithHearingDate("White, FMN")
            .build();

        assertThat(customization).isEqualTo(expected);
    }

    private List<HearingOrder> orders(String... titles) {
        return Stream.of(titles)
            .map(title -> HearingOrder.builder().title(title).build())
            .collect(Collectors.toList());
    }

    private JudgeAndLegalAdvisor judge(JudgeOrMagistrateTitle title, String name) {
        return JudgeAndLegalAdvisor.builder()
            .judgeTitle(title)
            .judgeLastName(name)
            .judgeFullName(name)
            .build();
    }
}
