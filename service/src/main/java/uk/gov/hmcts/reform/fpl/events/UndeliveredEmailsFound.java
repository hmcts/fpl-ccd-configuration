package uk.gov.hmcts.reform.fpl.events;

import lombok.Value;
import uk.gov.hmcts.reform.fpl.model.UndeliveredEmail;

import java.util.List;

@Value
public class UndeliveredEmailsFound {
    List<UndeliveredEmail> undeliveredEmails;
}
