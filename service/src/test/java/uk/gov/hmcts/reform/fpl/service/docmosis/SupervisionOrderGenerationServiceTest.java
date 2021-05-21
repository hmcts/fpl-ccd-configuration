package uk.gov.hmcts.reform.fpl.service.docmosis;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.enums.GeneratedOrderSubtype;
import uk.gov.hmcts.reform.fpl.enums.OrderStatus;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CaseData.CaseDataBuilder;
import uk.gov.hmcts.reform.fpl.model.OrderTypeAndDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisChild;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisGeneratedOrder;
import uk.gov.hmcts.reform.fpl.model.order.generated.FurtherDirections;
import uk.gov.hmcts.reform.fpl.model.order.generated.InterimEndDate;
import uk.gov.hmcts.reform.fpl.model.order.selector.Selector;
import uk.gov.hmcts.reform.fpl.service.CaseDataExtractionService;
import uk.gov.hmcts.reform.fpl.service.ChildrenService;
import uk.gov.hmcts.reform.fpl.service.HearingVenueLookUpService;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;
import uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import java.time.format.FormatStyle;
import java.util.List;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderSubtype.FINAL;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderSubtype.INTERIM;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.SUPERVISION_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.DRAFT;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.SEALED;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.InterimEndDateType.NAMED_DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.getDayOfMonthSuffix;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {SupervisionOrderGenerationService.class, CaseDataExtractionService.class,
    LookupTestConfig.class, HearingVenueLookUpService.class, JacksonAutoConfiguration.class,
    FixedTimeConfiguration.class, ChildrenService.class, CaseDetailsHelper.class})
class SupervisionOrderGenerationServiceTest extends AbstractOrderGenerationServiceTest {

    @Autowired
    private SupervisionOrderGenerationService service;

    @ParameterizedTest
    @EnumSource(GeneratedOrderSubtype.class)
    void shouldGetTemplateDataWhenGivenPopulatedCaseData(GeneratedOrderSubtype subtype) {
        OrderStatus orderStatus = SEALED;
        CaseData caseData = getCase(subtype, orderStatus);

        DocmosisGeneratedOrder templateData = service.getTemplateData(caseData);

        DocmosisGeneratedOrder expectedData = getExpectedDocument(subtype, orderStatus);
        assertThat(templateData).isEqualToComparingFieldByField(expectedData);
    }

    @ParameterizedTest
    @EnumSource(GeneratedOrderSubtype.class)
    void shouldGetTemplateDataWhenGivenPopulatedCaseDataInDraft(GeneratedOrderSubtype subtype) {
        OrderStatus orderStatus = DRAFT;
        CaseData caseData = getCase(subtype, orderStatus);

        DocmosisGeneratedOrder templateData = service.getTemplateData(caseData);

        DocmosisGeneratedOrder expectedData = getExpectedDocument(subtype, orderStatus);
        assertThat(templateData).isEqualToComparingFieldByField(expectedData);
    }

    CaseData getCase(GeneratedOrderSubtype subtype, OrderStatus orderStatus) {
        CaseDataBuilder caseDataBuilder = defaultCaseData(subtype, orderStatus)
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
            .childSelector(Selector.builder()
                .selected(List.of(0))
                .build());

        if (subtype == INTERIM) {
            caseDataBuilder.interimEndDate(InterimEndDate.builder()
                .type(NAMED_DATE)
                .endDate(time.now().toLocalDate())
                .build());
        }
        return caseDataBuilder.build();
    }

    private DocmosisGeneratedOrder getExpectedDocument(GeneratedOrderSubtype subtype, OrderStatus orderStatus) {
        String formattedDate = formatLocalDateToString(time.now().toLocalDate(), FormatStyle.LONG);

        List<DocmosisChild> children = List.of(
            DocmosisChild.builder()
                .name("Timmy Jones")
                .gender("Boy")
                .dateOfBirth(formattedDate).build());

        var orderBuilder = DocmosisGeneratedOrder.builder().children(children);

        if (subtype == INTERIM) {
            String detailsDate = formatLocalDateToString(
                time.now().toLocalDate(), "d'" + getDayOfMonthSuffix(time.now().toLocalDate().getDayOfMonth())
                    + "' MMMM y");

            orderBuilder
                .orderTitle("Interim supervision order")
                .childrenAct("Section 38 and Paragraphs 1 and 2 Schedule 3 Children Act 1989")
                .orderDetails(format("It is ordered that %s supervises the child until 11:59pm on the %s.",
                    LOCAL_AUTHORITY_NAME, detailsDate));
        } else if (subtype == FINAL) {
            orderBuilder
                .orderTitle("Supervision order")
                .childrenAct("Section 31 and Paragraphs 1 and 2 Schedule 3 Children Act 1989")
                .orderDetails(format("It is ordered that %s supervises the child for 5 months "
                    + "from the date of this order.", LOCAL_AUTHORITY_NAME));
        }
        return enrichWithStandardData(SUPERVISION_ORDER, orderStatus, orderBuilder.build());
    }

}
