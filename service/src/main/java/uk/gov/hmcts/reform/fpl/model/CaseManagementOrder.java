package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.enums.CMOStatus;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.common.Recital;
import uk.gov.hmcts.reform.fpl.model.common.Schedule;

import java.util.List;
import java.util.UUID;

@Data
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CaseManagementOrder {
    private final String hearingDate;
    private final UUID id;
    private final List<Element<Direction>> directions;
    private final Schedule schedule;
    private final List<Element<Recital>> recitals;
    private final CMOStatus cmoStatus;
    private final DocumentReference orderDoc;
    private final CaseManagementOrderAction caseManagementOrderAction;
    private final JudgeAndLegalAdvisor judgeAndLegalAdvisor;
}
