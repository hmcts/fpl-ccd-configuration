package uk.gov.hmcts.reform.fpl.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityUserLookupConfiguration;

import java.util.List;

@Service
public class OrganisationService {
    private final LocalAuthorityUserLookupConfiguration localAuthorityUserLookupConfiguration;


    @Autowired

    public OrganisationService(LocalAuthorityUserLookupConfiguration localAuthorityUserLookupConfiguration) {
        this.localAuthorityUserLookupConfiguration = localAuthorityUserLookupConfiguration;
    }


    public List<String>  getUserIds(String authorization, String localAuthorityCode) {
        return null;
    }
}
