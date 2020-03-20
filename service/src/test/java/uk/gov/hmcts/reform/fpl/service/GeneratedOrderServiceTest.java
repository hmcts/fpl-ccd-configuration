package uk.gov.hmcts.reform.fpl.service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.enums.GeneratedEPOKey;
import uk.gov.hmcts.reform.fpl.enums.GeneratedOrderKey;
import uk.gov.hmcts.reform.fpl.enums.GeneratedOrderSubtype;
import uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType;
import uk.gov.hmcts.reform.fpl.enums.InterimOrderKey;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.OrderTypeAndDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.emergencyprotectionorder.EPOChildren;
import uk.gov.hmcts.reform.fpl.model.emergencyprotectionorder.EPOPhrase;
import uk.gov.hmcts.reform.fpl.model.order.generated.FurtherDirections;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.model.order.generated.InterimEndDate;
import uk.gov.hmcts.reform.fpl.model.order.selector.ChildSelector;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toMap;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.EPOType.REMOVE_TO_ACCOMMODATION;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderSubtype.FINAL;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderSubtype.INTERIM;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.BLANK_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.CARE_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.EMERGENCY_PROTECTION_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.SUPERVISION_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HER_HONOUR_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HIS_HONOUR_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.DRAFT;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.SEALED;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.InterimEndDateType.END_OF_PROCEEDINGS;
import static uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.InterimEndDateType.NAMED_DATE;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createOrders;
import static uk.gov.hmcts.reform.fpl.utils.DocumentManagementStoreLoader.document;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    FixedTimeConfiguration.class, LookupTestConfig.class, DateFormatterService.class, GeneratedOrderService.class
})
class GeneratedOrderServiceTest {
    @Autowired
    private Time time;

    @Autowired
    private DateFormatterService dateFormatterService;

    @Autowired
    private GeneratedOrderService service;

    @Nested
    class C21Tests {

        @Test
        void shouldReturnExpectedC21OrderWhenOrderTitleIsNull() {
            GeneratedOrder order = GeneratedOrder.builder()
                .title(null)
                .details("Some details")
                .document(DocumentReference.builder().build())
                .build();

            GeneratedOrder builtOrder = service.buildCompleteOrder(OrderTypeAndDocument.builder()
                    .type(BLANK_ORDER)
                    .document(DocumentReference.builder().build())
                    .build(),
                order, JudgeAndLegalAdvisor.builder().build(), time.now().toLocalDate(), null, null).getValue();

            assertCommonC21Fields(builtOrder);
            assertThat(builtOrder.getTitle()).isEqualTo("Order");
        }

        @Test
        void shouldReturnExpectedOrderWhenC21OrderTitleIsEmptyString() {
            GeneratedOrder order = GeneratedOrder.builder()
                .title("")
                .details("Some details")
                .document(DocumentReference.builder().build())
                .build();

            GeneratedOrder builtOrder = service.buildCompleteOrder(OrderTypeAndDocument.builder()
                    .type(BLANK_ORDER)
                    .document(DocumentReference.builder().build())
                    .build(),
                order, JudgeAndLegalAdvisor.builder().build(), time.now().toLocalDate(), null, null).getValue();

            assertCommonC21Fields(builtOrder);
            assertThat(builtOrder.getTitle()).isEqualTo("Order");
        }

        @Test
        void shouldReturnExpectedOrderWhenOrderTitleIsStringWithSpaceCharacter() {
            GeneratedOrder order = GeneratedOrder.builder()
                .title(" ")
                .details("Some details")
                .document(DocumentReference.builder().build())
                .build();

            GeneratedOrder builtOrder = service.buildCompleteOrder(OrderTypeAndDocument.builder()
                    .type(BLANK_ORDER)
                    .document(DocumentReference.builder().build())
                    .build(),
                order, JudgeAndLegalAdvisor.builder().build(), time.now().toLocalDate(), null, null).getValue();

            assertCommonC21Fields(builtOrder);
            assertThat(builtOrder.getTitle()).isEqualTo("Order");
        }

