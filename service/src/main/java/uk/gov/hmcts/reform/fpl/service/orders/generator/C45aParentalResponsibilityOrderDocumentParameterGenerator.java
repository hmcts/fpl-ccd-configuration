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
import uk.gov.hmcts.reform.fpl.service.orders.generator.common.OrderMessageGenerator;

import java.util.List;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C45A_PARENTAL_RESPONSIBILITY_ORDER;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE_TIME;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class C45aParentalResponsibilityOrderDocumentParameterGenerator implements DocmosisParameterGenerator {

    private final ChildrenService childrenService;
    private final AppointedGuardianFormatter appointedGuardianFormatter;
    private final OrderMessageGenerator orderMessageGenerator;

    private static final Order ORDER = C45A_PARENTAL_RESPONSIBILITY_ORDER;
    private static String ORDER_HEADER = "Order header \n";
    private static String ORDER_MESSAGE = "Order Message";
    private static String NOTICE_HEADER = "Notice \n";
    private static String NOTICE_MESSAGE =
        "A parental responsibility order can only end\n"
            + "\n"
            + "a) When the child reaches 18 years\n"
            + "b) By order of the court made\n"
            + "\t- on the application of any person who has parental responsibility\n"
            + "\t- with leave of the court on the application of the child\n";

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

        List<Element<Child>> selectedChildren = childrenService.getSelectedChildren(caseData);

        return C43aSpecialGuardianshipOrderDocmosisParameters.builder()
            .orderTitle(ORDER.getTitle())
            .dateOfIssue(formatLocalDateTimeBaseUsingFormat(eventData.getManageOrdersApprovalDateTime(), DATE_TIME))
            .furtherDirections(eventData.getManageOrdersFurtherDirections())
            .orderDetails(getParentalResponsibilityMessage(caseData, selectedChildren.size()))
            .orderByConsent(orderMessageGenerator.getOrderByConsentMessage(eventData))
            .orderHeader(ORDER_HEADER)
            .orderMessage(ORDER_MESSAGE)
            .noticeHeader(NOTICE_HEADER)
            .noticeMessage(NOTICE_MESSAGE)
            .build();
    }

    private String getParentalResponsibilityMessage(CaseData caseData, int numOfChildren) {
        String childOrChildren = (numOfChildren == 1 ? "child" : "children");
        String applicant = appointedGuardianFormatter.getGuardiansNamesForDocument(caseData);

        return format("The Court orders that %s "
            + "shall have parental responsibility for the %s.", applicant, childOrChildren);
    }
}
