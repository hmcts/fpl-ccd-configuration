package uk.gov.hmcts.reform.fpl.controllers.cafcass;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;

@RestController
@RequestMapping("/cases")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CafcassCasesController {
    private static final String DATE_TIME_FORMAT_IN = "yyyy-MM-ddThh:mm:ss.SSS";
    private static final String DATE_TIME_FORMAT_OUT = "yyyy-MM-dd";

    @GetMapping
    public ResponseEntity<Object> searchCases(@RequestParam(name = "startDate") String startDate,
                                              @RequestParam(name = "endDate") String endDate) {
        try {
            if (!isEmpty(startDate) && !isEmpty(endDate)) {
                LocalDateTime startLocalDateTime =
                    DateFormatterHelper.parseLocalDateTimeFromStringUsingFormat(startDate, DATE_TIME_FORMAT_IN);
                LocalDateTime endLocalDateTime =
                    DateFormatterHelper.parseLocalDateTimeFromStringUsingFormat(endDate, DATE_TIME_FORMAT_IN);

                return ResponseEntity.ok(String.format("searchCases - Start date: [%s], End date: [%s]",
                    DateFormatterHelper.formatLocalDateTimeBaseUsingFormat(startLocalDateTime, DATE_TIME_FORMAT_OUT),
                    DateFormatterHelper.formatLocalDateTimeBaseUsingFormat(endLocalDateTime, DATE_TIME_FORMAT_OUT)
                    ));
            } else {
                throw new DateTimeException("empty");
            }
        } catch (DateTimeException e) {
            return ResponseEntity.status(400).body("bad input parameter");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal server error");
        }
    }

    @GetMapping("documents/{documentId}/binary")
    public ResponseEntity<Object> getDocumentBinary(@PathVariable String documentId) {
        try {
            if (!isEmpty(documentId)) {
                UUID docId = UUID.fromString(documentId);
                return ResponseEntity.ok(String.format("getDocumentBinary - document id: [%s]", docId));
            } else {
                throw new IllegalArgumentException("empty");
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body("bad input parameter");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal server error");
        }
    }
}
