package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.HearingType;
import uk.gov.hmcts.reform.fpl.enums.ManageDocumentType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.HearingFurtherEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.ManageDocument;
import uk.gov.hmcts.reform.fpl.model.ManageDocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.ManageDocumentType.FURTHER_EVIDENCE_DOCUMENTS;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.service.ManageDocumentService.FURTHER_EVIDENCE_DOCUMENTS_COLLECTION_KEY;
import static uk.gov.hmcts.reform.fpl.service.ManageDocumentService.HEARING_FURTHER_EVIDENCE_DOCUMENTS_COLLECTION_KEY;
import static uk.gov.hmcts.reform.fpl.service.ManageDocumentService.MANAGE_DOCUMENTS_HEARING_LABEL_KEY;
import static uk.gov.hmcts.reform.fpl.service.ManageDocumentService.MANAGE_DOCUMENTS_HEARING_LIST_KEY;
import static uk.gov.hmcts.reform.fpl.service.ManageDocumentService.TEMP_FURTHER_EVIDENCE_DOCUMENTS_COLLECTION_KEY;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createHearingBooking;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {JacksonAutoConfiguration.class, FixedTimeConfiguration.class})
public class ManageDocumentServiceTest {
    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private Time time;

    private ManageDocumentService manageDocumentService;
    private LocalDateTime futureDate;

    private static final String MANAGE_DOCUMENT_KEY = "manageDocument";

    @BeforeEach
    void before() {
        manageDocumentService = new ManageDocumentService(mapper, time);
        futureDate = time.now().plusDays(1);
    }

    @Test
    void shouldFullyPopulateFurtherEvidenceFields() {
        UUID selectHearingId = randomUUID();
        HearingBooking selectedHearingBooking = createHearingBooking(futureDate, futureDate.plusDays(3));

        List<Element<HearingBooking>> hearingBookings = List.of(
            element(createHearingBooking(futureDate.plusDays(5), futureDate.plusDays(6))),
            element(createHearingBooking(futureDate.plusDays(2), futureDate.plusDays(3))),
            element(createHearingBooking(futureDate, futureDate.plusDays(1))),
            Element.<HearingBooking>builder().id(selectHearingId).value(selectedHearingBooking).build());

        Map<String, Object> data = new HashMap<>(Map.of(
            MANAGE_DOCUMENTS_HEARING_LIST_KEY, selectHearingId,
            "hearingDetails", hearingBookings,
            MANAGE_DOCUMENT_KEY, buildManagementDocument(FURTHER_EVIDENCE_DOCUMENTS, YES.getValue())));

        CaseDetails caseDetails = CaseDetails.builder().data(data).build();
        manageDocumentService.initialiseFurtherEvidenceFields(caseDetails);

        DynamicList expectedDynamicList = ElementUtils
            .asDynamicList(hearingBookings, selectHearingId, hearingBooking -> hearingBooking.toLabel(DATE));

        assertThat(caseDetails.getData().get(MANAGE_DOCUMENTS_HEARING_LIST_KEY))
            .isEqualTo(expectedDynamicList);
        assertThat(caseDetails.getData().get(MANAGE_DOCUMENTS_HEARING_LABEL_KEY));
    }

    @Test
    void shouldExpandFurtherEvidenceCollectionWhenEmpty() {
        List<Element<ManageDocumentBundle>> emptyManageDocumentCollection = List.of();
        List<Element<ManageDocumentBundle>> manageDocumentBundleCollection
            = manageDocumentService.initialiseManageDocumentBundleCollection(emptyManageDocumentCollection);

        assertThat(manageDocumentBundleCollection).isNotEmpty();
    }

    @Test
    void shouldPersistExistingFurtherEvidenceDocumentBundleWhenExisting() {
        List<Element<ManageDocumentBundle>> furtherEvidenceBundle = buildManagementDocumentBundle();
        List<Element<ManageDocumentBundle>> updatedEvidenceBundle =
            manageDocumentService.initialiseManageDocumentBundleCollection(furtherEvidenceBundle);

        assertThat(updatedEvidenceBundle).isEqualTo(furtherEvidenceBundle);
    }

