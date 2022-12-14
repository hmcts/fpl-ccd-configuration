package uk.gov.hmcts.reform.fpl.service.removeorder;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.exceptions.removaltool.RemovableSentDocumentNotFoundException;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.RemovalToolData;
import uk.gov.hmcts.reform.fpl.model.SentDocument;
import uk.gov.hmcts.reform.fpl.model.SentDocuments;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.UUID.fromString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap.caseDetailsMap;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

class RemoveSentDocumentServiceTest {

    private static final String PARTY_NAME_1 = "Atesting One";
    private static final String PARTY_NAME_2 = "Btesting Two";

    private final RemoveSentDocumentService underTest = new RemoveSentDocumentService();

    @Test
    void shouldBuildSortedDynamicListOfDocumentsSentToParties() {
        List<Element<SentDocuments>> sentDocuments = new ArrayList<>();
        sentDocuments.add(element(buildSentDocuments(PARTY_NAME_1,
            Map.of("sentAt", "12 June 2019",
                "filename", "ABC",
                "uuid", fromString("11111111-1111-1111-1111-111111111111")
            ))));
        sentDocuments.add(element(buildSentDocuments(PARTY_NAME_2,
            Map.of("sentAt", "04 June 2019",
                "filename", "DEF",
                "uuid", fromString("21111111-1111-1111-1111-111111111111")
            ),
            Map.of("sentAt", "11 June 2019",
                "filename", "GHI",
                "uuid", fromString("31111111-1111-1111-1111-111111111111")
            )
        )));
        CaseData caseData = CaseData.builder().documentsSentToParties(sentDocuments).build();

        DynamicList listOfApplications = underTest.buildDynamicList(caseData);

        DynamicList expectedList = DynamicList.builder()
            .value(DynamicListElement.EMPTY)
            .listItems(List.of(
                buildListElement(fromString("11111111-1111-1111-1111-111111111111"),
                    "Atesting One - ABC (12 June 2019)"),
                buildListElement(fromString("21111111-1111-1111-1111-111111111111"),
                    "Btesting Two - DEF (04 June 2019)"),
                buildListElement(fromString("31111111-1111-1111-1111-111111111111"),
                    "Btesting Two - GHI (11 June 2019)")
            ))
            .build();

        assertThat(listOfApplications).isEqualTo(expectedList);
    }

    @Test
    void shouldBuildDynamicListOfDocumentsSentToPartiesWithSelected() {
        UUID docId = fromString("21111111-1111-1111-1111-111111111111");
        List<Element<SentDocuments>> sentDocuments = new ArrayList<>();
        sentDocuments.add(element(buildSentDocuments(PARTY_NAME_1,
            Map.of("sentAt", "12 June 2019",
                "filename", "ABC",
                "uuid", fromString("11111111-1111-1111-1111-111111111111")
            ))));
        sentDocuments.add(element(buildSentDocuments(PARTY_NAME_2,
            Map.of("sentAt", "04 June 2019",
                "filename", "DEF",
                "uuid", docId
            ),
            Map.of("sentAt", "11 June 2019",
                "filename", "GHI",
                "uuid", fromString("31111111-1111-1111-1111-111111111111")
            )
        )));
        CaseData caseData = CaseData.builder().documentsSentToParties(sentDocuments).build();

        DynamicList listOfApplications = underTest.buildDynamicList(caseData, docId);

        DynamicList expectedList = DynamicList.builder()
            .value(buildListElement(fromString("21111111-1111-1111-1111-111111111111"),
                "Btesting Two - DEF (04 June 2019)"))
            .listItems(List.of(
                buildListElement(fromString("11111111-1111-1111-1111-111111111111"),
                    "Atesting One - ABC (12 June 2019)"),
                buildListElement(fromString("21111111-1111-1111-1111-111111111111"),
                    "Btesting Two - DEF (04 June 2019)"),
                buildListElement(fromString("31111111-1111-1111-1111-111111111111"),
                    "Btesting Two - GHI (11 June 2019)")
            ))
            .build();

        assertThat(listOfApplications).isEqualTo(expectedList);
    }

