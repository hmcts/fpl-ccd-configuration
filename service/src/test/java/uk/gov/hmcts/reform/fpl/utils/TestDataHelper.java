package uk.gov.hmcts.reform.fpl.utils;

import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.Placement;
import uk.gov.hmcts.reform.fpl.model.PlacementOrderAndNotices;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

public class TestDataHelper {

    private TestDataHelper() {
    }

    public static DocumentReference testDocument() {
        return DocumentReference.builder()
            .filename(randomAlphanumeric(10))
            .url(randomAlphanumeric(10))
            .binaryUrl(randomAlphanumeric(10))
            .build();
    }

    public static Element<Child> testChild() {
        return element(Child.builder()
            .party(ChildParty.builder()
                .firstName(randomAlphanumeric(10))
                .lastName(randomAlphanumeric(10))
                .build())
            .build());
    }

    public static Placement testPlacement(Element<Child> child, DocumentReference application) {
        return Placement.builder()
            .childId(child.getId())
            .childName(child.getValue().getParty().getFullName())
            .application(application)
            .build();
    }

    public static Placement testPlacement(Element<Child> child) {
        return Placement.builder()
            .childId(child.getId())
            .childName(child.getValue().getParty().getFullName())
            .application(testDocument())
            .build();
    }

    public static Placement testPlacement(Element<Child> child,
                                          List<PlacementOrderAndNotices> placementOrderAndNotices) {
        return Placement.builder()
            .childId(child.getId())
            .childName(child.getValue().getParty().getFullName())
            .application(testDocument())
            .orderAndNotices(placementOrderAndNotices
                .stream()
                .map(ElementUtils::element)
                .collect(toList()))
            .build();
    }

    public static PlacementOrderAndNotices testPlacementOrderAndNotices(
        PlacementOrderAndNotices.PlacementOrderAndNoticesType type, String documentBinaryUrl) {
        return PlacementOrderAndNotices.builder()
            .type(type)
            .document(DocumentReference.builder().binaryUrl(documentBinaryUrl).build())
            .build();
    }

    public static PlacementOrderAndNotices testPlacementOrderAndNotices(
        PlacementOrderAndNotices.PlacementOrderAndNoticesType type, DocumentReference documentReference) {
        return PlacementOrderAndNotices.builder()
            .type(type)
            .document(documentReference)
            .build();
    }
}
