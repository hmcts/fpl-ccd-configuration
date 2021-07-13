package uk.gov.hmcts.reform.fpl.service.noc;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.SolicitorRole;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.interfaces.WithSolicitor;

import java.util.Map;
import java.util.Objects;

@Service
public class ChildNoticeOfChangeUpdateAction implements NoticeOfChangeUpdateAction {

    private static final SolicitorRole.Representing REPRESENTING = SolicitorRole.Representing.CHILD;

    @Override
    public boolean accepts(SolicitorRole.Representing representing) {
        return REPRESENTING == representing;
    }

    @Override
    public Map<String, Object> applyUpdates(WithSolicitor child, CaseData caseData, RespondentSolicitor solicitor) {
        child.setSolicitor(solicitor);

        RespondentSolicitor cafcassSolicitor = caseData.getChildrenEventData().getChildrenMainRepresentative();
        YesNo allSameSolicitor = YesNo.from(caseData.getAllChildren().stream()
            .allMatch(childElement -> Objects.equals(cafcassSolicitor, childElement.getValue().getSolicitor())));

        return Map.of(
            "children1", caseData.getAllChildren(),
            "childrenHaveSameRepresentation", allSameSolicitor.getValue()
        );
    }
}
