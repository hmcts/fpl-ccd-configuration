package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import net.serenitybdd.rest.SerenityRest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.docmosis.DocmosisHelper;
import uk.gov.hmcts.reform.fpl.model.User;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

import static org.apache.http.HttpStatus.SC_OK;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DocumentService {

    private final AuthenticationService authenticationService;
    private final DocmosisHelper docmosisHelper = new DocmosisHelper();

    //TODO Local env does not have required font, which cause different page layout, thus footer and header removal
    public String getPdfContent(DocumentReference documentReference, User user, String... ignores) {
        byte[] binaries = getDocument(documentReference, user);
        String text = docmosisHelper.extractPdfContent(binaries);
        return docmosisHelper.remove(text, ignores);
    }

    private byte[] getDocument(DocumentReference documentReference, User user) {
        return SerenityRest
            .given()
            .headers(authenticationService.getAuthorizationHeaders(user))
            .body(documentReference.getBinaryUrl())
            .get("/testing-support/document")
            .then()
            .statusCode(SC_OK)
            .extract()
            .asByteArray();
    }

}
