package uk.gov.hmcts.reform.fpl.service.representative;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.noc.ChangeOfRepresentation;
import uk.gov.hmcts.reform.fpl.model.noc.ChangedRepresentative;
import uk.gov.hmcts.reform.fpl.model.representative.ChangeOfRepresentationRequest;
import uk.gov.hmcts.reform.fpl.service.IdentityService;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.nullSafeList;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ChangeOfRepresentationService {

    private final IdentityService identityService;
    private final Time time;

    public List<Element<ChangeOfRepresentation>> changeRepresentative(
        ChangeOfRepresentationRequest changeOfRepresentationRequest) {

        List<Element<ChangeOfRepresentation>> changeOfRepresentatives = Lists.newArrayList(nullSafeList(
            changeOfRepresentationRequest.getCurrent()));

        changeOfRepresentatives.add(element(identityService.generateId(),
            ChangeOfRepresentation.builder()
                .respondent(changeOfRepresentationRequest.getRespondent().getParty().getFullName())
                .via(changeOfRepresentationRequest.getMethod().getLabel())
                .by(changeOfRepresentationRequest.getBy())
                .date(time.now().toLocalDate())
                .removed(Optional.ofNullable(changeOfRepresentationRequest.getRemovedRepresentative())
                    .map(this::from)
                    .orElse(null))
                .added(Optional.ofNullable(changeOfRepresentationRequest.getAddedRepresentative())
                    .map(this::from)
                    .orElse(null))
                .build()));

        changeOfRepresentatives.sort(Comparator.comparing(e -> e.getValue().getDate()));

        return changeOfRepresentatives;
    }

    private ChangedRepresentative from(RespondentSolicitor solicitor) {
        return ChangedRepresentative.builder()
            .firstName(solicitor.getFirstName())
            .lastName(solicitor.getLastName())
            .email(solicitor.getEmail())
            .organisation(solicitor.getOrganisation())
            .build();
    }
}
