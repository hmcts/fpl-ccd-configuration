package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.UserRole;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class DocumentUploaderService {

    private final IdamClient idamClient;
    private final RequestData requestData;

    public String getUploadedDocumentUserDetails() {
        UserDetails userDetails = idamClient.getUserDetails(requestData.authorisation());

        boolean isHmctsUser = userDetails.getRoles().stream().anyMatch(UserRole::isHmctsUser);

        return isHmctsUser ? "HMCTS" : userDetails.getEmail();
    }
}

