package uk.gov.hmcts.reform.fpl.service.email.content;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.sendtogatekeeper.NotifyGatekeeperTemplate;
import uk.gov.hmcts.reform.fpl.service.email.content.base.NotifyTemplateContentProvider;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GatekeeperEmailContentProvider extends NotifyTemplateContentProvider {
    private final LocalAuthorityNameLookupConfiguration config;

    @Autowired
    protected GatekeeperEmailContentProvider(@Value("${ccd.ui.base.url}") String uiBaseUrl,
                                             ObjectMapper mapper,
                                             LocalAuthorityNameLookupConfiguration config) {
        super(uiBaseUrl, mapper);
        this.config = config;
    }

    public NotifyGatekeeperTemplate buildGatekeeperNotification(CaseDetails caseDetails,
                                                                String localAuthorityCode) {

        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        NotifyGatekeeperTemplate template = super.buildNotifyTemplate(new NotifyGatekeeperTemplate(),
            caseDetails.getId(),
            caseData.getOrders(),
            caseData.getHearing(),
            caseData.getRespondents1());

        template.setLocalAuthority(config.getLocalAuthorityName(localAuthorityCode));

        return template;
    }

    public String buildRecipientsLabel(List<String> emailList, String recipientEmail) {
        String formattedRecipients = emailList.stream()
            .filter(email -> !recipientEmail.equals(email))
            .collect(Collectors.joining(", "));

        if (!formattedRecipients.isEmpty()) {
            return String.format("%s has also received this notification", formattedRecipients);
        }
        return "";
    }
}
