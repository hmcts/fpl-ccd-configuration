package uk.gov.hmcts.reform.fpl.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClientApi;
import uk.gov.hmcts.reform.ccd.document.am.model.Document;
import uk.gov.hmcts.reform.ccd.document.am.model.UploadResponse;
import uk.gov.hmcts.reform.document.DocumentUploadClientApi;
import uk.gov.hmcts.reform.fpl.request.RequestData;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.utils.DocumentManagementStoreLoader.successfulDocumentUploadResponse;

@ExtendWith(SpringExtension.class)
class SecureDocStoreServiceTest {

    private static final String USER_ID = "1";
    private static final String AUTH_TOKEN = "Bearer token";
    private static final String SERVICE_AUTH_TOKEN = "Bearer service token";

    @Mock
    private AuthTokenGenerator authTokenGenerator;
    @Mock
    private CaseDocumentClientApi caseDocumentClientApi;
    @Mock
    private RequestData requestData;
    @InjectMocks
    private SecureDocStoreService secureDocStoreService;

    @BeforeEach
    void setup() {
        given(authTokenGenerator.generate()).willReturn(SERVICE_AUTH_TOKEN);
        given(requestData.authorisation()).willReturn(AUTH_TOKEN);
        given(requestData.userId()).willReturn(USER_ID);
    }


    @Test
    void shouldReturnFirstUploadedDocument() {
//        UploadResponse request = successfulDocumentUploadResponse();
//        given(caseDocumentClientApi.uploadDocuments(eq(AUTH_TOKEN), eq(SERVICE_AUTH_TOKEN), any()))
//            .willReturn(request);
//
//        Document document = secureDocStoreService.uploadDocument(new byte[0], "file", "text/pdf");
//
//        Assertions.assertThat(document).isEqualTo(request.getDocuments().get(0));
    }


    @Test
    void uploadDocument() {
    }

    @Test
    void downloadDocument() {
    }
}
