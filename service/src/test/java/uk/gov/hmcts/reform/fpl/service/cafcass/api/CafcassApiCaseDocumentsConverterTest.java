package uk.gov.hmcts.reform.fpl.service.cafcass.api;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mockito;
import org.springframework.test.annotation.DirtiesContext;
import uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.ConfidentialGeneratedOrders;
import uk.gov.hmcts.reform.fpl.model.ManagedDocument;
import uk.gov.hmcts.reform.fpl.model.Placement;
import uk.gov.hmcts.reform.fpl.model.PlacementConfidentialDocument;
import uk.gov.hmcts.reform.fpl.model.PlacementNoticeDocument;
import uk.gov.hmcts.reform.fpl.model.PlacementSupportingDocument;
import uk.gov.hmcts.reform.fpl.model.StandardDirectionOrder;
import uk.gov.hmcts.reform.fpl.model.Supplement;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.cafcass.api.CafcassApiCaseData;
import uk.gov.hmcts.reform.fpl.model.cafcass.api.CafcassApiCaseDocument;
import uk.gov.hmcts.reform.fpl.model.common.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.OtherApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.SubmittedC1WithSupplementBundle;
import uk.gov.hmcts.reform.fpl.model.event.PlacementEventData;
import uk.gov.hmcts.reform.fpl.model.group.C110A;
import uk.gov.hmcts.reform.fpl.model.order.DraftOrder;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrdersBundle;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.cfv.ConfidentialLevel.CTSC;
import static uk.gov.hmcts.reform.fpl.enums.cfv.ConfidentialLevel.LA;
import static uk.gov.hmcts.reform.fpl.enums.cfv.ConfidentialLevel.NON_CONFIDENTIAL;
import static uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType.AA_PARENT_ORDERS;
import static uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType.C1_APPLICATION_DOCUMENTS;
import static uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType.C2_APPLICATION_DOCUMENTS;
import static uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType.PLACEMENT_RESPONSES;
import static uk.gov.hmcts.reform.fpl.utils.DocumentsHelper.getDocumentIdFromUrl;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

public class CafcassApiCaseDocumentsConverterTest extends CafcassApiConverterTestBase {
    private static final ManageDocumentService manageDocumentService = mock(ManageDocumentService.class);

    CafcassApiCaseDocumentsConverterTest() {
        super(new CafcassApiCaseDocumentsConverter(manageDocumentService));
    }

    private List<CafcassApiCaseDocument> getExpectedCafcassApiCaseDocuments(String category, boolean removed,
                                                                            List<DocumentReference> docRefs) {
        return docRefs.stream().map(docRef -> CafcassApiCaseDocument.builder()
                .documentId(getDocumentIdFromUrl(docRef.getUrl()).toString())
                .documentFileName(docRef.getFilename())
                .documentCategory(category)
                .removed(removed)
                .uploadTimestamp(docRef.getUploadedTimestamp())
                .build())
            .toList();
    }

    private void testCaseDocument(CaseData caseData, List<DocumentReference> documentReferences,
                                  DocumentType documentType) {
        testCaseDocument(caseData, documentReferences, documentType.getCafcassApiDocumentCategory());
    }

    private void testCaseDocument(CaseData caseData, List<DocumentReference> documentReferences, String category) {
        CafcassApiCaseData actual = testConvert(caseData);
        assertThat(actual.getCaseDocuments())
            .containsOnlyOnceElementsOf(getExpectedCafcassApiCaseDocuments(category, false, documentReferences));

    }

