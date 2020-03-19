package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseNote;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.time.LocalDate;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Comparator.comparing;
import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseNoteService {
    private final IdamClient idamClient;

    public CaseNote buildCaseNote(String authorisation, String note) {
        UserDetails userDetails = idamClient.getUserDetails(authorisation);

        return CaseNote.builder()
            .createdBy(userDetails.getFullName())
            .date(LocalDate.now())
            .caseNote(note)
            .build();
    }

    public List<Element<CaseNote>> addNoteToList(CaseNote caseNote, List<Element<CaseNote>> caseNotes) {
        List<Element<CaseNote>> updatedCaseNotes = ofNullable(caseNotes).orElse(newArrayList());
        updatedCaseNotes.add(element(caseNote));

        updatedCaseNotes.sort(comparing(caseNoteElement -> caseNoteElement.getValue().getDate()));

        return updatedCaseNotes;
    }
}
