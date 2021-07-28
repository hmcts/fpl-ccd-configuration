package uk.gov.hmcts.reform.fpl.model.notify;

import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.reform.fpl.service.email.content.ModifiedItemEmailContentProvider;

@Value
@Builder
public class ModifiedItemEmailContentProviderResponse {

    ModifiedItemEmailContentProvider provider;
    String templateKey;

}
