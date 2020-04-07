package uk.gov.hmcts.reform.fpl.utils;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import uk.gov.service.notify.NotificationClientException;

import java.util.Optional;

import static org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace;
import static uk.gov.service.notify.NotificationClient.prepareUpload;

@Slf4j
public class NotifyAttachedDocumentLinkHelper {
    private NotifyAttachedDocumentLinkHelper() {
    }

    public static Optional<JSONObject> generateAttachedDocumentLink(final byte[] documentContents) {
        try {
            return Optional.of(prepareUpload(documentContents));
        } catch (NotificationClientException e) {
            log.error("Unable to generate an attached document link due to {}, {}", e.getMessage(), getStackTrace(e));
        }

        return Optional.empty();
    }
}
