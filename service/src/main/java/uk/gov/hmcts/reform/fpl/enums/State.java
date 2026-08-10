package uk.gov.hmcts.reform.fpl.enums;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Stream;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@RequiredArgsConstructor
@Getter
public enum State {
    @CCD(
            hint = "# ${caseName}\n## **CCD ID: #${[CASE_REFERENCE]}**",
            description = "Initial case state – create title as a minimum; add documents, etc."
    )
    @JsonProperty("Open")
    OPEN("Open"),

    @CCD(
            hint = "# ${caseName}\n## **FamilyMan ID: ${familyManCaseNumber}**\n## **CCD ID: #${[CASE_REFERENCE]}**",
            description = "Submitted case state - LA can no longer edit"
    )
    @JsonProperty("Submitted")
    SUBMITTED("Submitted"),

    @CCD(
            hint = "# ${caseName}\n## **FamilyMan ID: ${familyManCaseNumber}**\n## **CCD ID: #${[CASE_REFERENCE]}**",
            description = "Gatekeeping case state - when send to gatekeeper event is triggered"
    )
    @JsonProperty("Gatekeeping")
    GATEKEEPING("Gatekeeping"),

    @CCD(
            label = "Gatekeeping Listing",
            hint = "# ${caseName}\n## **FamilyMan ID: ${familyManCaseNumber}**\n## **CCD ID: #${[CASE_REFERENCE]}**",
            description = "Gatekeeping Listing case state - when Judicial gatekeeping event is triggered"
    )
    @JsonProperty("GATEKEEPING_LISTING")
    GATEKEEPING_LISTING("GATEKEEPING_LISTING"),

    // State label renamed to 'Case management' as of FPLA-1920.
    // State ID remains 'PREPARE_FOR_HEARING' to avoid breaking existing cases.
    @CCD(
            label = "Case management",
            hint = "# ${caseName}\n## **FamilyMan ID: ${familyManCaseNumber}**\n## **CCD ID: #${[CASE_REFERENCE]}**",
            description = "State indicating that SDO is ready to send - triggered when SDO is issued"
    )
    @JsonProperty("PREPARE_FOR_HEARING")
    CASE_MANAGEMENT("PREPARE_FOR_HEARING", "Case management"),

    @CCD(
            label = "Closed",
            hint = "# ${caseName}\n## **FamilyMan ID: ${familyManCaseNumber}**\n## **CCD ID: #${[CASE_REFERENCE]}**",
            description = "Final case state, no longer allowed to create orders apart from C21"
    )
    CLOSED("CLOSED", "Closed"),

    @CCD(
            hint = "# ${caseName}\n## **FamilyMan ID: ${familyManCaseNumber}**\n## **CCD ID: #${[CASE_REFERENCE]}**",
            description = "Deleted case state - all data is removed"
    )
    @JsonProperty("Deleted")
    DELETED("Deleted"),

    @CCD(
            label = "Returned",
            hint = "# ${caseName}\n## **FamilyMan ID: ${familyManCaseNumber}**\n## **CCD ID: #${[CASE_REFERENCE]}**",
            description = "Returned case state - LA to make changes to previously submitted application"
    )
    RETURNED("RETURNED", "Returned"),

    @CCD(
            label = "Final hearing",
            hint = "# ${caseName}\n## **FamilyMan ID: ${familyManCaseNumber}**\n## **CCD ID: #${[CASE_REFERENCE]}**",
            description = "Final hearing case state - Triggered via CMO event and hearing is of final type"
    )
    FINAL_HEARING("FINAL_HEARING", "Final hearing");

    private final String value;
    private final String label;

    State(String value) {
        this.value = value;
        this.label = value;
    }

    public static State fromValue(final String value) {
        return tryFromValue(value)
            .orElseThrow(() -> new NoSuchElementException("Unable to map " + value + " to a case state"));
    }

    public static Optional<State> tryFromValue(final String value) {
        return Stream.of(values())
            .filter(state -> state.value.equalsIgnoreCase(value))
            .findFirst();
    }
}
