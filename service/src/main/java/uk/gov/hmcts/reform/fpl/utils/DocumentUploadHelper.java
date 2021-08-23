package uk.gov.hmcts.reform.fpl.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.UserRole;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class DocumentUploadHelper {

    private final IdamClient idamClient;
    private final RequestData requestData;

    public String getUploadedDocumentUserDetails() {
        UserDetails userDetails = getUserDetails();

        boolean isHmctsUser = userDetails.getRoles().stream().anyMatch(UserRole::isHmctsUser);

        return isHmctsUser ? "HMCTS" : userDetails.getEmail();
    }

    public String getUploadedDocumentName() {
        UserDetails userDetails = getUserDetails();
        return userDetails.getFullName();
    }

    private UserDetails getUserDetails() {
        return idamClient.getUserDetails(requestData.authorisation());
    }
}
