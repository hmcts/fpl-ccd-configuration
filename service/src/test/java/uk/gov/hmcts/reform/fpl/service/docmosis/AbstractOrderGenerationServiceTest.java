package uk.gov.hmcts.reform.fpl.service.docmosis;

import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.fpl.enums.GeneratedOrderSubtype;
import uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType;
import uk.gov.hmcts.reform.fpl.enums.OrderStatus;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisChild;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisGeneratedOrder;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisJudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.time.LocalDate;
import java.time.format.FormatStyle;
import java.util.List;

import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_CODE;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_NAME;
import static uk.gov.hmcts.reform.fpl.enums.ChildGender.BOY;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisImages.COURT_SEAL;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisImages.DRAFT_WATERMARK;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.BLANK_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.EMERGENCY_PROTECTION_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HER_HONOUR_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.DRAFT;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.SEALED;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE_TIME;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testChild;

abstract class AbstractOrderGenerationServiceTest {

    static final String LOCAL_AUTHORITY_NAME = LOCAL_AUTHORITY_1_NAME;
    static final long CASE_NUMBER = 1234123412341234L;
    static final String FORMATTED_CASE_NUMBER = "1234-1234-1234-1234";

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
            .id(CASE_NUMBER)
            .caseLocalAuthority(LOCAL_AUTHORITY_1_CODE)
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

    DocmosisGeneratedOrder enrichWithStandardData(GeneratedOrderType type,
                                                  OrderStatus orderStatus,
                                                  DocmosisGeneratedOrder docmosisGeneratedOrder) {
        return enrichWithStandardData(type, null, orderStatus, docmosisGeneratedOrder);
    }

    DocmosisGeneratedOrder enrichWithStandardData(GeneratedOrderType type,
                                                  GeneratedOrderSubtype subtype,
                                                  OrderStatus orderStatus,
                                                  DocmosisGeneratedOrder docmosisGeneratedOrder) {

        String date = EMERGENCY_PROTECTION_ORDER.equals(type)
            ? formatLocalDateTimeBaseUsingFormat(time.now(), DATE_TIME)
            : formatLocalDateToString(time.now().toLocalDate(), DATE);
        DocmosisJudgeAndLegalAdvisor judgeAndLegalAdvisor = DocmosisJudgeAndLegalAdvisor.builder()
            .judgeTitleAndName("Her Honour Judge Judy")
            .legalAdvisorName("Peter Parker")
            .build();

        var newOrderBuilder = docmosisGeneratedOrder
            .toBuilder()
            .orderType(type)
            .furtherDirections(type != BLANK_ORDER ? "Example Directions" : "")
            .familyManCaseNumber("123")
            .ccdCaseNumber(FORMATTED_CASE_NUMBER)
            .courtName("Family Court")
            .dateOfIssue(date)
            .judgeAndLegalAdvisor(judgeAndLegalAdvisor)
            .crest("[userImage:crest.png]");

        if (orderStatus == DRAFT) {
            newOrderBuilder.draftbackground(DRAFT_WATERMARK.getValue());
        } else if (orderStatus == SEALED) {
            newOrderBuilder.courtseal(COURT_SEAL.getValue());
        }

        return newOrderBuilder.build();
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
