package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.HearingType;
import uk.gov.hmcts.reform.fpl.enums.ManageDocumentTypeLA;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CourtBundle;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.ManageDocumentLA;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.enums.ManageDocumentTypeLA.C2;
import static uk.gov.hmcts.reform.fpl.enums.ManageDocumentTypeLA.CORRESPONDENCE;
import static uk.gov.hmcts.reform.fpl.enums.ManageDocumentTypeLA.COURT_BUNDLE;
import static uk.gov.hmcts.reform.fpl.enums.ManageDocumentTypeLA.FURTHER_EVIDENCE_DOCUMENTS;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.service.ManageDocumentLAService.COURT_BUNDLE_HEARING_LIST_KEY;
import static uk.gov.hmcts.reform.fpl.service.ManageDocumentLAService.COURT_BUNDLE_KEY;
import static uk.gov.hmcts.reform.fpl.service.ManageDocumentLAService.MANAGE_DOCUMENT_LA_KEY;
import static uk.gov.hmcts.reform.fpl.service.ManageDocumentService.TEMP_EVIDENCE_DOCUMENTS_COLLECTION_KEY;
import static uk.gov.hmcts.reform.fpl.service.ManageHearingsService.HEARING_DETAILS_KEY;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.asDynamicList;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

@ActiveProfiles("integration-test")
@WebMvcTest(ManageDocumentsLAController.class)
@OverrideAutoConfiguration(enabled = true)
public class ManageDocumentsLAControllerAboutToSubmitTest extends AbstractControllerTest {

    private static final String USER = "LA";

    ManageDocumentsLAControllerAboutToSubmitTest() {
        super("manage-documents-la");
    }

    @Autowired
    private Time time;

    @MockBean
    private IdamClient idamClient;

    @BeforeEach
    void init() {
        given(idamClient.getUserDetails(eq(USER_AUTH_TOKEN))).willReturn(createUserDetailsWithLARole());
    }

    @Test
    void shouldPopulateHearingFurtherDocumentsCollection() {
        List<Element<SupportingEvidenceBundle>> furtherEvidenceBundle = buildSupportingEvidenceBundle();
        UUID hearingId = UUID.randomUUID();
        HearingBooking hearingBooking = buildFinalHearingBooking();

        Map<String, Object> data = new HashMap<>(Map.of(
            "hearingDetails", List.of(element(hearingId, hearingBooking)),
            "manageDocumentsHearingList", DynamicList.builder()
                .value(DynamicListElement.builder()
                    .code(hearingId)
                    .build())
                .build(),
            TEMP_EVIDENCE_DOCUMENTS_COLLECTION_KEY, furtherEvidenceBundle,
            MANAGE_DOCUMENT_LA_KEY,
            buildManagementDocument(FURTHER_EVIDENCE_DOCUMENTS, YES.getValue())));

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetailsBefore(buildCaseDetails(Map.of()))
            .caseDetails(buildCaseDetails(data))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(callbackRequest);
        CaseData caseData = mapper.convertValue(callbackResponse.getData(), CaseData.class);

        assertThat(caseData.getHearingFurtherEvidenceDocuments().get(0).getValue()
            .getSupportingEvidenceBundle()).isEqualTo(furtherEvidenceBundle);

        assertExpectedFieldsAreRemoved(caseData);
    }

    @Test
    void shouldPopulateFurtherEvidenceDocumentsCollection() {
        List<Element<SupportingEvidenceBundle>> furtherEvidenceBundle = buildSupportingEvidenceBundle();

        Map<String, Object> data = new HashMap<>(Map.of(
            TEMP_EVIDENCE_DOCUMENTS_COLLECTION_KEY, furtherEvidenceBundle,
            MANAGE_DOCUMENT_LA_KEY, buildManagementDocument(FURTHER_EVIDENCE_DOCUMENTS, NO.getValue())));

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetailsBefore(buildCaseDetails(Map.of()))
            .caseDetails(buildCaseDetails(data))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(callbackRequest);
        CaseData caseData = mapper.convertValue(callbackResponse.getData(), CaseData.class);

        assertThat(caseData.getFurtherEvidenceDocumentsLA()).isEqualTo(furtherEvidenceBundle);
        assertExpectedFieldsAreRemoved(caseData);
    }

    @Test
    void shouldPopulateCourtBundleCollection() {
        UUID hearingId = UUID.randomUUID();
        HearingBooking hearingBooking = buildFinalHearingBooking();
        CourtBundle courtBundle = buildCourtBundle();

        Map<String, Object> data = new HashMap<>(Map.of(
            HEARING_DETAILS_KEY, List.of(element(hearingId, hearingBooking)),
            COURT_BUNDLE_HEARING_LIST_KEY, DynamicList.builder()
                .value(DynamicListElement.builder()
                    .code(hearingId)
                    .build())
                .build(),
            COURT_BUNDLE_KEY, courtBundle,
            MANAGE_DOCUMENT_LA_KEY, buildManagementDocument(COURT_BUNDLE)));

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(buildCaseDetails(data))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(callbackRequest);
        CaseData caseData = mapper.convertValue(callbackResponse.getData(), CaseData.class);

        assertThat(caseData.getCourtBundleList().get(0).getValue()).isEqualTo(courtBundle);
        assertThat(caseData.getCourtBundleList().get(0).getId()).isEqualTo(hearingId);

        assertExpectedFieldsAreRemoved(caseData);
    }

