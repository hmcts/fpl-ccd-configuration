package uk.gov.hmcts.reform.fpl.service.docmosis;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.enums.EPOExclusionRequirementType;
import uk.gov.hmcts.reform.fpl.enums.EPOType;
import uk.gov.hmcts.reform.fpl.enums.OrderStatus;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.OrderTypeAndDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisGeneratedOrder;
import uk.gov.hmcts.reform.fpl.model.emergencyprotectionorder.EPOChildren;
import uk.gov.hmcts.reform.fpl.model.emergencyprotectionorder.EPOPhrase;
import uk.gov.hmcts.reform.fpl.model.order.generated.FurtherDirections;
import uk.gov.hmcts.reform.fpl.selectors.ChildrenSmartSelector;
import uk.gov.hmcts.reform.fpl.service.CaseDataExtractionService;
import uk.gov.hmcts.reform.fpl.service.ChildrenService;
import uk.gov.hmcts.reform.fpl.service.HearingVenueLookUpService;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;
import uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper;
import uk.gov.hmcts.reform.fpl.utils.ChildSelectionUtils;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.EPOType.PREVENT_REMOVAL;
import static uk.gov.hmcts.reform.fpl.enums.EPOType.REMOVE_TO_ACCOMMODATION;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.EMERGENCY_PROTECTION_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.DRAFT;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.SEALED;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE_TIME_AT;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {EPOGenerationService.class, CaseDataExtractionService.class,
    LookupTestConfig.class, HearingVenueLookUpService.class, JacksonAutoConfiguration.class,
    FixedTimeConfiguration.class, ChildrenSmartSelector.class, ChildrenService.class, CaseDetailsHelper.class,
    ChildSelectionUtils.class})
class EPOGenerationServiceTest extends AbstractOrderGenerationServiceTest {
    @Autowired
    private EPOGenerationService service;

    @Test
    void shouldGetTemplateDataWhenGivenPopulatedCaseData() {
        OrderStatus orderStatus = SEALED;
        CaseData caseData = getCase(orderStatus);

        DocmosisGeneratedOrder templateData = service.getTemplateData(caseData);

        DocmosisGeneratedOrder expectedData = getExpectedDocument(orderStatus);
        assertThat(templateData).isEqualToComparingFieldByField(expectedData);
    }

    @Test
    void shouldGetTemplateDataWhenGivenPopulatedCaseDataInDraft() {
        OrderStatus orderStatus = DRAFT;
        CaseData caseData = getCase(orderStatus);

        DocmosisGeneratedOrder templateData = service.getTemplateData(caseData);

        DocmosisGeneratedOrder expectedData = getExpectedDocument(orderStatus);
        assertThat(templateData).isEqualToComparingFieldByField(expectedData);
    }

    @Test
    void shouldVerifyExclusionRequirementWhenTheStartDateIsSame() {
        CaseData caseData = getCaseWithEpoExclusionRequirement(DRAFT,
            EPOExclusionRequirementType.STARTING_ON_SAME_DATE,
            LocalDate.of(2021, 1, 13),
            "Test User", PREVENT_REMOVAL);

        DocmosisGeneratedOrder templateData = service.populateCustomOrderFields(caseData);

        assertThat(templateData.getExclusionRequirement()).isEqualTo("The Court directs that Test User be excluded"
            + " from 1 Main Street, Lurgan, BT66 7PP, Armagh, United Kingdom forthwith so that the child may "
            + "continue to live there, consent to the exclusion requirement having been given by Test User.");
    }

    @Test
    void shouldVerifyExclusionRequirementWhenTheStartDateIsDifferent() {
        CaseData caseData = getCaseWithEpoExclusionRequirement(DRAFT,
            EPOExclusionRequirementType.STARTING_ON_DIFFERENT_DATE,
            LocalDate.of(2021, 1, 26),
            "Test User", PREVENT_REMOVAL);


        DocmosisGeneratedOrder templateData = service.populateCustomOrderFields(caseData);

        assertThat(templateData.getExclusionRequirement()).isEqualTo("The Court directs that Test User be excluded"
            + " from 1 Main Street, Lurgan, BT66 7PP, Armagh, United Kingdom "
            + "from 26 January 2021 so that the child may "
            + "continue to live there, consent to the exclusion requirement having been given by Test User.");
    }

