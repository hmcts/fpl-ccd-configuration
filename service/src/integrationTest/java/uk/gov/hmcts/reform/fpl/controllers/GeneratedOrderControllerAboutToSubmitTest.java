package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.enums.EPOExclusionRequirementType;
import uk.gov.hmcts.reform.fpl.enums.EPOType;
import uk.gov.hmcts.reform.fpl.enums.GeneratedEPOKey;
import uk.gov.hmcts.reform.fpl.enums.GeneratedOrderKey;
import uk.gov.hmcts.reform.fpl.enums.GeneratedOrderSubtype;
import uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType;
import uk.gov.hmcts.reform.fpl.enums.InterimOrderKey;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.enums.UploadedOrderType;
import uk.gov.hmcts.reform.fpl.enums.UserRole;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.CloseCase;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.OrderTypeAndDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisData;
import uk.gov.hmcts.reform.fpl.model.emergencyprotectionorder.EPOChildren;
import uk.gov.hmcts.reform.fpl.model.emergencyprotectionorder.EPOPhrase;
import uk.gov.hmcts.reform.fpl.model.order.generated.FurtherDirections;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.model.order.generated.InterimEndDate;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocmosisDocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;
import uk.gov.hmcts.reform.fpl.utils.TestDataHelper;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.Constants.DEFAULT_LA_COURT;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_CODE;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderSubtype.FINAL;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderSubtype.INTERIM;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.BLANK_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.CARE_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.EMERGENCY_PROTECTION_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.SUPERVISION_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HER_HONOUR_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HIS_HONOUR_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.State.CLOSED;
import static uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.InterimEndDateType.END_OF_PROCEEDINGS;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.DocumentManagementStoreLoader.document;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testChild;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testChildParty;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testChildren;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocmosisDocument;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testEmail;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testJudgeAndLegalAdviser;

@WebMvcTest(GeneratedOrderController.class)
@OverrideAutoConfiguration(enabled = true)
class GeneratedOrderControllerAboutToSubmitTest extends AbstractCallbackTest {

    private Document document;

    @MockBean
    private DocmosisDocumentGeneratorService docmosisDocumentGeneratorService;

    @MockBean
    private UploadDocumentService uploadDocumentService;

    GeneratedOrderControllerAboutToSubmitTest() {
        super("create-order");
    }

    @BeforeEach
    void setUp() {
        document = document();

        given(docmosisDocumentGeneratorService.generateDocmosisDocument(any(DocmosisData.class), any()))
            .willReturn(testDocmosisDocument(TestDataHelper.DOCUMENT_CONTENT));
        given(uploadDocumentService.uploadPDF(any(), any())).willReturn(document);
    }

    @Test
    void shouldAddC21OrderToCaseDataAndRemoveTemporaryCaseDataOrderFields() {

        final GeneratedOrder order = GeneratedOrder.builder()
            .title("Example Order")
            .details("Example order details here - Lorem ipsum dolor sit amet, consectetur adipiscing elit")
            .build();

        final CaseData caseData = commonCaseData(BLANK_ORDER, false)
            .order(order)
            .build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(caseData);

        GeneratedOrder expectedOrder = commonExpectedOrder(BLANK_ORDER.getLabel())
            .title(order.getTitle())
            .details(order.getDetails())
            .children(expectedChildren(caseData))
            .build();

        assertOrderInUpdatedCase(response, expectedOrder);
    }

    @Test
    void shouldNotHaveDraftAppendedToFilename() {
        final CaseData caseData = commonCaseData(CARE_ORDER, FINAL, false).build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(caseData);

        final CaseData updatedCaseData = extractCaseData(response);

        assertThat(updatedCaseData.getOrderCollection().get(0).getValue().getDocument()).isEqualTo(expectedDocument());
    }

    @Test
    void shouldAddCloseCaseObjectToCaseDataWhenOptionWasSelectedAndChangeState() {
        final CaseData caseData = commonCaseData(CARE_ORDER, FINAL, false)
            .orderAppliesToAllChildren("Yes")
            .closeCaseFromOrder("Yes")
            .build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(caseData);
        CloseCase closeCase = mapper.convertValue(response.getData().get("closeCaseTabField"), CloseCase.class);
        State state = mapper.convertValue(response.getData().get("state"), State.class);

        CloseCase expected = CloseCase.builder()
            .date(dateNow())
            .build();

        assertThat(closeCase).isEqualTo(expected);
        assertThat(state).isEqualTo(CLOSED);
    }

