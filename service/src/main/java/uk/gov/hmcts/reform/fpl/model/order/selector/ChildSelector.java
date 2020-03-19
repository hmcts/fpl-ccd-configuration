package uk.gov.hmcts.reform.fpl.model.order.selector;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import uk.gov.hmcts.ccd.sdk.types.CCD;

@Data
@Builder
public class ChildSelector {
    @Setter(AccessLevel.PRIVATE)
    @Builder.Default
    private String childCount = "";
    @Builder.Default
    @CCD(ignore = true)
    private List<Integer> selected = new ArrayList<>();

    public void generateChildCount(int max) {
        if (max < 1) {
            setChildCount("");
        } else {
            StringBuilder builder = new StringBuilder();

            for (int i = 1; i <= max; i++) {
                builder.append(i);
            }

            setChildCount(builder.toString());
        }
    }
}
