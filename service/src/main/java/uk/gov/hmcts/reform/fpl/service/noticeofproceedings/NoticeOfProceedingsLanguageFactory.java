package uk.gov.hmcts.reform.fpl.service.noticeofproceedings;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.StandardDirectionOrder;
import uk.gov.hmcts.reform.fpl.model.order.UrgentHearingOrder;

import java.util.Optional;

@Component
public class NoticeOfProceedingsLanguageFactory {

    public LanguageTranslationRequirement calculate(CaseData caseData) {
        return Optional.ofNullable(caseData.getStandardDirectionOrder()).map(
            StandardDirectionOrder::getTranslationRequirements
        ).orElse(Optional.ofNullable(caseData.getUrgentHearingOrder())
                .map(UrgentHearingOrder::getTranslationRequirements)
                .orElse(LanguageTranslationRequirement.NO));
    }

}
