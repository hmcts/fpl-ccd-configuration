package uk.gov.hmcts.reform.fpl.service;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DocumentBundleView {
    String name;
    List<DocumentView> documents;
}
