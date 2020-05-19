package uk.gov.hmcts.reform.fpl.service.docmosis;

import com.google.common.collect.ImmutableList;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.fpl.enums.GeneratedOrderSubtype;
import uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType;
import uk.gov.hmcts.reform.fpl.enums.OrderStatus;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.OrderTypeAndDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisChild;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisGeneratedOrder;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisGeneratedOrder.DocmosisGeneratedOrderBuilder;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisJudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.emergencyprotectionorder.EPOChildren;
import uk.gov.hmcts.reform.fpl.model.emergencyprotectionorder.EPOPhrase;
import uk.gov.hmcts.reform.fpl.model.order.generated.FurtherDirections;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.model.order.generated.InterimEndDate;
import uk.gov.hmcts.reform.fpl.model.order.selector.ChildSelector;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.FormatStyle;
import java.util.List;

import static uk.gov.hmcts.reform.fpl.enums.DocmosisImages.COURT_SEAL;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisImages.DRAFT_WATERMARK;
import static uk.gov.hmcts.reform.fpl.enums.EPOType.REMOVE_TO_ACCOMMODATION;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderSubtype.FINAL;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderSubtype.INTERIM;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.BLANK_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.CARE_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.EMERGENCY_PROTECTION_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.SUPERVISION_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HER_HONOUR_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.DRAFT;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.SEALED;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.InterimEndDateType.END_OF_PROCEEDINGS;
import static uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.InterimEndDateType.NAMED_DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.getDayOfMonthSuffix;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

public abstract class AbstractOrderGenerationServiceTest {

    private static final String LOCAL_AUTHORITY_NAME = "Example Local Authority";

    @Autowired
    Time time;

    CaseData createPopulatedCaseData(GeneratedOrderType type, OrderStatus orderStatus) {
        return createPopulatedCaseData(type, null, orderStatus);
    }

    CaseData createPopulatedCaseData(GeneratedOrderType type, GeneratedOrderSubtype subtype, OrderStatus orderStatus) {
        CaseData.CaseDataBuilder caseDataBuilder = CaseData.builder();
        caseDataBuilder.orderAppliesToAllChildren(YES.getValue());
        caseDataBuilder.generatedOrderStatus(orderStatus);

        final LocalDate localDate = time.now().toLocalDate();

        switch (type) {
            case BLANK_ORDER:
                caseDataBuilder
                    .orderTypeAndDocument(OrderTypeAndDocument.builder()
                        .type(BLANK_ORDER)
                        .document(DocumentReference.builder().build())
                        .build())
                    .order(GeneratedOrder.builder()
                        .title("Example Title")
                        .details("Example details")
                        .build());
                break;
            case CARE_ORDER:
                caseDataBuilder
                    .orderTypeAndDocument(OrderTypeAndDocument.builder()
                        .type(CARE_ORDER)
                        .subtype(subtype)
                        .document(DocumentReference.builder().build())
                        .build())
                    .orderFurtherDirections(FurtherDirections.builder()
                        .directionsNeeded("Yes")
                        .directions("Example Directions")
                        .build());

                if (subtype == INTERIM) {
                    caseDataBuilder.interimEndDate(InterimEndDate.builder().type(END_OF_PROCEEDINGS).build());
                }

                break;
            case SUPERVISION_ORDER:
                caseDataBuilder
                    .orderTypeAndDocument(OrderTypeAndDocument.builder()
                        .type(SUPERVISION_ORDER)
                        .subtype(subtype)
                        .document(DocumentReference.builder().build())
                        .build())
                    .orderFurtherDirections(FurtherDirections.builder()
                        .directionsNeeded("Yes")
                        .directions("Example Directions")
                        .build())
                    .orderMonths(5)
                    .orderAppliesToAllChildren(NO.getValue())
                    .childSelector(ChildSelector.builder()
                        .selected(List.of(0))
                        .build());

                if (subtype == INTERIM) {
                    caseDataBuilder.interimEndDate(InterimEndDate.builder()
                        .type(NAMED_DATE)
                        .endDate(localDate)
                        .build());
                }

                break;
            case EMERGENCY_PROTECTION_ORDER:
                caseDataBuilder
                    .orderTypeAndDocument(OrderTypeAndDocument.builder()
                        .type(EMERGENCY_PROTECTION_ORDER)
                        .document(DocumentReference.builder().build())
                        .build())
                    .epoChildren(EPOChildren.builder()
                        .descriptionNeeded("Yes")
                        .description("Test description")
                        .build())
                    .epoEndDate(time.now())
                    .epoPhrase(EPOPhrase.builder()
                        .includePhrase("Yes")
                        .build())
                    .epoType(REMOVE_TO_ACCOMMODATION)
                    .orderFurtherDirections(FurtherDirections.builder()
                        .directionsNeeded("Yes")
                        .directions("Example Directions")
                        .build())
                    .epoRemovalAddress(Address.builder()
                        .addressLine1("1 Main Street")
                        .addressLine2("Lurgan")
                        .postTown("BT66 7PP")
                        .county("Armagh")
                        .country("United Kingdom")
                        .build());
                break;
            default:
        }

        caseDataBuilder
            .familyManCaseNumber("123")
            .caseLocalAuthority("example")
            .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                .judgeTitle(HER_HONOUR_JUDGE)
                .judgeLastName("Judy")
                .legalAdvisorName("Peter Parker")
                .build())
            .dateOfIssue(time.now().toLocalDate())
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
        final LocalDate date = time.now().toLocalDate();

