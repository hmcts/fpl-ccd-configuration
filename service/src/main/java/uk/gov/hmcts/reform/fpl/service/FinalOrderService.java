package uk.gov.hmcts.reform.fpl.service;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.config.HmctsCourtLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.FinalOrder;
import uk.gov.hmcts.reform.fpl.model.OrderTypeAndDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper;

import java.time.format.FormatStyle;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.google.common.collect.Iterables.getLast;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static uk.gov.hmcts.reform.fpl.enums.FinalOrderType.BLANK_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.FinalOrderType.CARE_ORDER;

@Slf4j
@Service
public class FinalOrderService {
    private final DateFormatterService dateFormatterService;
    private final HmctsCourtLookupConfiguration hmctsCourtLookupConfiguration;
    private final LocalAuthorityNameLookupConfiguration localAuthorityNameLookupConfiguration;
    private final Time time;

    public FinalOrderService(DateFormatterService dateFormatterService,
                             HmctsCourtLookupConfiguration hmctsCourtLookupConfiguration,
                             LocalAuthorityNameLookupConfiguration localAuthorityNameLookupConfiguration,
                             Time time) {
        this.dateFormatterService = dateFormatterService;
        this.hmctsCourtLookupConfiguration = hmctsCourtLookupConfiguration;
        this.localAuthorityNameLookupConfiguration = localAuthorityNameLookupConfiguration;
        this.time = time;
    }

    public OrderTypeAndDocument updateTypeAndDocument(OrderTypeAndDocument typeAndDocument, Document document) {
        return typeAndDocument.toBuilder()
            .document(DocumentReference.builder()
                .url(document.links.self.href)
                .binaryUrl(document.links.binary.href)
                .filename(document.originalDocumentName)
                .build())
            .build();
    }

    /**
     * Method to populate the final order based on type of order selected
     * Currently adds/formats only the order title based on the type (may be more fields in future orders)
     * Always adds order type, document, {@link JudgeAndLegalAdvisor} object and a formatted order date.
     *
     * @param finalOrder           this value will contain fixed details and document values as well as customisable
     *                             values.
     * @param typeAndDocument      the type of the order and the order document.
     * @param judgeAndLegalAdvisor the judge and legal advisor for the order.
     * @return Element containing randomUUID and a fully populated FinalOrder.
     */
    public Element<FinalOrder> addCustomValuesToFinalOrder(FinalOrder finalOrder,
                                                           OrderTypeAndDocument typeAndDocument,
                                                           JudgeAndLegalAdvisor judgeAndLegalAdvisor) {
        FinalOrder order = defaultIfNull(finalOrder, FinalOrder.builder().build());
        FinalOrder.FinalOrderBuilder orderBuilder = FinalOrder.builder();

        //Scalable for future types of orders which may have additional fields
        switch (typeAndDocument.getFinalOrderType()) {
            case BLANK_ORDER:
                orderBuilder.orderTitle(defaultIfBlank(order.getOrderTitle(), "Order"));
                orderBuilder.orderDetails(order.getOrderDetails());
                break;
            case CARE_ORDER:
                orderBuilder.orderTitle(null);
                break;
            default:
        }

        return Element.<FinalOrder>builder()
            .id(randomUUID())
            .value(orderBuilder
                .type(typeAndDocument.getFinalOrderType())
                .document(typeAndDocument.getDocument())
                .judgeAndLegalAdvisor(judgeAndLegalAdvisor)
                .orderDate(dateFormatterService.formatLocalDateTimeBaseUsingFormat(time.now(),
                    "h:mma, d MMMM yyyy"))
                .build())
            .build();
    }

    public Map<String, Object> getFinalOrderTemplateData(CaseData caseData) {
        ImmutableMap.Builder<String, Object> orderTemplateBuilder = new ImmutableMap.Builder<>();

        switch (caseData.getOrderTypeAndDocument().getFinalOrderType()) {
            case BLANK_ORDER:
                orderTemplateBuilder
                    .put("orderType", BLANK_ORDER)
                    .put("orderTitle", defaultIfNull(caseData.getFinalOrder().getOrderTitle(), "Order"))
                    .put("childrenAct", "Section 31 Children Act 1989")
                    .put("orderDetails", caseData.getFinalOrder().getOrderDetails());
                break;
            case CARE_ORDER:
                orderTemplateBuilder
                    .put("orderType", CARE_ORDER)
                    .put("orderTitle", "Care Order")
                    .put("childrenAct", "Children Act 1989")
                    .put("orderDetails", careOrderDetails(getChildrenDetails(caseData).size(),
                        caseData.getCaseLocalAuthority()));
                break;
            default:
        }

        orderTemplateBuilder
            .put("familyManCaseNumber", caseData.getFamilyManCaseNumber())
            .put("courtName", getCourtName(caseData.getCaseLocalAuthority()))
            .put("todaysDate", dateFormatterService.formatLocalDateTimeBaseUsingFormat(time.now(), "d MMMM yyyy"))
            .put("judgeTitleAndName", JudgeAndLegalAdvisorHelper.formatJudgeTitleAndName(
                caseData.getJudgeAndLegalAdvisor()))
            .put("legalAdvisorName", JudgeAndLegalAdvisorHelper.getLegalAdvisorName(
                caseData.getJudgeAndLegalAdvisor()))
            .put("children", getChildrenDetails(caseData))
            .build();

        return orderTemplateBuilder.build();
    }

    private String getCourtName(String caseLocalAuthority) {
        return hmctsCourtLookupConfiguration.getCourt(caseLocalAuthority).getName();
    }

    private String getLocalAuthorityName(String caseLocalAuthority) {
        return localAuthorityNameLookupConfiguration.getLocalAuthorityName(caseLocalAuthority);
    }

    private String careOrderDetails(int numOfChildren, String caseLocalAuthority) {
        return "It is ordered that the " + (numOfChildren == 1 ? "child is " :
            "children are ") + "placed in the care of " + getLocalAuthorityName(caseLocalAuthority) + ".";
    }

    private List<Map<String, String>> getChildrenDetails(CaseData caseData) {
        return caseData.getAllChildren().stream()
            .map(Element::getValue)
            .map(Child::getParty)
            .map(child -> ImmutableMap.of(
                "name", child.getFirstName() + " " + child.getLastName(),
                "gender", defaultIfNull(child.getGender(), ""),
                "dateOfBirth", child.getDateOfBirth() != null ? dateFormatterService
                    .formatLocalDateToString(child.getDateOfBirth(), FormatStyle.LONG) : ""))
            .collect(toList());
    }

    public String mostRecentUploadedOrderDocumentUrl(final List<Element<FinalOrder>> finalOrders) {
        return getLast(finalOrders.stream()
            .filter(Objects::nonNull)
            .map(Element::getValue)
            .filter(Objects::nonNull)
            .collect(toList()))
            .getDocument().getBinaryUrl();
    }

    public String generateDocumentFileName(OrderTypeAndDocument orderTypeAndDocument) {
        return orderTypeAndDocument.getFinalOrderType().getType() + ".pdf";
    }
}
