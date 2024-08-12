package uk.gov.hmcts.reform.fpl.service;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import uk.gov.hmcts.reform.fpl.enums.CaseRole;
import uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement;
import uk.gov.hmcts.reform.fpl.enums.ManageDocumentAction;
import uk.gov.hmcts.reform.fpl.enums.ManageDocumentRemovalReason;
import uk.gov.hmcts.reform.fpl.enums.UserRole;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.enums.cfv.ConfidentialLevel;
import uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType;
import uk.gov.hmcts.reform.fpl.enums.notification.DocumentUploaderType;
import uk.gov.hmcts.reform.fpl.events.ManageDocumentsUploadedEvent;
import uk.gov.hmcts.reform.fpl.handlers.ManageDocumentsUploadedEventTestData;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CaseSummary;
import uk.gov.hmcts.reform.fpl.model.CourtBundle;
import uk.gov.hmcts.reform.fpl.model.HearingCourtBundle;
import uk.gov.hmcts.reform.fpl.model.HearingDocuments;
import uk.gov.hmcts.reform.fpl.model.ManagedDocument;
import uk.gov.hmcts.reform.fpl.model.Placement;
import uk.gov.hmcts.reform.fpl.model.PlacementConfidentialDocument;
import uk.gov.hmcts.reform.fpl.model.PlacementNoticeDocument;
import uk.gov.hmcts.reform.fpl.model.RespondentStatementV2;
import uk.gov.hmcts.reform.fpl.model.SkeletonArgument;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.OtherApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.SubmittedC1WithSupplementBundle;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.model.event.ManageDocumentEventData;
import uk.gov.hmcts.reform.fpl.model.event.PlacementEventData;
import uk.gov.hmcts.reform.fpl.model.event.UploadableDocumentBundle;
import uk.gov.hmcts.reform.fpl.model.interfaces.NotifyDocumentUploaded;
import uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService;
import uk.gov.hmcts.reform.fpl.utils.DocumentUploadHelper;
import uk.gov.hmcts.reform.fpl.utils.ObjectHelper;
import uk.gov.hmcts.reform.fpl.utils.TestDataHelper;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.String.format;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.mockito.quality.Strictness.LENIENT;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType.AA_PARENT_APPLICANTS_DOCUMENTS;
import static uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType.AA_PARENT_APPLICATIONS;
import static uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType.AA_PARENT_EXPERT_REPORTS;
import static uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType.AA_PARENT_ORDERS;
import static uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType.AA_PARENT_RESPONDENTS_STATEMENTS;
import static uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType.APPLICANTS_OTHER_DOCUMENTS;
import static uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType.APPLICANTS_WITNESS_STATEMENTS;
import static uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType.C1_APPLICATION_DOCUMENTS;
import static uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType.C2_APPLICATION_DOCUMENTS;
import static uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType.CARE_PLAN;
import static uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType.CASE_SUMMARY;
import static uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType.CONTACT_NOTES;
import static uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType.COURT_BUNDLE;
import static uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType.COURT_CORRESPONDENCE;
import static uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType.DOCUMENTS_FILED_ON_ISSUE;
import static uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType.DRUG_AND_ALCOHOL_REPORTS;
import static uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType.EXPERT_REPORTS;
import static uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType.FAMILY_AND_VIABILITY_ASSESSMENTS;
import static uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType.GUARDIAN_EVIDENCE;
import static uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType.JUDGEMENTS;
import static uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType.LETTER_OF_INSTRUCTION;
import static uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType.MEDICAL_RECORDS;
import static uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType.MEETING_NOTES;
import static uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType.NOTICE_OF_ACTING_OR_ISSUE;
import static uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType.PARENT_ASSESSMENTS;
import static uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType.PLACEMENT_RESPONSES;
import static uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType.POLICE_DISCLOSURE;
import static uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType.POSITION_STATEMENTS;
import static uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType.PREVIOUS_PROCEEDING;
import static uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType.RESPONDENTS_STATEMENTS;
import static uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType.RESPONDENTS_WITNESS_STATEMENTS;
import static uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType.SKELETON_ARGUMENTS;
import static uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType.THRESHOLD;
import static uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType.TRANSCRIPTS;
import static uk.gov.hmcts.reform.fpl.handlers.ManageDocumentsUploadedEventTestData.buildSubmittedCaseDataWithNewDocumentUploaded;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

@SuppressWarnings("unchecked")
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = LENIENT)
class ManageDocumentServiceTest {

    enum Confidentiality {
        YES, NO, NULL
    }

    private static final int LA_LOGIN_TYPE = 1;
    private static final int EXT_SOL_LOGIN_TYPE = 3;
    private static final int HMCTS_LOGIN_TYPE = 4;
    private static final int LEGACY_LOGIN_TYPE = 999;

    @Mock
    private DocumentUploadHelper documentUploadHelper;

    @Mock
    private PlacementService placementService;

    @Mock
    private UserService userService;

    @Mock
    private CaseConverter caseConverter;

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
    void shouldPopulatePropertiesForSolicitorUsers(CaseRole caseRole) {
        when(userService.getCaseRoles(CASE_ID)).thenReturn(Set.of(caseRole));
        when(userService.isHmctsUser()).thenReturn(false);

        CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .build();

        assertThat(underTest.allowMarkDocumentConfidential(caseData)).isEqualTo(false);
        assertThat(underTest.allowSelectDocumentTypeToRemoveDocument(caseData)).isEqualTo(false);
    }

    @Test
    void shouldPopulatePropertiesForCafcassUser() {
        when(userService.getIdamRoles()).thenReturn(Set.of(UserRole.CAFCASS.getRoleName()));
        when(userService.isHmctsUser()).thenReturn(false);

        CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .build();

        assertThat(underTest.allowMarkDocumentConfidential(caseData)).isEqualTo(false);
        assertThat(underTest.allowSelectDocumentTypeToRemoveDocument(caseData)).isEqualTo(false);
    }

    @ParameterizedTest
    @EnumSource(value = CaseRole.class, names = {"LASHARED", "LASOLICITOR", "EPSMANAGING", "LAMANAGING", "LABARRISTER"})
    void shouldPopulatePropertiesForLAUsers(CaseRole caseRole) {
        when(userService.getCaseRoles(CASE_ID)).thenReturn(Set.of(caseRole));
        when(userService.isHmctsUser()).thenReturn(false);

        CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .build();

        assertThat(underTest.allowMarkDocumentConfidential(caseData)).isEqualTo(true);
        assertThat(underTest.allowSelectDocumentTypeToRemoveDocument(caseData)).isEqualTo(false);
    }

    @Test
    void shouldPopulatePropertiesForHmctsUser() {
        when(userService.getCaseRoles(CASE_ID)).thenReturn(Set.of());
        when(userService.isHmctsUser()).thenReturn(true);

        CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .build();

        assertThat(underTest.allowMarkDocumentConfidential(caseData)).isEqualTo(true);
        assertThat(underTest.allowSelectDocumentTypeToRemoveDocument(caseData)).isEqualTo(true);
    }

    private static Pair<String, String> toPair(DocumentType documentType) {
        return Pair.of(documentType.name(), documentType.getDescription());
    }