    @Test
    void shouldReturnSource() {
        testSource(List.of("data.standardDirectionOrder", "data.urgentDirectionsOrder",
            "data.hearingOrdersBundlesDrafts", "data.hearingOrdersBundlesDraftReview",
            "data.sealedCMOs", "data.orderCollection", "data.orderCollectionChild0", "data.orderCollectionChild1",
            "data.orderCollectionChild2", "data.orderCollectionChild3", "data.orderCollectionChild4",
            "data.orderCollectionChild5", "data.orderCollectionChild6", "data.orderCollectionChild7",
            "data.orderCollectionChild8", "data.orderCollectionChild9", "data.orderCollectionChild10",
            "data.orderCollectionChild11", "data.orderCollectionChild12", "data.orderCollectionChild13",
            "data.orderCollectionChild14",
            "data.submittedC1WithSupplement", "data.submittedForm", "data.translatedSubmittedForm",
            "data.supplementDocument", "data.placements", "data.additionalApplicationsBundle", "data.hearingDetails",
            "data.courtBundleListV2", "data.courtBundleListLA",
            "data.caseSummaryList", "data.caseSummaryListLA", "data.posStmtChildList", "data.posStmtChildListLA",
            "data.posStmtRespList", "data.posStmtRespListLA", "data.posStmtList", "data.posStmtListLA",
            "data.skeletonArgumentList", "data.skeletonArgumentListLA",
            "data.thresholdList", "data.thresholdListLA", "data.judgementList", "data.judgementListLA",
            "data.transcriptList", "data.transcriptListLA",
            "data.documentsFiledOnIssueList", "data.documentsFiledOnIssueListLA",
            "data.applicantWitnessStmtList", "data.applicantWitnessStmtListLA",
            "data.carePlanList", "data.carePlanListLA", "data.parentAssessmentList", "data.parentAssessmentListLA",
            "data.famAndViabilityList", "data.famAndViabilityListLA",
            "data.applicantOtherDocList", "data.applicantOtherDocListLA",
            "data.meetingNoteList", "data.meetingNoteListLA", "data.contactNoteList", "data.contactNoteListLA",
            "data.c1ApplicationDocList", "data.c1ApplicationDocListLA",
            "data.c2ApplicationDocList", "data.c2ApplicationDocListLA",
            "data.respStmtList", "data.respStmtListLA", "data.respWitnessStmtList", "data.respWitnessStmtListLA",
            "data.guardianEvidenceList", "data.guardianEvidenceListLA",
            "data.guardianReportsList", "data.guardianReportsListLA",
            "data.adultPsychRepParentsList", "data.adultPsychRepParentsListLA",
            "data.famCentreAssessNonResList", "data.famCentreAssessNonResListLA",
            "data.familyCentreAssesResList", "data.familyCentreAssesResListLA",
            "data.haematologistList", "data.haematologistListLA",
            "data.indepSocialWorkerList", "data.indepSocialWorkerListLA",
            "data.multiDisciplinAssessList", "data.multiDisciplinAssessListLA",
            "data.neuroSurgeonList", "data.neuroSurgeonListLA",
            "data.ophthalmologistList", "data.ophthalmologistListLA",
            "data.otherExpertReportList", "data.otherExpertReportListLA",
            "data.otherMedicalReportList", "data.otherMedicalReportListLA",
            "data.pediatricList", "data.pediatricListLA",
            "data.pediatricRadiologistList", "data.pediatricRadiologistListLA",
            "data.profDNATestingList", "data.profDNATestingListLA",
            "data.profDrugAlcoholList", "data.profDrugAlcoholListLA",
            "data.professionalHairStrandList", "data.professionalHairStrandListLA",
            "data.professionalOtherList", "data.professionalOtherListLA",
            "data.psychiatricChildOnlyList", "data.psychiatricChildOnlyListLA",
            "data.psychChildParentCarersList", "data.psychChildParentCarersListLA",
            "data.psycReportChildClinList", "data.psycReportChildClinListLA",
            "data.psycReportChildOnlyEdList", "data.psycReportChildOnlyEdListLA",
            "data.psychReportParentChildList", "data.psychReportParentChildListLA",
            "data.psychRepParentFullCogList", "data.psychRepParentFullCogListLA",
            "data.psychRepParentFuncList", "data.psychRepParentFuncListLA",
            "data.toxicologyStatementList", "data.toxicologyStatementListLA",
            "data.expertReportList", "data.expertReportListLA",
            "data.drugAndAlcoholReportList", "data.drugAndAlcoholReportListLA",
            "data.lettersOfInstructionList", "data.lettersOfInstructionListLA",
            "data.policeDisclosureList", "data.policeDisclosureListLA",
            "data.medicalRecordList", "data.medicalRecordListLA",
            "data.correspondenceDocList", "data.correspondenceDocListLA",
            "data.noticeOfActingOrIssueList", "data.noticeOfActingOrIssueListLA",
            "data.previousProceedingList", "data.previousProceedingListLA",
            "data.archivedDocumentsList", "data.archivedDocumentsListLA"
        ));
    }

    @Test
    void shouldReturnDistinctDocument() {
        DocumentReference c2 = getTestDocumentReference();
        DocumentReference supplementDoc = getTestDocumentReference();

        AdditionalApplicationsBundle additionalApplicationsBundle = AdditionalApplicationsBundle.builder()
            .c2DocumentBundle(C2DocumentBundle.builder()
                .document(c2)
                .supplementsBundle(wrapElements(
                    Supplement.builder().document(supplementDoc).build(),
                    Supplement.builder().document(supplementDoc).build()))
                .supportingEvidenceBundle(wrapElements(
                    SupportingEvidenceBundle.builder().document(supplementDoc).build()))
                .build())
            .build();


        CaseData caseData = CaseData.builder()
            .additionalApplicationsBundle(wrapElements(additionalApplicationsBundle))
            .build();

        testCaseDocument(caseData, List.of(c2, supplementDoc), C2_APPLICATION_DOCUMENTS);
    }