    @Test
    void shouldAddCloseCaseObjectToCaseDataWhenOptionWasNotSelected() {
        final CaseData caseData = commonCaseData(CARE_ORDER, FINAL, false)
            .children1(testChildren())
            .orderAppliesToAllChildren("Yes")
            .closeCaseFromOrder("No")
            .build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(caseData);

        assertThat(response.getData()).extracting("closeCaseTabField", "state").containsOnlyNulls();
    }

    @ParameterizedTest
    @EnumSource(GeneratedOrderSubtype.class)
    void shouldAddCareOrderToCaseDataAndRemoveTemporaryCaseDataOrderFields(GeneratedOrderSubtype subtype) {
        final CaseData caseData = commonCaseData(CARE_ORDER, subtype, false)
            .orderFurtherDirections(FurtherDirections.builder().directionsNeeded("No").build())
            .interimEndDate(InterimEndDate.builder().type(END_OF_PROCEEDINGS).build())
            .orderAppliesToAllChildren("Yes")
            .build();

        final AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(caseData);

        final String expiryDate = subtype == INTERIM
            ? "At the end of the proceedings, or until a further order is made" : null;
        final GeneratedOrder expectedCareOrder = commonExpectedOrder(format("%s care order", subtype.getLabel()))
            .expiryDate(expiryDate)
            .children(expectedChildren(caseData))
            .build();

        assertOrderInUpdatedCase(response, expectedCareOrder);
    }

    @Test
    void shouldAddInterimSupervisionOrderToCaseDataAndRemoveTemporaryCaseDataOrderFields() {
        final CaseData caseData = commonCaseData(SUPERVISION_ORDER, INTERIM, false)
            .orderFurtherDirections(FurtherDirections.builder().directionsNeeded("No").build())
            .interimEndDate(InterimEndDate.builder().type(END_OF_PROCEEDINGS).build())
            .build();

        final AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(caseData);

        final GeneratedOrder expectedSupervisionOrder = commonExpectedOrder("Interim supervision order")
            .expiryDate("At the end of the proceedings, or until a further order is made")
            .children(expectedChildren(caseData))
            .build();

        assertOrderInUpdatedCase(response, expectedSupervisionOrder);
    }

    @Test
    void shouldAddFinalSupervisionOrderToCaseDataAndRemoveTemporaryCaseDataOrderFields() {
        final CaseData caseData = commonCaseData(SUPERVISION_ORDER, FINAL, false)
            .orderFurtherDirections(FurtherDirections.builder().directionsNeeded("No").build())
            .orderMonths(14)
            .orderAppliesToAllChildren("Yes")
            .build();

        final AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(caseData);

        final LocalDateTime orderExpiration = now().plusMonths(14);
        final GeneratedOrder expectedSupervisionOrder = commonExpectedOrder("Final supervision order")
            .expiryDate(formatLocalDateTimeBaseUsingFormat(orderExpiration, "h:mma, d MMMM y"))
            .children(expectedChildren(caseData))
            .build();

        assertOrderInUpdatedCase(response, expectedSupervisionOrder);
    }

    @Test
    void shouldAddUploadedOrderToCaseDataAndRemoveTemporaryCaseDataOrderFields() {
        givenCurrentUser(UserDetails.builder()
            .roles(UserRole.HMCTS_ADMIN.getRoleNames())
            .build());

        final CaseData caseData = commonCaseData(UploadedOrderType.C27)
            .orderAppliesToAllChildren("Yes")
            .dateOfIssue(dateNow())
            .uploadedOrder(expectedDocument())
            .build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(caseData);

        GeneratedOrder expectedUploadedOrder = commonExpectedOrder(UploadedOrderType.C27.getFullLabel())
            .courtName(null)
            .judgeAndLegalAdvisor(null)
            .uploader("HMCTS")
            .build();

        assertOrderInUpdatedCase(response, expectedUploadedOrder);
    }

    @Test
    void shouldMigrateJudgeAndLegalAdvisorWhenUsingAllocatedJudge() {
        final CaseData caseData = commonCaseData(SUPERVISION_ORDER, FINAL, true)
            .orderFurtherDirections(FurtherDirections.builder().directionsNeeded("No").build())
            .orderMonths(14)
            .allocatedJudge(Judge.builder()
                .judgeTitle(HIS_HONOUR_JUDGE)
                .judgeLastName("Robinson")
                .judgeEmailAddress("robinson@example.com")
                .build())
            .build();

        CaseData updatedCaseData = extractCaseData(postAboutToSubmitEvent(caseData));

        GeneratedOrder generatedOrder = updatedCaseData.getOrderCollection().get(0).getValue();
        JudgeAndLegalAdvisor migratedJudge = generatedOrder.getJudgeAndLegalAdvisor();

        assertThat(migratedJudge.getJudgeTitle()).isEqualTo(HIS_HONOUR_JUDGE);
        assertThat(migratedJudge.getJudgeLastName()).isEqualTo("Robinson");
        assertThat(migratedJudge.getLegalAdvisorName()).isEqualTo("Peter Parker");
    }

