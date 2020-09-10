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
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.ManageDocumentType.FURTHER_EVIDENCE_DOCUMENTS;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.service.ManageDocumentService.C2_SUPPORTING_DOCUMENTS_COLLECTION;
import static uk.gov.hmcts.reform.fpl.service.ManageDocumentService.SUPPORTING_C2_LABEL;
import static uk.gov.hmcts.reform.fpl.service.ManageDocumentService.SUPPORTING_C2_LIST_KEY;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createHearingBooking;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.asDynamicList;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    JacksonAutoConfiguration.class, FixedTimeConfiguration.class, ManageDocumentService.class
})
public class ManageDocumentServiceTest {
    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private Time time;

    @Autowired
    private ManageDocumentService manageDocumentService;

    private LocalDateTime futureDate;

    @BeforeEach
    void before() {
        futureDate = time.now().plusDays(1);
    }

    @Test
    void shouldPopulateHearingListAndLabel() {
        UUID selectHearingId = randomUUID();
        HearingBooking selectedHearingBooking = createHearingBooking(futureDate, futureDate.plusDays(3));

        List<Element<HearingBooking>> hearingBookings = List.of(
            element(createHearingBooking(futureDate.plusDays(5), futureDate.plusDays(6))),
            element(createHearingBooking(futureDate.plusDays(2), futureDate.plusDays(3))),
            element(createHearingBooking(futureDate, futureDate.plusDays(1))),
            element(selectHearingId, selectedHearingBooking)
        );

        CaseData caseData = CaseData.builder()
            .manageDocumentsHearingList(selectHearingId.toString())
            .hearingDetails(hearingBookings)
            .manageDocument(buildManagementDocument(FURTHER_EVIDENCE_DOCUMENTS, YES.getValue()))
            .build();

        Map<String, Object> listAndLabel = manageDocumentService.initialiseHearingListAndLabel(caseData);

        DynamicList expectedDynamicList = ElementUtils.asDynamicList(
            hearingBookings, selectHearingId, hearingBooking -> hearingBooking.toLabel(DATE)
        );

        assertThat(listAndLabel)
            .extracting("manageDocumentsHearingList", "manageDocumentsHearingLabel")
            .containsExactly(expectedDynamicList, selectedHearingBooking.toLabel(DATE));
    }

    @Test
    void shouldExpandSupportingEvidenceCollectionWhenEmpty() {
        List<Element<SupportingEvidenceBundle>> emptySupportingEvidenceCollection = List.of();
        List<Element<SupportingEvidenceBundle>> supportingEvidenceBundleCollection
            = manageDocumentService.getSupportingEvidenceBundle(emptySupportingEvidenceCollection);

        assertThat(supportingEvidenceBundleCollection).isNotEmpty();
    }

    @Test
    void shouldPersistExistingSupportingEvidenceBundleWhenExists() {
        List<Element<SupportingEvidenceBundle>> supportEvidenceBundle = buildSupportingEvidenceBundle();
        List<Element<SupportingEvidenceBundle>> updatedSupportEvidenceBundle =
            manageDocumentService.getSupportingEvidenceBundle(supportEvidenceBundle);

        assertThat(updatedSupportEvidenceBundle).isEqualTo(supportEvidenceBundle);
    }

    @Test
    void shouldReturnEmptyCollectionWhenFurtherEvidenceIsNotRelatedToHearingAndCollectionIsNotPresent() {
        CaseData caseData = CaseData.builder()
            .manageDocument(buildManagementDocument(FURTHER_EVIDENCE_DOCUMENTS, NO.getValue()))
            .build();

        List<Element<SupportingEvidenceBundle>> supportingEvidenceBundleCollection =
            manageDocumentService.getFurtherEvidenceCollection(caseData);

        assertThat(supportingEvidenceBundleCollection).isNotEmpty();
        assertThat(supportingEvidenceBundleCollection.get(0).getValue())
            .isEqualTo(SupportingEvidenceBundle.builder().build());
    }

