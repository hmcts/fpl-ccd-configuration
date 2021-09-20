package uk.gov.hmcts.reform.fpl.service.children;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.UnregisteredOrganisation;
import uk.gov.hmcts.reform.fpl.model.children.ChildRepresentationDetails;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.representative.RepresentativeSolicitorSanitizer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.lang.String.format;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ChildRepresentationDetailsFlattener {

    private static final int MAX_CHILDREN = 15;
    private static final RespondentSolicitor BLANK_REPRESENTATIVE = RespondentSolicitor.builder()
        .organisation(Organisation.builder().build())
        .unregisteredOrganisation(UnregisteredOrganisation.builder().address(Address.builder().build()).build())
        .regionalOfficeAddress(Address.builder().build())
        .build();

    private final RepresentativeSolicitorSanitizer sanitizer;

    public Map<String, Object> serialise(List<Element<Child>> children, RespondentSolicitor mainRepresentative) {
        List<Element<Child>> safeCollection = defaultIfNull(children, new ArrayList<>());
        RespondentSolicitor representative = sanitizeSolicitor(mainRepresentative);

        Map<String, Object> data = new HashMap<>();
        for (int i = 0; i < MAX_CHILDREN; i++) {
            data.put(format("childRepresentationDetails%d", i), transformOrNull(safeCollection, i, representative));
        }

        return data;
    }

    private ChildRepresentationDetails transformOrNull(List<Element<Child>> safeCollection, int idx,
                                                       RespondentSolicitor mainRepresentative) {
        return idx < safeCollection.size() ? transform(safeCollection.get(idx).getValue(), idx, mainRepresentative)
                                           : null;
    }

    private ChildRepresentationDetails transform(Child child, int idx, RespondentSolicitor mainRepresentative) {
        RespondentSolicitor childSolicitor = sanitizeSolicitor(child.getSolicitor());
        boolean useMainSolicitor = Objects.equals(childSolicitor, mainRepresentative);

        // ccd can send a blank representative instead of null even though it was set to null previously
        // probably because the complex type is present but hidden in the UI on the previous page
        boolean isCurrentSolicitorNull = null == childSolicitor || BLANK_REPRESENTATIVE.equals(childSolicitor);

        return ChildRepresentationDetails.builder()
            .childDescription(format("Child %d - %s", idx + 1, child.getParty().getFullName()))
            .useMainSolicitor(isCurrentSolicitorNull ? null : YesNo.from(useMainSolicitor).getValue())
            .solicitor(useMainSolicitor || isCurrentSolicitorNull ? null : childSolicitor)
            .build();
    }

    private RespondentSolicitor sanitizeSolicitor(RespondentSolicitor solicitor) {
        return null == solicitor ? null : sanitizer.sanitize(solicitor);
    }
}
