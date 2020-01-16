package uk.gov.hmcts.reform.fpl.service;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.config.HmctsCourtLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.GeneratedOrderKey;
import uk.gov.hmcts.reform.fpl.enums.GeneratedOrderSubtype;
import uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.model.OrderTypeAndDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper;

import java.time.LocalDateTime;
import java.time.format.FormatStyle;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.google.common.collect.Iterables.getLast;
import static java.util.Optional.ofNullable;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderSubtype.FINAL;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderSubtype.INTERIM;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.BLANK_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.CARE_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.SUPERVISION_ORDER;

@Slf4j
@Service
public class GeneratedOrderService {
    private final DateFormatterService dateFormatterService;
    private final HmctsCourtLookupConfiguration hmctsCourtLookupConfiguration;
    private final LocalAuthorityNameLookupConfiguration localAuthorityNameLookupConfiguration;
    private final Time time;

    public GeneratedOrderService(DateFormatterService dateFormatterService,
                                 HmctsCourtLookupConfiguration hmctsCourtLookupConfiguration,
                                 LocalAuthorityNameLookupConfiguration localAuthorityNameLookupConfiguration,
                                 Time time) {
        this.dateFormatterService = dateFormatterService;
        this.hmctsCourtLookupConfiguration = hmctsCourtLookupConfiguration;
        this.localAuthorityNameLookupConfiguration = localAuthorityNameLookupConfiguration;
        this.time = time;
    }

    public OrderTypeAndDocument buildOrderTypeAndDocument(OrderTypeAndDocument typeAndDocument, Document document) {
        return typeAndDocument.toBuilder()
            .document(DocumentReference.builder()
                .url(document.links.self.href)
                .binaryUrl(document.links.binary.href)
                .filename(document.originalDocumentName)
                .build())
            .build();
    }

    /**
     * Method to populate the order based on type of order selected
     * Adds/formats the order title and details for C21
     * Always adds order type, document, {@link JudgeAndLegalAdvisor} object and a formatted order date.
     *
     * @param typeAndDocument      the type of the order and the order document (document only shown in check answers)
     * @param order                this value will contain fixed details and document values as well as customisable
     *                             values.
     * @param judgeAndLegalAdvisor the judge and legal advisor for the order.
     * @param orderMonths          the number of months the supervision order is valid
     * @return Element containing randomUUID and a fully populated order.
     */
    public Element<GeneratedOrder> buildCompleteOrder(OrderTypeAndDocument typeAndDocument,
                                                      GeneratedOrder order,
                                                      JudgeAndLegalAdvisor judgeAndLegalAdvisor,
                                                      Integer orderMonths) {
        GeneratedOrder generatedOrder = defaultIfNull(order, GeneratedOrder.builder().build());
        GeneratedOrder.GeneratedOrderBuilder orderBuilder = GeneratedOrder.builder();

        GeneratedOrderType orderType = typeAndDocument.getType();

        switch (orderType) {
            case BLANK_ORDER:
                orderBuilder.title(defaultIfBlank(generatedOrder.getTitle(), "Order"))
                    .details(generatedOrder.getDetails())
                    .expiryDate(null);
                break;
            case CARE_ORDER:
                orderBuilder.title(null)
                    .expiryDate(null);
                break;
            case SUPERVISION_ORDER:
                orderBuilder.title(null);
                if (typeAndDocument.getSubtype() == FINAL) {
                    ofNullable(orderMonths)
                        .map(i -> time.now().plusMonths(orderMonths))
                        .map(dateTime -> dateFormatterService.formatLocalDateTimeBaseUsingFormat(
                            dateTime, "h:mma, d MMMM y"))
                        .ifPresent(orderBuilder::expiryDate);
                }
                break;
            default:
        }

        return Element.<GeneratedOrder>builder()
            .id(randomUUID())
            .value(orderBuilder
                .type(orderType.getFullType(typeAndDocument.getSubtype()))
                .document(typeAndDocument.getDocument())
                .judgeAndLegalAdvisor(judgeAndLegalAdvisor)
                .date(dateFormatterService.formatLocalDateTimeBaseUsingFormat(time.now(),
                    "h:mma, d MMMM yyyy"))
                .build())
            .build();
    }

