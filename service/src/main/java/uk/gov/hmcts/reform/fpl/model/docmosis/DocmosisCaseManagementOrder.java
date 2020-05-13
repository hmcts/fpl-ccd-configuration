package uk.gov.hmcts.reform.fpl.model.docmosis;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.reform.fpl.model.common.Schedule;

import java.util.List;

@Getter
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class DocmosisCaseManagementOrder extends DocmosisOrder {
    private final List<DocmosisRepresentative> representatives;
    private final boolean scheduleProvided;
    private final Schedule schedule;
    private final List<DocmosisRecital> recitals;
    private final boolean recitalsProvided;
    private final DocmosisJudge allocatedJudge;
}
