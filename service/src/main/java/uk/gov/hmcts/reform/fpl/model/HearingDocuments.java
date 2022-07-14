package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

@Value
@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HearingDocuments {
    private final List<Element<HearingCourtBundle>> courtBundleListV2;
    private final List<Element<CourtBundle>> courtBundleList;
    private final List<Element<CaseSummary>> caseSummaryList;
    private final List<Element<PositionStatementChild>> positionStatementChildList;
    private final List<Element<PositionStatementRespondent>> positionStatementRespondentList;

    public List<Element<HearingCourtBundle>> getCourtBundleListV2() {
        return defaultIfNull(courtBundleListV2, new ArrayList<>());
    }

    public List<Element<CaseSummary>> getCaseSummaryList() {
        return defaultIfNull(caseSummaryList, new ArrayList<>());
    }

    public List<Element<PositionStatementChild>> getPositionStatementChildList() {
        return defaultIfNull(positionStatementChildList, new ArrayList<>());
    }

    public List<Element<PositionStatementRespondent>> getPositionStatementRespondentList() {
        return defaultIfNull(positionStatementRespondentList, new ArrayList<>());
    }
}
