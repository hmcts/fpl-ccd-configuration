package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import uk.gov.hmcts.reform.fpl.enums.HearingType;
import uk.gov.hmcts.reform.fpl.enums.OtherApplicationType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.HearingFurtherEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.ManageDocument;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.OtherApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.model.interfaces.ApplicationsBundle;
import uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.DocumentUploadHelper;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;
import uk.gov.hmcts.reform.fpl.utils.TestDataHelper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.Collections.emptyList;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static uk.gov.hmcts.reform.fpl.enums.FurtherEvidenceType.OTHER_REPORTS;
import static uk.gov.hmcts.reform.fpl.enums.ManageDocumentType.FURTHER_EVIDENCE_DOCUMENTS;
import static uk.gov.hmcts.reform.fpl.enums.OtherApplicationType.C1_WITH_SUPPLEMENT;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService.ADDITIONAL_APPLICATIONS_BUNDLE_KEY;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService.C2_DOCUMENTS_COLLECTION_KEY;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService.MANAGE_DOCUMENTS_HEARING_LIST_KEY;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService.MANAGE_DOCUMENT_KEY;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService.SUPPORTING_C2_LIST_KEY;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createHearingBooking;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.asDynamicList;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@SuppressWarnings("unchecked")
class ManageDocumentServiceTest {
    private static final String USER = "HMCTS";

    private static final Time time = new FixedTimeConfiguration().stoppedTime();
    private static final DocumentUploadHelper documentUploadHelper = mock(DocumentUploadHelper.class);
    private static final UserService userService = mock(UserService.class);
    private static final LocalDateTime futureDate = time.now().plusDays(1);

    private ManageDocumentService underTest;

    @BeforeEach
    void before() {
        underTest = new ManageDocumentService(new ObjectMapper(), time, documentUploadHelper, userService);

        given(documentUploadHelper.getUploadedDocumentUserDetails()).willReturn("HMCTS");
        given(userService.isHmctsUser()).willReturn(true);
    }

