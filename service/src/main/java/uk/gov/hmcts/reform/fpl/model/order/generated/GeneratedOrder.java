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
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Slf4j
@Data
@Builder(toBuilder = true)
public class GeneratedOrder implements RemovableOrder, AmendableOrder, TranslatableItem {

    // this is the new type
    @CCD(label = "Type of order", showCondition = "orderType=\"DO_NOT_SHOW\"")
    private final String orderType;
    @CCD(label = "Type of order")
    private final String type;
    @CCD(label = "Order title")
    private final String title;
    @CCD(label = "Order details", showCondition = "date!=\"*\"", typeOverride = FieldType.TextArea)
    private final String details;
    @CCD(
            label = "Order document",
            showCondition = "date=\"*\" OR dateTimeIssued=\"*\"",
            categoryID = "orders",
            typeOverride = FieldType.Document
    )
    private final DocumentReference document;
    @CCD(
            label = "Confidential Order document",
            showCondition = "date=\"*\" OR dateTimeIssued=\"*\"",
            categoryID = "ordersConfidential",
            typeOverride = FieldType.Document
    )
    private final DocumentReference documentConfidential;
    @CCD(label = "Translated document", categoryID = "orders", typeOverride = FieldType.Document)
    private final DocumentReference translatedDocument;
    @CCD(
            label = "Unsealed document copy",
            showCondition = "unsealedDocumentCopy=\"DO_NOT_SHOW\"",
            typeOverride = FieldType.Document
    )
    private final DocumentReference unsealedDocumentCopy;
    @CCD(label = "Welsh translation upload time", showCondition = "translatedDocument=\"DO_NOT_SHOW\"")
    private final LocalDateTime translationUploadDateTime;
    @CCD(label = " ", showCondition = "translationRequirements=\"DO_NOT_SHOW\"", typeOverride = FieldType.Text)
    private final LanguageTranslationRequirement translationRequirements;
    @CCD(label = "Amended")
    private final LocalDate amendedDate;
    @CCD(label = "Starts on")
    private final String dateOfIssue;
    @CCD(label = "Date and time issued", showCondition = "dateTimeIssued=\"DO_NOT_SHOW\"")
    private final LocalDateTime dateTimeIssued;
    @CCD(label = "Approval date")
    private final LocalDate approvalDate;
    @CCD(label = "Approval date")
    private final LocalDateTime approvalDateTime;
    @CCD(label = "Date and time of upload")
    private final String date;
    @CCD(label = "Judge and Justices' Legal Adviser", showCondition = "type = \"DO NOT SHOW\"")
    private final JudgeAndLegalAdvisor judgeAndLegalAdvisor;
    @CCD(label = "Directions")
    private final FurtherDirections furtherDirections;
    @CCD(label = "Ends on")
    private final String expiryDate;
    @CCD(label = " ", showCondition = "type = \"DO NOT SHOW\"")
    private final String courtName;
    @CCD(label = "Uploaded by")
    private final String uploader;
    @CCD(label = "Order description", typeOverride = FieldType.TextArea)
    private final String uploadedOrderDescription;
    @CCD(
            label = " ",
            showCondition = "type = \"DO NOT SHOW\"",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "BasicChild"
    )
    @JsonSerialize(contentConverter = BasicChildConverter.class)
    private final List<Element<Child>> children;
    @CCD(
            label = " ",
            showCondition = "type = \"DO NOT SHOW\"",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "Others"
    )
    private final List<Element<Other>> others;
    @CCD(label = "Children")
    private final String childrenDescription;
    @CCD(label = "Special guardians")
    private final String specialGuardians;
    @CCD(label = "Others notified")
    private final String othersNotified;
    @CCD(label = "Reason for removal", typeOverride = FieldType.TextArea)
    private String removalReason;
    @CCD(label = "Linked application", showCondition = "type = \"DO NOT SHOW\"")
    private String linkedApplicationId;
    @CCD(label = "Is the order final?", showCondition = "type = \"DO NOT SHOW\"", typeOverride = FieldType.YesOrNo)
    private String markedFinal;
    @CCD(
            label = "Notification document",
            showCondition = "notificationDocument!=\"\"",
            categoryID = "orders",
            typeOverride = FieldType.Document
    )
    private final DocumentReference notificationDocument;
    @CCD(
            label = "Child to live with order details (C43)",
            showCondition = "date!=\"*\"",
            typeOverride = FieldType.TextArea
    )
    private String childArrangementsLiveWithDetails;
    @CCD(
            label = "Contact with child order details (C43)",
            showCondition = "date!=\"*\"",
            typeOverride = FieldType.TextArea
    )
    private String childArrangementsContactWithDetails;
    @CCD(label = "Specific issue order details (C43)", showCondition = "date!=\"*\"", typeOverride = FieldType.TextArea)
    private String specificIssueOrderDetails;
    @CCD(
            label = "Prohibited steps order details (C43)",
            showCondition = "date!=\"*\"",
            typeOverride = FieldType.TextArea
    )
    private String prohibitedStepsOrderDetails;

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

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(
          label = "Sent for translation",
          showCondition = "needTranslation=\"YES\" AND translatedDocument!=\"*\"",
          typeOverride = FieldType.Label
  )
  private String sentForTranslationLabel;
  // ==== end synthesised definition-only fields ====
}