    @Test
    void shouldReturnFurtherEvidenceCollectionWhenFurtherEvidenceIsNotRelatedToHearingAndCollectionIsPresent() {
        List<Element<SupportingEvidenceBundle>> furtherEvidenceBundle = buildSupportingEvidenceBundle();

        CaseData caseData = CaseData.builder()
            .manageDocument(buildManagementDocument(FURTHER_EVIDENCE_DOCUMENTS, NO.getValue()))
            .furtherEvidenceDocuments(furtherEvidenceBundle)
            .build();

        List<Element<SupportingEvidenceBundle>> furtherDocumentBundleCollection =
            manageDocumentService.getFurtherEvidenceCollection(caseData);

        assertThat(furtherDocumentBundleCollection).isEqualTo(furtherEvidenceBundle);
    }

    @Test
    void shouldReturnHearingEvidenceCollectionWhenFurtherEvidenceIsRelatedToHearingWithExistingEntryInCollection() {
        List<Element<SupportingEvidenceBundle>> furtherEvidenceBundle = buildSupportingEvidenceBundle();
        UUID hearingId = UUID.randomUUID();
        List<Element<HearingBooking>> hearingBookings = List.of(element(hearingId, buildFinalHearingBooking()));

        CaseData caseData = CaseData.builder()
            .hearingDetails(hearingBookings)
            .manageDocumentsHearingList(asDynamicList(hearingBookings, hearingId, hearing -> hearing.toLabel(DATE)))
            .hearingFurtherEvidenceDocuments(List.of(
                element(hearingId, HearingFurtherEvidenceBundle.builder()
                    .supportingEvidenceBundle(furtherEvidenceBundle)
                    .build()
                )
            ))
            .manageDocument(buildManagementDocument(FURTHER_EVIDENCE_DOCUMENTS, YES.getValue()))
            .build();

        List<Element<SupportingEvidenceBundle>> furtherDocumentBundleCollection =
            manageDocumentService.getFurtherEvidenceCollection(caseData);

        assertThat(furtherDocumentBundleCollection).isEqualTo(furtherEvidenceBundle);
    }

    @Test
    void shouldSetDateTimeUploadedToNewCorrespondenceCollectionItems() {
        LocalDateTime yesterday = time.now().minusDays(1);
        List<Element<SupportingEvidenceBundle>> correspondingDocuments = buildSupportingEvidenceBundle();
        correspondingDocuments.add(element(SupportingEvidenceBundle.builder()
            .dateTimeUploaded(yesterday)
            .build()));

        List<Element<SupportingEvidenceBundle>> updatedCorrespondingDocuments
            = manageDocumentService.setDateTimeUploadedOnSupportingEvidence(correspondingDocuments, List.of());

        List<SupportingEvidenceBundle> supportingEvidenceBundle = unwrapElements(updatedCorrespondingDocuments);

        assertThat(supportingEvidenceBundle.get(0).getDateTimeUploaded()).isEqualTo(time.now());
        assertThat(supportingEvidenceBundle.get(1).getDateTimeUploaded()).isEqualTo(yesterday);
    }

    @Test
    void shouldSetNewDateTimeUploadedOnOverwriteOfPreviousDocumentUpload() {
        LocalDateTime yesterday = time.now().minusDays(1);

        List<Element<SupportingEvidenceBundle>> previousCorrespondingDocuments = List.of(
            element(SupportingEvidenceBundle.builder()
                .dateTimeUploaded(yesterday)
                .document(DocumentReference.builder()
                    .filename("Previous")
                    .build())
                .build()));

        List<Element<SupportingEvidenceBundle>> currentCorrespondingDocuments = List.of(
            element(SupportingEvidenceBundle.builder()
                .document(DocumentReference.builder()
                    .filename("Previous")
                    .build())
                .build()));

        List<Element<SupportingEvidenceBundle>> updatedCorrespondingDocuments
            = manageDocumentService.setDateTimeUploadedOnSupportingEvidence(currentCorrespondingDocuments,
            previousCorrespondingDocuments);

        List<SupportingEvidenceBundle> supportingEvidenceBundle = unwrapElements(updatedCorrespondingDocuments);

        assertThat(supportingEvidenceBundle.get(0).getDateTimeUploaded()).isEqualTo(time.now());
    }

