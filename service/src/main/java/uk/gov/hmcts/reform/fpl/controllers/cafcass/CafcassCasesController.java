package uk.gov.hmcts.reform.fpl.controllers.cafcass;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper;

import java.time.DateTimeException;
import java.time.LocalDate;
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

    @GetMapping("")
    public ResponseEntity<Object> searchCases(@RequestParam(name = "startDate") String startDate,
                                              @RequestParam(name = "endDate") String endDate) {
        log.info("searchCases request received");
        try {
//            if (isEmpty(startDate)) {
//                throw new IllegalArgumentException("startDate empty");
//            }
//            if (isEmpty(endDate)) {
//                throw new IllegalArgumentException("endDate empty");
//            }

            log.info("searchCases, " + startDate + ", " + endDate);
            LocalDate startLocalDateTime =
                DateFormatterHelper.parseLocalDateFromStringUsingFormat(startDate, DATE_TIME_FORMAT_IN);
            LocalDate endLocalDateTime =
                DateFormatterHelper.parseLocalDateFromStringUsingFormat(endDate, DATE_TIME_FORMAT_IN);

            if (startLocalDateTime.isAfter(endLocalDateTime)) {
                throw new Exception("startDate after endDate");
            }

            return ResponseEntity.ok(String.format("searchCases - Start date: [%s], End date: [%s]",
                DateFormatterHelper.formatLocalDateToString(startLocalDateTime, DATE_TIME_FORMAT_OUT),
                DateFormatterHelper.formatLocalDateToString(endLocalDateTime, DATE_TIME_FORMAT_OUT)
            ));
        } catch (DateTimeException e) {
            return ResponseEntity.status(400).body("bad input parameter - " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal server error - " + e.getMessage());
        }
    }

    @GetMapping("documents/{documentId}/binary")
    public ResponseEntity<Object> getDocumentBinary(@PathVariable String documentId) {
        try {
//            if (isEmpty(documentId)) {
//                throw new IllegalArgumentException("documentId empty");
//            }

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
        try {
//            if (isEmpty(caseId)) {
//                throw new IllegalArgumentException("caseId empty");
//            }
//            if (isEmpty(file)) {
//                throw new IllegalArgumentException("file empty");
//            }
//            if (isEmpty(typeOfDocument)) {
//                throw new IllegalArgumentException("typeOfDocument empty");
//            }

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
        try {
//            if (isEmpty(caseId)) {
//                throw new IllegalArgumentException("caseId empty");
//            }
            if (isEmpty(guardians)) {
                throw new IllegalArgumentException("list empty");
            }

            StringBuilder sb = new StringBuilder();
            sb.append("uploadGuardians - caseId: [%s], no of guardians: [%s]\n"
                    .formatted(UUID.fromString(caseId), guardians.size()));

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
