package uk.gov.hmcts.reform.fpl.service.translation;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.configuration.Language;
import uk.gov.hmcts.reform.fpl.model.group.C110A;

import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;

@Component
public class C110ATranslationRequirementCalculator {

    public LanguageTranslationRequirement calculate(CaseData caseData) {
        C110A c110A = caseData.getC110A();
        if (YesNo.YES == YesNo.fromString(caseData.getLanguageRequirement()) && c110A.getLanguageRequirementApplication() == Language.ENGLISH
            && YesNo.fromString(c110A.getLanguageRequirementApplicationNeedWelsh()) == YES) {
            return LanguageTranslationRequirement.ENGLISH_TO_WELSH;
        }
        if (YesNo.YES == YesNo.fromString(caseData.getLanguageRequirement()) && c110A.getLanguageRequirementApplication() == Language.WELSH
            && YesNo.fromString(c110A.getLanguageRequirementApplicationNeedEnglish()) == YES) {
            return LanguageTranslationRequirement.WELSH_TO_ENGLISH;
        }
        return LanguageTranslationRequirement.NO;
    }
}
