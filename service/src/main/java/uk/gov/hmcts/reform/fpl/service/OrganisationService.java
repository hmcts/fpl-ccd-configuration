package uk.gov.hmcts.reform.fpl.service;

import com.google.common.collect.ImmutableList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityUserLookupConfiguration;
import uk.gov.hmcts.reform.fpl.exceptions.UnknownLocalAuthorityCodeException;

import java.util.List;

@Service
@Slf4j
public class OrganisationService {
    private final LocalAuthorityUserLookupConfiguration localAuthorityUserLookupConfiguration;


    @Autowired
    public OrganisationService(LocalAuthorityUserLookupConfiguration localAuthorityUserLookupConfiguration) {
        this.localAuthorityUserLookupConfiguration = localAuthorityUserLookupConfiguration;
    }


    public List<String> findUserIdsInSameOrganisation(String userId, String localAuthorityCode) {
        try {
            return localAuthorityUserLookupConfiguration.getUserIds(localAuthorityCode);
        } catch (UnknownLocalAuthorityCodeException ex) {
            log.warn("Can't find LocalAuthority for code: {} in app config", localAuthorityCode);
            return ImmutableList.of(userId);
        }
    }
}
