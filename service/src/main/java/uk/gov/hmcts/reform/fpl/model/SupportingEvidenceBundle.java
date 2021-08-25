package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.fpl.enums.FurtherEvidenceType;
import uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement;
import uk.gov.hmcts.reform.fpl.enums.ModifiedOrderType;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.interfaces.TranslatableItem;
import uk.gov.hmcts.reform.fpl.validation.interfaces.time.PastOrPresentDate;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;

@Data
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SupportingEvidenceBundle implements TranslatableItem {
    private final String name;
    private final String notes;
    @PastOrPresentDate(message = "Date received cannot be in the future")
    private final LocalDateTime dateTimeReceived;
    private LocalDateTime dateTimeUploaded;
    private final DocumentReference document;
    private String uploadedBy;
    private List<String> confidential;
    private FurtherEvidenceType type;
    private String uploadedBySolicitor;
    private final DocumentReference translatedDocument;
    private final LocalDateTime translationUploadDateTime;
    private final LanguageTranslationRequirement translationRequirements;

    @JsonIgnore
    public boolean isConfidentialDocument() {
        return confidential != null && confidential.contains("CONFIDENTIAL");
    }

    @JsonIgnore
    public boolean isUploadedByHMCTS() {
        return "HMCTS".equals(uploadedBy);
    }

    @JsonIgnore
    public boolean isUploadedByRepresentativeSolicitor() {
        return "Yes".equals(uploadedBySolicitor);
    }

    @JsonGetter("confidentialTabLabel")
    public String generateConfidentialTabLabel() {
        return isConfidentialDocument() ? "Confidential" : null;
    }

    @Override
    public String asLabel() {
        return String.format("%s - %s - %s", Optional.ofNullable(type)
            .map(FurtherEvidenceType::getLabel)
            .orElse("Document"), name, formatLocalDateTimeBaseUsingFormat(dateTimeUploaded, DATE));
    }

    @Override
    @JsonIgnore
    public String getModifiedItemType() {
        return ModifiedOrderType.ANY_DOCUMENT.getLabel();
    }

    @Override
    @JsonIgnore
    public List<Element<Other>> getSelectedOthers() {
        return Collections.emptyList();
    }

    @Override
    @JsonIgnore
    public boolean hasBeenTranslated() {
        return Objects.nonNull(translatedDocument);
    }

    @JsonIgnore
    public boolean sentForTranslation() {
        return getNeedTranslation() == YesNo.YES && !hasBeenTranslated();
    }

    @Override
    public LocalDateTime translationUploadDateTime() {
        return translationUploadDateTime;
    }

}
