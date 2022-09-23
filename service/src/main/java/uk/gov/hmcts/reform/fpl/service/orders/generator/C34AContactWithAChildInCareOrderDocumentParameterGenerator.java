package uk.gov.hmcts.reform.fpl.service.orders.generator;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.selectors.ChildrenSmartSelector;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.C34aContactWithAChildInCareOrderDocmosisParameters;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.DocmosisParameters;

import java.util.List;
import java.util.Optional;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isEmpty;
@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class C34aContactWithAChildInCareOrderDocumentParameterGenerator implements DocmosisParameterGenerator {

    private final LocalAuthorityNameLookupConfiguration laNameLookup;
    private final ChildrenSmartSelector childrenSmartSelector;

    @Override
    public Order accept() {
        return Order.C34A_CONTACT_WITH_A_CHILD_IN_CARE;
    }

    private String getPeopleAllowedContactLabel(DynamicList dynamicList) {
        DynamicListElement dle = Optional.ofNullable(dynamicList == null ? null : dynamicList.getValue())
            .orElse(DynamicListElement.builder().label("").build());
        if (isEmpty(dle.getCode())) {
            return "";
        } else {
            return dle.getLabel();
        }
    }
    
    @Override
    public DocmosisParameters generate(CaseData caseData) {
        ManageOrdersEventData eventData = caseData.getManageOrdersEventData();

        String peopleAllowedContact = "";
        peopleAllowedContact += eventData.getManageOrdersAllowedContact1().getValue().getLabel() + "\n";
        String peopleAllowedContact2 = getPeopleAllowedContactLabel(eventData.getManageOrdersAllowedContact2());
        peopleAllowedContact += (isEmpty(peopleAllowedContact2) ? "" : (peopleAllowedContact2 + "\n"));
        String peopleAllowedContact3 = getPeopleAllowedContactLabel(eventData.getManageOrdersAllowedContact3());
        peopleAllowedContact += (isEmpty(peopleAllowedContact3) ? "" : (peopleAllowedContact3 + "\n"));

        List<Element<Child>> selectedChildren = childrenSmartSelector.getSelectedChildren(caseData);

        String localAuthorityCode = caseData.getCaseLocalAuthority();
        String localAuthorityName = laNameLookup.getLocalAuthorityName(localAuthorityCode);

        return C34aContactWithAChildInCareOrderDocmosisParameters.builder()
            .orderTitle(Order.C34A_CONTACT_WITH_A_CHILD_IN_CARE.getTitle())
            .childrenAct(Order.C34A_CONTACT_WITH_A_CHILD_IN_CARE.getChildrenAct())
            .orderMessage("The local authority is " + localAuthorityName + ".")
            .orderByConsent("Order By Consent")
            .orderDetails(orderDetails(selectedChildren.size(), peopleAllowedContact,
                eventData.getManageOrdersConditionsOfContact()))
            .noticeMessage("An authority may refuse to allow the contact that would otherwise be required by "
                + "virtue of Section 34(1) Children Act 1989 or an order under this section "
                + "if (a) they are satisfied that it is necessary to do so in order to safeguard or "
                + "promote the welfare of the [4]{child(ren)}; and (b) the refusal (i) is decided "
                + "upon as a matter of urgency; and (ii) does not last "
                + "for more than 7 days (Section 34(6) Children Act 1989).")
            .noticeHeader("Notice")
            .build();
    }

    @Override
    public DocmosisTemplates template() {
        return DocmosisTemplates.ORDER_V2;
    }

    private String orderDetails(int numOfChildren, String peopleAllowedContact, String conditionsOfContact) {
        String childOrChildren = (numOfChildren == 1 ? "child" : "children");
        return format("The Court orders that there may be contact between the %s and \n\n%s\n"
                + "The contact is subject to the following conditions\n\n%s",
            childOrChildren, peopleAllowedContact, conditionsOfContact);
    }
}
