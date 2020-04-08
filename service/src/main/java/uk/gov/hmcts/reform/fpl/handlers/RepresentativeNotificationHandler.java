package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.event.EventData;
import uk.gov.hmcts.reform.fpl.service.RepresentativeService;
import uk.gov.hmcts.reform.fpl.service.representative.RepresentativeNotificationService;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RepresentativeNotificationHandler {
    private final ObjectMapper objectMapper;
    private final RepresentativeService representativeService;
    private final RepresentativeNotificationService representativeNotificationService;

    public void sendToRepresentativesByServedPreference(final RepresentativeServingPreferences servedPreference,
                                                        final String templateId,
                                                        final Map<String, Object> templateParameters,
                                                        final EventData eventData) {
        CaseData caseData = objectMapper.convertValue(eventData.getCaseDetails().getData(), CaseData.class);
        List<Representative> representatives = representativeService.getRepresentativesByServedPreference(
            caseData.getRepresentatives(), servedPreference);

        if (!representatives.isEmpty()) {
            representativeNotificationService.sendNotificationToRepresentatives(eventData, templateParameters,
                representatives, templateId);
        }
    }
}
