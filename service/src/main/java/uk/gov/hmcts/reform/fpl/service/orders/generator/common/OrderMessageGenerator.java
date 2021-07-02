package uk.gov.hmcts.reform.fpl.service.orders.generator.common;

import lombok.RequiredArgsConstructor;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.ManageOrderDocumentService;

import java.util.Map;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class OrderMessageGenerator {
    private final ManageOrderDocumentService manageOrderDocumentService;

    public String formatOrderMessage(CaseData caseData, String message) {
        Map<String, String> context = manageOrderDocumentService.commonContextElements(caseData);

        return new StringSubstitutor(context).replace(message);
    }

    public String getCareOrderRestrictions(CaseData caseData) {
        String careOrderRestrictions = "While a care order is in place, no one can change the ${childOrChildren}’s "
            + "surname or take the ${childOrChildren} out of the UK unless they "
            + "have written consent from all people with parental responsibility, or permission from the Court.\n"
            + "\n"
            + "Taking the ${childOrChildren} from the UK without this consent or permission might be an offence under "
            + "the Child Abduction Act 1984.\n"
            + "\n"
            + "${localAuthorityName} has been given parental responsibility under this care order and may take "
            + "the ${childOrChildren} out of the UK for up to 1 month without this consent or permission.";

        return formatOrderMessage(caseData, careOrderRestrictions);
    }

}
