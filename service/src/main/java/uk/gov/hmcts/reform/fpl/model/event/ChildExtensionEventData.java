package uk.gov.hmcts.reform.fpl.model.event;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.fpl.model.ChildExtension;

@Value
@Jacksonized
@Builder
public class ChildExtensionEventData {
   ChildExtension childExtension0;
   ChildExtension childExtension1;
}
