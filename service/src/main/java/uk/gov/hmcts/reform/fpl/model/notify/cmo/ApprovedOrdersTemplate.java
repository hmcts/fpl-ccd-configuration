package uk.gov.hmcts.reform.fpl.model.notify.cmo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import org.json.JSONObject;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
public class ApprovedOrdersTemplate extends IssuedCMOTemplate {
    private final List<String> orderList;
    private final List<JSONObject> orderDocumentList;
}
