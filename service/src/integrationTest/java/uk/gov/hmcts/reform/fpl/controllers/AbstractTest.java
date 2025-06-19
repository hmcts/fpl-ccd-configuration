package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseAssignmentApi;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApiV2;
import uk.gov.hmcts.reform.ccd.client.model.CaseAssignmentUserRole;
import uk.gov.hmcts.reform.ccd.client.model.CaseAssignmentUserRolesResource;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClientApi;
import uk.gov.hmcts.reform.document.DocumentDownloadClientApi;
import uk.gov.hmcts.reform.document.DocumentMetadataDownloadClientApi;
import uk.gov.hmcts.reform.document.DocumentUploadClientApi;
import uk.gov.hmcts.reform.fpl.config.SystemUpdateUserConfiguration;
import uk.gov.hmcts.reform.fpl.enums.CaseRole;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.CaseConverter;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.testingsupport.DynamicListHelper;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.net.URLEncoder.encode;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.List.of;
import static java.util.stream.Collectors.toList;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.service.CaseConverter.MAP_TYPE;

@ActiveProfiles("integration-test")
@ComponentScan(basePackages = "uk.gov.hmcts.reform.fpl", lazyInit = true)
public abstract class AbstractTest {

    protected static final String USER_AUTH_TOKEN = "Bearer token";
    protected static final String SERVICE_AUTH_TOKEN = "Bearer service token";
    protected static final String SYS_USER_ID = "10";
    protected static final String USER_ID = "1";

    @Autowired
    private Time time;

    @Autowired
    protected ObjectMapper mapper;

    @Autowired
    protected CaseConverter caseConverter;

    @Autowired
    private SystemUpdateUserConfiguration userConfig;

    @MockBean
    private AuthTokenGenerator authTokenGenerator;

    @MockBean
    protected IdamClient idamClient;

    @MockBean
    protected CaseAssignmentApi caseAssignmentApi;

    @MockBean
    protected CoreCaseDataApiV2 coreCaseDataApi;

    @MockBean
    protected DocumentUploadClientApi documentUploadClientApi;

    @MockBean
    protected DocumentDownloadClientApi documentDownloadClientApi;

    @MockBean
    protected DocumentMetadataDownloadClientApi documentMetadataDownloadClientApi;

    @MockBean
    protected CaseDocumentClientApi caseDocumentClientApi;

    @Autowired
    protected DynamicListHelper dynamicLists;

    protected LocalDateTime now() {
        return time.now();
    }

    protected LocalDate dateNow() {
        return time.now().toLocalDate();
    }

    protected void givenCurrentUserWithName(String name) {
        givenCurrentUser(UserInfo.builder().name(name).build());
    }

    protected void givenCurrentUserWithEmail(String email) {
        givenCurrentUser(UserInfo.builder().sub(email).build());
    }

    protected void givenCurrentUser(UserInfo userInfo) {
        given(idamClient.getUserInfo(USER_AUTH_TOKEN)).willReturn(userInfo);
    }

    protected void givenCurrentUser(UserDetails userDetails) {
        given(idamClient.getUserDetails(USER_AUTH_TOKEN)).willReturn(userDetails);
    }

    protected void givenSystemUser() {
        given(idamClient.getAccessToken(userConfig.getUserName(), userConfig.getPassword()))
            .willReturn(USER_AUTH_TOKEN);
        given(idamClient.getUserInfo(USER_AUTH_TOKEN)).willReturn(UserInfo.builder()
            .uid(SYS_USER_ID)
            .build());
    }

    protected void givenCaseRoles(Long caseId, String userId, CaseRole... caseRoles) {
        final List<CaseAssignmentUserRole> assignedRoles = Stream.of(caseRoles)
            .map(CaseRole::formattedName)
            .map(caseRoleName -> CaseAssignmentUserRole.builder()
                .caseRole(caseRoleName)
                .caseDataId(caseId.toString())
                .build())
            .collect(toList());

        given(caseAssignmentApi.getUserRoles(USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN, of(caseId.toString()), of(userId)))
            .willReturn(CaseAssignmentUserRolesResource.builder().caseAssignmentUserRoles(assignedRoles).build());
    }

    protected void givenFplService() {
        given(authTokenGenerator.generate()).willReturn(SERVICE_AUTH_TOKEN);
    }

    protected <T> CaseData getCase(ArgumentCaptor<T> captor) {
        return mapper.convertValue(captor.getValue(), CaseData.class);
    }

    protected Map<String, Object> toMap(Object o) {
        return caseConverter.toMap(o);
    }

    protected String caseUrl(Long caseId) {
        return "http://fake-url/cases/case-details/" + caseId;
    }

    protected String caseUrl(Long caseId, String tab) {
        return format("http://fake-url/cases/case-details/%d#%s", caseId, encode(tab, UTF_8).replaceAll("\\+", "%20"));
    }

    protected String notificationReference(Long caseId) {
        return "localhost/" + caseId;
    }

    protected CaseDetails asCaseDetails(CaseData caseData) {
        return CaseDetails.builder()
            .id(caseData.getId())
            .state(Optional.ofNullable(caseData.getState()).map(State::getValue).orElse(null))
            .data(mapper.convertValue(caseData, MAP_TYPE))
            .build();
    }
}
