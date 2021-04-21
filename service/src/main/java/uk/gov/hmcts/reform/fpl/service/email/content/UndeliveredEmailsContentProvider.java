package uk.gov.hmcts.reform.fpl.service.email.content;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.UndeliveredEmail;
import uk.gov.hmcts.reform.fpl.model.notify.UndeliveredEmailsNotifyData;
import uk.gov.hmcts.reform.fpl.service.email.content.base.AbstractEmailContentProvider;

import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;
import static java.lang.System.lineSeparator;
import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.substringAfter;
import static uk.gov.hmcts.reform.fpl.service.email.NotificationService.SEPARATOR;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper.isCaseNumber;

@Service
public class UndeliveredEmailsContentProvider extends AbstractEmailContentProvider {

    public UndeliveredEmailsNotifyData buildParameters(List<UndeliveredEmail> undeliveredEmails) {
        final String formattedEmails = undeliveredEmails.stream()
            .map(this::formatUndeliveredEmail)
            .collect(joining("\n\n"));

        return UndeliveredEmailsNotifyData.builder()
            .emails(formattedEmails)
            .build();
    }

    private String formatUndeliveredEmail(UndeliveredEmail undeliveredEmail) {
        final String reference = normalizeReference(undeliveredEmail.getReference());

        final List<String> emailLines = new ArrayList<>();
        emailLines.add(format("To: %s", undeliveredEmail.getRecipient()));
        emailLines.add(format("Subject: %s", undeliveredEmail.getSubject()));

        if (isCaseNumber(reference)) {
            emailLines.add(format("Case id: %s", reference));
        } else if (isNotEmpty(reference)) {
            emailLines.add(format("Reference: %s", reference));
        }

        return String.join(lineSeparator(), emailLines);
    }

    private String normalizeReference(String reference) {
        if (reference == null) {
            return null;
        }
        return reference.contains(SEPARATOR) ? substringAfter(reference, SEPARATOR) : reference;
    }
}
