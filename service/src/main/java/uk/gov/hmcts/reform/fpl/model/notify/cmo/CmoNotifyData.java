package uk.gov.hmcts.reform.fpl.model.notify.cmo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.reform.fpl.model.notify.BaseCaseNotifyData;

import java.util.Map;

@Getter
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class CmoNotifyData extends BaseCaseNotifyData {
    private final String subjectLineWithHearingDate;
    private final String digitalPreference;
    private final String reference;
    private final String courtName;
    private final String localAuthorityNameOrRepresentativeFullName;
    @JsonProperty("link_to_document")
    private Map<String, Object> documentLink;
}
