package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.config.CtscEmailLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.HmctsCourtLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Court;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CourtService {

    private final HmctsCourtLookupConfiguration courtLookup;
    private final CtscEmailLookupConfiguration ctscLookup;

    public Court getCourt(CaseData caseData) {

        return ofNullable(caseData.getCourt()).orElseGet(() -> inferCourt(caseData));
    }

    public String getCourtEmail(CaseData caseData) {
        if (YES.getValue().equals(caseData.getSendToCtsc())) {
            return ctscLookup.getEmail();
        }

        return ofNullable(getCourt(caseData))
            .map(Court::getEmail)
            .orElse(null);
    }

    public String getCourtName(CaseData caseData) {
        return ofNullable(getCourt(caseData))
            .map(Court::getName)
            .orElse(null);
    }

    public String getCourtCode(CaseData caseData) {
        return ofNullable(getCourt(caseData))
            .map(Court::getCode)
            .orElse(null);
    }

    private Court inferCourt(CaseData caseData) {
        //User expected to pick up court, but did not select one yet, thus null returned - still must make this decision
        if (YesNo.YES.equals(caseData.getMultiCourts())) {
            return null;
        }

        return courtLookup.getCourts(caseData.getCaseLocalAuthority()).get(0);
    }


}
