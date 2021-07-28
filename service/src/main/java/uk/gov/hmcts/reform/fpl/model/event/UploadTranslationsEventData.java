package uk.gov.hmcts.reform.fpl.model.event;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;

import java.util.List;

@Value
@Builder(toBuilder = true)
@Jacksonized
public class UploadTranslationsEventData {

    DynamicList uploadTranslationsRelatedToDocument;
    DocumentReference uploadTranslationsOriginalDoc;
    DocumentReference uploadTranslationsTranslatedDoc;

    @JsonIgnore
    public List<String> getTransientFields() {
        return List.of(
            "uploadTranslationsRelatedToDocument",
            "uploadTranslationsOriginalDoc",
            "uploadTranslationsTranslatedDoc"
        );
    }

}
