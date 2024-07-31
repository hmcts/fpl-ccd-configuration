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
import uk.gov.hmcts.reform.fpl.exceptions.api.BadInputException;
import uk.gov.hmcts.reform.fpl.model.cafcass.api.CafcassApiCase;
import uk.gov.hmcts.reform.fpl.model.cafcass.api.CafcassApiSearchCasesResponse;
import uk.gov.hmcts.reform.fpl.service.cafcass.api.CafcassApiSearchCaseService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;

@Slf4j
@RestController
@RequestMapping("/cases")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CafcassCasesController {
    private static final String DATE_TIME_FORMAT_IN = "yyyy-MM-dd'T'hh:mm:ss.SSS";
    private static final String DATE_TIME_FORMAT_OUT = "yyyy-MM-dd";

    private final CafcassApiSearchCaseService cafcassApiSearchCaseService;

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
            return ResponseEntity.ok(String.format("getDocumentBinary - document id: [%s]",
                UUID.fromString(documentId)));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body("bad input parameter - " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal server error");
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

    @SuppressWarnings("unchecked")
    @PostMapping("{caseId}/guardians")
    public ResponseEntity<Object> uploadGuardians(@PathVariable String caseId,
                                                  @RequestBody List<Map<String, Object>> guardians) {
        log.info("uploadGuardians request received");
        try {
            if (isEmpty(guardians)) {
                throw new IllegalArgumentException("list empty");
            }

            StringBuilder sb = new StringBuilder();
            sb.append("uploadGuardians - caseId: [%s], no of guardians: [%s]\n"
                    .formatted(UUID.fromString(caseId), guardians.size()));
            log.info("uploadGuardians guardians size " + guardians.size());

            guardians.forEach(guardian -> {
                sb.append("guardianName: [%s], ".formatted(guardian.get("guardianName")));
                sb.append("children: [%s]".formatted(String.join(", ",
                    ((List<String>) guardian.get("children")))));
                sb.append("\n");
            });

            return ResponseEntity.ok(sb);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body("bad input parameter - " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal server error");
        }
    }
}
