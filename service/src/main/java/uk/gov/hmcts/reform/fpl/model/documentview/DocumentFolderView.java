package uk.gov.hmcts.reform.fpl.model.documentview;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.util.List;

@Data
@Builder
@Getter
public class DocumentFolderView implements DocumentContainerView {
    private String name;
    private List<DocumentBundleView> documentBundleViews;
}