    private static Stream<Arguments> buildDocumentTypeDynamicListArgs() {
        List<Arguments> args = new ArrayList<>();
        for (int i = 1; i < 7; i++) {
            for (int b = 0; b < 2; b++) {
                List<Pair<String, String>> expected = List.of(
                    toPair(COURT_BUNDLE),
                    toPair(CASE_SUMMARY),
                    toPair(POSITION_STATEMENTS),
                    toPair(THRESHOLD),
                    toPair(SKELETON_ARGUMENTS),
                    toPair(AA_PARENT_ORDERS),
                    toPair(JUDGEMENTS),
                    toPair(TRANSCRIPTS),
                    toPair(AA_PARENT_APPLICANTS_DOCUMENTS),
                    toPair(DOCUMENTS_FILED_ON_ISSUE),
                    toPair(APPLICANTS_WITNESS_STATEMENTS),
                    toPair(CARE_PLAN),
                    toPair(PARENT_ASSESSMENTS),
                    toPair(FAMILY_AND_VIABILITY_ASSESSMENTS),
                    toPair(APPLICANTS_OTHER_DOCUMENTS),
                    toPair(MEETING_NOTES),
                    toPair(CONTACT_NOTES),
                    toPair(AA_PARENT_APPLICATIONS),
                    toPair(C1_APPLICATION_DOCUMENTS),
                    toPair(C2_APPLICATION_DOCUMENTS),
                    toPair(AA_PARENT_RESPONDENTS_STATEMENTS),
                    toPair(RESPONDENTS_STATEMENTS),
                    toPair(RESPONDENTS_WITNESS_STATEMENTS),
                    toPair(GUARDIAN_EVIDENCE),
                    toPair(AA_PARENT_EXPERT_REPORTS),
                    toPair(EXPERT_REPORTS),
                    toPair(DRUG_AND_ALCOHOL_REPORTS),
                    toPair(LETTER_OF_INSTRUCTION),
                    toPair(POLICE_DISCLOSURE),
                    toPair(MEDICAL_RECORDS),
                    toPair(COURT_CORRESPONDENCE),
                    toPair(NOTICE_OF_ACTING_OR_ISSUE),
                    toPair(PREVIOUS_PROCEEDING),
                    b == 0 ? toPair(PLACEMENT_RESPONSES) : Pair.of("", ""));
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

        DynamicList expectedDynamicList = DynamicList.builder().build();
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
        for (int i = 1; i < 6; i++) {
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
        initialiseUserService(loginType, false);
    }

    private void initialiseUserService(int loginType, boolean isChildSolicitor) {
        when(userService.getCaseRoles(CASE_ID)).thenReturn(new HashSet<>(getUploaderCaseRoles(loginType,
            isChildSolicitor)));
        when(userService.isHmctsUser()).thenReturn(4 == loginType); // HMCTS for loginType = 4
        switch (loginType) {
            case 4:
                when(userService.getIdamRoles()).thenReturn(Set.of(UserRole.HMCTS_ADMIN.getRoleName()));
                break;
            case 5:
                when(userService.getIdamRoles()).thenReturn(Set.of(UserRole.CAFCASS.getRoleName()));
                break;
            default:
                break;
        }
    }

    private static DocumentUploaderType getUploaderType(int loginType) {
        switch (loginType) {
            case LA_LOGIN_TYPE:
                return DocumentUploaderType.DESIGNATED_LOCAL_AUTHORITY;
            case 2:
                return DocumentUploaderType.SECONDARY_LOCAL_AUTHORITY;
            case EXT_SOL_LOGIN_TYPE:
                return DocumentUploaderType.SOLICITOR;
            case HMCTS_LOGIN_TYPE:
                return DocumentUploaderType.HMCTS;
            case 5:
                return DocumentUploaderType.CAFCASS;
            case LEGACY_LOGIN_TYPE:
                return null;
            default:
                throw new IllegalStateException("unrecognised loginType: " + loginType);
        }
    }

    private static List<CaseRole> getUploaderCaseRoles(int loginType) {
        return getUploaderCaseRoles(loginType, false);
    }

    private static List<CaseRole> getUploaderCaseRoles(int loginType, boolean isChildSolicitor) {
        switch (loginType) {
            case LA_LOGIN_TYPE:
                return List.of(CaseRole.LASOLICITOR);
            case 2:
                return List.of(CaseRole.LASHARED);
            case EXT_SOL_LOGIN_TYPE:
                return isChildSolicitor ? List.of(CaseRole.CHILDSOLICITORA) : List.of(CaseRole.SOLICITORA);
            case HMCTS_LOGIN_TYPE:
            case 5:
                return List.of();
            case 6:
                return List.of(CaseRole.BARRISTER);
            case LEGACY_LOGIN_TYPE:
                return null;
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
        tplPopulateDocumentListWhenUploadingSingleDocument(PARENT_ASSESSMENTS,
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
        tplPopulateDocumentListWhenUploadingSingleDocument(CASE_SUMMARY,
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
        tplPopulateDocumentListWhenUploadingSingleDocument(SKELETON_ARGUMENTS,
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
        tplPopulateDocumentListWhenUploadingSingleDocument(RESPONDENTS_STATEMENTS,
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
        tplPopulateDocumentListWhenUploadingSingleDocument(COURT_BUNDLE,
            suffix -> "".equals(suffix) ? "courtBundleListV2" : ("courtBundleList" + suffix),
            loginType, confidentiality,
            list -> {
                List<Element> flist = (List<Element>) list.stream()
                    .filter(p -> elementIdOne.equals(((Element) p).getId()))
                    .toList();
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
        tplPopulateDocumentListWhenUploadMultipleDocumentWithTranslationRequirements(PARENT_ASSESSMENTS,
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
        tplPopulateDocumentListWhenUploadMultipleDocumentWithTranslationRequirements(CASE_SUMMARY,
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
        tplPopulateDocumentListWhenUploadMultipleDocumentWithTranslationRequirements(SKELETON_ARGUMENTS,
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
        tplPopulateDocumentListWhenUploadMultipleDocumentWithTranslationRequirements(COURT_BUNDLE,
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
        tplPopulateDocumentListWhenUploadMultipleDocument(PARENT_ASSESSMENTS,
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
        tplPopulateDocumentListWhenUploadMultipleDocument(CASE_SUMMARY,
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
        tplPopulateDocumentListWhenUploadMultipleDocument(SKELETON_ARGUMENTS,
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
        tplPopulateDocumentListWhenUploadMultipleDocument(RESPONDENTS_STATEMENTS,
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
        tplPopulateDocumentListWhenUploadMultipleDocument(COURT_BUNDLE,
            suffix -> "".equals(suffix) ? "courtBundleListV2" : ("courtBundleList" + suffix), loginType,
            confidentiality,
            list -> {
                List<Element> flist = (List<Element>) list.stream()
                    .filter(p -> elementIdOne.equals(((Element) p).getId())
                        || elementIdTwo.equals(((Element) p).getId()))
                    .toList();
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
        tplPopulateDocumentListWhenUploadDocumentWithDiffConfidentiality(PARENT_ASSESSMENTS,
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
        tplPopulateDocumentListWhenUploadDocumentWithDiffConfidentiality(CASE_SUMMARY,
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
        tplPopulateDocumentListWhenUploadDocumentWithDiffConfidentiality(SKELETON_ARGUMENTS,
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
        tplPopulateDocumentListWhenUploadDocumentWithDiffConfidentiality(RESPONDENTS_STATEMENTS,
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
        tplPopulateDocumentListWhenUploadDocumentWithDiffConfidentiality(COURT_BUNDLE,
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
                            .code(PLACEMENT_RESPONSES.name())
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
                case CAFCASS:
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
                            .code(PLACEMENT_RESPONSES.name())
                            .build())
                        .build())
                    .document(expectedDocumentOne)
                    .build()),
                element(elementIdTwo, UploadableDocumentBundle.builder()
                    .documentTypeDynamicList(DynamicList.builder()
                        .value(DynamicListElement.builder()
                            .code(PLACEMENT_RESPONSES.name())
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
                case CAFCASS:
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
    class BuildDocumentTypeDynamicListForRemovalTest {

        DynamicList expectedDynamicList1 = DynamicList.builder().build();

        @Test
        void shouldShowAnEmptyDynamicList() {
            when(caseConverter.toMap(any())).thenReturn(Map.of());
            when(dynamicListService.asDynamicList(List.of())).thenReturn(expectedDynamicList1);

            DynamicList dynamicList = underTest.buildDocumentTypeDynamicListForRemoval(CaseData.builder().build());
            assertThat(dynamicList).isEqualTo(expectedDynamicList1);
        }

        @Test
        void shouldShowASingleDocumentType() {
            initialiseUserService(HMCTS_LOGIN_TYPE);
            DocumentUploaderType uploaderType = getUploaderType(HMCTS_LOGIN_TYPE);

            when(caseConverter.toMap(any())).thenReturn(Map.of("courtBundleListV2", List.of(
                element(HearingCourtBundle.builder()
                    .courtBundle(List.of(
                        element(CourtBundle.builder()
                            .document(testDocumentReference())
                            .uploaderType(uploaderType)
                            .uploaderCaseRoles(getUploaderCaseRoles(HMCTS_LOGIN_TYPE))
                            .build()),
                        element(CourtBundle.builder()
                            .document(testDocumentReference())
                            .uploaderType(uploaderType)
                            .uploaderCaseRoles(getUploaderCaseRoles(HMCTS_LOGIN_TYPE))
                            .build())
                    ))
                    .build()))));
            when(dynamicListService.asDynamicList(List.of(
                Pair.of(COURT_BUNDLE.name(), COURT_BUNDLE.getDescription())
            ))).thenReturn(expectedDynamicList1);

            DynamicList dynamicList = underTest.buildDocumentTypeDynamicListForRemoval(CaseData.builder().build());
            assertThat(dynamicList).isEqualTo(expectedDynamicList1);
        }

        @Test
        void shouldShowMultipleDocumentTypes() {
            initialiseUserService(HMCTS_LOGIN_TYPE);
            DocumentUploaderType uploaderType = getUploaderType(HMCTS_LOGIN_TYPE);

            when(caseConverter.toMap(any())).thenReturn(Map.of(
                "transcriptListCTSC", List.of(element(ManagedDocument.builder().build())),
                "courtBundleListV2", List.of(
                    element(HearingCourtBundle.builder()
                        .courtBundle(List.of(
                            element(CourtBundle.builder()
                                .document(testDocumentReference())
                                .uploaderType(uploaderType)
                                .uploaderCaseRoles(getUploaderCaseRoles(HMCTS_LOGIN_TYPE))
                                .build()),
                            element(CourtBundle.builder()
                                .document(testDocumentReference())
                                .uploaderType(uploaderType)
                                .uploaderCaseRoles(getUploaderCaseRoles(HMCTS_LOGIN_TYPE))
                                .build())
                        ))
                        .build()))));
            when(dynamicListService.asDynamicList(List.of(
                Pair.of(COURT_BUNDLE.name(), COURT_BUNDLE.getDescription()),
                Pair.of(AA_PARENT_ORDERS.name(), AA_PARENT_ORDERS.getDescription()),
                Pair.of(TRANSCRIPTS.name(), TRANSCRIPTS.getDescription())
            ))).thenReturn(expectedDynamicList1);

            DynamicList dynamicList = underTest.buildDocumentTypeDynamicListForRemoval(CaseData.builder().build());
            assertThat(dynamicList).isEqualTo(expectedDynamicList1);
        }

        @Test
        void shouldShowPlacementResponseInDocumentTypes() {
            initialiseUserService(HMCTS_LOGIN_TYPE);
            DocumentUploaderType uploaderType = getUploaderType(HMCTS_LOGIN_TYPE);

            when(caseConverter.toMap(any())).thenReturn(Map.of(
                "transcriptListCTSC", List.of(element(ManagedDocument.builder().build())),
                "courtBundleListV2", List.of(
                    element(HearingCourtBundle.builder()
                        .courtBundle(List.of(
                            element(CourtBundle.builder()
                                .document(testDocumentReference())
                                .uploaderType(uploaderType)
                                .uploaderCaseRoles(getUploaderCaseRoles(HMCTS_LOGIN_TYPE))
                                .build()),
                            element(CourtBundle.builder()
                                .document(testDocumentReference())
                                .uploaderType(uploaderType)
                                .uploaderCaseRoles(getUploaderCaseRoles(HMCTS_LOGIN_TYPE))
                                .build())
                        ))
                        .build()))));
            when(dynamicListService.asDynamicList(List.of(
                Pair.of(COURT_BUNDLE.name(), COURT_BUNDLE.getDescription()),
                Pair.of(AA_PARENT_ORDERS.name(), AA_PARENT_ORDERS.getDescription()),
                Pair.of(TRANSCRIPTS.name(), TRANSCRIPTS.getDescription()),
                Pair.of(PLACEMENT_RESPONSES.name(), PLACEMENT_RESPONSES.getDescription())
            ))).thenReturn(expectedDynamicList1);

            DynamicList dynamicList = underTest.buildDocumentTypeDynamicListForRemoval(CaseData.builder()
                .placementEventData(PlacementEventData.builder()
                    .placements(List.of(element(Placement.builder()
                        .noticeDocuments(List.of(element(PlacementNoticeDocument.builder().build())))
                        .build())))
                    .build())
                .build());
            assertThat(dynamicList).isEqualTo(expectedDynamicList1);
        }

        @Test
        void shouldShowC1SupportingDocumentsInDocumentTypes() {
            initialiseUserService(HMCTS_LOGIN_TYPE);
            DocumentUploaderType uploaderType = getUploaderType(HMCTS_LOGIN_TYPE);

            when(caseConverter.toMap(any())).thenReturn(Map.of());
            when(dynamicListService.asDynamicList(List.of(
                Pair.of(AA_PARENT_APPLICATIONS.name(), AA_PARENT_APPLICATIONS.getDescription()),
                Pair.of(C1_APPLICATION_DOCUMENTS.name(), C1_APPLICATION_DOCUMENTS.getDescription())
            ))).thenReturn(expectedDynamicList1);

            DynamicList dynamicList = underTest.buildDocumentTypeDynamicListForRemoval(CaseData.builder()
                .additionalApplicationsBundle(List.of(element(AdditionalApplicationsBundle.builder()
                    .otherApplicationsBundle(OtherApplicationsBundle.builder()
                        .supportingEvidenceBundle(List.of(element(SupportingEvidenceBundle.builder()
                            .document(testDocumentReference())
                            .uploaderType(uploaderType)
                            .uploaderCaseRoles(getUploaderCaseRoles(HMCTS_LOGIN_TYPE))
                            .build())))
                        .build())
                    .build())))
                .build());
            assertThat(dynamicList).isEqualTo(expectedDynamicList1);
        }

        @Test
        void shouldShowC1SupportingDocumentsInDocumentTypesForSubmittedC1WithSupplementByLA() {
            initialiseUserService(HMCTS_LOGIN_TYPE);
            DocumentUploaderType uploaderType = getUploaderType(LA_LOGIN_TYPE);

            when(caseConverter.toMap(any())).thenReturn(Map.of());
            when(dynamicListService.asDynamicList(List.of(
                Pair.of(AA_PARENT_APPLICATIONS.name(), AA_PARENT_APPLICATIONS.getDescription()),
                Pair.of(C1_APPLICATION_DOCUMENTS.name(), C1_APPLICATION_DOCUMENTS.getDescription())
            ))).thenReturn(expectedDynamicList1);

            DynamicList dynamicList = underTest.buildDocumentTypeDynamicListForRemoval(CaseData.builder()
                .submittedC1WithSupplement(SubmittedC1WithSupplementBundle.builder()
                    .supportingEvidenceBundle(List.of(element(SupportingEvidenceBundle.builder()
                        .document(testDocumentReference())
                        .uploaderType(uploaderType)
                        .uploaderCaseRoles(getUploaderCaseRoles(LA_LOGIN_TYPE))
                        .build())))
                    .build())
                .build());
            assertThat(dynamicList).isEqualTo(expectedDynamicList1);
        }

        @Test
        void shouldShowC2SupportingDocumentsInDocumentTypes() {
            initialiseUserService(HMCTS_LOGIN_TYPE);
            DocumentUploaderType uploaderType = getUploaderType(HMCTS_LOGIN_TYPE);

            when(caseConverter.toMap(any())).thenReturn(Map.of());
            when(dynamicListService.asDynamicList(List.of(
                Pair.of(AA_PARENT_APPLICATIONS.name(), AA_PARENT_APPLICATIONS.getDescription()),
                Pair.of(C2_APPLICATION_DOCUMENTS.name(), C2_APPLICATION_DOCUMENTS.getDescription())
            ))).thenReturn(expectedDynamicList1);

            DynamicList dynamicList = underTest.buildDocumentTypeDynamicListForRemoval(CaseData.builder()
                .additionalApplicationsBundle(List.of(element(AdditionalApplicationsBundle.builder()
                    .c2DocumentBundle(C2DocumentBundle.builder()
                        .supportingEvidenceBundle(List.of(element(SupportingEvidenceBundle.builder()
                            .document(testDocumentReference())
                            .uploaderType(uploaderType)
                            .uploaderCaseRoles(getUploaderCaseRoles(HMCTS_LOGIN_TYPE))
                            .build())))
                        .build())
                    .build())))
                .build());
            assertThat(dynamicList).isEqualTo(expectedDynamicList1);
        }

        @Test
        void shouldShowConfidentialC2SupportingDocumentsInDocumentTypes() {
            initialiseUserService(HMCTS_LOGIN_TYPE);
            DocumentUploaderType uploaderType = getUploaderType(HMCTS_LOGIN_TYPE);

            when(caseConverter.toMap(any())).thenReturn(Map.of());
            when(dynamicListService.asDynamicList(List.of(
                Pair.of(AA_PARENT_APPLICATIONS.name(), AA_PARENT_APPLICATIONS.getDescription()),
                Pair.of(C2_APPLICATION_DOCUMENTS.name(), C2_APPLICATION_DOCUMENTS.getDescription())
            ))).thenReturn(expectedDynamicList1);

            DynamicList dynamicList = underTest.buildDocumentTypeDynamicListForRemoval(CaseData.builder()
                .additionalApplicationsBundle(List.of(element(AdditionalApplicationsBundle.builder()
                    .c2DocumentBundleConfidential(C2DocumentBundle.builder()
                        .supportingEvidenceBundle(List.of(element(SupportingEvidenceBundle.builder()
                            .document(testDocumentReference())
                            .uploaderType(uploaderType)
                            .uploaderCaseRoles(getUploaderCaseRoles(HMCTS_LOGIN_TYPE))
                            .build())))
                        .build())
                    .build())))
                .build());
            assertThat(dynamicList).isEqualTo(expectedDynamicList1);
        }

        private static Stream<Arguments> buildC2DocumentBundleModifiers() {
            return ManageDocumentServiceTest.buildC2DocumentBundleModifiers();
        }

        @ParameterizedTest
        @MethodSource("buildC2DocumentBundleModifiers")
        void shouldShowConfidentialC2SupportingDocumentsUploadedByOthersInDocumentTypes(String modifier) {
            initialiseUserService(HMCTS_LOGIN_TYPE);
            DocumentUploaderType uploaderType = getUploaderType(HMCTS_LOGIN_TYPE);

            when(caseConverter.toMap(any())).thenReturn(Map.of());
            when(dynamicListService.asDynamicList(List.of(
                Pair.of(AA_PARENT_APPLICATIONS.name(), AA_PARENT_APPLICATIONS.getDescription()),
                Pair.of(C2_APPLICATION_DOCUMENTS.name(), C2_APPLICATION_DOCUMENTS.getDescription())
            ))).thenReturn(expectedDynamicList1);

            DynamicList dynamicList = underTest.buildDocumentTypeDynamicListForRemoval(CaseData.builder()
                .additionalApplicationsBundle(List.of(element(
                    toConfidentialAdditionalApplicationsBundleBuilder(modifier,
                        C2DocumentBundle.builder()
                            .supportingEvidenceBundle(List.of(element(SupportingEvidenceBundle.builder()
                                .document(testDocumentReference())
                                .uploaderType(uploaderType)
                                .uploaderCaseRoles(getUploaderCaseRoles(HMCTS_LOGIN_TYPE))
                                .build())))
                            .build()).build()
                ))).build());
            assertThat(dynamicList).isEqualTo(expectedDynamicList1);
        }

    }

    @Nested
    class BuildAvailableDocumentsToBeRemovedTest {

        private CaseData.CaseDataBuilder createCaseDataBuilderForRemovalDocumentJourney() {
            return CaseData.builder().id(CASE_ID)
                .manageDocumentEventData(ManageDocumentEventData.builder()
                    .manageDocumentAction(ManageDocumentAction.REMOVE_DOCUMENTS)
                    .build());
        }

        UUID elementId1 = UUID.randomUUID();
        UUID elementId2 = UUID.randomUUID();
        UUID elementId3 = UUID.randomUUID();
        UUID elementId4 = UUID.randomUUID();

        String filename1 = "COURT BUNDLE1.docx";
        String filename2 = "COURT BUNDLE2.docx";
        String filename3 = "COURT BUNDLE3.docx";
        String filename4 = "COURT BUNDLE4.docx";

        DynamicList expectedDynamicList1 = DynamicList.builder().build();
        DynamicList expectedDynamicList2 = DynamicList.builder().build();
        DynamicList expectedDynamicList3 = DynamicList.builder().build();

        DocumentReference additionalApplicationDocument = testDocumentReference("additional-application");

        @ParameterizedTest
        @ValueSource(ints = {LA_LOGIN_TYPE, 2, EXT_SOL_LOGIN_TYPE, HMCTS_LOGIN_TYPE, 5})
        void testForNonConfidentialCourtBundleUploadedByThemselves(int loginType) {
            initialiseUserService(loginType);
            DocumentUploaderType uploaderType = getUploaderType(loginType);
            CaseData.CaseDataBuilder builder = createCaseDataBuilderForRemovalDocumentJourney();
            builder.hearingDocuments(HearingDocuments.builder()
                .courtBundleListV2(List.of(element(HearingCourtBundle.builder()
                    .courtBundle(List.of(
                        element(elementId1, CourtBundle.builder()
                            .document(testDocumentReference(filename1))
                            .uploaderType(uploaderType)
                            .uploaderCaseRoles(getUploaderCaseRoles(loginType))
                            .build()),
                        element(elementId2, CourtBundle.builder()
                            .document(testDocumentReference(filename2))
                            .uploaderType(uploaderType)
                            .uploaderCaseRoles(getUploaderCaseRoles(loginType))
                            .build())
                    ))
                    .build())))
                .build());

            when(dynamicListService.asDynamicList(List.of(
                Pair.of(format("hearingDocuments.courtBundleListV2###%s", elementId1), filename1),
                Pair.of(format("hearingDocuments.courtBundleListV2###%s", elementId2), filename2)
            ))).thenReturn(expectedDynamicList1);

            DynamicList dynamicList = underTest.buildAvailableDocumentsToBeRemoved(builder.build());
            assertThat(dynamicList).isEqualTo(expectedDynamicList1);
        }

        @ParameterizedTest
        @ValueSource(ints = {LA_LOGIN_TYPE, 2, EXT_SOL_LOGIN_TYPE, HMCTS_LOGIN_TYPE, 5})
        void testForNonConfidentialCourtBundleUploadedByHMCTS(int loginType) {
            initialiseUserService(loginType);
            DocumentUploaderType uploaderType = getUploaderType(loginType);
            CaseData.CaseDataBuilder builder = createCaseDataBuilderForRemovalDocumentJourney();
            builder.hearingDocuments(HearingDocuments.builder()
                .courtBundleListV2(List.of(element(HearingCourtBundle.builder()
                    .courtBundle(List.of(
                        element(elementId1, CourtBundle.builder()
                            .document(testDocumentReference(filename1))
                            .uploaderType(DocumentUploaderType.HMCTS)
                            .uploaderCaseRoles(getUploaderCaseRoles(HMCTS_LOGIN_TYPE))
                            .build()),
                        element(elementId2, CourtBundle.builder()
                            .document(testDocumentReference(filename2))
                            .uploaderType(DocumentUploaderType.HMCTS)
                            .uploaderCaseRoles(getUploaderCaseRoles(HMCTS_LOGIN_TYPE))
                            .build())
                    ))
                    .build())))
                .build());

            when(dynamicListService.asDynamicList(List.of(
                Pair.of(format("hearingDocuments.courtBundleListV2###%s", elementId1), filename1),
                Pair.of(format("hearingDocuments.courtBundleListV2###%s", elementId2), filename2)
            ))).thenReturn(expectedDynamicList1);
            when(dynamicListService.asDynamicList(List.of())).thenReturn(expectedDynamicList2);

            DynamicList dynamicList = underTest.buildAvailableDocumentsToBeRemoved(builder.build());
            if (uploaderType == DocumentUploaderType.HMCTS) {
                assertThat(dynamicList).isEqualTo(expectedDynamicList1);
            } else {
                assertThat(dynamicList).isEqualTo(expectedDynamicList2);
            }
        }

        @ParameterizedTest
        @ValueSource(ints = {LA_LOGIN_TYPE, 2})
        void testForNonConfidentialCourtBundleUploadedByLA(int loginType) {
            initialiseUserService(loginType);
            DocumentUploaderType uploaderType = getUploaderType(loginType);
            CaseData.CaseDataBuilder builder = createCaseDataBuilderForRemovalDocumentJourney();
            builder.hearingDocuments(HearingDocuments.builder()
                .courtBundleListV2(List.of(element(HearingCourtBundle.builder()
                    .courtBundle(List.of(
                        element(elementId1, CourtBundle.builder()
                            .document(testDocumentReference(filename1))
                            .uploaderType(getUploaderType(loginType))
                            .uploaderCaseRoles(getUploaderCaseRoles(loginType))
                            .build()),
                        element(elementId2, CourtBundle.builder()
                            .document(testDocumentReference(filename2))
                            .uploaderType(getUploaderType(loginType))
                            .uploaderCaseRoles(getUploaderCaseRoles(loginType))
                            .build())
                    ))
                    .build())))
                .build());

            when(dynamicListService.asDynamicList(List.of(
                Pair.of(format("hearingDocuments.courtBundleListV2###%s", elementId1), filename1),
                Pair.of(format("hearingDocuments.courtBundleListV2###%s", elementId2), filename2)
            ))).thenReturn(expectedDynamicList1);
            when(dynamicListService.asDynamicList(List.of())).thenReturn(expectedDynamicList2);

            DynamicList dynamicList = underTest.buildAvailableDocumentsToBeRemoved(builder.build());
            if (uploaderType == DocumentUploaderType.HMCTS
                || uploaderType == DocumentUploaderType.DESIGNATED_LOCAL_AUTHORITY
                || uploaderType == DocumentUploaderType.SECONDARY_LOCAL_AUTHORITY) {
                assertThat(dynamicList).isEqualTo(expectedDynamicList1);
            } else {
                assertThat(dynamicList).isEqualTo(expectedDynamicList2);
            }
        }

        @ParameterizedTest
        @ValueSource(ints = {LA_LOGIN_TYPE, 2, EXT_SOL_LOGIN_TYPE, HMCTS_LOGIN_TYPE, 5})
        void testForNonConfidentialCourtBundleUploadedBySolicitor(int loginType) {
            initialiseUserService(loginType);
            CaseData.CaseDataBuilder builder = createCaseDataBuilderForRemovalDocumentJourney();
            builder.hearingDocuments(HearingDocuments.builder()
                .courtBundleListV2(List.of(element(HearingCourtBundle.builder()
                    .courtBundle(List.of(
                        element(elementId1, CourtBundle.builder()
                            .document(testDocumentReference(filename1))
                            .uploaderType(DocumentUploaderType.SOLICITOR)
                            .uploaderCaseRoles(getUploaderCaseRoles(EXT_SOL_LOGIN_TYPE))
                            .build()),
                        element(elementId2, CourtBundle.builder()
                            .document(testDocumentReference(filename2))
                            .uploaderType(DocumentUploaderType.SOLICITOR)
                            .uploaderCaseRoles(getUploaderCaseRoles(EXT_SOL_LOGIN_TYPE))
                            .build())
                    ))
                    .build())))
                .build());

            when(dynamicListService.asDynamicList(List.of(
                Pair.of(format("hearingDocuments.courtBundleListV2###%s", elementId1), filename1),
                Pair.of(format("hearingDocuments.courtBundleListV2###%s", elementId2), filename2)
            ))).thenReturn(expectedDynamicList1);
            when(dynamicListService.asDynamicList(List.of())).thenReturn(expectedDynamicList2);

            DynamicList dynamicList = underTest.buildAvailableDocumentsToBeRemoved(builder.build());
            if (loginType == LA_LOGIN_TYPE || loginType == 2) { // LAs should get an empty dynamic list
                assertThat(dynamicList).isEqualTo(expectedDynamicList2);
            } else {
                assertThat(dynamicList).isEqualTo(expectedDynamicList1);
            }
        }

        @ParameterizedTest
        @ValueSource(ints = {EXT_SOL_LOGIN_TYPE}) // testing for solicitor login type only
        void shouldReturnEmptyDynamicListWhenNonConfidentialCourtBundleAreUploadedByOthers(
            int loginType) {
            initialiseUserService(loginType);
            CaseData.CaseDataBuilder builder = createCaseDataBuilderForRemovalDocumentJourney();
            builder.hearingDocuments(HearingDocuments.builder()
                .courtBundleListV2(List.of(element(HearingCourtBundle.builder()
                    .courtBundle(List.of(
                        element(elementId1, CourtBundle.builder()
                            .document(testDocumentReference(filename1))
                            .uploaderType(DocumentUploaderType.DESIGNATED_LOCAL_AUTHORITY)
                            .uploaderCaseRoles(getUploaderCaseRoles(LA_LOGIN_TYPE))
                            .build()),
                        element(elementId2, CourtBundle.builder()
                            .document(testDocumentReference(filename2))
                            .uploaderType(null)
                            .build()),
                        element(elementId3, CourtBundle.builder()
                            .document(testDocumentReference(filename3))
                            .uploaderType(DocumentUploaderType.HMCTS)
                            .uploaderCaseRoles(getUploaderCaseRoles(HMCTS_LOGIN_TYPE))
                            .build()),
                        element(elementId4, CourtBundle.builder()
                            .document(testDocumentReference(filename4))
                            .uploaderType(DocumentUploaderType.SOLICITOR)
                            .uploaderCaseRoles(List.of(CaseRole.SOLICITORJ))
                            .build())
                    ))
                    .build())))
                .build());

            when(dynamicListService.asDynamicList(List.of())).thenReturn(expectedDynamicList1);

            DynamicList dynamicList = underTest.buildAvailableDocumentsToBeRemoved(builder.build());
            assertThat(dynamicList).isEqualTo(expectedDynamicList1);
        }

        @ParameterizedTest
        @ValueSource(ints = {LA_LOGIN_TYPE, 2, EXT_SOL_LOGIN_TYPE, HMCTS_LOGIN_TYPE, 5})
        void testForConfidentialCTSCUploadedAndNonConfidentialCourtBundleExist(
            int loginType) {
            initialiseUserService(loginType);
            DocumentUploaderType uploaderType = getUploaderType(loginType);
            CaseData.CaseDataBuilder builder = createCaseDataBuilderForRemovalDocumentJourney();
            builder.hearingDocuments(HearingDocuments.builder()
                .courtBundleListCTSC(List.of(element(HearingCourtBundle.builder()
                    .courtBundle(List.of(
                        element(elementId3, CourtBundle.builder()
                            .document(testDocumentReference(filename3))
                            .uploaderType(DocumentUploaderType.HMCTS)
                            .uploaderCaseRoles(getUploaderCaseRoles(HMCTS_LOGIN_TYPE))
                            .build()),
                        element(elementId4, CourtBundle.builder()
                            .document(testDocumentReference(filename4))
                            // no uploaderType and uploaderCaseRoles means the case is migrated from existing data
                            .build())
                    ))
                    .build())))
                .courtBundleListV2(List.of(element(HearingCourtBundle.builder()
                    .courtBundle(List.of(
                        element(elementId1, CourtBundle.builder()
                            .document(testDocumentReference(filename1))
                            .uploaderType(DocumentUploaderType.SOLICITOR)
                            .uploaderCaseRoles(getUploaderCaseRoles(EXT_SOL_LOGIN_TYPE))
                            .build()),
                        element(elementId2, CourtBundle.builder()
                            .document(testDocumentReference(filename2))
                            .uploaderType(DocumentUploaderType.SOLICITOR)
                            .uploaderCaseRoles(getUploaderCaseRoles(EXT_SOL_LOGIN_TYPE))
                            .build())
                    ))
                    .build())))
                .build());
            when(dynamicListService.asDynamicList(List.of(
                Pair.of(format("hearingDocuments.courtBundleListV2###%s", elementId1), filename1),
                Pair.of(format("hearingDocuments.courtBundleListV2###%s", elementId2), filename2)
            ))).thenReturn(expectedDynamicList1);
            when(dynamicListService.asDynamicList(List.of(
                Pair.of(format("hearingDocuments.courtBundleListV2###%s", elementId1), filename1),
                Pair.of(format("hearingDocuments.courtBundleListV2###%s", elementId2), filename2),
                Pair.of(format("hearingDocuments.courtBundleListCTSC###%s", elementId3), filename3),
                Pair.of(format("hearingDocuments.courtBundleListCTSC###%s", elementId4), filename4)
            ))).thenReturn(expectedDynamicList2);
            when(dynamicListService.asDynamicList(List.of())).thenReturn(expectedDynamicList3);

            DynamicList dynamicList = underTest.buildAvailableDocumentsToBeRemoved(builder.build());
            if (uploaderType == DocumentUploaderType.HMCTS) {
                assertThat(dynamicList).isEqualTo(expectedDynamicList2);
            } else if (uploaderType == DocumentUploaderType.DESIGNATED_LOCAL_AUTHORITY
                || uploaderType == DocumentUploaderType.SECONDARY_LOCAL_AUTHORITY) {
                assertThat(dynamicList).isEqualTo(expectedDynamicList3);
            } else {
                assertThat(dynamicList).isEqualTo(expectedDynamicList1);
            }
        }

        @ParameterizedTest
        @ValueSource(ints = {LA_LOGIN_TYPE, 2, EXT_SOL_LOGIN_TYPE, HMCTS_LOGIN_TYPE, 5})
        void shouldReturnDynamicListWhenC2ApplicationSupportingDocumentUploadedByThemselves(
            int loginType) {
            initialiseUserService(loginType);
            CaseData.CaseDataBuilder builder = createCaseDataBuilderForRemovalDocumentJourney();
            builder.additionalApplicationsBundle(List.of(
                element(AdditionalApplicationsBundle.builder()
                    .c2DocumentBundle(C2DocumentBundle.builder()
                        .document(additionalApplicationDocument)
                        .supportingEvidenceBundle(List.of(element(elementId1, SupportingEvidenceBundle.builder()
                            .document(testDocumentReference(filename1))
                            .uploaderType(getUploaderType(loginType))
                            .uploaderCaseRoles(getUploaderCaseRoles(loginType))
                            .build())))
                        .build())
                    .build())
            ));

            when(dynamicListService.asDynamicList(List.of(
                Pair.of(format("%s###%s", C2_APPLICATION_DOCUMENTS.name(), elementId1), filename1)
            ))).thenReturn(expectedDynamicList1);

            DynamicList dynamicList = underTest.buildAvailableDocumentsToBeRemoved(builder.build());
            assertThat(dynamicList).isEqualTo(expectedDynamicList1);
        }

        @ParameterizedTest
        @ValueSource(ints = {LA_LOGIN_TYPE, 2, EXT_SOL_LOGIN_TYPE, HMCTS_LOGIN_TYPE, 5})
        void shouldReturnDynamicListWhenC1ApplicationSupportingDocumentUploadedByThemselves(
            int loginType) {
            initialiseUserService(loginType);
            CaseData.CaseDataBuilder builder = createCaseDataBuilderForRemovalDocumentJourney();
            builder.additionalApplicationsBundle(List.of(
                element(AdditionalApplicationsBundle.builder()
                    .otherApplicationsBundle(OtherApplicationsBundle.builder()
                        .document(additionalApplicationDocument)
                        .supportingEvidenceBundle(List.of(element(elementId1, SupportingEvidenceBundle.builder()
                            .document(testDocumentReference(filename1))
                            .uploaderType(getUploaderType(loginType))
                            .uploaderCaseRoles(getUploaderCaseRoles(loginType))
                            .build())))
                        .build())
                    .build())
            ));

            when(dynamicListService.asDynamicList(List.of(
                Pair.of(format("%s###%s", C1_APPLICATION_DOCUMENTS.name(), elementId1), filename1)
            ))).thenReturn(expectedDynamicList1);

            DynamicList dynamicList = underTest.buildAvailableDocumentsToBeRemoved(builder.build());
            assertThat(dynamicList).isEqualTo(expectedDynamicList1);
        }

        @ParameterizedTest
        @ValueSource(ints = {LA_LOGIN_TYPE, HMCTS_LOGIN_TYPE})
        void shouldReturnDynamicListWhenC1WithSupplementSupportingDocumentUploadedByLA(
            int loginType) {
            initialiseUserService(loginType);
            CaseData.CaseDataBuilder builder = createCaseDataBuilderForRemovalDocumentJourney();
            builder.submittedC1WithSupplement(
                SubmittedC1WithSupplementBundle.builder()
                    .supportingEvidenceBundle(List.of(element(elementId1, SupportingEvidenceBundle.builder()
                        .document(testDocumentReference(filename1))
                        .uploaderType(getUploaderType(LA_LOGIN_TYPE))
                        .uploaderCaseRoles(getUploaderCaseRoles(LA_LOGIN_TYPE))
                        .build())))
                    .build());

            when(dynamicListService.asDynamicList(List.of(
                Pair.of(format("%s###%s", C1_APPLICATION_DOCUMENTS.name(), elementId1), filename1)
            ))).thenReturn(expectedDynamicList1);

            DynamicList dynamicList = underTest.buildAvailableDocumentsToBeRemoved(builder.build());
            assertThat(dynamicList).isEqualTo(expectedDynamicList1);
        }

        @ParameterizedTest
        @ValueSource(ints = {LA_LOGIN_TYPE, HMCTS_LOGIN_TYPE})
        void shouldReturnDynamicListWhenConfidentialC2ApplicationSupportingDocumentUploadedByLA(
            int loginType) {
            initialiseUserService(loginType);
            CaseData.CaseDataBuilder builder = createCaseDataBuilderForRemovalDocumentJourney();
            builder.additionalApplicationsBundle(List.of(
                element(AdditionalApplicationsBundle.builder()
                    .c2DocumentBundleConfidential(C2DocumentBundle.builder()
                        .document(additionalApplicationDocument)
                        .supportingEvidenceBundle(List.of(element(elementId1, SupportingEvidenceBundle.builder()
                            .document(testDocumentReference(filename1))
                            .uploaderType(getUploaderType(LA_LOGIN_TYPE))
                            .uploaderCaseRoles(getUploaderCaseRoles(LA_LOGIN_TYPE))
                            .build())))
                        .build())
                    .c2DocumentBundleLA(C2DocumentBundle.builder()
                        .document(additionalApplicationDocument)
                        .supportingEvidenceBundle(List.of(element(elementId1, SupportingEvidenceBundle.builder()
                            .document(testDocumentReference(filename1))
                            .uploaderType(getUploaderType(LA_LOGIN_TYPE))
                            .uploaderCaseRoles(getUploaderCaseRoles(LA_LOGIN_TYPE))
                            .build())))
                        .build())
                    .build())
            ));

            when(dynamicListService.asDynamicList(List.of(
                Pair.of(format("%s###%s", C2_APPLICATION_DOCUMENTS.name(), elementId1), filename1)
            ))).thenReturn(expectedDynamicList1);

            DynamicList dynamicList = underTest.buildAvailableDocumentsToBeRemoved(builder.build());
            assertThat(dynamicList).isEqualTo(expectedDynamicList1);
        }

        @ParameterizedTest
        @ValueSource(ints = {EXT_SOL_LOGIN_TYPE, LEGACY_LOGIN_TYPE})
        void laShouldGetEmptyDynamicListWhenConfidentialC2ApplicationSupportingDocumentUploadedByOtherSolicitor(
            int uploaderLoginType) {
            initialiseUserService(LA_LOGIN_TYPE);
            CaseData.CaseDataBuilder builder = createCaseDataBuilderForRemovalDocumentJourney();
            builder.additionalApplicationsBundle(List.of(
                element(AdditionalApplicationsBundle.builder()
                    .c2DocumentBundleConfidential(C2DocumentBundle.builder()
                        .document(additionalApplicationDocument)
                        .supportingEvidenceBundle(List.of(element(elementId1, SupportingEvidenceBundle.builder()
                            .document(testDocumentReference(filename1))
                            .uploaderType(getUploaderType(uploaderLoginType))
                            .uploaderCaseRoles(getUploaderCaseRoles(uploaderLoginType))
                            .build())))
                        .build())
                    .c2DocumentBundleResp0(C2DocumentBundle.builder()
                        .document(additionalApplicationDocument)
                        .supportingEvidenceBundle(List.of(element(elementId1, SupportingEvidenceBundle.builder()
                            .document(testDocumentReference(filename1))
                            .uploaderType(getUploaderType(uploaderLoginType))
                            .uploaderCaseRoles(getUploaderCaseRoles(uploaderLoginType))
                            .build())))
                        .build())
                    .build())
            ));

            when(dynamicListService.asDynamicList(List.of())).thenReturn(expectedDynamicList1);

            DynamicList dynamicList = underTest.buildAvailableDocumentsToBeRemoved(builder.build());
            assertThat(dynamicList).isEqualTo(expectedDynamicList1);
        }

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        void shouldGetDynamicListWhenConfidentialC2ApplicationSupportingDocumentUploadedByOtherSolicitor(
            boolean isChildSolicitor) {
            initialiseUserService(EXT_SOL_LOGIN_TYPE, isChildSolicitor);
            CaseData.CaseDataBuilder builder = createCaseDataBuilderForRemovalDocumentJourney();
            if (isChildSolicitor) {
                builder.additionalApplicationsBundle(List.of(
                    element(AdditionalApplicationsBundle.builder()
                        .c2DocumentBundleConfidential(C2DocumentBundle.builder()
                            .document(additionalApplicationDocument)
                            .supportingEvidenceBundle(List.of(element(elementId1, SupportingEvidenceBundle.builder()
                                .document(testDocumentReference(filename1))
                                .uploaderType(getUploaderType(EXT_SOL_LOGIN_TYPE))
                                .uploaderCaseRoles(getUploaderCaseRoles(EXT_SOL_LOGIN_TYPE, true))
                                .build())))
                            .build())
                        .c2DocumentBundleChild0(C2DocumentBundle.builder()
                            .document(additionalApplicationDocument)
                            .supportingEvidenceBundle(List.of(element(elementId1, SupportingEvidenceBundle.builder()
                                .document(testDocumentReference(filename1))
                                .uploaderType(getUploaderType(EXT_SOL_LOGIN_TYPE))
                                .uploaderCaseRoles(getUploaderCaseRoles(EXT_SOL_LOGIN_TYPE, true))
                                .build())))
                            .build())
                        .build())
                ));
            } else {
                builder.additionalApplicationsBundle(List.of(
                    element(AdditionalApplicationsBundle.builder()
                        .c2DocumentBundleConfidential(C2DocumentBundle.builder()
                            .document(additionalApplicationDocument)
                            .supportingEvidenceBundle(List.of(element(elementId1, SupportingEvidenceBundle.builder()
                                .document(testDocumentReference(filename1))
                                .uploaderType(getUploaderType(EXT_SOL_LOGIN_TYPE))
                                .uploaderCaseRoles(getUploaderCaseRoles(EXT_SOL_LOGIN_TYPE, false))
                                .build())))
                            .build())
                        .c2DocumentBundleResp0(C2DocumentBundle.builder()
                            .document(additionalApplicationDocument)
                            .supportingEvidenceBundle(List.of(element(elementId1, SupportingEvidenceBundle.builder()
                                .document(testDocumentReference(filename1))
                                .uploaderType(getUploaderType(EXT_SOL_LOGIN_TYPE))
                                .uploaderCaseRoles(getUploaderCaseRoles(EXT_SOL_LOGIN_TYPE, false))
                                .build())))
                            .build())
                        .build())
                ));
            }

            when(dynamicListService.asDynamicList(List.of(
                Pair.of(format("%s###%s", C2_APPLICATION_DOCUMENTS.name(), elementId1), filename1)
            ))).thenReturn(expectedDynamicList1);

            DynamicList dynamicList = underTest.buildAvailableDocumentsToBeRemoved(builder.build());
            assertThat(dynamicList).isEqualTo(expectedDynamicList1);
        }
    }

    @Nested
    class RemoveDocumentTests {

        UUID hearingCourtBundleElementIdOne = UUID.randomUUID();
        UUID hearingCourtBundleElementIdTwo = UUID.randomUUID();

        UUID elementId1 = UUID.randomUUID();
        UUID elementId2 = UUID.randomUUID();
        UUID elementId3 = UUID.randomUUID();
        UUID elementId4 = UUID.randomUUID();

        String filename1 = "COURT BUNDLE1.docx";
        String filename2 = "COURT BUNDLE2.docx";
        String filename3 = "COURT BUNDLE3.docx";
        String filename4 = "COURT BUNDLE4.docx";

        CourtBundle cb1 = CourtBundle.builder()
            .document(testDocumentReference(filename1))
            .uploaderType(DocumentUploaderType.SOLICITOR)
            .uploaderCaseRoles(getUploaderCaseRoles(3))
            .build();
        CourtBundle cb2 = CourtBundle.builder()
            .document(testDocumentReference(filename2))
            .uploaderType(DocumentUploaderType.SOLICITOR)
            .uploaderCaseRoles(getUploaderCaseRoles(3))
            .build();
        CourtBundle cb3 = CourtBundle.builder()
            .document(testDocumentReference(filename3))
            .uploaderType(DocumentUploaderType.DESIGNATED_LOCAL_AUTHORITY)
            .uploaderCaseRoles(getUploaderCaseRoles(1))
            .build();
        CourtBundle cb4 = CourtBundle.builder()
            .document(testDocumentReference(filename4))
            .uploaderType(DocumentUploaderType.HMCTS)
            .uploaderCaseRoles(getUploaderCaseRoles(HMCTS_LOGIN_TYPE))
            .build();

        ManagedDocument md1 = ManagedDocument.builder()
            .document(testDocumentReference(filename1))
            .uploaderType(DocumentUploaderType.HMCTS)
            .uploaderCaseRoles(getUploaderCaseRoles(HMCTS_LOGIN_TYPE))
            .build();
        ManagedDocument md2 = ManagedDocument.builder()
            .document(testDocumentReference(filename2))
            .uploaderType(DocumentUploaderType.HMCTS)
            .uploaderCaseRoles(getUploaderCaseRoles(HMCTS_LOGIN_TYPE))
            .build();

        CaseSummary cs1 = CaseSummary.builder()
            .document(testDocumentReference(filename1))
            .uploaderType(DocumentUploaderType.SOLICITOR)
            .uploaderCaseRoles(getUploaderCaseRoles(3))
            .build();
        CaseSummary cs2 = CaseSummary.builder()
            .document(testDocumentReference(filename1))
            .uploaderType(DocumentUploaderType.SOLICITOR)
            .uploaderCaseRoles(getUploaderCaseRoles(3))
            .build();

        UUID placementId = UUID.randomUUID();
        UUID placementConfidentialDocId = UUID.randomUUID();
        PlacementNoticeDocument pnd1 = PlacementNoticeDocument.builder()
            .response(testDocumentReference(filename1))
            .uploaderType(DocumentUploaderType.SOLICITOR)
            .uploaderCaseRoles(getUploaderCaseRoles(3))
            .build();
        PlacementNoticeDocument pnd2 = PlacementNoticeDocument.builder()
            .response(testDocumentReference(filename2))
            .uploaderType(DocumentUploaderType.SOLICITOR)
            .uploaderCaseRoles(getUploaderCaseRoles(3))
            .build();

        DocumentReference additionalApplicationDocument = testDocumentReference("C2APPLICATION");

        @Test
        void shouldBeAbleToRemovePlacementResponseFromSinglePlacementResponseByAdmin() {
            int loginType = HMCTS_LOGIN_TYPE;
            initialiseUserService(loginType);
            CaseData.CaseDataBuilder builder = CaseData.builder().id(CASE_ID);
            builder.placementEventData(PlacementEventData.builder()
                .placements(List.of(element(placementId, Placement.builder()
                    .childId(UUID.fromString("11111111-1111-1111-1111-111111111111"))
                    .noticeDocuments(new ArrayList<>(List.of(element(elementId1, pnd1))))
                    .confidentialDocuments(List.of(element(placementConfidentialDocId,
                        PlacementConfidentialDocument.builder().build())))
                    .build())))
                .build());
            builder.manageDocumentEventData(ManageDocumentEventData.builder()
                .manageDocumentAction(ManageDocumentAction.REMOVE_DOCUMENTS)
                .manageDocumentRemoveDocReason(ManageDocumentRemovalReason.UPLOADED_TO_WRONG_CASE)
                .documentsToBeRemoved(DynamicList.builder()
                    .value(DynamicListElement.builder()
                        .code("PLACEMENT_RESPONSES###" + elementId1)
                        .build())
                    .build())
                .build());

            Map<String, Object> result = underTest.removeDocuments(builder.build());
            PlacementNoticeDocument removedPnd = PlacementNoticeDocument.builder()
                .response(pnd1.getDocument())
                .uploaderType(pnd1.getUploaderType())
                .uploaderCaseRoles(pnd1.getUploaderCaseRoles())
                .build();
            removedPnd.setRemovalReason("The document was uploaded to the wrong case");

            assertThat(result.get("placements")).isEqualTo(List.of(
                element(placementId, Placement.builder()
                    .childId(UUID.fromString("11111111-1111-1111-1111-111111111111"))
                    .confidentialDocuments(List.of(element(placementConfidentialDocId,
                        PlacementConfidentialDocument.builder().build())))
                    .noticeDocuments(List.of())
                    .noticeDocumentsRemoved(List.of(element(elementId1, removedPnd)))
                    .build())
            ));
            assertThat(result.get("placementsNonConfidential")).isEqualTo(List.of(
                element(placementId, Placement.builder()
                    .childId(UUID.fromString("11111111-1111-1111-1111-111111111111"))
                    .noticeDocuments(List.of())
                    .noticeDocumentsRemoved(List.of(element(elementId1, removedPnd)))
                    .build())
            ));
            assertThat(result.get("placementsNonConfidentialNotices")).isEqualTo(List.of(
                element(placementId, Placement.builder()
                    .childId(UUID.fromString("11111111-1111-1111-1111-111111111111"))
                    .noticeDocuments(List.of())
                    .noticeDocumentsRemoved(List.of(element(elementId1, removedPnd)))
                    .build())
            ));
        }

        @Test
        void shouldBeAbleToRemovePlacementResponseFromMultiplePlacementResponsesByAdmin() {
            int loginType = HMCTS_LOGIN_TYPE;
            initialiseUserService(loginType);
            CaseData.CaseDataBuilder builder = CaseData.builder().id(CASE_ID);
            builder.placementEventData(PlacementEventData.builder()
                .placements(List.of(element(placementId, Placement.builder()
                    .childId(UUID.fromString("11111111-1111-1111-1111-111111111111"))
                    .noticeDocuments(new ArrayList<>(List.of(element(elementId1, pnd1), element(elementId2, pnd2))))
                    .confidentialDocuments(List.of(element(placementConfidentialDocId,
                        PlacementConfidentialDocument.builder().build())))
                    .build())))
                .build());
            builder.manageDocumentEventData(ManageDocumentEventData.builder()
                .manageDocumentAction(ManageDocumentAction.REMOVE_DOCUMENTS)
                .manageDocumentRemoveDocReason(ManageDocumentRemovalReason.UPLOADED_TO_WRONG_CASE)
                .documentsToBeRemoved(DynamicList.builder()
                    .value(DynamicListElement.builder()
                        .code("PLACEMENT_RESPONSES###" + elementId2)
                        .build())
                    .build())
                .build());

            Map<String, Object> result = underTest.removeDocuments(builder.build());
            PlacementNoticeDocument removedPnd = PlacementNoticeDocument.builder()
                .response(pnd2.getDocument())
                .uploaderType(pnd2.getUploaderType())
                .uploaderCaseRoles(pnd2.getUploaderCaseRoles())
                .build();
            removedPnd.setRemovalReason("The document was uploaded to the wrong case");

            assertThat(result.get("placements")).isEqualTo(List.of(
                element(placementId, Placement.builder()
                    .childId(UUID.fromString("11111111-1111-1111-1111-111111111111"))
                    .confidentialDocuments(List.of(element(placementConfidentialDocId,
                        PlacementConfidentialDocument.builder().build())))
                    .noticeDocuments(List.of(element(elementId1, pnd1)))
                    .noticeDocumentsRemoved(List.of(element(elementId2, removedPnd)))
                    .build())
            ));
            assertThat(result.get("placementsNonConfidential")).isEqualTo(List.of(
                element(placementId, Placement.builder()
                    .childId(UUID.fromString("11111111-1111-1111-1111-111111111111"))
                    .noticeDocuments(List.of(element(elementId1, pnd1)))
                    .noticeDocumentsRemoved(List.of(element(elementId2, removedPnd)))
                    .build())
            ));
            assertThat(result.get("placementsNonConfidentialNotices")).isEqualTo(List.of(
                element(placementId, Placement.builder()
                    .childId(UUID.fromString("11111111-1111-1111-1111-111111111111"))
                    .noticeDocuments(List.of(element(elementId1, pnd1)))
                    .noticeDocumentsRemoved(List.of(element(elementId2, removedPnd)))
                    .build())
            ));
        }

        @Test
        void shouldBeAbleToRemoveNcCaseSummaryBySolicitor() {
            int loginType = 3;
            initialiseUserService(loginType);
            CaseData.CaseDataBuilder builder = CaseData.builder().id(CASE_ID);
            builder.hearingDocuments(HearingDocuments.builder()
                .caseSummaryList(new ArrayList<>(List.of(element(elementId1, cs1))))
                .build());
            builder.manageDocumentEventData(ManageDocumentEventData.builder()
                .manageDocumentAction(ManageDocumentAction.REMOVE_DOCUMENTS)
                .manageDocumentRemoveDocReason(ManageDocumentRemovalReason.UPLOADED_TO_WRONG_CASE)
                .documentsToBeRemoved(DynamicList.builder()
                    .value(DynamicListElement.builder()
                        .code("hearingDocuments.caseSummaryList###" + elementId1)
                        .build())
                    .build())
                .build());

            Map<String, Object> result = underTest.removeDocuments(builder.build());
            CaseSummary removedCs = CaseSummary.builder()
                .document(cs1.getDocument())
                .uploaderType(cs1.getUploaderType())
                .uploaderCaseRoles(cs1.getUploaderCaseRoles())
                .build();
            removedCs.setRemovalReason("The document was uploaded to the wrong case");
            assertThat(result.get("caseSummaryListRemoved")).isEqualTo(List.of(
                element(elementId1, removedCs)
            ));
            assertThat(result.get("caseSummaryList")).isEqualTo(List.of());
        }

        @Test
        void shouldBeAbleToRemoveCaseSummaryFromMultipleNcCaseSummariesBySolicitor() {
            int loginType = 3;
            initialiseUserService(loginType);
            CaseData.CaseDataBuilder builder = CaseData.builder().id(CASE_ID);
            builder.hearingDocuments(HearingDocuments.builder()
                .caseSummaryList(new ArrayList<>(List.of(element(elementId1, cs1), element(elementId2, cs2))))
                .build());
            builder.manageDocumentEventData(ManageDocumentEventData.builder()
                .manageDocumentAction(ManageDocumentAction.REMOVE_DOCUMENTS)
                .manageDocumentRemoveDocReason(ManageDocumentRemovalReason.UPLOADED_TO_WRONG_CASE)
                .documentsToBeRemoved(DynamicList.builder()
                    .value(DynamicListElement.builder()
                        .code("hearingDocuments.caseSummaryList###" + elementId1)
                        .build())
                    .build())
                .build());

            Map<String, Object> result = underTest.removeDocuments(builder.build());
            CaseSummary removedCs = CaseSummary.builder()
                .document(cs1.getDocument())
                .uploaderType(cs1.getUploaderType())
                .uploaderCaseRoles(cs1.getUploaderCaseRoles())
                .build();
            removedCs.setRemovalReason("The document was uploaded to the wrong case");
            assertThat(result.get("caseSummaryListRemoved")).isEqualTo(List.of(
                element(elementId1, removedCs)
            ));
            assertThat(result.get("caseSummaryList")).isEqualTo(List.of(element(elementId2, cs2)));
        }

        @Test
        void shouldBeAbleToRemoveNcThresholdByLA() {
            int loginType = 1;
            initialiseUserService(loginType);
            CaseData.CaseDataBuilder builder = CaseData.builder().id(CASE_ID);
            builder.thresholdList(new ArrayList<>(List.of(element(elementId1, md1))));
            builder.manageDocumentEventData(ManageDocumentEventData.builder()
                .manageDocumentAction(ManageDocumentAction.REMOVE_DOCUMENTS)
                .manageDocumentRemoveDocReason(ManageDocumentRemovalReason.UPLOADED_TO_WRONG_CASE)
                .documentsToBeRemoved(DynamicList.builder()
                    .value(DynamicListElement.builder()
                        .code("thresholdList###" + elementId1)
                        .build())
                    .build())
                .build());

            Map<String, Object> result = underTest.removeDocuments(builder.build());
            ManagedDocument removedMd = ManagedDocument.builder()
                .document(md1.getDocument())
                .uploaderType(md1.getUploaderType())
                .uploaderCaseRoles(md1.getUploaderCaseRoles())
                .build();
            removedMd.setRemovalReason("The document was uploaded to the wrong case");
            assertThat(result.get("thresholdListRemoved")).isEqualTo(List.of(element(elementId1, removedMd)));
            assertThat(result.get("thresholdList")).isEqualTo(List.of());
        }

        @Test
        void shouldBeAbleToRemoveThresholdFromMultipleNcThresholdsByLA() {
            int loginType = 1;
            initialiseUserService(loginType);
            CaseData.CaseDataBuilder builder = CaseData.builder().id(CASE_ID);
            builder.thresholdList(new ArrayList<>(List.of(element(elementId1, md1), element(elementId2, md2))));
            builder.manageDocumentEventData(ManageDocumentEventData.builder()
                .manageDocumentAction(ManageDocumentAction.REMOVE_DOCUMENTS)
                .manageDocumentRemoveDocReason(ManageDocumentRemovalReason.UPLOADED_TO_WRONG_CASE)
                .documentsToBeRemoved(DynamicList.builder()
                    .value(DynamicListElement.builder()
                        .code("thresholdList###" + elementId2)
                        .build())
                    .build())
                .build());

            Map<String, Object> result = underTest.removeDocuments(builder.build());
            ManagedDocument removedMd = ManagedDocument.builder()
                .document(md2.getDocument())
                .uploaderType(md2.getUploaderType())
                .uploaderCaseRoles(md2.getUploaderCaseRoles())
                .build();
            removedMd.setRemovalReason("The document was uploaded to the wrong case");
            assertThat(result.get("thresholdListRemoved")).isEqualTo(List.of(element(elementId2, removedMd)));
            assertThat(result.get("thresholdList")).isEqualTo(List.of(element(elementId1, md1)));
        }

        @Test
        void shouldBeAbleToRemoveNcCourtBundleWithoutCourtBundleNCByAdmin() {
            int loginType = HMCTS_LOGIN_TYPE;
            initialiseUserService(loginType);
            CaseData.CaseDataBuilder builder = CaseData.builder().id(CASE_ID);
            builder.hearingDocuments(HearingDocuments.builder()
                .courtBundleListV2(List.of(element(hearingCourtBundleElementIdOne, HearingCourtBundle.builder()
                    .courtBundle(new ArrayList<>(List.of(
                        element(elementId1, cb1)
                    )))
                    .build())))
                .build());
            builder.manageDocumentEventData(ManageDocumentEventData.builder()
                .manageDocumentAction(ManageDocumentAction.REMOVE_DOCUMENTS)
                .manageDocumentRemoveDocReason(ManageDocumentRemovalReason.UPLOADED_TO_WRONG_CASE)
                .documentsToBeRemoved(DynamicList.builder()
                    .value(DynamicListElement.builder()
                        .code("hearingDocuments.courtBundleListV2###" + elementId1)
                        .build())
                    .build())
                .build());

            Map<String, Object> result = underTest.removeDocuments(builder.build());
            CourtBundle removedCb = CourtBundle.builder()
                .document(cb1.getDocument())
                .uploaderType(cb1.getUploaderType())
                .uploaderCaseRoles(cb1.getUploaderCaseRoles())
                .build();
            removedCb.setRemovalReason("The document was uploaded to the wrong case");
            assertThat(result.get("courtBundleListRemoved")).isEqualTo(List.of(
                element(hearingCourtBundleElementIdOne, HearingCourtBundle.builder()
                    .courtBundle(List.of(element(elementId1, removedCb)))
                    .build())));
            assertThat(result.get("courtBundleListV2")).isEqualTo(List.of());
        }

        @Test
        void shouldBeAbleToRemoveNcCourtBundleFromMultipleCourtBundlesByAdmin() {
            int loginType = HMCTS_LOGIN_TYPE;
            initialiseUserService(loginType);
            CaseData.CaseDataBuilder builder = CaseData.builder().id(CASE_ID);
            builder.hearingDocuments(HearingDocuments.builder()
                .courtBundleListV2(List.of(element(hearingCourtBundleElementIdOne, HearingCourtBundle.builder()
                    .courtBundle(new ArrayList<>(List.of(
                        element(elementId1, cb1),
                        element(elementId2, cb2)
                    )))
                    .build())))
                .build());
            builder.manageDocumentEventData(ManageDocumentEventData.builder()
                .manageDocumentAction(ManageDocumentAction.REMOVE_DOCUMENTS)
                .manageDocumentRemoveDocReason(ManageDocumentRemovalReason.MISTAKE_ON_DOCUMENT)
                .documentsToBeRemoved(DynamicList.builder()
                    .value(DynamicListElement.builder()
                        .code("hearingDocuments.courtBundleListV2###" + elementId1)
                        .build())
                    .build())
                .build());

            Map<String, Object> result = underTest.removeDocuments(builder.build());
            CourtBundle removedCb = CourtBundle.builder()
                .document(cb1.getDocument())
                .uploaderType(cb1.getUploaderType())
                .uploaderCaseRoles(cb1.getUploaderCaseRoles())
                .build();
            removedCb.setRemovalReason("There is a mistake on the document");
            assertThat(result.get("courtBundleListRemoved")).isEqualTo(List.of(
                element(hearingCourtBundleElementIdOne, HearingCourtBundle.builder()
                    .courtBundle(List.of(element(elementId1, removedCb)))
                    .build())));
            assertThat(result.get("courtBundleListV2")).isEqualTo(List.of(
                element(hearingCourtBundleElementIdOne, HearingCourtBundle.builder()
                    .courtBundle(List.of(element(elementId2, cb2)))
                    .build())));
        }

        @Test
        void shouldBeAbleToRemoveNcCourtBundleFromSingleCourtBundleByAdmin() {
            int loginType = HMCTS_LOGIN_TYPE;
            initialiseUserService(loginType);
            CaseData.CaseDataBuilder builder = CaseData.builder().id(CASE_ID);
            builder.hearingDocuments(HearingDocuments.builder()
                .courtBundleListV2(List.of(element(hearingCourtBundleElementIdOne, HearingCourtBundle.builder()
                    .courtBundle(new ArrayList<>(List.of(
                        element(elementId1, cb1)
                    )))
                    .build())))
                .build());
            builder.manageDocumentEventData(ManageDocumentEventData.builder()
                .manageDocumentAction(ManageDocumentAction.REMOVE_DOCUMENTS)
                .manageDocumentRemoveDocReason(ManageDocumentRemovalReason.UPLOADED_TO_WRONG_CASE)
                .documentsToBeRemoved(DynamicList.builder()
                    .value(DynamicListElement.builder()
                        .code("hearingDocuments.courtBundleListV2###" + elementId1)
                        .build())
                    .build())
                .build());

            Map<String, Object> result = underTest.removeDocuments(builder.build());
            CourtBundle removedCb = CourtBundle.builder()
                .document(cb1.getDocument())
                .uploaderType(cb1.getUploaderType())
                .uploaderCaseRoles(cb1.getUploaderCaseRoles())
                .build();
            removedCb.setRemovalReason("The document was uploaded to the wrong case");
            assertThat(result.get("courtBundleListRemoved")).isEqualTo(List.of(
                element(hearingCourtBundleElementIdOne, HearingCourtBundle.builder()
                    .courtBundle(List.of(element(elementId1, removedCb)))
                    .build())));
            assertThat(result.get("courtBundleListV2")).isEqualTo(List.of());
        }

        @Test
        void shouldBeAbleToRemoveCourtBundleLAFromMultipleCourtBundlesByAdmin() {
            int loginType = HMCTS_LOGIN_TYPE;
            initialiseUserService(loginType);
            CaseData.CaseDataBuilder builder = CaseData.builder().id(CASE_ID);
            builder.hearingDocuments(HearingDocuments.builder()
                .courtBundleListLA(List.of(element(hearingCourtBundleElementIdOne, HearingCourtBundle.builder()
                    .courtBundle(new ArrayList<>(List.of(
                        element(elementId3, cb3),
                        element(elementId2, cb2)
                    )))
                    .build())))
                .build());
            builder.manageDocumentEventData(ManageDocumentEventData.builder()
                .manageDocumentAction(ManageDocumentAction.REMOVE_DOCUMENTS)
                .manageDocumentRemoveDocReason(ManageDocumentRemovalReason.MISTAKE_ON_DOCUMENT)
                .documentsToBeRemoved(DynamicList.builder()
                    .value(DynamicListElement.builder()
                        .code("hearingDocuments.courtBundleListLA###" + elementId3)
                        .build())
                    .build())
                .build());

            Map<String, Object> result = underTest.removeDocuments(builder.build());
            CourtBundle removedCb = CourtBundle.builder()
                .document(cb3.getDocument())
                .uploaderType(cb3.getUploaderType())
                .uploaderCaseRoles(cb3.getUploaderCaseRoles())
                .build();
            removedCb.setRemovalReason("There is a mistake on the document");
            assertThat(result.get("courtBundleListRemoved")).isEqualTo(List.of(
                element(hearingCourtBundleElementIdOne, HearingCourtBundle.builder()
                    .courtBundle(List.of(element(elementId3, removedCb)))
                    .build())));
            assertThat(result.get("courtBundleListLA")).isEqualTo(List.of(
                element(hearingCourtBundleElementIdOne, HearingCourtBundle.builder()
                    .courtBundle(List.of(element(elementId2, cb2)))
                    .build())));
        }

        @Test
        void shouldBeAbleToRemoveCourtBundleLAFromSingleCourtBundleByAdmin() {
            int loginType = HMCTS_LOGIN_TYPE;
            initialiseUserService(loginType);
            CaseData.CaseDataBuilder builder = CaseData.builder().id(CASE_ID);
            builder.hearingDocuments(HearingDocuments.builder()
                .courtBundleListLA(List.of(element(hearingCourtBundleElementIdOne, HearingCourtBundle.builder()
                    .courtBundle(new ArrayList<>(List.of(
                        element(elementId3, cb3)
                    )))
                    .build())))
                .build());
            builder.manageDocumentEventData(ManageDocumentEventData.builder()
                .manageDocumentAction(ManageDocumentAction.REMOVE_DOCUMENTS)
                .manageDocumentRemoveDocReason(ManageDocumentRemovalReason.OTHER)
                .manageDocumentRemoveDocAnotherReason("Another reason is here")
                .documentsToBeRemoved(DynamicList.builder()
                    .value(DynamicListElement.builder()
                        .code("hearingDocuments.courtBundleListLA###" + elementId3)
                        .build())
                    .build())
                .build());

            Map<String, Object> result = underTest.removeDocuments(builder.build());
            CourtBundle removedCb = CourtBundle.builder()
                .document(cb3.getDocument())
                .uploaderType(cb3.getUploaderType())
                .uploaderCaseRoles(cb3.getUploaderCaseRoles())
                .build();
            removedCb.setRemovalReason("Another reason is here");
            assertThat(result.get("courtBundleListRemoved")).isEqualTo(List.of(
                element(hearingCourtBundleElementIdOne, HearingCourtBundle.builder()
                    .courtBundle(List.of(element(elementId3, removedCb)))
                    .build())));
            assertThat(result.get("courtBundleListLA")).isEqualTo(List.of());
        }

        @Test
        void shouldBeAbleToRemoveCourtBundleCTSCFromSingleCourtBundleByAdmin() {
            int loginType = HMCTS_LOGIN_TYPE;
            initialiseUserService(loginType);
            CaseData.CaseDataBuilder builder = CaseData.builder().id(CASE_ID);
            builder.hearingDocuments(HearingDocuments.builder()
                .courtBundleListCTSC(List.of(element(hearingCourtBundleElementIdOne, HearingCourtBundle.builder()
                    .courtBundle(new ArrayList<>(List.of(
                        element(elementId4, cb4)
                    )))
                    .build())))
                .build());
            builder.manageDocumentEventData(ManageDocumentEventData.builder()
                .manageDocumentAction(ManageDocumentAction.REMOVE_DOCUMENTS)
                .manageDocumentRemoveDocReason(ManageDocumentRemovalReason.OTHER)
                .manageDocumentRemoveDocAnotherReason("Another reason is here")
                .documentsToBeRemoved(DynamicList.builder()
                    .value(DynamicListElement.builder()
                        .code("hearingDocuments.courtBundleListCTSC###" + elementId4)
                        .build())
                    .build())
                .build());

            Map<String, Object> result = underTest.removeDocuments(builder.build());
            CourtBundle removedCb = CourtBundle.builder()
                .document(cb4.getDocument())
                .uploaderType(cb4.getUploaderType())
                .uploaderCaseRoles(cb4.getUploaderCaseRoles())
                .build();
            removedCb.setRemovalReason("Another reason is here");
            assertThat(result.get("courtBundleListRemoved")).isEqualTo(List.of(
                element(hearingCourtBundleElementIdOne, HearingCourtBundle.builder()
                    .courtBundle(List.of(element(elementId4, removedCb)))
                    .build())));
            assertThat(result.get("courtBundleListCTSC")).isEqualTo(List.of());
        }

        @Test
        void shouldBeAbleToRemoveNcCourtBundleWithMultipleHearingCourtBundleByAdmin() {
            int loginType = HMCTS_LOGIN_TYPE;
            initialiseUserService(loginType);
            CaseData.CaseDataBuilder builder = CaseData.builder().id(CASE_ID);
            builder.hearingDocuments(HearingDocuments.builder()
                .courtBundleListV2(List.of(
                    element(hearingCourtBundleElementIdOne, HearingCourtBundle.builder()
                        .courtBundle(new ArrayList<>(List.of(element(elementId1, cb1))))
                        .build()),
                    element(hearingCourtBundleElementIdTwo, HearingCourtBundle.builder()
                        .courtBundle(new ArrayList<>(List.of(element(elementId2, cb2))))
                        .build()))
                )
                .build());
            builder.manageDocumentEventData(ManageDocumentEventData.builder()
                .manageDocumentAction(ManageDocumentAction.REMOVE_DOCUMENTS)
                .manageDocumentRemoveDocReason(ManageDocumentRemovalReason.OTHER)
                .manageDocumentRemoveDocAnotherReason("Another reason is here")
                .documentsToBeRemoved(DynamicList.builder()
                    .value(DynamicListElement.builder()
                        .code("hearingDocuments.courtBundleListV2###" + elementId1)
                        .build())
                    .build())
                .build());

            Map<String, Object> result = underTest.removeDocuments(builder.build());
            CourtBundle removedCb = CourtBundle.builder()
                .document(cb1.getDocument())
                .uploaderType(cb1.getUploaderType())
                .uploaderCaseRoles(cb1.getUploaderCaseRoles())
                .build();
            removedCb.setRemovalReason("Another reason is here");
            assertThat(result.get("courtBundleListRemoved")).isEqualTo(List.of(
                element(hearingCourtBundleElementIdOne, HearingCourtBundle.builder()
                    .courtBundle(List.of(element(elementId1, removedCb)))
                    .build())));
            assertThat(result.get("courtBundleListV2")).isEqualTo(List.of(
                element(hearingCourtBundleElementIdTwo, HearingCourtBundle.builder()
                    .courtBundle(new ArrayList<>(List.of(element(elementId2, cb2))))
                    .build())));
        }

        private static Stream<Arguments> buildC2DocumentBundleModifiers() {
            return ManageDocumentServiceTest.buildC2DocumentBundleModifiers();
        }

        private static SupportingEvidenceBundle buildSupportingEvidenceBundle(String filename,
                                                                              DocumentUploaderType uploaderType,
                                                                              List<CaseRole> uploaderCaseRoles) {
            return SupportingEvidenceBundle.builder()
                .document(testDocumentReference(filename))
                .uploaderType(uploaderType)
                .uploaderCaseRoles(uploaderCaseRoles)
                .build();
        }

        private static DocumentUploaderType modifierToDocumentUploaderType(String modifier) {
            if (modifier == null) {
                return null;
            }
            if ("".equals(modifier)) {
                return DocumentUploaderType.HMCTS;
            }
            if ("LA".equals(modifier)) {
                return DocumentUploaderType.DESIGNATED_LOCAL_AUTHORITY;
            }
            if (modifier.startsWith("Resp") || modifier.startsWith("Child")) {
                return DocumentUploaderType.SOLICITOR;
            }
            throw new AssertionError("unsupported modifier: " + modifier);
        }

        private static int extractNumericalPart(String input) {
            // Use regular expression to match digits in the input string
            Pattern pattern = Pattern.compile("\\d+");
            Matcher matcher = pattern.matcher(input);

            // Check if there is a match
            if (matcher.find()) {
                String numericalString = matcher.group();
                return Integer.parseInt(numericalString); // Parse the matched digits to integer
            } else {
                // Handle the case when no numerical part is found
                throw new IllegalArgumentException("No numerical part found in the input string.");
            }
        }

        private static List<CaseRole> modifierToCaseRole(String modifier) {
            if (modifier == null) {
                return null;
            }
            if ("".equals(modifier)) {
                return List.of();
            }
            if ("LA".equals(modifier)) {
                return List.of(CaseRole.LASOLICITOR);
            }
            if (modifier.startsWith("Resp")) {
                return List.of(CaseRole.getByIndex("SOLICITOR", extractNumericalPart(modifier)));
            }
            if (modifier.startsWith("Child")) {
                return List.of(CaseRole.getByIndex("CHILDSOLICITOR", extractNumericalPart(modifier)));
            }
            throw new AssertionError("unsupported modifier: " + modifier);
        }

        @ParameterizedTest
        @MethodSource("buildC2DocumentBundleModifiers")
        void adminShouldBeAbleToRemoveConfidentialC2SupportingDocumentFromAdditionalApplication(String modifier) {
            int loginType = HMCTS_LOGIN_TYPE;
            UUID additionalApplicationUUID = UUID.randomUUID();

            initialiseUserService(loginType);

            SupportingEvidenceBundle seb = buildSupportingEvidenceBundle(filename1,
                modifierToDocumentUploaderType(modifier), modifierToCaseRole(modifier));

            CaseData.CaseDataBuilder builder = CaseData.builder().id(CASE_ID);
            builder.additionalApplicationsBundle(List.of(
                element(additionalApplicationUUID, toConfidentialAdditionalApplicationsBundleBuilder(modifier,
                    C2DocumentBundle.builder()
                        .document(additionalApplicationDocument)
                        .supportingEvidenceBundle(List.of(element(elementId1, seb)))
                        .build()).build())
            ));
            builder.manageDocumentEventData(ManageDocumentEventData.builder()
                .manageDocumentAction(ManageDocumentAction.REMOVE_DOCUMENTS)
                .manageDocumentRemoveDocReason(ManageDocumentRemovalReason.UPLOADED_TO_WRONG_CASE)
                .documentsToBeRemoved(DynamicList.builder()
                    .value(DynamicListElement.builder()
                        .code(C2_APPLICATION_DOCUMENTS.name() + "###" + elementId1)
                        .build())
                    .build())
                .build());

            Map<String, Object> result = underTest.removeDocuments(builder.build());
            assertThat(result.get("c2ApplicationDocListRemoved")).isEqualTo(List.of(
                element(elementId1, ManagedDocument.builder()
                    .document(seb.getDocument())
                    .markAsConfidential(seb.getMarkAsConfidential())
                    .uploaderType(seb.getUploaderType())
                    .uploaderCaseRoles(seb.getUploaderCaseRoles())
                    .build())
            ));
            assertThat(result.get("additionalApplicationsBundle")).isEqualTo(List.of(
                element(additionalApplicationUUID, toConfidentialAdditionalApplicationsBundleBuilder(modifier,
                    C2DocumentBundle.builder()
                        .document(additionalApplicationDocument)
                        .supportingEvidenceBundle(List.of())
                        .build()).build())
            ));
        }

        @ParameterizedTest
        @MethodSource("buildC2DocumentBundleModifiers")
        void adminShouldBeAbleToRemoveC2SupportingDocumentFromAdditionalApplication(String modifier) {
            int loginType = HMCTS_LOGIN_TYPE;
            UUID additionalApplicationUUID = UUID.randomUUID();

            initialiseUserService(loginType);

            SupportingEvidenceBundle seb = buildSupportingEvidenceBundle(filename1,
                modifierToDocumentUploaderType(modifier), modifierToCaseRole(modifier));

            CaseData.CaseDataBuilder builder = CaseData.builder().id(CASE_ID);
            builder.additionalApplicationsBundle(List.of(
                element(additionalApplicationUUID, AdditionalApplicationsBundle.builder()
                    .c2DocumentBundle(C2DocumentBundle.builder()
                        .document(additionalApplicationDocument)
                        .supportingEvidenceBundle(List.of(element(elementId1, seb)))
                        .build()).build())
            ));
            builder.manageDocumentEventData(ManageDocumentEventData.builder()
                .manageDocumentAction(ManageDocumentAction.REMOVE_DOCUMENTS)
                .manageDocumentRemoveDocReason(ManageDocumentRemovalReason.UPLOADED_TO_WRONG_CASE)
                .documentsToBeRemoved(DynamicList.builder()
                    .value(DynamicListElement.builder()
                        .code(C2_APPLICATION_DOCUMENTS.name() + "###" + elementId1)
                        .build())
                    .build())
                .build());

            Map<String, Object> result = underTest.removeDocuments(builder.build());
            assertThat(result.get("c2ApplicationDocListRemoved")).isEqualTo(List.of(
                element(elementId1, ManagedDocument.builder()
                    .document(seb.getDocument())
                    .markAsConfidential(seb.getMarkAsConfidential())
                    .uploaderType(seb.getUploaderType())
                    .uploaderCaseRoles(seb.getUploaderCaseRoles())
                    .build())
            ));
            assertThat(result.get("additionalApplicationsBundle")).isEqualTo(List.of(
                element(additionalApplicationUUID, AdditionalApplicationsBundle.builder()
                    .c2DocumentBundle(
                        C2DocumentBundle.builder()
                            .document(additionalApplicationDocument)
                            .supportingEvidenceBundle(List.of())
                            .build()).build())
            ));
        }

        @ParameterizedTest
        @MethodSource("buildC2DocumentBundleModifiers")
        void adminShouldBeAbleToRemoveC1SupportingDocumentFromAdditionalApplication(String modifier) {
            int loginType = HMCTS_LOGIN_TYPE;
            UUID additionalApplicationUUID = UUID.randomUUID();

            initialiseUserService(loginType);

            SupportingEvidenceBundle seb = buildSupportingEvidenceBundle(filename1,
                modifierToDocumentUploaderType(modifier), modifierToCaseRole(modifier));

            CaseData.CaseDataBuilder builder = CaseData.builder().id(CASE_ID);
            builder.additionalApplicationsBundle(List.of(
                element(additionalApplicationUUID, AdditionalApplicationsBundle.builder()
                    .otherApplicationsBundle(OtherApplicationsBundle.builder()
                        .document(additionalApplicationDocument)
                        .supportingEvidenceBundle(List.of(element(elementId1, seb)))
                        .build()).build())
            ));
            builder.manageDocumentEventData(ManageDocumentEventData.builder()
                .manageDocumentAction(ManageDocumentAction.REMOVE_DOCUMENTS)
                .manageDocumentRemoveDocReason(ManageDocumentRemovalReason.UPLOADED_TO_WRONG_CASE)
                .documentsToBeRemoved(DynamicList.builder()
                    .value(DynamicListElement.builder()
                        .code(C1_APPLICATION_DOCUMENTS.name() + "###" + elementId1)
                        .build())
                    .build())
                .build());

            Map<String, Object> result = underTest.removeDocuments(builder.build());
            assertThat(result.get("c1ApplicationDocListRemoved")).isEqualTo(List.of(
                element(elementId1, ManagedDocument.builder()
                    .document(seb.getDocument())
                    .markAsConfidential(seb.getMarkAsConfidential())
                    .uploaderType(seb.getUploaderType())
                    .uploaderCaseRoles(seb.getUploaderCaseRoles())
                    .build())
            ));
            assertThat(result.get("additionalApplicationsBundle")).isEqualTo(List.of(
                element(additionalApplicationUUID, AdditionalApplicationsBundle.builder()
                    .otherApplicationsBundle(OtherApplicationsBundle.builder()
                        .document(additionalApplicationDocument)
                        .supportingEvidenceBundle(List.of())
                        .build()).build())
            ));
        }

        @Test
        void adminShouldBeAbleToRemoveSupportingDocumentFromC1WithSupplement() {
            int loginType = HMCTS_LOGIN_TYPE;
            initialiseUserService(loginType);

            SupportingEvidenceBundle seb = buildSupportingEvidenceBundle(filename1,
                DocumentUploaderType.DESIGNATED_LOCAL_AUTHORITY, modifierToCaseRole("LA"));

            CaseData.CaseDataBuilder builder = CaseData.builder().id(CASE_ID);
            builder.submittedC1WithSupplement(SubmittedC1WithSupplementBundle.builder()
                .supportingEvidenceBundle(List.of(element(elementId1, seb)))
                .build());
            builder.manageDocumentEventData(ManageDocumentEventData.builder()
                .manageDocumentAction(ManageDocumentAction.REMOVE_DOCUMENTS)
                .manageDocumentRemoveDocReason(ManageDocumentRemovalReason.UPLOADED_TO_WRONG_CASE)
                .documentsToBeRemoved(DynamicList.builder()
                    .value(DynamicListElement.builder()
                        .code(C1_APPLICATION_DOCUMENTS.name() + "###" + elementId1)
                        .build())
                    .build())
                .build());

            Map<String, Object> result = underTest.removeDocuments(builder.build());
            assertThat(result.get("c1ApplicationDocListRemoved")).isEqualTo(List.of(
                element(elementId1, ManagedDocument.builder()
                    .document(seb.getDocument())
                    .markAsConfidential(seb.getMarkAsConfidential())
                    .uploaderType(seb.getUploaderType())
                    .uploaderCaseRoles(seb.getUploaderCaseRoles())
                    .build())
            ));
            assertThat(result.get("submittedC1WithSupplement")).isEqualTo(
                SubmittedC1WithSupplementBundle.builder()
                    .supportingEvidenceBundle(List.of())
                    .build());
        }
    }

    @Nested
    class BuildManageDocumentsUploadedEventTest {
        @ParameterizedTest
        @MethodSource("provideTestData")
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

        private static Stream<Arguments> provideTestData() {
            return ManageDocumentsUploadedEventTestData.allUploadableDocumentsTypeParameters();
        }
    }

    private static AdditionalApplicationsBundle.AdditionalApplicationsBundleBuilder
        toConfidentialAdditionalApplicationsBundleBuilder(String modifier, C2DocumentBundle c2DocumentBundle) {
        AdditionalApplicationsBundle.AdditionalApplicationsBundleBuilder ret = AdditionalApplicationsBundle
            .builder();
        try {
            Method method0 = AdditionalApplicationsBundle.AdditionalApplicationsBundleBuilder.class
                .getMethod("c2DocumentBundleConfidential", C2DocumentBundle.class);
            ret = (AdditionalApplicationsBundle.AdditionalApplicationsBundleBuilder) method0
                .invoke(ret, c2DocumentBundle);

            if (isNotEmpty(modifier)) {
                Method method1 = AdditionalApplicationsBundle.AdditionalApplicationsBundleBuilder.class
                    .getMethod("c2DocumentBundle" + modifier, C2DocumentBundle.class);
                ret = (AdditionalApplicationsBundle.AdditionalApplicationsBundleBuilder) method1
                    .invoke(ret, c2DocumentBundle);
            }
            return ret;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Stream<Arguments> buildC2DocumentBundleModifiers() {
        List<Arguments> args = new ArrayList<>();
        args.add(Arguments.of((String) null)); // for legacy without updateCaseRoles/uploaderType
        args.add(Arguments.of(""));
        args.add(Arguments.of("LA"));
        for (int i = 0; i <= 9; i++) {
            args.add(Arguments.of("Resp" + i));
        }
        for (int i = 0; i <= 14; i++) {
            args.add(Arguments.of("Child" + i));
        }
        return args.stream();
    }
}
