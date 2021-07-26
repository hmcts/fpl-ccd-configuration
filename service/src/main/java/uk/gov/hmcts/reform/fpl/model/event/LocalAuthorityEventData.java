package uk.gov.hmcts.reform.fpl.model.event;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.fpl.model.Colleague;
import uk.gov.hmcts.reform.fpl.model.LocalAuthority;
import uk.gov.hmcts.reform.fpl.model.Temp;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;

import java.util.List;

@Data
@Jacksonized
@Builder(toBuilder = true)
public class LocalAuthorityEventData {

    @Temp
    private LocalAuthority localAuthority;
    @Temp
    private String localAuthorityMainContactShown;
    @Temp
    private DynamicList localAuthorityColleaguesList;
    @Temp
    private List<Element<Colleague>> localAuthorityColleagues;
}