    @Test
    void shouldPopulateFieldsWhenHearingAndC2DocumentBundleDetailsArePresentOnCaseData() {
        Element<C2DocumentBundle> c2Bundle1 = element(buildC2DocumentBundle(futureDate.plusDays(2)));
        C2DocumentBundle c2ApplicationBundle1 = buildC2DocumentBundle(randomUUID(), futureDate.plusDays(3));
        OtherApplicationsBundle otherApplicationsBundle = buildOtherApplicationBundle(
            randomUUID(), OtherApplicationType.C100_CHILD_ARRANGEMENTS, futureDate.plusDays(3));

        List<Element<HearingBooking>> hearingBookings = List.of(
            element(createHearingBooking(futureDate.plusDays(5), futureDate.plusDays(6))),
            element(createHearingBooking(futureDate.plusDays(2), futureDate.plusDays(3))),
            element(createHearingBooking(futureDate, futureDate.plusDays(1)))
        );

        CaseData caseData = CaseData.builder()
            .c2DocumentBundle(List.of(c2Bundle1))
            .additionalApplicationsBundle(wrapElements(
                AdditionalApplicationsBundle.builder()
                    .c2DocumentBundle(c2ApplicationBundle1)
                    .otherApplicationsBundle(otherApplicationsBundle).build()))
            .hearingDetails(hearingBookings)
            .build();

        DynamicList expectedC2DocumentsDynamicList = TestDataHelper.buildDynamicList(
            Pair.of(c2Bundle1.getId(), "C2, " + c2Bundle1.getValue().getUploadedDateTime()),
            Pair.of(c2ApplicationBundle1.getId(), "C2, " + c2ApplicationBundle1.getUploadedDateTime()),
            Pair.of(otherApplicationsBundle.getId(), "C100, " + otherApplicationsBundle.getUploadedDateTime())
        );

        DynamicList expectedHearingDynamicList = asDynamicList(hearingBookings, HearingBooking::toLabel);

        ManageDocument expectedManageDocument = ManageDocument.builder()
            .hasHearings(YES.getValue())
            .hasC2s(YES.getValue())
            .build();

        Map<String, Object> listAndLabel = underTest.initialiseManageDocumentEvent(caseData);

        assertThat(listAndLabel)
            .extracting(MANAGE_DOCUMENTS_HEARING_LIST_KEY, SUPPORTING_C2_LIST_KEY, MANAGE_DOCUMENT_KEY)
            .containsExactly(expectedHearingDynamicList, expectedC2DocumentsDynamicList, expectedManageDocument);
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldNotPopulateHearingListOrC2DocumentListWhenHearingAndC2DocumentsAreNotPresentOnCaseData(
        List<Element<HearingBooking>> hearingDetails) {
        CaseData caseData = CaseData.builder().hearingDetails(hearingDetails).build();
        ManageDocument expectedManageDocument = ManageDocument.builder()
            .hasHearings(NO.getValue())
            .hasC2s(NO.getValue())
            .build();

        Map<String, Object> listAndLabel = underTest.initialiseManageDocumentEvent(caseData);

        assertThat(listAndLabel)
            .extracting(MANAGE_DOCUMENTS_HEARING_LIST_KEY, SUPPORTING_C2_LIST_KEY, MANAGE_DOCUMENT_KEY)
            .containsExactly(null, null, expectedManageDocument);
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
            .manageDocument(buildFurtherEvidenceManagementDocument(YES.getValue()))
            .build();

        Map<String, Object> listAndLabel = underTest.initialiseHearingListAndLabel(
            caseData, caseData.getManageDocument().isDocumentRelatedToHearing());

        DynamicList expectedDynamicList = asDynamicList(hearingBookings, selectHearingId, HearingBooking::toLabel);

        assertThat(listAndLabel)
            .extracting(MANAGE_DOCUMENTS_HEARING_LIST_KEY, "manageDocumentsHearingLabel")
            .containsExactly(expectedDynamicList, selectedHearingBooking.toLabel());
    }

    @Test
    void shouldReturnEmptyHearingListAndLabelWhenTheFurtherEvidenceDocumentsAreNotRelatedToHearings() {
        UUID selectHearingId = randomUUID();

        List<Element<HearingBooking>> hearingBookings = List.of(
            element(createHearingBooking(futureDate.plusDays(1), futureDate.plusDays(3))),
            element(createHearingBooking(futureDate, futureDate.plusDays(2)))
        );

        CaseData caseData = CaseData.builder()
            .manageDocumentsHearingList(selectHearingId.toString())
            .hearingDetails(hearingBookings)
            .manageDocument(buildFurtherEvidenceManagementDocument(NO.getValue()))
            .build();

        Map<String, Object> listAndLabel = underTest.initialiseHearingListAndLabel(
            caseData, caseData.getManageDocument().isDocumentRelatedToHearing());

        assertThat(listAndLabel).isEmpty();
    }

    @Test
    void shouldThrowAnIllegalStateExceptionWhenFailingToFindAHearingToInitialiseCaseFieldsWith() {
        UUID selectedHearingId = randomUUID();
        List<Element<HearingBooking>> hearingBookings = List.of(
            element(createHearingBooking(futureDate.plusDays(5), futureDate.plusDays(6))),
            element(createHearingBooking(futureDate.plusDays(2), futureDate.plusDays(3))),
            element(createHearingBooking(futureDate, futureDate.plusDays(1))),
            element(createHearingBooking(futureDate, futureDate.plusDays(1)))
        );

        CaseData caseData = CaseData.builder()
            .manageDocumentsHearingList(selectedHearingId.toString())
            .hearingDetails(hearingBookings)
            .manageDocument(buildFurtherEvidenceManagementDocument(YES.getValue()))
            .build();

        assertThatThrownBy(() -> underTest.initialiseHearingListAndLabel(caseData, true))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage(String.format("Hearing booking with id %s not found", selectedHearingId));
    }

    @Test
    void shouldExpandSupportingEvidenceCollectionWhenEmpty() {
        List<Element<SupportingEvidenceBundle>> emptySupportingEvidenceCollection = List.of();
        List<Element<SupportingEvidenceBundle>> supportingEvidenceBundleCollection
            = underTest.getSupportingEvidenceBundle(emptySupportingEvidenceCollection);

        assertThat(supportingEvidenceBundleCollection).isNotEmpty();
    }

    @Test
    void shouldPersistExistingSupportingEvidenceBundleWhenExists() {
        List<Element<SupportingEvidenceBundle>> supportEvidenceBundle = buildSupportingEvidenceBundle();
        List<Element<SupportingEvidenceBundle>> updatedSupportEvidenceBundle =
            underTest.getSupportingEvidenceBundle(supportEvidenceBundle);

        assertThat(updatedSupportEvidenceBundle).isEqualTo(supportEvidenceBundle);
    }

    @Test
    void shouldReturnEvidenceBundleWithDefaultTypeWhenFurtherEvidenceIsNotRelatedToHearingAndCollectionIsNotPresent() {
        CaseData caseData = CaseData.builder()
            .manageDocument(buildFurtherEvidenceManagementDocument(NO.getValue()))
            .build();

        List<Element<SupportingEvidenceBundle>> supportingEvidenceBundleCollection =
            underTest.getFurtherEvidenceCollection(caseData, false,
                List.of(element(SupportingEvidenceBundle.builder().build())));

        SupportingEvidenceBundle firstSupportingEvidenceBundle = supportingEvidenceBundleCollection.get(0).getValue();

        assertThat(supportingEvidenceBundleCollection).isNotEmpty();
        assertThat(firstSupportingEvidenceBundle).isEqualTo(
            SupportingEvidenceBundle.builder().type(OTHER_REPORTS).build());
    }

    @Test
    void shouldReturnFurtherEvidenceCollectionWhenFurtherEvidenceIsNotRelatedToHearingAndCollectionIsPresent() {
        List<Element<SupportingEvidenceBundle>> furtherEvidenceBundle = buildSupportingEvidenceBundle();

        CaseData caseData = CaseData.builder()
            .manageDocument(buildFurtherEvidenceManagementDocument(NO.getValue()))
            .furtherEvidenceDocuments(furtherEvidenceBundle)
            .build();

        List<Element<SupportingEvidenceBundle>> furtherDocumentBundleCollection =
            underTest.getFurtherEvidenceCollection(caseData, false, furtherEvidenceBundle);

        assertThat(furtherDocumentBundleCollection).isEqualTo(furtherEvidenceBundle);
        assertThat(furtherDocumentBundleCollection.get(0).getValue().getType()).isEqualTo(OTHER_REPORTS);
    }

    @Test
    void shouldReturnHearingEvidenceCollectionWhenFurtherEvidenceIsRelatedToHearingWithExistingEntryInCollection() {
        List<Element<SupportingEvidenceBundle>> furtherEvidenceBundle = buildSupportingEvidenceBundle();
        UUID hearingId = UUID.randomUUID();
        List<Element<HearingBooking>> hearingBookings = List.of(element(hearingId, buildFinalHearingBooking()));

        CaseData caseData = CaseData.builder()
            .hearingDetails(hearingBookings)
            .manageDocumentsHearingList(asDynamicList(hearingBookings, hearingId, HearingBooking::toLabel))
            .hearingFurtherEvidenceDocuments(List.of(
                element(hearingId, HearingFurtherEvidenceBundle.builder()
                    .supportingEvidenceBundle(furtherEvidenceBundle)
                    .build())))
            .manageDocument(buildFurtherEvidenceManagementDocument(YES.getValue()))
            .build();

        List<Element<SupportingEvidenceBundle>> furtherDocumentBundleCollection =
            underTest.getFurtherEvidenceCollection(caseData, true, null);

        assertThat(furtherDocumentBundleCollection).isEqualTo(furtherEvidenceBundle);
        assertThat(furtherDocumentBundleCollection.get(0).getValue().getType()).isEqualTo(OTHER_REPORTS);
    }

    @Test
    void shouldReturnOnlyAdminUploadedSupportingEvidenceForHearingWhenBothAdminAndLAUploadedEvidenceExists() {
        Element<SupportingEvidenceBundle> adminEvidence = element(SupportingEvidenceBundle.builder()
            .name("Admin uploaded evidence")
            .uploadedBy("HMCTS")
            .build());

        Element<SupportingEvidenceBundle> laEvidence = element(SupportingEvidenceBundle.builder()
            .name("LA uploaded evidence")
            .uploadedBy("Raghu Karthik")
            .build());

        List<Element<SupportingEvidenceBundle>> furtherEvidenceBundle = List.of(adminEvidence, laEvidence);

        UUID hearingId = UUID.randomUUID();
        List<Element<HearingBooking>> hearingBookings = List.of(element(hearingId, buildFinalHearingBooking()));

        CaseData caseData = CaseData.builder()
            .hearingDetails(hearingBookings)
            .manageDocumentsHearingList(asDynamicList(hearingBookings, hearingId, HearingBooking::toLabel))
            .hearingFurtherEvidenceDocuments(List.of(
                element(hearingId, HearingFurtherEvidenceBundle.builder()
                    .supportingEvidenceBundle(furtherEvidenceBundle)
                    .build())))
            .manageDocument(buildFurtherEvidenceManagementDocument(YES.getValue()))
            .build();

        List<Element<SupportingEvidenceBundle>> furtherDocumentBundleCollection =
            underTest.getFurtherEvidenceCollection(caseData, true, null);

        assertThat(furtherDocumentBundleCollection).containsExactly(adminEvidence);
    }

    @Test
    void shouldReturnDefaultSupportingEvidenceWhenOnlyNonHmctsSupportingEvidencePresent() {
        UUID hearingId = UUID.randomUUID();
        List<Element<HearingBooking>> hearingBookings = List.of(element(hearingId, buildFinalHearingBooking()));

        SupportingEvidenceBundle supportingEvidence = SupportingEvidenceBundle.builder()
            .name("Supporting doc 1")
            .uploadedBy("LA Solicitor")
            .build();

        CaseData caseData = CaseData.builder()
            .hearingDetails(hearingBookings)
            .manageDocumentsHearingList(asDynamicList(hearingBookings, hearingId, HearingBooking::toLabel))
            .hearingFurtherEvidenceDocuments(List.of(
                element(hearingId, HearingFurtherEvidenceBundle.builder()
                    .supportingEvidenceBundle(ElementUtils.wrapElements(supportingEvidence))
                    .build())))
            .build();

        assertThat(underTest.getFurtherEvidenceCollection(caseData, true, emptyList()))
            .extracting(Element::getValue)
            .containsExactly(SupportingEvidenceBundle.builder().build());
    }

    @Test
    void shouldReturnEmptyEvidenceCollectionWhenFurtherEvidenceIsNotRelatedToHearingAndCollectionIsNotPresent() {
        CaseData caseData = CaseData.builder()
            .manageDocument(buildFurtherEvidenceManagementDocument(NO.getValue()))
            .furtherEvidenceDocuments(emptyList())
            .build();

        List<Element<SupportingEvidenceBundle>> furtherDocumentBundleCollection =
            underTest.getFurtherEvidenceCollection(caseData, false, null);

        assertThat(unwrapElements(furtherDocumentBundleCollection))
            .containsExactly(SupportingEvidenceBundle.builder().build());
    }

    @Test
    void shouldSetDateTimeUploadedOnNewCorrespondenceSupportingEvidenceItems() {
        LocalDateTime yesterday = time.now().minusDays(1);
        List<Element<SupportingEvidenceBundle>> correspondingDocuments = buildSupportingEvidenceBundle();
        correspondingDocuments.add(element(SupportingEvidenceBundle.builder()
            .dateTimeUploaded(yesterday)
            .build()));

        List<Element<SupportingEvidenceBundle>> updatedCorrespondingDocuments
            = underTest.setDateTimeUploadedOnSupportingEvidence(correspondingDocuments, List.of());

        List<SupportingEvidenceBundle> supportingEvidenceBundle = unwrapElements(updatedCorrespondingDocuments);
        SupportingEvidenceBundle newSupportingEvidenceBundle = supportingEvidenceBundle.get(0);
        SupportingEvidenceBundle existingSupportingEvidenceBundle = supportingEvidenceBundle.get(1);

        assertThat(newSupportingEvidenceBundle.getDateTimeUploaded()).isEqualTo(time.now());
        assertThat(existingSupportingEvidenceBundle.getDateTimeUploaded()).isEqualTo(yesterday);
    }

    @Test
    void shouldSetNewDateTimeUploadedOnOverwriteOfPreviousDocumentUpload() {
        LocalDateTime yesterday = time.now().minusDays(1);
        UUID updatedId = UUID.randomUUID();

        List<Element<SupportingEvidenceBundle>> previousCorrespondingDocuments = List.of(
            element(updatedId, SupportingEvidenceBundle.builder()
                .dateTimeUploaded(yesterday)
                .document(DocumentReference.builder().filename("Previous").build())
                .build())
        );

        List<Element<SupportingEvidenceBundle>> currentCorrespondingDocuments = List.of(
            element(updatedId, SupportingEvidenceBundle.builder()
                .dateTimeUploaded(yesterday)
                .document(DocumentReference.builder().filename("override").build())
                .build()),
            element(SupportingEvidenceBundle.builder()
                .document(DocumentReference.builder().filename("new").build())
                .build())
        );

        List<Element<SupportingEvidenceBundle>> updatedCorrespondingDocuments
            = underTest.setDateTimeUploadedOnSupportingEvidence(currentCorrespondingDocuments,
            previousCorrespondingDocuments);

        List<SupportingEvidenceBundle> supportingEvidenceBundle = unwrapElements(updatedCorrespondingDocuments);

        assertThat(supportingEvidenceBundle).hasSize(2)
            .first()
            .extracting(SupportingEvidenceBundle::getDateTimeUploaded)
            .isEqualTo(time.now());
    }

    @Test
    void shouldPersistUploadedDateTimeWhenDocumentReferenceDoesNotDifferBetweenOldAndNewSupportingEvidence() {
        LocalDateTime yesterday = time.now().minusDays(1);
        UUID updatedId = UUID.randomUUID();
        DocumentReference previousDocument = DocumentReference.builder().filename("Previous").build();

        List<Element<SupportingEvidenceBundle>> previousCorrespondingDocuments = List.of(
            element(updatedId, SupportingEvidenceBundle.builder()
                .dateTimeUploaded(yesterday)
                .document(previousDocument)
                .build())
        );

        List<Element<SupportingEvidenceBundle>> currentCorrespondingDocuments = List.of(
            element(updatedId, SupportingEvidenceBundle.builder()
                .dateTimeUploaded(yesterday)
                .document(previousDocument)
                .build()),
            element(SupportingEvidenceBundle.builder()
                .document(DocumentReference.builder().filename("new").build())
                .build())
        );

        List<Element<SupportingEvidenceBundle>> updatedCorrespondingDocuments
            = underTest.setDateTimeUploadedOnSupportingEvidence(currentCorrespondingDocuments,
            previousCorrespondingDocuments);

        List<SupportingEvidenceBundle> supportingEvidenceBundle = unwrapElements(updatedCorrespondingDocuments);

        assertThat(supportingEvidenceBundle).hasSize(2)
            .first()
            .extracting(SupportingEvidenceBundle::getDateTimeUploaded)
            .isEqualTo(yesterday);
    }

    @Test
    void shouldBuildNewHearingFurtherEvidenceCollectionIfFurtherEvidenceIsRelatedToHearingAndCollectionDoesNotExist() {
        List<Element<SupportingEvidenceBundle>> furtherEvidenceBundle = buildSupportingEvidenceBundle();
        UUID hearingId = UUID.randomUUID();
        HearingBooking hearingBooking = buildFinalHearingBooking();

        CaseData caseData = CaseData.builder()
            .hearingDetails(List.of(element(hearingId, hearingBooking)))
            .manageDocumentsHearingList(buildDynamicList(hearingId))
            .manageDocument(buildFurtherEvidenceManagementDocument(YES.getValue()))
            .build();

        List<Element<HearingFurtherEvidenceBundle>> hearingFurtherEvidenceBundleCollection =
            underTest.buildHearingFurtherEvidenceCollection(caseData, furtherEvidenceBundle);

        Element<HearingFurtherEvidenceBundle> furtherEvidenceBundleElement
            = hearingFurtherEvidenceBundleCollection.get(0);

        HearingFurtherEvidenceBundle hearingFurtherEvidenceBundle = furtherEvidenceBundleElement.getValue();

        assertThat(furtherEvidenceBundleElement.getId()).isEqualTo(hearingId);
        assertThat(hearingFurtherEvidenceBundle.getHearingName()).isEqualTo(hearingBooking.toLabel());
        assertThat(hearingFurtherEvidenceBundle.getSupportingEvidenceBundle()).isEqualTo(furtherEvidenceBundle);
    }

    @Test
    void shouldAppendToExistingEntryIfFurtherHearingEvidenceIsRelatedToHearingAndCollectionEntryExists() {
        List<Element<SupportingEvidenceBundle>> furtherEvidenceBundle = buildSupportingEvidenceBundle();
        UUID hearingId = UUID.randomUUID();
        HearingBooking hearingBooking = buildFinalHearingBooking();

        CaseData caseData = CaseData.builder()
            .hearingDetails(List.of(element(hearingId, hearingBooking)))
            .manageDocumentsHearingList(buildDynamicList(hearingId))
            .supportingEvidenceDocumentsTemp(List.of(element(SupportingEvidenceBundle.builder().build())))
            .hearingFurtherEvidenceDocuments(List.of(
                element(hearingId, HearingFurtherEvidenceBundle.builder()
                    .supportingEvidenceBundle(List.of(element(SupportingEvidenceBundle.builder().build())))
                    .build()),
                element(HearingFurtherEvidenceBundle.builder()
                    .supportingEvidenceBundle(List.of(element(SupportingEvidenceBundle.builder().build())))
                    .build())))
            .manageDocument(buildFurtherEvidenceManagementDocument(YES.getValue()))
            .build();

        List<Element<HearingFurtherEvidenceBundle>> hearingFurtherEvidenceBundle =
            underTest.buildHearingFurtherEvidenceCollection(caseData, furtherEvidenceBundle);

        Element<HearingFurtherEvidenceBundle> furtherEvidenceBundleElement = hearingFurtherEvidenceBundle.get(0);
        Element<SupportingEvidenceBundle> supportingEvidenceBundleElement
            = furtherEvidenceBundleElement.getValue().getSupportingEvidenceBundle().get(1);

        assertThat(hearingFurtherEvidenceBundle.size()).isEqualTo(2);
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
            .hearingFurtherEvidenceDocuments(new ArrayList<>(List.of(
                element(randomUUID(), HearingFurtherEvidenceBundle.builder()
                    .supportingEvidenceBundle(List.of(element(SupportingEvidenceBundle.builder().build())))
                    .build()))))
            .manageDocument(buildFurtherEvidenceManagementDocument(YES.getValue()))
            .build();

        List<Element<HearingFurtherEvidenceBundle>> hearingFurtherEvidenceBundleCollection =
            underTest.buildHearingFurtherEvidenceCollection(caseData, supportingEvidenceBundle);

        Element<HearingFurtherEvidenceBundle> furtherEvidenceBundleElement
            = hearingFurtherEvidenceBundleCollection.get(1);

        HearingFurtherEvidenceBundle hearingFurtherEvidenceBundle = furtherEvidenceBundleElement.getValue();

        assertThat(furtherEvidenceBundleElement.getId()).isEqualTo(hearingId);
        assertThat(hearingFurtherEvidenceBundle.getHearingName()).isEqualTo(hearingBooking.toLabel());
        assertThat(hearingFurtherEvidenceBundle.getSupportingEvidenceBundle()).isEqualTo(supportingEvidenceBundle);
    }

    @Test
    void shouldThrowAnIllegalStateExceptionWhenFailingToFindAHearingToAssignToFurtherEvidence() {
        List<Element<SupportingEvidenceBundle>> supportingEvidenceBundle = buildSupportingEvidenceBundle();
        HearingBooking hearingBooking = buildFinalHearingBooking();
        UUID selectedHearingId = randomUUID();

        CaseData caseData = CaseData.builder()
            .hearingDetails(List.of(element(UUID.randomUUID(), hearingBooking)))
            .manageDocumentsHearingList(buildDynamicList(selectedHearingId))
            .hearingFurtherEvidenceDocuments(new ArrayList<>(List.of(
                element(randomUUID(), HearingFurtherEvidenceBundle.builder()
                    .supportingEvidenceBundle(List.of(element(SupportingEvidenceBundle.builder().build())))
                    .build()))))
            .manageDocument(buildFurtherEvidenceManagementDocument(YES.getValue()))
            .build();

        final IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> underTest.buildHearingFurtherEvidenceCollection(caseData, supportingEvidenceBundle));

        assertThat(exception.getMessage()).isEqualTo(
            String.format("Hearing booking with id %s not found", selectedHearingId)
        );
    }

