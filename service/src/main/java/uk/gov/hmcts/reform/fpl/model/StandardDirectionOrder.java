package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.enums.AmendableOrderType;
import uk.gov.hmcts.reform.fpl.enums.OrderStatus;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.interfaces.AmendableOrder;
import uk.gov.hmcts.reform.fpl.model.interfaces.IssuableOrder;
import uk.gov.hmcts.reform.fpl.model.interfaces.RemovableOrder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.SEALED;
import static uk.gov.hmcts.reform.fpl.model.common.DocumentReference.buildFromDocument;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.parseLocalDateFromStringUsingFormat;

@Data
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StandardDirectionOrder implements IssuableOrder, RemovableOrder, AmendableOrder {
    public static final UUID COLLECTION_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    private final String hearingDate;
    private final String dateOfIssue;
    private final OrderStatus orderStatus;
    private final JudgeAndLegalAdvisor judgeAndLegalAdvisor;
    private final LocalDate dateOfUpload;
    private final String uploader;
    private List<Element<Direction>> directions;
    private DocumentReference orderDoc;
    private DocumentReference lastUploadedOrder;
    private String removalReason;
    private final LocalDate amendedDate;

    private final DocumentReference unsealedDocumentCopy;
    private final List<Element<CustomDirection>> customDirections;
    private final List<Element<StandardDirection>> standardDirections;

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
        return COLLECTION_ID;
    }

    @Override
    @JsonIgnore
    public boolean isRemovable() {
        return isSealed();
    }

    @Override
    public String asLabel() {
        String formattedDate = Optional.ofNullable(dateOfIssue)
            .orElse(formatLocalDateToString(defaultIfNull(dateOfUpload, LocalDate.now()), DATE));

        return "Gatekeeping order - " + formattedDate;
    }

    @Override
    public LocalDate amendableSortDate() {
        return null != dateOfUpload ? dateOfUpload
                                    : parseLocalDateFromStringUsingFormat(dateOfIssue, DATE);
    }

    @Override
    @JsonIgnore
    public DocumentReference getDocument() {
        return orderDoc;
    }

    @JsonIgnore
    @Override
    public Object getAmendedOrderType() {
        return AmendableOrderType.STANDARD_DIRECTION_ORDER;
    }
}

