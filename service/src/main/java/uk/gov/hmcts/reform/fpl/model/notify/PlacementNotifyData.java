package uk.gov.hmcts.reform.fpl.model.notify;

import lombok.Data;
import lombok.experimental.SuperBuilder;

import static java.util.Objects.isNull;

@Data
@SuperBuilder(toBuilder = true)
public class PlacementNotifyData implements NotifyData {

    private String childName;
    private String ccdNumber;
    private String caseUrl;
    private String localAuthority;

    private String documentUrl;
    private Object documentDownloadUrl;
    private String hasDocumentDownloadUrl;
}
