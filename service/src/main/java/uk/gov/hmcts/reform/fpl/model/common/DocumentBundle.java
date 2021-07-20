package uk.gov.hmcts.reform.fpl.model.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.model.interfaces.TranslatableItem;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor(onConstructor_ = {@JsonCreator})
public class DocumentBundle implements TranslatableItem {
    private final DocumentReference document;
    private final DocumentReference translatedDocument;
    private final LocalDateTime translationUploadDateTime;

    @Override
    @JsonIgnore
    public String asLabel() {
        return Optional.ofNullable(document)
            .map(documentReference ->
                "Notice of proceedings (" + (documentReference.getFilename().contains("_c6a") ? "C6A" : "C6") + ")")
            .orElse("");
    }

    @Override
    @JsonIgnore
    public boolean hasBeenTranslated() {
        return Objects.nonNull(translatedDocument);
    }

    @Override
    public LocalDateTime translationUploadDateTime() {
        return translationUploadDateTime;
    }
}