        @Test
        void shouldReturnExpectedOrderWhenOrderTitlePresent() {
            GeneratedOrder order = GeneratedOrder.builder()
                .title("Example Title")
                .details("Some details")
                .document(DocumentReference.builder().build())
                .build();

            GeneratedOrder builtOrder = service.buildCompleteOrder(OrderTypeAndDocument.builder()
                    .type(BLANK_ORDER)
                    .document(DocumentReference.builder().build())
                    .build(),
                order, JudgeAndLegalAdvisor.builder().build(), time.now().toLocalDate(), null, null).getValue();

            assertCommonC21Fields(builtOrder);
            assertThat(builtOrder.getTitle()).isEqualTo("Example Title");
        }

        private void assertCommonC21Fields(GeneratedOrder order) {
            assertThat(order.getType()).isEqualTo(BLANK_ORDER.getLabel());
            assertThat(order.getDocument()).isEqualTo(DocumentReference.builder().build());
            assertThat(order.getDetails()).isEqualTo("Some details");
            assertThat(order.getDate()).isNotNull();
            assertThat(order.getJudgeAndLegalAdvisor()).isEqualTo(JudgeAndLegalAdvisor.builder().build());
        }
    }

    @Test
    void shouldReturnExpectedOrderWhenJudgeAndLegalAdvisorFullyPopulated() {
        GeneratedOrder builtOrder = service.buildCompleteOrder(OrderTypeAndDocument.builder()
                .type(CARE_ORDER)
                .subtype(FINAL)
                .document(DocumentReference.builder().build())
                .build(),
            GeneratedOrder.builder().build(), JudgeAndLegalAdvisor.builder()
                .judgeTitle(HER_HONOUR_JUDGE)
                .judgeLastName("Judy")
                .legalAdvisorName("Peter Parker")
                .build(), time.now().toLocalDate(), null, null).getValue();

        assertThat(builtOrder.getType()).isEqualTo("Final care order");
        assertThat(builtOrder.getTitle()).isNull();
        assertThat(builtOrder.getDocument()).isEqualTo(DocumentReference.builder().build());
        assertThat(builtOrder.getDate()).isNotNull();
        assertThat(builtOrder.getJudgeAndLegalAdvisor()).isEqualTo(JudgeAndLegalAdvisor.builder()
            .judgeTitle(HER_HONOUR_JUDGE)
            .judgeLastName("Judy")
            .legalAdvisorName("Peter Parker")
            .build());
    }

    @ParameterizedTest
    @EnumSource(value = GeneratedOrderType.class, names = {"CARE_ORDER", "SUPERVISION_ORDER"})
    void shouldReturnEndOfProceedingsExpiryDateWhenInterimSubtypeAndEndOfProceedingsSelected(GeneratedOrderType type) {
        GeneratedOrder builtOrder = service.buildCompleteOrder(OrderTypeAndDocument.builder()
                .type(type)
                .subtype(INTERIM)
                .document(DocumentReference.builder().build())
                .build(),
            GeneratedOrder.builder().build(), JudgeAndLegalAdvisor.builder()
                .judgeTitle(HER_HONOUR_JUDGE)
                .judgeLastName("Judy")
                .legalAdvisorName("Peter Parker")
                .build(), time.now().toLocalDate(), null,
            InterimEndDate.builder().type(END_OF_PROCEEDINGS).build()).getValue();

        assertThat(builtOrder.getExpiryDate()).isEqualTo("End of the proceedings");
    }

    @ParameterizedTest
    @EnumSource(value = GeneratedOrderType.class, names = {"CARE_ORDER", "SUPERVISION_ORDER"})
    void shouldReturnFormattedExpiryDateWhenInterimSubtypeAndNamedDateSelected(GeneratedOrderType type) {
        GeneratedOrder builtOrder = service.buildCompleteOrder(OrderTypeAndDocument.builder()
                .type(type)
                .subtype(INTERIM)
                .document(DocumentReference.builder().build())
                .build(),
            GeneratedOrder.builder().build(),
            JudgeAndLegalAdvisor.builder()
                .judgeTitle(HER_HONOUR_JUDGE)
                .judgeLastName("Judy")
                .legalAdvisorName("Peter Parker")
                .build(),
            time.now().toLocalDate(),
            null,
            InterimEndDate.builder()
                .type(NAMED_DATE)
                .endDate(time.now().toLocalDate())
                .build())
            .getValue();

        assertThat(builtOrder.getExpiryDate()).isEqualTo(dateFormatterService.formatLocalDateToString(
            time.now().toLocalDate(), "'11:59pm', d MMMM y"));
    }

