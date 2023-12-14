package uk.gov.hmcts.reform.fpl.service.summary;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.summary.SyntheticCaseSummary;
import uk.gov.hmcts.reform.fpl.service.UserService;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;

@Component
public class CaseSummaryCaseFlagGenerator implements CaseSummaryFieldsGenerator {

    private final UserService userService;

    public CaseSummaryCaseFlagGenerator(UserService userService) {
        this.userService = userService;
    }

    @Override
    public SyntheticCaseSummary generate(CaseData caseData) {
        return SyntheticCaseSummary.builder()
            .caseSummaryFlagAssessmentForm(caseData.getRedDotAssessmentForm())
            .caseSummaryCaseFlagNotes(caseData.getCaseFlagNotes())
            .caseSummaryFlagAddedByFullName(generateUploadedFullName(caseData))
            .caseSummaryFlagAddedByEmail(generateUploadedByEmail(caseData))
            .build();
    }

    public Map<String, Object> generateFields(CaseData caseData) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("caseSummaryFlagAssessmentForm", caseData.getRedDotAssessmentForm());
        map.put("caseSummaryCaseFlagNotes", caseData.getCaseFlagNotes());
        map.put("caseSummaryFlagAddedByFullName", generateUploadedFullName(caseData));
        map.put("caseSummaryFlagAddedByEmail", generateUploadedByEmail(caseData));
        return map;
    }

    private String generateUploadedByEmail(CaseData caseData) {
        return shouldUpdate(caseData) ? userService.getUserEmail()
            : caseData.getSyntheticCaseSummary().getCaseSummaryFlagAddedByEmail();
    }

    private String generateUploadedFullName(CaseData caseData) {
        return  shouldUpdate(caseData) ? userService.getUserName()
            : caseData.getSyntheticCaseSummary().getCaseSummaryFlagAddedByFullName();
    }

    private boolean shouldUpdate(CaseData caseData) {
        return caseData.getCaseFlagValueUpdated() == YES;
    }
}
