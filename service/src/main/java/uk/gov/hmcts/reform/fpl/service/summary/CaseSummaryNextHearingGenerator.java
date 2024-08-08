package uk.gov.hmcts.reform.fpl.service.summary;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.model.summary.SyntheticCaseSummary;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import static java.util.Comparator.comparing;
import static java.util.Objects.isNull;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseSummaryNextHearingGenerator implements CaseSummaryFieldsGenerator {

    private final Time time;

    @Override
    public SyntheticCaseSummary generate(CaseData caseData) {

        return unwrapElements(caseData.getHearingDetails()).stream().filter(
            hearing -> hearing.getEndDate().compareTo(time.now()) >= 0
        ).min(comparing(HearingBooking::getEndDate)).map(
            nextHearing -> SyntheticCaseSummary.builder()
                .caseSummaryHasNextHearing("Yes")
                .caseSummaryNextHearingType(nextHearing.getType().getLabel())
                .caseSummaryNextHearingDate(nextHearing.getStartDate().toLocalDate())
                .caseSummaryNextHearingDateTime(nextHearing.getStartDate())
                .caseSummaryNextHearingJudge(generateSummaryNextHearingJudge(nextHearing,
                    caseData.getAllocatedJudge(),
                    HearingBooking::getHearingJudgeLabel))
                .caseSummaryNextHearingEmailAddress(generateSummaryNextHearingJudge(nextHearing,
                    caseData.getAllocatedJudge(),
                    (hearingBooking -> hearingBooking.getJudgeAndLegalAdvisor().getJudgeEmailAddress())))
                .caseSummaryNextHearingCMO(getCMO(nextHearing, caseData.getDraftUploadedCMOs()))
                .build()
        ).orElse(SyntheticCaseSummary.builder().build());

    }

    public Map<String, Object> generateFields(CaseData caseData) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.registerModule(new JavaTimeModule());
        SyntheticCaseSummary syntheticCaseSummary = generate(caseData);
        // Convert to a map for CCD
        return mapper.convertValue(syntheticCaseSummary, new TypeReference<>() {});
    }

    private String generateSummaryNextHearingJudge(HearingBooking hearing, Judge allocatedJudge,
                                                   Function<HearingBooking, String> fieldTo) {
        if (Objects.isNull(allocatedJudge)
            || isNull(hearing.getJudgeAndLegalAdvisor())
            || isNull(hearing.getHearingJudgeLabel())
            || isHearingJudgeSameAsAllocated(hearing, allocatedJudge)) {
            return null;
        }
        return fieldTo.apply(hearing);
    }

    private boolean isHearingJudgeSameAsAllocated(HearingBooking nextHearing, Judge allocatedJudge) {
        return allocatedJudge.equals(Judge.builder()
            .judgeTitle(nextHearing.getJudgeAndLegalAdvisor().getJudgeTitle())
            .judgeLastName(nextHearing.getJudgeAndLegalAdvisor().getJudgeLastName())
            .judgeEmailAddress(nextHearing.getJudgeAndLegalAdvisor().getJudgeEmailAddress())
            .build());
    }

    private DocumentReference getCMO(HearingBooking hearingBooking, List<Element<HearingOrder>> cmos) {
        return ElementUtils.findElement(hearingBooking.getCaseManagementOrderId(), cmos)
            .map(cmo -> cmo.getValue().getOrder())
            .orElse(null);
    }
}