    @Test
    void shouldReturnExpectedSupervisionOrderWhenFinalSubtypeSelected() {
        GeneratedOrder builtOrder = service.buildCompleteOrder(OrderTypeAndDocument.builder()
                .type(SUPERVISION_ORDER)
                .subtype(FINAL)
                .document(DocumentReference.builder().build())
                .build(),
            GeneratedOrder.builder().build(), JudgeAndLegalAdvisor.builder()
                .judgeTitle(HIS_HONOUR_JUDGE)
                .judgeLastName("Dredd")
                .legalAdvisorName("Frank N. Stein")
                .build(), time.now().toLocalDate(), 5, null).getValue();

        final LocalDateTime orderExpiration = time.now().plusMonths(5);
        final String expectedExpiryDate = dateFormatterService.formatLocalDateTimeBaseUsingFormat(orderExpiration,
            "h:mma, d MMMM y");

        assertThat(builtOrder.getType()).isEqualTo("Final supervision order");
        assertThat(builtOrder.getExpiryDate()).isEqualTo(expectedExpiryDate);
    }

    @Test
    void shouldAddDocumentToOrderTypeAndDocumentObjectWhenDocumentExists() {
        Document document = document();

        OrderTypeAndDocument returnedTypeAndDoc = service.buildOrderTypeAndDocument(OrderTypeAndDocument.builder()
            .type(BLANK_ORDER).build(), document);

        assertThat(returnedTypeAndDoc.getDocument()).isEqualTo(DocumentReference.builder()
            .binaryUrl(document.links.binary.href)
            .filename(document.originalDocumentName)
            .url(document.links.self.href)
            .build());
    }

    @ParameterizedTest
    @MethodSource("fileNameSource")
    void shouldGenerateCorrectFileNameGivenOrderType(GeneratedOrderType type,
                                                     GeneratedOrderSubtype subtype,
                                                     String expected) {
        final String fileName = service.generateOrderDocumentFileName(type, subtype);
        assertThat(fileName).isEqualTo(expected);
    }

    @ParameterizedTest
    @MethodSource("docmosisDataGenerationSource")
    void shouldCreateExpectedMapWhenGivenPopulatedCaseData(GeneratedOrderType orderType,
                                                           GeneratedOrderSubtype subtype) throws IOException {
        LocalDateTime now = time.now();
        CaseData caseData = createPopulatedCaseData(orderType, subtype, now.toLocalDate());

        Map<String, Object> expectedMap = createExpectedDocmosisData(orderType, subtype, now);
        Map<String, Object> templateData = service.getOrderTemplateData(caseData, SEALED);

        assertThat(templateData).containsAllEntriesOf(expectedMap);
        assertThat(templateData).containsKey("courtseal");
    }

    @ParameterizedTest
    @MethodSource("docmosisDataGenerationSource")
    void shouldCreateExpectedMapWhenGivenPopulatedCaseDataInDraft(GeneratedOrderType orderType,
                                                           GeneratedOrderSubtype subtype) throws IOException {
        LocalDateTime now = time.now();
        CaseData caseData = createPopulatedCaseData(orderType, subtype, now.toLocalDate());

        Map<String, Object> expectedMap = createExpectedDocmosisData(orderType, subtype, now);
        Map<String, Object> templateData = service.getOrderTemplateData(caseData, DRAFT);

        assertThat(templateData).containsAllEntriesOf(expectedMap);
        assertThat(templateData).containsKey("draftbackground");
    }

