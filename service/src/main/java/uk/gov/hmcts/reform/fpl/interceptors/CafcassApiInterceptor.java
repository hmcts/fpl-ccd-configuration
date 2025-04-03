package uk.gov.hmcts.reform.fpl.interceptors;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.HandlerInterceptor;
import uk.gov.hmcts.reform.fpl.exceptions.api.AuthorizationException;
import uk.gov.hmcts.reform.fpl.exceptions.api.ServiceUnavailableException;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.Objects;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.enums.UserRole.CAFCASS_SYSTEM_UPDATE;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CafcassApiInterceptor implements HandlerInterceptor {
    private final FeatureToggleService featureToggleService;
    private final ObjectProvider<IdamClient> idamClient;

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        if (!featureToggleService.isCafcassApiToggledOn()) {
            log.info("Cafcass API is disabled.");
            throw new ServiceUnavailableException();
        }

        String authToken = request.getHeader("Authorization");
        if (isNotEmpty(authToken)) {
            UserInfo userInfo = Objects.requireNonNull(idamClient.getIfAvailable()).getUserInfo(authToken);
            if (userInfo != null && userInfo.getRoles().contains(CAFCASS_SYSTEM_UPDATE.getRoleName())) {
                return true;
            }
        }
        throw new AuthorizationException();
    }
}
