package uk.gov.hmcts.reform.fpl.model.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import uk.gov.hmcts.reform.fpl.enums.DirectionType;
import uk.gov.hmcts.reform.fpl.model.StandardDirection;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.DirectionType.APPOINT_CHILDREN_GUARDIAN;
import static uk.gov.hmcts.reform.fpl.enums.DirectionType.ARRANGE_INTERPRETERS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionType.ASK_FOR_DISCLOSURE;
import static uk.gov.hmcts.reform.fpl.enums.DirectionType.OBJECT_TO_REQUEST_FOR_DISCLOSURE;
import static uk.gov.hmcts.reform.fpl.enums.DirectionType.REQUEST_PERMISSION_FOR_EXPERT_EVIDENCE;
import static uk.gov.hmcts.reform.fpl.enums.DirectionType.SEND_DOCUMENTS_TO_ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.DirectionType.SEND_MISSING_ANNEX;
import static uk.gov.hmcts.reform.fpl.enums.DirectionType.SEND_RESPONSE_TO_THRESHOLD_STATEMENT;
import static uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement.ENGLISH_TO_WELSH;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

class GatekeepingOrderEventDataTest {

    @Test
    void shouldReturnTemporaryFields() {
        assertThat(GatekeepingOrderEventData.temporaryFields()).containsExactly(
            "urgentHearingOrderDocument", "urgentHearingAllocation", "showUrgentHearingAllocation"
        );
    }

    @Test
    void shouldReturnRequestedStandardDirectionTypes() {
        final GatekeepingOrderEventData underTest = GatekeepingOrderEventData.builder()
            .directionsForAllParties(List.of(REQUEST_PERMISSION_FOR_EXPERT_EVIDENCE, ASK_FOR_DISCLOSURE))
            .directionsForLocalAuthority(List.of(SEND_DOCUMENTS_TO_ALL_PARTIES, SEND_MISSING_ANNEX))
            .directionsForCafcass(List.of(APPOINT_CHILDREN_GUARDIAN))
            .directionsForOthers(List.of(OBJECT_TO_REQUEST_FOR_DISCLOSURE))
            .directionsForRespondents(List.of(SEND_RESPONSE_TO_THRESHOLD_STATEMENT))
            .directionsForCourt(List.of(ARRANGE_INTERPRETERS))
            .build();

        assertThat(underTest.getRequestedDirections())
            .containsExactlyInAnyOrder(
                REQUEST_PERMISSION_FOR_EXPERT_EVIDENCE,
                ASK_FOR_DISCLOSURE,
                SEND_DOCUMENTS_TO_ALL_PARTIES,
                SEND_MISSING_ANNEX,
                APPOINT_CHILDREN_GUARDIAN,
                OBJECT_TO_REQUEST_FOR_DISCLOSURE,
                SEND_RESPONSE_TO_THRESHOLD_STATEMENT,
                ARRANGE_INTERPRETERS);
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldReturnEmptyRequestedStandardDirectionsTypes(List<DirectionType> requestedDirections) {
        final GatekeepingOrderEventData underTest = GatekeepingOrderEventData.builder()
            .directionsForAllParties(requestedDirections)
            .directionsForLocalAuthority(requestedDirections)
            .directionsForCafcass(requestedDirections)
            .directionsForOthers(requestedDirections)
            .directionsForRespondents(requestedDirections)
            .directionsForCourt(requestedDirections)
            .build();

        assertThat(underTest.getRequestedDirections()).isEmpty();
    }

    @Test
    void shouldResetStandardDirections() {
        final GatekeepingOrderEventData underTest = GatekeepingOrderEventData.builder()
            .standardDirections(wrapElements(
                StandardDirection.builder().build(),
                StandardDirection.builder().build()))
            .build();

        assertThat(underTest.resetStandardDirections()).isEmpty();
        assertThat(underTest.getStandardDirections()).isEmpty();
    }

    @Test
    void shouldReturnLanguageRequirements() {
        final GatekeepingOrderEventData underTest = GatekeepingOrderEventData.builder()
            .gatekeepingTranslationRequirements(ENGLISH_TO_WELSH)
            .build();

        assertThat(underTest.getGatekeepingTranslationRequirements()).isEqualTo(ENGLISH_TO_WELSH);
    }
}
