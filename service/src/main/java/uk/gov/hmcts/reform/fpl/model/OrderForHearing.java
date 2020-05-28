package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.time.LocalDate;
import java.util.List;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.fpl.model.common.DocumentReference.buildFromDocument;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.parseLocalDateFromStringUsingFormat;

@Data
@AllArgsConstructor
public class OrderForHearing {
    public final String hearingDate;
    public final String dateOfIssue;

    public List<Element<Direction>> directions;
    public DocumentReference orderDoc;

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