    @Nested
    class StandardAndUrgentDirectionOrder {
        @Test
        void shouldConvertStandardAndUrgentDirectionOrder() {
            DocumentReference sdoOrder = getTestDocumentReference();
            DocumentReference sdoTranslatedOrder = getTestDocumentReference();
            DocumentReference udoOrder = getTestDocumentReference();
            DocumentReference udoTranslatedOrder = getTestDocumentReference();

            CaseData caseData = CaseData.builder()
                .standardDirectionOrder(StandardDirectionOrder.builder()
                    .orderDoc(sdoOrder)
                    .translatedOrderDoc(sdoTranslatedOrder)
                    .build())
                .urgentDirectionsOrder(StandardDirectionOrder.builder()
                    .orderDoc(udoOrder)
                    .translatedOrderDoc(udoTranslatedOrder)
                    .build())
                .build();

            testCaseDocument(caseData, List.of(sdoOrder, sdoTranslatedOrder, udoOrder, udoTranslatedOrder),
                AA_PARENT_ORDERS);
        }

        @Test
        void shouldReturnEmptyListIfDocumentNotExist() {
            testCaseDocument(
                CaseData.builder()
                    .standardDirectionOrder(StandardDirectionOrder.builder().build())
                    .urgentDirectionsOrder(StandardDirectionOrder.builder().build()).build(),
                List.of(), "draftOrders");
        }

        @Test
        void shouldReturnEmptyListIfNull() {
            testCaseDocument(
                CaseData.builder().standardDirectionOrder(null).urgentDirectionsOrder(null).build(),
                List.of(), "draftOrders");
        }
    }

    @Nested
    class DraftOrders {
        private static final DocumentReference DRAFT_ORDER = getTestDocumentReference();
        private static final DocumentReference DRAFT_ORDER_CONFIDENTIAL = getTestDocumentReference();

        @Test
        void shouldConvertDraftOrdersAndConfidentialOrdersUploadedByChildSolicitor() {
            List<Element<HearingOrdersBundle>> hearingOrdersBundlesDrafts = wrapElements(
                HearingOrdersBundle.builder()
                    .orders(wrapElements(HearingOrder.builder().order(DRAFT_ORDER).build()))
                    .build(),
                HearingOrdersBundle.builder()
                    .ordersChild0(wrapElements(HearingOrder.builder()
                        .orderConfidential(DRAFT_ORDER_CONFIDENTIAL).build()))
                    .build());

            CaseData caseData = CaseData.builder()
                .hearingOrdersBundlesDrafts(hearingOrdersBundlesDrafts)
                .build();

            testCaseDocument(caseData, List.of(DRAFT_ORDER, DRAFT_ORDER_CONFIDENTIAL), "draftOrders");
        }

        @Test
        void shouldConvertDraftOrdersReviewAndConfidentialOrdersUploadedByChildSolicitor() {
            List<Element<HearingOrdersBundle>> hearingOrdersBundlesDraftReview = wrapElements(
                HearingOrdersBundle.builder()
                    .orders(wrapElements(HearingOrder.builder().order(DRAFT_ORDER).build()))
                    .build(),
                HearingOrdersBundle.builder()
                    .ordersChild0(wrapElements(HearingOrder.builder()
                        .orderConfidential(DRAFT_ORDER_CONFIDENTIAL).build()))
                    .build());
            CaseData caseData = CaseData.builder()
                .hearingOrdersBundlesDraftReview(hearingOrdersBundlesDraftReview)
                .build();
            testCaseDocument(caseData, List.of(DRAFT_ORDER, DRAFT_ORDER_CONFIDENTIAL), "draftOrders");
        }