    @Test
    void shouldPopulateC2SupportingDocumentsListAndLabelWhenSelectedBundleExistsInC2DocumentBundle() {
        UUID selectedC2DocumentId = UUID.randomUUID();
        C2DocumentBundle selectedC2Document = buildC2DocumentBundle(futureDate.plusDays(2));

        List<Element<C2DocumentBundle>> c2DocumentBundle = List.of(
            element(buildC2DocumentBundle(futureDate.plusDays(1))),
            element(selectedC2DocumentId, selectedC2Document),
            element(buildC2DocumentBundle(futureDate.plusDays(3)))
        );

        CaseData caseData = CaseData.builder()
            .c2DocumentBundle(c2DocumentBundle)
            .manageDocumentsSupportingC2List(buildDynamicList(selectedC2DocumentId))
            .build();

        Map<String, Object> listAndLabel = underTest.initialiseApplicationBundlesListAndLabel(caseData);

        DynamicList expectedC2DocumentsDynamicList = asDynamicList(c2DocumentBundle, selectedC2DocumentId,
            C2DocumentBundle::toLabel);

        assertThat(listAndLabel)
            .extracting("manageDocumentsSupportingC2List", "manageDocumentsSupportingC2Label")
            .containsExactly(expectedC2DocumentsDynamicList, selectedC2Document.toLabel());
    }

