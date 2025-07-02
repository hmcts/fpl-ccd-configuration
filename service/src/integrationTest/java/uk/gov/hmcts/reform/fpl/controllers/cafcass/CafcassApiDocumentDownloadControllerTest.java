package uk.gov.hmcts.reform.fpl.controllers.cafcass;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.reform.fpl.exceptions.EmptyFileException;
import uk.gov.hmcts.reform.fpl.service.cafcass.api.CafcassApiDocumentService;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.mockito.Mockito.when;

public class CafcassApiDocumentDownloadControllerTest extends CafcassApiControllerBaseTest {
    @MockBean
    private CafcassApiDocumentService cafcassApiDocumentService;

    @Test
    void shouldReturnDocumentBinary() throws Exception {
        UUID docId = UUID.randomUUID();
        byte[] docBinary = "This is a document".getBytes();

        when(cafcassApiDocumentService.downloadDocumentByDocumentId(docId.toString())).thenReturn(docBinary);

        MvcResult rsp = sendRequest(buildGetRequest(String.format("/cases/documents/%s/binary", docId)), 200);
        assertArrayEquals(rsp.getResponse().getContentAsByteArray(), docBinary);
    }

    @Test
    void shouldReturn400IfDocIdInvalid() throws Exception {
        sendRequest(buildGetRequest("/cases/documents/test/binary"), 400);
        sendRequest(buildGetRequest("/cases/documents/s/binary"), 400);
    }

    @Test
    void shouldReturn404IfDocumentNotFound() throws Exception {
        UUID docId = UUID.randomUUID();

        when(cafcassApiDocumentService.downloadDocumentByDocumentId(docId.toString()))
            .thenThrow(new EmptyFileException());

        sendRequest(buildGetRequest(String.format("/cases/documents/%s/binary", docId)), 404);
    }
}