        @Test
        void shouldNotConvertConfidentialOrdersNotUploadedByChildSolicitor() {
            List<Element<HearingOrdersBundle>> hearingOrdersBundlesDrafts = wrapElements(
                HearingOrdersBundle.builder()
                    .ordersCTSC(wrapElements(HearingOrder.builder()
                        .orderConfidential(DRAFT_ORDER_CONFIDENTIAL).build()))
                    .build(),
                HearingOrdersBundle.builder()
                    .ordersLA(wrapElements(HearingOrder.builder()
                        .orderConfidential(DRAFT_ORDER_CONFIDENTIAL).build()))
                    .build());

            List<Element<HearingOrdersBundle>> hearingOrdersBundlesDraftReview = wrapElements(
                HearingOrdersBundle.builder()
                    .ordersResp0(wrapElements(HearingOrder.builder()
                        .orderConfidential(DRAFT_ORDER_CONFIDENTIAL).build()))
                    .build());
            CaseData caseData = CaseData.builder()
                .hearingOrdersBundlesDrafts(hearingOrdersBundlesDrafts)
                .hearingOrdersBundlesDraftReview(hearingOrdersBundlesDraftReview)
                .build();

            testCaseDocument(caseData, List.of(), "draftOrders");
        }

        @Test
        void shouldReturnEmptyListIfNullOrEmpty() {
            testCaseDocument(
                CaseData.builder().hearingOrdersBundlesDrafts(null).hearingOrdersBundlesDraftReview(null).build(),
                List.of(), "draftOrders");
            testCaseDocument(
                CaseData.builder()
                    .hearingOrdersBundlesDrafts(List.of())
                    .hearingOrdersBundlesDraftReview(List.of()).build(),
                List.of(), "draftOrders");
        }
    }

    @Nested
    class ApprovedOrder {
        private static final DocumentReference SEALED_CMO = getTestDocumentReference();
        private static final DocumentReference SEALED_CMO_CONFIDENTIAL = getTestDocumentReference();
        private static final DocumentReference APPROVED_ORDER = getTestDocumentReference();
        private static final DocumentReference APPROVED_ORDER_CONFIDENTIAL = getTestDocumentReference();

        @Test
        void shouldConvertSealedCmosAndOrders() {
            CaseData caseData = CaseData.builder()
                .sealedCMOs(wrapElements(
                    HearingOrder.builder()
                        .order(SEALED_CMO)
                        .build(),
                    HearingOrder.builder()
                        .orderConfidential(SEALED_CMO_CONFIDENTIAL)
                        .build()
                ))
                .orderCollection(wrapElements(GeneratedOrder.builder().document(APPROVED_ORDER).build()))
                .confidentialOrders(ConfidentialGeneratedOrders.builder()
                    .orderCollectionChild0(wrapElements(
                        GeneratedOrder.builder().document(APPROVED_ORDER_CONFIDENTIAL).build()))
                    .build())
                .build();

            testCaseDocument(caseData,
                List.of(SEALED_CMO, SEALED_CMO_CONFIDENTIAL, APPROVED_ORDER, APPROVED_ORDER_CONFIDENTIAL),
                AA_PARENT_ORDERS);
        }

        @Test
        void shouldNotConvertConfidentialOrderNotUploadedByChildSolicitor() {
            CaseData caseData = CaseData.builder()
                .confidentialOrders(ConfidentialGeneratedOrders.builder()
                    .orderCollectionCTSC(wrapElements(
                        GeneratedOrder.builder().document(APPROVED_ORDER_CONFIDENTIAL).build()))
                    .orderCollectionLA(wrapElements(
                        GeneratedOrder.builder().document(APPROVED_ORDER_CONFIDENTIAL).build()))
                    .orderCollectionResp0(wrapElements(
                        GeneratedOrder.builder().document(APPROVED_ORDER_CONFIDENTIAL).build()))
                    .build())
                .build();
            testCaseDocument(caseData, List.of(), AA_PARENT_ORDERS);
        }

        @Test
        void shouldReturnEmptyListIfNullOrEmpty() {
            testCaseDocument(CaseData.builder().build(), List.of(), AA_PARENT_ORDERS);

            testCaseDocument(
                CaseData.builder()
                    .sealedCMOs(List.of()).orderCollection(List.of())
                    .confidentialOrders(ConfidentialGeneratedOrders.builder().build())
                    .build(),
                List.of(), AA_PARENT_ORDERS);

            testCaseDocument(
                CaseData.builder()
                    .confidentialOrders(ConfidentialGeneratedOrders.builder()
                        .orderCollectionResp0(List.of())
                        .orderCollectionChild0(List.of())
                        .build())
                    .build(),
                List.of(), AA_PARENT_ORDERS);
        }
    }

