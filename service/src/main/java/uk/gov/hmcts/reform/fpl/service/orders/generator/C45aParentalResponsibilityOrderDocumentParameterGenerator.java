package uk.gov.hmcts.reform.fpl.service.orders.generator;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates;
import uk.gov.hmcts.reform.fpl.enums.RelationshipWithChild;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.service.ChildrenService;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.C45aParentalResponsibilityOrderDocmosisParameters;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.DocmosisParameters;
import uk.gov.hmcts.reform.fpl.service.orders.generator.common.OrderMessageGenerator;

import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.fpl.enums.RelationshipWithChild.FATHER;
import static uk.gov.hmcts.reform.fpl.enums.RelationshipWithChild.SECOND_FEMALE_PARENT;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C45A_PARENTAL_RESPONSIBILITY_ORDER;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE_TIME;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class C45aParentalResponsibilityOrderDocumentParameterGenerator implements DocmosisParameterGenerator {

    private final ChildrenService childrenService;
    private final OrderMessageGenerator orderMessageGenerator;

    private static final Order ORDER = C45A_PARENTAL_RESPONSIBILITY_ORDER;
    private static final String NOTICE_HEADER = "Notice \n";
    private static final String NOTICE_MESSAGE = "A parental responsibility order can only end\n \n"
            + "a) When the child reaches 18 years\n"
            + "b) By order of the court made\n"
            + "      * on the application of any person who has parental responsibility\n"
            + "      * with leave of the court on the application of the child\n";
    private static final Map<RelationshipWithChild, String> ORDER_ACT = Map.of(
        FATHER, "Section 4(1) Children Act 1989",
        SECOND_FEMALE_PARENT, "Section 4ZA Children Act 1989"
    );

    @Override
    public Order accept() {
        return ORDER;
    }

    @Override
    public DocmosisTemplates template() {
        return DocmosisTemplates.ORDER_V2;
    }

    @Override
    public DocmosisParameters generate(CaseData caseData) {
        ManageOrdersEventData eventData = caseData.getManageOrdersEventData();

        RelationshipWithChild relationship = eventData.getManageOrdersRelationshipWithChild();
        String orderAct = ORDER_ACT.get(relationship);

        List<Element<Child>> selectedChildren = childrenService.getSelectedChildren(caseData);

        return C45aParentalResponsibilityOrderDocmosisParameters.builder()
            .orderTitle(ORDER.getTitle())
            .childrenAct(orderAct)
            .dateOfIssue(formatLocalDateTimeBaseUsingFormat(eventData.getManageOrdersApprovalDateTime(), DATE_TIME))
            .furtherDirections(eventData.getManageOrdersFurtherDirections())
            .orderDetails(getParentalResponsibilityMessage(caseData, selectedChildren.size()))
            .orderByConsent(orderMessageGenerator.getOrderByConsentMessage(eventData))
            .noticeHeader(NOTICE_HEADER)
            .noticeMessage(NOTICE_MESSAGE)
            .build();
    }

    private String getParentalResponsibilityMessage(CaseData caseData, int numOfChildren) {
        ManageOrdersEventData eventData = caseData.getManageOrdersEventData();
        String responsibleParty = eventData.getManageOrdersParentResponsible();
        String childOrChildren = (numOfChildren == 1 ? "child" : "children");

        return format("The Court orders that %s "
            + "shall have parental responsibility for the %s.", responsibleParty, childOrChildren);
    }
}
