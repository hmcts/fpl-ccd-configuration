package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseNote;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseNoteService {
    private final IdamClient idamClient;
    private final Time time;

    public CaseNote buildCaseNote(String authorisation, String note) {
        UserInfo userDetails = idamClient.getUserInfo(authorisation);

        return CaseNote.builder()
            .createdBy(userDetails.getName())
            .date(time.now().toLocalDate())
            .note(note)
            .build();
    }

    public List<Element<CaseNote>> addNoteToList(CaseNote caseNote, List<Element<CaseNote>> caseNotes) {
        List<Element<CaseNote>> updatedCaseNotes = ofNullable(caseNotes).orElse(newArrayList());
        updatedCaseNotes.add(element(caseNote));

        return updatedCaseNotes;
    }
}
