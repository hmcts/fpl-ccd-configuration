package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CaseData;

import java.time.LocalDate;

import static org.springframework.util.StringUtils.isEmpty;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseExtensionService {
    private final ObjectMapper mapper;

    public LocalDate getCaseCompletionDate(CaseDetails caseDetails) {
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        if (caseDetails.getData().get("caseExtensionTimeList").equals("EightWeekExtension")) {
            if (caseDetails.getData().get("caseExtensionTimeConfirmationList").equals("EightWeekExtension")) {
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
        if (isEmpty(caseData.getCaseCompletionDate())) {
            return caseData.getDateSubmitted();
        }
        return caseData.getCaseCompletionDate();
    }
}
