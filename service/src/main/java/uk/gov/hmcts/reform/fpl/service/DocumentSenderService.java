package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.DocumentSent;
import uk.gov.hmcts.reform.fpl.model.DocumentSentToParties;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

import java.util.List;

import static java.time.LocalDateTime.now;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DocumentSenderService {

    private final DateFormatterService dateFormatterService;

    public DocumentSentToParties send(DocumentReference documentReference) {
        //TODO  generate, stitch and send here
        return DocumentSentToParties.builder()
            .document(DocumentSent.builder()
                .document(documentReference)
                .sentAt(dateFormatterService.formatLocalDateTimeBaseUsingFormat(now(), "h:mma, d MMMM yyyy"))
                .build())
            .parties(getPartyNames(documentReference))
            .build();
    }

    //TODO implement me
    private List<String> getPartyNames(DocumentReference documentReference) {
        int random = RandomUtils.nextInt(0, 9) % 3;
        if (random == 1) {
            return List.of("John Smith");
        } else if (random == 2) {
            return List.of("George White", "Mark Green");
        }

        return List.of("John Smith", "Mark Green", "Alex Brown");
    }
}