    @Test
    void shouldReturnMostRecentUploadedOrderDocumentUrl() {
        DocumentReference lastOrderDocumentReference = DocumentReference.builder()
            .filename("C21 3.pdf")
            .url("http://dm-store:8080/documents/79ec80ec-7be6-493b-b4e6-f002f05b7079")
            .binaryUrl("http://dm-store:8080/documents/79ec80ec-7be6-493b-b4e6-f002f05b7079/binary")
            .build();
        final DocumentReference mostRecentDocument = service.getMostRecentUploadedOrderDocument(createOrders(
            lastOrderDocumentReference));

        assertThat(mostRecentDocument.getFilename()).isEqualTo(lastOrderDocumentReference.getFilename());
        assertThat(mostRecentDocument.getUrl()).isEqualTo(lastOrderDocumentReference.getUrl());
        assertThat(mostRecentDocument.getBinaryUrl()).isEqualTo(lastOrderDocumentReference.getBinaryUrl());
    }

    @Test
    void shouldRemovePropertiesOnCaseDetailsUsedForOrderCapture() {
        Map<String, Object> data = stream(GeneratedOrderKey.values())
            .collect(toMap(GeneratedOrderKey::getKey, value -> ""));
        data.putAll(stream(GeneratedEPOKey.values()).collect(toMap(GeneratedEPOKey::getKey, value -> "")));
        data.putAll(stream(InterimOrderKey.values()).collect(toMap(InterimOrderKey::getKey, value -> "")));

        data.put("DO NOT REMOVE", "");
        service.removeOrderProperties(data);

        assertThat(data).containsOnlyKeys("DO NOT REMOVE");
    }

    private static Stream<Arguments> fileNameSource() {
        return Stream.of(
            Arguments.of(BLANK_ORDER, null, "blank_order_c21.pdf"),
            Arguments.of(CARE_ORDER, INTERIM, "interim_care_order.pdf"),
            Arguments.of(CARE_ORDER, FINAL, "final_care_order.pdf"),
            Arguments.of(SUPERVISION_ORDER, INTERIM, "interim_supervision_order.pdf"),
            Arguments.of(SUPERVISION_ORDER, FINAL, "final_supervision_order.pdf"),
            Arguments.of(EMERGENCY_PROTECTION_ORDER, null, "emergency_protection_order.pdf"),
            Arguments.of(SUPERVISION_ORDER, null, "supervision_order.pdf"),
            Arguments.of(CARE_ORDER, null, "care_order.pdf")
        );
    }

    private static Stream<Arguments> docmosisDataGenerationSource() {
        return Stream.of(
            Arguments.of(BLANK_ORDER, null),
            Arguments.of(CARE_ORDER, INTERIM),
            Arguments.of(CARE_ORDER, FINAL),
            Arguments.of(SUPERVISION_ORDER, INTERIM),
            Arguments.of(SUPERVISION_ORDER, FINAL),
            Arguments.of(EMERGENCY_PROTECTION_ORDER, null)
        );
    }

