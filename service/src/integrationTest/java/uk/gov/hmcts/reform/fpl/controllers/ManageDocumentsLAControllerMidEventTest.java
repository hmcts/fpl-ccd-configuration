package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.ManageDocumentTypeLA;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CourtBundle;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.HearingFurtherEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.ManageDocumentLA;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.ManageDocumentTypeLA.CORRESPONDENCE;
import static uk.gov.hmcts.reform.fpl.enums.ManageDocumentTypeLA.COURT_BUNDLE;
import static uk.gov.hmcts.reform.fpl.enums.ManageDocumentTypeLA.FURTHER_EVIDENCE_DOCUMENTS;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.service.ManageDocumentLAService.COURT_BUNDLE_HEARING_LIST_KEY;
import static uk.gov.hmcts.reform.fpl.service.ManageDocumentLAService.COURT_BUNDLE_LIST_KEY;
import static uk.gov.hmcts.reform.fpl.service.ManageDocumentLAService.MANAGE_DOCUMENT_LA_KEY;
import static uk.gov.hmcts.reform.fpl.service.ManageDocumentService.CORRESPONDING_DOCUMENTS_COLLECTION_KEY;
import static uk.gov.hmcts.reform.fpl.service.ManageDocumentService.HEARING_FURTHER_EVIDENCE_DOCUMENTS_COLLECTION_KEY;
import static uk.gov.hmcts.reform.fpl.service.ManageDocumentService.MANAGE_DOCUMENTS_HEARING_LABEL_KEY;
import static uk.gov.hmcts.reform.fpl.service.ManageDocumentService.MANAGE_DOCUMENTS_HEARING_LIST_KEY;
import static uk.gov.hmcts.reform.fpl.service.ManageDocumentService.SUPPORTING_C2_LIST_KEY;
import static uk.gov.hmcts.reform.fpl.service.ManageDocumentService.TEMP_EVIDENCE_DOCUMENTS_COLLECTION_KEY;
import static uk.gov.hmcts.reform.fpl.service.ManageHearingsService.HEARING_DETAILS_KEY;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createHearingBooking;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ActiveProfiles("integration-test")
@WebMvcTest(ManageDocumentsController.class)
@OverrideAutoConfiguration(enabled = true)
public class ManageDocumentsLAControllerMidEventTest extends AbstractControllerTest {
    ManageDocumentsLAControllerMidEventTest() {
        super("manage-documents-la");
    }

    @Test
    void shouldInitialiseFurtherEvidenceCollection() {
        UUID selectedHearingId = randomUUID();
        LocalDateTime today = LocalDateTime.now();
        HearingBooking selectedHearingBooking = createHearingBooking(today, today.plusDays(3));
        List<Element<SupportingEvidenceBundle>> furtherEvidenceBundle = buildSupportingEvidenceBundle();

        List<Element<HearingBooking>> hearingBookings = List.of(
            element(createHearingBooking(today.plusDays(5), today.plusDays(6))),
            element(createHearingBooking(today.plusDays(2), today.plusDays(3))),
            element(createHearingBooking(today, today.plusDays(1))),
            Element.<HearingBooking>builder().id(selectedHearingId).value(selectedHearingBooking).build());

        Map<String, Object> data = new HashMap<>(Map.of(
            MANAGE_DOCUMENTS_HEARING_LIST_KEY, selectedHearingId,
            HEARING_DETAILS_KEY, hearingBookings,
            HEARING_FURTHER_EVIDENCE_DOCUMENTS_COLLECTION_KEY, List.of(
                element(selectedHearingId, HearingFurtherEvidenceBundle.builder()
                    .supportingEvidenceBundle(furtherEvidenceBundle)
                    .build())),
            MANAGE_DOCUMENT_LA_KEY, buildManagementDocument(FURTHER_EVIDENCE_DOCUMENTS, YES.getValue())));

        CaseDetails caseDetails = buildCaseDetails(data);

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails,
            "initialise-manage-document-collections");

        CaseData caseData = mapper.convertValue(callbackResponse.getData(), CaseData.class);

        DynamicList expectedDynamicList = ElementUtils
            .asDynamicList(hearingBookings, selectedHearingId, HearingBooking::toLabel);

        DynamicList hearingList
            = mapper.convertValue(callbackResponse.getData().get(MANAGE_DOCUMENTS_HEARING_LIST_KEY),
            DynamicList.class);

        assertThat(hearingList).isEqualTo(expectedDynamicList);

        assertThat(callbackResponse.getData().get(MANAGE_DOCUMENTS_HEARING_LABEL_KEY))
            .isEqualTo(selectedHearingBooking.toLabel());