    @Test
    void shouldReturnEmptyCollectionWhenFurtherEvidenceIsNotRelatedToHearingAndCollectionIsNotPresent() {
        Map<String, Object> data = new HashMap<>(Map.of(
            MANAGE_DOCUMENT_KEY, buildManagementDocument(FURTHER_EVIDENCE_DOCUMENTS, NO.getValue())));

        CaseDetails caseDetails = CaseDetails.builder().data(data).build();
        List<Element<ManageDocumentBundle>> furtherDocumentBundleCollection =
            manageDocumentService.initialiseFurtherDocumentBundleCollection(caseDetails);

        assertThat(furtherDocumentBundleCollection).isNotEmpty();
        assertThat(furtherDocumentBundleCollection.get(0).getValue()).isEqualTo(ManageDocumentBundle.builder().build());
    }

    @Test
    void shouldReturnFurtherEvidenceCollectionWhenFurtherEvidenceIsNotRelatedToHearingAndCollectionIsPresent() {
        List<Element<ManageDocumentBundle>> furtherEvidenceBundle = buildManagementDocumentBundle();

        Map<String, Object> data = new HashMap<>(Map.of(
            FURTHER_EVIDENCE_DOCUMENTS_COLLECTION_KEY, furtherEvidenceBundle,
            MANAGE_DOCUMENT_KEY, buildManagementDocument(FURTHER_EVIDENCE_DOCUMENTS, NO.getValue())));

        CaseDetails caseDetails = CaseDetails.builder().data(data).build();
        List<Element<ManageDocumentBundle>> furtherDocumentBundleCollection =
            manageDocumentService.initialiseFurtherDocumentBundleCollection(caseDetails);

        assertThat(furtherDocumentBundleCollection).isEqualTo(furtherEvidenceBundle);
    }

    @Test
    void shouldReturnHearingEvidenceCollectionWhenFurtherEvidenceIsRelatedToHearingWithExistingEntryInCollection() {
        List<Element<ManageDocumentBundle>> furtherEvidenceBundle = buildManagementDocumentBundle();
        UUID hearingId = UUID.randomUUID();
        HearingBooking hearingBooking = buildFinalHearingBooking();

        Map<String, Object> data = new HashMap<>(Map.of(
            "hearingDetails", List.of(element(hearingId, hearingBooking)),
            "manageDocumentsHearingList", hearingId,
            HEARING_FURTHER_EVIDENCE_DOCUMENTS_COLLECTION_KEY, List.of(
                element(hearingId, HearingFurtherEvidenceBundle.builder()
                    .manageDocumentBundle(furtherEvidenceBundle)
                    .build())),
            MANAGE_DOCUMENT_KEY, buildManagementDocument(FURTHER_EVIDENCE_DOCUMENTS, YES.getValue())));

        CaseDetails caseDetails = CaseDetails.builder().data(data).build();
        List<Element<ManageDocumentBundle>> furtherDocumentBundleCollection =
            manageDocumentService.initialiseFurtherDocumentBundleCollection(caseDetails);

        assertThat(furtherDocumentBundleCollection).isEqualTo(furtherEvidenceBundle);
    }

    @Test
    void shouldSetDateTimeUploadedToNewCorrespondenceCollectionItems() {
        LocalDateTime yesterday = time.now().minusDays(1);
        List<Element<ManageDocumentBundle>> correspondingDocuments = buildManagementDocumentBundle();
        correspondingDocuments.add(element(ManageDocumentBundle.builder()
                .dateTimeUploaded(yesterday)
                .build()));

        List<Element<ManageDocumentBundle>> updatedCorrespondingDocuments
            = manageDocumentService.setDateTimeUploadedOnManageDocumentCollection(correspondingDocuments, List.of());

        List<ManageDocumentBundle> manageDocumentBundle = unwrapElements(updatedCorrespondingDocuments);

        assertThat(manageDocumentBundle.get(0).getDateTimeUploaded()).isEqualTo(time.now());
        assertThat(manageDocumentBundle.get(1).getDateTimeUploaded()).isEqualTo(yesterday);
    }

