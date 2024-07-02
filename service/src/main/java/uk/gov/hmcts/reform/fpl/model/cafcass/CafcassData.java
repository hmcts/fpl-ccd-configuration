package uk.gov.hmcts.reform.fpl.model.cafcass;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDate;
import java.time.LocalDateTime;

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

    default String getCaseUrl()  {
        throw new UnsupportedOperationException();
    }

    default String  getHearingType()   {
        throw new UnsupportedOperationException();
    }

    default LocalDateTime getHearingDate() {
        throw new UnsupportedOperationException();
    }

    default String  getHearingVenue() {
        throw new UnsupportedOperationException();
    }

    default String  getPreHearingTime() {
        throw new UnsupportedOperationException();
    }

    default String  getHearingTime() {
        throw new UnsupportedOperationException();
    }

    default String getNotificationType()  {
        throw new UnsupportedOperationException();
    }

    default LocalDate getOrderApprovalDate()   {
        throw new UnsupportedOperationException();
    }

    default String getPlacementChildName() {
        throw new UnsupportedOperationException();
    }
}