    @Test
    void shouldBuildNewHearingFurtherEvidenceCollectionIfFurtherEvidenceIsRelatedToHearingAndCollectionDoesNotExist() {
        List<Element<SupportingEvidenceBundle>> furtherEvidenceBundle = buildSupportingEvidenceBundle();
        UUID hearingId = UUID.randomUUID();
        HearingBooking hearingBooking = buildFinalHearingBooking();

        CaseData caseData = CaseData.builder()
            .hearingDetails(List.of(element(hearingId, hearingBooking)))
            .manageDocumentsHearingList(buildDynamicList(hearingId))
            .manageDocument(buildManagementDocument(FURTHER_EVIDENCE_DOCUMENTS, YES.getValue()))
            .build();

        List<Element<HearingFurtherEvidenceBundle>> hearingFurtherEvidenceBundle =
            manageDocumentService.buildHearingFurtherEvidenceCollection(caseData, furtherEvidenceBundle);

        Element<HearingFurtherEvidenceBundle> furtherEvidenceBundleElement = hearingFurtherEvidenceBundle.get(0);

        assertThat(furtherEvidenceBundleElement.getId()).isEqualTo(hearingId);
        assertThat(furtherEvidenceBundleElement.getValue().getHearingName()).isEqualTo(hearingBooking.toLabel(DATE));
        assertThat(furtherEvidenceBundleElement.getValue().getSupportingEvidenceBundle())
            .isEqualTo(furtherEvidenceBundle);
    }

    @Test
    void shouldAppendToExistingEntryIfFurtherHearingEvidenceIsRelatedToHearingAndCollectionEntryExists() {
        List<Element<SupportingEvidenceBundle>> furtherEvidenceBundle = buildSupportingEvidenceBundle();
        UUID hearingId = UUID.randomUUID();
        HearingBooking hearingBooking = buildFinalHearingBooking();

        CaseData caseData = CaseData.builder()
            .hearingDetails(List.of(element(hearingId, hearingBooking)))
            .manageDocumentsHearingList(buildDynamicList(hearingId))
            .hearingFurtherEvidenceDocuments(List.of(element(
                hearingId,
                HearingFurtherEvidenceBundle.builder()
                    .supportingEvidenceBundle(List.of(element(SupportingEvidenceBundle.builder().build())))
                    .build())
            ))
            .manageDocument(buildManagementDocument(FURTHER_EVIDENCE_DOCUMENTS, YES.getValue()))
            .build();

        List<Element<HearingFurtherEvidenceBundle>> hearingFurtherEvidenceBundle =
            manageDocumentService.buildHearingFurtherEvidenceCollection(caseData, furtherEvidenceBundle);

        Element<HearingFurtherEvidenceBundle> furtherEvidenceBundleElement = hearingFurtherEvidenceBundle.get(0);
        Element<SupportingEvidenceBundle> supportingEvidenceBundleElement
            = furtherEvidenceBundleElement.getValue().getSupportingEvidenceBundle().get(0);

        assertThat(supportingEvidenceBundleElement).isEqualTo(furtherEvidenceBundle.get(0));
    }

    @Test
    void shouldAppendToNewEntryIfFurtherHearingEvidenceIsRelatedToHearingAndCollectionEntryExists() {
        List<Element<SupportingEvidenceBundle>> supportingEvidenceBundle = buildSupportingEvidenceBundle();
        UUID hearingId = UUID.randomUUID();
        HearingBooking hearingBooking = buildFinalHearingBooking();

        CaseData caseData = CaseData.builder()
            .hearingDetails(List.of(element(hearingId, hearingBooking)))
            .manageDocumentsHearingList(buildDynamicList(hearingId))
            .hearingFurtherEvidenceDocuments(new ArrayList<>(List.of(element(
                randomUUID(),
                HearingFurtherEvidenceBundle.builder()
                    .supportingEvidenceBundle(List.of(element(SupportingEvidenceBundle.builder().build())))
                    .build())
            )))
            .manageDocument(buildManagementDocument(FURTHER_EVIDENCE_DOCUMENTS, YES.getValue()))
            .build();

        List<Element<HearingFurtherEvidenceBundle>> hearingFurtherEvidenceBundle =
            manageDocumentService.buildHearingFurtherEvidenceCollection(caseData, supportingEvidenceBundle);

        Element<HearingFurtherEvidenceBundle> furtherEvidenceBundleElement = hearingFurtherEvidenceBundle.get(1);

        assertThat(furtherEvidenceBundleElement.getId()).isEqualTo(hearingId);
        assertThat(furtherEvidenceBundleElement.getValue().getHearingName()).isEqualTo(hearingBooking.toLabel(DATE));
        assertThat(furtherEvidenceBundleElement.getValue().getSupportingEvidenceBundle())
            .isEqualTo(supportingEvidenceBundle);
    }