    @Test
    void shouldSetNewDateTimeUploadedOnOverwriteOfPreviousDocumentUpload() {
        LocalDateTime yesterday = time.now().minusDays(1);

        List<Element<ManageDocumentBundle>> previousCorrespondingDocuments = List.of(
            element(ManageDocumentBundle.builder()
                .dateTimeUploaded(yesterday)
                .document(DocumentReference.builder()
                    .filename("Previous")
                    .build())
                .build()));

        List<Element<ManageDocumentBundle>> currentCorrespondingDocuments = List.of(
            element(ManageDocumentBundle.builder()
                .document(DocumentReference.builder()
                    .filename("Previous")
                    .build())
                .build()));

        List<Element<ManageDocumentBundle>> updatedCorrespondingDocuments
            = manageDocumentService.setDateTimeUploadedOnManageDocumentCollection(currentCorrespondingDocuments,
            previousCorrespondingDocuments);

        List<ManageDocumentBundle> manageDocumentBundle = unwrapElements(updatedCorrespondingDocuments);

        assertThat(manageDocumentBundle.get(0).getDateTimeUploaded()).isEqualTo(time.now());
    }

    @Test
    void shouldBuildNewHearingFurtherEvidenceCollectionIfFurtherEvidenceIsRelatedToHearingAndCollectionDoesNotExist() {
        List<Element<ManageDocumentBundle>> furtherEvidenceBundle = buildManagementDocumentBundle();
        UUID hearingId = UUID.randomUUID();
        HearingBooking hearingBooking = buildFinalHearingBooking();

        Map<String, Object> data = new HashMap<>(Map.of(
            "hearingDetails", List.of(element(hearingId, hearingBooking)),
            "manageDocumentsHearingList", DynamicList.builder()
                .value(DynamicListElement.builder()
                    .code(hearingId)
                    .build())
                .build(),
            TEMP_FURTHER_EVIDENCE_DOCUMENTS_COLLECTION_KEY, furtherEvidenceBundle,
            MANAGE_DOCUMENT_KEY, buildManagementDocument(FURTHER_EVIDENCE_DOCUMENTS, YES.getValue())));

        CaseDetails caseDetails = CaseDetails.builder().data(data).build();
        manageDocumentService.buildFurtherEvidenceCollection(caseDetails);
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        List<Element<HearingFurtherEvidenceBundle>> hearingFurtherEvidenceBundle =
            caseData.getHearingFurtherEvidenceDocuments();

        Element<HearingFurtherEvidenceBundle> furtherEvidenceBundleElement = hearingFurtherEvidenceBundle.get(0);

        assertThat(furtherEvidenceBundleElement.getId()).isEqualTo(hearingId);
        assertThat(furtherEvidenceBundleElement.getValue().getHearingName()).isEqualTo(hearingBooking.toLabel(DATE));
        assertThat(furtherEvidenceBundleElement.getValue().getManageDocumentBundle()).isEqualTo(furtherEvidenceBundle);
    }

    @Test
    void shouldAppendToExistingEntryIfFurtherHearingEvidenceIsRelatedToHearingAndCollectionEntryExists() {
        List<Element<ManageDocumentBundle>> furtherEvidenceBundle = buildManagementDocumentBundle();
        UUID hearingId = UUID.randomUUID();
        HearingBooking hearingBooking = buildFinalHearingBooking();

        Map<String, Object> data = new HashMap<>(Map.of(
            "hearingDetails", List.of(element(hearingId, hearingBooking)),
            "manageDocumentsHearingList", DynamicList.builder()
                .value(DynamicListElement.builder()
                    .code(hearingId)
                    .build())
                .build(),
            HEARING_FURTHER_EVIDENCE_DOCUMENTS_COLLECTION_KEY, List.of(
                element(hearingId, HearingFurtherEvidenceBundle.builder()
                    .manageDocumentBundle(List.of(
                        element(ManageDocumentBundle.builder().build())))
                    .build())),
            TEMP_FURTHER_EVIDENCE_DOCUMENTS_COLLECTION_KEY, furtherEvidenceBundle,
            MANAGE_DOCUMENT_KEY, buildManagementDocument(FURTHER_EVIDENCE_DOCUMENTS, YES.getValue())));

        CaseDetails caseDetails = CaseDetails.builder().data(data).build();
        manageDocumentService.buildFurtherEvidenceCollection(caseDetails);
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        List<Element<HearingFurtherEvidenceBundle>> hearingFurtherEvidenceBundle =
            caseData.getHearingFurtherEvidenceDocuments();

        Element<HearingFurtherEvidenceBundle> furtherEvidenceBundleElement = hearingFurtherEvidenceBundle.get(0);
        Element<ManageDocumentBundle> manageDocumentBundle
            = furtherEvidenceBundleElement.getValue().getManageDocumentBundle().get(1);

        assertThat(manageDocumentBundle).isEqualTo(furtherEvidenceBundle.get(0));
    }