    @Test
    void shouldPopulateC2SupportingDocumentsListAndLabelWhenSelectedBundleExistsInAdditionalApplicationsBundle() {
        UUID selectedC2DocumentId = UUID.randomUUID();
        UUID anotherC2DocumentId = UUID.randomUUID();

        C2DocumentBundle selectedC2Document = buildC2DocumentBundle(selectedC2DocumentId, futureDate.plusDays(1));
        OtherApplicationsBundle otherBundle = OtherApplicationsBundle.builder().id(randomUUID())
            .applicationType(C1_WITH_SUPPLEMENT).build();

        C2DocumentBundle anotherC2Document = buildC2DocumentBundle(futureDate.plusDays(2));

        AdditionalApplicationsBundle additionApplicationsBundle = AdditionalApplicationsBundle.builder()
            .c2DocumentBundle(selectedC2Document)
            .otherApplicationsBundle(otherBundle)
            .build();

        CaseData caseData = CaseData.builder()
            .c2DocumentBundle(List.of(element(anotherC2DocumentId, anotherC2Document)))
            .additionalApplicationsBundle(wrapElements(additionApplicationsBundle))
            .manageDocumentsSupportingC2List(buildDynamicList(selectedC2DocumentId))
            .build();

        Map<String, Object> listAndLabel = underTest.initialiseApplicationBundlesListAndLabel(caseData);

        List<Element<ApplicationsBundle>> expectedBundles = List.of(element(otherBundle.getId(), otherBundle),
            element(selectedC2DocumentId, selectedC2Document), element(anotherC2DocumentId, anotherC2Document));

        DynamicList expectedC2DocumentsDynamicList = asDynamicList(
            expectedBundles, selectedC2DocumentId, ApplicationsBundle::toLabel);

        assertThat(listAndLabel)
            .extracting("manageDocumentsSupportingC2List", "manageDocumentsSupportingC2Label")
            .containsExactly(expectedC2DocumentsDynamicList, selectedC2Document.toLabel());
    }

