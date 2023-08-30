package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import uk.gov.hmcts.reform.fpl.enums.CaseRole;
import uk.gov.hmcts.reform.fpl.enums.HearingDocumentType;
import uk.gov.hmcts.reform.fpl.enums.HearingType;
import uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement;
import uk.gov.hmcts.reform.fpl.enums.ManageDocumentAction;
import uk.gov.hmcts.reform.fpl.enums.OtherApplicationType;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.enums.cfv.ConfidentialLevel;
import uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType;
import uk.gov.hmcts.reform.fpl.enums.notification.DocumentUploaderType;
import uk.gov.hmcts.reform.fpl.events.ManageDocumentsUploadedEvent;
import uk.gov.hmcts.reform.fpl.exceptions.NoHearingBookingException;
import uk.gov.hmcts.reform.fpl.exceptions.RespondentNotFoundException;
import uk.gov.hmcts.reform.fpl.handlers.ManageDocumentsUploadedEventTestData;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CaseSummary;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.CourtBundle;
import uk.gov.hmcts.reform.fpl.model.DocumentWithConfidentialAddress;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.HearingCourtBundle;
import uk.gov.hmcts.reform.fpl.model.HearingDocuments;
import uk.gov.hmcts.reform.fpl.model.HearingFurtherEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.ManageDocument;
import uk.gov.hmcts.reform.fpl.model.ManagedDocument;
import uk.gov.hmcts.reform.fpl.model.Placement;
import uk.gov.hmcts.reform.fpl.model.PlacementNoticeDocument;
import uk.gov.hmcts.reform.fpl.model.PositionStatementChild;
import uk.gov.hmcts.reform.fpl.model.PositionStatementRespondent;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.RespondentStatement;
import uk.gov.hmcts.reform.fpl.model.RespondentStatementV2;
import uk.gov.hmcts.reform.fpl.model.SkeletonArgument;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.OtherApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.model.event.ManageDocumentEventData;
import uk.gov.hmcts.reform.fpl.model.event.PlacementEventData;
import uk.gov.hmcts.reform.fpl.model.event.UploadableDocumentBundle;
import uk.gov.hmcts.reform.fpl.model.interfaces.ApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.interfaces.ConfidentialBundle;
import uk.gov.hmcts.reform.fpl.model.interfaces.NotifyDocumentUploaded;
import uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.ConfidentialBundleHelper;
import uk.gov.hmcts.reform.fpl.utils.DocumentUploadHelper;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;
import uk.gov.hmcts.reform.fpl.utils.ObjectHelper;
import uk.gov.hmcts.reform.fpl.utils.TestDataHelper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.emptyList;
import static java.util.UUID.randomUUID;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.mockito.quality.Strictness.LENIENT;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.representativeSolicitors;
import static uk.gov.hmcts.reform.fpl.enums.FurtherEvidenceType.OTHER_REPORTS;
import static uk.gov.hmcts.reform.fpl.enums.ManageDocumentType.FURTHER_EVIDENCE_DOCUMENTS;
import static uk.gov.hmcts.reform.fpl.enums.OtherApplicationType.C1_WITH_SUPPLEMENT;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.handlers.ManageDocumentsUploadedEventTestData.buildSubmittedCaseDataWithNewDocumentUploaded;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService.ADDITIONAL_APPLICATIONS_BUNDLE_KEY;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService.C2_DOCUMENTS_COLLECTION_KEY;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService.CASE_SUMMARY_KEY;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService.CASE_SUMMARY_LIST_KEY;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService.CHILDREN_LIST_KEY;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService.COURT_BUNDLE_KEY;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService.COURT_BUNDLE_LIST_KEY;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService.DOCUMENT_WITH_CONFIDENTIAL_ADDRESS_KEY;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService.HEARING_DOCUMENT_HEARING_LIST_KEY;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService.HEARING_DOCUMENT_RESPONDENT_LIST_KEY;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService.MANAGE_DOCUMENTS_HEARING_LIST_KEY;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService.MANAGE_DOCUMENT_KEY;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService.PLACEMENT_LIST_KEY;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService.POSITION_STATEMENT_CHILD_KEY;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService.POSITION_STATEMENT_CHILD_LIST_KEY;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService.POSITION_STATEMENT_RESPONDENT_KEY;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService.POSITION_STATEMENT_RESPONDENT_LIST_KEY;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService.RESPONDENTS_LIST_KEY;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService.SKELETON_ARGUMENT_KEY;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService.SKELETON_ARGUMENT_LIST_KEY;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService.SUPPORTING_C2_LIST_KEY;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createHearingBooking;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE_TIME;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.asDynamicList;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testChild;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testRespondent;

@SuppressWarnings("unchecked")
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = LENIENT)
class ManageDocumentServiceTest {

    enum Confidentiality {
        YES, NO, NULL
    }

    private static final String USER = "HMCTS";
    public static final boolean NOT_SOLICITOR = false;
    public static final boolean IS_SOLICITOR = true;

    @Spy
    private final Time time = new FixedTimeConfiguration().stoppedTime();

    @Spy
    private final LocalDateTime futureDate = time.now().plusDays(1);

    @Spy
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private DocumentUploadHelper documentUploadHelper;

    @Mock
    private PlacementService placementService;

    @Mock
    private UserService userService;

    @InjectMocks
    private ManageDocumentService underTest;

    @Mock
    private DynamicListService dynamicListService;

    UUID elementIdOne = UUID.randomUUID();
    UUID elementIdTwo = UUID.randomUUID();

    DocumentReference expectedDocumentOne = TestDataHelper.testDocumentReference();
    DocumentReference expectedDocumentTwo = TestDataHelper.testDocumentReference();

    @BeforeEach
    void before() {
        given(documentUploadHelper.getUploadedDocumentUserDetails()).willReturn("HMCTS");
        given(userService.isHmctsUser()).willReturn(true);
    }

    @Test
    void shouldPopulateFieldsWhenPlacementNoticesArePresentOnCaseData() {
        Placement placement = Placement.builder()
            .childName("Test Child")
            .placementNotice(testDocumentReference())
            .build();

        List<Element<Placement>> placements = wrapElements(placement);

        CaseData caseData = CaseData.builder()
            .placementEventData(PlacementEventData.builder()
                .placements(placements)
                .build())
            .build();

        Map<String, Object> updates = underTest.baseEventData(caseData);

        ManageDocument expectedManageDocument = ManageDocument.builder()
            .hasHearings(NO.getValue())
            .hasC2s(NO.getValue())
            .hasPlacementNotices(YES.getValue())
            .hasConfidentialAddress(NO.getValue())
            .build();

        DynamicList expectedPlacementList = asDynamicList(placements, null, Placement::getChildName);

        assertThat(updates)
            .extracting(MANAGE_DOCUMENT_KEY)
            .isEqualTo(expectedManageDocument);

        assertThat(updates)
            .extracting(PLACEMENT_LIST_KEY)
            .isEqualTo(expectedPlacementList);

    }

    @Test
    void shouldPopulateFieldsWhenHearingAndC2DocumentBundleDetailsArePresentOnCaseData() {
        Element<C2DocumentBundle> c2Bundle1 = element(buildC2DocumentBundle(futureDate.plusDays(2)));
        C2DocumentBundle c2ApplicationBundle1 = buildC2DocumentBundle(futureDate.plusDays(3));
        OtherApplicationsBundle otherApplicationsBundle = buildOtherApplicationBundle(
            randomUUID(), OtherApplicationType.C100_CHILD_ARRANGEMENTS, futureDate.plusDays(3));

        List<Element<HearingBooking>> hearingBookings = List.of(
            element(createHearingBooking(futureDate.plusDays(5), futureDate.plusDays(6))),
            element(createHearingBooking(futureDate.plusDays(2), futureDate.plusDays(3))),
            element(createHearingBooking(futureDate, futureDate.plusDays(1))));

        Element<Respondent> respondent1 = testRespondent("John", "Smith");
        Element<Respondent> respondent2 = testRespondent("Alex", "Williams");

        Element<Child> child1 = testChild("Tom","Smith");
        Element<Child> child2 = testChild("Mary","Smith");

        CaseData caseData = CaseData.builder()
            .c2DocumentBundle(List.of(c2Bundle1))
            .additionalApplicationsBundle(wrapElements(
                AdditionalApplicationsBundle.builder()
                    .c2DocumentBundle(c2ApplicationBundle1)
                    .otherApplicationsBundle(otherApplicationsBundle).build()))
            .hearingDetails(hearingBookings)
            .respondents1(List.of(respondent1, respondent2))
            .children1(List.of(child1, child2))
            .build();

        DynamicList expectedC2DocumentsDynamicList = TestDataHelper.buildDynamicList(
            Pair.of(c2ApplicationBundle1.getId(), "C2, " + c2ApplicationBundle1.getUploadedDateTime()),
            Pair.of(c2Bundle1.getId(), "C2, " + c2Bundle1.getValue().getUploadedDateTime()),
            Pair.of(otherApplicationsBundle.getId(), "C100, " + otherApplicationsBundle.getUploadedDateTime())
        );

        DynamicList expectedHearingDynamicList = asDynamicList(hearingBookings, HearingBooking::toLabel);

        DynamicList expectedRespondentsDynamicList = TestDataHelper.buildDynamicList(
            Pair.of(respondent1.getId(), "John Smith"),
            Pair.of(respondent2.getId(), "Alex Williams"));

        DynamicList expectedChildrenDynamicList = TestDataHelper.buildDynamicList(
            Pair.of(child1.getId(), "Tom Smith"),
            Pair.of(child2.getId(), "Mary Smith")
        );

        ManageDocument expectedManageDocument = ManageDocument.builder()
            .hasHearings(YES.getValue())
            .hasC2s(YES.getValue())
            .hasConfidentialAddress(NO.getValue())
            .hasPlacementNotices(NO.getValue())
            .build();

        Map<String, Object> updates = underTest.baseEventData(caseData);

        assertThat(updates)
            .extracting(MANAGE_DOCUMENTS_HEARING_LIST_KEY)
            .isEqualTo(expectedHearingDynamicList);

        assertThat(updates)
            .extracting(HEARING_DOCUMENT_HEARING_LIST_KEY)
            .isEqualTo(expectedHearingDynamicList);

        assertThat(updates)
            .extracting(SUPPORTING_C2_LIST_KEY)
            .isEqualTo(expectedC2DocumentsDynamicList);

        assertThat(updates)
            .extracting(RESPONDENTS_LIST_KEY)
            .isEqualTo(expectedRespondentsDynamicList);

        assertThat(updates)
            .extracting(HEARING_DOCUMENT_RESPONDENT_LIST_KEY)
            .isEqualTo(expectedRespondentsDynamicList);

        assertThat(updates)
            .extracting(MANAGE_DOCUMENT_KEY)
            .isEqualTo(expectedManageDocument);

        assertThat(updates)
            .extracting(CHILDREN_LIST_KEY)
            .isEqualTo(expectedChildrenDynamicList);
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldNotPopulateHearingListOrC2DocumentListWhenHearingAndC2DocumentsAreNotPresentOnCaseData(
        List<Element<HearingBooking>> hearingDetails) {
        CaseData caseData = CaseData.builder().hearingDetails(hearingDetails).build();
        ManageDocument expectedManageDocument = ManageDocument.builder()
            .hasHearings(NO.getValue())
            .hasC2s(NO.getValue())
            .hasConfidentialAddress(NO.getValue())
            .hasPlacementNotices(NO.getValue())
            .build();

        Map<String, Object> updates = underTest.baseEventData(caseData);

        assertThat(updates)
            .extracting(MANAGE_DOCUMENTS_HEARING_LIST_KEY, SUPPORTING_C2_LIST_KEY, MANAGE_DOCUMENT_KEY)
            .containsExactly(null, null, expectedManageDocument);

        assertThat(updates).doesNotContainKeys(MANAGE_DOCUMENTS_HEARING_LIST_KEY);
        assertThat(updates).doesNotContainKeys(SUPPORTING_C2_LIST_KEY);
        assertThat(updates).doesNotContainKeys(RESPONDENTS_LIST_KEY);
        assertThat(updates).containsEntry(MANAGE_DOCUMENT_KEY, expectedManageDocument);
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
            .manageDocumentsRelatedToHearing(YES.getValue())
            .manageDocument(buildFurtherEvidenceManagementDocument())
            .build();

        Map<String, Object> listAndLabel = underTest.initialiseHearingListAndLabel(caseData);

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
            .manageDocumentsRelatedToHearing(NO.getValue())
            .manageDocument(buildFurtherEvidenceManagementDocument())
            .build();

        Map<String, Object> listAndLabel = underTest.initialiseHearingListAndLabel(caseData);

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
            .manageDocumentsRelatedToHearing(YES.getValue())
            .manageDocument(buildFurtherEvidenceManagementDocument())
            .build();

        assertThatThrownBy(() -> underTest.initialiseHearingListAndLabel(caseData))
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
            .manageDocumentsRelatedToHearing(NO.getValue())
            .manageDocument(buildFurtherEvidenceManagementDocument())
            .build();

        List<Element<SupportingEvidenceBundle>> supportingEvidenceBundleCollection =
            underTest.getFurtherEvidences(caseData, List.of(element(SupportingEvidenceBundle.builder().build())));

        SupportingEvidenceBundle firstSupportingEvidenceBundle = supportingEvidenceBundleCollection.get(0).getValue();

        assertThat(supportingEvidenceBundleCollection).isNotEmpty();
        assertThat(firstSupportingEvidenceBundle).isEqualTo(
            SupportingEvidenceBundle.builder().type(OTHER_REPORTS).build());
    }

    @Test
    void shouldReturnFurtherEvidenceCollectionWhenFurtherEvidenceIsNotRelatedToHearingAndCollectionIsPresent() {
        List<Element<SupportingEvidenceBundle>> furtherEvidenceBundle = buildSupportingEvidenceBundle();

        CaseData caseData = CaseData.builder()
            .manageDocumentsRelatedToHearing(NO.getValue())
            .manageDocument(buildFurtherEvidenceManagementDocument())
            .furtherEvidenceDocuments(furtherEvidenceBundle)
            .build();

        List<Element<SupportingEvidenceBundle>> furtherDocumentBundleCollection =
            underTest.getFurtherEvidences(caseData, furtherEvidenceBundle);

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
            .manageDocumentsRelatedToHearing(YES.getValue())
            .manageDocument(buildFurtherEvidenceManagementDocument())
            .build();

        List<Element<SupportingEvidenceBundle>> furtherDocumentBundleCollection =
            underTest.getFurtherEvidences(caseData, null);

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
            .manageDocumentsRelatedToHearing(YES.getValue())
            .manageDocument(buildFurtherEvidenceManagementDocument())
            .build();

        List<Element<SupportingEvidenceBundle>> furtherDocumentBundleCollection =
            underTest.getFurtherEvidences(caseData, null);

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
            .manageDocumentsRelatedToHearing(YES.getValue())
            .hearingFurtherEvidenceDocuments(List.of(
                element(hearingId, HearingFurtherEvidenceBundle.builder()
                    .supportingEvidenceBundle(ElementUtils.wrapElements(supportingEvidence))
                    .build())))
            .build();

        assertThat(underTest.getFurtherEvidences(caseData, emptyList()))
            .extracting(Element::getValue)
            .containsExactly(SupportingEvidenceBundle.builder().build());
    }

    @Test
    void shouldReturnEmptyEvidenceCollectionWhenFurtherEvidenceIsNotRelatedToHearingAndCollectionIsNotPresent() {
        CaseData caseData = CaseData.builder()
            .manageDocument(buildFurtherEvidenceManagementDocument())
            .furtherEvidenceDocuments(emptyList())
            .build();

        List<Element<SupportingEvidenceBundle>> furtherDocumentBundleCollection =
            underTest.getFurtherEvidences(caseData, null);

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
            = underTest.setDateTimeUploadedOnSupportingEvidence(correspondingDocuments, List.of(), NOT_SOLICITOR);

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
            previousCorrespondingDocuments, NOT_SOLICITOR);

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
            previousCorrespondingDocuments, NOT_SOLICITOR);

        List<SupportingEvidenceBundle> supportingEvidenceBundle = unwrapElements(updatedCorrespondingDocuments);