    @Test
    void shouldPopulateC2SupportingDocumentsListAndLabel() {
        UUID selectedC2DocumentId = UUID.randomUUID();
        LocalDateTime tomorrow = time.now().plusDays(1);

        List<Element<C2DocumentBundle>> c2DocumentBundle = List.of(
            element(buildC2DocumentBundle(time.now().plusDays(2))),
            element(selectedC2DocumentId, buildC2DocumentBundle(tomorrow)),
            element(buildC2DocumentBundle(time.now().plusDays(2))));

        CaseData caseData = CaseData.builder()
            .c2DocumentBundle(c2DocumentBundle)
            .manageDocumentsSupportingC2List(selectedC2DocumentId)
            .build();

        Map<String, Object> listAndLabel = manageDocumentService.initialiseC2DocumentListAndLabel(caseData);

        AtomicInteger i = new AtomicInteger(1);
        DynamicList expectedC2DocumentsDynamicList = ElementUtils
            .asDynamicList(c2DocumentBundle, selectedC2DocumentId, documentBundle
                -> documentBundle.toLabel(i.toString()));

        DynamicList dynamicC2DocumentList = mapper.convertValue(caseDetails.getData().get(SUPPORTING_C2_LIST_KEY),
            DynamicList.class);

        assertThat(listAndLabel)
            .extracting("manageDocumentsHearingList", "manageDocumentsHearingLabel")
            .containsExactly(expectedDynamicList, selectedHearingBooking.toLabel(DATE));
    }

    @Test
    void shouldGetSelectedC2DocumentEvidenceBundleWhenParentC2SelectedFromDynamicList() {
        UUID selectedC2DocumentId = UUID.randomUUID();
        List<Element<SupportingEvidenceBundle>> furtherEvidenceBundle = buildSupportingEvidenceBundle();

        List<Element<C2DocumentBundle>> c2DocumentBundleList = List.of(
            element(buildC2DocumentBundle(time.now().plusDays(2))),
            element(selectedC2DocumentId, C2DocumentBundle.builder()
                .supportingEvidenceBundle(furtherEvidenceBundle)
                .build()),
            element(buildC2DocumentBundle(time.now().plusDays(2))));

        AtomicInteger i = new AtomicInteger(1);
        DynamicList expectedC2DocumentsDynamicList = ElementUtils
            .asDynamicList(c2DocumentBundleList, selectedC2DocumentId, documentBundle ->
                documentBundle.toLabel(i.toString()));

        Map<String, Object> data = new HashMap<>(Map.of(
            "c2DocumentBundle", c2DocumentBundleList,
            SUPPORTING_C2_LIST_KEY, expectedC2DocumentsDynamicList));

        CaseDetails caseDetails = CaseDetails.builder().data(data).build();

        List<Element<SupportingEvidenceBundle>> c2SupportingEvidenceBundle =
            manageDocumentService.getC2SupportingEvidenceBundle(caseDetails);

        assertThat(c2SupportingEvidenceBundle).isEqualTo(furtherEvidenceBundle);
    }

