package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.enums.CMOStatus;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.Recital;
import uk.gov.hmcts.reform.fpl.model.common.Schedule;

import java.util.List;
import java.util.UUID;

@Data
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CaseManagementOrder {
    private final String hearingDate;
    private final UUID hearingDateId; // QUESTION: 25/11/2019 What is this? Why is this? Is this not just id?
    private final Schedule schedule;
    private final UUID id;
    private final List<Element<Recital>> recitals;
    private final CMOStatus cmoStatus;
}
