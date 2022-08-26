package uk.gov.hmcts.reform.fpl.utils.elasticsearch;

public interface ESClause<T> {
    T toMap();
}
