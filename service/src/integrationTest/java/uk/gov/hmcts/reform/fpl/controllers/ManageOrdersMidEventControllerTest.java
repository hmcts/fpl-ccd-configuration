package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.enums.EPOType;
import uk.gov.hmcts.reform.fpl.enums.HearingType;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.selector.Selector;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocmosisDocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.service.orders.generator.DocumentMerger;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.Objects.deepEquals;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_CODE;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.EPO;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.ORDER;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.DISTRICT_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.docmosis.RenderFormat.PDF;
import static uk.gov.hmcts.reform.fpl.enums.orders.SupervisionOrderEndDateType.SET_CALENDAR_DAY;
import static uk.gov.hmcts.reform.fpl.enums.orders.SupervisionOrderEndDateType.SET_NUMBER_OF_MONTHS;
import static uk.gov.hmcts.reform.fpl.model.common.DocumentReference.buildFromDocument;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C23_EMERGENCY_PROTECTION_ORDER;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C32_CARE_ORDER;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C35A_SUPERVISION_ORDER;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.asDynamicList;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocmosisDocument;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocument;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentBinaries;

@WebMvcTest(ManageOrdersController.class)
@OverrideAutoConfiguration(enabled = true)
class ManageOrdersMidEventControllerTest extends AbstractCallbackTest {

    @Autowired
    private Time time;

    private static final String FAMILY_MAN_CASE_NUMBER = "CASE_NUMBER";

    private static final Judge JUDGE = Judge.builder()
        .judgeLastName("Judy")
        .judgeTitle(DISTRICT_JUDGE)
        .build();

    private static final Child CHILD_1 = Child.builder()
        .party(ChildParty.builder().firstName("first1").lastName("last1").build())
        .build();
    private static final Child CHILD_2 = Child.builder()
        .party(ChildParty.builder().firstName("first2").lastName("last2").build())
        .build();
    private static final List<Element<Child>> CHILDREN = wrapElements(CHILD_1, CHILD_2);

    private static final byte[] DOCUMENT_BINARIES = testDocumentBinaries();
    private static final DocmosisDocument DOCMOSIS_DOCUMENT = testDocmosisDocument(DOCUMENT_BINARIES);
    private static final Document UPLOADED_DOCUMENT = testDocument();
    private static final DocumentReference DOCUMENT_REFERENCE = buildFromDocument(UPLOADED_DOCUMENT);
    private static final long CCD_CASE_NUMBER = 1234123412341234L;

    @MockBean
    private DocmosisDocumentGeneratorService docmosisGenerationService;

    @MockBean
    private DocumentMerger documentMerger;

    @MockBean
    private UploadDocumentService uploadService;

    ManageOrdersMidEventControllerTest() {
        super("manage-orders");
    }

    @Test
    void orderSelectionShouldPopulateQuestionConditionHolder() {
        CaseData caseData = CaseData.builder()
            .manageOrdersEventData(ManageOrdersEventData.builder().manageOrdersType(C32_CARE_ORDER).build())
            .build();

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(caseData, "order-selection");

        Map<String, String> expectedQuestions = Map.ofEntries(
            Map.entry("approver", "YES"),
            Map.entry("previewOrder", "YES"),
            Map.entry("furtherDirections", "YES"),
            Map.entry("orderDetails", "NO"),
            Map.entry("whichChildren", "YES"),
            Map.entry("approvalDate", "YES"),
            Map.entry("approvalDateTime", "NO"),
            Map.entry("epoIncludePhrase", "NO"),
            Map.entry("epoChildrenDescription", "NO"),
            Map.entry("epoExpiryDate", "NO"),
            Map.entry("epoTypeAndPreventRemoval", "NO"),
            Map.entry("supervisionOrderExpiryDate", "NO"),
            Map.entry("hearingDetails", "YES")
        );

        assertThat(response.getData().get("orderTempQuestions")).isEqualTo(expectedQuestions);
    }

