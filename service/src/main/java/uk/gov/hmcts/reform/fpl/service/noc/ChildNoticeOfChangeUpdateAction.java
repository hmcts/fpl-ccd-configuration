package uk.gov.hmcts.reform.fpl.service.noc;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.SolicitorRole;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.interfaces.WithSolicitor;
import uk.gov.hmcts.reform.fpl.service.children.ChildRepresentationDetailsFlattener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ChildNoticeOfChangeUpdateAction implements NoticeOfChangeUpdateAction {

    private static final SolicitorRole.Representing REPRESENTING = SolicitorRole.Representing.CHILD;

    private final ChildRepresentationDetailsFlattener childRepSerializer;

    @Override
    public boolean accepts(SolicitorRole.Representing representing) {
        return REPRESENTING == representing;
    }

    @Override
    public Map<String, Object> applyUpdates(WithSolicitor child, CaseData caseData, RespondentSolicitor solicitor) {
        child.setSolicitor(solicitor);

        List<Element<Child>> children = caseData.getAllChildren();

        YesNo allSameSolicitor = YesNo.from(children.stream()
            .allMatch(childElement -> Objects.equals(solicitor, childElement.getValue().getSolicitor())));

        Map<String, Object> data = new HashMap<>(Map.of(
            "children1", children,
            "childrenHaveSameRepresentation", allSameSolicitor.getValue()
        ));

        RespondentSolicitor cafcassSolicitor = caseData.getChildrenEventData().getChildrenMainRepresentative();
        if (YesNo.YES == allSameSolicitor && !Objects.equals(cafcassSolicitor, solicitor)) {
            data.put("childrenMainRepresentative", solicitor);
            cafcassSolicitor = solicitor;
        }

        data.putAll(childRepSerializer.serialise(caseData.getAllChildren(), cafcassSolicitor));

        return data;
    }
}