    @Nested
    class OriginalApplications {
        private static final DocumentReference APPLICATION_DOC = getTestDocumentReference();
        private static final DocumentReference APPLICATION_TRANSLATED_DOC = getTestDocumentReference();
        private static final DocumentReference SUPPLEMENT_DOC = getTestDocumentReference();
        private static final Supplement SUPPLEMENT = Supplement.builder().document(SUPPLEMENT_DOC).build();
        private static final DocumentReference SUPPORTING_EVIDENCE_DOC = getTestDocumentReference();
        private static final DocumentReference SUPPORTING_EVIDENCE_TRANSLATED_DOC = getTestDocumentReference();
        private static final SupportingEvidenceBundle SUPPORTING_EVIDENCE_BUNDLE = SupportingEvidenceBundle.builder()
            .document(SUPPORTING_EVIDENCE_DOC)
            .translatedDocument(SUPPORTING_EVIDENCE_TRANSLATED_DOC)
            .build();

        @Test
        void shouldConvertC1OriginalApplicationDocument() {
            CaseData caseData = CaseData.builder()
                .submittedC1WithSupplement(SubmittedC1WithSupplementBundle.builder()
                    .document(APPLICATION_DOC)
                    .supplementsBundle(wrapElements(SUPPLEMENT))
                    .supportingEvidenceBundle(wrapElements(SUPPORTING_EVIDENCE_BUNDLE))
                    .build())
                .build();

            testCaseDocument(
                caseData,
                List.of(APPLICATION_DOC, SUPPLEMENT_DOC, SUPPORTING_EVIDENCE_DOC, SUPPORTING_EVIDENCE_TRANSLATED_DOC),
                "originalApplications");
        }

        @Test
        void shouldConvertC110AOriginalApplicationDocument() {
            CaseData caseData = CaseData.builder()
                .c110A(C110A.builder()
                    .submittedForm(APPLICATION_DOC)
                    .translatedSubmittedForm(APPLICATION_TRANSLATED_DOC)
                    .supplementDocument(SUPPLEMENT_DOC)
                    .build())
                .build();

            testCaseDocument(
                caseData,
                List.of(APPLICATION_DOC, APPLICATION_TRANSLATED_DOC, SUPPLEMENT_DOC),
                "originalApplications");
        }

        @Test
        void shouldReturnEmptyListIfNullOrEmpty() {
            testCaseDocument(
                CaseData.builder().c110A(null).submittedC1WithSupplement(null).build(),
                List.of(),
                "originalApplications");

            testCaseDocument(
                CaseData.builder().c110A(
                    C110A.builder().build())
                    .submittedC1WithSupplement(SubmittedC1WithSupplementBundle.builder().build()).build(),
                List.of(),
                "originalApplications");
        }
    }

    @Nested
    class PlacementApplications {
        private static final DocumentReference PLACEMENT_APPLICATION_1 = getTestDocumentReference();
        private static final DocumentReference PLACEMENT_APPLICATION_2 = getTestDocumentReference();
        private static final DocumentReference PLACEMENT_NOTICE_DOCUMENT_1 = getTestDocumentReference();
        private static final DocumentReference PLACEMENT_NOTICE_DOCUMENT_2 = getTestDocumentReference();
        private static final DocumentReference CONFIDENTIAL_DOCUMENT_1 = getTestDocumentReference();
        private static final DocumentReference CONFIDENTIAL_DOCUMENT_2 = getTestDocumentReference();
        private static final DocumentReference CONFIDENTIAL_DOCUMENT_3 = getTestDocumentReference();
        private static final DocumentReference CONFIDENTIAL_DOCUMENT_4 = getTestDocumentReference();
        private static final DocumentReference SUPPORTING_DOCUMENT_1 = getTestDocumentReference();
        private static final DocumentReference SUPPORTING_DOCUMENT_2 = getTestDocumentReference();
        private static final DocumentReference SUPPORTING_DOCUMENT_3 = getTestDocumentReference();
        private static final DocumentReference SUPPORTING_DOCUMENT_4 = getTestDocumentReference();
        private static final DocumentReference NOTICE_DOCUMENT_1 = getTestDocumentReference();
        private static final DocumentReference NOTICE_DOCUMENT_2 = getTestDocumentReference();
        private static final DocumentReference NOTICE_DOCUMENT_3 = getTestDocumentReference();
        private static final DocumentReference NOTICE_DOCUMENT_4 = getTestDocumentReference();

