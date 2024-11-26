package uk.gov.hmcts.reform.fpl.testingsupport.controllers;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseAccessDataStoreApi;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApiV2;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.ccd.document.am.model.Document;
import uk.gov.hmcts.reform.ccd.model.AddCaseAssignedUserRolesRequest;
import uk.gov.hmcts.reform.ccd.model.AuditEvent;
import uk.gov.hmcts.reform.ccd.model.CaseAssignedUserRoleWithOrganisation;
import uk.gov.hmcts.reform.fnp.client.PaymentApi;
import uk.gov.hmcts.reform.fnp.model.payment.Payments;
import uk.gov.hmcts.reform.fpl.config.SystemUpdateUserConfiguration;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.enums.docmosis.RenderFormat;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.fpl.service.DocumentDownloadService;
import uk.gov.hmcts.reform.fpl.service.RoleAssignmentService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.fpl.utils.ResourceReader;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;
import uk.gov.service.notify.NotificationList;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.resolve;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;

@Slf4j
@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@ConditionalOnExpression("${testing.support.enabled:false}")
@SuppressWarnings("unchecked")
public class TestingSupportController {
    private static final String POPULATE_EVENT_ID_TEMPLATE = "populateCase-%s";
    private static final String EMAIL = "email";

    private final IdamClient idamClient;
    private final RoleAssignmentService roleAssignmentService;
    private final RequestData requestData;
    private final AuthTokenGenerator authToken;

    private final CaseAccessDataStoreApi caseAccess;
    private final CoreCaseDataApi coreCaseDataApi;
    private final CoreCaseDataApiV2 coreCaseDataApiV2;
    private final CoreCaseDataService coreCaseDataService;

    private final PaymentApi paymentApi;
    private final NotificationClient notifications;
    private final SystemUpdateUserConfiguration userConfig;
    private final DocumentDownloadService documentDownloadService;
    private final UploadDocumentService documentUploadService;

    @Value("${fpl.env}")
    private String environment;

    @PostMapping(value = "/testing-support/case/create", produces = APPLICATION_JSON_VALUE)
    public Map createCase(@RequestBody Map<String, Object> requestBody) {

        StartEventResponse startEventResponse = coreCaseDataApi.startCase(
            requestData.authorisation(),
            authToken.generate(),
            CASE_TYPE,
            "openCase");

        CaseDataContent caseDataContent = CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(Event.builder()
                .id(startEventResponse.getEventId())
                .build())
            .data(requestBody)
            .build();

        return coreCaseDataApiV2.saveCase(
            requestData.authorisation(),
            authToken.generate(),
            CASE_TYPE,
            caseDataContent);
    }

    @PostMapping("/testing-support/case/populate/{caseId}")
    public void populateCase(@PathVariable("caseId") Long caseId, @RequestBody Map<String, Object> requestBody) {
        State state = State.fromValue(requestBody.get("state").toString());
        Map<String, Object> caseData = (Map<String, Object>) requestBody.get("caseData");

        try {
            coreCaseDataService.performPostSubmitCallback(caseId,
                String.format(POPULATE_EVENT_ID_TEMPLATE, state.getValue()),
                (caseDetails -> caseData));
        } catch (FeignException e) {
            log.error(String.format("Populate case event failed: %s", e.contentUTF8()));
            throw e;
        }
    }

    @GetMapping("/testing-support/case/{caseId}")
    public CaseDetails getCase(@PathVariable("caseId") String caseId) {
        try {
            return coreCaseDataApi.getCase(requestData.authorisation(), authToken.generate(), caseId);
        } catch (FeignException e) {
            throw new ResponseStatusException(ofNullable(resolve(e.status())).orElse(INTERNAL_SERVER_ERROR));
        }
    }

    @GetMapping("/testing-support/case/{caseId}/lastEvent")
    public AuditEvent getLastEvent(@PathVariable("caseId") String caseId) {
        return coreCaseDataApiV2.getAuditEvents(requestData.authorisation(), authToken.generate(), false, caseId)
            .getAuditEvents().stream()
            .max(Comparator.comparing(AuditEvent::getCreatedDate))
            .orElse(null);
    }

    @PostMapping("/testing-support/user")
    public UserDetails getUser(@RequestBody Map<String, String> requestBody) {
        final String token = idamClient.getAccessToken(requestBody.get(EMAIL), requestBody.get("password"));
        return idamClient.getUserDetails(token);
    }

    @GetMapping("/testing-support/document")
    public byte[] getDocumentContent(@RequestBody String url) {
        return documentDownloadService.downloadDocument(url);
    }

    @GetMapping("/testing-support/case/{caseId}/payments")
    public Payments getPayments(@PathVariable("caseId") String caseId) {
        try {
            return paymentApi.getCasePayments(requestData.authorisation(), authToken.generate(), caseId);
        } catch (FeignException e) {
            return Payments.builder().payments(emptyList()).build();
        }
    }

    @GetMapping("/testing-support/case/{caseId}/emails")
    public NotificationList getEmails(@PathVariable("caseId") String caseId) {
        try {
            return notifications.getNotifications(null, EMAIL, environment + "/" + caseId, null);
        } catch (NotificationClientException e) {
            return null;
        }
    }

    @PostMapping("/testing-support/case/{caseId}/access")
    public void grantCaseAccess(@PathVariable("caseId") Long caseId, @RequestBody Map<String, String> requestBody) {
        String userToken = idamClient.getAccessToken(userConfig.getUserName(), userConfig.getPassword());

        final String email = requestBody.get(EMAIL);
        final String password = requestBody.get("password");
        final String role = requestBody.get("role");

        log.info("About to grant {} to user {} to case {}", role, email, caseId);

        final String token = idamClient.getAccessToken(email, password);
        final String userId = idamClient.getUserDetails(token).getId();

        final AddCaseAssignedUserRolesRequest accessRequest = AddCaseAssignedUserRolesRequest.builder()
            .caseAssignedUserRoles(List.of(CaseAssignedUserRoleWithOrganisation.builder()
                .caseDataId(caseId.toString())
                .userId(userId)
                .caseRole(role)
                .build()))
            .build();

        caseAccess.addCaseUserRoles(userToken, authToken.generate(), accessRequest);

        log.info("Role {} granted to user {} to case {}", role, email, caseId);
    }

    @GetMapping(value = "/testing-support/test-document", produces = APPLICATION_JSON_VALUE)
    public DocumentReference getTestDoc() {
        byte[] pdf = ResourceReader.readBytes("test_support/document.pdf");

        Document document = documentUploadService.uploadDocument(pdf, "mockFile.pdf", RenderFormat.PDF.getMediaType());
        DocumentReference reference = DocumentReference.buildFromDocument(document);

        log.info("Generated test document {}", reference);

        return reference;
    }

    @GetMapping("/testing-support/assign-system-role")
    public void assignSystemUserRole() {
        roleAssignmentService.assignSystemUserRole();
    }
}
