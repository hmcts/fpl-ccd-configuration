package uk.gov.hmcts.reform.fpl.model.notify.additionalapplicationsuploaded;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.reform.fpl.model.notify.SharedNotifyTemplate;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
public final class AdditionalApplicationsUploadedTemplate extends SharedNotifyTemplate {
    private final String callout;
    private final String respondentLastName;
    private final String documentUrl;
    private final List<String> applicationTypes;
}