        @Test
        void shouldConvertAllPlacementApplications() {
            final Placement placement1 = Placement.builder()
                .application(PLACEMENT_APPLICATION_1)
                .placementNotice(PLACEMENT_NOTICE_DOCUMENT_1)
                .confidentialDocuments(wrapElements(
                    PlacementConfidentialDocument.builder().document(CONFIDENTIAL_DOCUMENT_1).build(),
                    PlacementConfidentialDocument.builder().document(CONFIDENTIAL_DOCUMENT_3).build()
                ))
                .supportingDocuments(wrapElements(
                    PlacementSupportingDocument.builder().document(SUPPORTING_DOCUMENT_1).build(),
                    PlacementSupportingDocument.builder().document(SUPPORTING_DOCUMENT_3).build()
                ))
                .noticeDocuments(wrapElements(
                    PlacementNoticeDocument.builder().response(NOTICE_DOCUMENT_1).build(),
                    PlacementNoticeDocument.builder().response(NOTICE_DOCUMENT_3).build()
                ))
                .build();


            final Placement placement2 = Placement.builder()
                .application(PLACEMENT_APPLICATION_2)
                .placementNotice(PLACEMENT_NOTICE_DOCUMENT_2)
                .confidentialDocuments(wrapElements(
                    PlacementConfidentialDocument.builder().document(CONFIDENTIAL_DOCUMENT_2).build(),
                    PlacementConfidentialDocument.builder().document(CONFIDENTIAL_DOCUMENT_4).build()
                ))
                .supportingDocuments(wrapElements(
                    PlacementSupportingDocument.builder().document(SUPPORTING_DOCUMENT_2).build(),
                    PlacementSupportingDocument.builder().document(SUPPORTING_DOCUMENT_4).build()
                ))
                .noticeDocuments(wrapElements(
                    PlacementNoticeDocument.builder().response(NOTICE_DOCUMENT_2).build(),
                    PlacementNoticeDocument.builder().response(NOTICE_DOCUMENT_4).build()
                ))
                .build();

            CaseData caseData = CaseData.builder()
                .placementEventData(PlacementEventData.builder()
                    .placements(wrapElements(placement1, placement2))
                    .build())
                .build();

            testCaseDocument(caseData, List.of(PLACEMENT_APPLICATION_1, PLACEMENT_APPLICATION_2,
                PLACEMENT_NOTICE_DOCUMENT_1, PLACEMENT_NOTICE_DOCUMENT_2,
                CONFIDENTIAL_DOCUMENT_1, CONFIDENTIAL_DOCUMENT_3, CONFIDENTIAL_DOCUMENT_2, CONFIDENTIAL_DOCUMENT_4,
                SUPPORTING_DOCUMENT_1, SUPPORTING_DOCUMENT_3, SUPPORTING_DOCUMENT_2, SUPPORTING_DOCUMENT_4,
                NOTICE_DOCUMENT_1, NOTICE_DOCUMENT_3, NOTICE_DOCUMENT_2, NOTICE_DOCUMENT_4), PLACEMENT_RESPONSES);
        }

        @Test
        void shouldConvertPlacementApplicationIfOnlyApplicationDocumentExist() {
            final Placement placement = Placement.builder().application(PLACEMENT_APPLICATION_1).build();

            CaseData caseData = CaseData.builder()
                .placementEventData(PlacementEventData.builder()
                    .placements(wrapElements(placement))
                    .build())
                .build();

            testCaseDocument(caseData, List.of(PLACEMENT_APPLICATION_1), PLACEMENT_RESPONSES);
        }

        @Test
        void shouldReturnEmptyListIfNoPlacementExist() {
            testCaseDocument(CaseData.builder().build(), List.of(), PLACEMENT_RESPONSES);
            testCaseDocument(CaseData.builder()
                .placementEventData(PlacementEventData.builder()
                    .placements(wrapElements(Placement.builder().build()))
                    .build()).build(), List.of(), PLACEMENT_RESPONSES);
        }
    }

    @Nested
    class AdditionalApplications {
        private static final DocumentReference C2 = getTestDocumentReference();
        private static final DocumentReference C2_2 = getTestDocumentReference();
        private static final DocumentReference OTHER_APPLICATION = getTestDocumentReference();
        private static final DocumentReference OTHER_APPLICATION_2 = getTestDocumentReference();
        private static final DocumentReference SUPPLEMENT_1 = getTestDocumentReference();
        private static final DocumentReference SUPPLEMENT_2 = getTestDocumentReference();
        private static final DocumentReference SUPPLEMENT_3 = getTestDocumentReference();
        private static final DocumentReference SUPPLEMENT_4 = getTestDocumentReference();
        private static final DocumentReference SUPPORTING_EVIDENCE_1 = getTestDocumentReference();
        private static final DocumentReference SUPPORTING_EVIDENCE_2 = getTestDocumentReference();
        private static final DocumentReference SUPPORTING_EVIDENCE_3 = getTestDocumentReference();
        private static final DocumentReference SUPPORTING_EVIDENCE_4 = getTestDocumentReference();
        private static final DocumentReference DRAFT_ORDER_1 = getTestDocumentReference();
        private static final DocumentReference DRAFT_ORDER_2 = getTestDocumentReference();

