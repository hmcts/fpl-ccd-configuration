import { test } from "../fixtures/create-fixture";
import { Apihelp } from "../utils/api-helper";
import caseData from "../caseData/mandatorySubmissionFields.json";
import { newSwanseaLocalAuthorityUserOne } from "../settings/user-credentials";
import { expect } from "@playwright/test";

test.describe("Submit a case", () => {
    let apiDataSetup = new Apihelp();
    const dateTime = new Date().toISOString();
    let caseNumber: string;
    let casename: string;
    test.beforeEach(async () => {
        caseNumber = await apiDataSetup.createCase(
            "Case Submitted via API",
            newSwanseaLocalAuthorityUserOne,
        );
    });

    test("Submitted Case via API", async ({ signInPage }) => {
        casename = "Case Submitted via API" + dateTime.slice(0, 10);
        await apiDataSetup.updateCase(casename, caseNumber, caseData);
        await signInPage.visit();
    });
});
