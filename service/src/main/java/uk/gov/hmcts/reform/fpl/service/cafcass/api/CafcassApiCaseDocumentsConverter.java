package uk.gov.hmcts.reform.fpl.service.cafcass.api;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.cfv.ConfidentialLevel;
import uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.PlacementConfidentialDocument;
import uk.gov.hmcts.reform.fpl.model.PlacementNoticeDocument;
import uk.gov.hmcts.reform.fpl.model.PlacementSupportingDocument;
import uk.gov.hmcts.reform.fpl.model.Supplement;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.cafcass.api.CafcassApiCaseData;
import uk.gov.hmcts.reform.fpl.model.cafcass.api.CafcassApiCaseDocument;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.OtherApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.SubmittedC1WithSupplementBundle;
import uk.gov.hmcts.reform.fpl.model.group.C110A;
import uk.gov.hmcts.reform.fpl.model.interfaces.WithDocument;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType.AA_PARENT_ORDERS;
import static uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType.C1_APPLICATION_DOCUMENTS;
import static uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType.C2_APPLICATION_DOCUMENTS;
import static uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType.NOTICE_OF_ACTING_OR_ISSUE;
import static uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType.PLACEMENT_RESPONSES;
import static uk.gov.hmcts.reform.fpl.utils.DocumentsHelper.getDocumentIdFromUrl;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CafcassApiCaseDocumentsConverter implements CafcassApiCaseDataConverter {
    private final ManageDocumentService manageDocumentService;

    @Override
    public CafcassApiCaseData.CafcassApiCaseDataBuilder convert(CaseData caseData,
                                                                CafcassApiCaseData.CafcassApiCaseDataBuilder builder) {
        return builder.caseDocuments(getCaseDocuments(caseData));
    }

    private List<CafcassApiCaseDocument> getCaseDocuments(CaseData caseData) {
        return Stream.of(
                getStandardAndUrgentDirectionOrder(caseData),
                getDraftOrders(caseData),
                getApprovedOrders(caseData),
                getOriginalApplications(caseData),
                getPlacementApplications(caseData),
                getAdditionalApplications(caseData),
                getHearingNotice(caseData),
                getManageDocuments(caseData))
            .flatMap(List::stream)
            .distinct()
            .toList();
    }

    private List<CafcassApiCaseDocument> getStandardAndUrgentDirectionOrder(CaseData caseData) {
        return Stream.of(caseData.getUrgentDirectionsOrder(), caseData.getStandardDirectionOrder())
            .filter(Objects::nonNull)
            .map(sdo -> buildCafcassApiCaseDocumentList(AA_PARENT_ORDERS, false,
                Stream.of(sdo.getOrderDoc(), sdo.getTranslatedOrderDoc())))
            .flatMap(List::stream)
            .toList();
    }

    private List<CafcassApiCaseDocument> getDraftOrders(CaseData caseData) {
        // Remarks: cafcass don't have permission to read draftUploadedCMOs and refusedHearingOrders
        return buildCafcassApiCaseDocumentList("draftOrders", false,
            Stream.of(
                    caseData.getHearingOrdersBundlesDrafts(),
                    caseData.getHearingOrdersBundlesDraftReview())
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .map(Element::getValue)
                .flatMap(draftOrderBundles ->
                    Stream.of(draftOrderBundles.getOrders(), draftOrderBundles.getAllChildConfidentialOrders())
                        .flatMap(List::stream)
                        .map(Element::getValue))
                .map(HearingOrder::getOrderOrOrderConfidential));
    }

    private List<CafcassApiCaseDocument> getApprovedOrders(CaseData caseData) {
        // Remarks: cafcass don't have permission to read refusedHearingOrders
        return buildCafcassApiCaseDocumentList(AA_PARENT_ORDERS, false,
            Stream.concat(
                // approved CMOs
                unwrapElements(caseData.getSealedCMOs()).stream().map(HearingOrder::getOrderOrOrderConfidential),
                // approved orders (only those can be read by cafcass)
                Stream.of(caseData.getOrderCollection(),
                        caseData.getConfidentialOrders().getAllChildConfidentialOrders())
                    .filter(Objects::nonNull)
                    .flatMap(List::stream)
                    .map(Element::getValue)
                    .map(GeneratedOrder::getDocumentOrDocumentConfidential)));
    }

    private List<CafcassApiCaseDocument> getOriginalApplications(CaseData caseData) {
        List<DocumentReference> documentReferences = new ArrayList<>();

        SubmittedC1WithSupplementBundle submittedC1 = caseData.getSubmittedC1WithSupplement();
        if (isNotEmpty(submittedC1)) {
            documentReferences.add(submittedC1.getDocument());
            documentReferences.addAll(getAllDocumentsFromSupplements(submittedC1.getSupplementsBundle()));
            documentReferences.addAll(
                getAllDocumentsFromSupportingEvidenceBundles(submittedC1.getSupportingEvidenceBundle()));
        }

        C110A c110a = caseData.getC110A();
        if (isNotEmpty(c110a)) {
            documentReferences.addAll(Stream.of(c110a.getDocument(), c110a.getTranslatedDocument(),
                    c110a.getSupplementDocument())
                .toList());
        }

        return buildCafcassApiCaseDocumentList("originalApplications", false, documentReferences);
    }

    private List<CafcassApiCaseDocument> getPlacementApplications(CaseData caseData) {
        return buildCafcassApiCaseDocumentList(PLACEMENT_RESPONSES, false,
            unwrapElements(caseData.getPlacementEventData().getPlacements()).stream()
                .map(placement -> {
                    List<DocumentReference> documentReferences = new ArrayList<>();

                    documentReferences.add(placement.getApplication());
                    documentReferences.add(placement.getPlacementNotice());
                    documentReferences.addAll(unwrapElements(placement.getConfidentialDocuments()).stream()
                        .map(PlacementConfidentialDocument::getDocument).toList());
                    documentReferences.addAll(unwrapElements(placement.getSupportingDocuments()).stream()
                        .map(PlacementSupportingDocument::getDocument).toList());
                    documentReferences.addAll(unwrapElements(placement.getNoticeDocuments()).stream()
                        .map(PlacementNoticeDocument::getDocument).toList());

                    return documentReferences;
                })
                .flatMap(List::stream));
    }

    private List<CafcassApiCaseDocument> getAdditionalApplications(CaseData caseData) {
        final List<CafcassApiCaseDocument> resultList = new ArrayList<>();

        // additional application
        unwrapElements(caseData.getAdditionalApplicationsBundle()).forEach(additionalApplicationsBundle -> {
            C2DocumentBundle c2Bundle = (additionalApplicationsBundle.isConfidentialC2UploadedByChildSolicitor())
                ? additionalApplicationsBundle.getC2DocumentBundleConfidential()
                : additionalApplicationsBundle.getC2DocumentBundle();
            if (isNotEmpty(c2Bundle)) {
                List<DocumentReference> c2DocRef = new ArrayList<>();
                c2DocRef.add(c2Bundle.getDocument());

                c2DocRef.addAll(getAllDocumentsFromSupplements(c2Bundle.getSupplementsBundle()));
                c2DocRef.addAll(getAllDocumentsFromSupportingEvidenceBundles(
                    c2Bundle.getSupportingEvidenceBundle()));

                resultList.addAll(buildCafcassApiCaseDocumentList(C2_APPLICATION_DOCUMENTS, false, c2DocRef));
            }

            OtherApplicationsBundle otherBundle = additionalApplicationsBundle.getOtherApplicationsBundle();
            if (isNotEmpty(otherBundle)) {
                List<DocumentReference> otherDocRef = new ArrayList<>();

                otherDocRef.add(otherBundle.getDocument());
                otherDocRef.addAll(getAllDocumentsFromSupplements(otherBundle.getSupplementsBundle()));
                otherDocRef.addAll(getAllDocumentsFromSupportingEvidenceBundles(
                    otherBundle.getSupportingEvidenceBundle()));

                resultList.addAll(buildCafcassApiCaseDocumentList(C1_APPLICATION_DOCUMENTS, false, otherDocRef));
            }
        });

        return resultList;
    }

    private List<DocumentReference> getAllDocumentsFromSupportingEvidenceBundles(List<Element<SupportingEvidenceBundle>>
                                                                                     bundles) {
        return unwrapElements(bundles).stream()
            .flatMap(supportingEvidenceBundleElement -> Stream.of(
                supportingEvidenceBundleElement.getDocument(),
                supportingEvidenceBundleElement.getTranslatedDocument()))
            .toList();
    }

    private List<DocumentReference> getAllDocumentsFromSupplements(List<Element<Supplement>> bundles) {
        return unwrapElements(bundles).stream().map(Supplement::getDocument).toList();
    }

    private List<CafcassApiCaseDocument> getHearingNotice(CaseData caseData) {
        return buildCafcassApiCaseDocumentList(NOTICE_OF_ACTING_OR_ISSUE, false,
            unwrapElements(caseData.getHearingDetails()).stream()
                .flatMap(hearingBooking -> Stream.of(
                    hearingBooking.getNoticeOfHearing(),
                    hearingBooking.getTranslatedNoticeOfHearing())));
    }

    private List<CafcassApiCaseDocument> getManageDocuments(CaseData caseData) {
        return Arrays.stream(DocumentType.values())
            .filter(documentType -> isNotEmpty(documentType.getBaseFieldNameResolver()))
            .map(documentType -> buildCafcassApiCaseDocumentList(documentType, false,
                Stream.of(ConfidentialLevel.NON_CONFIDENTIAL, ConfidentialLevel.LA)
                    .map(confidentialLevel -> manageDocumentService
                        .toFieldNameToListOfElementMap(caseData, documentType, confidentialLevel).values())
                    .flatMap(Collection::stream).flatMap(List::stream)
                    .map(Element::getValue)
                    .map(object -> (WithDocument) object)
                    .map(WithDocument::getDocument)))
            .flatMap(List::stream)
            .toList();
    }

    private CafcassApiCaseDocument buildCafcassApiCaseDocument(String category, boolean removed,
                                                               DocumentReference docRef) {
        return CafcassApiCaseDocument.builder()
            .documentId(getDocumentIdFromUrl(docRef.getUrl()).toString())
            .documentFileName(docRef.getFilename())
            .documentCategory(category)
            .removed(removed)
            .uploadTimestamp(docRef.getUploadedTimestamp())
            .build();
    }

    private List<CafcassApiCaseDocument> buildCafcassApiCaseDocumentList(DocumentType docType, boolean removed,
                                                                         List<DocumentReference> docRefList) {
        return buildCafcassApiCaseDocumentList(docType.getCategory(), removed, docRefList);
    }

    private List<CafcassApiCaseDocument> buildCafcassApiCaseDocumentList(String category, boolean removed,
                                                                         List<DocumentReference> docRefList) {
        return buildCafcassApiCaseDocumentList(category, removed, docRefList.stream());
    }

    private List<CafcassApiCaseDocument> buildCafcassApiCaseDocumentList(DocumentType category, boolean removed,
                                                                         Stream<DocumentReference> docRefList) {
        return buildCafcassApiCaseDocumentList(category.getCategory(), removed, docRefList);
    }

    private List<CafcassApiCaseDocument> buildCafcassApiCaseDocumentList(String category, boolean removed,
                                                                         Stream<DocumentReference> docRefList) {
        return docRefList
            .filter(Objects::nonNull)
            .map(docRef -> buildCafcassApiCaseDocument(category, removed, docRef))
            .toList();
    }
}
