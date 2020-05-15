package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;

import java.time.LocalDate;
import java.util.Optional;

import static uk.gov.hmcts.reform.fpl.enums.CaseExtensionTime.*;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseExtensionService {

    public LocalDate getCaseCompletionDate(CaseData caseData) {
        if (caseData.getCaseExtensionTimeList().equals(eightWeekExtension)) {
            if (caseData.getCaseExtensionTimeConfirmationList().equals(eightWeekExtension)) {
                return getCaseCompletionDateFor8WeekExtension(caseData);
            }
            return caseData.getEightWeeksExtensionDateOther();
        }
        return caseData.getExtensionDateOther();
    }

    public LocalDate getCaseCompletionDateFor8WeekExtension(CaseData caseData) {
        return getCaseCompletionOrSubmittedDate(caseData).plusWeeks(8);
    }

    public LocalDate getCaseCompletionOrSubmittedDate(CaseData caseData) {
        return Optional.ofNullable(caseData.getCaseCompletionDate()).orElse(caseData.getDateSubmitted());
    }
}
