package uk.gov.hmcts.reform.fpl.model.event;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.fpl.model.ChildExtension;
import uk.gov.hmcts.reform.fpl.model.Temp;
import uk.gov.hmcts.reform.fpl.model.order.selector.Selector;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;

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
   @Temp
   Selector childSelectorForExtension;
   @Temp
   String extensionForAllChildren;
   @Temp
   String sameExtensionForAllChildren;

   @JsonIgnore
   public List<ChildExtension> getAllChildExtension() {
      UnaryOperator<ChildExtension> verify = childExtension -> Optional.ofNullable(childExtension)
              .filter(child -> child.getId() != null)
              .orElse(null);
      ArrayList<ChildExtension> childExtensions = new ArrayList<>();
      childExtensions.add(verify.apply(childExtension0));
      childExtensions.add(verify.apply(childExtension1));
      return childExtensions;
   }
}