    @Test
    void shouldPopulateOtherBundleListAndLabelWhenSelectedBundleExistsInAdditionalApplicationsBundle() {
        UUID selectedBundleId = UUID.randomUUID();
        UUID anotherBundleId = UUID.randomUUID();

        C2DocumentBundle c2Document = buildC2DocumentBundle(anotherBundleId, futureDate.plusDays(1));
        OtherApplicationsBundle selectedBundle = OtherApplicationsBundle.builder().id(selectedBundleId)
            .uploadedDateTime(futureDate.toString())
            .applicationType(C1_WITH_SUPPLEMENT).build();

        AdditionalApplicationsBundle additionApplicationsBundle = AdditionalApplicationsBundle.builder()
            .c2DocumentBundle(c2Document)
            .otherApplicationsBundle(selectedBundle)
            .build();

        CaseData caseData = CaseData.builder()
            .additionalApplicationsBundle(wrapElements(additionApplicationsBundle))
            .manageDocumentsSupportingC2List(buildDynamicList(selectedBundleId))
            .build();

        Map<String, Object> listAndLabel = underTest.initialiseApplicationBundlesListAndLabel(caseData);

        List<Element<ApplicationsBundle>> expectedBundles = List.of(
            element(selectedBundleId, selectedBundle), element(anotherBundleId, c2Document));

        DynamicList expectedC2DocumentsDynamicList = asDynamicList(
            expectedBundles, selectedBundleId, ApplicationsBundle::toLabel);

        assertThat(listAndLabel)
            .extracting("manageDocumentsSupportingC2List", "manageDocumentsSupportingC2Label")
            .containsExactly(expectedC2DocumentsDynamicList, selectedBundle.toLabel());
    }

    @Test
    void shouldThrowExceptionWhenSelectedApplicationBundleIsNotFound() {
        UUID selectedBundleId = randomUUID();

        AdditionalApplicationsBundle additionApplicationsBundle = AdditionalApplicationsBundle.builder()
            .c2DocumentBundle(buildC2DocumentBundle(randomUUID(), futureDate.plusDays(1)))
            .otherApplicationsBundle(buildOtherApplicationBundle(randomUUID(), C1_WITH_SUPPLEMENT, futureDate))
            .build();

        CaseData caseData = CaseData.builder()
            .additionalApplicationsBundle(wrapElements(additionApplicationsBundle))
            .manageDocumentsSupportingC2List(buildDynamicList(selectedBundleId))
            .build();

        assertThatThrownBy(() -> underTest.initialiseApplicationBundlesListAndLabel(caseData))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage(String.format("No application bundle found for the selected bundle id, %s", selectedBundleId));
    }

    @Test
    void shouldGetSelectedC2DocumentEvidenceBundleWhenParentC2SelectedFromDynamicList() {
        UUID selectedC2DocumentId = UUID.randomUUID();
        List<Element<SupportingEvidenceBundle>> furtherEvidenceBundle = buildSupportingEvidenceBundle();
        C2DocumentBundle c2Application = C2DocumentBundle.builder().id(randomUUID()).build();

        List<Element<C2DocumentBundle>> c2DocumentBundleList = List.of(
            element(buildC2DocumentBundle(futureDate)),
            element(selectedC2DocumentId, C2DocumentBundle.builder()
                .supportingEvidenceBundle(furtherEvidenceBundle).build()),
            element(buildC2DocumentBundle(futureDate))
        );

        DynamicList expectedDynamicList = buildDynamicList(selectedC2DocumentId);

        CaseData caseData = CaseData.builder()
            .c2DocumentBundle(c2DocumentBundleList)
            .additionalApplicationsBundle(
                wrapElements(AdditionalApplicationsBundle.builder().c2DocumentBundle(c2Application).build()))
            .manageDocumentsSupportingC2List(expectedDynamicList)
            .build();

        List<Element<SupportingEvidenceBundle>> c2SupportingEvidenceBundle =
            underTest.getApplicationsSupportingEvidenceBundles(caseData);

        assertThat(c2SupportingEvidenceBundle).isEqualTo(furtherEvidenceBundle);
    }

    @Test
    void shouldGetSelectedC2DocumentEvidenceBundleWhenParentC2FromAdditionalApplicationsIsSelectedFromDynamicList() {
        UUID selectedC2DocumentId = UUID.randomUUID();
        List<Element<SupportingEvidenceBundle>> c2ApplicationEvidenceBundle = buildSupportingEvidenceBundle();

        C2DocumentBundle c2Application = C2DocumentBundle.builder()
            .id(selectedC2DocumentId)
            .supportingEvidenceBundle(c2ApplicationEvidenceBundle)
            .build();

        List<Element<C2DocumentBundle>> c2DocumentBundleList = List.of(element(buildC2DocumentBundle(futureDate)));
        AdditionalApplicationsBundle applicationsBundle = AdditionalApplicationsBundle.builder()
            .c2DocumentBundle(c2Application)
            .otherApplicationsBundle(buildOtherApplicationBundle(randomUUID(), C1_WITH_SUPPLEMENT, futureDate))
            .build();

        DynamicList expectedDynamicList = buildDynamicList(selectedC2DocumentId);

        CaseData caseData = CaseData.builder()
            .c2DocumentBundle(c2DocumentBundleList)
            .additionalApplicationsBundle(wrapElements(applicationsBundle))
            .manageDocumentsSupportingC2List(expectedDynamicList)
            .build();

        List<Element<SupportingEvidenceBundle>> c2SupportingEvidenceBundle =
            underTest.getApplicationsSupportingEvidenceBundles(caseData);

        assertThat(c2SupportingEvidenceBundle).isEqualTo(c2ApplicationEvidenceBundle);
    }

    @Test
    void shouldGetSelectedOtherApplicationEvidenceBundleWhenIdFromAdditionalApplicationsIsSelectedFromDynamicList() {
        UUID selectedApplicationBundleId = UUID.randomUUID();
        List<Element<SupportingEvidenceBundle>> otherApplicationEvidenceBundle = buildSupportingEvidenceBundle();

        OtherApplicationsBundle otherApplicationsBundle = OtherApplicationsBundle.builder()
            .id(selectedApplicationBundleId)
            .applicationType(C1_WITH_SUPPLEMENT)
            .supportingEvidenceBundle(otherApplicationEvidenceBundle)
            .build();

        List<Element<C2DocumentBundle>> c2DocumentBundleList = List.of(element(buildC2DocumentBundle(futureDate)));
        AdditionalApplicationsBundle applicationsBundle = AdditionalApplicationsBundle.builder()
            .c2DocumentBundle(buildC2DocumentBundle(randomUUID(), futureDate))
            .otherApplicationsBundle(otherApplicationsBundle)
            .build();

        DynamicList expectedDynamicList = buildDynamicList(selectedApplicationBundleId);

        CaseData caseData = CaseData.builder()
            .c2DocumentBundle(c2DocumentBundleList)
            .additionalApplicationsBundle(wrapElements(applicationsBundle))
            .manageDocumentsSupportingC2List(expectedDynamicList)
            .build();

        List<Element<SupportingEvidenceBundle>> c2SupportingEvidenceBundle =
            underTest.getApplicationsSupportingEvidenceBundles(caseData);

        assertThat(c2SupportingEvidenceBundle).isEqualTo(otherApplicationEvidenceBundle);
    }

