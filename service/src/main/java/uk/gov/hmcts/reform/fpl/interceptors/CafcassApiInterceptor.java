package uk.gov.hmcts.reform.fpl.interceptors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.HandlerInterceptor;
import uk.gov.hmcts.reform.fpl.config.CafcassSystemUpdateUserConfiguration;
import uk.gov.hmcts.reform.fpl.exceptions.api.AuthorizationException;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.Objects;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CafcassApiInterceptor implements HandlerInterceptor {
    private final ObjectProvider<IdamClient> idamClient;
    private final CafcassSystemUpdateUserConfiguration userConfig;

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        String authToken = request.getHeader("Authorization");
        if (isNotEmpty(authToken)) {
            UserInfo userInfo = Objects.requireNonNull(idamClient.getIfAvailable()).getUserInfo(authToken);
            if (userInfo != null && userInfo.getSub().equalsIgnoreCase(userConfig.getUserName())) {
                return true;
            }
        }
        throw new AuthorizationException();
    }
}
