package uk.gov.hmcts.reform.fpl.service;

import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.config.HmctsCourtLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.GeneratedEPOKey;
import uk.gov.hmcts.reform.fpl.enums.GeneratedOrderKey;
import uk.gov.hmcts.reform.fpl.enums.GeneratedOrderSubtype;
import uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType;
import uk.gov.hmcts.reform.fpl.enums.InterimOrderKey;
import uk.gov.hmcts.reform.fpl.enums.OrderStatus;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.OrderTypeAndDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.emergencyprotectionorder.EPOChildren;
import uk.gov.hmcts.reform.fpl.model.order.generated.FurtherDirections;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.model.order.generated.InterimEndDate;
import uk.gov.hmcts.reform.fpl.model.order.selector.ChildSelector;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.FormatStyle;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.google.common.collect.Iterables.getLast;
import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisImages.COURT_SEAL;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisImages.CREST;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisImages.DRAFT_WATERMARK;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderSubtype.FINAL;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderSubtype.INTERIM;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.BLANK_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.DRAFT;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.SEALED;
import static uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.InterimEndDateType.END_OF_PROCEEDINGS;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE_TIME_AT;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE_WITH_ORDINAL_SUFFIX;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.TIME_DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.getDayOfMonthSuffix;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

// REFACTOR: 27/01/2020 Extract docmosis logic into a new service that extends DocmosisTemplateDataGeneration

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class GeneratedOrderService {
    private static final String ORDER_TITLE = "orderTitle";
    private static final String CHILDREN_ACT = "childrenAct";
    private static final String ORDER_DETAILS = "orderDetails";
    private static final String CHILDREN = "children";
    private final HmctsCourtLookupConfiguration hmctsCourtLookupConfiguration;
    private final LocalAuthorityNameLookupConfiguration localAuthorityNameLookupConfiguration;
    private final ChildrenService childrenService;
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

    /**
     * Method to populate the order based on type of order selected
     * Adds/formats the order title/details for C21 and the expiry date for supervision order
     * Always adds order type, document, {@link JudgeAndLegalAdvisor} object and a formatted order date.
     *
     * @param typeAndDocument      the type of the order and the order document (document only shown in check answers)
     * @param order                this value will contain fixed details and document values as well as customisable
     *                             values.
     * @param judgeAndLegalAdvisor the judge and legal advisor for the order.
     * @param orderMonths          the number of months the supervision order is valid
     * @param interimEndDate       the end date wrapper for an interim order
     * @return Element containing randomUUID and a fully populated order, ready to be added to orderCollection.
     */
    public Element<GeneratedOrder> buildCompleteOrder(OrderTypeAndDocument typeAndDocument,
                                                      GeneratedOrder order,
                                                      JudgeAndLegalAdvisor judgeAndLegalAdvisor,
                                                      LocalDate dateOfIssue,
                                                      Integer orderMonths,
                                                      InterimEndDate interimEndDate) {
        GeneratedOrder generatedOrder = defaultIfNull(order, GeneratedOrder.builder().build());
        GeneratedOrder.GeneratedOrderBuilder orderBuilder = GeneratedOrder.builder();

        GeneratedOrderType orderType = typeAndDocument.getType();

        String expiryDate = null;

        switch (orderType) {
            case BLANK_ORDER:
                orderBuilder.title(defaultIfBlank(generatedOrder.getTitle(), "Order"))
                    .details(generatedOrder.getDetails());
                break;
            case CARE_ORDER:
                orderBuilder.title(null);
                if (typeAndDocument.getSubtype() == INTERIM) {
                    requireNonNull(interimEndDate);
                    expiryDate = getInterimExpiryDate(interimEndDate);
                }
                break;
            case SUPERVISION_ORDER:
                orderBuilder.title(null);
                expiryDate = getSupervisionOrderExpiryDate(typeAndDocument, orderMonths, interimEndDate);
                break;
            default:
        }

        orderBuilder.expiryDate(expiryDate)
            .dateOfIssue(formatLocalDateToString(dateOfIssue, DATE))
            .type(typeAndDocument.getFullType(typeAndDocument.getSubtype()))
            .document(typeAndDocument.getDocument())
            .judgeAndLegalAdvisor(judgeAndLegalAdvisor)
            .date(formatLocalDateTimeBaseUsingFormat(time.now(), TIME_DATE));

        return Element.<GeneratedOrder>builder()
            .id(randomUUID())
            .value(orderBuilder.build())
            .build();
    }

    public Map<String, Object> getOrderTemplateData(CaseData caseData,
                                                    OrderStatus orderStatus,
                                                    JudgeAndLegalAdvisor judgeAndLegalAdvisor) {
        ImmutableMap.Builder<String, Object> orderTemplateBuilder = new ImmutableMap.Builder<>();

        OrderTypeAndDocument orderTypeAndDocument = caseData.getOrderTypeAndDocument();
        InterimEndDate interimEndDate = caseData.getInterimEndDate();
        GeneratedOrderType orderType = orderTypeAndDocument.getType();
        GeneratedOrderSubtype subtype = orderTypeAndDocument.getSubtype();

        List<Child> children = getSelectedChildren(unwrapElements(caseData.getAllChildren()),
            caseData.getChildSelector(), caseData.getOrderAppliesToAllChildren());
        List<Map<String, String>> childrenDetails = getChildrenDetails(children);
        int childrenCount = children.size();

        switch (orderType) {
            case BLANK_ORDER:
                orderTemplateBuilder
                    .put(ORDER_TITLE, defaultIfNull(caseData.getOrder().getTitle(), "Order"))
                    .put(CHILDREN_ACT, "Children Act 1989")
                    .put(ORDER_DETAILS, caseData.getOrder().getDetails());
                break;
            case CARE_ORDER:
                if (subtype == INTERIM) {
                    orderTemplateBuilder
                        .put(ORDER_TITLE, orderTypeAndDocument.getFullType(INTERIM))
                        .put(CHILDREN_ACT, "Section 38 Children Act 1989");
                } else if (subtype == FINAL) {
                    orderTemplateBuilder
                        .put(ORDER_TITLE, orderTypeAndDocument.getFullType())
                        .put(CHILDREN_ACT, "Section 31 Children Act 1989");
                }
                orderTemplateBuilder
                    .put("localAuthorityName", getLocalAuthorityName(caseData.getCaseLocalAuthority()))
                    .put(ORDER_DETAILS, getFormattedCareOrderDetails(childrenCount,
                        caseData.getCaseLocalAuthority(), orderTypeAndDocument.isInterim(), interimEndDate));
                break;
            case SUPERVISION_ORDER:
                if (subtype == INTERIM) {
                    orderTemplateBuilder
                        .put(ORDER_TITLE, orderTypeAndDocument.getFullType(INTERIM))
                        .put(CHILDREN_ACT, "Section 38 and Paragraphs 1 and 2 Schedule 3 Children Act 1989")
                        .put(ORDER_DETAILS,
                            getFormattedInterimSupervisionOrderDetails(childrenCount,
                                caseData.getCaseLocalAuthority(), interimEndDate));
                } else {
                    orderTemplateBuilder
                        .put(ORDER_TITLE, orderTypeAndDocument.getFullType())
                        .put(CHILDREN_ACT, "Section 31 and Paragraphs 1 and 2 Schedule 3 Children Act 1989")
                        .put(ORDER_DETAILS,
                            getFormattedFinalSupervisionOrderDetails(childrenCount,
                                caseData.getCaseLocalAuthority(), caseData.getOrderMonths()));
                }
                break;
            case EMERGENCY_PROTECTION_ORDER:
                orderTemplateBuilder
                    .put("localAuthorityName", getLocalAuthorityName(caseData.getCaseLocalAuthority()))
                    .put("childrenDescription", getChildrenDescription(caseData.getEpoChildren()))
                    .put("epoType", caseData.getEpoType())
                    .put("includePhrase", caseData.getEpoPhrase().getIncludePhrase())
                    .put("removalAddress", getFormattedRemovalAddress(caseData))
                    .put("epoStartDateTime", formatEPODateTime(time.now()))
                    .put("epoEndDateTime", formatEPODateTime(caseData.getEpoEndDate()));
                break;
            default:
                throw new UnsupportedOperationException("Unexpected value: " + orderType);
        }

        orderTemplateBuilder
            .put("orderType", orderType)
            .put("familyManCaseNumber", caseData.getFamilyManCaseNumber())
            .put("courtName", getCourtName(caseData.getCaseLocalAuthority()))
            .put("dateOfIssue", formatLocalDateToString(caseData.getDateOfIssue(), DATE))
            .put("judgeTitleAndName", JudgeAndLegalAdvisorHelper.formatJudgeTitleAndName(
                judgeAndLegalAdvisor))
            .put("legalAdvisorName", JudgeAndLegalAdvisorHelper.getLegalAdvisorName(judgeAndLegalAdvisor))
            .put(CHILDREN, childrenDetails)
            .put("childrenCount", childrenCount)
            .put("furtherDirections", caseData.getFurtherDirectionsText())
            .put("crest", CREST.getValue())
            .build();

        if (orderStatus == DRAFT) {
            orderTemplateBuilder.put("draftbackground", DRAFT_WATERMARK.getValue());
        }

        if (orderStatus == SEALED) {
            orderTemplateBuilder.put("courtseal", COURT_SEAL.getValue());
        }

        return orderTemplateBuilder.build();
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

    public JudgeAndLegalAdvisor getAllocatedJudgeFromMostRecentOrder(CaseData caseData) {
        Optional<Element<GeneratedOrder>> generatedOrder = caseData.getOrderCollection()
            .stream().reduce((first, last) -> last);

        return generatedOrder
            .map(Element::getValue)
            .map(GeneratedOrder::getJudgeAndLegalAdvisor)
            .orElse(JudgeAndLegalAdvisor.builder().build());
    }

    private String getSupervisionOrderExpiryDate(OrderTypeAndDocument typeAndDocument, Integer orderMonths,
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
        return interimEndDate.toLocalDateTime()
            .map(dateTime -> formatLocalDateTimeBaseUsingFormat(dateTime, TIME_DATE))
            .orElse(END_OF_PROCEEDINGS.getLabel());
    }

    private String getCourtName(String courtName) {
        return hmctsCourtLookupConfiguration.getCourt(courtName).getName();
    }

    private String getLocalAuthorityName(String caseLocalAuthority) {
        return localAuthorityNameLookupConfiguration.getLocalAuthorityName(caseLocalAuthority);
    }

    private String getFormattedCareOrderDetails(int numOfChildren,
                                                String caseLocalAuthority,
                                                boolean isInterim,
                                                InterimEndDate interimEndDate) {
        String childOrChildren = (numOfChildren == 1 ? "child is" : "children are");
        return String.format("It is ordered that the %s placed in the care of %s%s.",
            childOrChildren, getLocalAuthorityName(caseLocalAuthority),
            isInterim ? " until " + getInterimEndDateString(interimEndDate) : "");
    }

    private String getFormattedInterimSupervisionOrderDetails(int numOfChildren, String caseLocalAuthority,
                                                              InterimEndDate interimEndDate) {
        return String.format("It is ordered that %s supervises the %s until %s.",
            getLocalAuthorityName(caseLocalAuthority),
            (numOfChildren == 1) ? "child" : CHILDREN,
            getInterimEndDateString(interimEndDate));
    }

    private String getInterimEndDateString(InterimEndDate interimEndDate) {
        return interimEndDate.toLocalDateTime()
            .map(dateTime -> {
                final String dayOrdinalSuffix = getDayOfMonthSuffix(dateTime.getDayOfMonth());
                return formatLocalDateTimeBaseUsingFormat(
                    dateTime, String.format(DATE_WITH_ORDINAL_SUFFIX, dayOrdinalSuffix));
            })
            .orElse("the end of the proceedings");
    }

    private String getFormattedFinalSupervisionOrderDetails(int numOfChildren,
                                                            String caseLocalAuthority,
                                                            int numOfMonths) {
        final LocalDateTime orderExpiration = time.now().plusMonths(numOfMonths);
        final String dayOrdinalSuffix = getDayOfMonthSuffix(orderExpiration.getDayOfMonth());
        return String.format(
            "It is ordered that %s supervises the %s for %d months from the date of this order until %s.",
            getLocalAuthorityName(caseLocalAuthority),
            (numOfChildren == 1) ? "child" : CHILDREN,
            numOfMonths,
            formatLocalDateTimeBaseUsingFormat(orderExpiration,
                String.format(DATE_WITH_ORDINAL_SUFFIX, dayOrdinalSuffix)));
    }

    private List<Child> getSelectedChildren(List<Child> allChildren, ChildSelector selector, String choice) {
        if (useAllChildren(choice)) {
            return allChildren;
        }

        return selector.getSelected().stream()
            .map(allChildren::get)
            .collect(Collectors.toList());
    }

    private List<Map<String, String>> getChildrenDetails(List<Child> children) {
        return children.stream()
            .map(Child::getParty)
            .map(child -> ImmutableMap.of(
                "name", child.getFirstName() + " " + child.getLastName(),
                "gender", defaultIfNull(child.getGender(), ""),
                "dateOfBirth", child.getDateOfBirth() != null ? formatLocalDateToString(
                    child.getDateOfBirth(), FormatStyle.LONG) : ""))
            .collect(toList());
    }

    private boolean useAllChildren(String choice) {
        // If there is only one child in the case then the choice will be null
        return choice == null || "Yes".equals(choice);
    }

    private String getChildrenDescription(EPOChildren epoChildren) {
        if ("Yes".equals(epoChildren.getDescriptionNeeded())) {
            return epoChildren.getDescription();
        }

        return "";
    }

    private String formatEPODateTime(LocalDateTime dateTime) {
        return formatLocalDateTimeBaseUsingFormat(dateTime, DATE_TIME_AT);
    }

    private String getFormattedRemovalAddress(CaseData caseData) {
        return Optional.ofNullable(caseData.getEpoRemovalAddress())
            .map(address -> address.getAddressAsString(", ")).orElse("");
    }


    /**
     * Determine if the service should generate the draft order document.
     *
     * <p>Will return {@code true} if one of the following is met:
     * <ul>
     *     <li>the order is a blank order</li>
     *     <li>further directions is not null and one of the following is met:<ul>
     *         <li>not all children have a final order (can't close the case)</li>
     *         <li>closeCaseFromOrder is not null (close case decision has been made)</li>
     *         <li>close case is not enabled</li>
     *     </ul></li>
     * </ul>
     *
     * @param orderType          type of order
     * @param furtherDirections  further directions for the order
     * @param children           children in the case
     * @param closeCaseFromOrder YesOrNo field for close case from order
     * @param closeCaseEnabled   feature toggle flag for close case
     */
    public boolean shouldGenerateDocument(OrderTypeAndDocument orderType,
                                          FurtherDirections furtherDirections,
                                          List<Element<Child>> children,
                                          String closeCaseFromOrder,
                                          boolean closeCaseEnabled) {
        return BLANK_ORDER == orderType.getType()
            || furtherDirections != null
            && (!childrenService.allChildrenHaveFinalOrder(children) || closeCaseFromOrder != null
            || !closeCaseEnabled);
    }


    /**
     * Determine if the user should see the close case page.
     *
     * <p>Will return {@code true} if all of the following are met:
     * <ul>
     *     <li>close case is enabled</li>
     *     <li>the order type is final or epo</li>
     *     <li>all children will be marked to have a final order issued against them</li>
     *     <li>the flag hasn't already been set</li>
     * </ul>
     *
     * @param orderType          type of order
     * @param closeCaseFromOrder YesOrNo field for close case from order
     * @param children           list of children in the case
     * @param closeCaseEnabled   feature toggle flag for close case
     */
    public boolean showCloseCase(OrderTypeAndDocument orderType,
                                 String closeCaseFromOrder,
                                 List<Element<Child>> children,
                                 boolean closeCaseEnabled) {
        return closeCaseEnabled
            && orderType.isClosable()
            && childrenService.allChildrenHaveFinalOrder(children)
            && closeCaseFromOrder == null;
    }
}
