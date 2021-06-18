package uk.gov.hmcts.reform.fpl.model.notify.cmo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.json.serializer.ApprovedOrdersTemplateSerializer;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;

import java.util.List;
import java.util.Map;

@Data
@Builder
@JsonSerialize(using = ApprovedOrdersTemplateSerializer.class)
public class ApprovedOrdersTemplate implements NotifyData {
    private final String orderList;
    private final List<String> documentLinks;
    private final String subjectLineWithHearingDate;
    private final String lastName;
    private final String caseUrl;
    private final String digitalPreference;
    private final List<Map<String, Object>> attachedDocuments;
}
