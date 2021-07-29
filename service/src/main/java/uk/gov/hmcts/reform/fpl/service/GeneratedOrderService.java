package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.enums.GeneratedEPOKey;
import uk.gov.hmcts.reform.fpl.enums.GeneratedOrderKey;
import uk.gov.hmcts.reform.fpl.enums.GeneratedOrderSubtype;
import uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType;
import uk.gov.hmcts.reform.fpl.enums.InterimOrderKey;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.OrderTypeAndDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisGeneratedOrder;
import uk.gov.hmcts.reform.fpl.model.order.generated.FurtherDirections;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.model.order.generated.InterimEndDate;
import uk.gov.hmcts.reform.fpl.service.docmosis.BlankOrderGenerationService;
import uk.gov.hmcts.reform.fpl.service.docmosis.CareOrderGenerationService;
import uk.gov.hmcts.reform.fpl.service.docmosis.DischargeCareOrderGenerationService;
import uk.gov.hmcts.reform.fpl.service.docmosis.EPOGenerationService;
import uk.gov.hmcts.reform.fpl.service.docmosis.SupervisionOrderGenerationService;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.DocumentUploadHelper;
import uk.gov.hmcts.reform.fpl.utils.OrderHelper;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static com.google.common.collect.Iterables.getLast;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderSubtype.INTERIM;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.BLANK_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.DISCHARGE_OF_CARE_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.InterimEndDateType.END_OF_PROCEEDINGS;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.TIME_DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

