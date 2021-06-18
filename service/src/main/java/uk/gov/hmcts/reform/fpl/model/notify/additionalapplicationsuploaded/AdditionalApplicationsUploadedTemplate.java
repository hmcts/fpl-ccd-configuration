package uk.gov.hmcts.reform.fpl.model.notify.additionalapplicationsuploaded;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.reform.fpl.model.notify.SharedNotifyTemplate;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@SuperBuilder
public final class AdditionalApplicationsUploadedTemplate extends SharedNotifyTemplate {
    private final String callout;
    @JsonProperty("respondentLastName")
    private final String lastName;
    private final String documentUrl;
    private final List<String> applicationTypes;
}
