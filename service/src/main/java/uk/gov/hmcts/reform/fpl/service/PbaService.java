package uk.gov.hmcts.reform.fpl.service;

import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.rd.client.OrganisationApi;
import uk.gov.hmcts.reform.rd.model.PbaOrganisationResponse;

import java.util.List;
import java.util.Optional;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Service
@Slf4j
@RequiredArgsConstructor
public class PbaService {

    private final HttpServletRequest httpServletRequest;

    private final UserService userService;

    private final OrganisationApi pbaRefDataClient;

    private final AuthTokenGenerator authTokenGenerator;

    private final DynamicListService dynamicListService;

    public DynamicList populatePbaDynamicList(String selectedCode) {
        Optional<List<String>> pbaNumbers = retrievePbaNumbers();

        return dynamicListService.asDynamicList(pbaNumbers.orElseGet(() -> List.of("")),
            defaultIfNull(selectedCode, ""),pba -> pba, pba -> pba);
    }

    public Optional<List<String>> retrievePbaNumbers() {
        String userAuthToken = httpServletRequest.getHeader(AUTHORIZATION);
        String userEmail = userService.getUserEmail();

        try {
            PbaOrganisationResponse pbaOrganisationResponse =
                pbaRefDataClient.retrievePbaNumbers(userAuthToken, authTokenGenerator.generate(), userEmail);

            return Optional.of(pbaOrganisationResponse.getOrganisationEntityResponse().getPaymentAccount());
        } catch (FeignException.NotFound | FeignException.Forbidden ex) {
            log.error("Error retrieving PBA numbers from PBA Ref Data for user: {}",
                userService.getUserInfo().getUid());
            return Optional.empty();
        } catch (NullPointerException ex) {
            log.error("No PBA number found for user: {}, org may not have PBA number assigned",
                userService.getUserInfo().getUid());
            return Optional.empty();
        }
    }
}