// REFACTOR: 08/10/2020 Split into separate services (see: FPLA-2290)
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class GeneratedOrderService {
    private final BlankOrderGenerationService blankOrderGenerationService;
    private final CareOrderGenerationService careOrderGenerationService;
    private final SupervisionOrderGenerationService supervisionOrderGenerationService;
    private final EPOGenerationService epoGenerationService;
    private final DischargeCareOrderGenerationService dischargeCareOrderGenerationService;
    private final DischargeCareOrderService dischargeCareOrder;
    private final ChildrenService childrenService;
    private final CourtService courtService;
    private final DocumentUploadHelper uploadHelper;
    private final Time time;

    public OrderTypeAndDocument buildOrderTypeAndDocument(OrderTypeAndDocument typeAndDocument, Document document) {
        return typeAndDocument.toBuilder()
            .document(DocumentReference.builder()
                .url(document.links.self.href)
                .binaryUrl(document.links.binary.href)
                .filename(document.originalDocumentName)
                .build())
            .build();
    }

    public GeneratedOrder buildCompleteOrder(OrderTypeAndDocument typeAndDocument,
                                             JudgeAndLegalAdvisor judgeAndLegalAdvisor,
                                             CaseData caseData) {
        GeneratedOrder.GeneratedOrderBuilder orderBuilder = GeneratedOrder.builder();
        GeneratedOrderType orderType = typeAndDocument.getType();
        String expiryDate = null;
        String date = null;

        switch (orderType) {
            case BLANK_ORDER:
                GeneratedOrder generatedOrder = caseData.getOrder();
                orderBuilder.title(defaultIfBlank(generatedOrder.getTitle(), "Order"))
                    .details(generatedOrder.getDetails());
                break;
            case CARE_ORDER:
                orderBuilder.title(null);
                if (typeAndDocument.getSubtype() == INTERIM) {
                    requireNonNull(caseData.getInterimEndDate());
                    expiryDate = getInterimExpiryDate(caseData.getInterimEndDate());
                }
                break;
            case SUPERVISION_ORDER:
                orderBuilder.title(null);
                expiryDate = getManageOrderExpiryDate(typeAndDocument, caseData.getOrderMonths(),
                    caseData.getInterimEndDate());
                break;
            case EMERGENCY_PROTECTION_ORDER:
                date = formatLocalDateTimeBaseUsingFormat(caseData.getDateAndTimeOfIssue(), TIME_DATE);
                expiryDate = formatLocalDateTimeBaseUsingFormat(caseData.getEpoEndDate(), TIME_DATE);
                break;
            case UPLOAD:
                return GeneratedOrder.builder()
                    .document(typeAndDocument.getDocument())
                    .type(typeAndDocument.getTypeLabel())
                    .date(formatLocalDateTimeBaseUsingFormat(time.now(), TIME_DATE))
                    .dateOfIssue(formatLocalDateToString(caseData.getDateOfIssue(), DATE))
                    .uploader(uploadHelper.getUploadedDocumentUserDetails())
                    .uploadedOrderDescription(typeAndDocument.getOrderDescription())
                    .build();
            default:
        }

        orderBuilder.expiryDate(expiryDate)
            .dateOfIssue(date != null ? date : formatLocalDateToString(caseData.getDateOfIssue(), DATE))
            .type(OrderHelper.getFullOrderType(typeAndDocument))
            .document(typeAndDocument.getDocument())
            .judgeAndLegalAdvisor(judgeAndLegalAdvisor)
            .date(formatLocalDateTimeBaseUsingFormat(time.now(), TIME_DATE))
            .courtName(courtService.getCourtName(caseData))
            .children(getChildren(orderType, caseData));

        return orderBuilder.build();
    }

    private List<Element<Child>> getChildren(GeneratedOrderType orderType, CaseData caseData) {
        if (orderType == DISCHARGE_OF_CARE_ORDER) {
            return wrapElements(dischargeCareOrder.getChildrenInSelectedCareOrders(caseData));
        } else {
            return childrenService.getSelectedChildren(caseData);
        }
    }

    public DocmosisGeneratedOrder getOrderTemplateData(CaseData caseData) {
        OrderTypeAndDocument orderTypeAndDocument = caseData.getOrderTypeAndDocument();
        GeneratedOrderType orderType = orderTypeAndDocument.getType();

        switch (orderType) {
            case BLANK_ORDER:
                return blankOrderGenerationService.getTemplateData(caseData);
            case CARE_ORDER:
                return careOrderGenerationService.getTemplateData(caseData);
            case SUPERVISION_ORDER:
                return supervisionOrderGenerationService.getTemplateData(caseData);
            case EMERGENCY_PROTECTION_ORDER:
                return epoGenerationService.getTemplateData(caseData);
            case DISCHARGE_OF_CARE_ORDER:
                return dischargeCareOrderGenerationService.getTemplateData(caseData);
            default:
                throw new UnsupportedOperationException("Unexpected value: " + orderType);
        }
    }

    public String generateOrderDocumentFileName(GeneratedOrderType orderType, GeneratedOrderSubtype orderSubtype) {
        String subtype = (orderSubtype != null) ? orderSubtype.getLabel().toLowerCase() + "_" : "";

        return subtype + orderType.getFileName();
    }

    public DocumentReference getMostRecentUploadedOrderDocument(final List<Element<GeneratedOrder>> orders) {
        return getLast(orders.stream()
            .filter(Objects::nonNull)
            .map(Element::getValue)
            .filter(Objects::nonNull)
            .collect(toList()))
            .getDocument();
    }

    public void removeOrderProperties(Map<String, Object> caseData) {
        Arrays.stream(GeneratedEPOKey.values()).forEach(ccdField -> caseData.remove(ccdField.getKey()));
        Arrays.stream(GeneratedOrderKey.values()).forEach(ccdField -> caseData.remove(ccdField.getKey()));
        Arrays.stream(InterimOrderKey.values()).forEach(ccdField -> caseData.remove(ccdField.getKey()));
    }

    /**
     * Determine if the draft order document should be generated. This ensures the document is only generated once
     *
     * <p>Will return {@code true} if:
     * <ul>
     * <li>the order is a blank order</li>
     * <li>further directions have been considered (i.e it is not a blank order)</li>
     * </ul>
     *
     * @param orderType         type of order
     * @param furtherDirections further directions for the order
     */
    public boolean shouldGenerateDocument(OrderTypeAndDocument orderType,
                                          FurtherDirections furtherDirections) {
        return BLANK_ORDER == orderType.getType() || furtherDirections != null;
    }

    /**
     * Determine if the user should see the close case page.
     *
     * <p>Will return {@code true} if all of the following are met:
     * <ul>
     * <li>close case is enabled</li>
     * <li>the order type is final or epo</li>
     * <li>all children will be marked to have a final order issued against them</li>
     * </ul>
     *
     * @param orderType type of order
     * @param children  list of children in the case
     */
    public boolean showCloseCase(OrderTypeAndDocument orderType,
                                 List<Element<Child>> children) {
        return orderType.isClosable()
            && childrenService.allChildrenHaveFinalOrder(children);
    }

    public boolean isFinalOrderAllowed(OrderTypeAndDocument orderType, List<Element<Child>> children) {
        return !(orderType.isClosable() && childrenService.allChildrenHaveFinalOrder(children));
    }

    private String getManageOrderExpiryDate(OrderTypeAndDocument typeAndDocument, Integer orderMonths,
                                            InterimEndDate interimEndDate) {
        switch (typeAndDocument.getSubtype()) {
            case INTERIM:
                requireNonNull(interimEndDate);
                return getInterimExpiryDate(interimEndDate);
            case FINAL:
                requireNonNull(orderMonths);
                return formatLocalDateTimeBaseUsingFormat(time.now().plusMonths(orderMonths), TIME_DATE);
            default:
                throw new UnsupportedOperationException("Unexpected value: " + typeAndDocument.getSubtype());
        }
    }

    private String getInterimExpiryDate(InterimEndDate interimEndDate) {
        return Optional.ofNullable(interimEndDate.getEndDateTime())
            .or(interimEndDate::toLocalDateTime)
            .map(dateTime -> formatLocalDateTimeBaseUsingFormat(dateTime, TIME_DATE))
            .orElse(END_OF_PROCEEDINGS.getLabel());
    }
}