        @Test
        void shouldConvertBothC2AndOtherAdditionalApplications() {
            AdditionalApplicationsBundle additionalApplicationsBundle = AdditionalApplicationsBundle.builder()
                .c2DocumentBundle(C2DocumentBundle.builder()
                    .document(C2)
                    .supplementsBundle(wrapElements(
                        Supplement.builder().document(SUPPLEMENT_1).build(),
                        Supplement.builder().document(SUPPLEMENT_2).build()
                        ))
                    .supportingEvidenceBundle(wrapElements(
                        SupportingEvidenceBundle.builder().document(SUPPORTING_EVIDENCE_1).build(),
                        SupportingEvidenceBundle.builder().document(SUPPORTING_EVIDENCE_2).build()))
                    .draftOrdersBundle(wrapElements(
                        DraftOrder.builder().document(DRAFT_ORDER_1).build(),
                        DraftOrder.builder().document(DRAFT_ORDER_2).build()))
                    .build())
                .otherApplicationsBundle(OtherApplicationsBundle.builder()
                    .document(OTHER_APPLICATION)
                    .supplementsBundle(wrapElements(
                        Supplement.builder().document(SUPPLEMENT_3).build(),
                        Supplement.builder().document(SUPPLEMENT_4).build()))
                    .supportingEvidenceBundle(wrapElements(
                        SupportingEvidenceBundle.builder().document(SUPPORTING_EVIDENCE_3).build(),
                        SupportingEvidenceBundle.builder().document(SUPPORTING_EVIDENCE_4).build()))
                    .build())
                .build();

            CaseData caseData = CaseData.builder()
                .additionalApplicationsBundle(wrapElements(additionalApplicationsBundle)).build();

            testAdditionalApplications(caseData,
                List.of(C2, SUPPLEMENT_1, SUPPLEMENT_2, SUPPORTING_EVIDENCE_1, SUPPORTING_EVIDENCE_2),
                List.of(
                    OTHER_APPLICATION, SUPPLEMENT_3, SUPPLEMENT_4, SUPPORTING_EVIDENCE_3, SUPPORTING_EVIDENCE_4));
        }

        @Test
        void shouldConvertAllAdditionalApplications() {
            AdditionalApplicationsBundle additionalApplicationsBundle1 = AdditionalApplicationsBundle.builder()
                .c2DocumentBundle(C2DocumentBundle.builder().document(C2).build())
                .otherApplicationsBundle(OtherApplicationsBundle.builder().document(OTHER_APPLICATION).build())
                .build();
            AdditionalApplicationsBundle additionalApplicationsBundle2 = AdditionalApplicationsBundle.builder()
                .c2DocumentBundle(C2DocumentBundle.builder().document(C2_2).build())
                .otherApplicationsBundle(OtherApplicationsBundle.builder().document(OTHER_APPLICATION_2).build())
                .build();
            CaseData caseData = CaseData.builder()
                .additionalApplicationsBundle(wrapElements(
                    additionalApplicationsBundle1, additionalApplicationsBundle2))
                .build();

            testAdditionalApplications(caseData,
                List.of(C2, C2_2),
                List.of(OTHER_APPLICATION, OTHER_APPLICATION_2));
        }

        @Test
        void shouldConvertConfidentialC2UploadedByChildSolicitorOnly() {
            AdditionalApplicationsBundle c2ByChildSolicitor = AdditionalApplicationsBundle.builder()
                .c2DocumentBundleConfidential(C2DocumentBundle.builder().document(C2).build())
                .c2DocumentBundleChild0(C2DocumentBundle.builder().document(C2).build())
                .build();
            AdditionalApplicationsBundle c2ByLa = AdditionalApplicationsBundle.builder()
                .c2DocumentBundleConfidential(C2DocumentBundle.builder().document(C2_2).build())
                .c2DocumentBundleLA(C2DocumentBundle.builder().document(C2_2).build())
                .build();
            AdditionalApplicationsBundle c2ByRespondentSolicitor = AdditionalApplicationsBundle.builder()
                .c2DocumentBundleConfidential(C2DocumentBundle.builder().document(C2_2).build())
                .c2DocumentBundleResp0(C2DocumentBundle.builder().document(C2_2).build())
                .build();
            testAdditionalApplications(CaseData.builder()
                .additionalApplicationsBundle(wrapElements(
                    c2ByChildSolicitor, c2ByLa, c2ByRespondentSolicitor)).build(),
                List.of(C2), List.of());
        }

