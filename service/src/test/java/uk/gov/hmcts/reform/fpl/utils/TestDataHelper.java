package uk.gov.hmcts.reform.fpl.utils;

import feign.FeignException;
import feign.Request;
import feign.Response;
import org.apache.commons.lang3.RandomUtils;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.enums.ChildGender;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeRole;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.Placement;
import uk.gov.hmcts.reform.fpl.model.PlacementOrderAndNotices;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.EmailAddress;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.common.Telephone;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisJudge;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static feign.Request.HttpMethod.GET;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.time.LocalDate.now;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static uk.gov.hmcts.reform.fpl.enums.ChildGender.BOY;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HER_HONOUR_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.MAGISTRATES;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

public class TestDataHelper {
    public static final String ALLOCATED_JUDGE_KEY = "allocatedJudge";
    public static final byte[] DOCUMENT_CONTENT = {1, 2, 3, 4, 5};

    private TestDataHelper() {
    }

    public static DocumentReference testDocumentReference() {
        return testDocumentReference(randomAlphanumeric(10));
    }

    public static DocumentReference testDocumentReference(String filename) {
        return DocumentReference.builder()
            .filename(filename)
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
        return testChild(randomAlphanumeric(10), randomAlphanumeric(10), null, now());
    }

    public static Element<Child> testChild(String firstName, String lastName, ChildGender gender, LocalDate dob) {
        return element(Child.builder()
            .party(testChildParty(firstName, lastName, gender, dob))
            .build());
    }

    public static ChildParty testChildParty(String firstName, String lastName, ChildGender gender, LocalDate dob) {
        return ChildParty.builder()
            .firstName(firstName)
            .lastName(lastName)
            .gender(ofNullable(gender).map(ChildGender::getLabel).orElse(null))
            .dateOfBirth(dob)
            .build();
    }

    public static ChildParty testChildParty() {
        return testChildParty(
            randomAlphanumeric(10),
            randomAlphanumeric(10),
            BOY,
            LocalDate.now().minusDays(RandomUtils.nextInt(1, 1000)));
    }

    public static List<Element<Child>> testChildren() {
        return List.of(testChild(), testChild(), testChild());
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

    public static Address testAddress() {
        return Address.builder()
            .addressLine1("Flat 1001")
            .addressLine2("Pinnacle Apartments")
            .addressLine3("Saffron Central Square")
            .postcode("CRO 2GE")
            .build();
    }

    public static Telephone testTelephone() {
        return Telephone.builder()
            .telephoneNumber("0795554444")
            .contactDirection("Harry Kane")
            .build();
    }

    public static EmailAddress testEmail() {
        return EmailAddress.builder().email("test@test.com").build();
    }

    public static Other testOther() {
        return Other.builder()
            .name(randomAlphanumeric(20))
            .birthPlace(randomAlphanumeric(10))
            .address(testAddress())
            .build();
    }

    public static Judge testJudge() {
        return Judge.builder()
            .judgeTitle(MAGISTRATES)
            .judgeLastName("Stark")
            .judgeFullName("Brandon Stark")
            .build();
    }

    public static JudgeAndLegalAdvisor testJudgeAndLegalAdviser() {
        return JudgeAndLegalAdvisor.builder()
            .judgeTitle(HER_HONOUR_JUDGE)
            .judgeLastName("Judy")
            .legalAdvisorName("Peter Parker")
            .build();
    }

    public static DocmosisJudge testDocmosisJudge() {
        return DocmosisJudge.builder()
            .judgeTitleAndName("Brandon Stark (JP)")
            .build();
    }

    public static FeignException feignException(int status) {
        return feignException(status, "Test");
    }

    public static FeignException feignException(int status, String message) {
        return FeignException.errorStatus(message, Response.builder()
            .status(status)
            .request(Request.create(GET, EMPTY, Map.of(), new byte[]{}, UTF_8, null))
            .build());
    }
}
