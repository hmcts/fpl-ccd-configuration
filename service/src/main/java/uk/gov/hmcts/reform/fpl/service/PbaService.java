package uk.gov.hmcts.reform.fpl.service;

import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.rd.client.PbaApi;
import uk.gov.hmcts.reform.rd.model.PbaOrganisationResponse;

import java.util.List;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Service
@Slf4j
@RequiredArgsConstructor
public class PbaService {

    private final HttpServletRequest httpServletRequest;

    private final UserService userService;

    private final PbaApi pbaRefDataClient;

    private final AuthTokenGenerator authTokenGenerator;

    private final DynamicListService dynamicListService;

    public DynamicList populatePbaDynamicList(String selectedCode) {
        return dynamicListService.asDynamicList(retrievePbaNumbers(),
            defaultIfNull(selectedCode, ""),pba -> pba, pba -> pba);
    }

    private List<String> retrievePbaNumbers() {
        String userAuthToken = httpServletRequest.getHeader(AUTHORIZATION);
        String userEmail = userService.getUserEmail();

        try {
            /*ResponseEntity<PbaOrganisationResponse> responseEntity =
                pbaRefDataClient.retrievePbaNumbers(userAuthToken, authTokenGenerator.generate(), userEmail);

            PbaOrganisationResponse pbaOrganisationResponse = Objects.requireNonNull(responseEntity.getBody());*/
            PbaOrganisationResponse pbaOrganisationResponse =
                pbaRefDataClient.retrievePbaNumbers(userAuthToken, authTokenGenerator.generate(), userEmail);

            return pbaOrganisationResponse.getOrganisationEntityResponse().getPaymentAccount();
        } catch (FeignException.NotFound | FeignException.Forbidden ex) {
            log.error("Error retrieving PBA numbers from PBA Ref Data for user {}", userEmail);
            return List.of();
        }
    }
}
