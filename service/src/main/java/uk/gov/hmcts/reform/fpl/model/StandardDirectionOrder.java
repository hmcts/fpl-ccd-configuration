package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.ccd.document.am.model.Document;
import uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement;
import uk.gov.hmcts.reform.fpl.enums.ModifiedOrderType;
import uk.gov.hmcts.reform.fpl.enums.OrderStatus;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.interfaces.AmendableOrder;
import uk.gov.hmcts.reform.fpl.model.interfaces.IssuableOrder;
import uk.gov.hmcts.reform.fpl.model.interfaces.RemovableOrder;
import uk.gov.hmcts.reform.fpl.model.interfaces.TranslatableItem;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement.NO;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.SEALED;
import static uk.gov.hmcts.reform.fpl.model.common.DocumentReference.buildFromDocument;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.parseLocalDateFromStringUsingFormat;

@Slf4j
@Data
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StandardDirectionOrder implements IssuableOrder, RemovableOrder, AmendableOrder, TranslatableItem {
    public static final UUID COLLECTION_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    public static final UUID UDO_COLLECTION_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

    private final String hearingDate;
    private final String dateOfIssue;
    private final OrderStatus orderStatus;
    private final JudgeAndLegalAdvisor judgeAndLegalAdvisor;
    private final LocalDate dateOfUpload;
    private final String uploader;
    private final LocalDate amendedDate;
    private final DocumentReference unsealedDocumentCopy;
    private final List<Element<CustomDirection>> customDirections;
    private final List<Element<StandardDirection>> standardDirections;
    private List<Element<Direction>> directions;
    private DocumentReference orderDoc;
    private DocumentReference translatedOrderDoc;
    private DocumentReference lastUploadedOrder;
    private String removalReason;
    private final List<Element<Other>> others;
    private final LocalDateTime translationUploadDateTime;
    private final LanguageTranslationRequirement translationRequirements;

    @JsonIgnore
    @Setter
    @Builder.Default
    private boolean orderTypeIsSdo = true; // for removal tools use only

    @JsonIgnore
    public boolean isSealed() {
        return SEALED == orderStatus;
    }

    @JsonIgnore
    public void setDirectionsToEmptyList() {
        this.directions = emptyList();
    }

    @JsonIgnore
    public void setOrderDocReferenceFromDocument(Document document) {
        if (document != null) {
            this.orderDoc = buildFromDocument(document);
        }
    }

    @JsonIgnore
    public UUID getCollectionId() {
        if (isOrderTypeIsSdo()) {
            return COLLECTION_ID;
        } else {
            return UDO_COLLECTION_ID;
        }
    }

    @Override
    @JsonIgnore
    public boolean isRemovable() {
        return isSealed();
    }

    @Override
    @JsonIgnore
    public boolean hasBeenTranslated() {
        return Objects.nonNull(translatedOrderDoc);
    }

    @Override
    public LocalDateTime translationUploadDateTime() {
        return translationUploadDateTime;
    }

    @Override
    public String asLabel() {
        String formattedDate = Optional.ofNullable(dateOfIssue)
            .orElse(formatLocalDateToString(defaultIfNull(dateOfUpload, LocalDate.now()), DATE));

        return ((isOrderTypeIsSdo()) ? "Gatekeeping order - " : "Urgent directions order - ") + formattedDate;
    }

    @Override
    public LocalDate amendableSortDate() {
        if (null != dateOfUpload) {
            return dateOfUpload;
        }

        try {
            if (null != dateOfIssue) {
                return parseLocalDateFromStringUsingFormat(dateOfIssue, DATE);
            }
        } catch (DateTimeParseException ignored) {
            log.warn("Could not parse {} with format {}", dateOfIssue, DATE);
        }

        log.warn("Could not find any date to sort amendable list by, falling back to null");
        return null;
    }

    @Override
    @JsonIgnore
    public DocumentReference getTranslatedDocument() {
        return translatedOrderDoc;
    }

    @Override
    public LanguageTranslationRequirement getTranslationRequirements() {
        return defaultIfNull(translationRequirements, NO);
    }

    @Override
    @JsonIgnore
    public DocumentReference getDocument() {
        return orderDoc;
    }

    @JsonIgnore
    @Override
    public String getModifiedItemType() {
        return ModifiedOrderType.STANDARD_DIRECTION_ORDER.getLabel();
    }

    @JsonIgnore
    @Override
    public List<Element<Other>> getSelectedOthers() {
        return defaultIfNull(this.getOthers(), new ArrayList<>());
    }
}

