package uk.gov.hmcts.reform.fpl.controllers.cafcass;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper;

import java.time.DateTimeException;
import java.time.LocalDate;

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
            if (isEmpty(startDate)) {
                throw new IllegalArgumentException("startDate empty");
            }
            if (isEmpty(endDate)) {
                throw new IllegalArgumentException("endDate empty");
            }

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
}
