package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.cdi.CdiMetadataField;
import uk.gov.hmcts.reform.fpl.model.cdi.CaseDataInjectionResponse;

import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseDataInjectionService {
    private static final String AGE_BAND_ID = "caseSummaryCaseAgeBandInjected";
    private static final String GREEN = "GREEN";
    private static final String AMBER = "AMBER";
    private static final String RED = "RED";

    public CaseDataInjectionResponse generate(CaseData caseData) {
        LocalDate dateSubmitted = caseData.getDateSubmitted();
        Long caseAgeWeeks = Math.max(0, ChronoUnit.WEEKS.between(dateSubmitted, LocalDate.now()));
        String caseAgeBand = getCaseAgeBand(caseAgeWeeks);

        return CaseDataInjectionResponse.builder()
            .metadataFields(List.of(
                CdiMetadataField.text(AGE_BAND_ID, "Case age band", caseAgeBand)
            ))
            .build();
    }

    private String getCaseAgeBand(Long caseAgeWeeks) {
        if (caseAgeWeeks < 26) {
            return GREEN;
        }

        if (caseAgeWeeks <= 52) {
            return AMBER;
        }

        return RED;
    }

}


