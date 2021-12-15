package uk.gov.hmcts.reform.fpl.model.cafcass;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public interface CafcassData {
    String SAME_DAY = "same day";
    default String getDocumentName() {
        throw new UnsupportedOperationException();
    }

    default String getHearingDetails() {
        throw new UnsupportedOperationException();
    }

    default String getLocalAuthourity() {
        throw new UnsupportedOperationException();
    }

    default String getOrdersAndDirections() {
        throw new UnsupportedOperationException();
    }

    default boolean isTimeFramePresent() {
        throw new UnsupportedOperationException();
    }

    default String getTimeFrameValue() {
        throw new UnsupportedOperationException();
    }

    default String getFirstRespondentName() {
        throw new UnsupportedOperationException();
    }

    default String getEldestChildLastName() {
        throw new UnsupportedOperationException();
    }

    default String getDocumentTypes() {
        throw new UnsupportedOperationException();
    }

    default String getEmailSubjectInfo()  {
        throw new UnsupportedOperationException();
    }
}