        assertThat(caseData.getSupportingEvidenceDocumentsTemp()).isEqualTo(furtherEvidenceBundle);
    }

    @Test
    void shouldInitialiseCorrespondenceCollection() {
        List<Element<SupportingEvidenceBundle>> correspondenceDocuments = buildSupportingEvidenceBundle();

        Map<String, Object> data = new HashMap<>(Map.of(
            CORRESPONDING_DOCUMENTS_COLLECTION_KEY, correspondenceDocuments,
            MANAGE_DOCUMENT_LA_KEY, buildManagementDocument(CORRESPONDENCE)));

        CaseDetails caseDetails = buildCaseDetails(data);

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails,
            "initialise-manage-document-collections");

        CaseData caseData = mapper.convertValue(callbackResponse.getData(), CaseData.class);
        assertThat(caseData.getCorrespondenceDocuments()).isEqualTo(correspondenceDocuments);
    }

    @Test
    void shouldInitialiseCourtBundleCollection() {
        UUID selectedHearingId = randomUUID();
        LocalDateTime today = LocalDateTime.now();
        HearingBooking selectedHearingBooking = createHearingBooking(today, today.plusDays(3));

        List<Element<CourtBundle>> courtBundle = buildCourtBundleList(selectedHearingId);

        List<Element<HearingBooking>> hearingBookings = List.of(
            element(createHearingBooking(today.plusDays(5), today.plusDays(6))),
            element(createHearingBooking(today.plusDays(2), today.plusDays(3))),
            element(createHearingBooking(today, today.plusDays(1))),
            Element.<HearingBooking>builder().id(selectedHearingId).value(selectedHearingBooking).build());

        DynamicList hearingList = ElementUtils
            .asDynamicList(hearingBookings, selectedHearingId, HearingBooking::toLabel);

        Map<String, Object> data = new HashMap<>(Map.of(
            HEARING_DETAILS_KEY, hearingBookings,
            COURT_BUNDLE_LIST_KEY, courtBundle,
            COURT_BUNDLE_HEARING_LIST_KEY, hearingList,
            MANAGE_DOCUMENT_LA_KEY, buildManagementDocument(COURT_BUNDLE)));

        CaseDetails caseDetails = buildCaseDetails(data);

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails,
            "initialise-manage-document-collections");

        CaseData caseData = mapper.convertValue(callbackResponse.getData(), CaseData.class);
        assertThat(caseData.getCourtBundleList()).isEqualTo(courtBundle);
    }

    @Test
    void shouldInitialiseC2SupportingDocuments() {
        UUID selectedC2DocumentId = UUID.randomUUID();
        LocalDateTime today = LocalDateTime.now();

        List<Element<SupportingEvidenceBundle>> c2EvidenceDocuments = buildSupportingEvidenceBundle();

        List<Element<C2DocumentBundle>> c2DocumentBundle = List.of(
            element(buildC2DocumentBundle(today.plusDays(2))),
            element(selectedC2DocumentId, buildC2DocumentBundle(c2EvidenceDocuments)),
            element(buildC2DocumentBundle(today.plusDays(2))));

        Map<String, Object> data = new HashMap<>(Map.of(
            "c2DocumentBundle", c2DocumentBundle,
            MANAGE_DOCUMENT_LA_KEY, buildManagementDocument(ManageDocumentTypeLA.C2),
            SUPPORTING_C2_LIST_KEY, selectedC2DocumentId));

        CaseDetails caseDetails = buildCaseDetails(data);

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails,
            "initialise-manage-document-collections");

        CaseData caseData = mapper.convertValue(callbackResponse.getData(), CaseData.class);
        assertThat(caseData.getSupportingEvidenceDocumentsTemp()).isEqualTo(c2EvidenceDocuments);
    }

    @Test
    void shouldReturnValidationErrorsIfSupportingEvidenceDateTimeReceivedOnFurtherEvidenceIsInTheFuture() {
        LocalDateTime futureDate = LocalDateTime.now().plusDays(1);
        CaseDetails caseDetails = buildCaseDetails(TEMP_EVIDENCE_DOCUMENTS_COLLECTION_KEY, futureDate);
        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails,
            "validate-supporting-evidence");

        assertThat(callbackResponse.getErrors()).isNotEmpty();
        assertThat(callbackResponse.getErrors()).containsExactly("Date received cannot be in the future");
    }

    @Test
    void shouldReturnNoValidationErrorsIfSupportingEvidenceDateTimeReceivedOnFurtherEvidenceIsInThePast() {
        LocalDateTime pastDate = LocalDateTime.now().minusDays(2);
        CaseDetails caseDetails = buildCaseDetails(TEMP_EVIDENCE_DOCUMENTS_COLLECTION_KEY, pastDate);
        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails,
            "validate-supporting-evidence");

        assertThat(callbackResponse.getErrors()).isEmpty();
    }

    private CaseDetails buildCaseDetails(Map<String, Object> caseData) {
        return CaseDetails.builder().data(caseData).build();
    }

    private CaseDetails buildCaseDetails(String key, LocalDateTime dateTimeReceived) {
        return buildCaseDetails(Map.of(key, List.of(
            element(SupportingEvidenceBundle.builder().dateTimeReceived(dateTimeReceived).build()))));
    }

    private ManageDocumentLA buildManagementDocument(ManageDocumentTypeLA type) {
        return ManageDocumentLA.builder().type(type).build();
    }

    private ManageDocumentLA buildManagementDocument(ManageDocumentTypeLA type, String isRelatedToHearing) {
        return buildManagementDocument(type).toBuilder().relatedToHearing(isRelatedToHearing).build();
    }

    private List<Element<SupportingEvidenceBundle>> buildSupportingEvidenceBundle() {
        return wrapElements(SupportingEvidenceBundle.builder().name("test").build());
    }

    private List<Element<CourtBundle>> buildCourtBundleList(UUID hearingId) {
        return List.of(element(hearingId, CourtBundle.builder().hearing("test hearing").build()));
    }

    private C2DocumentBundle buildC2DocumentBundle(LocalDateTime dateTime) {
        return C2DocumentBundle.builder().uploadedDateTime(dateTime.toString()).build();
    }

    private C2DocumentBundle buildC2DocumentBundle(List<Element<SupportingEvidenceBundle>> supportingEvidenceBundle) {
        return buildC2DocumentBundle(now()).toBuilder()
            .supportingEvidenceBundle(supportingEvidenceBundle)
            .build();
    }
}
