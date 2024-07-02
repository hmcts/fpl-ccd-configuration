package uk.gov.hmcts.reform.fpl.controllers.cafcass;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;

@RestController
@RequestMapping("/cases")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CafcassCasesController {
    private static final String DATE_TIME_FORMAT_IN = "yyyy-MM-ddThh:mm:ss.SSS";
    private static final String DATE_TIME_FORMAT_OUT = "yyyy-MM-dd";

    @GetMapping("")
    public ResponseEntity<Object> searchCases(@RequestParam(name = "startDate") String startDate,
                                              @RequestParam(name = "endDate") String endDate) {
        try {
            if (isEmpty(startDate)) {
                throw new IllegalArgumentException("startDate empty");
            }
            if (isEmpty(endDate)) {
                throw new IllegalArgumentException("endDate empty");
            }

            LocalDateTime startLocalDateTime =
                DateFormatterHelper.parseLocalDateTimeFromStringUsingFormat(startDate, DATE_TIME_FORMAT_IN);
            LocalDateTime endLocalDateTime =
                DateFormatterHelper.parseLocalDateTimeFromStringUsingFormat(endDate, DATE_TIME_FORMAT_IN);

            return ResponseEntity.ok(String.format("searchCases - Start date: [%s], End date: [%s]",
                DateFormatterHelper.formatLocalDateTimeBaseUsingFormat(startLocalDateTime, DATE_TIME_FORMAT_OUT),
                DateFormatterHelper.formatLocalDateTimeBaseUsingFormat(endLocalDateTime, DATE_TIME_FORMAT_OUT)
            ));
        } catch (DateTimeException e) {
            return ResponseEntity.status(400).body("bad input parameter - " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal server error");
        }
    }

    @GetMapping("documents/{documentId}/binary")
    public ResponseEntity<Object> getDocumentBinary(@PathVariable String documentId) {
        try {
            if (isEmpty(documentId)) {
                throw new IllegalArgumentException("documentId empty");
            }

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
                                                 @RequestPart(value = "file") MultipartFile file,
                                                 @RequestPart(value = "typeOfDocument") String typeOfDocument) {
        try {
            if (isEmpty(caseId)) {
                throw new IllegalArgumentException("caseId empty");
            }
            if (isEmpty(file)) {
                throw new IllegalArgumentException("file empty");
            }
            if (isEmpty(typeOfDocument)) {
                throw new IllegalArgumentException("typeOfDocument empty");
            }

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
                                                  @RequestBody Map<String, Object> requestBody) {
        try {
            if (isEmpty(caseId)) {
                throw new IllegalArgumentException("caseId empty");
            }
            if (isEmpty(requestBody)) {
                throw new IllegalArgumentException("requestBody empty");
            }

            return ResponseEntity.ok(String.format(
                "uploadGuardians - caseId: [%s], guardianName: [%s], children: [%s]",
                UUID.fromString(caseId), requestBody.get("guardianName"), requestBody.get("children")));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body("bad input parameter - " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal server error");
        }
    }
}
