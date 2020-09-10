package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.config.CtscEmailLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.HmctsCourtLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;

import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class HmctsAdminNotificationHandler {
    private final CtscEmailLookupConfiguration ctscEmailLookupConfiguration;
    private final HmctsCourtLookupConfiguration hmctsCourtLookupConfiguration;

    public String getHmctsAdminEmail(final CaseData caseData) {
        if (YES.getValue().equals(caseData.getSendToCtsc())) {
            return ctscEmailLookupConfiguration.getEmail();
        }

        return hmctsCourtLookupConfiguration.getCourt(caseData.getCaseLocalAuthority()).getEmail();
    }
}
