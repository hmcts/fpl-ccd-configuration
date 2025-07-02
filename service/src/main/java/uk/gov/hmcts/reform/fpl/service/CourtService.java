package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.config.CtscEmailLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.HighCourtAdminEmailLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.HmctsCourtLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.OrderStatus;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Court;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.Collections;
import java.util.Optional;

import static java.lang.String.format;
import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsFirst;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
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
    private final HighCourtAdminEmailLookupConfiguration highCourtAdminEmailLookupConfiguration;

    public Court getCourt(CaseData caseData) {

        return ofNullable(caseData.getCourt()).orElseGet(() -> inferCourt(caseData));
    }

    public Optional<Court> getCourt(String code) {
        return courtLookup.getCourtByCode(code);
    }

    public String getCourtEmail(CaseData caseData) {
        if (YES.getValue().equals(caseData.getSendToCtsc())) {
            return ctscLookup.getEmail();
        }

        return getSelectedCourtEmail(caseData);
    }

    /**
     * This method is intended to not send a notification to CTSC but will continue to send to court email when
     * required.
     *
     * <p>
     * Returns null if the CaseData sendToCtsc field is set to Yes.<br/>
     * Otherwise will return the RCJ High Court email or the selected court email.
     *</p>
     * @param caseData - CaseData to retrieve values from
     * @return court email or null if sendToCtsc is set to Yes
     */
    public String getCourtEmailNotCtsc(final CaseData caseData) {
        if (YES.getValue().equals(caseData.getSendToCtsc())) {
            return null;
        }

        return getSelectedCourtEmail(caseData);
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
        String localAuthorityCode = caseData.getCaseLocalAuthority();
        if (isEmpty(localAuthorityCode)) {
            // try to get the court selected in "Orders and direction sought" section
            // this is the case for standalone application submitted by solicitors
            // who do not have court info from onboarding configs
            Optional<String> courtCode = Optional.ofNullable(caseData.getOrders()).map(o -> o.getCourt());
            if (courtCode.isPresent()) {
                return courtLookup.getCourtByCode(courtCode.get())
                    .orElseThrow(() -> new IllegalArgumentException(
                        format("Fail to lookup court by court code: {}", courtCode.get())));
            } else {
                throw new IllegalArgumentException(
                    format("unexpected missing court information (case id: {})", caseData.getId()));
            }
        }
        return courtLookup.getCourts(localAuthorityCode).get(0);
    }

    private String getSelectedCourtEmail(final CaseData caseData) {
        final Optional<Court> court = ofNullable(getCourt(caseData));
        if (court.isPresent() && court.get().getCode().equals(RCJ_HIGH_COURT_CODE)) {
            return highCourtAdminEmailLookupConfiguration.getEmail();
        }

        // If the case has a court specific email use that, otherwise default to CTSC so no emails are missed
        return court.map(Court::getEmail).orElse(ctscLookup.getEmail());
    }
}
