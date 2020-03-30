package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.config.CtscEmailLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.HmctsCourtLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.event.EventData;

import java.util.Map;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class HmctsAdminNotificationHandler {
    private final CtscEmailLookupConfiguration ctscEmailLookupConfiguration;
    private final HmctsCourtLookupConfiguration hmctsCourtLookupConfiguration;

    public String getHmctsAdminEmail(final EventData eventData) {
        String ctscValue = getCtscValue(eventData.getCaseDetails().getData());

        if (ctscValue.equals("Yes")) {
            return ctscEmailLookupConfiguration.getEmail();
        }

        return hmctsCourtLookupConfiguration.getCourt(eventData.getLocalAuthorityCode()).getEmail();
    }

    private String getCtscValue(final Map<String, Object> caseData) {
        return caseData.get("sendToCtsc") != null ? caseData.get("sendToCtsc").toString() : "No";
    }
}
