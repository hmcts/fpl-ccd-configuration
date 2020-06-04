package uk.gov.hmcts.reform.fpl.service.docmosis;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.enums.GeneratedOrderSubtype;
import uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType;
import uk.gov.hmcts.reform.fpl.enums.OrderStatus;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CaseData.CaseDataBuilder;
import uk.gov.hmcts.reform.fpl.model.OrderTypeAndDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisChild;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisGeneratedOrder;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisGeneratedOrder.DocmosisGeneratedOrderBuilder;
import uk.gov.hmcts.reform.fpl.model.order.generated.FurtherDirections;
import uk.gov.hmcts.reform.fpl.model.order.generated.InterimEndDate;
import uk.gov.hmcts.reform.fpl.model.order.selector.ChildSelector;
import uk.gov.hmcts.reform.fpl.service.CaseDataExtractionService;
import uk.gov.hmcts.reform.fpl.service.HearingVenueLookUpService;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import java.time.LocalDateTime;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderSubtype.FINAL;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderSubtype.INTERIM;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.SUPERVISION_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.DRAFT;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.SEALED;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.InterimEndDateType.NAMED_DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.getDayOfMonthSuffix;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {SupervisionOrderGenerationService.class, CaseDataExtractionService.class,
    LookupTestConfig.class, HearingVenueLookUpService.class, JacksonAutoConfiguration.class,
    FixedTimeConfiguration.class})
class SupervisionOrderGenerationServiceTest extends AbstractOrderGenerationServiceTest {

    @Autowired
    private SupervisionOrderGenerationService service;

    @ParameterizedTest
    @MethodSource("docmosisDataGenerationSource")
    void shouldGetTemplateDataWhenGivenPopulatedCaseData(GeneratedOrderType type,
        GeneratedOrderSubtype subtype) {
        OrderStatus orderStatus = SEALED;
        CaseData caseData = createPopulatedCaseData(subtype, orderStatus);

        DocmosisGeneratedOrder templateData = service.getTemplateData(caseData);

        DocmosisGeneratedOrder expectedData = createExpectedDocmosisData(type, subtype, orderStatus);
        assertThat(templateData).isEqualToComparingFieldByField(expectedData);
    }

    @ParameterizedTest
    @MethodSource("docmosisDataGenerationSource")
    void shouldGetTemplateDataWhenGivenPopulatedCaseDataInDraft(GeneratedOrderType type,
        GeneratedOrderSubtype subtype) {
        OrderStatus orderStatus = DRAFT;
        CaseData caseData = createPopulatedCaseData(subtype, orderStatus);

        DocmosisGeneratedOrder templateData = service.getTemplateData(caseData);

        DocmosisGeneratedOrder expectedData = createExpectedDocmosisData(type, subtype, orderStatus);
        assertThat(templateData).isEqualToComparingFieldByField(expectedData);
    }

    @Override
    CaseDataBuilder populateCustomCaseData(GeneratedOrderSubtype subtype) {
        CaseDataBuilder caseDataBuilder = CaseData.builder()
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
                .endDate(time.now().toLocalDate())
                .build());
        }
        return caseDataBuilder;
    }

    @Override
    DocmosisGeneratedOrderBuilder populateCustomOrderFields(GeneratedOrderSubtype subtype) {
        String formattedDate = formatLocalDateToString(time.now().toLocalDate(), FormatStyle.LONG);

        ImmutableList<DocmosisChild> children = ImmutableList.of(
            DocmosisChild.builder()
                .name("Timmy Jones")
                .gender("Boy")
                .dateOfBirth(formattedDate).build());

        DocmosisGeneratedOrderBuilder<?,?> orderBuilder = DocmosisGeneratedOrder.builder();
        if (subtype == INTERIM) {
            String detailsDate = formatLocalDateToString(
                time.now().toLocalDate(), "d'" + getDayOfMonthSuffix(time.now().toLocalDate().getDayOfMonth())
                    + "' MMMM y");

            orderBuilder = createOrderBuilder("Interim supervision order",
                "Section 38 and Paragraphs 1 and 2 Schedule 3 Children Act 1989",
                String.format("It is ordered that Example Local Authority supervises"
                    + " the child until 11:59pm on the %s.", detailsDate), children);
        } else if (subtype == FINAL) {
            LocalDateTime expiryDate = time.now().plusMonths(5);
            final String formattedDateTime = formatLocalDateTimeBaseUsingFormat(expiryDate,
                "h:mma 'on the' d'" + getDayOfMonthSuffix(expiryDate.getDayOfMonth()) + "' MMMM y");

            orderBuilder = createOrderBuilder("Supervision order",
                "Section 31 and Paragraphs 1 and 2 Schedule 3 Children Act 1989",
                String.format("It is ordered that Example Local Authority supervises the child for 5 months "
                    + "from the date of this order until %s.", formattedDateTime), children);
        }
        return orderBuilder
            .orderType(SUPERVISION_ORDER);
    }

    private static Stream<Arguments> docmosisDataGenerationSource() {
        return Stream.of(
            Arguments.of(SUPERVISION_ORDER, INTERIM),
            Arguments.of(SUPERVISION_ORDER, FINAL)
        );
    }


}