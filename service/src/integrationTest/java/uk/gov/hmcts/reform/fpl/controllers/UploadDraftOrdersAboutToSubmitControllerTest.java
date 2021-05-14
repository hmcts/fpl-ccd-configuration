package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.controllers.orders.UploadDraftOrdersController;
import uk.gov.hmcts.reform.fpl.enums.CMOStatus;
import uk.gov.hmcts.reform.fpl.enums.CMOType;
import uk.gov.hmcts.reform.fpl.enums.HearingOrderType;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.HearingFurtherEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.event.UploadDraftOrdersData;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrdersBundle;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.DRAFT;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.SEND_TO_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.HearingOrderKind.CMO;
import static uk.gov.hmcts.reform.fpl.enums.HearingOrderType.AGREED_CMO;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.formatJudgeTitleAndName;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

@WebMvcTest(UploadDraftOrdersController.class)
@OverrideAutoConfiguration(enabled = true)
class UploadDraftOrdersAboutToSubmitControllerTest extends AbstractUploadDraftOrdersControllerTest {

    private static final DocumentReference DOCUMENT_REFERENCE = testDocumentReference();

    @MockBean
    private FeatureToggleService featureToggleService;

    UploadDraftOrdersAboutToSubmitControllerTest() {
        super();
    }

    @Test
    void shouldAddCMOToListWithDraftStatusAndNotMigrateDocs() {
        List<Element<SupportingEvidenceBundle>> bundles = List.of(element(
            SupportingEvidenceBundle.builder()
                .name("case summary")
                .build()
        ));

        List<Element<HearingBooking>> hearings = hearingsOnDateAndDayAfter(now().plusDays(3));

        UploadDraftOrdersData eventData = UploadDraftOrdersData.builder()
            .hearingOrderDraftKind(List.of(CMO))
            .futureHearingsForCMO(dynamicList(hearings))
            .uploadedCaseManagementOrder(DOCUMENT_REFERENCE)
            .cmoUploadType(CMOType.DRAFT)
            .cmoSupportingDocs(bundles)
            .build();

        CaseData caseData = CaseData.builder()
            .uploadDraftOrdersEventData(eventData)
            .hearingDetails(hearings)
            .build();

        CaseData responseData = extractCaseData(postAboutToSubmitEvent(caseData));

        HearingOrder cmo = orderWithDocs(hearings.get(0).getValue(), HearingOrderType.DRAFT_CMO, DRAFT, bundles);

        List<Element<HearingOrder>> unsealedCMOs = responseData.getDraftUploadedCMOs();

        assertThat(unsealedCMOs)
            .hasSize(1)
            .first()
            .extracting(Element::getValue)
            .isEqualTo(cmo);

        hearings.get(0).getValue().setCaseManagementOrderId(unsealedCMOs.get(0).getId());

        assertThat(responseData.getHearingDetails()).isEqualTo(hearings);

        assertThat(responseData.getHearingFurtherEvidenceDocuments()).isEmpty();
    }

    @Test
    void shouldAddCMOToListWithSendToJudgeStatusAndMigrateDocs() {
        givenCurrentUser(UserDetails.builder()
            .email("Test LA")
            .roles(List.of("caseworker-publiclaw-solicitor"))
            .build());

        UUID bundleId = UUID.randomUUID();
        List<Element<SupportingEvidenceBundle>> cmoBundles = List.of(element(bundleId,
            SupportingEvidenceBundle.builder()
                .name("case summary")
                .build()));

        List<Element<SupportingEvidenceBundle>> hearingDocsBundles = List.of(element(bundleId,
            SupportingEvidenceBundle.builder()
                .name("case summary")
                .uploadedBy("Test LA")
                .dateTimeUploaded(now())
                .build()));

        List<Element<HearingBooking>> hearings = hearingsOnDateAndDayAfter(now().minusDays(3));

        UploadDraftOrdersData eventData = UploadDraftOrdersData.builder()
            .hearingOrderDraftKind(List.of(CMO))
            .pastHearingsForCMO(dynamicList(hearings))
            .uploadedCaseManagementOrder(DOCUMENT_REFERENCE)
            .cmoUploadType(CMOType.AGREED)
            .cmoSupportingDocs(cmoBundles)
            .build();

        CaseData caseData = CaseData.builder()
            .uploadDraftOrdersEventData(eventData)
            .hearingDetails(hearings)
            .build();

        CaseData responseData = extractCaseData(postAboutToSubmitEvent(caseData));

        HearingOrder cmo = orderWithDocs(hearings.get(0).getValue(), AGREED_CMO, SEND_TO_JUDGE, cmoBundles);

        List<Element<HearingOrder>> unsealedCMOs = responseData.getDraftUploadedCMOs();

        assertThat(unsealedCMOs)
            .hasSize(1)
            .first()
            .extracting(Element::getValue)
            .isEqualTo(cmo);

        hearings.get(0).getValue().setCaseManagementOrderId(unsealedCMOs.get(0).getId());

        assertThat(responseData.getHearingDetails()).isEqualTo(hearings);

        List<Element<HearingFurtherEvidenceBundle>> furtherEvidenceBundle = List.of(
            element(hearings.get(0).getId(), HearingFurtherEvidenceBundle.builder()
                .hearingName(hearings.get(0).getValue().toLabel())
                .supportingEvidenceBundle(hearingDocsBundles)
                .build())
        );

        assertThat(responseData.getHearingFurtherEvidenceDocuments())
            .hasSize(1)
            .isEqualTo(furtherEvidenceBundle);
    }

