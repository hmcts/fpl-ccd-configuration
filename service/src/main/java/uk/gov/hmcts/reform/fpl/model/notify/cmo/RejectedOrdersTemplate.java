package uk.gov.hmcts.reform.fpl.model.notify.cmo;

import lombok.Data;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;

import java.util.List;

@Data
@SuperBuilder
public class RejectedOrdersTemplate implements NotifyData {
    private final List<String> ordersAndRequestedChanges;
    private final String subjectLineWithHearingDate;
    private final String respondentLastName;
    private final String caseUrl;
}
