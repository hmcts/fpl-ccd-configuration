package uk.gov.hmcts.reform.fpl.model.event;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.fpl.model.ChildExtension;
import uk.gov.hmcts.reform.fpl.model.Temp;

import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@Value
@Jacksonized
@Builder
@JsonInclude(value= NON_NULL)
public class ChildExtensionEventData {
   @Temp
   ChildExtension childExtension0;
   @Temp
   ChildExtension childExtension1;

   @JsonIgnore
   public List<ChildExtension> getAllChildExtension() {
      return List.of(childExtension0, childExtension1);
   }
}
