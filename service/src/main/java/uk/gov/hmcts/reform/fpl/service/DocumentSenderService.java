package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.DocumentSentToParty;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;

import static java.time.LocalDateTime.now;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DocumentSenderService {

    private final Clock clock;
    private final DateFormatterService dateFormatterService;

    public List<DocumentSentToParty> send(DocumentReference documentToBeSent,
                                          List<Representative> representativesServedByPost) {
        //TODO  generate, stitch and send here

        List<DocumentSentToParty> documentsSentToParties = new ArrayList<>();

        for (Representative representative : representativesServedByPost) {

            //TODO create general letter and cover letter - separate PR
            //coverDocumentsService.createGeneralLetter(familyManCaseNumber, representative);
            //coverDocumentsService.createCoverSheet(ccdCaseNumber, representative);

            //TODO stitching service, pass in general letter, cover sheet, mainDocument - separate PR

            documentsSentToParties.add(DocumentSentToParty.builder()
                .partyName(representative.getFullName())
                //TODO use stitched document
                .document(documentToBeSent)
                //add compiled document from stitching service
                .sentAt(dateFormatterService
                    .formatLocalDateTimeBaseUsingFormat(now(clock), "h:mma, d MMMM yyyy")).build());
        }

        return documentsSentToParties;
    }
}
