package uk.gov.hmcts.reform.fpl.service.orders.generator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.service.ManageOrderDocumentService;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.C42FamilyAssistanceOrderDocmosisParameters;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.DocmosisParameters;
import uk.gov.hmcts.reform.fpl.service.orders.generator.common.OrderMessageGenerator;

import java.time.LocalDate;
import java.util.UUID;
import java.util.stream.Stream;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C42_FAMILY_ASSISTANCE_ORDER;
import static uk.gov.hmcts.reform.fpl.service.orders.generator.C34aContactWithAChildCareOrderParameterGeneratorTest.CONSENT;

@ExtendWith(MockitoExtension.class)
class C42FamilyAssistanceOrderParameterGeneratorTest {

    private static final String LA_NAME = "Testing LA Name";
    private static final String LOCAL_AUTHORITY_NAME = "Swansea City Council";
    private static final String FURTHER_DIRECTIONS = "These are some further directions";

    private static final Child PARTY_ONE = Child.builder()
        .party(ChildParty.builder().firstName("John").lastName("Smith").build())
        .build();

    private static final Respondent PARTY_TWO = Respondent.builder()
        .party(RespondentParty.builder().firstName("Jack").lastName("Smith").build())
        .build();

    private static final Respondent PARTY_THREE = Respondent.builder()
        .party(RespondentParty.builder().firstName("Jean").lastName("Smith").build())
        .build();

    @Mock
    private ManageOrderDocumentService manageOrderDocumentService;
    @Mock
    private LocalAuthorityNameLookupConfiguration laNameLookup;
    @Mock
    private OrderMessageGenerator orderMessageGenerator;

    @InjectMocks
    private C42FamilyAssistanceOrderDocumentParameterGenerator underTest;

    @Test
    void accept() {
        assertThat(underTest.accept()).isEqualTo(C42_FAMILY_ASSISTANCE_ORDER);
    }

    @Test
    void template() {
        assertThat(underTest.template()).isEqualTo(DocmosisTemplates.ORDER_V2);
    }

    @ParameterizedTest
    @MethodSource("parties")
    void generateOrderByConsent(int numParties, String furtherDirections) {
        CaseData caseData = buildCaseData(true, furtherDirections, numParties);
        when(laNameLookup.getLocalAuthorityName(any())).thenReturn(LOCAL_AUTHORITY_NAME);
        when(orderMessageGenerator.getOrderByConsentMessage(any())).thenReturn(CONSENT);

        DocmosisParameters generatedParameters = underTest.generate(caseData);
        DocmosisParameters expectedParameters = buildExpectedParams(true, numParties, furtherDirections);

        assertThat(generatedParameters).isEqualTo(expectedParameters);
    }

    @ParameterizedTest
    @MethodSource("parties")
    void generateOrderNotByConsent(int numParties, String furtherDirections) {
        CaseData caseData = buildCaseData(false, furtherDirections, numParties);
        when(laNameLookup.getLocalAuthorityName(any())).thenReturn(LOCAL_AUTHORITY_NAME);
        when(orderMessageGenerator.getOrderByConsentMessage(any())).thenReturn(null);

        DocmosisParameters generatedParameters = underTest.generate(caseData);
        DocmosisParameters expectedParameters = buildExpectedParams(false, numParties, furtherDirections);

        assertThat(generatedParameters).isEqualTo(expectedParameters);
    }


    private static Stream<Arguments> parties() {
        return Stream.of(
            Arguments.of(1, null),
            Arguments.of(2, null),
            Arguments.of(3, null),
            Arguments.of(3, FURTHER_DIRECTIONS)
        );
    }

    private CaseData buildCaseData(boolean byConsent, String furtherDirections, int numParties) {
        ManageOrdersEventData.ManageOrdersEventDataBuilder eventBuilder = ManageOrdersEventData.builder()
            .manageOrdersType(C42_FAMILY_ASSISTANCE_ORDER)
            .manageOrdersPartyToBeBefriended1(DynamicList.builder()
                .value(DynamicListElement.builder()
                    .code(UUID.randomUUID().toString())
                    .label(PARTY_ONE.getParty().getFullName())
                    .build())
                .build())
            .manageOrdersPartyToBeBefriended2(numParties >= 2 ? DynamicList.builder()
                .value(DynamicListElement.builder()
                    .code(UUID.randomUUID().toString())
                    .label(PARTY_TWO.getParty().getFullName())
                    .build())
                .build() : null)
            .manageOrdersPartyToBeBefriended3(numParties >= 3 ? DynamicList.builder()
                .value(DynamicListElement.builder()
                    .code(UUID.randomUUID().toString())
                    .label(PARTY_THREE.getParty().getFullName())
                    .build())
                .build() : null)
            .manageOrdersFurtherDirections(furtherDirections)
            .manageOrdersFamilyAssistanceEndDate(LocalDate.of(2022, 7, 29))
            .manageOrdersIsByConsent(byConsent ? YES.getValue() : NO.getValue());

        return CaseData.builder()
            .caseLocalAuthority(LA_NAME)
            .manageOrdersEventData(eventBuilder.build())
            .build();
    }

    private DocmosisParameters buildExpectedParams(boolean byConsent, int numberOfParties, String furtherDirections) {

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(getOrderMessageFromNumParties(numberOfParties));
        stringBuilder.append("\nThe Court directs\n");
        stringBuilder.append("Where - \n(a) there are no proceedings pending under Part 2 Children Act 1989; \n"
            + "(b) an officer of the service / Welsh family proceedings officer made available under this order is "
            + "given cause to suspect, whilst this order is in force, that the child concerned is at risk of harm; and"
            + " \n(c) as a result, the officer makes a risk assessment under section 16A of that Act,\n\n");
        stringBuilder.append("the officer may apply to the court for it to revive the previous proceedings and to"
            + " consider that risk assessment and give such directions as the court thinks necessary.");
        if (isNotEmpty(furtherDirections)) {
            stringBuilder.append("\n\n" + furtherDirections);
        }
        stringBuilder.append("\n\nThis order ends on 29th July 2022.");

        return C42FamilyAssistanceOrderDocmosisParameters.builder()
            .orderTitle(C42_FAMILY_ASSISTANCE_ORDER.getTitle())
            .childrenAct(C42_FAMILY_ASSISTANCE_ORDER.getChildrenAct())
            .orderByConsent(byConsent ? "By consent" : null)
            .orderDetails(stringBuilder.toString())
            .localAuthorityName(LOCAL_AUTHORITY_NAME)
            .noticeHeader("Notice \n")
            .noticeMessage("This order will have effect for 12 months from the date ordered on, or such lesser period"
                + " as specified.")
            .build();
    }

    public String getOrderMessageFromNumParties(int number) {
        String baseOrder = "The Court orders an officer of the service to be made available to advise, assist and,"
            + " where appropriate, befriend\n";
        switch (number) {
            case 1:
                return baseOrder + "John Smith\n";
            case 2:
                return baseOrder + "John Smith\nJack Smith\n";
            case 3:
                return baseOrder + "John Smith\nJack Smith\nJean Smith\n";
            default:
                return "";
        }
    }
}
