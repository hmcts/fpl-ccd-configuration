package uk.gov.hmcts.reform.fpl.interceptors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.HandlerInterceptor;
import uk.gov.hmcts.reform.fpl.exceptions.api.AuthorizationException;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static uk.gov.hmcts.reform.fpl.enums.UserRole.CAFCASS_SYSTEM_UPDATE;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CafcassApiInterceptor implements HandlerInterceptor {
    private final IdamClient idamClient;

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        UserInfo userInfo = idamClient.getUserInfo(request.getHeader("Authorization"));
        log.info(String.join(", ", userInfo.getRoles()));
        if (userInfo == null || !userInfo.getRoles().contains(CAFCASS_SYSTEM_UPDATE)) {
            throw new AuthorizationException();
        }
        return true;
    }
}