    private Map<String, Object> createExpectedDocmosisData(GeneratedOrderType type,
                                                           GeneratedOrderSubtype subtype,
                                                           LocalDateTime dateTime) {
        ImmutableMap.Builder<String, Object> expectedMap = ImmutableMap.builder();
        final LocalDate date = dateTime.toLocalDate();
        String formattedDate = dateFormatterService.formatLocalDateToString(date, FormatStyle.LONG);

        List<Map<String, String>> children = ImmutableList.of(
            ImmutableMap.of(
                "name", "Timmy Jones",
                "gender", "Boy",
                "dateOfBirth", formattedDate),
            ImmutableMap.of(
                "name", "Robbie Jones",
                "gender", "Boy",
                "dateOfBirth", formattedDate));

        switch (type) {
            case BLANK_ORDER:
                expectedMap
                    .put("orderType", BLANK_ORDER)
                    .put("orderTitle", "Example Title")
                    .put("childrenAct", "Children Act 1989")
                    .put("orderDetails", "Example details");
                break;
            case CARE_ORDER:
                expectedMap
                    .put("orderType", CARE_ORDER);
                if (subtype == INTERIM) {
                    expectedMap
                        .put("orderTitle", "Interim care order")
                        .put("childrenAct", "Section 38 Children Act 1989")
                        .put("orderDetails",
                            "It is ordered that the children are placed in the care of Example Local Authority"
                                + " until the end of the proceedings.");
                } else if (subtype == FINAL) {
                    expectedMap
                        .put("orderTitle", "Care order")
                        .put("childrenAct", "Section 31 Children Act 1989")
                        .put("orderDetails",
                            "It is ordered that the children are placed in the care of Example Local Authority.");
                }
                break;
            case SUPERVISION_ORDER:
                children = ImmutableList.of(
                    ImmutableMap.of(
                        "name", "Timmy Jones",
                        "gender", "Boy",
                        "dateOfBirth", formattedDate));

                expectedMap
                    .put("orderType", SUPERVISION_ORDER);
                if (subtype == INTERIM) {
                    String dayOrdinalSuffix = dateFormatterService.getDayOfMonthSuffix(date.getDayOfMonth());
                    String detailsDate = dateFormatterService.formatLocalDateToString(
                        date, "d'" + dayOrdinalSuffix + "' MMMM y");

                    expectedMap
                        .put("orderTitle", "Interim supervision order")
                        .put("childrenAct", "Section 38 and Paragraphs 1 and 2 Schedule 3 Children Act 1989")
                        .put("orderDetails", String.format("It is ordered that Example Local Authority supervises"
                            + " the child until 11:59pm on the %s.", detailsDate));
                } else if (subtype == FINAL) {
                    LocalDateTime expiryDate = dateTime.plusMonths(5);
                    final String suffix = dateFormatterService.getDayOfMonthSuffix(expiryDate.getDayOfMonth());
                    final String formattedDateTime =
                        dateFormatterService.formatLocalDateTimeBaseUsingFormat(expiryDate,
                            "h:mma 'on the' d'" + suffix + "' MMMM y");
                    expectedMap
                        .put("orderTitle", "Supervision order")
                        .put("childrenAct", "Section 31 and Paragraphs 1 and 2 Schedule 3 Children Act 1989")
                        .put("orderDetails",
                            String.format(
                                "It is ordered that Example Local Authority supervises the child for 5 months from the "
                                    + "date of this order until %s.", formattedDateTime));
                }
                break;
            case EMERGENCY_PROTECTION_ORDER:
                expectedMap
                    .put("orderType", EMERGENCY_PROTECTION_ORDER)
                    .put("localAuthorityName", "Example Local Authority")
                    .put("childrenDescription", "Test description")
                    .put("epoType", REMOVE_TO_ACCOMMODATION)
                    .put("includePhrase", "Yes")
                    .put("removalAddress", "1 Main Street, Lurgan, BT66 7PP, Armagh, United Kingdom")
                    .put("childrenCount", 2)
                    .put("epoStartDateTime", dateFormatterService.formatLocalDateTimeBaseUsingFormat(time.now(),
                        "d MMMM yyyy 'at' h:mma"))
                    .put("epoEndDateTime", dateFormatterService.formatLocalDateTimeBaseUsingFormat(time.now(),
                        "d MMMM yyyy 'at' h:mma"));
                break;
            default:
        }

        expectedMap
            .put("furtherDirections", (type != BLANK_ORDER) ? "Example Directions" : "")
            .put("familyManCaseNumber", "123")
            .put("courtName", "Family Court")
            .put("dateOfIssue", DateFormatterService.formatLocalDateToString(time.now().toLocalDate(), "d MMMM yyyy"))
            .put("judgeTitleAndName", "Her Honour Judge Judy")
            .put("legalAdvisorName", "Peter Parker")
            .put("children", children);

        return expectedMap.build();
    }

    private CaseData createPopulatedCaseData(GeneratedOrderType type,
                                             GeneratedOrderSubtype subtype,
                                             LocalDate localDate) {
        CaseData.CaseDataBuilder caseDataBuilder = CaseData.builder();
        caseDataBuilder.orderAppliesToAllChildren(YES.getValue());

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
}
