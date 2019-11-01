package uk.gov.hmcts.reform.fpl.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
@AllArgsConstructor
public class MultiList {
    private final List<CodeLabel> list_items;

    private final CodeLabel value;

    @Builder
    @Data
    @AllArgsConstructor
    public static class CodeLabel {
        private final String code;
        private final String label;
    }
}
