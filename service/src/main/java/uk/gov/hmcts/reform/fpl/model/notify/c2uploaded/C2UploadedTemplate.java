package uk.gov.hmcts.reform.fpl.model.notify.c2uploaded;

import lombok.Getter;
import lombok.Setter;
import uk.gov.hmcts.reform.fpl.model.notify.SharedNotifyTemplate;

@Getter
@Setter
public final class C2UploadedTemplate extends SharedNotifyTemplate {
    private String callout;
    private String respondentLastName;
}
