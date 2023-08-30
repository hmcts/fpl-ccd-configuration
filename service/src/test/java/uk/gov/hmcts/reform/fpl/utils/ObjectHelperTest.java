package uk.gov.hmcts.reform.fpl.utils;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.fpl.enums.C2ApplicationType;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CaseSummary;
import uk.gov.hmcts.reform.fpl.model.HearingDocuments;
import uk.gov.hmcts.reform.fpl.model.ManagedDocument;
import uk.gov.hmcts.reform.fpl.model.SentDocuments;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

public class ObjectHelperTest {

    @Test
    public void testEnum() throws Exception {
        CaseData caseData = CaseData.builder()
            .state(State.CLOSED)
            .build();

        assertEquals(State.CLOSED, ObjectHelper.getFieldValue(caseData, "state", State.class));
    }

    @Test
    public void testList() throws Exception {
        List<Element<SentDocuments>> testList = List.of(
            element(SentDocuments.builder().partyName("test1").build()),
            element(SentDocuments.builder().partyName("test2").build()),
            element(SentDocuments.builder().partyName("test3").build()));

        CaseData caseData = CaseData.builder().documentsSentToParties(testList).build();

        assertEquals(testList, ObjectHelper.getFieldValue(caseData, "documentsSentToParties", List.class));
    }

    @Test
    public void testNested() throws Exception {
        CaseData caseData = CaseData.builder()
            .localAuthorityPolicy(OrganisationPolicy.builder()
                .organisation(Organisation.builder()
                    .organisationID("testID").build()).build())
            .build();

        assertEquals("testID", ObjectHelper.getFieldValue(caseData,
            "localAuthorityPolicy.organisation.organisationID", String.class));
    }

    @Test
    public void testListInNestedField() throws Exception {
        List<Element<CaseSummary>> testList = List.of(
            element(CaseSummary.builder().document(TestDataHelper.testDocumentReference()).build()),
            element(CaseSummary.builder().document(TestDataHelper.testDocumentReference()).build()),
            element(CaseSummary.builder().document(TestDataHelper.testDocumentReference()).build()));

        CaseData caseData = CaseData.builder()
            .hearingDocuments(HearingDocuments.builder().caseSummaryList(testList).build())
            .build();

        assertEquals(testList, ObjectHelper.getFieldValue(caseData, "hearingDocuments.caseSummaryList", List.class));
    }

    @Test
    public void testFieldInParentClass() throws Exception {
        List<Element<ManagedDocument>> testList = List.of(
            element(ManagedDocument.builder().removalReason("test").build()),
            element(ManagedDocument.builder().removalReason("test2").build()),
            element(ManagedDocument.builder().removalReason("test3").build()));

        CaseData caseData = CaseData.builder()
            .applicantWitnessStmtList(testList)
            .build();

        assertEquals(testList, ObjectHelper.getFieldValue(caseData,
            "applicantWitnessStmtList", List.class));
    }

    @Test
    public void testNestedObject() throws Exception {
        OrganisationPolicy testObj = OrganisationPolicy.builder()
            .organisation(Organisation.builder()
                .organisationID("testID").build()).build();
        CaseData caseData = CaseData.builder()
            .localAuthorityPolicy(testObj)
            .build();

        assertEquals(testObj, ObjectHelper.getFieldValue(caseData,
            "localAuthorityPolicy", OrganisationPolicy.class));
    }

    @Test
    public void testMap() throws Exception {
        Map<String, C2ApplicationType> testMap = Map.of(
            "test1", C2ApplicationType.WITH_NOTICE,
            "test2", C2ApplicationType.WITHOUT_NOTICE
        );
        CaseData caseData = CaseData.builder()
            .c2ApplicationType(testMap)
            .build();

        assertEquals(testMap, ObjectHelper.getFieldValue(caseData,
            "c2ApplicationType", Map.class));
    }

    @Test
    public void shouldThrowExceptionIfGetterNotFound() {
        class TestGetterNotFound {
            public String testField;
        }

        TestGetterNotFound testObj = new TestGetterNotFound();
        testObj.testField = "testing";

        assertThrows(NoSuchMethodException.class, () -> ObjectHelper.getFieldValue(testObj,
            "testField", String.class));
    }
}
