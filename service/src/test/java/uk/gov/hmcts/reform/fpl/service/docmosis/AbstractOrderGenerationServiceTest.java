package uk.gov.hmcts.reform.fpl.service.docmosis;

import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.fpl.enums.GeneratedOrderSubtype;
import uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType;
import uk.gov.hmcts.reform.fpl.enums.OrderStatus;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisChild;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisGeneratedOrder;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisGeneratedOrder.DocmosisGeneratedOrderBuilder;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisJudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.time.LocalDate;
import java.time.format.FormatStyle;
import java.util.List;

import static uk.gov.hmcts.reform.fpl.enums.ChildGender.BOY;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisImages.COURT_SEAL;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisImages.DRAFT_WATERMARK;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.BLANK_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HER_HONOUR_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.DRAFT;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.SEALED;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testChild;

abstract class AbstractOrderGenerationServiceTest {

    static final String LOCAL_AUTHORITY_NAME = "Example Local Authority";

    @Autowired
    Time time;

    CaseData.CaseDataBuilder defaultCaseData(OrderStatus orderStatus) {
        return defaultCaseData(null, orderStatus);
    }

    CaseData.CaseDataBuilder defaultCaseData(GeneratedOrderSubtype subtype, OrderStatus orderStatus) {
        CaseData.CaseDataBuilder caseDataBuilder = CaseData.builder();
        caseDataBuilder.generatedOrderStatus(orderStatus);

        final LocalDate today = time.now().toLocalDate();

        return caseDataBuilder
            .familyManCaseNumber("123")
            .caseLocalAuthority("example")
            .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                .judgeTitle(HER_HONOUR_JUDGE)
                .judgeLastName("Judy")
                .legalAdvisorName("Peter Parker")
                .build())
            .dateOfIssue(today)
            .children1(List.of(
                testChild("Timmy", "Jones", BOY, today),
                testChild("Robbie", "Jones", BOY, today)
            ));
    }

    DocmosisGeneratedOrderBuilder<?, ?> defaultExpectedData(GeneratedOrderType type, OrderStatus orderStatus) {
        return defaultExpectedData(type, null, orderStatus);
    }

    DocmosisGeneratedOrderBuilder<?, ?> defaultExpectedData(GeneratedOrderType type, GeneratedOrderSubtype subtype,
                                                            OrderStatus orderStatus) {

        DocmosisJudgeAndLegalAdvisor judgeAndLegalAdvisor = DocmosisJudgeAndLegalAdvisor.builder()
            .judgeTitleAndName("Her Honour Judge Judy")
            .legalAdvisorName("Peter Parker")
            .build();

        DocmosisGeneratedOrderBuilder orderBuilder = DocmosisGeneratedOrder.builder()
            .orderType(type)
            .furtherDirections(type != BLANK_ORDER ? "Example Directions" : "")
            .familyManCaseNumber("123")
            .courtName("Family Court")
            .dateOfIssue(formatLocalDateToString(time.now().toLocalDate(), "d MMMM yyyy"))
            .judgeAndLegalAdvisor(judgeAndLegalAdvisor)
            .crest("[userImage:crest.png]");

        if (orderStatus == DRAFT) {
            orderBuilder.draftbackground(DRAFT_WATERMARK.getValue()).build();
        } else if (orderStatus == SEALED) {
            orderBuilder.courtseal(COURT_SEAL.getValue()).build();
        }

        return orderBuilder;
    }

    List<DocmosisChild> getChildren() {
        final LocalDate date = time.now().toLocalDate();

        String formattedDate = formatLocalDateToString(date, FormatStyle.LONG);
        return List.of(
            DocmosisChild.builder()
                .name("Timmy Jones")
                .gender("Boy")
                .dateOfBirth(formattedDate).build(),
            DocmosisChild.builder()
                .name("Robbie Jones")
                .gender("Boy")
                .dateOfBirth(formattedDate).build());
    }
}
