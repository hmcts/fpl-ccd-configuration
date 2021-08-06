package uk.gov.hmcts.reform.fpl.service.orders.history;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.CaseData;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement.ENGLISH_TO_WELSH;
import static uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement.NO;

@Component
public class SealedOrderLanguageRequirementGenerator {

    public LanguageTranslationRequirement translationRequirements(CaseData caseData) {

        if (specifiedByUser(caseData)) {
            return caseData.getManageOrdersEventData().getManageOrdersTranslationNeeded();
        }

        return YesNo.fromString(caseData.getLanguageRequirement()) == YesNo.YES ? ENGLISH_TO_WELSH : NO;
    }

    private boolean specifiedByUser(CaseData caseData) {
        return isNotEmpty(caseData.getManageOrdersEventData()
            .getManageOrdersTranslationNeeded());
    }

}
