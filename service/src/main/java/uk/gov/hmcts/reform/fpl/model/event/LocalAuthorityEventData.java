package uk.gov.hmcts.reform.fpl.model.event;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.fpl.model.Colleague;
import uk.gov.hmcts.reform.fpl.model.LocalAuthority;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;

import java.util.List;

@Value
@Builder(toBuilder = true)
@Jacksonized
public class LocalAuthorityEventData {

    LocalAuthority localAuthority;
    List<Element<Colleague>> colleagues;
    DynamicList mainContact;

}