    public Map<String, Object> getOrderTemplateData(CaseData caseData) {
        ImmutableMap.Builder<String, Object> orderTemplateBuilder = new ImmutableMap.Builder<>();
        OrderTypeAndDocument orderTypeAndDocument = caseData.getOrderTypeAndDocument();
        GeneratedOrderType orderType = orderTypeAndDocument.getType();
        GeneratedOrderSubtype subtype = orderTypeAndDocument.getSubtype();

        switch (orderType) {
            case BLANK_ORDER:
                orderTemplateBuilder
                    .put("orderTitle", defaultIfNull(caseData.getOrder().getTitle(), "Order"))
                    .put("childrenAct", "Children Act 1989")
                    .put("orderDetails", caseData.getOrder().getDetails());
                break;
            case CARE_ORDER:
                if (subtype == INTERIM) {
                    orderTemplateBuilder
                        .put("orderTitle", CARE_ORDER.getFullType(INTERIM))
                        .put("childrenAct", "Section 38 Children Act 1989");
                } else if (subtype == FINAL) {
                    orderTemplateBuilder
                        .put("orderTitle", CARE_ORDER.getFullType())
                        .put("childrenAct", "Section 31 Children Act 1989");
                }
                orderTemplateBuilder
                    .put("orderDetails", careOrderDetails(getChildrenDetails(caseData).size(),
                        caseData.getCaseLocalAuthority(), orderTypeAndDocument));
                break;
            case SUPERVISION_ORDER:
                if (subtype == INTERIM) {
                    orderTemplateBuilder
                        .put("orderTitle", SUPERVISION_ORDER.getFullType(INTERIM))
                        .put("childrenAct", "Section 38 and Paragraphs 1 and 2 Schedule 3 Children Act 1989")
                        .put("orderDetails", interimSupervisionOrderDetails(getChildrenDetails(caseData).size(),
                            caseData.getCaseLocalAuthority()));
                } else if (subtype == FINAL) {
                    orderTemplateBuilder
                        .put("orderTitle", SUPERVISION_ORDER.getFullType())
                        .put("childrenAct", "Section 31 and Paragraphs 1 and 2 Schedule 3 Children Act 1989")
                        .put("orderDetails", finalSupervisionOrderDetails(getChildrenDetails(caseData).size(),
                            caseData.getCaseLocalAuthority(), caseData.getOrderMonths()));
                }
                break;
            default:
                throw new UnsupportedOperationException("Unexpected value: " + orderType);
        }

        orderTemplateBuilder
            .put("orderType", orderType)
            .put("familyManCaseNumber", caseData.getFamilyManCaseNumber())
            .put("courtName", getCourtName(caseData.getCaseLocalAuthority()))
            .put("todaysDate", dateFormatterService.formatLocalDateTimeBaseUsingFormat(time.now(), "d MMMM yyyy"))
            .put("judgeTitleAndName", JudgeAndLegalAdvisorHelper.formatJudgeTitleAndName(
                caseData.getJudgeAndLegalAdvisor()))
            .put("legalAdvisorName", JudgeAndLegalAdvisorHelper.getLegalAdvisorName(caseData.getJudgeAndLegalAdvisor()))
            .put("children", getChildrenDetails(caseData))
            .put("furtherDirections", caseData.getFurtherDirectionsText())
            .build();

        return orderTemplateBuilder.build();
    }

    public String generateOrderDocumentFileName(GeneratedOrderType orderType, GeneratedOrderSubtype orderSubtype) {
        String type = orderType.getLabel().toLowerCase().replace("(", "").replace(")", "").replace(" ", "_");
        String subtype = (orderSubtype != null) ? orderSubtype.getLabel().toLowerCase() + "_" : "";

        return subtype + type + ".pdf";
    }

    public String getMostRecentUploadedOrderDocumentUrl(final List<Element<GeneratedOrder>> orders) {
        return getLast(orders.stream()
            .filter(Objects::nonNull)
            .map(Element::getValue)
            .filter(Objects::nonNull)
            .collect(toList()))
            .getDocument().getBinaryUrl();
    }

    public void removeOrderProperties(Map<String, Object> data) {
        Arrays.stream(GeneratedOrderKey.values()).forEach(value -> data.remove(value.getKey()));
    }

    private String getCourtName(String courtName) {
        return hmctsCourtLookupConfiguration.getCourt(courtName).getName();
    }

    private String getLocalAuthorityName(String caseLocalAuthority) {
        return localAuthorityNameLookupConfiguration.getLocalAuthorityName(caseLocalAuthority);
    }

    private String careOrderDetails(int numOfChildren, String caseLocalAuthority, OrderTypeAndDocument typeAndDoc) {
        String childOrChildren = (numOfChildren == 1 ? "child is" : "children are");
        return String.format("It is ordered that the %s placed in the care of %s%s",
            childOrChildren, getLocalAuthorityName(caseLocalAuthority),
            hasInterimSubtype(typeAndDoc) ? " until the end of the proceedings." : ".");
    }

    private Object interimSupervisionOrderDetails(int numOfChildren, String caseLocalAuthority) {
        return String.format(
            "It is ordered that %s supervises the %s until the end of the proceedings",
            getLocalAuthorityName(caseLocalAuthority),
            (numOfChildren == 1) ? "child" : "children");
    }

    private String finalSupervisionOrderDetails(int numOfChildren, String caseLocalAuthority, int numOfMonths) {
        final LocalDateTime orderExpiration = time.now().plusMonths(numOfMonths);
        final String dayOrdinalSuffix = dateFormatterService.getDayOfMonthSuffix(orderExpiration.getDayOfMonth());
        return String.format(
            "It is ordered that %s supervises the %s for %d months from the date of this order until %s.",
            getLocalAuthorityName(caseLocalAuthority),
            (numOfChildren == 1) ? "child" : "children",
            numOfMonths,
            dateFormatterService.formatLocalDateTimeBaseUsingFormat(orderExpiration,
            "h:mma 'on the' d'" + dayOrdinalSuffix + "' MMMM y"));
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

    private boolean hasInterimSubtype(OrderTypeAndDocument typeAndDoc) {
        return typeAndDoc.getType() != BLANK_ORDER && typeAndDoc.getSubtype() != null
            && typeAndDoc.getSubtype() == INTERIM;
    }
}
