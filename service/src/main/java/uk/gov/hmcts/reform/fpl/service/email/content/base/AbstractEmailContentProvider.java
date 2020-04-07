package uk.gov.hmcts.reform.fpl.service.email.content.base;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractEmailContentProvider {
    protected final String uiBaseUrl;
    protected final ObjectMapper mapper;
}