    @Test
    void shouldPopulateSentDocumentFields() {
        DocumentReference doc = testDocumentReference();

        CaseDetailsMap caseDetailsMap = caseDetailsMap(Map.of());
        underTest.populateSentDocumentFields(caseDetailsMap, SentDocument.builder()
                .letterId("11111111-1111-1111-1111-111111111111")
                .partyName("Party Name")
                .sentAt("15 October 2020 4pm")
                .document(doc)
            .build());

        HashMap<String, Object> expectedMap = new HashMap<>();
        expectedMap.put("partyNameToBeRemoved", "Party Name");
        expectedMap.put("sentDocumentToBeRemoved", doc);
        expectedMap.put("sentAtToBeRemoved", "15 October 2020 4pm");
        expectedMap.put("letterIdToBeRemoved", "11111111-1111-1111-1111-111111111111");

        assertThat(caseDetailsMap).isEqualTo(expectedMap);
    }

    @Test
    void shouldGetRemovedSentDocumentById() {
        UUID id = UUID.randomUUID();
        List<Element<SentDocument>> sentDocuments = new ArrayList<>();
        sentDocuments.add(element(SentDocument.builder().build()));
        sentDocuments.add(element(id, SentDocument.builder().build()));
        sentDocuments.add(element(SentDocument.builder().build()));

        CaseData caseData = CaseData.builder()
            .documentsSentToParties(List.of(
                element(SentDocuments.builder()
                    .documentsSentToParty(sentDocuments)
                    .build())
            ))
            .build();
        Element<SentDocument> application = underTest.getRemovedSentDocumentById(caseData, id);

        assertThat(application).isEqualTo(sentDocuments.get(1));
    }

    @Test
    void shouldThrowExceptionGetRemovedSentDocumentById() {
        final UUID id = UUID.randomUUID();
        List<Element<SentDocument>> sentDocuments = new ArrayList<>();
        sentDocuments.add(element(SentDocument.builder().build()));
        sentDocuments.add(element(SentDocument.builder().build()));
        sentDocuments.add(element(SentDocument.builder().build()));

        CaseData caseData = CaseData.builder()
            .documentsSentToParties(List.of(
                element(SentDocuments.builder()
                    .documentsSentToParty(sentDocuments)
                    .build())
            ))
            .build();
        assertThatThrownBy(() -> underTest.getRemovedSentDocumentById(caseData, id))
            .isInstanceOf(RemovableSentDocumentNotFoundException.class)
            .hasMessageContaining(String.format("Removable documents sent to parties with id %s not found", id));
    }

    @Test
    void shouldGetRemovedSentDocumentByIdWithTwoParties() {
        final UUID id = UUID.randomUUID();

        List<Element<SentDocument>> sentDocumentsForPartyOne = new ArrayList<>();
        sentDocumentsForPartyOne.add(element(SentDocument.builder().build()));
        sentDocumentsForPartyOne.add(element(SentDocument.builder().build()));
        sentDocumentsForPartyOne.add(element(SentDocument.builder().build()));

        List<Element<SentDocument>> sentDocumentsForPartyTwo = new ArrayList<>();
        sentDocumentsForPartyTwo.add(element(SentDocument.builder().build()));
        sentDocumentsForPartyTwo.add(element(id, SentDocument.builder().build()));
        sentDocumentsForPartyTwo.add(element(SentDocument.builder().build()));

        CaseData caseData = CaseData.builder()
            .documentsSentToParties(List.of(
                element(SentDocuments.builder()
                    .documentsSentToParty(sentDocumentsForPartyOne)
                    .build()),
                element(SentDocuments.builder()
                    .documentsSentToParty(sentDocumentsForPartyTwo)
                    .build())
            ))
            .build();
        Element<SentDocument> application = underTest.getRemovedSentDocumentById(caseData, id);

        assertThat(application).isEqualTo(sentDocumentsForPartyTwo.get(1));
    }

    @SuppressWarnings("unchecked")
    private List<Element<SentDocument>> createSentDocumentsForPartyOne() {
        return new ArrayList(List.of(
            element(fromString("11111111-1111-1111-1111-111111111111"), SentDocument.builder().build()),
            element(fromString("11111111-1111-1111-1111-111111111112"), SentDocument.builder().build()),
            element(fromString("11111111-1111-1111-1111-111111111113"), SentDocument.builder().build())
        ));
    }

    @SuppressWarnings("unchecked")
    private List<Element<SentDocument>> createSentDocumentsForPartyTwo() {
        return new ArrayList<>(List.of(
            element(fromString("21111111-1111-1111-1111-111111111111"), SentDocument.builder().build()),
            element(fromString("21111111-1111-1111-1111-111111111112"), SentDocument.builder().build()),
            element(fromString("21111111-1111-1111-1111-111111111113"), SentDocument.builder().build())
        ));
    }