    @Test
    void shouldGetEmptyC2DocumentEvidenceBundleWhenParentSelectedFromDynamicListButEvidenceBundleIsEmpty() {
        UUID selectedC2DocumentId = UUID.randomUUID();

        List<Element<C2DocumentBundle>> c2DocumentBundleList = List.of(
            element(buildC2DocumentBundle(futureDate)),
            element(selectedC2DocumentId, buildC2DocumentBundle(futureDate)),
            element(buildC2DocumentBundle(futureDate))
        );

        DynamicList expectedC2DocumentsDynamicList = buildDynamicList(selectedC2DocumentId);

        CaseData caseData = CaseData.builder()
            .c2DocumentBundle(c2DocumentBundleList)
            .manageDocumentsSupportingC2List(expectedC2DocumentsDynamicList)
            .build();

        List<Element<SupportingEvidenceBundle>> c2SupportingEvidenceBundle =
            underTest.getApplicationsSupportingEvidenceBundles(caseData);

        SupportingEvidenceBundle actualSupportingEvidenceBundle = c2SupportingEvidenceBundle.get(0).getValue();

        assertThat(actualSupportingEvidenceBundle).isEqualTo(SupportingEvidenceBundle.builder().build());
    }

    @Test
    void shouldReturnUpdatedC2DocumentBundleWithUpdatedSupportingEvidenceEntry() {
        UUID selectedC2DocumentId = randomUUID();
        UUID anotherC2DocumentId = randomUUID();
        C2DocumentBundle selectedC2DocumentBundle = buildC2DocumentBundle(futureDate.plusDays(2));
        List<Element<SupportingEvidenceBundle>> newSupportingEvidenceBundle = buildSupportingEvidenceBundle(futureDate);

        C2DocumentBundle c2Bundle = C2DocumentBundle.builder().uploadedDateTime(futureDate.toString()).build();

        List<Element<C2DocumentBundle>> c2DocumentBundleList = List.of(
            element(anotherC2DocumentId, c2Bundle),
            element(selectedC2DocumentId, selectedC2DocumentBundle)
        );

        DynamicList c2DynamicList = buildDynamicList(selectedC2DocumentId);

        CaseData caseData = CaseData.builder()
            .manageDocumentsSupportingC2List(c2DynamicList)
            .c2DocumentBundle(c2DocumentBundleList)
            .supportingEvidenceDocumentsTemp(newSupportingEvidenceBundle)
            .build();

        Map<String, Object> actualData = underTest.buildFinalApplicationBundleSupportingDocuments(caseData);

        C2DocumentBundle expectedC2Bundle = selectedC2DocumentBundle.toBuilder()
            .supportingEvidenceBundle(newSupportingEvidenceBundle).build();

        Map<String, Object> expectedData = Map.of(
            C2_DOCUMENTS_COLLECTION_KEY,
            List.of(element(anotherC2DocumentId, c2Bundle), element(selectedC2DocumentId, expectedC2Bundle)));

        assertThat(actualData).isEqualTo(expectedData);
    }

    @Test
    void shouldReturnUpdatedC2DocumentBundleInAdditionalApplictaionsBundleWithUpdatedSupportingEvidenceEntry() {
        UUID selectedC2BundleId = UUID.randomUUID();
        List<Element<SupportingEvidenceBundle>> newSupportingEvidence = buildSupportingEvidenceBundle(futureDate);

        C2DocumentBundle c2Bundle = C2DocumentBundle.builder().uploadedDateTime(futureDate.toString()).build();
        List<Element<C2DocumentBundle>> c2DocumentBundleList = List.of(element(c2Bundle));

        C2DocumentBundle selectedC2Application = buildC2DocumentBundle(selectedC2BundleId, futureDate.plusDays(2));
        OtherApplicationsBundle otherApplicationsBundle = buildOtherApplicationBundle(
            randomUUID(), C1_WITH_SUPPLEMENT, futureDate);

        Element<AdditionalApplicationsBundle> applicationsBundle = element(AdditionalApplicationsBundle.builder()
            .c2DocumentBundle(selectedC2Application)
            .otherApplicationsBundle(otherApplicationsBundle)
            .build());

        CaseData caseData = CaseData.builder()
            .manageDocumentsSupportingC2List(buildDynamicList(selectedC2BundleId))
            .c2DocumentBundle(c2DocumentBundleList)
            .additionalApplicationsBundle(List.of(applicationsBundle))
            .supportingEvidenceDocumentsTemp(newSupportingEvidence)
            .build();

        Map<String, Object> actualData = underTest.buildFinalApplicationBundleSupportingDocuments(caseData);

        C2DocumentBundle expectedC2Bundle = selectedC2Application.toBuilder()
            .supportingEvidenceBundle(newSupportingEvidence).build();

        Map<String, Object> expectedData = Map.of(ADDITIONAL_APPLICATIONS_BUNDLE_KEY,
            List.of(element(applicationsBundle.getId(),
                applicationsBundle.getValue().toBuilder().c2DocumentBundle(expectedC2Bundle).build())));

        assertThat(actualData).isEqualTo(expectedData);
    }

    @Test
    void shouldReturnUpdatedOtherApplicationInAdditionalApplicationsBundleWithUpdatedSupportingEvidenceEntry() {
        UUID selectedBundleId = UUID.randomUUID();
        List<Element<SupportingEvidenceBundle>> newSupportingEvidence = buildSupportingEvidenceBundle(futureDate);

        C2DocumentBundle c2ApplicationBundle = buildC2DocumentBundle(randomUUID(), futureDate.plusDays(2));
        OtherApplicationsBundle otherApplicationsBundle = buildOtherApplicationBundle(
            selectedBundleId, C1_WITH_SUPPLEMENT, futureDate);

        Element<AdditionalApplicationsBundle> applicationsBundle = element(AdditionalApplicationsBundle.builder()
            .c2DocumentBundle(c2ApplicationBundle)
            .otherApplicationsBundle(otherApplicationsBundle)
            .build());

        CaseData caseData = CaseData.builder()
            .manageDocumentsSupportingC2List(buildDynamicList(selectedBundleId))
            .additionalApplicationsBundle(List.of(applicationsBundle))
            .supportingEvidenceDocumentsTemp(newSupportingEvidence)
            .build();

        Map<String, Object> actualData = underTest.buildFinalApplicationBundleSupportingDocuments(caseData);

        OtherApplicationsBundle expectedOtherApplication = otherApplicationsBundle.toBuilder()
            .supportingEvidenceBundle(newSupportingEvidence).build();

        Map<String, Object> expectedData = Map.of(ADDITIONAL_APPLICATIONS_BUNDLE_KEY,
            List.of(element(applicationsBundle.getId(),
                applicationsBundle.getValue().toBuilder()
                    .otherApplicationsBundle(expectedOtherApplication).build())));

        assertThat(actualData).isEqualTo(expectedData);
    }

