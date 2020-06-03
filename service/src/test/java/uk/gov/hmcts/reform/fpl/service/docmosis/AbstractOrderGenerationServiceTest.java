package uk.gov.hmcts.reform.fpl.service.docmosis;

import com.google.common.collect.ImmutableList;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.fpl.enums.GeneratedOrderSubtype;
import uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType;
import uk.gov.hmcts.reform.fpl.enums.OrderStatus;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisChild;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisGeneratedOrder;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisGeneratedOrder.DocmosisGeneratedOrderBuilder;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisJudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.time.LocalDate;
import java.time.format.FormatStyle;
import java.util.List;

import static uk.gov.hmcts.reform.fpl.enums.DocmosisImages.COURT_SEAL;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisImages.DRAFT_WATERMARK;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.BLANK_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HER_HONOUR_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.DRAFT;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.SEALED;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

public abstract class AbstractOrderGenerationServiceTest {

    static final String LOCAL_AUTHORITY_NAME = "Example Local Authority";

    @Autowired
    Time time;

    abstract CaseData.CaseDataBuilder populateCustomCaseData(GeneratedOrderSubtype subtype);

    abstract DocmosisGeneratedOrderBuilder<?, ?> populateCustomOrderFields(GeneratedOrderSubtype subtype);

    CaseData createPopulatedCaseData(OrderStatus orderStatus) {
        return createPopulatedCaseData(null, orderStatus);
    }

    CaseData createPopulatedCaseData(GeneratedOrderSubtype subtype, OrderStatus orderStatus) {
        CaseData.CaseDataBuilder caseDataBuilder = populateCustomCaseData(subtype);
        caseDataBuilder.generatedOrderStatus(orderStatus);

        final LocalDate localDate = time.now().toLocalDate();

        caseDataBuilder
            .familyManCaseNumber("123")
            .caseLocalAuthority("example")
            .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                .judgeTitle(HER_HONOUR_JUDGE)
                .judgeLastName("Judy")
                .legalAdvisorName("Peter Parker")
                .build())
            .dateOfIssue(localDate)
            .children1(ImmutableList.of(
                element(Child.builder()
                    .party(ChildParty.builder()
                        .firstName("Timmy")
                        .lastName("Jones")
                        .gender("Boy")
                        .dateOfBirth(localDate)
                        .build())
                    .build()),
                element(Child.builder()
                    .party(ChildParty.builder()
                        .firstName("Robbie")
                        .lastName("Jones")
                        .gender("Boy")
                        .dateOfBirth(localDate)
                        .build())
                    .build())))
            .build();

        return caseDataBuilder.build();
    }

    DocmosisGeneratedOrder createExpectedDocmosisData(GeneratedOrderType type, OrderStatus orderStatus) {
        return createExpectedDocmosisData(type, null, orderStatus);
    }

    DocmosisGeneratedOrder createExpectedDocmosisData(GeneratedOrderType type, GeneratedOrderSubtype subtype,
        OrderStatus orderStatus) {

        DocmosisGeneratedOrderBuilder<?, ?> orderBuilder = populateCustomOrderFields(subtype);

        if (orderStatus == DRAFT) {
            orderBuilder.draftbackground(DRAFT_WATERMARK.getValue()).build();
        }

        if (orderStatus == SEALED) {
            orderBuilder.courtseal(COURT_SEAL.getValue()).build();
        }

        DocmosisJudgeAndLegalAdvisor judgeAndLegalAdvisor = DocmosisJudgeAndLegalAdvisor.builder()
            .judgeTitleAndName("Her Honour Judge Judy")
            .legalAdvisorName("Peter Parker")
            .build();

        return orderBuilder
            .orderType(type)
            .furtherDirections(type != BLANK_ORDER ? "Example Directions" : "")
            .familyManCaseNumber("123")
            .courtName("Family Court")
            .dateOfIssue(formatLocalDateToString(time.now().toLocalDate(), "d MMMM yyyy"))
            .judgeAndLegalAdvisor(judgeAndLegalAdvisor)
            .crest("[userImage:crest.png]")
            .build();
    }

    List<DocmosisChild> getChildren() {
        final LocalDate date = time.now().toLocalDate();

        String formattedDate = formatLocalDateToString(date, FormatStyle.LONG);
        return ImmutableList.of(
            DocmosisChild.builder()
                .name("Timmy Jones")
                .gender("Boy")
                .dateOfBirth(formattedDate).build(),
            DocmosisChild.builder()
                .name("Robbie Jones")
                .gender("Boy")
                .dateOfBirth(formattedDate).build());
    }

    DocmosisGeneratedOrderBuilder<?, ?> createOrderBuilder(String orderTitle, String childrenAct,
        String orderDetails, List<DocmosisChild> children) {
        return DocmosisGeneratedOrder.builder()
            .orderTitle(orderTitle)
            .childrenAct(childrenAct)
            .orderDetails(orderDetails)
            .children(children);
    }
}
