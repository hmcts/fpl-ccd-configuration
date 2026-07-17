package uk.gov.hmcts.reform.fpl.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@Builder
public class CaseNote {
    @CCD(label = "Created by")
    private final String createdBy;
    @CCD(label = "Created on")
    private final LocalDate date;
    @CCD(label = "Note", typeOverride = FieldType.TextArea)
    private final String note;
}
