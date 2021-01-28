package uk.gov.hmcts.reform.fpl.model.notify.cmo;

import lombok.Data;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;

import java.util.List;

@Data
@SuperBuilder
public class ApprovedOrdersTemplate implements NotifyData {
    private final String orderList;
    private final List<Object> documentLinks;
    private final String subjectLineWithHearingDate;
    private final String respondentLastName;
    private final String caseUrl;
    private final String digitalPreference;
}