    @Test
    void shouldPopulateCorrespondenceEvidenceCollection() {
        List<Element<SupportingEvidenceBundle>> furtherEvidenceBundle = buildSupportingEvidenceBundle();

        Map<String, Object> data = new HashMap<>(Map.of(
            TEMP_EVIDENCE_DOCUMENTS_COLLECTION_KEY, furtherEvidenceBundle,
            MANAGE_DOCUMENT_LA_KEY, buildManagementDocument(CORRESPONDENCE)));

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetailsBefore(buildCaseDetails(Map.of()))
            .caseDetails(buildCaseDetails(data))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(callbackRequest);
        CaseData caseData = mapper.convertValue(callbackResponse.getData(), CaseData.class);

        assertThat(caseData.getCorrespondenceDocumentsLA()).isEqualTo(furtherEvidenceBundle);
        assertExpectedFieldsAreRemoved(caseData);
    }

    @Test
    void shouldPopulateC2DocumentBundleCollection() {
        UUID selectedC2DocumentId = UUID.randomUUID();
        C2DocumentBundle selectedC2DocumentBundle = buildC2DocumentBundle(time.now().plusDays(2));
        List<Element<SupportingEvidenceBundle>> supportingEvidenceBundle
            = buildSupportingEvidenceBundle(time.now().plusDays(3));

        List<Element<C2DocumentBundle>> c2DocumentBundleList = List.of(
            element(buildC2DocumentBundle(time.now().plusDays(2))),
            element(selectedC2DocumentId, selectedC2DocumentBundle),
            element(buildC2DocumentBundle(time.now().plusDays(2))));

        AtomicInteger i = new AtomicInteger(1);
        DynamicList expectedC2DocumentsDynamicList = asDynamicList(c2DocumentBundleList, selectedC2DocumentId,
            documentBundle -> documentBundle.toLabel(i.getAndIncrement()));

        Map<String, Object> data = new HashMap<>(Map.of(
            "c2DocumentBundle", c2DocumentBundleList,
            "manageDocumentsSupportingC2List", expectedC2DocumentsDynamicList,
            "c2SupportingDocuments", supportingEvidenceBundle,
            MANAGE_DOCUMENT_LA_KEY, buildManagementDocument(C2)));

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetailsBefore(buildCaseDetails(Map.of()))
            .caseDetails(buildCaseDetails(data))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(callbackRequest);
        CaseData caseData = mapper.convertValue(callbackResponse.getData(), CaseData.class);

        assertThat(caseData.getC2DocumentBundle().get(0)).isEqualTo(c2DocumentBundleList.get(0));
        assertExpectedFieldsAreRemoved(caseData);
    }

    private CaseDetails buildCaseDetails(Map<String, Object> caseData) {
        return CaseDetails.builder().data(caseData).build();
    }

    private HearingBooking buildFinalHearingBooking() {
        return HearingBooking.builder()
            .type(HearingType.FINAL)
            .startDate(LocalDateTime.now())
            .build();
    }

    private ManageDocumentLA buildManagementDocument(ManageDocumentTypeLA type) {
        return ManageDocumentLA.builder().type(type).build();
    }

    private ManageDocumentLA buildManagementDocument(ManageDocumentTypeLA type, String isRelatedToHearing) {
        return buildManagementDocument(type).toBuilder().relatedToHearing(isRelatedToHearing).build();
    }

    private List<Element<SupportingEvidenceBundle>> buildSupportingEvidenceBundle() {
        return wrapElements(SupportingEvidenceBundle.builder()
            .dateTimeUploaded(LocalDateTime.now())
            .uploadedBy(USER)
            .name("test")
            .build());
    }

    private List<Element<SupportingEvidenceBundle>> buildSupportingEvidenceBundle(LocalDateTime localDateTime) {
        return wrapElements(SupportingEvidenceBundle.builder()
            .name("test")
            .dateTimeUploaded(localDateTime)
            .uploadedBy(USER)
            .build());
    }

    private void assertExpectedFieldsAreRemoved(CaseData caseData) {
        assertThat(caseData.getSupportingEvidenceDocumentsTemp()).isEmpty();
        assertThat(caseData.getManageDocumentLA()).isNull();
        assertThat(caseData.getManageDocumentsCourtBundle()).isNull();
        assertThat(caseData.getC2SupportingDocuments()).isNull();
        assertThat(caseData.getManageDocumentsHearingList()).isNull();
        assertThat(caseData.getManageDocumentsSupportingC2List()).isNull();
    }

    private C2DocumentBundle buildC2DocumentBundle(LocalDateTime dateTime) {
        return C2DocumentBundle.builder().uploadedDateTime(dateTime.toString()).build();
    }

    private CourtBundle buildCourtBundle() {
        return CourtBundle.builder()
            .hearing("string")
            .document(testDocumentReference())
            .documentRedacted(testDocumentReference())
            .uploadedBy("Kurt")
            .build();
    }

    private UserDetails createUserDetailsWithLARole() {
        return UserDetails.builder()
            .id(USER_ID)
            .surname("Swansea")
            .forename("Kurt")
            .email("kurt@swansea.gov.uk")
            .roles(List.of("caseworker-publiclaw-solicitor"))
            .build();
    }
}
