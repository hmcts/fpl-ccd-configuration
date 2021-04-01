package uk.gov.hmcts.reform.fpl.service.summary;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.model.summary.SyntheticCaseSummary;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.util.Comparator.comparing;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.findElement;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

@Component
public class CaseSummaryPreviousHearingGenerator implements CaseSummaryFieldsGenerator {

    private final Time time;

    public CaseSummaryPreviousHearingGenerator(Time time) {
        this.time = time;
    }

    @Override
    public SyntheticCaseSummary generate(CaseData caseData) {
        return unwrapElements(caseData.getHearingDetails()).stream().filter(
            hearing -> hearing.getEndDate().isBefore(time.now())
        ).max(comparing(HearingBooking::getEndDate)).map(
            previousHearing ->
                SyntheticCaseSummary.builder()
                    .caseSummaryHasPreviousHearing("Yes")
                    .caseSummaryPreviousHearingType(previousHearing.getType().getLabel())
                    .caseSummaryPreviousHearingDate(previousHearing.getStartDate().toLocalDate())
                    .caseSummaryPreviousHearingCMO(getAssociatedCMO(previousHearing,caseData.getSealedCMOs()))
                    .build()
        ).orElse(SyntheticCaseSummary.builder().build());
    }

    private DocumentReference getAssociatedCMO(HearingBooking hearing, List<Element<HearingOrder>> sealedCMOs) {
        UUID cmoId = hearing.getCaseManagementOrderId();
        Optional<Element<HearingOrder>> cmo = findElement(cmoId, sealedCMOs);
        return cmo.map(element -> element.getValue().getOrder()).orElse(null);
    }
}

