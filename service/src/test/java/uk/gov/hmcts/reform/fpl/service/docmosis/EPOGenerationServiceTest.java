package uk.gov.hmcts.reform.fpl.service.docmosis;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.enums.GeneratedOrderSubtype;
import uk.gov.hmcts.reform.fpl.enums.OrderStatus;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CaseData.CaseDataBuilder;
import uk.gov.hmcts.reform.fpl.model.OrderTypeAndDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisGeneratedOrder;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisGeneratedOrder.DocmosisGeneratedOrderBuilder;
import uk.gov.hmcts.reform.fpl.model.emergencyprotectionorder.EPOChildren;
import uk.gov.hmcts.reform.fpl.model.emergencyprotectionorder.EPOPhrase;
import uk.gov.hmcts.reform.fpl.model.order.generated.FurtherDirections;
import uk.gov.hmcts.reform.fpl.service.CaseDataExtractionService;
import uk.gov.hmcts.reform.fpl.service.HearingVenueLookUpService;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.EPOType.REMOVE_TO_ACCOMMODATION;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.EMERGENCY_PROTECTION_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.DRAFT;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.SEALED;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {EPOGenerationService.class, CaseDataExtractionService.class,
    LookupTestConfig.class, HearingVenueLookUpService.class, JacksonAutoConfiguration.class,
    FixedTimeConfiguration.class})
class EPOGenerationServiceTest extends AbstractOrderGenerationServiceTest {
    @Autowired
    private EPOGenerationService service;

    @Test
    void shouldGetTemplateDataWhenGivenPopulatedCaseData() {
        OrderStatus orderStatus = SEALED;
        CaseData caseData = createPopulatedCaseData(orderStatus);

        DocmosisGeneratedOrder templateData = service.getTemplateData(caseData);

        DocmosisGeneratedOrder expectedData = createExpectedDocmosisData(EMERGENCY_PROTECTION_ORDER, orderStatus);
        assertThat(templateData).isEqualToComparingFieldByField(expectedData);
    }

    @Test
    void shouldGetTemplateDataWhenGivenPopulatedCaseDataInDraft() {
        OrderStatus orderStatus = DRAFT;
        CaseData caseData = createPopulatedCaseData(orderStatus);

        DocmosisGeneratedOrder templateData = service.getTemplateData(caseData);

        DocmosisGeneratedOrder expectedData = createExpectedDocmosisData(EMERGENCY_PROTECTION_ORDER, orderStatus);
        assertThat(templateData).isEqualToComparingFieldByField(expectedData);
    }

    @Override
    CaseDataBuilder populateCustomCaseData(GeneratedOrderSubtype subtype) {
        return CaseData.builder()
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
                .build())
            .orderAppliesToAllChildren(YES.getValue());
    }

    @SuppressWarnings("rawtypes")
    @Override
    DocmosisGeneratedOrderBuilder populateCustomOrderFields(GeneratedOrderSubtype subtype) {
        return DocmosisGeneratedOrder.builder()
            .orderType(EMERGENCY_PROTECTION_ORDER)
            .localAuthorityName(LOCAL_AUTHORITY_NAME)
            .children(getChildren())
            .childrenDescription("Test description")
            .epoType(REMOVE_TO_ACCOMMODATION)
            .includePhrase("Yes")
            .removalAddress("1 Main Street, Lurgan, BT66 7PP, Armagh, United Kingdom")
            .epoStartDateTime(formatLocalDateTimeBaseUsingFormat(time.now(), "d MMMM yyyy 'at' h:mma"))
            .epoEndDateTime(formatLocalDateTimeBaseUsingFormat(time.now(), "d MMMM yyyy 'at' h:mma"))
            .children(getChildren());
    }
}
