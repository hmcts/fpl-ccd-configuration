package uk.gov.hmcts.reform.fpl.model.order.generated;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.json.converter.BasicChildConverter;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.GeneratedOrderTypeDescriptor;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.interfaces.AmendableOrder;
import uk.gov.hmcts.reform.fpl.model.interfaces.RemovableOrder;
import uk.gov.hmcts.reform.fpl.model.interfaces.TranslatableItem;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderSubtype.FINAL;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.EMERGENCY_PROTECTION_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement.NO;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.TIME_DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.parseLocalDateFromStringUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.parseLocalDateTimeFromStringUsingFormat;

@Slf4j
@Data
@Builder(toBuilder = true)
public class GeneratedOrder implements RemovableOrder, AmendableOrder, TranslatableItem {

    // this is the new type
    private final String orderType;
    private final String type;
    private final String title;
    private final String details;
    private final DocumentReference document;
    private final DocumentReference documentConfidential;
    private final DocumentReference translatedDocument;
    private final DocumentReference unsealedDocumentCopy;
    private final LocalDateTime translationUploadDateTime;
    private final LanguageTranslationRequirement translationRequirements;
    private final LocalDate amendedDate;
    private final String dateOfIssue;
    private final LocalDateTime dateTimeIssued;
    private final LocalDate approvalDate;
    private final LocalDateTime approvalDateTime;
    private final String date;
    private final JudgeAndLegalAdvisor judgeAndLegalAdvisor;
    private final FurtherDirections furtherDirections;
    private final String expiryDate;
    private final String courtName;
    private final String uploader;
    private final String uploadedOrderDescription;
    @JsonSerialize(contentConverter = BasicChildConverter.class)
    private final List<Element<Child>> children;
    private final List<Element<Other>> others;
    private final String childrenDescription;
    private final String specialGuardians;
    private final String othersNotified;
    private String removalReason;
    private String linkedApplicationId;
    private String markedFinal;
    private final DocumentReference notificationDocument;

    @JsonIgnore
    public boolean isRemovable() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean hasBeenTranslated() {
        return Objects.nonNull(translatedDocument);
    }

    @JsonIgnore
    public boolean isFinalOrder() {
        if (isNewVersion()) {
            return YesNo.YES == YesNo.fromString(markedFinal);
        }

        GeneratedOrderTypeDescriptor descriptor = GeneratedOrderTypeDescriptor.fromType(this.type);

        if (EMERGENCY_PROTECTION_ORDER.equals(descriptor.getType())) {
            return true;
        }

        return FINAL.equals(descriptor.getSubtype());
    }

    @Override
    public String asLabel() {
        return String.format("%s - %s",
            defaultIfEmpty(title, type),
            isNewVersion() ? formatLocalDateTimeBaseUsingFormat(dateTimeIssued, DATE) : dateOfIssue
        );
    }

    @Override
    public LocalDateTime translationUploadDateTime() {
        return translationUploadDateTime;
    }

    @Override
    public LanguageTranslationRequirement getTranslationRequirements() {
        return defaultIfNull(translationRequirements, NO);
    }

    @Override
    public LocalDate amendableSortDate() {
        if (null != approvalDate) {
            return approvalDate;
        }

        if (null != approvalDateTime) {
            return approvalDateTime.toLocalDate();
        }

        try {
            if (null != dateOfIssue) {
                return parseLocalDateFromStringUsingFormat(dateOfIssue, DATE);
            }
        } catch (DateTimeParseException ignored) {
            log.warn("Could not parse {} with format {}", dateOfIssue, DATE);
        }

        try {
            if (null != date) {
                return parseLocalDateTimeFromStringUsingFormat(date, TIME_DATE).toLocalDate();
            }
        } catch (DateTimeParseException ignored) {
            log.warn("Could not parse {} with format {}", date, TIME_DATE);
        }

        log.warn("Could not find any date to sort amendable list by, falling back to null");
        return null;
    }

    @JsonIgnore
    public List<UUID> getChildrenIDs() {
        if (ObjectUtils.isEmpty(children)) {
            return List.of();
        }

        return children.stream().map(Element::getId).collect(Collectors.toList());
    }

    @JsonIgnore
    public boolean isNewVersion() {
        return Objects.nonNull(dateTimeIssued);
    }

    @Override
    public DocumentReference getDocument() {
        return document;
    }

    @JsonIgnore
    @Override
    public String getModifiedItemType() {
        return type;
    }

    public List<Element<Other>> getOthers() {
        return defaultIfNull(others, new ArrayList<>());
    }

    @JsonIgnore
    @Override
    public List<Element<Other>> getSelectedOthers() {
        return this.getOthers();
    }

    @JsonIgnore
    public boolean isConfidential() {
        return isNotEmpty(documentConfidential);
    }

    @JsonIgnore
    public DocumentReference getDocumentOrDocumentConfidential() {
        return (isConfidential()) ? documentConfidential : document;
    }
}