    @Test
    void shouldNotUpdatePreviousSupportingEvidenceEntryWhenNoUpdatesWhereMade() {
        UUID selectedC2DocumentId = UUID.randomUUID();
        LocalDateTime uploadDateTime = futureDate.plusDays(2);
        C2DocumentBundle selectedC2DocumentBundle = buildC2DocumentBundle(uploadDateTime);
        List<Element<SupportingEvidenceBundle>> newSupportingEvidenceBundle = buildSupportingEvidenceBundle(futureDate);

        List<Element<C2DocumentBundle>> c2DocumentBundleList = List.of(
            element(buildC2DocumentBundle(uploadDateTime, buildSupportingEvidenceBundle(uploadDateTime))),
            element(selectedC2DocumentId, selectedC2DocumentBundle),
            element(buildC2DocumentBundle(uploadDateTime, buildSupportingEvidenceBundle(uploadDateTime)))
        );

        DynamicList expectedC2DocumentsDynamicList = buildDynamicList(selectedC2DocumentId);

        CaseData caseData = CaseData.builder()
            .manageDocumentsSupportingC2List(expectedC2DocumentsDynamicList)
            .c2SupportingDocuments(newSupportingEvidenceBundle)
            .c2DocumentBundle(c2DocumentBundleList)
            .build();

        Map<String, Object> actualC2Bundles = underTest
            .buildFinalApplicationBundleSupportingDocuments(caseData);

        List<Element<C2DocumentBundle>> updatedC2DocumentBundle
            = (List<Element<C2DocumentBundle>>) actualC2Bundles.get(C2_DOCUMENTS_COLLECTION_KEY);

        LocalDateTime firstC2DocumentUploadTime = updatedC2DocumentBundle.get(0).getValue()
            .getSupportingEvidenceBundle().get(0).getValue()
            .getDateTimeUploaded();

        LocalDateTime thirdC2DocumentUploadTime = updatedC2DocumentBundle.get(2).getValue()
            .getSupportingEvidenceBundle().get(0).getValue()
            .getDateTimeUploaded();

        assertThat(firstC2DocumentUploadTime).isEqualTo(uploadDateTime);
        assertThat(thirdC2DocumentUploadTime).isEqualTo(uploadDateTime);
    }

    @Test
    void shouldNotUpdateAdditionalApplicationsBundlesWhenSelectedApplicationBundleDoesNotExist() {
        UUID selectedBundleId = UUID.randomUUID();

        List<Element<AdditionalApplicationsBundle>> applicationsBundles = wrapElements(
            AdditionalApplicationsBundle.builder()
                .c2DocumentBundle(buildC2DocumentBundle(randomUUID(), futureDate))
                .otherApplicationsBundle(buildOtherApplicationBundle(randomUUID(), C1_WITH_SUPPLEMENT, futureDate))
                .build());

        DynamicList applicationBundlesDynamicList = buildDynamicList(selectedBundleId);

        CaseData caseData = CaseData.builder()
            .additionalApplicationsBundle(applicationsBundles)
            .manageDocumentsSupportingC2List(applicationBundlesDynamicList)
            .c2SupportingDocuments(buildSupportingEvidenceBundle(futureDate))
            .build();

        Map<String, Object> updatedBundles = underTest.buildFinalApplicationBundleSupportingDocuments(caseData);

        assertThat(updatedBundles.get(ADDITIONAL_APPLICATIONS_BUNDLE_KEY)).isEqualTo(applicationsBundles);
    }

    @Test
    void shouldUpdatePreviousSupportingEvidenceWhenFurtherEvidenceIsAssociatedWithAHearingAndNewDocumentHasBeenAdded() {
        SupportingEvidenceBundle previousSupportingEvidenceBundle = SupportingEvidenceBundle.builder()
            .dateTimeUploaded(futureDate)
            .document(DocumentReference.builder().filename("previousDocument.pdf").build())
            .build();

        SupportingEvidenceBundle editedSupportingEvidenceBundle = SupportingEvidenceBundle.builder()
            .document(DocumentReference.builder().filename("editedDocument.pdf").build())
            .build();

        List<Element<SupportingEvidenceBundle>> previousSupportingEvidenceList = List.of(
            element(previousSupportingEvidenceBundle));

        UUID hearingId = UUID.randomUUID();
        HearingBooking hearingBooking = buildFinalHearingBooking();

        CaseData caseData = CaseData.builder()
            .hearingDetails(List.of(element(hearingId, hearingBooking)))
            .manageDocumentsHearingList(buildDynamicList(hearingId))
            .supportingEvidenceDocumentsTemp(List.of(
                element(editedSupportingEvidenceBundle),
                element(SupportingEvidenceBundle.builder().build())))
            .manageDocument(buildFurtherEvidenceManagementDocument(YES.getValue()))
            .build();

        CaseData caseDataBefore = CaseData.builder()
            .hearingFurtherEvidenceDocuments(new ArrayList<>(List.of(
                element(hearingId, HearingFurtherEvidenceBundle.builder()
                    .supportingEvidenceBundle(previousSupportingEvidenceList)
                    .build()))))
            .build();

        List<Element<SupportingEvidenceBundle>> updatedEvidenceBundle =
            underTest.setDateTimeOnHearingFurtherEvidenceSupportingEvidence(caseData, caseDataBefore);

        SupportingEvidenceBundle firstSupportingEvidenceBundle = updatedEvidenceBundle.get(0).getValue();
        SupportingEvidenceBundle secondSupportingEvidenceBundle = updatedEvidenceBundle.get(1).getValue();

        assertThat(updatedEvidenceBundle.size()).isEqualTo(2);
        assertThat(firstSupportingEvidenceBundle.getDateTimeUploaded()).isEqualTo(time.now());
        assertThat(secondSupportingEvidenceBundle.getDateTimeUploaded()).isEqualTo(time.now());
    }

    @Test
    void shouldNotUpdatePreviousSupportingEvidenceWhenFurtherEvidenceIsAssociatedWithAHearing() {
        List<Element<SupportingEvidenceBundle>> previousSupportingEvidenceList
            = buildSupportingEvidenceBundle(futureDate);

        UUID hearingId = UUID.randomUUID();
        HearingBooking hearingBooking = buildFinalHearingBooking();

        CaseData caseData = CaseData.builder()
            .hearingDetails(List.of(element(hearingId, hearingBooking)))
            .manageDocumentsHearingList(buildDynamicList(hearingId))
            .supportingEvidenceDocumentsTemp(List.of(
                previousSupportingEvidenceList.get(0),
                element(SupportingEvidenceBundle.builder().build())))
            .manageDocument(buildFurtherEvidenceManagementDocument(YES.getValue()))
            .build();

        CaseData caseDataBefore = CaseData.builder()
            .hearingFurtherEvidenceDocuments(new ArrayList<>(List.of(
                element(hearingId, HearingFurtherEvidenceBundle.builder()
                    .supportingEvidenceBundle(previousSupportingEvidenceList)
                    .build()))))
            .build();

        List<Element<SupportingEvidenceBundle>> updatedEvidenceBundle =
            underTest.setDateTimeOnHearingFurtherEvidenceSupportingEvidence(caseData, caseDataBefore);

        SupportingEvidenceBundle firstSupportingEvidenceBundle = updatedEvidenceBundle.get(0).getValue();
        SupportingEvidenceBundle secondSupportingEvidenceBundle = updatedEvidenceBundle.get(1).getValue();

        assertThat(updatedEvidenceBundle.size()).isEqualTo(2);
        assertThat(firstSupportingEvidenceBundle).isEqualTo(previousSupportingEvidenceList.get(0).getValue());
        assertThat(secondSupportingEvidenceBundle.getDateTimeUploaded()).isEqualTo(time.now());
    }