        String formattedDate = formatLocalDateToString(date, FormatStyle.LONG);

        List<DocmosisChild> children = ImmutableList.of(
            DocmosisChild.builder()
                .name("Timmy Jones")
                .gender("Boy")
                .dateOfBirth(formattedDate).build(),
            DocmosisChild.builder()
                .name("Robbie Jones")
                .gender("Boy")
                .dateOfBirth(formattedDate).build());

        DocmosisGeneratedOrderBuilder<?, ?> orderBuilder = DocmosisGeneratedOrder.builder();
        switch (type) {
            case BLANK_ORDER:
                orderBuilder = initialiseBlankOrder();
                break;
            case CARE_ORDER:
                orderBuilder = initialiseCareOrder(subtype);
                break;
            case SUPERVISION_ORDER:
                children = ImmutableList.of(
                    DocmosisChild.builder()
                        .name("Timmy Jones")
                        .gender("Boy")
                        .dateOfBirth(formattedDate).build());
                orderBuilder = initialiseSupervisionOrder(subtype);
                break;
            case EMERGENCY_PROTECTION_ORDER:
                orderBuilder = initialiseEPO();
                break;
            default:

        }

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
            .children(children)
            .crest("[userImage:crest.png]")
            .build();
    }

    private DocmosisGeneratedOrderBuilder initialiseBlankOrder() {
        return initialiseOrderBuilder("Example Title", "Children Act 1989",
            "Example details");//.orderType(BLANK_ORDER);
    }

    private DocmosisGeneratedOrderBuilder initialiseCareOrder(GeneratedOrderSubtype subtype) {
        DocmosisGeneratedOrderBuilder orderBuilder = DocmosisGeneratedOrder.builder();
        if (subtype == INTERIM) {
            orderBuilder = initialiseOrderBuilder("Interim care order",
                "Section 38 Children Act 1989", "It is ordered that the children are "
                    + "placed in the care of Example Local Authority until the end of the proceedings.");
        } else if (subtype == FINAL) {
            orderBuilder = initialiseOrderBuilder("Care order", "Section 31 Children Act 1989",
                "It is ordered that the children are placed in the care of "
                    + "Example Local Authority.");
        }
        return orderBuilder
            .orderType(CARE_ORDER)
            .localAuthorityName(LOCAL_AUTHORITY_NAME);
    }

    private DocmosisGeneratedOrderBuilder initialiseSupervisionOrder(GeneratedOrderSubtype subtype) {
        DocmosisGeneratedOrderBuilder orderBuilder = DocmosisGeneratedOrder.builder();
        if (subtype == INTERIM) {
            String detailsDate = formatLocalDateToString(
                time.now().toLocalDate(), "d'" + getDayOfMonthSuffix(time.now().toLocalDate().getDayOfMonth())
                    + "' MMMM y");

            orderBuilder = initialiseOrderBuilder("Interim supervision order",
                "Section 38 and Paragraphs 1 and 2 Schedule 3 Children Act 1989",
                String.format("It is ordered that Example Local Authority supervises"
                    + " the child until 11:59pm on the %s.", detailsDate));
        } else if (subtype == FINAL) {
            LocalDateTime expiryDate = time.now().plusMonths(5);
            final String formattedDateTime = formatLocalDateTimeBaseUsingFormat(expiryDate,
                "h:mma 'on the' d'" + getDayOfMonthSuffix(expiryDate.getDayOfMonth()) + "' MMMM y");

            orderBuilder = initialiseOrderBuilder("Supervision order",
                "Section 31 and Paragraphs 1 and 2 Schedule 3 Children Act 1989",
                String.format("It is ordered that Example Local Authority supervises the child for 5 months "
                    + "from the date of this order until %s.", formattedDateTime));
        }
        return orderBuilder
            .orderType(SUPERVISION_ORDER);
    }

    private DocmosisGeneratedOrderBuilder initialiseEPO() {
        return DocmosisGeneratedOrder.builder()
            .orderType(EMERGENCY_PROTECTION_ORDER)
            .localAuthorityName(LOCAL_AUTHORITY_NAME)
            .childrenDescription("Test description")
            .epoType(REMOVE_TO_ACCOMMODATION)
            .includePhrase("Yes")
            .removalAddress("1 Main Street, Lurgan, BT66 7PP, Armagh, United Kingdom")
            .epoStartDateTime(formatLocalDateTimeBaseUsingFormat(time.now(), "d MMMM yyyy 'at' h:mma"))
            .epoEndDateTime(formatLocalDateTimeBaseUsingFormat(time.now(), "d MMMM yyyy 'at' h:mma"));
    }

    private DocmosisGeneratedOrderBuilder initialiseOrderBuilder(String orderTitle, String childrenAct,
        String orderDetails) {
        return DocmosisGeneratedOrder.builder()
            .orderTitle(orderTitle)
            .childrenAct(childrenAct)
            .orderDetails(orderDetails);
    }
}
