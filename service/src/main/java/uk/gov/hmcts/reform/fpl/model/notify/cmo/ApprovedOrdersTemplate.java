package uk.gov.hmcts.reform.fpl.model.notify.cmo;

import lombok.Data;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;

import java.util.List;
import java.util.Map;

@Data
@SuperBuilder
public class ApprovedOrdersTemplate implements NotifyData {
    private final String orderList;
    private final List<String> documentLinks;
    private final String subjectLineWithHearingDate;
    private final String respondentLastName;
    private final String caseUrl;
    private final String digitalPreference;
    private final Map<String, Object> attachedDocument1;
    private final Map<String, Object> attachedDocument2;
    private final Map<String, Object> attachedDocument3;
    private final Map<String, Object> attachedDocument4;
    private final Map<String, Object> attachedDocument5;
    private final Map<String, Object> attachedDocument6;
    private final Map<String, Object> attachedDocument7;
    private final Map<String, Object> attachedDocument8;
    private final Map<String, Object> attachedDocument9;
    private final Map<String, Object> attachedDocument10;
    private final Map<String, Object> attachedDocument11;

}
