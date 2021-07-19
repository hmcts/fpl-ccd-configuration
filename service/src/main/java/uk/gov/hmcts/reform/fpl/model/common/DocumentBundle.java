package uk.gov.hmcts.reform.fpl.model.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.model.interfaces.TranslatableItem;

import java.util.Objects;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor(onConstructor_ = {@JsonCreator})
public class DocumentBundle implements TranslatableItem {
    private final DocumentReference document;
    private final DocumentReference translatedDocument;

    @Override
    @JsonIgnore
    public String asLabel() {
        return "Notice of proceedings (" + (document.getFilename().contains("_c6a") ? "C6A" : "C6") + ")";
    }

    @Override
    @JsonIgnore
    public boolean hasBeenTranslated() {
        return Objects.nonNull(translatedDocument);
    }
}
