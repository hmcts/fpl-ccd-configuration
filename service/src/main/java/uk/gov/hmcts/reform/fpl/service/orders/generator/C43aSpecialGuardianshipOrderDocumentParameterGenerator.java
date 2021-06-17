package uk.gov.hmcts.reform.fpl.service.orders.generator;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates;
import uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.service.ChildrenService;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.C43aSpecialGuardianshipOrderDocmosisParameters;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.DocmosisParameters;

import java.util.List;

import static java.lang.String.format;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class C43aSpecialGuardianshipOrderDocumentParameterGenerator implements DocmosisParameterGenerator {

    private final ChildrenService childrenService;
    private static String paragraphBreak = "\n \n";

    private static String WARNING_MESSAGE = "Where a Special Guardianship Order is in force no person may "
        + "cause the child to be known by a new surname or remove the "
        + "child from the United Kingdom without either the written consent"
        + " of every person who has parental responsibility for the child or "
        + "the leave of the court. "
        + paragraphBreak
        + "However, this does not prevent the removal "
        + "of a child for a period of less than 3 months, "
        + "by its special guardian(s) (Section 14C (3) and (4) Children Act 1989)."
        + paragraphBreak
        + "It may be a criminal offence under the Child Abduction Act 1984 "
        + "to remove the child from the United Kingdom without leave of the court.";

    @Override
    public Order accept() {
        return Order.C43A_SPECIAL_GUARDIANSHIP_ORDER;
    }

    @Override
    public DocmosisTemplates template() {
        return DocmosisTemplates.SGO;
    }

    @Override
    public DocmosisParameters generate(CaseData caseData) {
        ManageOrdersEventData manageOrdersEventData = caseData.getManageOrdersEventData();

        List<Element<Child>> selectedChildren = childrenService.getSelectedChildren(caseData);

        return C43aSpecialGuardianshipOrderDocmosisParameters.builder()
            .orderTitle(Order.C43A_SPECIAL_GUARDIANSHIP_ORDER.getTitle())
            .orderType(GeneratedOrderType.SPECIAL_GUARDIANSHIP_ORDER)
            .furtherDirections(manageOrdersEventData.getManageOrdersFurtherDirections())
            .warningMessage(WARNING_MESSAGE)
            .orderDetails(getSpecialGuardianAppointeeMessage(selectedChildren.size(), 1))
            .orderByConsent(getOrderByConsentMessage(manageOrdersEventData))
            .build();
    }

    private String getSpecialGuardianAppointeeMessage(int numOfChildren, int numOfApplicants) {
        String childOrChildren = (numOfChildren == 1 ? "child" : "children");
        String[] applicants = {"Applicant1"};
        String basicMessage = format("The Court orders %s (see text for both options below) \n \n ", applicants[0]);

        if (numOfApplicants > 1) {
            return basicMessage + multipleApplicantMessage(childOrChildren);
        } else {
            return basicMessage + singleApplicantMessage(childOrChildren);
        }
    }

    private String singleApplicantMessage(String childOrChildren) {
        return format("[Applicant name] is appointed as Special Guardian for the %s.", childOrChildren);
    }

    private String multipleApplicantMessage(String childOrChildren) {
        return format("[Applicant names comma separated] are appointed as Special Guardians for the %s",
            childOrChildren);
    }

    private String getOrderByConsentMessage(ManageOrdersEventData manageOrdersEventData) {
        if ("Yes".equals(manageOrdersEventData.getManageOrdersIsByConsent())) {
            return "By consent";
        }
        return null;
    }
}
