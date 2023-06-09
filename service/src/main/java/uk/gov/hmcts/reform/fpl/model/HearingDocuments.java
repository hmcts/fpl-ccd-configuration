package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@Value
@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HearingDocuments {
    // the element ID is the ID of the linked hearing
    private final List<Element<HearingCourtBundle>> courtBundleListV2;
    // the element ID is the ID of the linked hearing
    private final List<Element<CourtBundle>> courtBundleList;
    // the element ID is the ID of the linked hearing
    private final List<Element<CaseSummary>> caseSummaryList;
    private final List<Element<CaseSummary>> caseSummaryListLA;
    private final List<Element<CaseSummary>> caseSummaryListCTSC;
    private final List<Element<CaseSummary>> caseSummaryListRemoved;

    // the element ID of each position statement is unique
    @Deprecated(since = "DFPL-1491")
    private final List<Element<PositionStatementChild>> positionStatementChildListV2;
    private final List<Element<PositionStatementChild>> posStmtChildList;
    private final List<Element<PositionStatementChild>> posStmtChildListLA;
    private final List<Element<PositionStatementChild>> posStmtChildListCTSC;
    private final List<Element<PositionStatementChild>> posStmtChildListRemoved;
    // the element ID of each position statement is unique
    @Deprecated(since = "DFPL-1491")
    private final List<Element<PositionStatementRespondent>> positionStatementRespondentListV2;
    private final List<Element<PositionStatementRespondent>> posStmtRespList;
    private final List<Element<PositionStatementRespondent>> posStmtRespListLA;
    private final List<Element<PositionStatementRespondent>> posStmtRespListCTSC;
    private final List<Element<PositionStatementRespondent>> posStmtRespListRemoved;
    // the element ID of each Skeleton argument is unique
    private final List<Element<SkeletonArgument>> skeletonArgumentList;

    public static class HearingDocumentsBuilder {
        @Deprecated
        // for old case data without data migration
        public HearingDocumentsBuilder positionStatementChildList(
                List<Element<PositionStatementChild>> positionStatementChildList) {
            if (isEmpty(this.positionStatementChildListV2)) {
                this.positionStatementChildListV2 = positionStatementChildList.stream()
                    .map(doc -> element(doc.getId(), doc.getValue().toBuilder().hearingId(doc.getId()).build()))
                    .collect(Collectors.toList());
            }
            return this;
        }

        @Deprecated
        // for old case data without data migration
        public HearingDocumentsBuilder positionStatementRespondentList(
            List<Element<PositionStatementRespondent>> positionStatementChildList) {
            if (isEmpty(this.positionStatementRespondentListV2)) {
                this.positionStatementRespondentListV2 = positionStatementChildList.stream()
                    .map(doc -> element(doc.getId(), doc.getValue().toBuilder().hearingId(doc.getId()).build()))
                    .collect(Collectors.toList());
            }
            return this;
        }
    }

    public List<Element<HearingCourtBundle>> getCourtBundleListV2() {
        return defaultIfNull(courtBundleListV2, new ArrayList<>());
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

    @Deprecated
    public List<Element<PositionStatementChild>> getPositionStatementChildListV2() {
        List<Element<PositionStatementChild>> oldList = defaultIfNull(positionStatementChildListV2, new ArrayList<>());
        List<Element<PositionStatementChild>> newList = defaultIfNull(posStmtChildList, new ArrayList<>());
        return Stream.concat(oldList.stream(), newList.stream()).collect(toList());
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

    @Deprecated
    public List<Element<PositionStatementRespondent>> getPositionStatementRespondentListV2() {
        List<Element<PositionStatementRespondent>> oldList = defaultIfNull(positionStatementRespondentListV2,
            new ArrayList<>());
        List<Element<PositionStatementRespondent>> newList = defaultIfNull(posStmtRespList, new ArrayList<>());
        return Stream.concat(oldList.stream(), newList.stream()).collect(toList());
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
}
