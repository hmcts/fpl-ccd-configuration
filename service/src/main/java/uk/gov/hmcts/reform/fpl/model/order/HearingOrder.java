package uk.gov.hmcts.reform.fpl.model.order;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import uk.gov.hmcts.reform.fpl.enums.CMOStatus;
import uk.gov.hmcts.reform.fpl.enums.HearingOrderType;
import uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement;
import uk.gov.hmcts.reform.fpl.enums.ModifiedOrderType;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.interfaces.AmendableOrder;
import uk.gov.hmcts.reform.fpl.model.interfaces.RemovableOrder;
import uk.gov.hmcts.reform.fpl.model.interfaces.TranslatableItem;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static java.lang.String.format;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.APPROVED;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.DRAFT;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.SEND_TO_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.HearingOrderType.AGREED_CMO;
import static uk.gov.hmcts.reform.fpl.enums.HearingOrderType.C21;
import static uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement.NO;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService.DOCUMENT_ACKNOWLEDGEMENT_KEY;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.formatJudgeTitleAndName;

@Data
@Builder(toBuilder = true)
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@EqualsAndHashCode
public class HearingOrder implements RemovableOrder, AmendableOrder, TranslatableItem {
    private List<String> documentAcknowledge;
    private String title;
    private HearingOrderType type;
    private DocumentReference order;
    private DocumentReference orderConfidential;
    private DocumentReference translatedOrder;
    private DocumentReference lastUploadedOrder;
    private String hearing;
    // Case management order, 21 June 2020
    private UUID hearingId;
    private LocalDate dateSent;
    private LocalDate dateIssued;
    private final LocalDate amendedDate;
    private final LocalDateTime translationUploadDateTime;
    private LanguageTranslationRequirement translationRequirements;
    private CMOStatus status;
    private String judgeTitleAndName;
    private String requestedChanges;
    // DFPL-552 the uploading option is removed, but user can still access exiting Case Summary
    // DFPL-526 Future case summaries are collected and put in the new 'Hearing Document' tab .
    private List<Element<SupportingEvidenceBundle>> supportingDocs;
    private String removalReason;
    private final List<Element<Other>> others;
    private String othersNotified;
    private String uploaderEmail;

    public static HearingOrder from(DocumentReference order, HearingBooking hearing, LocalDate date) {
        return from(order, hearing, date, AGREED_CMO, null, null, null);
    }

    public static HearingOrder from(DocumentReference order, HearingBooking hearing, LocalDate date,
                                    HearingOrderType orderType,
                                    List<Element<SupportingEvidenceBundle>> supportingDocs,
                                    LanguageTranslationRequirement translationRequirement) {
        return from(order, hearing, date, orderType, supportingDocs, translationRequirement, null);
    }

    public static HearingOrder from(DocumentReference order, HearingBooking hearing, LocalDate date,
                                    HearingOrderType orderType,
                                    List<Element<SupportingEvidenceBundle>> supportingDocs,
                                    LanguageTranslationRequirement translationRequirement, UUID hearingId) {
        return HearingOrder.builder()
            .type(orderType)
            .title(orderType == AGREED_CMO ? "Agreed CMO discussed at hearing" : "Draft CMO from advocates' meeting")
            .order(order)
            .hearing(hearing.toLabel())
            .hearingId(hearingId)
            .dateSent(date)
            .status(orderType == AGREED_CMO ? SEND_TO_JUDGE : DRAFT)
            .judgeTitleAndName(formatJudgeTitleAndName(hearing.getJudgeAndLegalAdvisor()))
            .supportingDocs(supportingDocs)
            .translationRequirements(translationRequirement)
            .build();
    }

    public static HearingOrder from(DraftOrder draftOrder) {
        return HearingOrder.builder()
            .type(C21)
            .title(draftOrder.getTitle())
            .order(draftOrder.getDocument())
            .hearing("No hearing")
            .dateSent(draftOrder.getDateUploaded())
            .status(SEND_TO_JUDGE)
            .judgeTitleAndName("")
            .supportingDocs(null)
            .translationRequirements(NO)
            .build();
    }

    @JsonIgnore
    public boolean isRemovable() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean hasBeenTranslated() {
        return Objects.nonNull(translatedOrder);
    }

    @Override
    public LocalDateTime translationUploadDateTime() {
        return translationUploadDateTime;
    }

    @JsonIgnore
    @Override
    public DocumentReference getTranslatedDocument() {
        return translatedOrder;
    }

    @Override
    public LanguageTranslationRequirement getTranslationRequirements() {
        return defaultIfNull(translationRequirements, NO);
    }

    @Override
    public String asLabel() {
        if (type == C21) {
            return format("Draft order sent on %s for %s, %s", formatLocalDateToString(dateSent, DATE),
                getTitle(),
                getDocument().getFilename());
        } else {
            if (APPROVED.equals(status)) {
                return format("Sealed case management order issued on %s",
                    formatLocalDateToString(dateIssued, DATE));
            }

            if (SEND_TO_JUDGE.equals(status)) {
                return format("Agreed case management order sent on %s, %s",
                    formatLocalDateToString(dateSent, DATE),
                    getDocument().getFilename());
            }

            return format("Draft case management order sent on %s, %s",
                formatLocalDateToString(dateSent, DATE),
                getDocument().getFilename());
        }
    }

    @Override
    public LocalDate amendableSortDate() {
        return dateIssued;
    }

    @JsonIgnore
    @Override
    public DocumentReference getDocument() {
        return order;
    }

    @JsonIgnore
    @Override
    public String getModifiedItemType() {
        return ModifiedOrderType.CASE_MANAGEMENT_ORDER.getLabel();
    }

    public List<Element<Other>> getOthers() {
        return defaultIfNull(others, new ArrayList<>());
    }

    @JsonIgnore
    @Override
    public List<Element<Other>> getSelectedOthers() {
        return this.getOthers();
    }

    public List<String> getDocumentAcknowledge() {
        if (this.documentAcknowledge == null) {
            this.documentAcknowledge = new ArrayList<>();
        }
        if (order != null && !this.documentAcknowledge.contains(DOCUMENT_ACKNOWLEDGEMENT_KEY)) {
            this.documentAcknowledge.add(DOCUMENT_ACKNOWLEDGEMENT_KEY);
        }
        return this.documentAcknowledge;
    }

    @JsonIgnore
    public boolean isConfidentialOrder() {
        return isNotEmpty(orderConfidential);
    }
}
