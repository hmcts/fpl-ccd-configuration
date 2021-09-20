package uk.gov.hmcts.reform.fpl.service.orders.prepopulator.question;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.enums.C2AdditionalOrdersRequested;
import uk.gov.hmcts.reform.fpl.enums.OtherApplicationType;
import uk.gov.hmcts.reform.fpl.enums.ParentalResponsibilityType;
import uk.gov.hmcts.reform.fpl.enums.RelationshipWithChild;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.OtherApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.buildDynamicList;

class ParentalResponsibilityPrePopulatorTest {

    private static final String PARENT_RESPONSIBLE = "manageOrdersParentResponsible";
    private static final String RELATIONSHIP_TO_CHILD = "manageOrdersRelationshipWithChild";
    private static final String APPLICANT_NAME = "Remmie responsible";
    private final ParentalResponsibilityPrePopulator underTest = new ParentalResponsibilityPrePopulator();

    private static UUID linkedApplicationId;
    private static DynamicList selectedLinkedApplicationList;

    @BeforeEach
    void setup() {
        linkedApplicationId = randomUUID();
        selectedLinkedApplicationList = buildDynamicList(0, Pair.of(linkedApplicationId, "My application"));
    }

    @Test
    void testAcceptsParentalResponsibilitySection() {
        assertThat(underTest.accept()).isEqualTo(OrderQuestionBlock.PARENTAL_RESPONSIBILITY);
    }

    @Test
    void shouldPrePopulateWithFatherRelationshipDetailsAndApplicantName() {
        List<Element<AdditionalApplicationsBundle>> additionalApplicationsBundle =
            buildAdditionalApplicationsBundle(
                linkedApplicationId,
                ParentalResponsibilityType.PR_BY_FATHER
            );

        CaseData caseData = buildCaseData(selectedLinkedApplicationList, additionalApplicationsBundle);

        assertThat(underTest.prePopulate(caseData)).isEqualTo(
            Map.of(
                PARENT_RESPONSIBLE, APPLICANT_NAME,
                RELATIONSHIP_TO_CHILD, RelationshipWithChild.FATHER
            )
        );
    }

    @Test
    void shouldPrePopulateWithMotherRelationshipDetailsAndApplicantName() {
        List<Element<AdditionalApplicationsBundle>> additionalApplicationsBundle =
            buildAdditionalApplicationsBundle(
                linkedApplicationId,
                ParentalResponsibilityType.PR_BY_SECOND_FEMALE_PARENT
            );

        CaseData caseData = buildCaseData(selectedLinkedApplicationList, additionalApplicationsBundle);

        assertThat(underTest.prePopulate(caseData)).isEqualTo(
            Map.of(
                PARENT_RESPONSIBLE, APPLICANT_NAME,
                RELATIONSHIP_TO_CHILD, RelationshipWithChild.SECOND_FEMALE_PARENT
            )
        );
    }

    @Test
    public void shouldReturnEmptyMapWhen_RelationshipWithChildIsSet() {
        List<Element<AdditionalApplicationsBundle>> additionalApplicationsBundle =
            buildAdditionalApplicationsBundle(
                linkedApplicationId,
                ParentalResponsibilityType.PR_BY_FATHER
            );

        CaseData caseData = buildCaseData(selectedLinkedApplicationList, additionalApplicationsBundle).toBuilder()
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersRelationshipWithChild(RelationshipWithChild.FATHER)
                .manageOrdersParentResponsible(null)
                .build()
            )
            .build();

