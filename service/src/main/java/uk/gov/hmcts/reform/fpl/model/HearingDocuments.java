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
    private final List<Element<HearingCourtBundle>> courtBundleListLA;
    private final List<Element<HearingCourtBundle>> courtBundleListCTSC;
    private final List<Element<HearingCourtBundle>> courtBundleListRemoved;

    private final List<Element<CaseSummary>> caseSummaryList;
    private final List<Element<CaseSummary>> caseSummaryListLA;
    private final List<Element<CaseSummary>> caseSummaryListCTSC;
    private final List<Element<CaseSummary>> caseSummaryListRemoved;

    private final List<Element<PositionStatementChild>> posStmtChildList;
    private final List<Element<PositionStatementChild>> posStmtChildListLA;
    private final List<Element<PositionStatementChild>> posStmtChildListCTSC;
    private final List<Element<PositionStatementChild>> posStmtChildListRemoved;

    private final List<Element<PositionStatementRespondent>> posStmtRespList;
    private final List<Element<PositionStatementRespondent>> posStmtRespListLA;
    private final List<Element<PositionStatementRespondent>> posStmtRespListCTSC;
    private final List<Element<PositionStatementRespondent>> posStmtRespListRemoved;

    private final List<Element<ManagedDocument>> posStmtList;
    private final List<Element<ManagedDocument>> posStmtListLA;
    private final List<Element<ManagedDocument>> posStmtListCTSC;
    private final List<Element<ManagedDocument>> posStmtListRemoved;

    private final List<Element<SkeletonArgument>> skeletonArgumentList;
    private final List<Element<SkeletonArgument>> skeletonArgumentListLA;
    private final List<Element<SkeletonArgument>> skeletonArgumentListCTSC;
    private final List<Element<SkeletonArgument>> skeletonArgumentListRemoved;

    public List<Element<HearingCourtBundle>> getCourtBundleListV2() {
        return defaultIfNull(courtBundleListV2, new ArrayList<>());
    }

    public List<Element<HearingCourtBundle>> getCourtBundleListLA() {
        return defaultIfNull(courtBundleListLA, new ArrayList<>());
    }

    public List<Element<HearingCourtBundle>> getCourtBundleListCTSC() {
        return defaultIfNull(courtBundleListCTSC, new ArrayList<>());
    }

    public List<Element<HearingCourtBundle>> getCourtBundleListRemoved() {
        return defaultIfNull(courtBundleListRemoved, new ArrayList<>());
    }

    public List<Element<CaseSummary>> getCaseSummaryList() {
        return defaultIfNull(caseSummaryList, new ArrayList<>());
    }

    public List<Element<CaseSummary>> getCaseSummaryListLA() {
        return defaultIfNull(caseSummaryListLA, new ArrayList<>());
    }

    public List<Element<CaseSummary>> getCaseSummaryListCTSC() {
        return defaultIfNull(caseSummaryListCTSC, new ArrayList<>());
    }

    public List<Element<PositionStatementChild>> getPosStmtChildList() {
        return defaultIfNull(posStmtChildList, new ArrayList<>());
    }

    public List<Element<PositionStatementChild>> getPosStmtChildListLA() {
        return defaultIfNull(posStmtChildListLA, new ArrayList<>());
    }

    public List<Element<PositionStatementChild>> getPosStmtChildListCTSC() {
        return defaultIfNull(posStmtChildListCTSC, new ArrayList<>());
    }

    public List<Element<PositionStatementRespondent>> getPosStmtRespList() {
        return defaultIfNull(posStmtRespList, new ArrayList<>());
    }

    public List<Element<PositionStatementRespondent>> getPosStmtRespListLA() {
        return defaultIfNull(posStmtRespListLA, new ArrayList<>());
    }

    public List<Element<PositionStatementRespondent>> getPosStmtRespListCTSC() {
        return defaultIfNull(posStmtRespListCTSC, new ArrayList<>());
    }

    public List<Element<SkeletonArgument>> getSkeletonArgumentList() {
        return defaultIfNull(skeletonArgumentList, new ArrayList<>());
    }

    public List<Element<SkeletonArgument>> getSkeletonArgumentListLA() {
        return defaultIfNull(skeletonArgumentListLA, new ArrayList<>());
    }

    public List<Element<SkeletonArgument>> getSkeletonArgumentListCTSC() {
        return defaultIfNull(skeletonArgumentListCTSC, new ArrayList<>());
    }

    public List<Element<ManagedDocument>> getPosStmtList() {
        return defaultIfNull(posStmtList, new ArrayList<>());
    }
    public List<Element<ManagedDocument>> getPosStmtListLA() {
        return defaultIfNull(posStmtListLA, new ArrayList<>());
    }
    public List<Element<ManagedDocument>> getPosStmtListCTSC() {
        return defaultIfNull(posStmtListCTSC, new ArrayList<>());
    }
}
