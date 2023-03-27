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
import uk.gov.hmcts.reform.fpl.model.notify.cmo.DraftOrdersRemovedTemplate;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.service.email.content.AbstractEmailContentProviderTest;
import uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HER_HONOUR_JUDGE;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ContextConfiguration(classes = {DraftOrdersRemovedContentProvider.class})
public class DraftOrdersRemovedContentProviderTest extends AbstractEmailContentProviderTest {

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
    private DraftOrdersRemovedContentProvider underTest;

    @BeforeEach
    void setUp() {
        when(helper.getEldestChildLastName(caseData.getAllChildren())).thenReturn("White");
    }

    @Test
    void shouldReturnDraftOrdersUploadedTemplate() {
        JudgeAndLegalAdvisor judge = judge(HER_HONOUR_JUDGE, "Black");
        HearingOrder order = HearingOrder.builder().title("order 1").build();
        HearingBooking hearing = HearingBooking.builder()
            .type(CASE_MANAGEMENT)
            .startDate(LocalDateTime.of(2020, Month.FEBRUARY, 20, 0, 0, 0))
            .judgeAndLegalAdvisor(judge)
            .build();

        DraftOrdersRemovedTemplate actual = underTest.buildContent(caseData, Optional.of(hearing), judge,
            order, "Removal reason");

        DraftOrdersRemovedTemplate expected = DraftOrdersRemovedTemplate.builder()
            .caseUrl(caseUrl(CASE_NUMBER.toString(), TabUrlAnchor.DRAFT_ORDERS))
            .judgeTitle("Her Honour Judge")
            .judgeName("Black")
            .respondentLastName("White")
            .subjectLineWithHearingDate("White, FMN, case management hearing, 20 February 2020")
            .draftOrdersRemoved("order 1")
            .removalReason("Removal reason")
            .build();

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void shouldReturnDraftOrdersUploadedTemplateWithoutHearing() {
        JudgeAndLegalAdvisor judge = judge(HER_HONOUR_JUDGE, "Black");
        HearingOrder order = HearingOrder.builder().title("order 1").build();

        DraftOrdersRemovedTemplate actual = underTest.buildContent(caseData, Optional.empty(), judge,
            order, "Removal reason");

        DraftOrdersRemovedTemplate expected = DraftOrdersRemovedTemplate.builder()
            .caseUrl(caseUrl(CASE_NUMBER.toString(), TabUrlAnchor.DRAFT_ORDERS))
            .judgeTitle("Her Honour Judge")
            .judgeName("Black")
            .respondentLastName("White")
            .subjectLineWithHearingDate("White, FMN")
            .draftOrdersRemoved("order 1")
            .removalReason("Removal reason")
            .build();

        assertThat(actual).isEqualTo(expected);
    }

    private JudgeAndLegalAdvisor judge(JudgeOrMagistrateTitle title, String name) {
        return JudgeAndLegalAdvisor.builder()
            .judgeTitle(title)
            .judgeLastName(name)
            .judgeFullName(name)
            .build();
    }
}
