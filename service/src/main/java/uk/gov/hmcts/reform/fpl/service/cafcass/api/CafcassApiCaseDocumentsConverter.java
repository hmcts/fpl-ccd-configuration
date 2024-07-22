package uk.gov.hmcts.reform.fpl.service.cafcass.api;

import com.google.common.collect.ImmutableList;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.cfv.ConfidentialLevel;
import uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.cafcass.api.CafcassApiCaseData;
import uk.gov.hmcts.reform.fpl.model.cafcass.api.CafcassApiCaseDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.interfaces.WithDocument;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType.AA_PARENT_ORDERS;
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

        //draftOrderForReviewPriorToHearing
        //applications
        //originalApplications
        // c1AndOtherApplications (main application form)
        // c2Applications (main application form)
        //orders
        //draftOrders
        //applicantsDocuments
        //parent_expertReports
        //documentsSentToParties
        resultList.addAll(getOrders(caseData));
        resultList.addAll(getApplicationForm(caseData));
        resultList.addAll(getPlacementApplications(caseData));
        resultList.addAll(getAdditionalApplications(caseData));
        resultList.addAll(getHearingNotice(caseData));
        resultList.addAll(getManageDocuments(caseData));

        return ImmutableList.copyOf(resultList);
    }

    private List<CafcassApiCaseDocument> getOrders(CaseData caseData) {
        List<CafcassApiCaseDocument> resultList = new ArrayList<>();
        // TODO check ccd config, only add to the list if Cafcass can view it

        // TODO urgentDirectionsOrder
        caseData.getUrgentDirectionsOrder();
        // TODO standardDirectionOrder
        caseData.getStandardDirectionOrder();

        // draft orders (hearingOrdersBundlesDrafts, hearingOrdersBundlesDraftReview)
        // Remarks: cafcass don't have permission to read draftUploadedCMOs and refusedHearingOrders
        resultList.addAll(Stream.of(unwrapElements(caseData.getHearingOrdersBundlesDrafts()),
                unwrapElements(caseData.getHearingOrdersBundlesDraftReview()))
            .flatMap(List::stream)
            // convert Stream<HearingOrdersBundle> to Stream<HearingOrder>
            .map(draftOrderBundles ->
                Stream.of(draftOrderBundles.getOrders(), draftOrderBundles.getAllChildConfidentialOrders())
                    .flatMap(List::stream)
                    .map(Element::getValue)
                    .toList())
            .flatMap(List::stream)
            .map(draftOrder -> (draftOrder.isConfidentialOrder())
                ? draftOrder.getOrderConfidential() : draftOrder.getOrder())
            .map(draftOrderDoc -> buildCafcassApiCaseDocument("draftOrders", draftOrderDoc, false))
            .toList());

        // approved CMOs
        resultList.addAll(unwrapElements(caseData.getSealedCMOs()).stream()
            .map(sealedCmo -> (sealedCmo.isConfidentialOrder())
                ? sealedCmo.getOrderConfidential() : sealedCmo.getOrder())
            .map(sealedCmoDoc -> buildCafcassApiCaseDocument(AA_PARENT_ORDERS, sealedCmoDoc, false))
            .toList());

        // approved orders (only those can be read by cafcass)
        resultList.addAll(Stream.of(caseData.getOrderCollection(),
                caseData.getConfidentialOrders().getAllChildConfidentialOrders())
            .flatMap(List::stream)
            .map(Element::getValue)
            .map(order -> (order.isConfidential()) ? order.getDocumentConfidential() : order.getDocument())
            .map(orderDoc -> buildCafcassApiCaseDocument(AA_PARENT_ORDERS, orderDoc, false))
            .toList());

        return resultList;
    }

    private List<CafcassApiCaseDocument> getApplicationForm(CaseData caseData) {
        //TODO
        return List.of();
    }

    private List<CafcassApiCaseDocument> getPlacementApplications(CaseData caseData) {
        //TODO
        return List.of();
    }

    private List<CafcassApiCaseDocument> getAdditionalApplications(CaseData caseData) {
        //TODO
        return List.of();
    }

    private List<CafcassApiCaseDocument> getHearingNotice(CaseData caseData) {
        //TODO
        return List.of();
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
