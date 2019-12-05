package uk.gov.hmcts.reform.fpl.service;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.config.HmctsCourtLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.C21Order;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
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
public class CreateC21OrderService {
    private final DateFormatterService dateFormatterService;
    private final HmctsCourtLookupConfiguration hmctsCourtLookupConfiguration;
    private final LocalAuthorityNameLookupConfiguration localAuthorityNameLookupConfiguration;
    private final Time time;

    public CreateC21OrderService(DateFormatterService dateFormatterService,
                                 HmctsCourtLookupConfiguration hmctsCourtLookupConfiguration,
                                 LocalAuthorityNameLookupConfiguration localAuthorityNameLookupConfiguration,
                                 Time time) {
        this.dateFormatterService = dateFormatterService;
        this.hmctsCourtLookupConfiguration = hmctsCourtLookupConfiguration;
        this.localAuthorityNameLookupConfiguration = localAuthorityNameLookupConfiguration;
        this.time = time;
    }

    public C21Order addTypeAndDocumentToOrder(C21Order c21Order, OrderTypeAndDocument typeAndDocument) {
        C21Order c21 = defaultIfNull(c21Order, C21Order.builder().build());
        return c21.toBuilder()
            .type(typeAndDocument.getFinalOrderType())
            .document(typeAndDocument.getDocument())
            .build();
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
     * Method to format title of order, add {@link JudgeAndLegalAdvisor} object and a formatted order date.
     *
     * @param c21Order             this value will contain fixed details and document values as well as customisable
     *                             values.
     * @param judgeAndLegalAdvisor the judge and legal advisor for the order.
     * @return Element containing randomUUID and a fully populated C21Order.
     */
    public Element<C21Order> addCustomValuesToC21Order(C21Order c21Order,
                                                       OrderTypeAndDocument typeAndDocument,
                                                       JudgeAndLegalAdvisor judgeAndLegalAdvisor) {
        C21Order c21 = defaultIfNull(c21Order, C21Order.builder().build());
        C21Order.C21OrderBuilder orderBuilder = C21Order.builder();

        //Scalable for future types of orders which may have additional fields
        switch (typeAndDocument.getFinalOrderType()) {
            case BLANK_ORDER:
                orderBuilder.orderTitle(defaultIfBlank(c21.getOrderTitle(), "Order"));
                break;
            case CARE_ORDER:
                orderBuilder.orderTitle(null);
                break;
        }

        return Element.<C21Order>builder()
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

    public Map<String, Object> getC21OrderTemplateData(CaseData caseData) {
        ImmutableMap.Builder<String, Object> orderTemplateBuilder = new ImmutableMap.Builder<>();

        switch (caseData.getOrderTypeAndDocument().getFinalOrderType()) {
            case BLANK_ORDER:
                orderTemplateBuilder
                    .put("orderType", BLANK_ORDER)
                    .put("orderTitle", defaultIfNull(caseData.getC21Order().getOrderTitle(), "Order"))
                    .put("childrenAct", "Section 31 Children Act 1989")
                    .put("orderDetails", caseData.getC21Order().getOrderDetails());

                break;
            case CARE_ORDER:
                orderTemplateBuilder
                    .put("orderType", CARE_ORDER)
                    .put("orderTitle", "Care Order")
                    .put("childrenAct", "Children Act 1989")
                    .put("orderDetails", careOrderDetails(getChildrenDetails(caseData).size(),
                        caseData.getCaseLocalAuthority()));
                break;
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

    public String mostRecentUploadedC21DocumentUrl(final List<Element<C21Order>> c21Orders) {
        return getLast(c21Orders.stream()
            .filter(Objects::nonNull)
            .map(Element::getValue)
            .filter(Objects::nonNull)
            .collect(toList()))
            .getDocument().getBinaryUrl();
    }
}
