package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.config.EpsLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityCodeLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityIdLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.LocalAuthority;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class LocalAuthorityService {

    private final IdamClient idamClient;
    private final RequestData requestData;
    private final EpsLookupConfiguration epsLocalAuthorities;
    private final LocalAuthorityIdLookupConfiguration localAuthorityIds;
    private final LocalAuthorityCodeLookupConfiguration localAuthorityCodes;
    private final LocalAuthorityNameLookupConfiguration localAuthorityNames;

    public String getLocalAuthorityCode() {
        UserInfo userInfo = idamClient.getUserInfo(requestData.authorisation());
        String email = userInfo.getSub();
        String domain = extractEmailDomain(email);

        return localAuthorityCodes.getLocalAuthorityCode(domain);
    }

    public String getLocalAuthorityName(String localAuthorityCode) {
        return localAuthorityNames.getLocalAuthorityName(localAuthorityCode);
    }

    public String getLocalAuthorityId(String localAuthorityCode) {
        return localAuthorityIds.getLocalAuthorityId(localAuthorityCode);
    }

    public List<LocalAuthority> getOutsourcingLocalAuthorities(String organisationId) {
        List<String> localAuthorities = epsLocalAuthorities.getLocalAuthorities(organisationId);

        return localAuthorities.stream()
            .map(localAuthorityCode -> LocalAuthority.builder()
                .code(localAuthorityCode)
                .name(getLocalAuthorityName(localAuthorityCode))
                .build())
            .collect(Collectors.toList());
    }

    private String extractEmailDomain(String email) {
        int start = email.indexOf('@');

        return email.toLowerCase().substring(start + 1);
    }
}