        @Test
        void shouldReturnEmptyListIfNoAdditionalApplication() {
            testAdditionalApplications(CaseData.builder().build(), List.of(), List.of());
            testAdditionalApplications(CaseData.builder()
                .additionalApplicationsBundle(wrapElements(AdditionalApplicationsBundle.builder().build())).build(),
                List.of(), List.of());
        }

        private void testAdditionalApplications(CaseData caseData, List<DocumentReference> c2Docs,
                                                List<DocumentReference> c1Docs) {
            CafcassApiCaseData actual = testConvert(caseData);
            List<CafcassApiCaseDocument> expectedDocs = new ArrayList<>();
            expectedDocs.addAll(getExpectedCafcassApiCaseDocuments(C2_APPLICATION_DOCUMENTS.getCafcassApiDocumentCategory(),
                false, c2Docs));
            expectedDocs.addAll(getExpectedCafcassApiCaseDocuments(C1_APPLICATION_DOCUMENTS.getCafcassApiDocumentCategory(),
                false, c1Docs));

            assertThat(actual.getCaseDocuments()).containsOnlyOnceElementsOf(expectedDocs);
        }
    }

    @Nested
    @DirtiesContext
    class ManagedDocuments {
        private static final DocumentReference NON_CONFIDENTIAL_DOC_1 = getTestDocumentReference();
        private static final DocumentReference NON_CONFIDENTIAL_DOC_2 = getTestDocumentReference();
        private static final DocumentReference CONFIDENTIAL_DOC_LA_1 = getTestDocumentReference();
        private static final DocumentReference CONFIDENTIAL_DOC_LA_2 = getTestDocumentReference();

        private static final List<Element<?>> NON_CONFIDENTIAL_DOCS = List.of(
            element(ManagedDocument.builder().document(NON_CONFIDENTIAL_DOC_1).build()),
            element(ManagedDocument.builder().document(NON_CONFIDENTIAL_DOC_2).build()));
        private static final List<Element<?>> CONFIDENTIAL_DOC_LA = List.of(
            element(ManagedDocument.builder().document(CONFIDENTIAL_DOC_LA_1).build()),
            element(ManagedDocument.builder().document(CONFIDENTIAL_DOC_LA_2).build()));
        private static final List<Element<?>> CONFIDENTIAL_DOC_CTSC = List.of(
            element(ManagedDocument.builder().document(getTestDocumentReference()).build()),
            element(ManagedDocument.builder().document(getTestDocumentReference()).build()));

        @ParameterizedTest
        @EnumSource(DocumentType.class)
        void shouldConvertAllManagedDocuments(DocumentType documentType) {
            Mockito.reset(manageDocumentService);
            if (isNotEmpty(documentType.getBaseFieldNameResolver())) {
                when(
                    manageDocumentService.readFromFieldName(any(),
                        eq(documentType.getBaseFieldNameResolver().apply(NON_CONFIDENTIAL)))
                ).thenReturn(
                    NON_CONFIDENTIAL_DOCS
                );

                when(
                    manageDocumentService.readFromFieldName(any(),
                        eq(documentType.getBaseFieldNameResolver().apply(LA)))
                ).thenReturn(
                    CONFIDENTIAL_DOC_LA
                );

                when(
                    manageDocumentService.readFromFieldName(any(),
                        eq(documentType.getBaseFieldNameResolver().apply(CTSC)))
                ).thenReturn(
                    CONFIDENTIAL_DOC_CTSC
                );

                testCaseDocument(CaseData.builder().build(),
                    List.of(NON_CONFIDENTIAL_DOC_1, NON_CONFIDENTIAL_DOC_2, CONFIDENTIAL_DOC_LA_1,
                        CONFIDENTIAL_DOC_LA_2),
                    documentType);
            } else {
                testCaseDocument(CaseData.builder().build(), List.of(), documentType);
            }
            Mockito.reset(manageDocumentService);
        }

        @Test
        void shouldReturnEmptyListIfNoDocumentFound() {
            when(manageDocumentService.readFromFieldName(any(), any())).thenReturn(List.of());
            testCaseDocument(CaseData.builder().build(), List.of(), "");

            when(manageDocumentService.readFromFieldName(any(), any())).thenReturn(null);
            testCaseDocument(CaseData.builder().build(), List.of(), "");
        }
    }
}