        assertThat(supportingEvidenceBundle).hasSize(2)
            .first()
            .extracting(SupportingEvidenceBundle::getDateTimeUploaded)
            .isEqualTo(yesterday);
    }

    @Test
    void shouldSetDateTimeUploadedAndSolicitorUploadOnNewCorrespondenceSupportingEvidenceItemsUploadedBySolicitor() {
        LocalDateTime yesterday = time.now().minusDays(1);
        List<Element<SupportingEvidenceBundle>> correspondingDocuments = buildSupportingEvidenceBundle();
        correspondingDocuments.add(element(SupportingEvidenceBundle.builder()
            .dateTimeUploaded(yesterday)
            .build()));

        List<Element<SupportingEvidenceBundle>> updatedCorrespondingDocuments
            = underTest.setDateTimeUploadedOnSupportingEvidence(correspondingDocuments, List.of(), IS_SOLICITOR);

        List<SupportingEvidenceBundle> supportingEvidenceBundle = unwrapElements(updatedCorrespondingDocuments);
        SupportingEvidenceBundle newSupportingEvidenceBundle = supportingEvidenceBundle.get(0);
        SupportingEvidenceBundle existingSupportingEvidenceBundle = supportingEvidenceBundle.get(1);

        assertThat(newSupportingEvidenceBundle.getDateTimeUploaded()).isEqualTo(time.now());
        assertThat(newSupportingEvidenceBundle.getUploadedBySolicitor()).isEqualTo("Yes");
        assertThat(existingSupportingEvidenceBundle.getDateTimeUploaded()).isEqualTo(yesterday);
        assertThat(existingSupportingEvidenceBundle.getUploadedBySolicitor()).isNull();
    }

    @Test
    void shouldBuildNewHearingFurtherEvidenceCollectionIfFurtherEvidenceIsRelatedToHearingAndCollectionDoesNotExist() {
        List<Element<SupportingEvidenceBundle>> furtherEvidenceBundle = buildSupportingEvidenceBundle();
        UUID hearingId = UUID.randomUUID();
        HearingBooking hearingBooking = buildFinalHearingBooking();

        CaseData caseData = CaseData.builder()
            .hearingDetails(List.of(element(hearingId, hearingBooking)))
            .manageDocumentsHearingList(buildDynamicList(hearingId))
            .manageDocumentsRelatedToHearing(YES.getValue())
            .manageDocument(buildFurtherEvidenceManagementDocument())
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
            .manageDocumentsRelatedToHearing(YES.getValue())
            .manageDocument(buildFurtherEvidenceManagementDocument())
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
            .manageDocumentsRelatedToHearing(YES.getValue())
            .manageDocument(buildFurtherEvidenceManagementDocument())
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
            .manageDocumentsRelatedToHearing(YES.getValue())
            .manageDocument(buildFurtherEvidenceManagementDocument())
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
            element(buildC2DocumentBundle(futureDate.plusDays(3))),
            element(selectedC2DocumentId, selectedC2Document),
            element(buildC2DocumentBundle(futureDate.plusDays(1)))
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
            element(anotherC2DocumentId, anotherC2Document), element(selectedC2DocumentId, selectedC2Document));

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
        OtherApplicationsBundle selectedBundle
            = buildOtherApplicationBundle(selectedBundleId, C1_WITH_SUPPLEMENT, futureDate);

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
            .c2DocumentBundle(buildC2DocumentBundle(futureDate.plusDays(1)))
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

        C2DocumentBundle c2Bundle = buildC2DocumentBundle(futureDate);

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

        Map<String, Object> actualData = underTest.buildFinalApplicationBundleSupportingDocuments(caseData,
            NOT_SOLICITOR);

        C2DocumentBundle expectedC2Bundle = selectedC2DocumentBundle.toBuilder()
            .supportingEvidenceBundle(newSupportingEvidenceBundle).build();

        Map<String, Object> expectedData = Map.of(
            C2_DOCUMENTS_COLLECTION_KEY,
            List.of(element(anotherC2DocumentId, c2Bundle), element(selectedC2DocumentId, expectedC2Bundle)),
            DOCUMENT_WITH_CONFIDENTIAL_ADDRESS_KEY, emptyList());

        assertThat(actualData).isEqualTo(expectedData);
    }

    @Test
    void shouldReturnUpdatedC2DocumentBundleInAdditionalApplicationsBundleWithUpdatedSupportingEvidenceEntry() {
        UUID selectedC2BundleId = UUID.randomUUID();
        List<Element<SupportingEvidenceBundle>> newSupportingEvidence = buildSupportingEvidenceBundle(futureDate);

        C2DocumentBundle c2Bundle = buildC2DocumentBundle(futureDate);
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

        Map<String, Object> actualData = underTest.buildFinalApplicationBundleSupportingDocuments(caseData,
            NOT_SOLICITOR);

        C2DocumentBundle expectedC2Bundle = selectedC2Application.toBuilder()
            .supportingEvidenceBundle(newSupportingEvidence).build();

        Map<String, Object> expectedData = Map.of(ADDITIONAL_APPLICATIONS_BUNDLE_KEY,
            List.of(element(applicationsBundle.getId(),
                applicationsBundle.getValue().toBuilder().c2DocumentBundle(expectedC2Bundle).build())),
            DOCUMENT_WITH_CONFIDENTIAL_ADDRESS_KEY, emptyList());

        assertThat(actualData).isEqualTo(expectedData);
    }

    @Test
    void shouldReturnUpdatedOtherApplicationInAdditionalApplicationsBundleWithUpdatedSupportingEvidenceEntry() {
        UUID selectedBundleId = UUID.randomUUID();
        List<Element<SupportingEvidenceBundle>> newSupportingEvidence = buildSupportingEvidenceBundle(futureDate);

        C2DocumentBundle c2ApplicationBundle = buildC2DocumentBundle(futureDate.plusDays(2));
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

        Map<String, Object> actualData = underTest.buildFinalApplicationBundleSupportingDocuments(caseData,
            NOT_SOLICITOR);

        OtherApplicationsBundle expectedOtherApplication = otherApplicationsBundle.toBuilder()
            .supportingEvidenceBundle(newSupportingEvidence).build();

        Map<String, Object> expectedData = Map.of(ADDITIONAL_APPLICATIONS_BUNDLE_KEY,
            List.of(element(applicationsBundle.getId(),
                applicationsBundle.getValue().toBuilder()
                    .otherApplicationsBundle(expectedOtherApplication).build())),
                DOCUMENT_WITH_CONFIDENTIAL_ADDRESS_KEY, emptyList());

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
            .buildFinalApplicationBundleSupportingDocuments(caseData, NOT_SOLICITOR);

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
                .c2DocumentBundle(buildC2DocumentBundle(futureDate))
                .otherApplicationsBundle(buildOtherApplicationBundle(randomUUID(), C1_WITH_SUPPLEMENT, futureDate))
                .build(),
            AdditionalApplicationsBundle.builder()
                .c2DocumentBundle(buildC2DocumentBundle(futureDate))
                .build());

        DynamicList applicationBundlesDynamicList = buildDynamicList(selectedBundleId);

        CaseData caseData = CaseData.builder()
            .additionalApplicationsBundle(applicationsBundles)
            .manageDocumentsSupportingC2List(applicationBundlesDynamicList)
            .c2SupportingDocuments(buildSupportingEvidenceBundle(futureDate))
            .build();

        Map<String, Object> updatedBundles = underTest.buildFinalApplicationBundleSupportingDocuments(caseData,
            NOT_SOLICITOR);

        assertThat(updatedBundles).containsEntry(ADDITIONAL_APPLICATIONS_BUNDLE_KEY, applicationsBundles);
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
            .manageDocumentsRelatedToHearing(YES.getValue())
            .manageDocument(buildFurtherEvidenceManagementDocument())
            .build();

        CaseData caseDataBefore = CaseData.builder()
            .hearingFurtherEvidenceDocuments(new ArrayList<>(List.of(
                element(hearingId, HearingFurtherEvidenceBundle.builder()
                    .supportingEvidenceBundle(previousSupportingEvidenceList)
                    .build()))))
            .build();

        List<Element<SupportingEvidenceBundle>> updatedEvidenceBundle =
            underTest.setDateTimeOnHearingFurtherEvidenceSupportingEvidence(caseData, caseDataBefore, NOT_SOLICITOR);

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
            .manageDocumentsRelatedToHearing(YES.getValue())
            .manageDocument(buildFurtherEvidenceManagementDocument())
            .build();

        CaseData caseDataBefore = CaseData.builder()
            .hearingFurtherEvidenceDocuments(new ArrayList<>(List.of(
                element(hearingId, HearingFurtherEvidenceBundle.builder()
                    .supportingEvidenceBundle(previousSupportingEvidenceList)
                    .build()))))
            .build();

        List<Element<SupportingEvidenceBundle>> updatedEvidenceBundle =
            underTest.setDateTimeOnHearingFurtherEvidenceSupportingEvidence(caseData, caseDataBefore, NOT_SOLICITOR);

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

        Map<String, Object> updatedBundles = underTest.buildFinalApplicationBundleSupportingDocuments(caseData,
            NOT_SOLICITOR);

        C2DocumentBundle updatedBundle = selectedC2DocumentBundle.toBuilder()
            .supportingEvidenceBundle(List.of(supportingEvidencePast, supportingEvidenceFuture)).build();

        Map<String, Object> expectedBundles = Map.of(
            C2_DOCUMENTS_COLLECTION_KEY, List.of(element(selectedC2DocumentId, updatedBundle)),
            DOCUMENT_WITH_CONFIDENTIAL_ADDRESS_KEY, emptyList());

        assertThat(updatedBundles).isEqualTo(expectedBundles);
    }

    @Test
    void shouldReturnUpdatedOtherApplicationUpdatedSupportingEvidenceEntryUploadedBySolicitor() {
        UUID selectedBundleId = UUID.randomUUID();
        UUID evidenceId = randomUUID();

        SupportingEvidenceBundle bundle = SupportingEvidenceBundle.builder()
            .name("test")
            .build();

        C2DocumentBundle c2ApplicationBundle = buildC2DocumentBundle(futureDate.plusDays(2));
        OtherApplicationsBundle otherApplicationsBundle = buildOtherApplicationBundle(
            selectedBundleId, C1_WITH_SUPPLEMENT, futureDate);

        Element<AdditionalApplicationsBundle> applicationsBundle = element(AdditionalApplicationsBundle.builder()
            .c2DocumentBundle(c2ApplicationBundle)
            .otherApplicationsBundle(otherApplicationsBundle)
            .build());

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .manageDocumentsSupportingC2List(buildDynamicList(selectedBundleId))
            .additionalApplicationsBundle(List.of(applicationsBundle))
            .supportingEvidenceDocumentsTemp(List.of(element(evidenceId, bundle)))
            .build();

        given(documentUploadHelper.getUploadedDocumentUserDetails()).willReturn("NOT HMCTS");
        given(userService.isHmctsUser()).willReturn(false);
        given(userService.hasAnyCaseRoleFrom(representativeSolicitors(), 12345L)).willReturn(IS_SOLICITOR);

        Map<String, Object> actualData = underTest.buildFinalApplicationBundleSupportingDocuments(caseData,
            IS_SOLICITOR);

        OtherApplicationsBundle expectedOtherApplication = otherApplicationsBundle.toBuilder()
            .supportingEvidenceBundle(List.of(element(evidenceId, bundle.toBuilder()
                .uploadedBySolicitor("Yes")
                .build())
            )).build();

        Map<String, Object> expectedData = Map.of(ADDITIONAL_APPLICATIONS_BUNDLE_KEY,
            List.of(element(applicationsBundle.getId(),
                applicationsBundle.getValue().toBuilder()
                    .otherApplicationsBundle(expectedOtherApplication).build())),
            DOCUMENT_WITH_CONFIDENTIAL_ADDRESS_KEY, emptyList());

        assertThat(actualData).isEqualTo(expectedData);
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
            .manageDocumentsRelatedToHearing(YES.getValue())
            .manageDocument(buildFurtherEvidenceManagementDocument())
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
            .manageDocumentsRelatedToHearing(YES.getValue())
            .manageDocument(buildFurtherEvidenceManagementDocument())
            .build();

        List<Element<HearingFurtherEvidenceBundle>> hearingFurtherEvidenceBundleCollection =
            underTest.buildHearingFurtherEvidenceCollection(caseData, furtherEvidenceBundle);

        assertThat(hearingFurtherEvidenceBundleCollection).size().isEqualTo(2);
        assertThat(hearingFurtherEvidenceBundleCollection.get(0).getValue().getHearingName())
            .isEqualTo("Case Management hearing 1");
    }

    @Nested
    class GetRespondentStatementFurtherEvidenceCollection {
        UUID selectedRespondentId = UUID.randomUUID();

        List<Element<SupportingEvidenceBundle>> supportingEvidenceBundleOne = List.of(
            element(SupportingEvidenceBundle.builder().build()));

        List<Element<SupportingEvidenceBundle>> supportingEvidenceBundleTwo = List.of(
            element(SupportingEvidenceBundle.builder().build()));

        @Test
        void shouldGetRespondentStatementsSupportingEvidenceDocumentsWithMatchingRespondentId() {
            CaseData caseData = CaseData.builder()
                .respondentStatements(List.of(
                    element(RespondentStatement.builder()
                        .respondentId(UUID.randomUUID())
                        .supportingEvidenceBundle(supportingEvidenceBundleOne)
                        .build()),
                    element(RespondentStatement.builder()
                        .respondentId(selectedRespondentId)
                        .supportingEvidenceBundle(supportingEvidenceBundleTwo)
                        .build())))
                .build();

            List<Element<SupportingEvidenceBundle>> actualBundle
                = underTest.getRespondentStatements(caseData, selectedRespondentId);

            assertThat(actualBundle).isEqualTo(supportingEvidenceBundleTwo);
        }

        @Test
        void shouldReturnEmptySupportingEvidenceDocumentsWhenRespondentStatementsDoNotHaveExpectedRespondentId() {
            CaseData caseData = CaseData.builder()
                .respondentStatements(List.of(
                    element(RespondentStatement.builder()
                        .respondentId(UUID.randomUUID())
                        .supportingEvidenceBundle(supportingEvidenceBundleOne)
                        .build()),
                    element(RespondentStatement.builder()
                        .respondentId(UUID.randomUUID())
                        .supportingEvidenceBundle(supportingEvidenceBundleTwo)
                        .build())))
                .build();

            List<Element<SupportingEvidenceBundle>> actualBundle
                = underTest.getRespondentStatements(caseData, selectedRespondentId);

            assertThat(actualBundle).extracting(Element::getValue)
                .containsExactly(SupportingEvidenceBundle.builder().build());
        }

        @Test
        void shouldReturnEmptySupportingEvidenceDocumentsWhenCaseDoesNotContainRespondentStatements() {
            CaseData caseData = CaseData.builder().build();

            List<Element<SupportingEvidenceBundle>> actualBundle
                = underTest.getRespondentStatements(caseData, selectedRespondentId);

            assertThat(actualBundle).extracting(Element::getValue)
                .containsExactly(SupportingEvidenceBundle.builder().build());
        }
    }

    @Nested
    class GetUpdatedRespondentStatements {
        UUID respondentOneId = UUID.randomUUID();
        UUID respondentTwoId = UUID.randomUUID();
        UUID respondentStatementId = UUID.randomUUID();
        UUID supportingEvidenceBundleId = UUID.randomUUID();

        @Test
        void shouldUpdateExistingRespondentStatementsWithNewBundle() {
            DynamicList respondentStatementList = buildRespondentStatementList();
            List<Element<SupportingEvidenceBundle>> updatedBundle = buildSupportingEvidenceBundle();

            CaseData caseData = CaseData.builder()
                .respondents1(List.of(
                    element(respondentOneId, Respondent.builder()
                        .party(RespondentParty.builder()
                            .firstName("David")
                            .lastName("Stevenson")
                            .build())
                        .build()),
                    element(Respondent.builder().build())))
                .supportingEvidenceDocumentsTemp(updatedBundle)
                .respondentStatementList(respondentStatementList)
                .respondentStatements(newArrayList(
                    element(respondentStatementId, RespondentStatement.builder()
                        .respondentId(respondentOneId)
                        .supportingEvidenceBundle(
                            newArrayList(element(supportingEvidenceBundleId,
                                SupportingEvidenceBundle.builder().document(testDocumentReference()).build())))
                        .respondentName("David Stevenson")
                        .build())))
                .build();

            List<Element<RespondentStatement>> updatedRespondentStatements =
                underTest.getUpdatedRespondentStatements(caseData, NOT_SOLICITOR);

            assertThat(updatedRespondentStatements).containsExactly(
                element(respondentStatementId, RespondentStatement.builder()
                    .respondentId(respondentOneId)
                    .respondentName("David Stevenson")
                    .supportingEvidenceBundle(updatedBundle)
                    .build()));
        }

        @Test
        void shouldAddNewEntryToRespondentStatementsWhenRespondentStatementDoesNotExist() {
            UUID respondentStatementId = UUID.randomUUID();
            DynamicList respondentStatementList = buildRespondentStatementList();
            List<Element<SupportingEvidenceBundle>> updatedBundle = buildSupportingEvidenceBundle();

            List<Element<RespondentStatement>> respondentStatements = new ArrayList<>();

            respondentStatements.add(element(respondentStatementId, RespondentStatement.builder()
                .respondentId(respondentTwoId)
                .supportingEvidenceBundle(List.of(
                    element(supportingEvidenceBundleId, SupportingEvidenceBundle.builder().build())))
                .build()));

            CaseData caseData = CaseData.builder()
                .respondents1(List.of(
                    element(respondentOneId, Respondent.builder()
                        .party(RespondentParty.builder()
                            .firstName("Sam")
                            .lastName("Watson")
                            .build())
                        .build()),
                    element(Respondent.builder().build()),
                    element(Respondent.builder().build())))
                .supportingEvidenceDocumentsTemp(updatedBundle)
                .respondentStatementList(respondentStatementList)
                .respondentStatements(respondentStatements)
                .build();

            List<Element<RespondentStatement>> updatedRespondentStatements =
                underTest.getUpdatedRespondentStatements(caseData, NOT_SOLICITOR);

            assertThat(updatedRespondentStatements.size()).isEqualTo(2);

            Element<RespondentStatement> firstRespondentStatement = updatedRespondentStatements.get(0);
            Element<RespondentStatement> secondRespondentStatement = updatedRespondentStatements.get(1);

            assertThat(firstRespondentStatement).isEqualTo(element(respondentStatementId, RespondentStatement.builder()
                .respondentId(respondentTwoId)
                .supportingEvidenceBundle(List.of(
                    element(supportingEvidenceBundleId, SupportingEvidenceBundle.builder().build())
                )).build()));

            assertThat(secondRespondentStatement.getValue().getRespondentId()).isEqualTo(respondentOneId);
            assertThat(secondRespondentStatement.getValue().getRespondentName()).isEqualTo("Sam Watson");
            assertThat(secondRespondentStatement.getValue().getSupportingEvidenceBundle()).isEqualTo(updatedBundle);
        }

        @Test
        void shouldRemoveRespondentStatementEntryWhenUpdatingExistingWithEmptySupportingEvidence() {
            List<Element<SupportingEvidenceBundle>> updatedBundle = List.of();

            DynamicList respondentStatementList = buildRespondentStatementList();

            CaseData caseData = CaseData.builder()
                .respondents1(List.of(
                    element(respondentOneId, Respondent.builder()
                        .party(RespondentParty.builder()
                            .firstName("David")
                            .lastName("Stevenson")
                            .build())
                        .build()),
                    element(Respondent.builder().build())))
                .supportingEvidenceDocumentsTemp(updatedBundle)
                .respondentStatementList(respondentStatementList)
                .respondentStatements(newArrayList(
                    element(respondentStatementId, RespondentStatement.builder()
                        .respondentId(respondentOneId)
                        .supportingEvidenceBundle(List.of(
                            element(supportingEvidenceBundleId, SupportingEvidenceBundle.builder().build())
                        ))
                        .respondentName("David Stevenson")
                        .build())))
                .build();

            List<Element<RespondentStatement>> updatedRespondentStatements =
                underTest.getUpdatedRespondentStatements(caseData, NOT_SOLICITOR);

            assertThat(updatedRespondentStatements).isEmpty();
        }

        @Test
        void shouldThrowAnErrorWhenRespondentCannotBeFound() {
            DynamicList respondentStatementList = buildRespondentStatementList();

            CaseData caseData = CaseData.builder()
                .respondents1(List.of(
                    element(Respondent.builder()
                        .party(RespondentParty.builder()
                            .firstName("Sam")
                            .lastName("Watson")
                            .build())
                        .build()),
                    element(Respondent.builder().build()),
                    element(Respondent.builder().build())))
                .respondentStatementList(respondentStatementList)
                .build();

            assertThatThrownBy(() -> underTest.getUpdatedRespondentStatements(caseData, NOT_SOLICITOR))
                .isInstanceOf(RespondentNotFoundException.class)
                .hasMessage(String.format("Respondent with id %s not found", respondentOneId));
        }

        private List<Element<SupportingEvidenceBundle>> buildSupportingEvidenceBundle() {
            return List.of(element(supportingEvidenceBundleId,
                SupportingEvidenceBundle.builder()
                    .name("Test name")
                    .uploadedBy("Test uploaded by")
                    .build()));
        }

        private DynamicList buildRespondentStatementList() {
            return DynamicList.builder()
                .value(DynamicListElement.builder()
                    .code(respondentOneId)
                    .build())
                .listItems(List.of(
                    DynamicListElement.builder()
                        .code(respondentOneId)
                        .label("Respondent 1")
                        .build(),
                    DynamicListElement.builder()
                        .code(respondentTwoId)
                        .label("Respondent 2")
                        .build()
                )).build();
        }
    }

    @Test
    void shouldSortDocumentByUploadedDateTime() {
        LocalDateTime now = LocalDateTime.now();
        List<Element<SupportingEvidenceBundle>> documents = new ArrayList<>(wrapElements(
            SupportingEvidenceBundle.builder().name("doc1").dateTimeUploaded(now.minusSeconds(1)).build(),
            SupportingEvidenceBundle.builder().name("doc2").dateTimeUploaded(now.minusDays(1)).build(),
            SupportingEvidenceBundle.builder().name("doc3").dateTimeUploaded(null).build(),
            SupportingEvidenceBundle.builder().name("doc4").dateTimeUploaded(now).build()));

        assertThat(underTest.sortCorrespondenceDocumentsByUploadedDate(documents))
            .isEqualTo(List.of(documents.get(3), documents.get(0), documents.get(1), documents.get(2)));
    }

    @Test
    void shouldGetSelectedRespondentIdFromDynamicList() {
        UUID selectedRespondentId = randomUUID();
        UUID additionalRespondentId = randomUUID();

        DynamicList dynamicList = DynamicList.builder()
            .value(DynamicListElement.builder()
                .code(selectedRespondentId)
                .build())
            .listItems(List.of(DynamicListElement.builder()
                    .code(selectedRespondentId)
                    .label("Joe Bloggs")
                    .build(),
                DynamicListElement.builder()
                    .code(additionalRespondentId)
                    .label("Paul Smith")
                    .build()
            ))
            .build();

        CaseData caseData = CaseData.builder().respondentStatementList(dynamicList).build();

        assertThat(underTest.getSelectedRespondentId(caseData)).isEqualTo(selectedRespondentId);
    }

    @Test
    void shouldGetDocWithConfidentialAddrFromRespondentStatementElements() {
        CaseData caseData = CaseData.builder().build();
        UUID uuid = randomUUID();
        DocumentReference confidentialDoc = DocumentReference.builder()
            .filename("test file name 1")
            .binaryUrl("test url 1").build();

        DocumentReference normalDoc = DocumentReference.builder()
            .filename("test file name 2")
            .binaryUrl("test url 2").build();


        List<Element<SupportingEvidenceBundle>> bundles =  List.of(
            element(uuid, SupportingEvidenceBundle.builder()
                .name("test bundle name 1")
                .document(confidentialDoc)
                .hasConfidentialAddress(YES.getValue())
                .build()),
            element(uuid, SupportingEvidenceBundle.builder()
                .name("test bundle name 2")
                .document(normalDoc)
                .hasConfidentialAddress(NO.getValue())
                .build())
        );

        {
            List<ConfidentialBundle> confidentialBundle = List.of(buildConfidentialBundle(bundles));
            List<Element<DocumentWithConfidentialAddress>> resultLIst =
                underTest.getDocumentsWithConfidentialAddress(caseData, new ArrayList<>(),
                    ConfidentialBundleHelper.getSupportingEvidenceBundle(confidentialBundle));
            List<Element<DocumentWithConfidentialAddress>> expected = List.of(
                element(uuid, DocumentWithConfidentialAddress.builder()
                    .name("test bundle name 1")
                    .document(confidentialDoc).build())
            );
            assertThat(resultLIst).isEqualTo(expected);
        }
    }

    @Test
    void shouldGetDocWithConfidentialAddrFromHearingCourtBundles() {
        CaseData caseData = CaseData.builder().build();
        UUID uuid1 = randomUUID();
        UUID uuid2 = randomUUID();

        DocumentReference confidentialDoc = DocumentReference.builder()
            .filename("test file name 1")
            .binaryUrl("test url 1").build();

        DocumentReference normalDoc = DocumentReference.builder()
            .filename("test file name 2")
            .binaryUrl("test url 2").build();

        List<Element<CourtBundle>> courtBundles = List.of(
            element(uuid1, CourtBundle.builder()
                .document(confidentialDoc)
                .hasConfidentialAddress(YES.getValue()).build()),
            element(uuid2, CourtBundle.builder()
                .document(normalDoc)
                .hasConfidentialAddress(NO.getValue()).build()));

        List<Element<HearingCourtBundle>> hearingCourtBundles = List.of(
            element(randomUUID(), HearingCourtBundle.builder()
                .hearing("Test hearing")
                .courtBundle(courtBundles)
                .build()));

        List<Element<DocumentWithConfidentialAddress>> resultLIst =
            underTest.getDocumentsWithConfidentialAddressFromCourtBundles(caseData,
                caseData.getHearingDocuments().getCourtBundleListV2(),
                hearingCourtBundles);

        List<Element<DocumentWithConfidentialAddress>> expected = List.of(
            element(uuid1, DocumentWithConfidentialAddress.builder()
                .name("Court bundle of Test hearing")
                .document(confidentialDoc).build())
        );

        assertThat(resultLIst).isEqualTo(expected);
    }

    private ConfidentialBundle buildConfidentialBundle(List<Element<SupportingEvidenceBundle>> bundles) {
        return new ConfidentialBundle() {

            @Override
            public List<Element<SupportingEvidenceBundle>> getSupportingEvidenceBundle() {
                return bundles;
            }

            @Override
            public List<Element<SupportingEvidenceBundle>> getSupportingEvidenceLA() {
                return bundles;
            }

            @Override
            public List<Element<SupportingEvidenceBundle>> getSupportingEvidenceNC() {
                return bundles;
            }
        };
    }

    @Test
    void shouldReturnNewHearingDocumentListWithCourtBundleWhenNoExistingCourtBundlesPresentForSelectedHearing() {
        UUID selectedHearingId = randomUUID();

        List<Element<HearingBooking>> hearingBookings = List.of(
            element(selectedHearingId, createHearingBooking(futureDate, futureDate.plusDays(3)))
        );
        List<Element<CourtBundle>> courtBundle = List.of(element(CourtBundle.builder().build()));

        CaseData caseData = CaseData.builder()
            .manageDocumentsCourtBundle(courtBundle)
            .hearingDocumentsHearingList(selectedHearingId.toString())
            .hearingDetails(hearingBookings)
            .manageDocumentsHearingDocumentType(HearingDocumentType.COURT_BUNDLE)
            .build();

        List<HearingCourtBundle> results = unwrapElements((List<Element<HearingCourtBundle>>)
            underTest.buildHearingDocumentList(caseData).get(COURT_BUNDLE_LIST_KEY));
        assertThat(results).isNotEmpty();
        assertThat(results.get(0).getCourtBundle())
            .isNotEmpty()
            .isEqualTo(courtBundle);
    }


    @Test
    void shouldReturnNewCourtBundleListWithCourtBundleWhenNoExistingCourtBundlePresentForSelectedHearing() {
        UUID selectedHearingId = randomUUID();

        List<Element<HearingBooking>> hearingBookings = List.of(
            element(selectedHearingId, createHearingBooking(futureDate, futureDate.plusDays(3)))
        );
        List<Element<CourtBundle>> courtBundle = List.of(element(CourtBundle.builder().build()));

        CaseData caseData = CaseData.builder()
            .manageDocumentsCourtBundle(courtBundle)
            .hearingDocumentsHearingList(selectedHearingId.toString())
            .hearingDetails(hearingBookings)
            .manageDocumentsHearingDocumentType(HearingDocumentType.COURT_BUNDLE)
            .build();

        List<HearingCourtBundle> results = unwrapElements(
            (List<Element<HearingCourtBundle>>) underTest
                .buildHearingDocumentList(caseData).get(COURT_BUNDLE_LIST_KEY));
        assertThat(results).isNotEmpty();
        assertThat(results.get(0).getCourtBundle())
            .isNotEmpty()
            .isEqualTo(courtBundle);
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldReturnEditedCourtBundleListWithCourtBundleWhenExistingCourtBundlePresentForSelectedHearing() {
        UUID selectedHearingId = randomUUID();
        List<Element<HearingBooking>> hearingBookings = List.of(
            element(selectedHearingId, createHearingBooking(futureDate, futureDate.plusDays(3)))
        );
        List<Element<CourtBundle>> currentCourtBundle = List.of(element(createCourtBundleWithFile("Current filename")));
        List<Element<CourtBundle>> editedCourtBundle = List.of(element(createCourtBundleWithFile("New filename")));

        List<Element<HearingCourtBundle>> courtBundleList = List.of(element(
            selectedHearingId,
            HearingCourtBundle.builder()
                .hearing(hearingBookings.get(0).getValue().toLabel())
                .courtBundle(currentCourtBundle)
                .build()));

        CaseData caseData = CaseData.builder()
            .manageDocumentsCourtBundle(editedCourtBundle)
            .hearingDocuments(HearingDocuments.builder()
                .courtBundleListV2(courtBundleList).build())
            .hearingDocumentsHearingList(selectedHearingId.toString())
            .hearingDetails(hearingBookings)
            .manageDocumentsHearingDocumentType(HearingDocumentType.COURT_BUNDLE)
            .build();

        List<HearingCourtBundle> results = unwrapElements(
            (List<Element<HearingCourtBundle>>) underTest
                .buildHearingDocumentList(caseData).get(COURT_BUNDLE_LIST_KEY));
        assertThat(results).isNotEmpty();
        assertThat(results.get(0).getCourtBundle())
            .isNotEmpty()
            .isEqualTo(editedCourtBundle);
    }

    @Test
    void shouldReturnAdditionalCourtBundleForSelectedHearing() {
        UUID selectedHearingId = randomUUID();
        List<Element<HearingBooking>> hearingBookings = List.of(
            element(selectedHearingId, createHearingBooking(futureDate, futureDate.plusDays(3)))
        );
        List<Element<CourtBundle>> currentCourtBundle = List.of(element(createCourtBundleWithFile("Current filename")));
        List<Element<CourtBundle>> newCourtBundle = new ArrayList<>(currentCourtBundle);
        newCourtBundle.add(element(createCourtBundleWithFile("New filename 1")));
        newCourtBundle.add(element(createCourtBundleWithFile("New filename 2")));

        List<Element<HearingCourtBundle>> courtBundleList = List.of(element(
            selectedHearingId,
            HearingCourtBundle.builder()
                .hearing(hearingBookings.get(0).getValue().toLabel())
                .courtBundle(currentCourtBundle)
                .build()));

        CaseData caseData = CaseData.builder()
            .manageDocumentsCourtBundle(newCourtBundle)
            .hearingDocuments(HearingDocuments.builder()
                .courtBundleListV2(courtBundleList).build())
            .hearingDocumentsHearingList(selectedHearingId.toString())
            .hearingDetails(hearingBookings)
            .manageDocumentsHearingDocumentType(HearingDocumentType.COURT_BUNDLE)
            .build();

        List<HearingCourtBundle> results = unwrapElements(
            (List<Element<HearingCourtBundle>>) underTest
                .buildHearingDocumentList(caseData).get(COURT_BUNDLE_LIST_KEY));
        assertThat(results).isNotEmpty();
        assertThat(results.get(0).getCourtBundle())
            .hasSize(3)
            .isEqualTo(newCourtBundle);
    }

    @Test
    void shouldThrowExceptionWhenBuildingCourtBundleListNotWithBookedHearing() {
        UUID selectedHearingId = randomUUID();
        UUID hearingBookingId = randomUUID();

        List<Element<HearingBooking>> hearingBookings = List.of(
            element(hearingBookingId, createHearingBooking(futureDate, futureDate.plusDays(3)))
        );

        CaseData caseData = CaseData.builder()
            .hearingDocumentsHearingList(selectedHearingId.toString())
            .hearingDetails(hearingBookings)
            .manageDocumentsHearingDocumentType(HearingDocumentType.COURT_BUNDLE)
            .build();

        assertThatThrownBy(() -> underTest.buildHearingDocumentList(caseData))
            .isInstanceOf(NoHearingBookingException.class);
    }

    @Test
    void shouldThrowExceptionWhenInitialisingCourtBundleListNotWithBookedHearing() {
        UUID selectedHearingId = randomUUID();
        UUID hearingBookingId = randomUUID();

        List<Element<HearingBooking>> hearingBookings = List.of(
            element(hearingBookingId, createHearingBooking(futureDate, futureDate.plusDays(3)))
        );

        CaseData caseData = CaseData.builder()
            .hearingDocumentsHearingList(selectedHearingId.toString())
            .hearingDetails(hearingBookings)
            .build();

        assertThatThrownBy(() -> underTest.initialiseHearingDocumentFields(caseData))
            .isInstanceOf(NoHearingBookingException.class);
    }

    @Test
    void shouldNotInitialiseCourtBundleFieldsIfBundleHearingDifferentToSelected() {
        UUID selectedHearingId = randomUUID();
        UUID courtBundleHearingId = randomUUID();

        List<Element<HearingBooking>> hearingBookings = List.of(
            element(selectedHearingId, createHearingBooking(futureDate, futureDate.plusDays(3)))
        );
        List<Element<CourtBundle>> courtBundle = List.of(element(createCourtBundleWithFile("Current filename")));
        List<Element<HearingCourtBundle>> courtBundleList = List.of(element(
            courtBundleHearingId,
            HearingCourtBundle.builder()
                .hearing(hearingBookings.get(0).getValue().toLabel())
                .courtBundle(courtBundle)
                .build()));

        CaseData caseData = CaseData.builder()
            .manageDocumentsCourtBundle(courtBundle)
            .hearingDocuments(HearingDocuments.builder()
                .courtBundleListV2(courtBundleList).build())
            .hearingDocumentsHearingList(selectedHearingId.toString())
            .hearingDetails(hearingBookings)
            .manageDocumentsHearingDocumentType(HearingDocumentType.COURT_BUNDLE)
            .build();

        Map<String, Object> map = underTest.initialiseHearingDocumentFields(caseData);
        List<CourtBundle> result = getCourtBundleFromMap(map);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDocument()).isNull();
    }

    @Test
    void shouldThrowExceptionWhenInitialisingCourtBundleFieldsNotWithBookedHearing() {
        UUID selectedHearingId = randomUUID();
        UUID courtBundleHearingId = randomUUID();

        List<Element<HearingBooking>> hearingBookings = List.of(
            element(courtBundleHearingId, createHearingBooking(futureDate, futureDate.plusDays(3)))
        );
        List<Element<CourtBundle>> courtBundle = List.of(element(createCourtBundleWithFile("Current filename")));
        List<Element<HearingCourtBundle>> courtBundleList = List.of(element(
            courtBundleHearingId,
            HearingCourtBundle.builder()
                .hearing(hearingBookings.get(0).getValue().toLabel())
                .courtBundle(courtBundle)
                .build()));

        CaseData caseData = CaseData.builder()
            .manageDocumentsCourtBundle(courtBundle)
            .hearingDocuments(HearingDocuments.builder()
                .courtBundleListV2(courtBundleList).build())
            .hearingDocumentsHearingList(selectedHearingId.toString())
            .hearingDetails(hearingBookings)
            .build();

        assertThatThrownBy(() -> underTest.initialiseHearingDocumentFields(caseData))
            .isInstanceOf(NoHearingBookingException.class);
    }

    private List<CourtBundle> getCourtBundleFromMap(Map<String, Object> map) {
        if (map.containsKey(COURT_BUNDLE_KEY)) {
            List value = (List) map.get(COURT_BUNDLE_KEY);
            return unwrapElements((List<Element<CourtBundle>>) value);
        }
        return null;
    }

    @Test
    void shouldInitialiseHearingDocumentFields() {
        UUID selectedHearingId = randomUUID();
        LocalDateTime today = LocalDateTime.now();

        HearingBooking selectedHearingBooking = createHearingBooking(today, today.plusDays(3));
        List<Element<HearingBooking>> hearingBookings = List.of(element(selectedHearingId, selectedHearingBooking));

        CaseData caseData = CaseData.builder()
            .hearingDetails(hearingBookings)
            .hearingDocumentsHearingList(selectedHearingId.toString())
            .manageDocumentsHearingDocumentType(HearingDocumentType.COURT_BUNDLE)
            .build();

        assertThat(getCourtBundleFromMap(underTest
            .initialiseHearingDocumentFields(caseData)))
            .isEqualTo(List.of(CourtBundle.builder().build()));

        assertThat((CaseSummary) underTest
            .initialiseHearingDocumentFields(caseData.toBuilder()
                .manageDocumentsHearingDocumentType(HearingDocumentType.CASE_SUMMARY).build())
            .get(CASE_SUMMARY_KEY))
            .isEqualTo(CaseSummary.builder().hearing(selectedHearingBooking.toLabel()).build());

        assertThat((PositionStatementChild) underTest
            .initialiseHearingDocumentFields(caseData.toBuilder()
                .manageDocumentsHearingDocumentType(HearingDocumentType.POSITION_STATEMENT_CHILD).build())
            .get(POSITION_STATEMENT_CHILD_KEY))
            .isEqualTo(PositionStatementChild.builder()
                .hearing(selectedHearingBooking.toLabel())
                .hearingId(selectedHearingId).build());

        assertThat((PositionStatementRespondent) underTest
            .initialiseHearingDocumentFields(caseData.toBuilder()
                .manageDocumentsHearingDocumentType(HearingDocumentType.POSITION_STATEMENT_RESPONDENT).build())
            .get(POSITION_STATEMENT_RESPONDENT_KEY))
            .isEqualTo(PositionStatementRespondent.builder()
                .hearing(selectedHearingBooking.toLabel())
                .hearingId(selectedHearingId).build());

        assertThat((SkeletonArgument) underTest
            .initialiseHearingDocumentFields(caseData.toBuilder()
                .manageDocumentsHearingDocumentType(HearingDocumentType.SKELETON_ARGUMENT).build())
            .get(SKELETON_ARGUMENT_KEY))
            .isEqualTo(SkeletonArgument.builder()
                .hearing(selectedHearingBooking.toLabel())
                .hearingId(selectedHearingId).build());
    }

    @Test
    void shouldReturnNewCaseSummaryListWhenNoExistingCaseSummaryPresentForSelectedHearing() {
        UUID selectedHearingId = randomUUID();
        LocalDateTime today = LocalDateTime.now();

        HearingBooking selectedHearingBooking = createHearingBooking(today, today.plusDays(3));
        List<Element<HearingBooking>> hearingBookings = List.of(element(selectedHearingId, selectedHearingBooking));

        CaseData caseData = CaseData.builder()
            .manageDocumentsCaseSummary(CaseSummary.builder().hearing("Test hearing").build())
            .hearingDocumentsHearingList(selectedHearingId.toString())
            .manageDocumentsHearingDocumentType(HearingDocumentType.CASE_SUMMARY)
            .hearingDetails(hearingBookings)
            .build();

        assertThat(underTest.buildHearingDocumentList(caseData).get(CASE_SUMMARY_LIST_KEY))
            .isEqualTo(List.of(element(selectedHearingId, caseData.getManageDocumentsCaseSummary())));
    }

    @Test
    void shouldReturnNewPositionStatementChildListWhenNoExistingListPresentForSelectedHearing() {
        UUID selectedHearingId = randomUUID();
        LocalDateTime today = LocalDateTime.now();

        UUID selectedChildId = randomUUID();
        DynamicList childrenDynamicList = TestDataHelper.buildDynamicList(0,
            Pair.of(selectedChildId, "Tom Smith"),
            Pair.of(randomUUID(), "Mary Smith")
        );

        PositionStatementChild positionStatementChild =
            PositionStatementChild.builder()
                .hearing("Test hearing")
                .hearingId(selectedHearingId)
                .childId(selectedChildId)
                .childName("Tom Smith")
                .build();

        HearingBooking selectedHearingBooking = createHearingBooking(today, today.plusDays(3));
        List<Element<HearingBooking>> hearingBookings = List.of(element(selectedHearingId, selectedHearingBooking));

        CaseData caseData = CaseData.builder()
            .manageDocumentsPositionStatementChild(positionStatementChild)
            .hearingDocumentsHearingList(selectedHearingId.toString())
            .manageDocumentsChildrenList(childrenDynamicList)
            .manageDocumentsHearingDocumentType(HearingDocumentType.POSITION_STATEMENT_CHILD)
            .hearingDetails(hearingBookings)
            .build();

        assertThat(
            unwrapElements((List<Element<PositionStatementChild>>) underTest.buildHearingDocumentList(caseData)
                .get(POSITION_STATEMENT_CHILD_LIST_KEY)))
            .isEqualTo(List.of(caseData.getManageDocumentsPositionStatementChild()));
    }

    @Test
    void shouldAddNewPositionStatementChildToTheListWhenExistingListPresentForSelectedHearing() {
        UUID selectedHearingId = randomUUID();
        LocalDateTime today = LocalDateTime.now();

        UUID selectedChildId = randomUUID();
        UUID childTwoId = randomUUID();
        DynamicList childrenDynamicList = TestDataHelper.buildDynamicList(0,
            Pair.of(selectedChildId, "Tom Smith"),
            Pair.of(childTwoId, "Mary Smith")
        );

        PositionStatementChild positionStatementChild =
            PositionStatementChild.builder()
                .hearing("Test hearing")
                .hearingId(selectedHearingId)
                .childId(selectedChildId)
                .childName("Tom Smith")
                .build();

        PositionStatementChild positionStatementChildTwo =
            PositionStatementChild.builder()
                .hearing("Test hearing")
                .hearingId(selectedHearingId)
                .childId(childTwoId)
                .childName("Mary Smith")
                .build();

        HearingBooking selectedHearingBooking = createHearingBooking(today, today.plusDays(3));
        List<Element<HearingBooking>> hearingBookings = List.of(element(selectedHearingId, selectedHearingBooking));

        CaseData caseData = CaseData.builder()
            .manageDocumentsPositionStatementChild(positionStatementChild)
            .hearingDocumentsHearingList(selectedHearingId.toString())
            .manageDocumentsChildrenList(childrenDynamicList)
            .manageDocumentsHearingDocumentType(HearingDocumentType.POSITION_STATEMENT_CHILD)
            .hearingDetails(hearingBookings)
            .hearingDocuments(HearingDocuments.builder()
                .positionStatementChildListV2(List.of(element(positionStatementChildTwo))).build())
            .build();

        assertThat(
            unwrapElements((List<Element<PositionStatementChild>>)
                underTest.buildHearingDocumentList(caseData).get(POSITION_STATEMENT_CHILD_LIST_KEY)))
            .isEqualTo(List.of(positionStatementChildTwo, caseData.getManageDocumentsPositionStatementChild()));
    }

    @Test
    void shouldReturnCombinedPositionStatementChildListWhenExistingListPresentForOtherHearing() {
        UUID selectedHearingId = randomUUID();
        LocalDateTime today = LocalDateTime.now();

        UUID selectedChildId = randomUUID();
        UUID childTwoId = randomUUID();
        DynamicList childrenDynamicList = TestDataHelper.buildDynamicList(0,
            Pair.of(selectedChildId, "Tom Smith"),
            Pair.of(childTwoId, "Mary Smith")
        );

        PositionStatementChild positionStatementChild =
            PositionStatementChild.builder()
                .hearing("Test hearing")
                .hearingId(selectedHearingId)
                .childId(selectedChildId)
                .childName("Tom Smith")
                .build();

        PositionStatementChild positionStatementChildTwo =
            PositionStatementChild.builder()
                .hearing("Other hearing")
                .hearingId(randomUUID())
                .childId(childTwoId)
                .childName("Mary Smith")
                .build();

        HearingBooking selectedHearingBooking = createHearingBooking(today, today.plusDays(3));
        List<Element<HearingBooking>> hearingBookings = List.of(element(selectedHearingId, selectedHearingBooking));

        CaseData caseData = CaseData.builder()
            .manageDocumentsPositionStatementChild(positionStatementChild)
            .hearingDocumentsHearingList(selectedHearingId.toString())
            .manageDocumentsChildrenList(childrenDynamicList)
            .manageDocumentsHearingDocumentType(HearingDocumentType.POSITION_STATEMENT_CHILD)
            .hearingDetails(hearingBookings)
            .hearingDocuments(HearingDocuments.builder()
                .positionStatementChildListV2(List.of(element(positionStatementChildTwo))).build())
            .build();

        assertThat(
            unwrapElements((List<Element<PositionStatementChild>>)
                underTest.buildHearingDocumentList(caseData).get(POSITION_STATEMENT_CHILD_LIST_KEY)))
            .containsExactlyInAnyOrder(positionStatementChild, positionStatementChildTwo);
    }

    @Test
    void shouldReturnNewPositionStatementRespondentListWhenNoExistingListPresentForSelectedHearing() {
        UUID selectedHearingId = randomUUID();
        LocalDateTime today = LocalDateTime.now();

        UUID selectedRespondentId = randomUUID();
        DynamicList respondentDynamicList = TestDataHelper.buildDynamicList(0,
            Pair.of(selectedRespondentId, "Tom Smith"),
            Pair.of(randomUUID(), "Mary Smith")
        );

        PositionStatementRespondent positionStatementRespondent =
            PositionStatementRespondent.builder()
                .hearing("Test hearing")
                .hearingId(selectedHearingId)
                .respondentId(selectedRespondentId)
                .respondentName("Tom Smith")
                .build();

        HearingBooking selectedHearingBooking = createHearingBooking(today, today.plusDays(3));
        List<Element<HearingBooking>> hearingBookings = List.of(element(selectedHearingId, selectedHearingBooking));

        CaseData caseData = CaseData.builder()
            .manageDocumentsPositionStatementRespondent(positionStatementRespondent)
            .hearingDocumentsHearingList(selectedHearingId.toString())
            .hearingDocumentsRespondentList(respondentDynamicList)
            .manageDocumentsHearingDocumentType(HearingDocumentType.POSITION_STATEMENT_RESPONDENT)
            .hearingDetails(hearingBookings)
            .build();

        assertThat(
            unwrapElements((List<Element<PositionStatementRespondent>>)
                underTest.buildHearingDocumentList(caseData).get(POSITION_STATEMENT_RESPONDENT_LIST_KEY)))
            .isEqualTo(List.of(caseData.getManageDocumentsPositionStatementRespondent()));
    }

    @Test
    void shouldAddNewPositionStatementRespondentToTheListWhenExistingListPresentForSelectedHearing() {
        UUID selectedHearingId = randomUUID();
        LocalDateTime today = LocalDateTime.now();

        UUID selectedRespondentId = randomUUID();
        UUID respondentTwoId = randomUUID();
        DynamicList respondentDynamicList = TestDataHelper.buildDynamicList(0,
            Pair.of(selectedRespondentId, "Tom Smith"),
            Pair.of(respondentTwoId, "Mary Smith")
        );

        PositionStatementRespondent positionStatementRespondent =
            PositionStatementRespondent.builder()
                .hearing("Test hearing")
                .hearingId(selectedHearingId)
                .respondentId(selectedRespondentId)
                .respondentName("Tom Smith")
                .build();

        PositionStatementRespondent positionStatementRespondentTwo =
            PositionStatementRespondent.builder()
                .hearing("Test hearing")
                .hearingId(selectedHearingId)
                .respondentId(respondentTwoId)
                .respondentName("Mary Smith")
                .build();

        HearingBooking selectedHearingBooking = createHearingBooking(today, today.plusDays(3));
        List<Element<HearingBooking>> hearingBookings = List.of(element(selectedHearingId, selectedHearingBooking));

        CaseData caseData = CaseData.builder()
            .manageDocumentsPositionStatementRespondent(positionStatementRespondent)
            .hearingDocumentsHearingList(selectedHearingId.toString())
            .hearingDocumentsRespondentList(respondentDynamicList)
            .manageDocumentsHearingDocumentType(HearingDocumentType.POSITION_STATEMENT_RESPONDENT)
            .hearingDetails(hearingBookings)
            .hearingDocuments(HearingDocuments.builder()
                .positionStatementRespondentListV2(List.of(element(positionStatementRespondentTwo)))
                .build())
            .build();

        assertThat(
            unwrapElements((List<Element<PositionStatementRespondent>>) underTest
                .buildHearingDocumentList(caseData).get(POSITION_STATEMENT_RESPONDENT_LIST_KEY)))
            .isEqualTo(List.of(positionStatementRespondentTwo,
                caseData.getManageDocumentsPositionStatementRespondent()));
    }

    @Test
    void shouldReturnNewPositionStatementRespondentListWhenExistingListPresentForOtherHearing() {
        UUID selectedHearingId = randomUUID();
        LocalDateTime today = LocalDateTime.now();

        UUID selectedRespondentId = randomUUID();
        UUID respondentTwoId = randomUUID();
        DynamicList respondentDynamicList = TestDataHelper.buildDynamicList(0,
            Pair.of(selectedRespondentId, "Tom Smith"),
            Pair.of(respondentTwoId, "Mary Smith")
        );

        PositionStatementRespondent positionStatementRespondent =
            PositionStatementRespondent.builder()
                .hearing("Test hearing")
                .hearingId(selectedHearingId)
                .respondentId(selectedRespondentId)
                .respondentName("Tom Smith")
                .build();

        PositionStatementRespondent positionStatementRespondentTwo =
            PositionStatementRespondent.builder()
                .hearing("Other hearing")
                .hearingId(randomUUID())
                .respondentId(respondentTwoId)
                .respondentName("Mary Smith")
                .build();

        HearingBooking selectedHearingBooking = createHearingBooking(today, today.plusDays(3));
        List<Element<HearingBooking>> hearingBookings = List.of(element(selectedHearingId, selectedHearingBooking));

        CaseData caseData = CaseData.builder()
            .manageDocumentsPositionStatementRespondent(positionStatementRespondent)
            .hearingDocumentsHearingList(selectedHearingId.toString())
            .hearingDocumentsRespondentList(respondentDynamicList)
            .manageDocumentsHearingDocumentType(HearingDocumentType.POSITION_STATEMENT_RESPONDENT)
            .hearingDetails(hearingBookings)
            .hearingDocuments(HearingDocuments.builder()
                .positionStatementRespondentListV2(List.of(element(positionStatementRespondentTwo)))
                .build())
            .build();

        assertThat(
            unwrapElements((List<Element<PositionStatementRespondent>>) underTest
                .buildHearingDocumentList(caseData).get(POSITION_STATEMENT_RESPONDENT_LIST_KEY)))
            .containsExactlyInAnyOrder(positionStatementRespondent, positionStatementRespondentTwo);
    }

    @Test
    void shouldReplaceCaseSummaryIfSameHearing() {
        UUID hearingOne = randomUUID();
        UUID hearingTwo = randomUUID();
        String hearingOneLabel = "Hearing one";
        String hearingTwoLabel = "Hearing two";

        LocalDateTime today = LocalDateTime.now();
        HearingBooking hearingBookingOne = createHearingBooking(today, today.plusDays(3));
        HearingBooking hearingBookingTwo = createHearingBooking(today, today.plusDays(5));
        List<Element<HearingBooking>> hearingBookings = List.of(element(hearingOne, hearingBookingOne),
            element(hearingTwo, hearingBookingTwo));

        CaseSummary existingHearingOne = CaseSummary.builder().hearing(hearingOneLabel).build();
        CaseSummary existingHearingTwo = CaseSummary.builder().hearing(hearingTwoLabel).build();
        CaseSummary newHearingTwo = CaseSummary.builder().hearing(hearingTwoLabel).build();

        CaseData caseData = CaseData.builder()
            .manageDocumentsHearingDocumentType(HearingDocumentType.CASE_SUMMARY)
            .hearingDetails(hearingBookings)
            .manageDocumentsCaseSummary(newHearingTwo)
            .hearingDocumentsHearingList(hearingTwo.toString())
            .hearingDocuments(HearingDocuments.builder()
                .caseSummaryList(List.of(element(hearingOne, existingHearingOne),
                    element(hearingTwo, existingHearingTwo)))
                .build())
            .build();

        assertThat(unwrapElements((List<Element<CaseSummary>>) underTest.buildHearingDocumentList(caseData)
            .get(CASE_SUMMARY_LIST_KEY))).containsExactlyInAnyOrder(existingHearingOne, newHearingTwo);
    }

    @Test
    void shouldAddNewSkeletonArgument() {
        UUID selectedHearingId = randomUUID();
        LocalDateTime today = LocalDateTime.now();
        UUID childId = randomUUID();
        final String PARTY_NAME = "Tom Smith";
        final String USER = "HMCTS";

        DynamicList partyDynamicList = TestDataHelper.buildDynamicList(0,
            Pair.of(childId, PARTY_NAME)
        );

        SkeletonArgument skeletonArgument =
            SkeletonArgument.builder()
                .hearing("Test hearing")
                .hearingId(selectedHearingId)
                .partyId(childId)
                .partyName(PARTY_NAME)
                .build();

        HearingBooking selectedHearingBooking = createHearingBooking(today, today.plusDays(3));
        List<Element<HearingBooking>> hearingBookings = List.of(element(selectedHearingId, selectedHearingBooking));

        CaseData caseData = CaseData.builder()
            .manageDocumentsSkeletonArgument(skeletonArgument)
            .hearingDocumentsHearingList(selectedHearingId.toString())
            .hearingDocumentsPartyList(partyDynamicList)
            .manageDocumentsHearingDocumentType(HearingDocumentType.SKELETON_ARGUMENT)
            .hearingDetails(hearingBookings)
            .build();

        List<Element<SkeletonArgument>> skeletonArgumentUnderTest = (List<Element<SkeletonArgument>>) underTest
            .buildHearingDocumentList(caseData).get(SKELETON_ARGUMENT_LIST_KEY);

        assertThat(unwrapElements(skeletonArgumentUnderTest)).hasSize(1)
            .first()
            .extracting(SkeletonArgument::getDateTimeUploaded, SkeletonArgument::getUploadedBy,
                        SkeletonArgument::getPartyName, SkeletonArgument::getPartyId)
            .containsExactly(time.now(), USER, PARTY_NAME, childId);
    }

    @Test
    void shouldUpdatePlacementNoticesForLA() {
        final DocumentReference laResponseRef = testDocumentReference();

        final PlacementNoticeDocument laResponse = PlacementNoticeDocument.builder()
            .response(laResponseRef)
            .responseDescription("LA response")
            .type(PlacementNoticeDocument.RecipientType.LOCAL_AUTHORITY)
            .build();

        final Placement placement = Placement.builder()
            .placementNotice(testDocumentReference())
            .childName("Test Child")
            .noticeDocuments(wrapElements(laResponse))
            .build();

        final PlacementEventData uploadLAData = PlacementEventData.builder()
            .placements(wrapElements(placement))
            .placement(placement)
            .build();

        when(placementService.savePlacementNoticeResponses(any(),
            eq(PlacementNoticeDocument.RecipientType.LOCAL_AUTHORITY)))
            .thenReturn(uploadLAData);


        Placement placementBefore = Placement.builder()
            .placementNotice(placement.getPlacementNotice())
            .childName("Test Child")
            .build();

        PlacementNoticeDocument toAdd = PlacementNoticeDocument.builder()
            .response(laResponseRef)
            .responseDescription("LA response")
            .build();

        CaseData caseData = CaseData.builder()
            .placementEventData(PlacementEventData.builder()
                .placements(wrapElements(placementBefore))
                .placement(placementBefore)
                .build())
            .placementNoticeResponses(wrapElements(toAdd))
            .build();

        PlacementEventData after = underTest.updatePlacementNoticesLA(caseData);

        Placement updated = after.getPlacements().get(0).getValue();
        PlacementNoticeDocument updatedNoticeDocument = updated.getNoticeDocuments().get(0).getValue();

        assertThat(after.getPlacements()).hasSize(1);
        assertThat(updated.getChildName()).contains("Test Child");
        assertThat(updated.getNoticeDocuments()).hasSize(1);
        assertThat(updatedNoticeDocument.getType()).isEqualTo(PlacementNoticeDocument.RecipientType.LOCAL_AUTHORITY);
        assertThat(updatedNoticeDocument.getResponse()).isEqualTo(laResponseRef);
        assertThat(updatedNoticeDocument.getResponseDescription()).isEqualTo("LA response");
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

    private ManageDocument buildFurtherEvidenceManagementDocument() {
        return ManageDocument.builder().type(FURTHER_EVIDENCE_DOCUMENTS).build();
    }

    private HearingBooking buildFinalHearingBooking() {
        return HearingBooking.builder()
            .type(HearingType.FINAL)
            .startDate(time.now())
            .build();
    }

    private C2DocumentBundle buildC2DocumentBundle(UUID id, LocalDateTime dateTime) {
        return buildC2DocumentBundle(dateTime).toBuilder().id(id).build();
    }

    private C2DocumentBundle buildC2DocumentBundle(LocalDateTime dateTime) {
        return C2DocumentBundle.builder()
            .id(UUID.randomUUID())
            .uploadedDateTime(formatLocalDateTimeBaseUsingFormat(dateTime, DATE_TIME))
            .build();
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
            .id(id)
            .applicationType(type)
            .uploadedDateTime(formatLocalDateTimeBaseUsingFormat(time, DATE_TIME))
            .build();
    }

    private DynamicList buildDynamicList(UUID selectedId) {
        return DynamicList.builder()
            .value(DynamicListElement.builder()
                .code(selectedId)
                .build())
            .build();
    }

    private CourtBundle createCourtBundleWithFile(String filename) {
        return CourtBundle.builder()
            .document(
                DocumentReference.builder()
                    .filename(filename)
                    .build())
            .build();
    }

    private static final long CASE_ID = 12345L;

    @ParameterizedTest
    @EnumSource(value = CaseRole.class, names = {
        "SOLICITOR",
        "SOLICITORA", "SOLICITORB", "SOLICITORC", "SOLICITORD", "SOLICITORE",
        "SOLICITORF", "SOLICITORG", "SOLICITORH", "SOLICITORI", "SOLICITORJ",
        "CAFCASSSOLICITOR",
        "CHILDSOLICITORA", "CHILDSOLICITORB", "CHILDSOLICITORC", "CHILDSOLICITORD",
        "CHILDSOLICITORE", "CHILDSOLICITORF", "CHILDSOLICITORG", "CHILDSOLICITORH", "CHILDSOLICITORI",
        "CHILDSOLICITORJ", "CHILDSOLICITORK", "CHILDSOLICITORL", "CHILDSOLICITORM", "CHILDSOLICITORN",
        "CHILDSOLICITORO"
    })
    void shouldReturnSolicitorUploaderType(CaseRole caseRole) {
        when(userService.getCaseRoles(CASE_ID)).thenReturn(Set.of(caseRole));
        when(userService.isHmctsUser()).thenReturn(false);

        CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .build();

        assertThat(underTest.getUploaderType(caseData)).isEqualTo(DocumentUploaderType.SOLICITOR);
    }

    @ParameterizedTest
    @EnumSource(value = CaseRole.class, names = {"BARRISTER"})
    void shouldReturnBarristerUploaderType(CaseRole caseRole) {
        when(userService.getCaseRoles(CASE_ID)).thenReturn(Set.of(caseRole));
        when(userService.isHmctsUser()).thenReturn(false);

        CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .build();

        assertThat(underTest.getUploaderType(caseData)).isEqualTo(DocumentUploaderType.BARRISTER);
    }

    @ParameterizedTest
    @EnumSource(value = CaseRole.class, names = {"LASHARED"})
    void shouldReturnSecondaryLocalAuthorityUploaderType(CaseRole caseRole) {
        when(userService.getCaseRoles(CASE_ID)).thenReturn(Set.of(caseRole));
        when(userService.isHmctsUser()).thenReturn(false);

        CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .build();

        assertThat(underTest.getUploaderType(caseData)).isEqualTo(DocumentUploaderType.SECONDARY_LOCAL_AUTHORITY);
    }

    @Test
    void shouldReturnHmctsUploaderType() {
        when(userService.getCaseRoles(CASE_ID)).thenReturn(Set.of());
        when(userService.isHmctsUser()).thenReturn(true);

        CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .build();

        assertThat(underTest.getUploaderType(caseData)).isEqualTo(DocumentUploaderType.HMCTS);
    }

    @ParameterizedTest
    @EnumSource(value = CaseRole.class, names = {"LASOLICITOR", "EPSMANAGING", "LAMANAGING", "LABARRISTER"})
    void shouldReturnDesignatedLocalAuthorityUploaderType(CaseRole caseRole) {
        when(userService.getCaseRoles(CASE_ID)).thenReturn(Set.of(caseRole));
        when(userService.isHmctsUser()).thenReturn(false);

        CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .build();

        assertThat(underTest.getUploaderType(caseData)).isEqualTo(DocumentUploaderType.DESIGNATED_LOCAL_AUTHORITY);
    }

    @Test
    void shouldThrowExceptionIfUnableToDetermineDocumentUploaderType() {
        when(userService.getCaseRoles(CASE_ID)).thenReturn(Set.of(CaseRole.CREATOR));
        when(userService.isHmctsUser()).thenReturn(false);

        CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .build();

        IllegalStateException thrownException = assertThrows(IllegalStateException.class,
            () -> underTest.getUploaderType(caseData));
        assertThat(thrownException.getMessage()).contains("Unable to determine document uploader type");
    }

    @ParameterizedTest
    @EnumSource(value = CaseRole.class, names = {"BARRISTER",
        "SOLICITOR",
        "SOLICITORA", "SOLICITORB", "SOLICITORC", "SOLICITORD", "SOLICITORE",
        "SOLICITORF", "SOLICITORG", "SOLICITORH", "SOLICITORI", "SOLICITORJ",
        "CAFCASSSOLICITOR",
        "CHILDSOLICITORA", "CHILDSOLICITORB", "CHILDSOLICITORC", "CHILDSOLICITORD", "CHILDSOLICITORE",
        "CHILDSOLICITORF", "CHILDSOLICITORG", "CHILDSOLICITORH", "CHILDSOLICITORI", "CHILDSOLICITORJ",
        "CHILDSOLICITORK", "CHILDSOLICITORL", "CHILDSOLICITORM", "CHILDSOLICITORN", "CHILDSOLICITORO"
    })
    void shouldNotAllowMarkDocumentConfidential(CaseRole caseRole) {
        when(userService.getCaseRoles(CASE_ID)).thenReturn(Set.of(caseRole));
        when(userService.isHmctsUser()).thenReturn(false);

        CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .build();

        assertThat(underTest.allowMarkDocumentConfidential(caseData)).isEqualTo(false);
    }

    @ParameterizedTest
    @EnumSource(value = CaseRole.class, names = {"LASHARED", "LASOLICITOR", "EPSMANAGING", "LAMANAGING", "LABARRISTER"})
    void shouldAllowMarkDocumentConfidential(CaseRole caseRole) {
        when(userService.getCaseRoles(CASE_ID)).thenReturn(Set.of(caseRole));
        when(userService.isHmctsUser()).thenReturn(false);

        CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .build();

        assertThat(underTest.allowMarkDocumentConfidential(caseData)).isEqualTo(true);
    }

    @Test
    void shouldAllowMarkDocumentConfidentialForHmctsUser() {
        when(userService.getCaseRoles(CASE_ID)).thenReturn(Set.of());
        when(userService.isHmctsUser()).thenReturn(true);

        CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .build();

        assertThat(underTest.allowMarkDocumentConfidential(caseData)).isEqualTo(true);
    }

    private static Pair<String, String> toPair(DocumentType documentType) {
        return Pair.of(documentType.name(), documentType.getDescription());
    }

    private static Stream<Arguments> buildDocumentTypeDynamicListArgs() {
        List<Arguments> args = new ArrayList<>();
        for (int i = 1; i < 5; i++) {
            for (int b = 0; b < 2; b++) {
                List<Pair<String, String>> expected = List.of(
                    toPair(DocumentType.COURT_BUNDLE),
                    toPair(DocumentType.CASE_SUMMARY),
                    toPair(DocumentType.POSITION_STATEMENTS),
                    toPair(DocumentType.THRESHOLD),
                    toPair(DocumentType.SKELETON_ARGUMENTS),
                    toPair(DocumentType.AA_PARENT_ORDERS),
                    toPair(DocumentType.JUDGEMENTS),
                    toPair(DocumentType.TRANSCRIPTS),
                    toPair(DocumentType.AA_PARENT_APPLICANTS_DOCUMENTS),
                    toPair(DocumentType.DOCUMENTS_FILED_ON_ISSUE),
                    toPair(DocumentType.APPLICANTS_WITNESS_STATEMENTS),
                    toPair(DocumentType.CARE_PLAN),
                    toPair(DocumentType.PARENT_ASSESSMENTS),
                    toPair(DocumentType.FAMILY_AND_VIABILITY_ASSESSMENTS),
                    toPair(DocumentType.APPLICANTS_OTHER_DOCUMENTS),
                    toPair(DocumentType.MEETING_NOTES),
                    toPair(DocumentType.CONTACT_NOTES),
                    toPair(DocumentType.AA_PARENT_RESPONDENTS_STATEMENTS),
                    toPair(DocumentType.RESPONDENTS_STATEMENTS),
                    toPair(DocumentType.RESPONDENTS_WITNESS_STATEMENTS),
                    toPair(DocumentType.GUARDIAN_EVIDENCE),
                    toPair(DocumentType.AA_PARENT_EXPERT_REPORTS),
                    toPair(DocumentType.EXPERT_REPORTS),
                    toPair(DocumentType.DRUG_AND_ALCOHOL_REPORTS),
                    toPair(DocumentType.LETTER_OF_INSTRUCTION),
                    toPair(DocumentType.POLICE_DISCLOSURE),
                    toPair(DocumentType.MEDICAL_RECORDS),
                    toPair(DocumentType.COURT_CORRESPONDENCE),
                    toPair(DocumentType.NOTICE_OF_ACTING_OR_ISSUE),
                    b == 0 ? toPair(DocumentType.PLACEMENT_RESPONSES) : Pair.of("", ""));
                args.add(Arguments.of(i, b == 0, expected.stream()
                    .filter(p -> !Pair.of("", "").equals(p))
                    .collect(Collectors.toList())
                ));
            }
        }
        return args.stream();
    }

    @ParameterizedTest
    @MethodSource("buildDocumentTypeDynamicListArgs")
    void shouldBuildDocumentTypeDynamicList(int loginType,
                                            boolean hasPlacement,
                                            List<Pair<String, String>> expectedPairList) {
        initialiseUserService(loginType);

        CaseData caseData = hasPlacement ? CaseData.builder().id(CASE_ID)
            .placementEventData(
                PlacementEventData.builder()
                    .placements(List.of(element(Placement.builder()
                        .placementNotice(TestDataHelper.testDocumentReference())
                        .build())))
                    .build())
            .build()
            : CaseData.builder().id(CASE_ID).build();

        DynamicList expectedDynamicList = DynamicList.builder()
            .value(DynamicListElement.builder().label("SUCCESS").code("SUCCESS").build())
            .build();
        when(dynamicListService.asDynamicList(expectedPairList)).thenReturn(expectedDynamicList);

        assertThat(underTest.buildDocumentTypeDynamicList(caseData)).isEqualTo(expectedDynamicList);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3})
    void shouldReturnEmptyMapIfUploadableBundleIsEmpty(int loginType) {
        initialiseUserService(loginType);

        CaseData caseData = CaseData.builder().id(CASE_ID)
            .manageDocumentEventData(ManageDocumentEventData.builder()
                .manageDocumentAction(ManageDocumentAction.UPLOAD_DOCUMENTS)
                .documentAcknowledge(List.of("ACK_RELATED_TO_CASE"))
                .uploadableDocumentBundle(List.of())
                .build())
            .build();

        assertThat(underTest.uploadDocuments(caseData)).isEqualTo(Map.of());
    }

    private static Stream<Arguments> buildUploadingDocumentArgs() {
        List<Arguments> args = new ArrayList<>();
        for (int i = 1; i < 5; i++) {
            for (Confidentiality c : Confidentiality.values()) {
                args.add(Arguments.of(i, c));
            }
        }
        return args.stream();
    }

    private String toConfidential(Confidentiality confidentiality) {
        switch (confidentiality) {
            case YES:
                return YES.name();
            case NO:
                return NO.name();
            case NULL:
            default:
                return null;
        }
    }

    private String getFieldNameSuffix(DocumentUploaderType uploaderType, Confidentiality confidentiality) {
        String suffix = List.of(DocumentUploaderType.DESIGNATED_LOCAL_AUTHORITY,
            DocumentUploaderType.SECONDARY_LOCAL_AUTHORITY).contains(uploaderType) ? "LA" : "";
        suffix = List.of(DocumentUploaderType.HMCTS).contains(uploaderType) ? "CTSC" : suffix;
        suffix = confidentiality == Confidentiality.YES ? suffix : "";
        return suffix;
    }

    private CaseData prepareCaseDataForUploadDocumentJourney(
        List<Element<UploadableDocumentBundle>> uploadableDocumentBundle) {
        return CaseData.builder().id(CASE_ID)
            .manageDocumentEventData(ManageDocumentEventData.builder()
                .manageDocumentAction(ManageDocumentAction.UPLOAD_DOCUMENTS)
                .documentAcknowledge(List.of("ACK_RELATED_TO_CASE"))
                .uploadableDocumentBundle(uploadableDocumentBundle)
                .build())
            .build();
    }

    private void initialiseUserService(int loginType) {
        when(userService.getCaseRoles(CASE_ID)).thenReturn(new HashSet<>(getUploaderCaseRoles(loginType)));
        when(userService.isHmctsUser()).thenReturn(4 == loginType); // HMCTS for loginType = 4
    }

    private static DocumentUploaderType getUploaderType(int loginType) {
        switch (loginType) {
            case 1:
                return DocumentUploaderType.DESIGNATED_LOCAL_AUTHORITY;
            case 2:
                return DocumentUploaderType.SECONDARY_LOCAL_AUTHORITY;
            case 3:
                return DocumentUploaderType.SOLICITOR;
            case 4:
                return DocumentUploaderType.HMCTS;
            default:
                throw new IllegalStateException("unrecognised loginType: " + loginType);
        }
    }

    private static List<CaseRole> getUploaderCaseRoles(int loginType) {
        switch (loginType) {
            case 1:
                return List.of(CaseRole.LASOLICITOR);
            case 2:
                return List.of(CaseRole.LASHARED);
            case 3:
                return List.of(CaseRole.SOLICITORA);
            case 4:
                return List.of();
            default:
                throw new IllegalStateException("unrecognised loginType: " + loginType);
        }
    }

    private void tplPopulateDocumentListWhenUploadingSingleDocument(DocumentType documentType,
                                                                    Function<String, String> fieldNameProvider,
                                                                    int loginType, Confidentiality confidentiality,
                                                                    Predicate<List> matcher) {
        initialiseUserService(loginType);

        CaseData caseData = prepareCaseDataForUploadDocumentJourney(
            List.of(
                element(elementIdOne, UploadableDocumentBundle.builder()
                    .documentTypeDynamicList(DynamicList.builder()
                        .value(DynamicListElement.builder()
                            .code(documentType.name())
                            .build())
                        .build())
                    .document(expectedDocumentOne)
                    .confidential(toConfidential(confidentiality))
                    .build())
            )
        );

        String suffix = getFieldNameSuffix(getUploaderType(loginType), confidentiality);
        assertThat(underTest.uploadDocuments(caseData))
            .containsKey(fieldNameProvider.apply(suffix))
            .extracting(fieldNameProvider.apply(suffix)).asList()
            .matches(matcher);
    }

    @ParameterizedTest
    @MethodSource("buildUploadingDocumentArgs")
    void shouldPopulateDocumentListWhenUploadASingleParentAssessment(int loginType, Confidentiality confidentiality) {
        tplPopulateDocumentListWhenUploadingSingleDocument(DocumentType.PARENT_ASSESSMENTS,
            suffix -> "parentAssessmentList" + suffix, loginType, confidentiality,
            list -> list.contains(element(elementIdOne, ManagedDocument.builder()
                .document(expectedDocumentOne)
                .markAsConfidential(YesNo.from(confidentiality == Confidentiality.YES).getValue())
                .uploaderType(getUploaderType(loginType))
                .uploaderCaseRoles(getUploaderCaseRoles(loginType))
                .build())));
    }

    @ParameterizedTest
    @MethodSource("buildUploadingDocumentArgs")
    void shouldPopulateDocumentListWhenUploadASingleCaseSummary(int loginType, Confidentiality confidentiality) {
        tplPopulateDocumentListWhenUploadingSingleDocument(DocumentType.CASE_SUMMARY,
            suffix -> "caseSummaryList" + suffix, loginType, confidentiality,
            list -> list.contains(element(elementIdOne, CaseSummary.builder()
                .document(expectedDocumentOne)
                .markAsConfidential(YesNo.from(confidentiality == Confidentiality.YES).getValue())
                .uploaderType(getUploaderType(loginType))
                .uploaderCaseRoles(getUploaderCaseRoles(loginType))
                .build())));
    }

    @ParameterizedTest
    @MethodSource("buildUploadingDocumentArgs")
    void shouldPopulateDocumentListWhenUploadASingleSkeletonArgument(int loginType, Confidentiality confidentiality) {
        tplPopulateDocumentListWhenUploadingSingleDocument(DocumentType.SKELETON_ARGUMENTS,
            suffix -> "skeletonArgumentList" + suffix, loginType, confidentiality,
            list -> list.contains(element(elementIdOne, SkeletonArgument.builder()
                .document(expectedDocumentOne)
                .markAsConfidential(YesNo.from(confidentiality == Confidentiality.YES).getValue())
                .uploaderType(getUploaderType(loginType))
                .uploaderCaseRoles(getUploaderCaseRoles(loginType))
                .build())));
    }

    @ParameterizedTest
    @MethodSource("buildUploadingDocumentArgs")
    void shouldPopulateDocumentListWhenUploadASingleRespondentStatement(int loginType,
                                                                        Confidentiality confidentiality) {
        tplPopulateDocumentListWhenUploadingSingleDocument(DocumentType.RESPONDENTS_STATEMENTS,
            suffix -> "respStmtList" + suffix, loginType, confidentiality,
            list -> list.contains(element(elementIdOne, RespondentStatementV2.builder()
                .document(expectedDocumentOne)
                .markAsConfidential(YesNo.from(confidentiality == Confidentiality.YES).getValue())
                .uploaderType(getUploaderType(loginType))
                .uploaderCaseRoles(getUploaderCaseRoles(loginType))
                .build())));
    }

    @ParameterizedTest
    @MethodSource("buildUploadingDocumentArgs")
    void shouldPopulateDocumentListWhenUploadASingleCourtBundle(int loginType, Confidentiality confidentiality) {
        tplPopulateDocumentListWhenUploadingSingleDocument(DocumentType.COURT_BUNDLE,
            suffix -> "".equals(suffix) ? "courtBundleListV2" : ("courtBundleList" + suffix),
            loginType, confidentiality,
            list -> {
                List<Element> flist = (List<Element>) list.stream()
                    .filter(p -> elementIdOne.equals(((Element) p).getId()))
                    .collect(Collectors.toList());
                if (flist.size() != 1) {
                    return false;
                } else {
                    return flist.stream().allMatch((s) -> {
                        Object wrapped = s.getValue();
                        if (wrapped.getClass().isAssignableFrom(HearingCourtBundle.class)) {
                            DocumentReference expectedDocument = expectedDocumentOne;

                            HearingCourtBundle hcb = (HearingCourtBundle) wrapped;
                            boolean test = hcb.getCourtBundle() != null;
                            test = test && hcb.getCourtBundle().size() == 1;
                            test = test && expectedDocument.equals(hcb.getCourtBundle().get(0).getValue()
                                .getDocument());
                            test = test && getUploaderType(loginType).equals(hcb.getCourtBundle().get(0).getValue()
                                .getUploaderType());
                            test = test && hcb.getCourtBundleNC() != null;
                            test = test && hcb.getCourtBundleNC().size() == 1;
                            test = test && expectedDocument.equals(hcb.getCourtBundleNC().get(0).getValue()
                                .getDocument());
                            test = test && getUploaderType(loginType).equals(hcb.getCourtBundleNC().get(0).getValue()
                                .getUploaderType());
                            return test;
                        }
                        return false;
                    });
                }
            });
    }

    void tplPopulateDocumentListWhenUploadMultipleDocumentWithTranslationRequirements(
        DocumentType documentType,
        Function<String, String> fieldNameProvider,
        int loginType,
        Confidentiality confidentiality,
        Predicate<List> matcher) {
        initialiseUserService(loginType);
        CaseData caseData = prepareCaseDataForUploadDocumentJourney(
            List.of(
                element(elementIdOne, UploadableDocumentBundle.builder()
                    .documentTypeDynamicList(DynamicList.builder()
                        .value(DynamicListElement.builder()
                            .code(documentType.name())
                            .build())
                        .build())
                    .document(expectedDocumentOne)
                    .translationRequirements(LanguageTranslationRequirement.ENGLISH_TO_WELSH)
                    .confidential(toConfidential(confidentiality))
                    .build()),
                element(elementIdTwo, UploadableDocumentBundle.builder()
                    .documentTypeDynamicList(DynamicList.builder()
                        .value(DynamicListElement.builder()
                            .code(documentType.name())
                            .build())
                        .build())
                    .document(expectedDocumentTwo)
                    .confidential(toConfidential(confidentiality))
                    .translationRequirements(LanguageTranslationRequirement.WELSH_TO_ENGLISH)
                    .build())
            )
        );

        String suffix = getFieldNameSuffix(getUploaderType(loginType), confidentiality);
        assertThat(underTest.uploadDocuments(caseData))
            .containsKey(fieldNameProvider.apply(suffix))
            .extracting(fieldNameProvider.apply(suffix)).asList()
            .matches(matcher);
    }

    @ParameterizedTest
    @MethodSource("buildUploadingDocumentArgs")
    void shouldPopulateDocumentListWhenUploadMultipleParentAssessmentWithTranslationRequirements(
        int loginType,
        Confidentiality confidentiality) {
        tplPopulateDocumentListWhenUploadMultipleDocumentWithTranslationRequirements(DocumentType.PARENT_ASSESSMENTS,
            suffix -> "parentAssessmentList" + suffix, loginType, confidentiality,
            list -> list.contains(element(elementIdOne, ManagedDocument.builder()
                .document(expectedDocumentOne)
                .markAsConfidential(YesNo.from(confidentiality == Confidentiality.YES).getValue())
                .uploaderType(getUploaderType(loginType))
                .uploaderCaseRoles(getUploaderCaseRoles(loginType))
                .translationRequirements(LanguageTranslationRequirement.ENGLISH_TO_WELSH)
                .build()))
                && list.contains(element(elementIdTwo, ManagedDocument.builder()
                .document(expectedDocumentTwo)
                .markAsConfidential(YesNo.from(confidentiality == Confidentiality.YES).getValue())
                .uploaderType(getUploaderType(loginType))
                .translationRequirements(LanguageTranslationRequirement.WELSH_TO_ENGLISH)
                .uploaderCaseRoles(getUploaderCaseRoles(loginType))
                .build()))
        );
    }


    @ParameterizedTest
    @MethodSource("buildUploadingDocumentArgs")
    void shouldPopulateDocumentListWhenUploadMultipleCaseSummaryWithTranslationRequirements(
        int loginType,
        Confidentiality confidentiality) {
        tplPopulateDocumentListWhenUploadMultipleDocumentWithTranslationRequirements(DocumentType.CASE_SUMMARY,
            suffix -> "caseSummaryList" + suffix, loginType, confidentiality,
            list -> list.contains(element(elementIdOne, CaseSummary.builder()
                .document(expectedDocumentOne)
                .markAsConfidential(YesNo.from(confidentiality == Confidentiality.YES).getValue())
                .uploaderType(getUploaderType(loginType))
                .uploaderCaseRoles(getUploaderCaseRoles(loginType))
                .translationRequirements(LanguageTranslationRequirement.ENGLISH_TO_WELSH)
                .build()))
                && list.contains(element(elementIdTwo, CaseSummary.builder()
                .document(expectedDocumentTwo)
                .markAsConfidential(YesNo.from(confidentiality == Confidentiality.YES).getValue())
                .uploaderType(getUploaderType(loginType))
                .uploaderCaseRoles(getUploaderCaseRoles(loginType))
                .translationRequirements(LanguageTranslationRequirement.WELSH_TO_ENGLISH)
                .build()))
        );
    }

    @ParameterizedTest
    @MethodSource("buildUploadingDocumentArgs")
    void shouldPopulateDocumentListWhenUploadMultipleSkeletonArgumentWithTranslationRequirements(
        int loginType,
        Confidentiality confidentiality) {
        tplPopulateDocumentListWhenUploadMultipleDocumentWithTranslationRequirements(DocumentType.SKELETON_ARGUMENTS,
            suffix -> "skeletonArgumentList" + suffix, loginType, confidentiality,
            list -> list.contains(element(elementIdOne, SkeletonArgument.builder()
                .document(expectedDocumentOne)
                .markAsConfidential(YesNo.from(confidentiality == Confidentiality.YES).getValue())
                .uploaderType(getUploaderType(loginType))
                .uploaderCaseRoles(getUploaderCaseRoles(loginType))
                .translationRequirements(LanguageTranslationRequirement.ENGLISH_TO_WELSH)
                .build()))
                && list.contains(element(elementIdTwo, SkeletonArgument.builder()
                .document(expectedDocumentTwo)
                .markAsConfidential(YesNo.from(confidentiality == Confidentiality.YES).getValue())
                .uploaderType(getUploaderType(loginType))
                .uploaderCaseRoles(getUploaderCaseRoles(loginType))
                .translationRequirements(LanguageTranslationRequirement.WELSH_TO_ENGLISH)
                .build()))
        );
    }

    @ParameterizedTest
    @MethodSource("buildUploadingDocumentArgs")
    void shouldPopulateDocumentListWhenUploadMultipleRespondentStatementWithTranslationRequirements(
        int loginType,
        Confidentiality confidentiality) {
        tplPopulateDocumentListWhenUploadMultipleDocumentWithTranslationRequirements(
            DocumentType.RESPONDENTS_STATEMENTS,
            suffix -> "respStmtList" + suffix, loginType, confidentiality,
            list -> list.contains(element(elementIdOne, RespondentStatementV2.builder()
                .document(expectedDocumentOne)
                .markAsConfidential(YesNo.from(confidentiality == Confidentiality.YES).getValue())
                .uploaderType(getUploaderType(loginType))
                .uploaderCaseRoles(getUploaderCaseRoles(loginType))
                .translationRequirements(LanguageTranslationRequirement.ENGLISH_TO_WELSH)
                .build()))
                && list.contains(element(elementIdTwo, RespondentStatementV2.builder()
                .document(expectedDocumentTwo)
                .markAsConfidential(YesNo.from(confidentiality == Confidentiality.YES).getValue())
                .uploaderType(getUploaderType(loginType))
                .uploaderCaseRoles(getUploaderCaseRoles(loginType))
                .translationRequirements(LanguageTranslationRequirement.WELSH_TO_ENGLISH)
                .build()))
        );
    }

    @ParameterizedTest
    @MethodSource("buildUploadingDocumentArgs")
    @SuppressWarnings("unchecked")
    void shouldPopulateDocumentListWhenUploadMultipleCourtBundleWithTranslationRequirements(
        int loginType,
        Confidentiality confidentiality) {
        tplPopulateDocumentListWhenUploadMultipleDocumentWithTranslationRequirements(DocumentType.COURT_BUNDLE,
            suffix -> "".equals(suffix) ? "courtBundleListV2" : ("courtBundleList" + suffix), loginType,
            confidentiality,
            list -> {
                List<Element> flist = (List<Element>) list.stream()
                    .filter(p -> elementIdOne.equals(((Element) p).getId())
                        || elementIdTwo.equals(((Element) p).getId()))
                    .collect(Collectors.toList());
                if (flist.size() != 2) {
                    return false;
                } else {
                    return flist.stream().allMatch((s) -> {
                        Object wrapped = s.getValue();
                        if (wrapped.getClass().isAssignableFrom(HearingCourtBundle.class)) {
                            final DocumentReference expectedDocument = elementIdOne.equals(s.getId())
                                ? expectedDocumentOne : expectedDocumentTwo;
                            final LanguageTranslationRequirement expectedTranslationRequirements
                                = elementIdOne.equals(s.getId()) ? LanguageTranslationRequirement.ENGLISH_TO_WELSH
                                : LanguageTranslationRequirement.WELSH_TO_ENGLISH;

                            HearingCourtBundle hcb = (HearingCourtBundle) wrapped;
                            boolean test = hcb.getCourtBundle() != null;
                            test = test && hcb.getCourtBundle().size() == 1;
                            test = test && expectedDocument.equals(hcb.getCourtBundle().get(0).getValue()
                                .getDocument());
                            test = test && getUploaderType(loginType).equals(hcb.getCourtBundle().get(0).getValue()
                                .getUploaderType());
                            test = test && getUploaderCaseRoles(loginType).equals(hcb.getCourtBundle().get(0).getValue()
                                .getUploaderCaseRoles());
                            test = test && expectedTranslationRequirements.equals(hcb.getCourtBundle()
                                .get(0).getValue()
                                .getTranslationRequirements());
                            test = test && hcb.getCourtBundleNC() != null;
                            test = test && hcb.getCourtBundleNC().size() == 1;
                            test = test && expectedDocument.equals(hcb.getCourtBundleNC().get(0).getValue()
                                .getDocument());
                            test = test && getUploaderType(loginType).equals(hcb.getCourtBundleNC().get(0).getValue()
                                .getUploaderType());
                            test = test && getUploaderCaseRoles(loginType).equals(hcb.getCourtBundleNC().get(0)
                                .getValue().getUploaderCaseRoles());
                            test = test && expectedTranslationRequirements.equals(hcb.getCourtBundleNC()
                                .get(0).getValue()
                                .getTranslationRequirements());
                            return test;
                        }
                        return false;
                    });
                }
            }
        );
    }

    void tplPopulateDocumentListWhenUploadMultipleDocument(DocumentType documentType,
                                                           Function<String, String> fieldNameProvider,
                                                           int loginType, Confidentiality confidentiality,
                                                           Predicate<List> matcher) {
        initialiseUserService(loginType);
        CaseData caseData = prepareCaseDataForUploadDocumentJourney(
            List.of(
                element(elementIdOne, UploadableDocumentBundle.builder()
                    .documentTypeDynamicList(DynamicList.builder()
                        .value(DynamicListElement.builder()
                            .code(documentType.name())
                            .build())
                        .build())
                    .document(expectedDocumentOne)
                    .confidential(toConfidential(confidentiality))
                    .build()),
                element(elementIdTwo, UploadableDocumentBundle.builder()
                    .documentTypeDynamicList(DynamicList.builder()
                        .value(DynamicListElement.builder()
                            .code(documentType.name())
                            .build())
                        .build())
                    .document(expectedDocumentTwo)
                    .confidential(toConfidential(confidentiality))
                    .build())
            )
        );

        String suffix = getFieldNameSuffix(getUploaderType(loginType), confidentiality);
        assertThat(underTest.uploadDocuments(caseData))
            .containsKey(fieldNameProvider.apply(suffix))
            .extracting(fieldNameProvider.apply(suffix)).asList()
            .matches(matcher);
    }

    @ParameterizedTest
    @MethodSource("buildUploadingDocumentArgs")
    void shouldPopulateDocumentListWhenUploadMultipleParentAssessment(int loginType, Confidentiality confidentiality) {
        tplPopulateDocumentListWhenUploadMultipleDocument(DocumentType.PARENT_ASSESSMENTS,
            suffix -> "parentAssessmentList" + suffix, loginType, confidentiality,
            list -> list.contains(element(elementIdOne, ManagedDocument.builder()
                .document(expectedDocumentOne)
                .markAsConfidential(YesNo.from(confidentiality == Confidentiality.YES).getValue())
                .uploaderType(getUploaderType(loginType))
                .uploaderCaseRoles(getUploaderCaseRoles(loginType))
                .build()))
                && list.contains(element(elementIdTwo, ManagedDocument.builder()
                .document(expectedDocumentTwo)
                .markAsConfidential(YesNo.from(confidentiality == Confidentiality.YES).getValue())
                .uploaderType(getUploaderType(loginType))
                .uploaderCaseRoles(getUploaderCaseRoles(loginType))
                .build()))
        );
    }

    @ParameterizedTest
    @MethodSource("buildUploadingDocumentArgs")
    void shouldPopulateDocumentListWhenUploadMultipleCaseSummary(int loginType, Confidentiality confidentiality) {
        tplPopulateDocumentListWhenUploadMultipleDocument(DocumentType.CASE_SUMMARY,
            suffix -> "caseSummaryList" + suffix, loginType, confidentiality,
            list -> list.contains(element(elementIdOne, CaseSummary.builder()
                .document(expectedDocumentOne)
                .markAsConfidential(YesNo.from(confidentiality == Confidentiality.YES).getValue())
                .uploaderType(getUploaderType(loginType))
                .uploaderCaseRoles(getUploaderCaseRoles(loginType))
                .build()))
                && list.contains(element(elementIdTwo, CaseSummary.builder()
                .document(expectedDocumentTwo)
                .markAsConfidential(YesNo.from(confidentiality == Confidentiality.YES).getValue())
                .uploaderType(getUploaderType(loginType))
                .uploaderCaseRoles(getUploaderCaseRoles(loginType))
                .build()))
        );
    }

    @ParameterizedTest
    @MethodSource("buildUploadingDocumentArgs")
    void shouldPopulateDocumentListWhenUploadMultipleSkeletonArgument(int loginType, Confidentiality confidentiality) {
        tplPopulateDocumentListWhenUploadMultipleDocument(DocumentType.SKELETON_ARGUMENTS,
            suffix -> "skeletonArgumentList" + suffix, loginType, confidentiality,
            list -> list.contains(element(elementIdOne, SkeletonArgument.builder()
                .document(expectedDocumentOne)
                .markAsConfidential(YesNo.from(confidentiality == Confidentiality.YES).getValue())
                .uploaderType(getUploaderType(loginType))
                .uploaderCaseRoles(getUploaderCaseRoles(loginType))
                .build()))
                && list.contains(element(elementIdTwo, SkeletonArgument.builder()
                .document(expectedDocumentTwo)
                .markAsConfidential(YesNo.from(confidentiality == Confidentiality.YES).getValue())
                .uploaderType(getUploaderType(loginType))
                .uploaderCaseRoles(getUploaderCaseRoles(loginType))
                .build()))
        );
    }

    @ParameterizedTest
    @MethodSource("buildUploadingDocumentArgs")
    void shouldPopulateDocumentListWhenUploadMultipleRespondentStatement(int loginType,
                                                                         Confidentiality confidentiality) {
        tplPopulateDocumentListWhenUploadMultipleDocument(DocumentType.RESPONDENTS_STATEMENTS,
            suffix -> "respStmtList" + suffix, loginType, confidentiality,
            list -> list.contains(element(elementIdOne, RespondentStatementV2.builder()
                .document(expectedDocumentOne)
                .markAsConfidential(YesNo.from(confidentiality == Confidentiality.YES).getValue())
                .uploaderType(getUploaderType(loginType))
                .uploaderCaseRoles(getUploaderCaseRoles(loginType))
                .build()))
                && list.contains(element(elementIdTwo, RespondentStatementV2.builder()
                .document(expectedDocumentTwo)
                .markAsConfidential(YesNo.from(confidentiality == Confidentiality.YES).getValue())
                .uploaderType(getUploaderType(loginType))
                .uploaderCaseRoles(getUploaderCaseRoles(loginType))
                .build()))
        );
    }

    @ParameterizedTest
    @MethodSource("buildUploadingDocumentArgs")
    @SuppressWarnings("unchecked")
    void shouldPopulateDocumentListWhenUploadMultipleCourtBundle(int loginType, Confidentiality confidentiality) {
        tplPopulateDocumentListWhenUploadMultipleDocument(DocumentType.COURT_BUNDLE,
            suffix -> "".equals(suffix) ? "courtBundleListV2" : ("courtBundleList" + suffix), loginType,
            confidentiality,
            list -> {
                List<Element> flist = (List<Element>) list.stream()
                    .filter(p -> elementIdOne.equals(((Element) p).getId())
                        || elementIdTwo.equals(((Element) p).getId()))
                    .collect(Collectors.toList());
                if (flist.size() != 2) {
                    return false;
                } else {
                    return flist.stream().allMatch((s) -> {
                        Object wrapped = s.getValue();
                        if (wrapped.getClass().isAssignableFrom(HearingCourtBundle.class)) {
                            DocumentReference expectedDocument = elementIdOne.equals(s.getId()) ? expectedDocumentOne
                                : expectedDocumentTwo;

                            HearingCourtBundle hcb = (HearingCourtBundle) wrapped;
                            boolean test = hcb.getCourtBundle() != null;
                            test = test && hcb.getCourtBundle().size() == 1;
                            test = test && expectedDocument.equals(hcb.getCourtBundle().get(0).getValue()
                                .getDocument());
                            test = test && getUploaderType(loginType).equals(hcb.getCourtBundle().get(0).getValue()
                                .getUploaderType());
                            test = test && hcb.getCourtBundleNC() != null;
                            test = test && hcb.getCourtBundleNC().size() == 1;
                            test = test && expectedDocument.equals(hcb.getCourtBundleNC().get(0).getValue()
                                .getDocument());
                            test = test && getUploaderType(loginType).equals(hcb.getCourtBundleNC().get(0).getValue()
                                .getUploaderType());
                            return test;
                        }
                        return false;
                    });
                }
            }
        );
    }

    private void tplPopulateDocumentListWhenUploadDocumentWithDiffConfidentiality(
        DocumentType documentType,
        Function<String, String> fieldNameProvider,
        int loginType,
        Predicate<List> matcher1,
        Predicate<List> matcher2) {
        initialiseUserService(loginType);

        CaseData caseData = prepareCaseDataForUploadDocumentJourney(
            List.of(
                element(elementIdOne, UploadableDocumentBundle.builder()
                    .documentTypeDynamicList(DynamicList.builder()
                        .value(DynamicListElement.builder()
                            .code(documentType.name())
                            .build())
                        .build())
                    .document(expectedDocumentOne)
                    .confidential("YES")
                    .build()),
                element(elementIdTwo, UploadableDocumentBundle.builder()
                    .documentTypeDynamicList(DynamicList.builder()
                        .value(DynamicListElement.builder()
                            .code(documentType.name())
                            .build())
                        .build())
                    .document(expectedDocumentTwo)
                    .confidential("NO")
                    .build())
            )
        );

        String suffix = getFieldNameSuffix(getUploaderType(loginType), Confidentiality.YES);
        Map<String, Object> actual = underTest.uploadDocuments(caseData);
        assertThat(actual)
            .containsKey(fieldNameProvider.apply(suffix))
            .extracting(fieldNameProvider.apply(suffix)).asList()
            .matches(matcher1);
        assertThat(actual)
            .containsKey(fieldNameProvider.apply(""))
            .extracting(fieldNameProvider.apply("")).asList()
            .matches(matcher2);
    }

    @ParameterizedTest
    @MethodSource("buildUploadingDocumentArgs")
    void shouldPopulateDocumentListWhenUploadMultipleParentAssessmentWithDiffConfidentiality(
        int loginType,
        Confidentiality ignoreMe) {
        tplPopulateDocumentListWhenUploadDocumentWithDiffConfidentiality(DocumentType.PARENT_ASSESSMENTS,
            suffix -> "parentAssessmentList" + suffix, loginType,
            list -> list.contains(element(elementIdOne,
                ManagedDocument.builder()
                    .document(expectedDocumentOne)
                    .markAsConfidential(YES.getValue())
                    .uploaderType(getUploaderType(loginType))
                    .uploaderCaseRoles(getUploaderCaseRoles(loginType))
                    .build())),
            list -> list.contains(element(elementIdTwo,
                ManagedDocument.builder()
                    .document(expectedDocumentTwo)
                    .markAsConfidential(NO.getValue())
                    .uploaderType(getUploaderType(loginType))
                    .uploaderCaseRoles(getUploaderCaseRoles(loginType))
                    .build())));
    }

    @ParameterizedTest
    @MethodSource("buildUploadingDocumentArgs")
    void shouldPopulateDocumentListWhenUploadMultipleCaseSummaryWithDiffConfidentiality(
        int loginType,
        Confidentiality ignoreMe) {
        tplPopulateDocumentListWhenUploadDocumentWithDiffConfidentiality(DocumentType.CASE_SUMMARY,
            suffix -> "caseSummaryList" + suffix, loginType,
            list -> list.contains(element(elementIdOne,
                CaseSummary.builder().document(expectedDocumentOne)
                    .markAsConfidential(YES.getValue())
                    .uploaderType(getUploaderType(loginType))
                    .uploaderCaseRoles(getUploaderCaseRoles(loginType))
                    .build())),
            list -> list.contains(element(elementIdTwo,
                CaseSummary.builder().document(expectedDocumentTwo)
                    .markAsConfidential(NO.getValue())
                    .uploaderType(getUploaderType(loginType))
                    .uploaderCaseRoles(getUploaderCaseRoles(loginType))
                    .build())));
    }

    @ParameterizedTest
    @MethodSource("buildUploadingDocumentArgs")
    void shouldPopulateDocumentListWhenUploadMultipleSkeletonArgumentWithDiffConfidentiality(
        int loginType,
        Confidentiality ignoreMe) {
        tplPopulateDocumentListWhenUploadDocumentWithDiffConfidentiality(DocumentType.SKELETON_ARGUMENTS,
            suffix -> "skeletonArgumentList" + suffix, loginType,
            list -> list.contains(element(elementIdOne,
                SkeletonArgument.builder()
                    .document(expectedDocumentOne)
                    .markAsConfidential(YES.getValue())
                    .uploaderType(getUploaderType(loginType))
                    .uploaderCaseRoles(getUploaderCaseRoles(loginType))
                    .build())),
            list -> list.contains(element(elementIdTwo,
                SkeletonArgument.builder()
                    .document(expectedDocumentTwo)
                    .markAsConfidential(NO.getValue())
                    .uploaderType(getUploaderType(loginType))
                    .uploaderCaseRoles(getUploaderCaseRoles(loginType))
                    .build())));
    }

    @ParameterizedTest
    @MethodSource("buildUploadingDocumentArgs")
    void shouldPopulateDocumentListWhenUploadMultipleRespondentStatementWithDiffConfidentiality(
        int loginType,
        Confidentiality ignoreMe) {
        tplPopulateDocumentListWhenUploadDocumentWithDiffConfidentiality(DocumentType.RESPONDENTS_STATEMENTS,
            suffix -> "respStmtList" + suffix, loginType,
            list -> list.contains(element(elementIdOne,
                RespondentStatementV2.builder()
                    .document(expectedDocumentOne)
                    .markAsConfidential(YES.getValue())
                    .uploaderType(getUploaderType(loginType))
                    .uploaderCaseRoles(getUploaderCaseRoles(loginType))
                    .build())),
            list -> list.contains(element(elementIdTwo,
                RespondentStatementV2.builder()
                    .document(expectedDocumentTwo)
                    .markAsConfidential(NO.getValue())
                    .uploaderType(getUploaderType(loginType))
                    .uploaderCaseRoles(getUploaderCaseRoles(loginType))
                    .build())));
    }

    @ParameterizedTest
    @MethodSource("buildUploadingDocumentArgs")
    void shouldPopulateDocumentListWhenUploadMultipleCourtBundleWithDiffConfidentiality(
        int loginType,
        Confidentiality ignoreMe) {
        tplPopulateDocumentListWhenUploadDocumentWithDiffConfidentiality(DocumentType.COURT_BUNDLE,
            suffix -> "".equals(suffix) ? "courtBundleListV2" : ("courtBundleList" + suffix), loginType,
            list -> {
                Optional<Element> op = list.stream().filter(p -> elementIdOne.equals(((Element) p).getId()))
                    .findAny();
                if (!op.isPresent()) {
                    return false;
                } else {
                    Object wrapped = op.get().getValue();
                    if (wrapped.getClass().isAssignableFrom(HearingCourtBundle.class)) {
                        HearingCourtBundle hcb = (HearingCourtBundle) wrapped;
                        boolean test = hcb.getCourtBundle() != null;
                        test = test && hcb.getCourtBundle().size() == 1;
                        test = test && expectedDocumentOne.equals(hcb.getCourtBundle().get(0).getValue().getDocument());
                        test = test && getUploaderType(loginType).equals(hcb.getCourtBundle().get(0).getValue()
                            .getUploaderType());
                        test = test && hcb.getCourtBundleNC() != null;
                        test = test && hcb.getCourtBundleNC().size() == 1;
                        test = test && expectedDocumentOne.equals(hcb.getCourtBundleNC().get(0).getValue()
                            .getDocument());
                        test = test && getUploaderType(loginType).equals(hcb.getCourtBundleNC().get(0).getValue()
                            .getUploaderType());
                        return test;
                    } else {
                        return false;
                    }
                }
            },
            list -> {
                Optional<Element> op = list.stream().filter(p -> elementIdTwo.equals(((Element) p).getId()))
                    .findAny();
                if (!op.isPresent()) {
                    return false;
                } else {
                    Object wrapped = op.get().getValue();
                    if (wrapped.getClass().isAssignableFrom(HearingCourtBundle.class)) {
                        HearingCourtBundle hcb = (HearingCourtBundle) wrapped;
                        boolean test = hcb.getCourtBundle() != null;
                        test = test && hcb.getCourtBundle().size() == 1;
                        test = test && expectedDocumentTwo.equals(hcb.getCourtBundle().get(0).getValue().getDocument());
                        test = test && getUploaderType(loginType).equals(hcb.getCourtBundle().get(0).getValue()
                            .getUploaderType());
                        test = test && hcb.getCourtBundleNC() != null;
                        test = test && hcb.getCourtBundleNC().size() == 1;
                        test = test && expectedDocumentTwo.equals(hcb.getCourtBundleNC().get(0).getValue()
                            .getDocument());
                        test = test && getUploaderType(loginType).equals(hcb.getCourtBundleNC().get(0).getValue()
                            .getUploaderType());
                        return test;
                    } else {
                        return false;
                    }
                }
            });
    }

    @ParameterizedTest
    @MethodSource("buildUploadingDocumentArgs")
    void shouldPopulatePlacementsWhenUploadingSinglePlacementResponse(int loginType, Confidentiality ignoreMe) {
        initialiseUserService(loginType);

        CaseData caseData = prepareCaseDataForUploadDocumentJourney(
            List.of(
                element(elementIdOne, UploadableDocumentBundle.builder()
                    .documentTypeDynamicList(DynamicList.builder()
                        .value(DynamicListElement.builder()
                            .code(DocumentType.PLACEMENT_RESPONSES.name())
                            .build())
                        .build())
                    .document(expectedDocumentOne)
                    .build())
            )
        );

        UUID placementLAId = UUID.randomUUID();
        UUID placementRespondentId = UUID.randomUUID();
        UUID placementAdminId = UUID.randomUUID();

        Placement placement = Placement.builder().noticeDocuments(List.of()).build();
        Placement placementLA = Placement.builder().noticeDocuments(List.of(
            element(elementIdOne, PlacementNoticeDocument.builder()
                .uploaderType(getUploaderType(loginType))
                .uploaderCaseRoles(getUploaderCaseRoles(loginType))
                .response(expectedDocumentOne)
                .build())
        )).build();
        Placement placementRespondent = Placement.builder().noticeDocuments(List.of()).build();
        Placement placementAdmin = Placement.builder().noticeDocuments(List.of()).build();

        when(placementService.preparePlacementFromExisting(caseData)).thenReturn(
            PlacementEventData.builder()
                .placement(placement)
                .placements(List.of(element(placement)))
                .build()
        );
        when(placementService.savePlacementNoticeResponses(any(), eq(PlacementNoticeDocument.RecipientType
            .LOCAL_AUTHORITY))).thenReturn(PlacementEventData.builder()
            .placement(placementLA)
            .placements(List.of(element(placementLAId, placementLA)))
            .build()
        );
        when(placementService.savePlacementNoticeResponses(any(), eq(PlacementNoticeDocument.RecipientType
            .RESPONDENT))).thenReturn(PlacementEventData.builder()
            .placement(placementRespondent)
            .placements(List.of(element(placementRespondentId, placementRespondent)))
            .build()
        );
        when(placementService.savePlacementNoticeResponsesAdmin(any())).thenReturn(
            PlacementEventData.builder()
                .placement(placementAdmin)
                .placements(List.of(element(placementAdminId, placementAdmin)))
                .build()
        );

        Predicate<List> matcher = (list) -> {
            boolean test = true;
            switch (getUploaderType(loginType)) {
                case DESIGNATED_LOCAL_AUTHORITY:
                case SECONDARY_LOCAL_AUTHORITY:
                    test = test && list.contains(element(placementLAId, placementLA));
                    return test;
                case HMCTS:
                    test = test && list.contains(element(placementAdminId, placementAdmin));
                    return test;
                case SOLICITOR:
                    test = test && list.contains(element(placementRespondentId, placementRespondent));
                    return test;
                default:
                    break;
            }
            return false;
        };

        Map<String, Object> actual = underTest.uploadDocuments(caseData);
        if (getUploaderType(loginType) == DocumentUploaderType.DESIGNATED_LOCAL_AUTHORITY
            || getUploaderType(loginType) == DocumentUploaderType.SECONDARY_LOCAL_AUTHORITY) {
            assertThat(actual)
                .containsKey("placements")
                .extracting("placements").asList()
                .matches(matcher);
        } else {
            assertThat(actual)
                .containsKey("placements")
                .containsKey("placementsNonConfidential")
                .containsKey("placementsNonConfidentialNotices")
                .extracting("placements").asList()
                .matches(matcher);
        }
    }

    @ParameterizedTest
    @MethodSource("buildUploadingDocumentArgs")
    void shouldPopulateDocumentListWhenUploadMultiplePlacementResponses(
        int loginType, Confidentiality ignoreMe) {
        initialiseUserService(loginType);

        CaseData caseData = prepareCaseDataForUploadDocumentJourney(
            List.of(
                element(elementIdOne, UploadableDocumentBundle.builder()
                    .documentTypeDynamicList(DynamicList.builder()
                        .value(DynamicListElement.builder()
                            .code(DocumentType.PLACEMENT_RESPONSES.name())
                            .build())
                        .build())
                    .document(expectedDocumentOne)
                    .build()),
                element(elementIdTwo, UploadableDocumentBundle.builder()
                    .documentTypeDynamicList(DynamicList.builder()
                        .value(DynamicListElement.builder()
                            .code(DocumentType.PLACEMENT_RESPONSES.name())
                            .build())
                        .build())
                    .document(expectedDocumentTwo)
                    .build())

            )
        );

        UUID placementLAId = UUID.randomUUID();
        UUID placementRespondentId = UUID.randomUUID();
        UUID placementAdminId = UUID.randomUUID();

        Placement placement = Placement.builder().noticeDocuments(List.of()).build();
        Placement placementLA = Placement.builder().noticeDocuments(List.of(
            element(elementIdOne, PlacementNoticeDocument.builder()
                .uploaderType(getUploaderType(loginType))
                .uploaderCaseRoles(getUploaderCaseRoles(loginType))
                .response(expectedDocumentOne)
                .build()),
            element(elementIdTwo, PlacementNoticeDocument.builder()
                .uploaderType(getUploaderType(loginType))
                .uploaderCaseRoles(getUploaderCaseRoles(loginType))
                .response(expectedDocumentTwo)
                .build())
        )).build();
        Placement placementRespondent = Placement.builder().noticeDocuments(List.of()).build();
        Placement placementAdmin = Placement.builder().noticeDocuments(List.of()).build();

        when(placementService.preparePlacementFromExisting(caseData)).thenReturn(
            PlacementEventData.builder()
                .placement(placement)
                .placements(List.of(element(placement)))
                .build()
        );
        when(placementService.savePlacementNoticeResponses(any(), eq(PlacementNoticeDocument.RecipientType
            .LOCAL_AUTHORITY))).thenReturn(PlacementEventData.builder()
            .placement(placementLA)
            .placements(List.of(element(placementLAId, placementLA)))
            .build()
        );
        when(placementService.savePlacementNoticeResponses(any(), eq(PlacementNoticeDocument.RecipientType
            .RESPONDENT))).thenReturn(PlacementEventData.builder()
            .placement(placementRespondent)
            .placements(List.of(element(placementRespondentId, placementRespondent)))
            .build()
        );
        when(placementService.savePlacementNoticeResponsesAdmin(any())).thenReturn(
            PlacementEventData.builder()
                .placement(placementAdmin)
                .placements(List.of(element(placementAdminId, placementAdmin)))
                .build()
        );

        Predicate<List> matcher = (list) -> {
            boolean test = true;
            switch (getUploaderType(loginType)) {
                case DESIGNATED_LOCAL_AUTHORITY:
                case SECONDARY_LOCAL_AUTHORITY:
                    test = test && list.contains(element(placementLAId, placementLA));
                    return test;
                case HMCTS:
                    test = test && list.contains(element(placementAdminId, placementAdmin));
                    return test;
                case SOLICITOR:
                    test = test && list.contains(element(placementRespondentId, placementRespondent));
                    return test;
                default:
                    break;
            }
            return false;
        };

        Map<String, Object> actual = underTest.uploadDocuments(caseData);
        if (getUploaderType(loginType) == DocumentUploaderType.DESIGNATED_LOCAL_AUTHORITY
            || getUploaderType(loginType) == DocumentUploaderType.SECONDARY_LOCAL_AUTHORITY) {
            assertThat(actual)
                .containsKey("placements")
                .extracting("placements").asList()
                .matches(matcher);
        } else {
            assertThat(actual)
                .containsKey("placements")
                .containsKey("placementsNonConfidential")
                .containsKey("placementsNonConfidentialNotices")
                .extracting("placements").asList()
                .matches(matcher);
        }
    }

    @Nested
    class BuildManageDocumentsUploadedEventTest {
        @ParameterizedTest
        @MethodSource("allDocumentsTypeParameters")
        void shouldBuildManageDocumentsUploadedEvent(DocumentType documentType, ConfidentialLevel confidentialLevel)
            throws Exception {

            CaseData caseDataBefore = ManageDocumentsUploadedEventTestData.commonCaseBuilder().build();

            CaseData caseData = buildSubmittedCaseDataWithNewDocumentUploaded(List.of(documentType),
                    List.of(confidentialLevel));

            List<Element<Object>> documentList = ObjectHelper.getFieldValue(caseData,
                documentType.getBaseFieldNameResolver().apply(confidentialLevel), List.class);

            ManageDocumentsUploadedEvent eventData =
                underTest.buildManageDocumentsUploadedEvent(caseData, caseDataBefore);

            assertEquals(caseData, eventData.getCaseData());

            Map<DocumentType, List<Element<NotifyDocumentUploaded>>> expectedNewDocuments = new HashMap<>();
            Map<DocumentType, List<Element<NotifyDocumentUploaded>>> expectedNewDocumentsLA = new HashMap<>();
            Map<DocumentType, List<Element<NotifyDocumentUploaded>>> expectedNewDocumentsCTSC = new HashMap<>();

            List<Element<NotifyDocumentUploaded>> expectedDocuments;
            if (DocumentType.COURT_BUNDLE.equals(documentType)) {
                expectedDocuments = documentList.stream()
                    .map(Element::getValue).map(doc -> (HearingCourtBundle) doc)
                    .map(HearingCourtBundle::getCourtBundle)
                    .flatMap(List::stream)
                    .map(elm -> element(elm.getId(), (NotifyDocumentUploaded) elm.getValue()))
                    .collect(Collectors.toList());
            } else {
                expectedDocuments = documentList.stream()
                    .map(elm -> element(elm.getId(), (NotifyDocumentUploaded) elm.getValue()))
                    .collect(Collectors.toList());
            }

            if (ConfidentialLevel.NON_CONFIDENTIAL.equals(confidentialLevel)) {
                expectedNewDocuments.put(documentType, expectedDocuments);
            } else if (ConfidentialLevel.LA.equals(confidentialLevel)) {
                expectedNewDocumentsLA.put(documentType, expectedDocuments);
            } else if (ConfidentialLevel.CTSC.equals(confidentialLevel)) {
                expectedNewDocumentsCTSC.put(documentType, expectedDocuments);
            }

            assertEquals(expectedNewDocuments, eventData.getNewDocuments());
            assertEquals(expectedNewDocumentsLA, eventData.getNewDocumentsLA());
            assertEquals(expectedNewDocumentsCTSC, eventData.getNewDocumentsCTSC());
        }

        private static Stream<Arguments> allDocumentsTypeParameters() {
            List<Arguments> streamList = new ArrayList<>();

            for (DocumentType docType : DocumentType.values()) {
                if(isNotEmpty(docType.getBaseFieldNameResolver())) {
                    for (ConfidentialLevel level : ConfidentialLevel.values()) {
                        streamList.add(Arguments.of(docType, level));
                    }
                }
            }

            return streamList.stream();
        }

    }
}
