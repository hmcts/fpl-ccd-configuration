package uk.gov.hmcts.reform.fpl.service.cafcass.api;

import com.google.common.collect.ImmutableList;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
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
import uk.gov.hmcts.reform.fpl.model.order.DraftOrder;
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

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CafcassApiCaseDocumentsConverter implements CafcassApiCaseDataConverter {
    private final ManageDocumentService manageDocumentService;

    @Override
    public CafcassApiCaseData.CafcassApiCaseDataBuilder convert(CaseData caseData, CafcassApiCaseData.CafcassApiCaseDataBuilder builder) {
        return builder.caseDocuments(getCaseDocuments(caseData));
    }

    private List<CafcassApiCaseDocument> getCaseDocuments(CaseData caseData) {
        List<CafcassApiCaseDocument> resultList = new ArrayList<>();

        resultList.addAll(getStandardAndUrgentDirectionOrder(caseData));
        resultList.addAll(getDraftOrders(caseData));
        resultList.addAll(getApprovedOrders(caseData));
        resultList.addAll(getOriginalApplications(caseData));
        resultList.addAll(getPlacementApplications(caseData));
        resultList.addAll(getAdditionalApplications(caseData));
        resultList.addAll(getHearingNotice(caseData));
        resultList.addAll(getManageDocuments(caseData));

        return ImmutableList.copyOf(resultList);
    }
    private List<CafcassApiCaseDocument> getStandardAndUrgentDirectionOrder(CaseData caseData) {
        return Stream.of(caseData.getUrgentDirectionsOrder(), caseData.getStandardDirectionOrder())
            .filter(Objects::nonNull)
            .map(sdo ->
                Stream.of(sdo.getOrderDoc(), sdo.getTranslatedOrderDoc())
                    .filter(Objects::nonNull)
                    .map(docRef -> buildCafcassApiCaseDocument(AA_PARENT_ORDERS, docRef, false))
                    .toList())
            .flatMap(List::stream)
            .toList();
    }

    private List<CafcassApiCaseDocument> getDraftOrders(CaseData caseData) {
        // Remarks: cafcass don't have permission to read draftUploadedCMOs and refusedHearingOrders
        return Stream.of(
                unwrapElements(caseData.getHearingOrdersBundlesDrafts()),
                unwrapElements(caseData.getHearingOrdersBundlesDraftReview()))
            .flatMap(List::stream)
            .map(draftOrderBundles ->
                Stream.of(draftOrderBundles.getOrders(), draftOrderBundles.getAllChildConfidentialOrders())
                    .flatMap(List::stream)
                    .map(Element::getValue)
                    .toList())
            .flatMap(List::stream)
            .map(HearingOrder::getOrderOrOrderConfidential)
            .map(draftOrderDoc -> buildCafcassApiCaseDocument("draftOrders", draftOrderDoc, false))
            .toList();
    }

    private List<CafcassApiCaseDocument> getApprovedOrders(CaseData caseData) {
        // Remarks: cafcass don't have permission to read refusedHearingOrders
        return Stream.concat(
                // approved CMOs
                unwrapElements(caseData.getSealedCMOs()).stream().map(HearingOrder::getOrderOrOrderConfidential),
                // approved orders (only those can be read by cafcass)
                Stream.of(caseData.getOrderCollection(),
                        caseData.getConfidentialOrders().getAllChildConfidentialOrders())
                    .flatMap(List::stream)
                    .map(Element::getValue)
                    .map(GeneratedOrder::getDocumentOrDocumentConfidential)
            )
            .map(sealedCmoDoc -> buildCafcassApiCaseDocument(AA_PARENT_ORDERS, sealedCmoDoc, false))
            .toList();
    }

    private List<CafcassApiCaseDocument> getOriginalApplications(CaseData caseData) {
        List<DocumentReference> documentReferences = new ArrayList<>();

        SubmittedC1WithSupplementBundle submittedC1 = caseData.getSubmittedC1WithSupplement();
        if (isNotEmpty(submittedC1)) {
            documentReferences.add(submittedC1.getDocument());
            documentReferences.addAll(getAllDocumentsFromSupplements(submittedC1.getSupplementsBundle()));
            documentReferences.addAll(getAllDocumentsFromSupportingEvidenceBundles(submittedC1.getSupportingEvidenceBundle()));
        }

        C110A c110a = caseData.getC110A();
        if (isNotEmpty(c110a)) {
            documentReferences.addAll(Stream.of(c110a.getDocument(), c110a.getTranslatedDocument(),
                    c110a.getSupplementDocument())
                .toList());
        }

        return documentReferences.stream()
            .map(docRef -> buildCafcassApiCaseDocument("originalApplications", docRef, false))
            .toList();
    }

    private List<CafcassApiCaseDocument> getPlacementApplications(CaseData caseData) {
        return unwrapElements(caseData.getPlacementEventData().getPlacements()).stream()
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
            .flatMap(List::stream)
            .map(docRef -> buildCafcassApiCaseDocument(PLACEMENT_RESPONSES, docRef, false))
            .toList();
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
                    c2DocRef.addAll(unwrapElements(c2Bundle.getDraftOrdersBundle()).stream()
                        .map(DraftOrder::getDocument).toList());

                    resultList.addAll(c2DocRef.stream()
                        .map(docRef -> buildCafcassApiCaseDocument(C2_APPLICATION_DOCUMENTS, docRef,
                            false))
                        .toList());
                }

                OtherApplicationsBundle otherBundle = additionalApplicationsBundle.getOtherApplicationsBundle();
                if (isNotEmpty(otherBundle)) {
                    List<DocumentReference> otherDocRef = new ArrayList<>();

                    otherDocRef.add(otherBundle.getDocument());
                    otherDocRef.addAll(getAllDocumentsFromSupplements(otherBundle.getSupplementsBundle()));
                    otherDocRef.addAll(getAllDocumentsFromSupportingEvidenceBundles(
                        otherBundle.getSupportingEvidenceBundle()));

                    resultList.addAll(otherDocRef.stream()
                        .map(docRef -> buildCafcassApiCaseDocument(C1_APPLICATION_DOCUMENTS, docRef,
                            false))
                        .toList());
                }
        });


        return resultList;
    }

    private List<DocumentReference> getAllDocumentsFromSupportingEvidenceBundles(List<Element<SupportingEvidenceBundle>>
                                                                                     bundles) {
        return unwrapElements(bundles).stream()
            .map(supportingEvidenceBundleElement -> List.of(supportingEvidenceBundleElement.getDocument(),
                supportingEvidenceBundleElement.getTranslatedDocument()))
            .flatMap(List::stream).toList();
    }

    private List<DocumentReference> getAllDocumentsFromSupplements(List<Element<Supplement>> bundles) {
        return unwrapElements(bundles).stream().map(Supplement::getDocument).toList();
    }

    private List<CafcassApiCaseDocument> getHearingNotice(CaseData caseData) {
        return unwrapElements(caseData.getHearingDetails()).stream()
            .map(hearingBooking -> List.of(hearingBooking.getNoticeOfHearing(),
                hearingBooking.getTranslatedNoticeOfHearing()))
            .flatMap(List::stream)
            .map(docRef -> buildCafcassApiCaseDocument(NOTICE_OF_ACTING_OR_ISSUE, docRef, false))
            .toList();
    }

    private List<CafcassApiCaseDocument> getManageDocuments(CaseData caseData) {
        return Arrays.stream(DocumentType.values())
            .filter(documentType -> documentType.getBaseFieldNameResolver() != null)
            .map(documentType ->
                Stream.concat(
                    Stream.of(ConfidentialLevel.NON_CONFIDENTIAL, ConfidentialLevel.LA)
                        .map(confidentialLevel -> manageDocumentService
                            .toFieldNameToListOfElementMap(caseData, documentType, confidentialLevel).values())
                        .flatMap(Collection::stream).flatMap(List::stream),
                    manageDocumentService.getListOfRemovedElement(caseData, documentType).stream())
                    .map(Element::getValue)
//                    .filter(object -> object instanceof WithDocument)
                    .map(object -> (WithDocument) object)
                    .map(doc -> buildCafcassApiCaseDocument(documentType, doc.getDocument(), false))
                    .toList())
            .flatMap(List::stream)
            .toList();
    }

    private CafcassApiCaseDocument buildCafcassApiCaseDocument(String category, DocumentReference docRef,
                                                               boolean removed) {
        return CafcassApiCaseDocument.builder()
            .documentId(getDocumentIdFromUrl(docRef.getUrl()).toString())
            .document_filename(docRef.getFilename())
            .documentCategory(category)
            .removed(removed)
            .build();
    }

    private CafcassApiCaseDocument buildCafcassApiCaseDocument(DocumentType docType, DocumentReference docRef,
                                                               boolean removed) {
        return buildCafcassApiCaseDocument(docType.getCategory(), docRef, removed);
    }
}
