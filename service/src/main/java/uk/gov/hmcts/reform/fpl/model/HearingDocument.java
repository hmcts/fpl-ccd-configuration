package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.common.DocumentMetaData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor(force = true)
public class HearingDocument extends DocumentMetaData {
    protected String hearing;
    protected DocumentReference document;
    protected String hasConfidentialAddress;
    protected List<String> documentAcknowledge;

    public String getHasConfidentialAddress() {
        return (document != null && (!YesNo.isYesOrNo(hasConfidentialAddress)))
            ? YesNo.NO.getValue() : hasConfidentialAddress;
    }

    @JsonIgnore
    @Override
    public DocumentReference getTypeOfDocument() {
        return document;
    }

}