    @Test
    void shouldSetFinalOrderIssuedOnSingleChild() {
        final CaseData caseData = commonCaseData(CARE_ORDER, FINAL, false)
            .children1(List.of(testChild()))
            .build();

        final CaseData updatedCaseData = extractCaseData(postAboutToSubmitEvent(caseData));

        assertThat(updatedCaseData.getAllChildren()).extracting(element -> element.getValue().getFinalOrderIssued())
            .containsOnly("Yes");
    }

    @Test
    void shouldSetFinalOrderIssuedOnChildren() {
        final CaseData caseData = commonCaseData(CARE_ORDER, FINAL, false)
            .orderAppliesToAllChildren("Yes")
            .build();

        final CaseData updatedCaseData = extractCaseData(postAboutToSubmitEvent(caseData));

        assertThat(updatedCaseData.getAllChildren()).extracting(element -> element.getValue().getFinalOrderIssued())
            .containsOnly("Yes");

        assertThat(updatedCaseData.getAllChildren()).extracting(element -> element.getValue().getFinalOrderIssuedType())
            .containsOnly("Care order");
    }

    @Test
    void shouldNotSetFinalOrderIssuedForInterimOrder() {
        final CaseData caseData = commonCaseData(CARE_ORDER, INTERIM, false)
            .children1(testChildren())
            .orderAppliesToAllChildren("Yes")
            .interimEndDate(InterimEndDate.builder().type(END_OF_PROCEEDINGS).build())
            .build();

        final CaseData updatedCaseData = extractCaseData(postAboutToSubmitEvent(caseData));

        assertThat(updatedCaseData.getAllChildren()).extracting(element -> element.getValue().getFinalOrderIssued())
            .containsOnlyNulls();

        assertThat(caseData.getAllChildren()).extracting(element -> element.getValue().getFinalOrderIssuedType())
            .containsOnlyNulls();
    }

    @Test
    void shouldNotSetFinalOrderIssuedForBlankOrder() {
        final CaseData caseData = commonCaseData(BLANK_ORDER, false)
            .orderAppliesToAllChildren("Yes")
            .order(GeneratedOrder.builder()
                .title("Example Order")
                .details("Example order details here - Lorem ipsum dolor sit amet, consectetur adipiscing elit")
                .build())
            .build();

        final CaseData updatedCaseData = extractCaseData(postAboutToSubmitEvent(caseData));

        assertThat(updatedCaseData.getAllChildren()).extracting(element -> element.getValue().getFinalOrderIssued())
            .containsOnlyNulls();
    }

    @Test
    void shouldSetFinalOrderIssuedForEmergencyProtectionOrder() {
        final CaseData caseData = commonCaseData(EMERGENCY_PROTECTION_ORDER, false)
            .dateOfIssue(null)
            .dateAndTimeOfIssue(now())
            .orderAppliesToAllChildren("Yes")
            .epoChildren(EPOChildren.builder().descriptionNeeded("No").build())
            .epoType(EPOType.PREVENT_REMOVAL)
            .epoPhrase(EPOPhrase.builder().includePhrase("PHRASE").build())
            .epoEndDate(now().plusDays(5))
            .epoExclusionRequirementType(EPOExclusionRequirementType.NO_TO_EXCLUSION)
            .build();

        final CaseData updatedCaseData = extractCaseData(postAboutToSubmitEvent(caseData));

        assertThat(updatedCaseData.getAllChildren()).extracting(element -> element.getValue().getFinalOrderIssued())
            .containsOnly("Yes");
    }

    @Test
    void shouldSetFinalOrderIssuedForUploadedEducationSupervisionOrder() {
        givenCurrentUser(UserDetails.builder()
            .roles(UserRole.HMCTS_ADMIN.getRoleNames())
            .build());

        CaseData caseData = commonCaseData(UploadedOrderType.C37)
            .orderAppliesToAllChildren("Yes")
            .build();

        final CaseData updatedCaseData = extractCaseData(postAboutToSubmitEvent(caseData));

        assertThat(updatedCaseData.getAllChildren()).extracting(element -> element.getValue().getFinalOrderIssued())
            .containsOnly("Yes");
    }