    @Test
    void shouldReplaceSupportingDocumentInDraftCMOWhenANewSupportingDocumentIsUploaded() {
        UUID cmoId = UUID.randomUUID();

        Element<SupportingEvidenceBundle> existingSupportingDoc = element(
            SupportingEvidenceBundle.builder()
                .name("case summary doc1")
                .build()
        );

        Element<SupportingEvidenceBundle> newSupportingDoc = element(
            SupportingEvidenceBundle.builder()
                .name("case summary doc2")
                .build()
        );

        List<Element<HearingBooking>> hearings = List.of(hearingWithCMOId(now().plusDays(3), cmoId));
        Element<HearingOrder> cmoElement = element(cmoId,
            orderWithDocs(
                hearings.get(0).getValue(), HearingOrderType.DRAFT_CMO, DRAFT, newArrayList(existingSupportingDoc)));

        UploadDraftOrdersData eventData = UploadDraftOrdersData.builder()
            .previousCMO(cmoElement.getValue().getOrder())
            .hearingOrderDraftKind(List.of(CMO))
            .futureHearingsForCMO(dynamicLists.from(0,
                Pair.of(hearings.get(0).getValue().toLabel(), hearings.get(0).getId())))
            .uploadedCaseManagementOrder(DOCUMENT_REFERENCE)
            .cmoUploadType(CMOType.DRAFT)
            .cmoSupportingDocs(List.of(newSupportingDoc))
            .build();

        CaseData caseData = CaseData.builder()
            .uploadDraftOrdersEventData(eventData)
            .hearingDetails(hearings)
            .draftUploadedCMOs(newArrayList(cmoElement))
            .hearingOrdersBundlesDrafts(List.of(element(UUID.randomUUID(), HearingOrdersBundle.builder()
                .hearingName(hearings.get(0).getValue().toLabel())
                .orders(newArrayList(cmoElement))
                .build())))
            .build();

        CaseData responseData = extractCaseData(postAboutToSubmitEvent(caseData));

        HearingOrder cmo = orderWithDocs(
            hearings.get(0).getValue(), HearingOrderType.DRAFT_CMO, DRAFT,
            List.of(newSupportingDoc));

        List<Element<HearingOrder>> unsealedCMOs = responseData.getDraftUploadedCMOs();

        assertThat(unsealedCMOs)
            .hasSize(1)
            .first()
            .extracting(Element::getValue)
            .isEqualTo(cmo);

        hearings.get(0).getValue().setCaseManagementOrderId(unsealedCMOs.get(0).getId());

        assertThat(responseData.getHearingDetails()).isEqualTo(hearings);

        assertThat(responseData.getHearingFurtherEvidenceDocuments()).isEmpty();
    }

