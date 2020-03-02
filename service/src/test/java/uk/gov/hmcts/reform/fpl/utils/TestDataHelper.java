package uk.gov.hmcts.reform.fpl.utils;

import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeRole;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.Placement;
import uk.gov.hmcts.reform.fpl.model.PlacementOrderAndNotices;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

public class TestDataHelper {

    private TestDataHelper() {
    }

    public static DocumentReference testDocumentReference() {
        return DocumentReference.builder()
            .filename(randomAlphanumeric(10))
            .url(randomAlphanumeric(10))
            .binaryUrl(randomAlphanumeric(10))
            .build();
    }

    public static Document testDocument() {
        final Document.Link binaryLink = new Document.Link();
        binaryLink.href = randomAlphanumeric(10);
        final Document.Link selfLink = new Document.Link();
        selfLink.href = randomAlphanumeric(10);

        final Document.Links links = new Document.Links();
        links.binary = binaryLink;
        links.self = selfLink;

        final Document document = new Document();
        document.links = links;
        document.originalDocumentName = randomAlphanumeric(10);

        return document;
    }

    public static DocmosisDocument testDocmosisDocument(byte[] binaries) {
        return DocmosisDocument.builder()
            .documentTitle(randomAlphanumeric(10))
            .bytes(binaries)
            .build();
    }

    public static byte[] testDocumentBinaries() {
        return randomAlphanumeric(10).getBytes();
    }

    public static Element<Child> testChild() {
        return element(Child.builder()
            .party(ChildParty.builder()
                .firstName(randomAlphanumeric(10))
                .lastName(randomAlphanumeric(10))
                .build())
            .build());
    }

    public static Representative testRepresentative() {
        return testRepresentative(RepresentativeServingPreferences.POST);
    }

    public static Representative testRepresentative(RepresentativeServingPreferences servingPreferences) {
        return Representative.builder()
            .fullName(randomAlphanumeric(10))
            .positionInACase("Position")
            .role(RepresentativeRole.REPRESENTING_PERSON_1)
            .servingPreferences(servingPreferences)
            .address(Address.builder().build())
            .build();
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
            .application(testDocumentReference())
            .build();
    }

    public static Placement testPlacement(Element<Child> child,
                                          List<PlacementOrderAndNotices> placementOrderAndNotices) {
        return Placement.builder()
            .childId(child.getId())
            .childName(child.getValue().getParty().getFullName())
            .application(testDocumentReference())
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