    @Test
    void shouldVerifyExclusionRequirementWhenNoToExclusionHasBeenSelected() {
        CaseData caseData = getCaseWithEpoExclusionRequirement(DRAFT,
            EPOExclusionRequirementType.NO_TO_EXCLUSION,
            LocalDate.of(2021, 1, 13),
            "Temp User", PREVENT_REMOVAL);

        DocmosisGeneratedOrder templateData = service.populateCustomOrderFields(caseData);

        assertThat(templateData.getExclusionRequirement()).isNull();
    }

    @Test
    void shouldVerifyExclusionRequirementWhenRemoveToAccommodationHasBeenSelected() {
        CaseData caseData = getCaseWithEpoExclusionRequirement(DRAFT,
            EPOExclusionRequirementType.STARTING_ON_SAME_DATE,
            LocalDate.of(2021, 1, 13),
            "Temp User", REMOVE_TO_ACCOMMODATION);

        DocmosisGeneratedOrder templateData = service.populateCustomOrderFields(caseData);

        assertThat(templateData.getExclusionRequirement()).isNull();
    }

    CaseData getCase(OrderStatus orderStatus) {
        return defaultCaseData(orderStatus)
            .dateOfIssue(null)
            .dateAndTimeOfIssue(time.now())
            .orderTypeAndDocument(OrderTypeAndDocument.builder()
                .type(EMERGENCY_PROTECTION_ORDER)
                .document(DocumentReference.builder().build())
                .build())
            .epoChildren(EPOChildren.builder()
                .descriptionNeeded("Yes")
                .description("Test description")
                .build())
            .epoEndDate(time.now().plusDays(5))
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
            .orderAppliesToAllChildren(YES.getValue())
            .build();
    }

    CaseData getCaseWithEpoExclusionRequirement(OrderStatus orderStatus,
                                                EPOExclusionRequirementType epoExclusionRequirementType,
                                                LocalDate epoExclusionStartDate,
                                                String whoIsExcluded, EPOType epoType) {
        return defaultCaseData(orderStatus)
            .dateOfIssue(null)
            .dateAndTimeOfIssue(time.now())
            .orderTypeAndDocument(OrderTypeAndDocument.builder()
                .type(EMERGENCY_PROTECTION_ORDER)
                .document(DocumentReference.builder().build())
                .build())
            .epoChildren(EPOChildren.builder()
                .descriptionNeeded("Yes")
                .description("Test description")
                .build())
            .epoEndDate(time.now().plusDays(5))
            .epoPhrase(EPOPhrase.builder()
                .includePhrase("Yes")
                .build())
            .epoType(epoType)
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
            .orderAppliesToAllChildren(YES.getValue())
            .epoExclusionRequirementType(epoExclusionRequirementType)
            .epoWhoIsExcluded(whoIsExcluded)
            .epoExclusionStartDate(epoExclusionStartDate)
            .build();
    }

    private DocmosisGeneratedOrder getExpectedDocument(OrderStatus orderStatus) {
        DocmosisGeneratedOrder orderBuilder = DocmosisGeneratedOrder.builder()
            .orderType(EMERGENCY_PROTECTION_ORDER)
            .localAuthorityName(LOCAL_AUTHORITY_NAME)
            .children(getChildren())
            .childrenDescription("Test description")
            .epoType(REMOVE_TO_ACCOMMODATION)
            .includePhrase("Yes")
            .removalAddress("1 Main Street, Lurgan, BT66 7PP, Armagh, United Kingdom")
            .epoStartDateTime(formatLocalDateTimeBaseUsingFormat(time.now(), DATE_TIME_AT))
            .epoEndDateTime(formatLocalDateTimeBaseUsingFormat(time.now().plusDays(5), DATE_TIME_AT))
            .children(getChildren())
            .build();

        return enrichWithStandardData(EMERGENCY_PROTECTION_ORDER, orderStatus, orderBuilder);
    }
}
