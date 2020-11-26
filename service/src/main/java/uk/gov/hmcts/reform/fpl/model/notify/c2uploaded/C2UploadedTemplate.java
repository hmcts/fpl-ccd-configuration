package uk.gov.hmcts.reform.fpl.model.notify.c2uploaded;

import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.reform.fpl.model.notify.SharedNotifyTemplate;

@SuperBuilder
public final class C2UploadedTemplate extends SharedNotifyTemplate {
    private final String callout;
    private final String respondentLastName;
    private final String documentUrl;
}
