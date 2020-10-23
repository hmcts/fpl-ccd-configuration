package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.enums.OrderStatus;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.interfaces.IssuableOrder;

import java.time.LocalDate;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.SEALED;
import static uk.gov.hmcts.reform.fpl.model.common.DocumentReference.buildFromDocument;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.parseLocalDateFromStringUsingFormat;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StandardDirectionOrder implements IssuableOrder {
    private final String hearingDate;
    private final String dateOfIssue;
    private final OrderStatus orderStatus;
    private final JudgeAndLegalAdvisor judgeAndLegalAdvisor;
    private final LocalDate dateOfUpload;
    private final String uploader;
    private List<Element<Direction>> directions;
    private DocumentReference orderDoc;

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
    public LocalDate getDateOfIssueAsDate() {
        return ofNullable(dateOfIssue)
            .map(date -> parseLocalDateFromStringUsingFormat(date, DATE))
            .orElse(LocalDate.now());
    }
}