        assertThat(underTest.prePopulate(caseData)).isEqualTo(Map.of());
    }

    @Test
    void shouldReturnEmptyMapWhen_ParentResponsibleIsSet() {
        List<Element<AdditionalApplicationsBundle>> additionalApplicationsBundle =
            buildAdditionalApplicationsBundle(
                linkedApplicationId,
                ParentalResponsibilityType.PR_BY_FATHER
            );

        CaseData caseData = buildCaseData(selectedLinkedApplicationList, additionalApplicationsBundle).toBuilder()
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersRelationshipWithChild(null)
                .manageOrdersParentResponsible(APPLICANT_NAME)
                .build()
            )
            .build();

        assertThat(underTest.prePopulate(caseData)).isEqualTo(Map.of());
    }

    @Test
    void shouldReturnEmptyMapWhen_LinkedApplicationIsNull() {
        List<Element<AdditionalApplicationsBundle>> additionalApplicationsBundle =
            buildAdditionalApplicationsBundle(
                null,
                null
            );

        CaseData caseData = buildCaseData(selectedLinkedApplicationList, additionalApplicationsBundle).toBuilder()
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersRelationshipWithChild(null)
                .manageOrdersParentResponsible(null)
                .build()
            )
            .build();

        assertThat(underTest.prePopulate(caseData)).isEqualTo(Map.of());
    }

    @Test
    void shouldReturnEmptyMapWhen_SelectedApplicationIdIsNull() {
        List<Element<AdditionalApplicationsBundle>> additionalApplicationsBundle =
            List.of(element(
                AdditionalApplicationsBundle.builder()
                    .c2DocumentBundle(C2DocumentBundle.builder()
                        .id(null)
                        .applicantName(APPLICANT_NAME)
                        .c2AdditionalOrdersRequested(List.of(C2AdditionalOrdersRequested.PARENTAL_RESPONSIBILITY))
                        .parentalResponsibilityType(ParentalResponsibilityType.PR_BY_FATHER)
                        .build())
                    .build())
            );

        CaseData caseData = buildCaseData(selectedLinkedApplicationList, additionalApplicationsBundle).toBuilder()
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersRelationshipWithChild(null)
                .manageOrdersParentResponsible(null)
                .build()
            )
            .build();

        assertThat(underTest.prePopulate(caseData)).isEqualTo(Map.of());
    }

    @Test
    void shouldReturnEmptyMapWhen_SelectedApplicationBundleIsNull() {
        CaseData caseData = buildCaseData(selectedLinkedApplicationList, null).toBuilder()
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersRelationshipWithChild(null)
                .manageOrdersParentResponsible(null)
                .build()
            )
            .build();

        assertThat(underTest.prePopulate(caseData)).isEqualTo(Map.of());
    }

    @Test
    void shouldReturnEmptyMapWhen_selectedApplicationIdIsNull() {
        CaseData caseData = buildCaseData(null, null).toBuilder()
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersRelationshipWithChild(null)
                .manageOrdersParentResponsible(null)
                .build()
            )
            .build();

        assertThat(underTest.prePopulate(caseData)).isEqualTo(Map.of());
    }


    @Test
    void shouldReturnEmptyMapWhen_NotC2andOtherApplicationIsNot_C1_Parental_Responsibility() {
        OtherApplicationsBundle otherApplicationsBundle = OtherApplicationsBundle.builder()
            .id(linkedApplicationId)
            .applicationType(OtherApplicationType.C1_APPOINTMENT_OF_A_GUARDIAN)
            .parentalResponsibilityType(ParentalResponsibilityType.PR_BY_SECOND_FEMALE_PARENT)
            .applicantName(APPLICANT_NAME)
            .build();

        List<Element<AdditionalApplicationsBundle>> additionalApplicationsBundle =
            List.of(element(linkedApplicationId,
                AdditionalApplicationsBundle.builder()
                    .otherApplicationsBundle(otherApplicationsBundle)
                    .build())
            );

        CaseData caseData = buildCaseData(selectedLinkedApplicationList, additionalApplicationsBundle);

        assertThat(underTest.prePopulate(caseData)).isEqualTo(
            Map.of()
        );
    }

    @Test
    void shouldPrePopulateWithMotherRelationshipDetailsAndApplicantName_WithC1Application() {
        OtherApplicationsBundle otherApplicationsBundle = OtherApplicationsBundle.builder()
            .id(linkedApplicationId)
            .applicationType(OtherApplicationType.C1_PARENTAL_RESPONSIBILITY)
            .parentalResponsibilityType(ParentalResponsibilityType.PR_BY_SECOND_FEMALE_PARENT)
            .applicantName(APPLICANT_NAME)
            .build();

        List<Element<AdditionalApplicationsBundle>> additionalApplicationsBundle =
            List.of(element(linkedApplicationId,
                AdditionalApplicationsBundle.builder()
                    .otherApplicationsBundle(otherApplicationsBundle)
                    .build())
            );

        CaseData caseData = buildCaseData(selectedLinkedApplicationList, additionalApplicationsBundle);

        assertThat(underTest.prePopulate(caseData)).isEqualTo(
            Map.of(
                PARENT_RESPONSIBLE, ParentalResponsibilityPrePopulatorTest.APPLICANT_NAME,
                RELATIONSHIP_TO_CHILD, RelationshipWithChild.SECOND_FEMALE_PARENT
            )
        );
    }

    private CaseData buildCaseData(DynamicList selectedLinkedApplicationList,
                                   List<Element<AdditionalApplicationsBundle>> additionalApplicationsBundle) {
        return CaseData.builder()
            .additionalApplicationsBundle(additionalApplicationsBundle)
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersLinkedApplication(selectedLinkedApplicationList)
                .manageOrdersRelationshipWithChild(null)
                .manageOrdersParentResponsible(null)
                .build()
            ).build();
    }

    private List<Element<AdditionalApplicationsBundle>> buildAdditionalApplicationsBundle(
        UUID uuid, ParentalResponsibilityType responsibilityType) {
        OtherApplicationsBundle otherApplicationsBundle = OtherApplicationsBundle.builder()
            .id(uuid)
            .applicationType(OtherApplicationType.C1_WITH_SUPPLEMENT)
            .build();

        return List.of(element(
            AdditionalApplicationsBundle.builder()
                .otherApplicationsBundle(otherApplicationsBundle)
                .c2DocumentBundle(C2DocumentBundle.builder()
                    .id(uuid)
                    .applicantName(APPLICANT_NAME)
                    .c2AdditionalOrdersRequested(List.of(C2AdditionalOrdersRequested.PARENTAL_RESPONSIBILITY))
                    .parentalResponsibilityType(responsibilityType)
                    .build())
                .build())
        );
    }
}
