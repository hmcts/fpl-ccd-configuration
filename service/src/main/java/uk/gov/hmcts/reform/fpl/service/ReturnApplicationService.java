package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.ReturnApplication;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.time.LocalDate;

import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ReturnApplicationService {
    private final Time time;

    public ReturnApplication updateReturnApplication(ReturnApplication returnApplication,
                                                     DocumentReference documentReference,
                                                     LocalDate dateSubmitted) {
        return ReturnApplication.builder()
            .note(returnApplication.getNote())
            .reason(returnApplication.getReason())
            .document(documentReference)
            .returnedDate(formatLocalDateToString(time.now().toLocalDate(), DATE))
            .submittedDate(formatLocalDateToString(dateSubmitted, DATE))
            .build();
    }

    public void appendReturnedToFileName(DocumentReference file) {
        String fileName = file.getFilename();
        file.setFilename(new StringBuilder(fileName).insert(fileName.lastIndexOf('.'), "_returned").toString());
    }
}