    @Test
    void shouldGetEmptyC2DocumentEvidenceBundleWhenParentSelectedFromDynamicListButEvidenceBundleIsEmpty() {
        UUID selectedC2DocumentId = UUID.randomUUID();

        List<Element<C2DocumentBundle>> c2DocumentBundleList = List.of(
            element(buildC2DocumentBundle(time.now().plusDays(2))),
            element(selectedC2DocumentId, buildC2DocumentBundle(time.now().plusDays(2))),
            element(buildC2DocumentBundle(time.now().plusDays(2))));

        AtomicInteger i = new AtomicInteger(1);
        DynamicList expectedC2DocumentsDynamicList = ElementUtils
            .asDynamicList(c2DocumentBundleList, selectedC2DocumentId, documentBundle ->
                documentBundle.toLabel(i.toString()));

        Map<String, Object> data = new HashMap<>(Map.of(
            "c2DocumentBundle", c2DocumentBundleList,
            SUPPORTING_C2_LIST_KEY, expectedC2DocumentsDynamicList));

        CaseDetails caseDetails = CaseDetails.builder().data(data).build();

        List<Element<SupportingEvidenceBundle>> c2SupportingEvidenceBundle =
            manageDocumentService.getC2SupportingEvidenceBundle(caseDetails);

        assertThat(c2SupportingEvidenceBundle.get(0).getValue()).isEqualTo(SupportingEvidenceBundle.builder().build());
    }

    @Test
    void shouldReturnUpdatedC2DocumentBundleWithUpdatedSupportingEvidenceEntry() {
        // TODO
        // Test does not assert that old c2 documents are still present

        UUID selectedC2DocumentId = UUID.randomUUID();
        C2DocumentBundle selectedC2DocumentBundle = buildC2DocumentBundle(time.now().plusDays(2));
        List<Element<SupportingEvidenceBundle>> newSupportingEvidenceBundle = buildSupportingEvidenceBundle(time.now());

        List<Element<C2DocumentBundle>> c2DocumentBundleList = List.of(
            element(buildC2DocumentBundle(time.now().plusDays(2))),
            element(selectedC2DocumentId, selectedC2DocumentBundle),
            element(buildC2DocumentBundle(time.now().plusDays(2))));

        AtomicInteger i = new AtomicInteger(1);
        DynamicList expectedC2DocumentsDynamicList = ElementUtils
            .asDynamicList(c2DocumentBundleList, selectedC2DocumentId, documentBundle ->
                documentBundle.toLabel(i.toString()));

        Map<String, Object> data = new HashMap<>(Map.of(
            "c2DocumentBundle", c2DocumentBundleList,
            C2_SUPPORTING_DOCUMENTS_COLLECTION, newSupportingEvidenceBundle,
            SUPPORTING_C2_LIST_KEY, expectedC2DocumentsDynamicList));

        CaseDetails caseDetails = CaseDetails.builder().data(data).build();
        List<Element<C2DocumentBundle>> updatedC2DocumentBundle =
            manageDocumentService.buildFinalC2SupportingDocuments(caseDetails);

        List<Element<SupportingEvidenceBundle>> updatedC2EvidenceBundlers
            = updatedC2DocumentBundle.get(1).getValue().getSupportingEvidenceBundle();

        assertThat(updatedC2EvidenceBundlers).isEqualTo(newSupportingEvidenceBundle);
    }

    @Test
    void shouldNotUpdatePreviousSupportingEvidenceEntryWhenNoUpdatesWhereMade() {
        // TODO
        // Test should assert that dates on old c2 documents are kept as they where
    }

    private List<Element<SupportingEvidenceBundle>> buildSupportingEvidenceBundle() {
        return wrapElements(SupportingEvidenceBundle.builder().name("test").build());
    }

    private List<Element<SupportingEvidenceBundle>> buildSupportingEvidenceBundle(LocalDateTime localDateTime) {
        return wrapElements(SupportingEvidenceBundle.builder()
            .name("test")
            .dateTimeUploaded(localDateTime)
            .build());
    }

    private ManageDocument buildManagementDocument(ManageDocumentType type, String isRelatedToHearing) {
        return ManageDocument.builder().type(type).relatedToHearing(isRelatedToHearing).build();
    }

    private HearingBooking buildFinalHearingBooking() {
        return HearingBooking.builder()
            .type(HearingType.FINAL)
            .startDate(time.now())
            .build();
    }

    private C2DocumentBundle buildC2DocumentBundle(LocalDateTime dateTime) {
        return C2DocumentBundle.builder().uploadedDateTime(dateTime.toString()).build();
    }

    private DynamicList buildDynamicList(UUID selectedId) {
        return DynamicList.builder()
            .value(DynamicListElement.builder()
                .code(selectedId)
                .build())
            .build();
    }
}