    @Test
    void orderSelectionShouldPrePopulateFirstSectionDetails() throws JsonProcessingException {
        Element<HearingBooking> pastHearing = element(UUID.randomUUID(),
            HearingBooking.builder().type(HearingType.CASE_MANAGEMENT)
                .startDate(now().minusDays(2))
                .endDate(now().minusDays(2)).build());

        Element<HearingBooking> futureHearing = element(UUID.randomUUID(),
            HearingBooking.builder().type(HearingType.CASE_MANAGEMENT)
                .startDate(now().plusDays(2))
                .endDate(now().plusDays(2)).build());

        CaseData caseData = CaseData.builder()
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersType(C32_CARE_ORDER)
                .build())
            .hearingDetails(List.of(pastHearing, futureHearing))
            .build();

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(caseData, "order-selection");

        CaseData responseCaseData = extractCaseData(response);

        assertThat(response.getData().get("hearingDetailsSectionSubHeader")).isEqualTo("C32 - Care order");
        assertThat(responseCaseData.getManageOrdersEventData().getManageOrdersApprovedAtHearingList())
            .isEqualTo(
                asDynamicList(List.of(pastHearing), null, HearingBooking::toLabel)
            );
    }

    @Test
    void issuingDetailsShouldAutoPopulateApprovalDateWithCurrentDate() {
        CaseData caseData = CaseData.builder()
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersType(C35A_SUPERVISION_ORDER)
                .manageOrdersApprovalDate(dateNow())
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(caseData, "issuing-details");

        deepEquals(response.getData().get("manageOrdersApprovalDate"), dateNow());
    }

    @Test
    void issuingDetailsShouldValidateAgainstFutureApprovalDate() {
        CaseData caseData = CaseData.builder()
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersType(C32_CARE_ORDER)
                .manageOrdersApprovalDate(dateNow().plusDays(1))
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(caseData, "issuing-details");

        assertThat(response.getErrors()).containsOnly("Approval date cannot not be in the future");
    }

    @Test
    void issuingDetailsShouldPrepopulateNextSectionDetails() {
        final CaseData caseData = CaseData.builder()
            .children1(CHILDREN)
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersType(C32_CARE_ORDER)
                .manageOrdersApprovalDate(dateNow())
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(caseData, "issuing-details");

        CaseData responseCaseData = extractCaseData(response);

        assertThat(responseCaseData.getChildSelector()).isEqualTo(Selector.newSelector(2));
        assertThat(response.getData().get("children_label"))
            .isEqualTo("Child 1: first1 last1\nChild 2: first2 last2\n");

        assertThat(response.getData().get("childrenDetailsSectionSubHeader")).isEqualTo("C32 - Care order");
    }

    @Test
    void childrenDetailsShouldReturnErrorWhenNoChildrenSelected() {
        CaseData caseData = CaseData.builder()
            .orderAppliesToAllChildren("No")
            .childSelector(Selector.newSelector(2))
            .manageOrdersEventData(ManageOrdersEventData.builder().manageOrdersType(C32_CARE_ORDER).build())
            .build();

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(caseData, "children-details");

        assertThat(response.getErrors()).containsOnly("Select the children included in the order");
    }

    @Test
    void childrenDetailsShouldPrepopulateNextSectionDetails() {
        CaseData caseData = CaseData.builder()
            .orderAppliesToAllChildren("Yes")
            .manageOrdersEventData(ManageOrdersEventData.builder().manageOrdersType(C32_CARE_ORDER).build())
            .build();

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(caseData, "children-details");

        assertThat(response.getData().get("orderDetailsSectionSubHeader")).isEqualTo("C32 - Care order");
    }

    @Test
    void orderDetailsShouldPrepopulateNextSectionDetails() {
        CaseData caseData = CaseData.builder()
            .caseLocalAuthority(LOCAL_AUTHORITY_1_CODE)
            .id(CCD_CASE_NUMBER)
            .familyManCaseNumber(FAMILY_MAN_CASE_NUMBER)
            .children1(CHILDREN)
            .orderAppliesToAllChildren("Yes")
            .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder().useAllocatedJudge("Yes").build())
            .allocatedJudge(JUDGE)
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersType(C32_CARE_ORDER)
                .manageOrdersApprovalDate(dateNow())
                .build())
            .build();

        when(docmosisGenerationService.generateDocmosisDocument(anyMap(), eq(ORDER), eq(PDF)))
            .thenReturn(DOCMOSIS_DOCUMENT);
        when(uploadService.uploadDocument(DOCUMENT_BINARIES, "Preview order.pdf", "application/pdf"))
            .thenReturn(UPLOADED_DOCUMENT);

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(caseData, "order-details");

        Map<String, String> mappedDocument = mapper.convertValue(DOCUMENT_REFERENCE, new TypeReference<>() {
        });

        assertThat(response.getData().get("orderPreview")).isEqualTo(mappedDocument);
    }

    @Test
    void reviewShouldNotAlterCaseData() {
        CaseData caseData = CaseData.builder()
            .manageOrdersEventData(ManageOrdersEventData.builder().manageOrdersType(C32_CARE_ORDER).build())
            .build();

        CaseDetails caseDetails = asCaseDetails(caseData);

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(caseDetails, "review");

        assertThat(response.getData()).isEqualTo(caseDetails.getData());
    }

    @Test
    void epoEndDateShouldReturnErrorForPastDate() {
        CaseData caseData = buildCaseData().toBuilder().manageOrdersEventData(
            buildRemoveToAccommodationEventData(now().plusDays(1), now().minusDays(1))).build();

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(caseData, "order-details");

        assertThat(response.getErrors()).containsOnly("Enter an end date in the future");
    }

    @Test
    void epoEndDateShouldReturnErrorWhenEndDateIsNotInRangeWithApprovalDate() {
        final LocalDateTime approvalDate = LocalDateTime.now().minusDays(5);

        CaseData caseData = buildCaseData().toBuilder().manageOrdersEventData(
            buildRemoveToAccommodationEventData(approvalDate, approvalDate.plusDays(8).plusSeconds(1))).build();

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(caseData, "order-details");

        assertThat(response.getErrors()).containsOnly("Emergency protection orders cannot last longer than 8 days");
    }

    @Test
    void epoEndDateShouldReturnErrorWhenEndDateTimeIsMidnight() {
        final LocalDateTime endDateTime = LocalDateTime.of(dateNow().plusDays(1), LocalTime.MIDNIGHT);

        CaseData caseData = CaseData.builder().manageOrdersEventData(
            buildRemoveToAccommodationEventData(now(), endDateTime)).build();

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(caseData, "order-details");

        assertThat(response.getErrors()).containsOnly("Enter a valid time");
    }

    @Test
    void shouldNotReturnErrorsWhenEPOOrderDetailsAreValidForRemoveToAccommodation() {
        CaseData caseData = buildCaseData().toBuilder().manageOrdersEventData(
            buildRemoveToAccommodationEventData(now().minusDays(4), now().plusDays(1))).build();

        when(docmosisGenerationService.generateDocmosisDocument(anyMap(), eq(EPO), eq(PDF)))
            .thenReturn(DOCMOSIS_DOCUMENT);

        when(uploadService.uploadDocument(DOCUMENT_BINARIES, "Preview order.pdf", "application/pdf"))
            .thenReturn(UPLOADED_DOCUMENT);

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(caseData, "order-details");

        Map<String, String> mappedDocument = mapper.convertValue(DOCUMENT_REFERENCE, new TypeReference<>() {
        });

        assertThat(response.getErrors()).isEmpty();
        assertThat(response.getData().get("orderPreview")).isEqualTo(mappedDocument);
    }

    @Test
    void shouldNotReturnErrorsWhenEPOOrderDetailsAreValidForPreventRemoval() {
        CaseData caseData = buildCaseData().toBuilder().manageOrdersEventData(
            buildPreventRemovalEventData(Address.builder().addressLine1("test").postcode("SW").build()))
            .build();

        when(docmosisGenerationService.generateDocmosisDocument(anyMap(), eq(EPO), eq(PDF)))
            .thenReturn(DOCMOSIS_DOCUMENT);
        when(uploadService.uploadDocument(DOCUMENT_BINARIES, "Preview order.pdf", "application/pdf"))
            .thenReturn(UPLOADED_DOCUMENT);

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(caseData, "order-details");

        Map<String, String> mappedDocument = mapper.convertValue(DOCUMENT_REFERENCE, new TypeReference<>() {
        });

        assertThat(response.getErrors()).isEmpty();
        assertThat(response.getData().get("orderPreview")).isEqualTo(mappedDocument);
    }

    @Test
    void shouldThrowExceptionWhenMidEventUrlParameterDoesNotMatchSectionNames() {
        assertThatThrownBy(() -> postMidEvent(CaseData.builder().build(), "does-not-match"))
            .getRootCause()
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("No enum constant uk.gov.hmcts.reform.fpl.model.order.OrderSection.DOES_NOT_MATCH");
    }

    @Test
    void supervisionOrderEndDateShouldNotAllowCurrentDate() {
        final LocalDate testInvalidDate = dateNow().minusDays(1);
        final String testFutureDateMessage = "Enter an end date after the approval date";

        CaseData caseData = CaseData.builder()
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersApprovalDate(dateNow())
                .manageOrdersType(C35A_SUPERVISION_ORDER)
                .manageSupervisionOrderEndDateType(SET_CALENDAR_DAY)
                .manageOrdersSetDateEndDate(testInvalidDate)
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(caseData, "order-details");

        assertThat(response.getErrors()).containsOnly(testFutureDateMessage);
    }

    @Test
    void supervisionOrderNumberOfMonthsShouldNotAllowInvalidFutureDate() {
        final int testInvalidMonth = 16;
        final String testEndDateRangeMessage = "This order cannot last longer than 12 months";

        CaseData caseData = CaseData.builder()
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersType(C35A_SUPERVISION_ORDER)
                .manageSupervisionOrderEndDateType(SET_NUMBER_OF_MONTHS)
                .manageOrdersSetMonthsEndDate(testInvalidMonth)
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(caseData, "order-details");

        assertThat(response.getErrors()).containsOnly(testEndDateRangeMessage);
    }

    @Test
    void supervisionOrderNumberOfMonthsShouldNotAllowInvalidPastDate() {
        final int testInvalidMonth = -1;
        final String testUnderDateRangeMessage = "Supervision orders in months should be at least 1";

        CaseData caseData = CaseData.builder()
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersType(C35A_SUPERVISION_ORDER)
                .manageSupervisionOrderEndDateType(SET_NUMBER_OF_MONTHS)
                .manageOrdersSetMonthsEndDate(testInvalidMonth)
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(caseData, "order-details");

        deepEquals(response.getErrors(), testUnderDateRangeMessage);
    }

    private CaseData buildCaseData() {
        return CaseData.builder()
            .caseLocalAuthority(LOCAL_AUTHORITY_1_CODE)
            .id(CCD_CASE_NUMBER)
            .familyManCaseNumber(FAMILY_MAN_CASE_NUMBER)
            .children1(CHILDREN)
            .orderAppliesToAllChildren("Yes")
            .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder().useAllocatedJudge("Yes").build())
            .allocatedJudge(JUDGE)
            .build();
    }

    private ManageOrdersEventData buildRemoveToAccommodationEventData(
        LocalDateTime approvalDateTime, LocalDateTime endDateTime) {
        return ManageOrdersEventData.builder()
            .manageOrdersApprovalDateTime(approvalDateTime)
            .manageOrdersEndDateTime(endDateTime)
            .manageOrdersType(C23_EMERGENCY_PROTECTION_ORDER)
            .manageOrdersEpoType(EPOType.REMOVE_TO_ACCOMMODATION)
            .manageOrdersChildrenDescription("Children Description")
            .build();
    }

    private ManageOrdersEventData buildPreventRemovalEventData(Address removalAddress) {
        return ManageOrdersEventData.builder()
            .manageOrdersApprovalDateTime(now())
            .manageOrdersEndDateTime(now().plusDays(1))
            .manageOrdersType(C23_EMERGENCY_PROTECTION_ORDER)
            .manageOrdersEpoType(EPOType.PREVENT_REMOVAL)
            .manageOrdersEpoRemovalAddress(removalAddress)
            .manageOrdersChildrenDescription("Children Description")
            .manageOrdersExclusionRequirement("Yes")
            .manageOrdersWhoIsExcluded("John")
            .manageOrdersExclusionStartDate(dateNow().plusDays(2))
            .manageOrdersPowerOfArrest(DOCUMENT_REFERENCE)
            .build();
    }
}
