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
import uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType;
import uk.gov.hmcts.reform.fpl.events.UpdateGuardianEvent;
import uk.gov.hmcts.reform.fpl.exceptions.EmptyFileException;
import uk.gov.hmcts.reform.fpl.exceptions.api.BadInputException;
import uk.gov.hmcts.reform.fpl.exceptions.api.NotFoundException;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Guardian;
import uk.gov.hmcts.reform.fpl.model.cafcass.api.CafcassApiCase;
import uk.gov.hmcts.reform.fpl.model.cafcass.api.CafcassApiSearchCasesResponse;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.service.CaseConverter;
import uk.gov.hmcts.reform.fpl.service.EventService;
import uk.gov.hmcts.reform.fpl.service.cafcass.api.CafcassApiDocumentService;
import uk.gov.hmcts.reform.fpl.service.cafcass.api.CafcassApiGuardianService;
import uk.gov.hmcts.reform.fpl.service.cafcass.api.CafcassApiSearchCaseService;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static java.lang.String.format;

@Slf4j
@RestController
@RequestMapping("/cases")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CafcassCasesController {
    private final EventService eventPublisher;
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
                                                 @RequestParam(value = "typeOfDocument") String typeOfDocument)
            throws Exception {
        log.info("uploadDocument request received");

        try {
            if (!cafcassApiDocumentService.isValidFile(file)) {
                throw new BadInputException("invalid file provided, is empty or not in pdf format");
            }

            CaseData caseDataBefore = getCaseData(caseId);

            DocumentReference documentReference =
                    cafcassApiDocumentService.uploadDocumentToDocStore(file, typeOfDocument);

            switch (typeOfDocument) {
                case "GUARDIAN_REPORT":
                    CaseData updatedCaseDataGuardianReport =
                        getCaseData(cafcassApiDocumentService.uploadDocument(
                            documentReference, caseDataBefore.getId(), DocumentType.GUARDIAN_REPORT));
                    eventPublisher.publishEvent(
                        cafcassApiDocumentService.generateDocumentUploadedEvent(documentReference,
                            DocumentType.GUARDIAN_REPORT, updatedCaseDataGuardianReport));
                    break;
                case "POSITION_STATEMENT":
                    CaseData updatedCaseDataPosStmt =
                        getCaseData(cafcassApiDocumentService.uploadDocument(
                            documentReference, caseDataBefore.getId(), DocumentType.POSITION_STATEMENTS));
                    eventPublisher.publishEvent(
                        cafcassApiDocumentService.generateDocumentUploadedEvent(documentReference,
                            DocumentType.POSITION_STATEMENTS, updatedCaseDataPosStmt));
                    break;
                default:
                    log.error("bad input parameter " + typeOfDocument + " is not a valid type");
                    return ResponseEntity.status(400).build();
            }

            return ResponseEntity
                .status(200)
                .body(format("%s uploaded successfully to case with Id: %s", typeOfDocument, caseId));
        } catch (IOException e) {
            log.error("Bad document input", e);
            throw new BadInputException(e.getMessage());
        }
    }

    @PostMapping("{caseId}/guardians")
    public ResponseEntity<Object> uploadGuardians(@PathVariable String caseId,
                                                  @RequestBody List<Guardian> guardians) {
        log.info("uploadGuardians request received - caseId: [{}]", caseId);
        if (!cafcassApiGuardianService.validateGuardians(guardians)) {
            throw new BadInputException();
        }

        CaseData caseData = getCaseData(caseId);

        if (cafcassApiGuardianService.checkIfAnyGuardianUpdated(caseData, guardians)) {
            CaseData updatedCaseData = getCaseData(cafcassApiGuardianService.updateGuardians(caseData, guardians));
            eventPublisher.publishEvent(UpdateGuardianEvent.builder().caseData(updatedCaseData).build());
        } else {
            log.info("uploadGuardians - no changes");
        }
        return ResponseEntity.ok().build();
    }

    private CaseData getCaseData(String caseId) {
        try {
            return getCaseData(coreCaseDataService.findCaseDetailsById(caseId));
        } catch (Exception e) {
            throw new NotFoundException("Case reference not found");
        }
    }

    private CaseData getCaseData(CaseDetails caseDetails) {
        return caseConverter.convert(caseDetails);
    }
}
