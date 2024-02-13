package uk.gov.hmcts.reform.fpl.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.fpl.model.interfaces.NotifyDocumentUploaded;

import java.util.Optional;
import java.util.UUID;

import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatDateUsingFormat;

@Data
@SuperBuilder(toBuilder = true)
@Jacksonized
@EqualsAndHashCode(callSuper = true)
public class RespondentStatementV2 extends SupportingEvidenceBundle implements NotifyDocumentUploaded {
    private String respondentName;
    private UUID respondentId;

    @Override
    public String asLabel() {
        return String.format("%s - %s - %s", "Respondent Statement", getDocument().getFilename(),
            formatDateUsingFormat(getDocument().getCreatedOn(), DATE));
    }
}
