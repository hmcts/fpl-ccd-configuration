package uk.gov.hmcts.reform.fpl.service.removeorder;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.SentDocument;
import uk.gov.hmcts.reform.fpl.model.SentDocuments;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.UUID.fromString;
import static org.assertj.core.api.Assertions.assertThat;
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
