package uk.gov.hmcts.reform.fpl.service.children.validation.collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.UserService;
import uk.gov.hmcts.reform.fpl.service.children.validation.ChildrenEventSection;
import uk.gov.hmcts.reform.fpl.service.children.validation.ChildrenEventSectionValidator;
import uk.gov.hmcts.reform.fpl.service.children.validation.user.AdminUserValidator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.fpl.service.children.validation.ChildrenEventSection.COLLECTION;

@Component
public final class AdminUserChildCollectionValidator extends AdminUserValidator
    implements ChildrenEventSectionValidator {

    @Autowired
    public AdminUserChildCollectionValidator(UserService user) {
        super(user);
    }

    @Override
    public boolean accepts(ChildrenEventSection section) {
        return COLLECTION == section && acceptsUser();
    }

    @Override
    public List<String> validate(CaseData caseData, CaseData caseDataBefore) {
        List<Element<Child>> currentChildren = caseData.getAllChildren();
        List<Element<Child>> oldChildren = caseDataBefore.getAllChildren();

        // no changes, just return
        if (Objects.equals(currentChildren, oldChildren)) {
            return List.of();
        }

        Set<UUID> currentIds = currentChildren.stream().map(Element::getId).collect(Collectors.toSet());
        Set<UUID> oldIds = oldChildren.stream().map(Element::getId).collect(Collectors.toSet());

        return checkForRemoval(currentIds, oldIds, oldChildren);
    }

    private List<String> checkForRemoval(Set<UUID> currentIds, Set<UUID> oldIds, List<Element<Child>> children) {
        List<String> errors = new ArrayList<>();

        Set<UUID> alteredIds = new HashSet<>(oldIds);
        alteredIds.removeAll(currentIds);

        alteredIds.forEach(id -> children.stream()
            .filter(element -> Objects.equals(id, element.getId()))
            .findFirst()
            .ifPresent(child -> errors.add(format(CHILD_REMOVAL_ERROR, child.getValue().getParty().getFullName())))
        );

        return errors;
    }
}
