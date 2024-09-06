package uk.gov.hmcts.reform.fpl.service.cafcass.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.config.CafcassSystemUpdateUserConfiguration;
import uk.gov.hmcts.reform.fpl.service.SecureDocStoreService;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CafcassApiDocumentServiceTest {
    private SecureDocStoreService secureDocStoreService = mock(SecureDocStoreService.class);
    private CafcassSystemUpdateUserConfiguration cafcassSysUser =
        new CafcassSystemUpdateUserConfiguration("cafcass@test.com", "test");

    private CafcassApiDocumentService underTest;

    @BeforeEach
    void setUpWithMockConverters() {
        underTest = new CafcassApiDocumentService(secureDocStoreService, cafcassSysUser);
    }

    @Test
    void shouldReturnDocumentBinary() {
        UUID docId = UUID.randomUUID();
        byte[] docBinary = "This is a document".getBytes();
        when(secureDocStoreService.downloadDocument(docId.toString(), cafcassSysUser.getUserName(),
            cafcassSysUser.getPassword())).thenReturn(docBinary);

        assertArrayEquals(docBinary, underTest.downloadDocumentByDocumentId(docId.toString()));
    }
}
