package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "DocumentsSentToParty", generate = true)
@Data
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SentDocuments {
    @CCD(label = "Recipient")
    private final String partyName;
    @CCD(label = "Document ", typeOverride = FieldType.Collection, typeParameterOverride = "DocumentSentToParty")
    private final List<Element<SentDocument>> documentsSentToParty;

    public SentDocuments(String partyName, List<Element<SentDocument>> documentsSentToParty) {
        this.partyName = partyName;
        this.documentsSentToParty = defaultIfNull(documentsSentToParty, new ArrayList<>());
    }

    public SentDocuments(String partyName) {
        this(partyName, new ArrayList<>());
    }

    public SentDocuments addDocument(SentDocument printedDocument) {
        this.documentsSentToParty.add(ElementUtils.element(printedDocument));
        return this;
    }
}
