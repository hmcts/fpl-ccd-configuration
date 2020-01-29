package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.PrintedDocument;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

import java.util.ArrayList;
import java.util.List;

import static java.time.LocalDateTime.now;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DocumentSenderService {

    private final DateFormatterService dateFormatterService;

    public List<PrintedDocument> send(DocumentReference mainDocument,
                                      Long ccdCaseNumber,
                                      String familyManCaseNumber,
                                      List<Representative> representativesServedByPost) {
        //TODO  generate, stitch and send here

        List<PrintedDocument> printedDocuments = new ArrayList<>();

        for (Representative representative : representativesServedByPost) {

            //TODO create general letter and cover letter - separate PR
            //coverDocumentsService.createGeneralLetter(familyManCaseNumber, representative);
            //coverDocumentsService.createCoverSheet(ccdCaseNumber, representative);

            //TODO stitching service, pass in general letter, cover sheet, mainDocument - separate PR

            printedDocuments.add(PrintedDocument.builder()
                .representativeName(representative.getFullName())
                //add compiled document from stitching service
                .sentAt(dateFormatterService.formatLocalDateTimeBaseUsingFormat(now(), "h:mma, d MMMM yyyy")).build());
        }

        return printedDocuments;
    }
}
