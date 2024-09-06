package uk.gov.hmcts.reform.fpl.service.cafcass.api;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.config.CafcassSystemUpdateUserConfiguration;
import uk.gov.hmcts.reform.fpl.exceptions.EmptyFileException;
import uk.gov.hmcts.reform.fpl.service.SecureDocStoreService;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CafcassApiDocumentService {
    private final SecureDocStoreService secureDocStoreService;
    private final CafcassSystemUpdateUserConfiguration cafcassSysUser;

    public byte[] downloadDocumentByDocumentId(String documentId) throws IllegalArgumentException, EmptyFileException {
        return secureDocStoreService.downloadDocument(documentId, cafcassSysUser.getUserName(),
            cafcassSysUser.getPassword());
    }
}
