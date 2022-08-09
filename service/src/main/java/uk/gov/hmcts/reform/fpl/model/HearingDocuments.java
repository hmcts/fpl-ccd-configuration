package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

    // the element ID of each position statement is unique
    private final List<Element<PositionStatementChild>> positionStatementChildListV2;
    // the element ID of each position statement is unique
    private final List<Element<PositionStatementRespondent>> positionStatementRespondentListV2;

    public static class HearingDocumentsBuilder {
        @Deprecated
        // for old case data without data migration
        public HearingDocumentsBuilder positionStatementChildList(
                List<Element<PositionStatementChild>> positionStatementChildList) {
            if (isEmpty(this.positionStatementChildListV2)) {
                this.positionStatementChildListV2 = positionStatementChildList.stream()
                    .map(doc -> element(doc.getValue().toBuilder().hearingId(doc.getId()).build()))
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
                    .map(doc -> element(doc.getValue().toBuilder().hearingId(doc.getId()).build()))
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

    public List<Element<PositionStatementChild>> getPositionStatementChildListV2() {
        return defaultIfNull(positionStatementChildListV2, new ArrayList<>());
    }

    public List<Element<PositionStatementRespondent>> getPositionStatementRespondentListV2() {
        return defaultIfNull(positionStatementRespondentListV2, new ArrayList<>());
    }
}