    @Test
    void shouldSortSupportingEvidenceByDateUploadedInChronologicalOrder() {
        UUID selectedC2DocumentId = UUID.randomUUID();
        Element<SupportingEvidenceBundle> supportingEvidencePast = element(SupportingEvidenceBundle.builder()
            .name("past")
            .dateTimeUploaded(futureDate.minusDays(2))
            .uploadedBy(USER)
            .build());

        Element<SupportingEvidenceBundle> supportingEvidenceFuture = element(SupportingEvidenceBundle.builder()
            .name("future")
            .dateTimeUploaded(futureDate.plusDays(2))
            .uploadedBy(USER)
            .build());

        C2DocumentBundle selectedC2DocumentBundle = buildC2DocumentBundle(futureDate);

        List<Element<C2DocumentBundle>> c2DocumentBundleList = List.of(
            element(selectedC2DocumentId, selectedC2DocumentBundle)
        );

        DynamicList c2DynamicList = buildDynamicList(selectedC2DocumentId);

        CaseData caseData = CaseData.builder()
            .manageDocumentsSupportingC2List(c2DynamicList)
            .c2DocumentBundle(c2DocumentBundleList)
            .supportingEvidenceDocumentsTemp(List.of(supportingEvidenceFuture, supportingEvidencePast))
            .build();

        Map<String, Object> updatedBundles = underTest.buildFinalApplicationBundleSupportingDocuments(caseData);

        C2DocumentBundle updatedBundle = selectedC2DocumentBundle.toBuilder()
            .supportingEvidenceBundle(List.of(supportingEvidencePast, supportingEvidenceFuture)).build();

        Map<String, Object> expectedBundles = Map.of(
            C2_DOCUMENTS_COLLECTION_KEY, List.of(element(selectedC2DocumentId, updatedBundle)));

        assertThat(updatedBundles).isEqualTo(expectedBundles);
    }

    @Test
    void shouldRemoveHearingFurtherEvidenceBundleElementWhenAllDocumentsForThatHearingAreRemoved() {
        List<Element<SupportingEvidenceBundle>> furtherEvidenceBundle = buildSupportingEvidenceBundle();
        UUID hearingId = UUID.randomUUID();
        UUID hearingIdTwo = UUID.randomUUID();
        List<Element<HearingBooking>> hearingBookings = List.of(element(hearingId, buildFinalHearingBooking()));

        CaseData caseData = CaseData.builder()
            .hearingDetails(hearingBookings)
            .manageDocumentsHearingList(asDynamicList(hearingBookings, hearingId, HearingBooking::toLabel))
            .supportingEvidenceDocumentsTemp(emptyList())
            .hearingFurtherEvidenceDocuments(new LinkedList<>(Arrays.asList(
                element(hearingId, HearingFurtherEvidenceBundle.builder()
                    .hearingName("Case Management hearing 1")
                    .supportingEvidenceBundle(emptyList())
                    .build()),
                element(hearingIdTwo, HearingFurtherEvidenceBundle.builder()
                    .hearingName("Case Management hearing 2")
                    .supportingEvidenceBundle(buildSupportingEvidenceBundle())
                    .build()))))
            .manageDocument(buildFurtherEvidenceManagementDocument(YES.getValue()))
            .build();

        List<Element<HearingFurtherEvidenceBundle>> hearingFurtherEvidenceBundleCollection =
            underTest.buildHearingFurtherEvidenceCollection(caseData, furtherEvidenceBundle);

        assertThat(hearingFurtherEvidenceBundleCollection).size().isEqualTo(1);
        assertThat(hearingFurtherEvidenceBundleCollection.get(0).getValue().getHearingName())
            .isEqualTo("Case Management hearing 2");
    }

    @Test
    void shouldNotRemoveHearingFurtherEvidenceBundleElementWhenDocumentsForThatHearingExist() {
        List<Element<SupportingEvidenceBundle>> furtherEvidenceBundle = buildSupportingEvidenceBundle();
        UUID hearingId = UUID.randomUUID();
        UUID hearingIdTwo = UUID.randomUUID();
        List<Element<HearingBooking>> hearingBookings = List.of(element(hearingId, buildFinalHearingBooking()));

        CaseData caseData = CaseData.builder()
            .hearingDetails(hearingBookings)
            .manageDocumentsHearingList(asDynamicList(hearingBookings, hearingId, HearingBooking::toLabel))
            .supportingEvidenceDocumentsTemp(buildSupportingEvidenceBundle())
            .hearingFurtherEvidenceDocuments(new LinkedList<>(Arrays.asList(
                element(hearingId, HearingFurtherEvidenceBundle.builder()
                    .hearingName("Case Management hearing 1")
                    .supportingEvidenceBundle(emptyList())
                    .build()),
                element(hearingIdTwo, HearingFurtherEvidenceBundle.builder()
                    .hearingName("Case Management hearing 2")
                    .supportingEvidenceBundle(buildSupportingEvidenceBundle())
                    .build()))))
            .manageDocument(buildFurtherEvidenceManagementDocument(YES.getValue()))
            .build();

        List<Element<HearingFurtherEvidenceBundle>> hearingFurtherEvidenceBundleCollection =
            underTest.buildHearingFurtherEvidenceCollection(caseData, furtherEvidenceBundle);

        assertThat(hearingFurtherEvidenceBundleCollection).size().isEqualTo(2);
        assertThat(hearingFurtherEvidenceBundleCollection.get(0).getValue().getHearingName())
            .isEqualTo("Case Management hearing 1");
    }

    private List<Element<SupportingEvidenceBundle>> buildSupportingEvidenceBundle() {
        return wrapElements(SupportingEvidenceBundle.builder()
            .name("test")
            .uploadedBy(USER)
            .build());
    }

    private List<Element<SupportingEvidenceBundle>> buildSupportingEvidenceBundle(LocalDateTime localDateTime) {
        return wrapElements(SupportingEvidenceBundle.builder()
            .name("test")
            .dateTimeUploaded(localDateTime)
            .uploadedBy(USER)
            .build());
    }

    private ManageDocument buildFurtherEvidenceManagementDocument(String isRelatedToHearing) {
        return ManageDocument.builder().type(FURTHER_EVIDENCE_DOCUMENTS).relatedToHearing(isRelatedToHearing).build();
    }

    private HearingBooking buildFinalHearingBooking() {
        return HearingBooking.builder()
            .type(HearingType.FINAL)
            .startDate(time.now())
            .build();
    }

    private C2DocumentBundle buildC2DocumentBundle(LocalDateTime dateTime) {
        return buildC2DocumentBundle(null, dateTime);
    }

    private C2DocumentBundle buildC2DocumentBundle(UUID id, LocalDateTime dateTime) {
        return C2DocumentBundle.builder().id(id).uploadedDateTime(dateTime.toString()).build();
    }

    private C2DocumentBundle buildC2DocumentBundle(LocalDateTime dateTime, List<Element<SupportingEvidenceBundle>>
        supportingEvidenceBundle) {
        return C2DocumentBundle.builder().uploadedDateTime(dateTime.toString())
            .supportingEvidenceBundle(supportingEvidenceBundle)
            .build();
    }

    private OtherApplicationsBundle buildOtherApplicationBundle(
        UUID id, OtherApplicationType type, LocalDateTime time) {
        return OtherApplicationsBundle.builder()
            .id(id).applicationType(type).uploadedDateTime(time.toString()).build();
    }

    private DynamicList buildDynamicList(UUID selectedId) {
        return DynamicList.builder()
            .value(DynamicListElement.builder()
                .code(selectedId)
                .build())
            .build();
    }
}
