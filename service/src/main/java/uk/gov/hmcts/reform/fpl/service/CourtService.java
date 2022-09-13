package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.config.CtscEmailLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.HmctsCourtLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.OrderStatus;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Court;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.Collections;
import java.util.Optional;

import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsFirst;
import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisImages.COURT_SEAL;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.SEALED;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.service.CourtLookUpService.RCJ_HIGH_COURT_CODE;

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

    public String getPreviousCourtName(CaseData caseData) {
        return Optional.ofNullable(caseData.getPastCourtList())
            .orElse(Collections.emptyList()).stream()
            .map(Element::getValue)
            .max(comparing(Court::getDateTransferred, nullsFirst(naturalOrder())))
            .map(Court::getName)
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

    public boolean isHighCourtCase(CaseData caseData) {
        return isHighCourtCase(caseData.getCourt());
    }

    public boolean isHighCourtCase(Court court) {
        return Optional.ofNullable(court)
                .map(Court::getCode)
                .filter(code -> code.equals(RCJ_HIGH_COURT_CODE))
                .isPresent();
    }

    public String getCourtSeal(CaseData caseData, OrderStatus status) {
        String seal = null;
        if (SEALED == status && !isHighCourtCase(caseData)) {
            seal = COURT_SEAL.getValue(caseData.getImageLanguage());
        }
        return seal;
    }

    private Court inferCourt(CaseData caseData) {
        //User expected to pick up court, but did not select one yet, thus null returned - still must make this decision
        if (YesNo.YES.equals(caseData.getMultiCourts())) {
            return null;
        }

        return courtLookup.getCourts(caseData.getCaseLocalAuthority()).get(0);
    }

    public Optional<Court> getCourt(String code) {
        return courtLookup.getCourtByCode(code);
    }
}