    @Test
    void shouldRemoveTemporaryFields() {
        List<Element<HearingBooking>> hearings = hearingsOnDateAndDayAfter(LocalDateTime.of(2020, 3, 15, 10, 7));
        List<Element<HearingOrder>> draftCMOs = List.of();

        CaseData caseData = CaseData.builder()
            .uploadDraftOrdersEventData(UploadDraftOrdersData.builder()
                .hearingOrderDraftKind(List.of(CMO))
                .uploadedCaseManagementOrder(DOCUMENT_REFERENCE)
                .pastHearingsForCMO(dynamicList(hearings))
                .futureHearingsForCMO("DUMMY DATA")
                .cmoJudgeInfo("DUMMY DATA")
                .cmoHearingInfo("DUMMY DATA")
                .showReplacementCMO(YesNo.NO)
                .replacementCMO(DOCUMENT_REFERENCE)
                .previousCMO(DOCUMENT_REFERENCE)
                .cmoSupportingDocs(List.of())
                .cmoToSend(DOCUMENT_REFERENCE)
                .showCMOsSentToJudge(YesNo.NO)
                .cmosSentToJudge("DUMMY DATA")
                .cmoUploadType(CMOType.DRAFT)
                .build())
            .hearingDetails(hearings)
            .draftUploadedCMOs(draftCMOs)
            .build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(asCaseDetails(caseData));

        Set<String> keys = mapper.convertValue(caseData, new TypeReference<Map<String, Object>>() {
        }).keySet();

        keys.removeAll(List.of(
            "showCMOsSentToJudge", "cmosSentToJudge", "cmoUploadType", "pastHearingsForCMO", "futureHearingsForCMO",
            "cmoHearingInfo", "showReplacementCMO", "previousCMO", "uploadedCaseManagementOrder", "replacementCMO",
            "cmoSupportingDocs", "cmoJudgeInfo", "cmoToSend", "hearingsForHearingOrderDrafts",
            "currentHearingOrderDrafts", "hearingOrderDraftKind"
        ));

        assertThat(response.getData().keySet()).isEqualTo(keys);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldUpdateDocumentViews(boolean isFurtherEvidenceTabEnabled) {
        List<Element<HearingBooking>> hearings = hearingsOnDateAndDayAfter(LocalDateTime.of(2020, 3, 15, 10, 7));
        List<Element<HearingOrder>> draftCMOs = List.of();
        List<Element<HearingFurtherEvidenceBundle>> furtherEvidenceBundle = getFurtherEvidenceBundle(hearings);

        CaseData caseData = CaseData.builder()
            .uploadDraftOrdersEventData(UploadDraftOrdersData.builder()
                .hearingOrderDraftKind(List.of(CMO))
                .uploadedCaseManagementOrder(DOCUMENT_REFERENCE)
                .pastHearingsForCMO(dynamicList(hearings))
                .futureHearingsForCMO("DUMMY DATA")
                .cmoJudgeInfo("DUMMY DATA")
                .cmoHearingInfo("DUMMY DATA")
                .showReplacementCMO(YesNo.NO)
                .replacementCMO(DOCUMENT_REFERENCE)
                .previousCMO(DOCUMENT_REFERENCE)
                .cmoSupportingDocs(List.of())
                .cmoToSend(DOCUMENT_REFERENCE)
                .showCMOsSentToJudge(YesNo.NO)
                .cmosSentToJudge("DUMMY DATA")
                .cmoUploadType(CMOType.DRAFT).build())
            .hearingDetails(hearings)
            .hearingFurtherEvidenceDocuments(furtherEvidenceBundle)
            .draftUploadedCMOs(draftCMOs)
            .build();

        given(featureToggleService.isFurtherEvidenceDocumentTabEnabled()).willReturn(isFurtherEvidenceTabEnabled);

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(asCaseDetails(caseData));

        if (isFurtherEvidenceTabEnabled) {
            assertThat((String) response.getData().get("documentViewLA")).isNotEmpty();
            assertThat((String) response.getData().get("documentViewHMCTS")).isNotEmpty();
            assertThat((String) response.getData().get("documentViewNC")).isNotEmpty();
            assertThat(response.getData().get("showFurtherEvidenceTab")).isEqualTo("YES");
        } else {
            assertThat((String) response.getData().get("documentViewLA")).isNull();
            assertThat((String) response.getData().get("documentViewHMCTS")).isNull();
            assertThat((String) response.getData().get("documentViewNC")).isNull();
            assertThat((String) response.getData().get("showFurtherEvidenceTab")).isNull();
        }
    }

    private List<Element<HearingFurtherEvidenceBundle>> getFurtherEvidenceBundle(
        List<Element<HearingBooking>> hearings) {
        List<Element<SupportingEvidenceBundle>> hearingDocsBundles = List.of(element(UUID.randomUUID(),
            SupportingEvidenceBundle.builder()
                .name("case summary")
                .uploadedBy("Test LA")
                .document(testDocumentReference())
                .dateTimeUploaded(now())
                .build()));

        return List.of(element(hearings.get(0).getId(), HearingFurtherEvidenceBundle.builder()
            .hearingName(hearings.get(0).getValue().toLabel())
            .supportingEvidenceBundle(hearingDocsBundles)
            .build())
        );
    }

    private HearingOrder orderWithDocs(HearingBooking hearing, HearingOrderType type, CMOStatus status,
                                       List<Element<SupportingEvidenceBundle>> supportingDocs) {
        return HearingOrder.builder()
            .type(type)
            .title(type == AGREED_CMO ? "Agreed CMO discussed at hearing" : "Draft CMO from advocates' meeting")
            .status(status)
            .hearing(hearing.toLabel())
            .order(DOCUMENT_REFERENCE)
            .dateSent(dateNow())
            .judgeTitleAndName(formatJudgeTitleAndName(hearing.getJudgeAndLegalAdvisor()))
            .supportingDocs(supportingDocs)
            .build();
    }

    private DynamicList dynamicList(List<Element<HearingBooking>> hearings) {
        return dynamicLists.from(0,
            Pair.of(hearings.get(0).getValue().toLabel(), hearings.get(0).getId()),
            Pair.of(hearings.get(1).getValue().toLabel(), hearings.get(1).getId())
        );
    }

    private List<Element<HearingBooking>> hearingsOnDateAndDayAfter(LocalDateTime startDate) {
        return List.of(
            hearing(startDate),
            hearing(startDate.plusDays(1))
        );
    }
}