    private List<Element<SentDocument>> createExpectedSentDocumentsForPartyTwo() {
        return List.of(
            element(fromString("21111111-1111-1111-1111-111111111111"), SentDocument.builder().build()),
            element(fromString("21111111-1111-1111-1111-111111111113"), SentDocument.builder().build())
        );
    }

    @SuppressWarnings("unchecked")
    private List<Element<SentDocument>> createSentDocumentsForPartyThree() {
        return new ArrayList<>(List.of(
            element(fromString("31111111-1111-1111-1111-111111111112"), SentDocument.builder().build())
        ));
    }

    @SuppressWarnings("unchecked")
    private List<Element<SentDocument>> createExistingHiddenSentDocuments() {
        return new ArrayList<>(List.of(
            element(fromString("e1111111-1111-1111-1111-111111111111"), SentDocument.builder().build())
        ));
    }

    @Test
    void shouldRemoveSentDocumentFromCase() {
        final UUID idToBeRemoved = fromString("21111111-1111-1111-1111-111111111112");

        List<Element<SentDocuments>> documentsSentToParties = List.of(
            element(fromString("AAAAAAAA-1111-1111-1111-111111111111"), SentDocuments.builder()
                .documentsSentToParty(createSentDocumentsForPartyOne())
                .build()),
            element(fromString("BBBBBBBB-1111-1111-1111-111111111111"), SentDocuments.builder()
                .documentsSentToParty(createSentDocumentsForPartyTwo())
                .build())
        );

        CaseDetailsMap caseDetailsMap = caseDetailsMap(CaseDetails.builder()
            .data(Map.of("documentsSentToParties", documentsSentToParties))
            .build());
        CaseData caseData = CaseData.builder()
            .documentsSentToParties(documentsSentToParties)
            .removalToolData(RemovalToolData.builder()
                .reasonToRemoveSentDocument("This is the reason.")
                .build())
            .build();

        underTest.removeSentDocumentFromCase(caseData, caseDetailsMap, idToBeRemoved);

        List<Element<SentDocuments>> expectedHiddenDocumentsSentToParties = List.of(
            element(fromString("BBBBBBBB-1111-1111-1111-111111111111"), SentDocuments.builder()
                .documentsSentToParty(List.of(element(idToBeRemoved,
                    SentDocument.builder().removalReason("This is the reason.").build())))
                .build())
        );
        List<Element<SentDocuments>> expectedNewDocumentsSentToParties = List.of(
            element(fromString("AAAAAAAA-1111-1111-1111-111111111111"), SentDocuments.builder()
                .documentsSentToParty(createSentDocumentsForPartyOne())
                .build()),
            element(fromString("BBBBBBBB-1111-1111-1111-111111111111"), SentDocuments.builder()
                .documentsSentToParty(createExpectedSentDocumentsForPartyTwo())
                .build())
        );

        assertThat(caseDetailsMap).containsAllEntriesOf(
            Map.of("documentsSentToParties", expectedNewDocumentsSentToParties,
                "hiddenDocumentsSentToParties", expectedHiddenDocumentsSentToParties));
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldRemoveLastSentDocumentFromParty() {
        final UUID idToBeRemoved = fromString("31111111-1111-1111-1111-111111111112");

        List<Element<SentDocuments>> documentsSentToParties = new ArrayList<>(List.of(
            element(fromString("AAAAAAAA-1111-1111-1111-111111111111"), SentDocuments.builder()
                .documentsSentToParty(createSentDocumentsForPartyOne())
                .build()),
            element(fromString("BBBBBBBB-1111-1111-1111-111111111111"), SentDocuments.builder()
                .documentsSentToParty(createSentDocumentsForPartyThree())
                .build())
        ));

        CaseDetailsMap caseDetailsMap = caseDetailsMap(CaseDetails.builder()
            .data(Map.of("documentsSentToParties", documentsSentToParties))
            .build());
        CaseData caseData = CaseData.builder()
            .documentsSentToParties(documentsSentToParties)
            .removalToolData(RemovalToolData.builder()
                .reasonToRemoveSentDocument("This is the reason.")
                .build())
            .build();

        underTest.removeSentDocumentFromCase(caseData, caseDetailsMap, idToBeRemoved);

        List<Element<SentDocuments>> expectedHiddenDocumentsSentToParties = List.of(
            element(fromString("BBBBBBBB-1111-1111-1111-111111111111"), SentDocuments.builder()
                .documentsSentToParty(List.of(element(idToBeRemoved,
                    SentDocument.builder().removalReason("This is the reason.").build())))
                .build())
        );
        List<Element<SentDocuments>> expectedNewDocumentsSentToParties = List.of(
            element(fromString("AAAAAAAA-1111-1111-1111-111111111111"), SentDocuments.builder()
                .documentsSentToParty(createSentDocumentsForPartyOne())
                .build())
        );

        assertThat(caseDetailsMap).containsAllEntriesOf(
            Map.of("documentsSentToParties", expectedNewDocumentsSentToParties,
                "hiddenDocumentsSentToParties", expectedHiddenDocumentsSentToParties));
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldRemoveSentDocumentFromCaseIfHiddenSentDocumentsExists() {
        final UUID idToBeRemoved = fromString("21111111-1111-1111-1111-111111111112");

        List<Element<SentDocuments>> documentsSentToParties = List.of(
            element(fromString("AAAAAAAA-1111-1111-1111-111111111111"), SentDocuments.builder()
                .documentsSentToParty(createSentDocumentsForPartyOne())
                .build()),
            element(fromString("BBBBBBBB-1111-1111-1111-111111111111"), SentDocuments.builder()
                .documentsSentToParty(createSentDocumentsForPartyTwo())
                .build())
        );

        CaseDetailsMap caseDetailsMap = caseDetailsMap(CaseDetails.builder()
            .data(Map.of("documentsSentToParties", documentsSentToParties))
            .build());
        CaseData caseData = CaseData.builder()
            .documentsSentToParties(documentsSentToParties)
            .removalToolData(RemovalToolData.builder()
                .reasonToRemoveSentDocument("This is the reason.")
                .hiddenDocumentsSentToParties(new ArrayList<>(List.of(
                    element(fromString("BBBBBBBB-1111-1111-1111-111111111111"), SentDocuments.builder()
                        .documentsSentToParty(createExistingHiddenSentDocuments())
                        .build())
                )))
                .build())
            .build();

        underTest.removeSentDocumentFromCase(caseData, caseDetailsMap, idToBeRemoved);

        List<Element<SentDocuments>> expectedHiddenDocumentsSentToParties =
            new ArrayList<>(List.of(
                element(fromString("BBBBBBBB-1111-1111-1111-111111111111"), SentDocuments.builder()
                    .documentsSentToParty(createExistingHiddenSentDocuments())
                    .build())
            ));
        expectedHiddenDocumentsSentToParties.get(0).getValue().getDocumentsSentToParty()
            .add(element(idToBeRemoved,
                    SentDocument.builder().removalReason("This is the reason.").build()));

        List<Element<SentDocuments>> expectedNewDocumentsSentToParties = List.of(
            element(fromString("AAAAAAAA-1111-1111-1111-111111111111"), SentDocuments.builder()
                .documentsSentToParty(createSentDocumentsForPartyOne())
                .build()),
            element(fromString("BBBBBBBB-1111-1111-1111-111111111111"), SentDocuments.builder()
                .documentsSentToParty(createExpectedSentDocumentsForPartyTwo())
                .build())
        );

        assertThat(caseDetailsMap).containsAllEntriesOf(
            Map.of("documentsSentToParties", expectedNewDocumentsSentToParties,
                "hiddenDocumentsSentToParties", expectedHiddenDocumentsSentToParties));
    }

    private SentDocuments buildSentDocuments(String partyName, Map... fileInfos) {
        List<Element<SentDocument>> documentsSentToParty = new ArrayList<>();

        for (Map fileInfo : fileInfos) {
            documentsSentToParty.add(element((UUID) fileInfo.get("uuid"), SentDocument.builder()
                .partyName(partyName)
                .document(testDocumentReference((String) fileInfo.get("filename")))
                .sentAt((String) fileInfo.get("sentAt"))
                .build()));
        }

        return SentDocuments.builder()
            .partyName(partyName)
            .documentsSentToParty(documentsSentToParty)
            .build();
    }

    private DynamicListElement buildListElement(UUID id, String label) {
        return DynamicListElement.builder()
            .code(id)
            .label(label)
            .build();
    }
}
