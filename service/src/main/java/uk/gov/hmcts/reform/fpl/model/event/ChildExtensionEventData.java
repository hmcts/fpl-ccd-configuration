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
   ChildExtension childExtension2;
   @Temp
   ChildExtension childExtension3;
   @Temp
   ChildExtension childExtension4;
   @Temp
   ChildExtension childExtension5;
   @Temp
   ChildExtension childExtension6;
   @Temp
   ChildExtension childExtension7;
   @Temp
   ChildExtension childExtension8;
   @Temp
   ChildExtension childExtension9;
   @Temp
   ChildExtension childExtension10;
   @Temp
   ChildExtension childExtension11;
   @Temp
   ChildExtension childExtension12;
   @Temp
   ChildExtension childExtension13;
   @Temp
   ChildExtension childExtension14;
   @Temp
   ChildExtension childExtension15;
   @Temp
   ChildExtension childExtensionAll;
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

      List<ChildExtension> childExtensions = new ArrayList<>();
      childExtensions.add(verify.apply(childExtension0));
      childExtensions.add(verify.apply(childExtension1));
      childExtensions.add(verify.apply(childExtension3));
      childExtensions.add(verify.apply(childExtension4));
      childExtensions.add(verify.apply(childExtension5));
      childExtensions.add(verify.apply(childExtension6));
      childExtensions.add(verify.apply(childExtension7));
      childExtensions.add(verify.apply(childExtension8));
      childExtensions.add(verify.apply(childExtension9));
      childExtensions.add(verify.apply(childExtension10));
      childExtensions.add(verify.apply(childExtension11));
      childExtensions.add(verify.apply(childExtension12));
      childExtensions.add(verify.apply(childExtension13));
      childExtensions.add(verify.apply(childExtension14));
      childExtensions.add(verify.apply(childExtension15));
      return childExtensions;
   }
}
