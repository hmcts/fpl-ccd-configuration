package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.enums.OtherApplicationType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.OtherApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.event.ConfirmApplicationReviewedEventData;
import uk.gov.hmcts.reform.fpl.service.additionalapplications.ConfirmApplicationReviewedService;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.asDynamicList;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@ExtendWith(SpringExtension.class)
class ConfirmApplicationReviewedServiceTest {

    private ConfirmApplicationReviewedService confirmApplicationReviewedService =
        new ConfirmApplicationReviewedService();

    private static final Element<AdditionalApplicationsBundle> REVIEWED_BUNDLE =
        element(AdditionalApplicationsBundle.builder()
           .uploadedDateTime("1 January 2021, 12:00pm")
           .author("TEST_REVIEWED")
           .c2DocumentBundle(C2DocumentBundle.builder().uploadedDateTime("1 January 2021, 12:00pm").build())
           .applicationReviewed(YES)
           .build());

    private static final Element<AdditionalApplicationsBundle> NEW_BUNDLE_1 =
        element(AdditionalApplicationsBundle.builder()
            .uploadedDateTime("1 January 2021, 12:00pm")
            .author("TESTING1")
            .c2DocumentBundle(C2DocumentBundle.builder()
                .uploadedDateTime("1 January 2021, 12:00pm").build())
            .build());

    private static final Element<AdditionalApplicationsBundle> NEW_BUNDLE_2 =
        element(AdditionalApplicationsBundle.builder()
            .uploadedDateTime("1 January 2021, 12:00pm")
            .author("TESTING2")
            .otherApplicationsBundle(OtherApplicationsBundle.builder()
                .applicationType(OtherApplicationType.C1_TERMINATION_OF_APPOINTMENT_OF_A_GUARDIAN)
                .uploadedDateTime("1 January 2021, 12:00pm").build())
            .build());

    @Test
    void shouldInitEventFieldWithListOfBundlesToBeReviewed() {
        CaseData caseData = CaseData.builder()
            .additionalApplicationsBundle(List.of(REVIEWED_BUNDLE, NEW_BUNDLE_1, NEW_BUNDLE_2))
            .build();

        Map<String, Object> resultMap = confirmApplicationReviewedService.initEventField(caseData);

        Map<String, Object> expectedMap = Map.of(
            "hasApplicationToBeReviewed", YES,
            "confirmApplicationReviewedList",
            asDynamicList(List.of(NEW_BUNDLE_1, NEW_BUNDLE_2), AdditionalApplicationsBundle::toLabel)
        );

        assertThat(resultMap).isEqualTo(expectedMap);
    }

    @Test
    void shouldInitEventFieldWithOutBundlesToBeReviewed() {
        CaseData caseData = CaseData.builder()
            .additionalApplicationsBundle(List.of(REVIEWED_BUNDLE))
            .build();

        Map<String, Object> resultMap = confirmApplicationReviewedService.initEventField(caseData);

        Map<String, Object> expectedMap = Map.of("hasApplicationToBeReviewed", NO);

        assertThat(resultMap).isEqualTo(expectedMap);
    }

    @Test
    void shouldGetSelectedApplicationsToBeReviewed() {
        CaseData caseData = CaseData.builder()
            .additionalApplicationsBundle(List.of(REVIEWED_BUNDLE, NEW_BUNDLE_1, NEW_BUNDLE_2))
            .confirmApplicationReviewedEventData(ConfirmApplicationReviewedEventData.builder()
                .confirmApplicationReviewedList(asDynamicList(List.of(NEW_BUNDLE_1, NEW_BUNDLE_2),
                    NEW_BUNDLE_1.getId(), AdditionalApplicationsBundle::toLabel))
                .build())
            .build();

        Element<AdditionalApplicationsBundle> result =
            confirmApplicationReviewedService.getSelectedApplicationsToBeReviewed(caseData);

        assertThat(result).isEqualTo(NEW_BUNDLE_1);
    }

    @Test
    void shouldThrowExceptionWhenSelectedBundleNotFound() {
        CaseData caseData = CaseData.builder()
            .additionalApplicationsBundle(List.of(REVIEWED_BUNDLE, NEW_BUNDLE_1, NEW_BUNDLE_2))
            .confirmApplicationReviewedEventData(ConfirmApplicationReviewedEventData.builder()
                .confirmApplicationReviewedList(asDynamicList(List.of(NEW_BUNDLE_1, NEW_BUNDLE_2),
                    UUID.randomUUID(), AdditionalApplicationsBundle::toLabel))
                .build())
            .build();

        assertThatThrownBy(() -> confirmApplicationReviewedService.getSelectedApplicationsToBeReviewed(caseData))
            .isInstanceOf(NoSuchElementException.class)
            .hasMessage("No value present");
    }

    @Test
    void shouldMarkSelectedApplicationsAsReviewed() {
        CaseData caseData = CaseData.builder()
            .additionalApplicationsBundle(List.of(REVIEWED_BUNDLE, NEW_BUNDLE_1, NEW_BUNDLE_2))
            .confirmApplicationReviewedEventData(ConfirmApplicationReviewedEventData.builder()
                .confirmApplicationReviewedList(asDynamicList(List.of(NEW_BUNDLE_1, NEW_BUNDLE_2),
                    NEW_BUNDLE_1.getId(), AdditionalApplicationsBundle::toLabel))
                .build())
            .build();

        List<Element<AdditionalApplicationsBundle>> resultList =
            confirmApplicationReviewedService.markSelectedBundleAsReviewed(caseData);

        List<Element<AdditionalApplicationsBundle>> expectedList =
            List.of(REVIEWED_BUNDLE,
                element(NEW_BUNDLE_1.getId(), NEW_BUNDLE_1.getValue().toBuilder().applicationReviewed(YES).build()),
                NEW_BUNDLE_2);

        assertThat(resultList).isEqualTo(expectedList);
    }

    @Test
    void shouldThrowExceptionSelectedApplicationsCannotBeMarkedAsReviewed() {
        CaseData caseData = CaseData.builder()
            .additionalApplicationsBundle(List.of(REVIEWED_BUNDLE, NEW_BUNDLE_1, NEW_BUNDLE_2))
            .confirmApplicationReviewedEventData(ConfirmApplicationReviewedEventData.builder()
                .confirmApplicationReviewedList(asDynamicList(List.of(NEW_BUNDLE_1, NEW_BUNDLE_2),
                    UUID.randomUUID(), AdditionalApplicationsBundle::toLabel))
                .build())
            .build();

        assertThatThrownBy(() -> confirmApplicationReviewedService.markSelectedBundleAsReviewed(caseData))
            .isInstanceOf(NoSuchElementException.class)
            .hasMessage("No value present");
    }
}
