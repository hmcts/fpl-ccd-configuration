package uk.gov.hmcts.reform.fpl.service.orders.generator;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.service.AppointedGuardianFormatter;
import uk.gov.hmcts.reform.fpl.service.ChildrenService;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.C43aSpecialGuardianshipOrderDocmosisParameters;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.DocmosisParameters;

import java.util.List;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE_TIME;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class C43aSpecialGuardianshipOrderDocumentParameterGenerator implements DocmosisParameterGenerator {

    private final ChildrenService childrenService;
    private final AppointedGuardianFormatter appointedGuardianFormatter;

    private static String paragraphBreak = "\n \n";
    private static String ORDER_HEADER = "Warning \n";
    private static String ORDER_MESSAGE = "Where a Special Guardianship Order is in force no person may "
        + "cause the child to be known by a new surname or remove the "
        + "child from the United Kingdom without either the written consent"
        + " of every person who has parental responsibility for the child or "
        + "the leave of the court. "
        + "However, this does not prevent the removal "
        + "of a child for a period of less than 3 months, "
        + "by its special guardian(s) (Section 14C (3) and (4) Children Act 1989)."
        + paragraphBreak
        + "It may be a criminal offence under the Child Abduction Act 1984 "
        + "to remove the child from the United Kingdom without leave of the court.\n"
        + "";
    private static String NOTICE_HEADER = "Notice \n";
    private static String NOTICE_MESSAGE = "Any person with parental responsibility for a child may "
        + "obtain advice on what can be done to prevent the issue of a passport to the child. They should write "
        + "to The United Kingdom Passport Agency, Globe House, 89 Eccleston Square, LONDON, SW1V 1PN.";

    @Override
    public Order accept() {
        return Order.C43A_SPECIAL_GUARDIANSHIP_ORDER;
    }

    @Override
    public DocmosisTemplates template() {
        return DocmosisTemplates.ORDER_V2;
    }

    @Override
    public DocmosisParameters generate(CaseData caseData) {
        ManageOrdersEventData eventData = caseData.getManageOrdersEventData();

        List<Element<Child>> selectedChildren = childrenService.getSelectedChildren(caseData);

        return C43aSpecialGuardianshipOrderDocmosisParameters.builder()
            .orderTitle(Order.C43A_SPECIAL_GUARDIANSHIP_ORDER.getTitle())
            .dateOfIssue(formatLocalDateTimeBaseUsingFormat(eventData.getManageOrdersApprovalDateTime(), DATE_TIME))
            .furtherDirections(eventData.getManageOrdersFurtherDirections())
            .orderDetails(getSpecialGuardianAppointeeMessage(caseData, selectedChildren.size()))
            .orderByConsent(getOrderByConsentMessage(eventData))
            .orderHeader(ORDER_HEADER)
            .orderMessage(ORDER_MESSAGE)
            .noticeHeader(NOTICE_HEADER)
            .noticeMessage(NOTICE_MESSAGE)
            .build();
    }

    private String getSpecialGuardianAppointeeMessage(CaseData caseData, int numOfChildren) {
        String childOrChildren = (numOfChildren == 1 ? "child" : "children");
        String applicant = appointedGuardianFormatter.getGuardiansNamesForDocument(caseData);

        return format("The Court orders that %s appointed as special guardian for the %s.", applicant, childOrChildren);
    }

    private String getOrderByConsentMessage(ManageOrdersEventData manageOrdersEventData) {
        if ("Yes".equals(manageOrdersEventData.getManageOrdersIsByConsent())) {
            return "By consent";
        }
        return null;
    }
}
