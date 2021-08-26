package uk.gov.hmcts.reform.fpl.service.furtherevidence;

import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

import java.util.UUID;


@Value
@Builder
public class SupportingEvidenceBundleView {

    UUID uuid;
    DocumentReference document;

}
