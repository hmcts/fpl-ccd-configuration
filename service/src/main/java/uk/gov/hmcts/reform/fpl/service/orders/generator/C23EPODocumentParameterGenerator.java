package uk.gov.hmcts.reform.fpl.service.orders.generator;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates;
import uk.gov.hmcts.reform.fpl.enums.EPOType;
import uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.service.ChildrenService;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.C23EPODocmosisParameters;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.DocmosisParameters;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE_TIME_AT;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class C23EPODocumentParameterGenerator implements DocmosisParameterGenerator {

    private static final GeneratedOrderType TYPE = GeneratedOrderType.EMERGENCY_PROTECTION_ORDER;

    private final LocalAuthorityNameLookupConfiguration laNameLookup;
    private final ChildrenService childrenService;

    @Override
    public Order accept() {
        return Order.C23_EMERGENCY_PROTECTION_ORDER;
    }

    @Override
    public DocmosisParameters generate(CaseData caseData) {
        ManageOrdersEventData eventData = caseData.getManageOrdersEventData();

        String localAuthorityCode = caseData.getCaseLocalAuthority();
        String localAuthorityName = laNameLookup.getLocalAuthorityName(localAuthorityCode);

        List<Element<Child>> selectedChildren = childrenService.getSelectedChildren(caseData);

        return C23EPODocmosisParameters.builder()
            .orderType(TYPE)
            .furtherDirections(eventData.getManageOrdersFurtherDirections())
            .orderDetails(orderDetails(selectedChildren.size(), localAuthorityName))
            .localAuthorityName(localAuthorityName)
            .epoType(eventData.getManageOrdersEpoType())
            .includePhrase(eventData.getManageOrdersIncludePhrase())
            .childrenDescription(eventData.getManageOrdersChildrenDescription())
            .epoStartDateTime(formatDateTime(eventData.getManageOrdersApprovalDateTime()))
            .epoEndDateTime(formatDateTime(eventData.getManageOrdersEndDateTime()))
            .removalAddress(formatAddress(eventData.getManageOrdersEpoRemovalAddress()))
            .exclusionRequirement(buildExclusionRequirement(eventData))
            .build();
    }

    @Override
    public DocmosisTemplates template() {
        return DocmosisTemplates.EPO;
    }

    private String orderDetails(int numOfChildren, String caseLocalAuthority) {
        String childOrChildren = (numOfChildren == 1 ? "child is" : "children are");
        return format("It is ordered that the %s placed in the care of %s.", childOrChildren, caseLocalAuthority);
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return formatLocalDateTimeBaseUsingFormat(dateTime, DATE_TIME_AT);
    }

    private String formatAddress(Address removalAddress) {
        return Optional.ofNullable(removalAddress)
            .map(address -> address.getAddressAsString(", ")).orElse("");
    }

    private String buildExclusionRequirement(ManageOrdersEventData eventData) {
        if (eventData.getManageOrdersEpoType() == EPOType.REMOVE_TO_ACCOMMODATION
            || "No".equals(eventData.getManageOrdersExclusionRequirement())) {
            return null;
        }

        return String.format("The Court directs that %s be excluded from %s from %s so that the child may continue to live "
                + "there, consent to the exclusion requirement having been given by %s.",
            eventData.getManageOrdersWhoIsExcluded(),
            eventData.getManageOrdersEpoRemovalAddress(),
            formatLocalDateToString(eventData.getManageOrdersExclusionStartDate(), DATE),
            eventData.getManageOrdersWhoIsExcluded());
    }
}
