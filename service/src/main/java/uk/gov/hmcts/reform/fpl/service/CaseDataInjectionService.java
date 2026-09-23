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

    private final Clock clock;

    public CaseDataInjectionResponse generate(CaseData caseData) {
        LocalDate dateSubmitted = caseData == null ? null : caseData.getDateSubmitted();
        Long caseAgeWeeks = dateSubmitted == null
            ? null
            : Math.max(0, ChronoUnit.WEEKS.between(dateSubmitted, LocalDate.now(clock)));
        String caseAgeBand = getBand(caseAgeWeeks);

        return CaseDataInjectionResponse.builder()
            .metadataFields(List.of(
                CdiMetadataField.text(AGE_BAND_ID, "Case age band", caseAgeBand)
            ))
            .build();
    }

    private String getBand(Long caseAgeWeeks) {
        if (caseAgeWeeks == null) {
            return "";
        }

        if (caseAgeWeeks < 26) {
            return GREEN;
        }

        if (caseAgeWeeks <= 52) {
            return AMBER;
        }

        return RED;
    }

    private String toBandLabel(String caseAgeBand) {
        return switch (caseAgeBand) {
            case GREEN -> "Under 26 weeks";
            case AMBER -> "26 to 52 weeks";
            case RED -> "Over 52 weeks";
            default -> "";
        };
    }
}