    @Test
    void shouldAppendToNewEntryIfFurtherHearingEvidenceIsRelatedToHearingAndCollectionEntryExists() {
        List<Element<ManageDocumentBundle>> furtherEvidenceBundle = buildManagementDocumentBundle();
        UUID hearingId = UUID.randomUUID();
        HearingBooking hearingBooking = buildFinalHearingBooking();

        Map<String, Object> data = new HashMap<>(Map.of(
            "hearingDetails", List.of(element(hearingId, hearingBooking)),
            "manageDocumentsHearingList", DynamicList.builder()
                .value(DynamicListElement.builder()
                    .code(hearingId)
                    .build())
                .build(),
            HEARING_FURTHER_EVIDENCE_DOCUMENTS_COLLECTION_KEY, List.of(
                element(UUID.randomUUID(), HearingFurtherEvidenceBundle.builder()
                    .manageDocumentBundle(List.of(
                        element(ManageDocumentBundle.builder().build())))
                    .build())),
            TEMP_FURTHER_EVIDENCE_DOCUMENTS_COLLECTION_KEY, furtherEvidenceBundle,
            MANAGE_DOCUMENT_KEY, buildManagementDocument(FURTHER_EVIDENCE_DOCUMENTS, YES.getValue())));

        CaseDetails caseDetails = CaseDetails.builder().data(data).build();
        manageDocumentService.buildFurtherEvidenceCollection(caseDetails);
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        List<Element<HearingFurtherEvidenceBundle>> hearingFurtherEvidenceBundle =
            caseData.getHearingFurtherEvidenceDocuments();

        Element<HearingFurtherEvidenceBundle> furtherEvidenceBundleElement = hearingFurtherEvidenceBundle.get(1);

        assertThat(furtherEvidenceBundleElement.getId()).isEqualTo(hearingId);
        assertThat(furtherEvidenceBundleElement.getValue().getHearingName()).isEqualTo(hearingBooking.toLabel(DATE));
        assertThat(furtherEvidenceBundleElement.getValue().getManageDocumentBundle()).isEqualTo(furtherEvidenceBundle);
    }

    @Test
    void shouldAppendToFurtherEvidenceBundleIfUnrelatedToAHearing() {
        List<Element<ManageDocumentBundle>> furtherEvidenceBundle = buildManagementDocumentBundle();
        Map<String, Object> data = new HashMap<>(Map.of(
            TEMP_FURTHER_EVIDENCE_DOCUMENTS_COLLECTION_KEY, furtherEvidenceBundle,
            MANAGE_DOCUMENT_KEY, buildManagementDocument(FURTHER_EVIDENCE_DOCUMENTS, NO.getValue())));

        CaseDetails caseDetails = CaseDetails.builder().data(data).build();
        manageDocumentService.buildFurtherEvidenceCollection(caseDetails);
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        List<Element<ManageDocumentBundle>> evidenceBundle = caseData.getFurtherEvidenceDocuments();
        assertThat(evidenceBundle).isEqualTo(furtherEvidenceBundle);
    }

    private List<Element<ManageDocumentBundle>> buildManagementDocumentBundle() {
        return wrapElements(ManageDocumentBundle.builder().name("test").build());
    }

    private ManageDocument buildManagementDocument(ManageDocumentType type) {
        return ManageDocument.builder().type(type).build();
    }

    private ManageDocument buildManagementDocument(ManageDocumentType type, String isRelatedToHearing) {
        return buildManagementDocument(type).toBuilder().relatedToHearing(isRelatedToHearing).build();
    }

    private HearingBooking buildFinalHearingBooking() {
        return HearingBooking.builder()
            .type(HearingType.FINAL)
            .startDate(time.now())
            .build();
    }
}