    private JudgeAndLegalAdvisor buildJudgeAndLegalAdvisor(YesNo useAllocatedJudge) {
        return testJudgeAndLegalAdviser().toBuilder()
            .useAllocatedJudge(useAllocatedJudge.getValue())
            .build();
    }

    private CaseData.CaseDataBuilder commonCaseData(GeneratedOrderType orderType,
                                                    boolean allocatedJudge) {
        return commonCaseData(orderType, null, null, allocatedJudge);
    }

    private CaseData.CaseDataBuilder commonCaseData(GeneratedOrderType orderType,
                                                    GeneratedOrderSubtype subtype,
                                                    boolean allocatedJudge) {
        return commonCaseData(orderType, subtype, null, allocatedJudge);
    }

    private CaseData.CaseDataBuilder commonCaseData(UploadedOrderType uploadedOrderType) {
        return commonCaseData(GeneratedOrderType.UPLOAD, null, uploadedOrderType, false);
    }

    private CaseData.CaseDataBuilder commonCaseData(GeneratedOrderType orderType,
                                                    GeneratedOrderSubtype subtype,
                                                    UploadedOrderType uploadedOrderType,
                                                    boolean allocatedJudge) {

        ChildParty child1 = testChildParty().toBuilder().fathersName("Smith").build();
        ChildParty child2 = testChildParty().toBuilder().email(testEmail()).build();
        ChildParty child3 = testChildParty();

        return CaseData.builder().orderTypeAndDocument(
            OrderTypeAndDocument.builder()
                .type(orderType)
                .subtype(subtype)
                .uploadedOrderType(uploadedOrderType)
                .document(DocumentReference.builder().build())
                .build())
            .judgeAndLegalAdvisor(buildJudgeAndLegalAdvisor(YesNo.from(allocatedJudge)))
            .familyManCaseNumber("12345L")
            .id(1234123412341234L)
            .children1(Stream.of(child1, child2, child3)
                .map(party -> Child.builder().party(party).build())
                .map(ElementUtils::element)
                .collect(toList()))
            .caseLocalAuthority(LOCAL_AUTHORITY_1_CODE)
            .dateOfIssue(dateNow());
    }

    private GeneratedOrder.GeneratedOrderBuilder commonExpectedOrder(String fullType) {
        return GeneratedOrder.builder()
            .type(fullType)
            .dateOfIssue(formatLocalDateTimeBaseUsingFormat(now(), "d MMMM yyyy"))
            .document(expectedDocument())
            .date(formatLocalDateTimeBaseUsingFormat(now(), "h:mma, d MMMM yyyy"))
            .judgeAndLegalAdvisor(
                JudgeAndLegalAdvisor.builder()
                    .judgeTitle(HER_HONOUR_JUDGE)
                    .judgeLastName("Judy")
                    .legalAdvisorName("Peter Parker")
                    .build())
            .courtName(DEFAULT_LA_COURT);
    }

    private DocumentReference expectedDocument() {
        return DocumentReference.builder()
            .binaryUrl(document.links.binary.href)
            .filename("file.pdf")
            .url(document.links.self.href)
            .build();
    }

    private void assertOrderInUpdatedCase(AboutToStartOrSubmitCallbackResponse response, GeneratedOrder expectedOrder) {
        List<String> keys = stream(GeneratedOrderKey.values()).map(GeneratedOrderKey::getKey).collect(toList());
        keys.addAll(stream(GeneratedEPOKey.values()).map(GeneratedEPOKey::getKey).collect(toList()));
        keys.addAll(stream(InterimOrderKey.values()).map(InterimOrderKey::getKey).collect(toList()));

        assertThat(response.getData()).doesNotContainKeys(keys.toArray(String[]::new));

        CaseData caseData = extractCaseData(response);

        assertThat(caseData.getOrderCollection().get(0).getValue()).isEqualTo(expectedOrder);
    }

    private List<Element<Child>> expectedChildren(CaseData caseData) {
        final OrderTypeAndDocument currentOrder = caseData.getOrderTypeAndDocument();
        final List<Element<Child>> children = caseData.getAllChildren();

        return children.stream().map(child -> element(child.getId(), Child.builder()
            .finalOrderIssued(currentOrder.isFinal() ? "Yes" : null)
            .party(ChildParty.builder()
                .firstName(child.getValue().getParty().getFirstName())
                .lastName(child.getValue().getParty().getLastName())
                .dateOfBirth(child.getValue().getParty().getDateOfBirth())
                .gender(child.getValue().getParty().getGender())
                .build())
            .build()))
            .collect(toList());
    }
}
