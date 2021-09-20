package uk.gov.hmcts.reform.fpl.model.documentview;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.util.List;

@Data
@Builder
@Getter
public class DocumentBundleView implements DocumentContainerView {
    private String name;
    private List<DocumentView> documents;
}
