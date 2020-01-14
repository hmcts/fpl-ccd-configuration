package uk.gov.hmcts.reform.fpl.service;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.config.HmctsCourtLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.EPOChildren;
import uk.gov.hmcts.reform.fpl.model.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.model.OrderTypeAndDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper;

import java.time.LocalDateTime;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.google.common.collect.Iterables.getLast;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.BLANK_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.CARE_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.EMERGENCY_PROTECTION_ORDER;

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
     * Currently adds/formats the order title and details based on the type (may be more fields in future orders)
     * Always adds order type, document, {@link JudgeAndLegalAdvisor} object and a formatted order date.
     *
     * @param typeAndDocument      the type of the order and the order document (document only shown in check answers)
     * @param order                this value will contain fixed details and document values as well as customisable
     *                             values.
     * @param judgeAndLegalAdvisor the judge and legal advisor for the order.
     * @return Element containing randomUUID and a fully populated order.
     */
    public Element<GeneratedOrder> buildCompleteOrder(OrderTypeAndDocument typeAndDocument,
                                                      GeneratedOrder order,
                                                      JudgeAndLegalAdvisor judgeAndLegalAdvisor) {
        GeneratedOrder generatedOrder = defaultIfNull(order, GeneratedOrder.builder().build());
        GeneratedOrder.GeneratedOrderBuilder orderBuilder = GeneratedOrder.builder();

        //Scalable for future types of orders which may have additional fields
        switch (typeAndDocument.getType()) {
            case BLANK_ORDER:
                orderBuilder.title(defaultIfBlank(generatedOrder.getTitle(), "Order"));
                orderBuilder.details(generatedOrder.getDetails());
                break;
            case CARE_ORDER:
                orderBuilder.title(null);
                break;
            default:
        }

        return Element.<GeneratedOrder>builder()
            .id(randomUUID())
            .value(orderBuilder
                .type(typeAndDocument.getType())
                .document(typeAndDocument.getDocument())
                .judgeAndLegalAdvisor(judgeAndLegalAdvisor)
                .date(dateFormatterService.formatLocalDateTimeBaseUsingFormat(time.now(),
                    "h:mma, d MMMM yyyy"))
                .build())
            .build();
    }

    public Map<String, Object> getOrderTemplateData(CaseData caseData) {
        ImmutableMap.Builder<String, Object> orderTemplateBuilder = new ImmutableMap.Builder<>();

        //Scalable for future order types
        switch (caseData.getOrderTypeAndDocument().getType()) {
            case BLANK_ORDER:
                orderTemplateBuilder
                    .put("orderType", BLANK_ORDER)
                    .put("orderTitle", defaultIfNull(caseData.getOrder().getTitle(), "Order"))
                    .put("childrenAct", "Children Act 1989")
                    .put("orderDetails", caseData.getOrder().getDetails());
                break;
            case CARE_ORDER:
                orderTemplateBuilder
                    .put("orderType", CARE_ORDER)
                    .put("orderTitle", "Care order")
                    .put("childrenAct", "Section 31 Children Act 1989")
                    .put("orderDetails", careOrderDetails(getChildrenDetails(caseData).size(),
                        caseData.getCaseLocalAuthority()));
                break;
            case EMERGENCY_PROTECTION_ORDER:
                orderTemplateBuilder
                    .put("orderType", EMERGENCY_PROTECTION_ORDER)
                    .put("localAuthorityName", getLocalAuthorityName(caseData.getCaseLocalAuthority()))
                    .put("childrenDescription", getChildrenDescription(caseData.getEpoChildren()))
                    .put("epoType", caseData.getEpoType())
                    .put("includePhrase", caseData.getEpoPhrase())
                    .put("removalAddress", getFormattedRemovalAddress(caseData))
                    .put("childrenCount", caseData.getChildren1().size())
                    .put("epoStartDateTime", formatEPODateTime(time.now()))
                    .put("epoEndDateTime", formatEPODateTime(caseData.getEpoEndDate()));
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
            .put("furtherDirections", caseData.getFurtherDirectionsText())
            .build();

        return orderTemplateBuilder.build();
    }

    public String generateOrderDocumentFileName(String type) {
        return type.toLowerCase().replaceAll("[()]", "").replaceAll("[ ]", "_") + ".pdf";
    }

    public String getMostRecentUploadedOrderDocumentUrl(final List<Element<GeneratedOrder>> orders) {
        return getLast(orders.stream()
            .filter(Objects::nonNull)
            .map(Element::getValue)
            .filter(Objects::nonNull)
            .collect(toList()))
            .getDocument().getBinaryUrl();
    }

    public CaseDetails removeOrderProperties(CaseDetails caseDetails) {
        caseDetails.getData().remove("epoRemovalAddress");
        caseDetails.getData().remove("epoChildren");
        caseDetails.getData().remove("epoEndDate");
        caseDetails.getData().remove("epoPhrase");
        caseDetails.getData().remove("epoType");
        caseDetails.getData().remove("orderTypeAndDocument");
        caseDetails.getData().remove("order");
        caseDetails.getData().remove("judgeAndLegalAdvisor");
        caseDetails.getData().remove("orderFurtherDirections");

        return caseDetails;
    }

    private String getCourtName(String courtName) {
        return hmctsCourtLookupConfiguration.getCourt(courtName).getName();
    }

    private String getLocalAuthorityName(String caseLocalAuthority) {
        return localAuthorityNameLookupConfiguration.getLocalAuthorityName(caseLocalAuthority);
    }

    private String careOrderDetails(int numOfChildren, String caseLocalAuthority) {
        String childOrChildren = (numOfChildren == 1 ? "child is " : "children are ");
        return "It is ordered that the " + childOrChildren + "placed in the care of " + getLocalAuthorityName(
            caseLocalAuthority) + ".";
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

    private String getChildrenDescription(EPOChildren epoChildren) {
        if (epoChildren.getDescriptionNeeded().equals("Yes")) {
            return epoChildren.getDescription();
        }

        return "";
    }

    private String formatEPODateTime(LocalDateTime dateTime) {
        return dateFormatterService.formatLocalDateTimeBaseUsingFormat(dateTime, "d MMMM yyyy 'at' h:mma");
    }

    private String getFormattedRemovalAddress(CaseData caseData) {
        if (caseData.getEpoRemovalAddress() != null) {
            return caseData.getEpoRemovalAddress().getAddressAsString();
        }

        return "";
    }
}
