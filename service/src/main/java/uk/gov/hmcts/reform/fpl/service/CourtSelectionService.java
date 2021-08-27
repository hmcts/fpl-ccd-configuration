package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.config.HmctsCourtLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Court;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;

import java.util.List;
import java.util.Objects;

import static java.util.Optional.ofNullable;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CourtSelectionService {

    private final HmctsCourtLookupConfiguration courtLookup;
    private final DynamicListService dynamicListService;

    public DynamicList getCourtsList(CaseData caseData) {
        final String selectedCourtCode = ofNullable(caseData.getCourt())
            .map(Court::getCode)
            .orElse(null);

        final List<Court> availableCourts = courtLookup.getCourts(caseData.getCaseLocalAuthority());

        return dynamicListService.asDynamicList(availableCourts, selectedCourtCode, Court::getCode, Court::getName);
    }

    public Court getSelectedCourt(CaseData caseData) {
        final String selectedCourtCode = ofNullable(caseData.getCourtsList())
            .map(DynamicList::getValueCode)
            .orElse(null);

        return courtLookup.getCourts(caseData.getCaseLocalAuthority())
            .stream()
            .filter(court -> Objects.equals(court.getCode(), selectedCourtCode))
            .findFirst()
            .orElse(null);
    }
}
