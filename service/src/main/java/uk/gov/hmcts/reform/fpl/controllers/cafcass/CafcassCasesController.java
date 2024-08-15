package uk.gov.hmcts.reform.fpl.controllers.cafcass;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.exceptions.EmptyFileException;
import uk.gov.hmcts.reform.fpl.exceptions.api.BadInputException;
import uk.gov.hmcts.reform.fpl.exceptions.api.NotFoundException;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Guardian;
import uk.gov.hmcts.reform.fpl.model.cafcass.api.CafcassApiCase;
import uk.gov.hmcts.reform.fpl.model.cafcass.api.CafcassApiSearchCasesResponse;
import uk.gov.hmcts.reform.fpl.service.CaseConverter;
import uk.gov.hmcts.reform.fpl.service.cafcass.api.CafcassApiDocumentService;
import uk.gov.hmcts.reform.fpl.service.cafcass.api.CafcassApiGuardianService;
import uk.gov.hmcts.reform.fpl.service.cafcass.api.CafcassApiSearchCaseService;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Slf4j
@RestController
@RequestMapping("/cases")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CafcassCasesController {
    private final CaseConverter caseConverter;
    private final CoreCaseDataService coreCaseDataService;
    private final CafcassApiSearchCaseService cafcassApiSearchCaseService;
    private final CafcassApiDocumentService cafcassApiDocumentService;
    private final CafcassApiGuardianService cafcassApiGuardianService;

    @GetMapping("")
    public CafcassApiSearchCasesResponse searchCases(
        @RequestParam(name = "startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
        @RequestParam(name = "endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        log.info("searchCases, " + startDate + ", " + endDate);

        if (startDate.isAfter(endDate) || startDate.plusMinutes(15).isBefore(endDate)) {
            throw new BadInputException();
        }

        List<CafcassApiCase> caseDetails = cafcassApiSearchCaseService.searchCaseByDateRange(startDate, endDate);

        return CafcassApiSearchCasesResponse.builder()
            .total(caseDetails.size())
            .cases(caseDetails)
            .build();
    }

    @GetMapping("documents/{documentId}/binary")
    public ResponseEntity<Object> getDocumentBinary(@PathVariable String documentId) {
        log.info("getDocumentBinary request received");

        try {
            UUID validatedUid = UUID.fromString(documentId);
        } catch (Exception e) {
            throw new BadInputException("Case document Id is not valid");
        }

        try {
            return ResponseEntity.ok(cafcassApiDocumentService.downloadDocumentByDocumentId(documentId));
        } catch (EmptyFileException e) {
            throw new NotFoundException("Case document not found");
        }
    }

    @PostMapping("{caseId}/document")
    public ResponseEntity<Object> uploadDocument(@PathVariable String caseId,
                                                 @RequestParam(value = "file") MultipartFile file,
                                                 @RequestParam(value = "typeOfDocument") String typeOfDocument) {
        log.info("uploadDocument request received");

        try {
            return ResponseEntity.ok(String.format(
                "uploadDocument - caseId: [%s], file length: [%s], typeOfDocument: [%s]",
                UUID.fromString(caseId), file.getSize(), typeOfDocument));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body("bad input parameter - " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal server error");
        }
    }

    @PostMapping("{caseId}/guardians")
    public ResponseEntity<Object> uploadGuardians(@PathVariable String caseId,
                                                  @RequestBody @Valid @NotNull List<@NotNull Guardian> guardians) {
        log.info("uploadGuardians request received - caseId: [{}]", caseId);
        CaseData caseData = getCaseData(caseId);

        if (cafcassApiGuardianService.checkIfAnyGuardianUpdated(caseData, guardians)) {
            cafcassApiGuardianService.updateGuardians(caseData, guardians);
        }
        return ResponseEntity.ok().build();
    }

    private CaseData getCaseData(String caseId) {
        CaseDetails caseDetails;
        try {
            caseDetails = coreCaseDataService.findCaseDetailsById(caseId);
        } catch (Exception e) {
            throw new NotFoundException("Case reference not found");
        }
        return caseConverter.convert(caseDetails);
    }
}
