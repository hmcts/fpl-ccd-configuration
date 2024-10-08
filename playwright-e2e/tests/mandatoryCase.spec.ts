import { test } from "../fixtures/fixtures";
import { newSwanseaLocalAuthorityUserOne } from "../settings/user-credentials";
import { createCase, updateCase } from "../utils/api-helper";
import caseData from "../caseData/Cafcass-integration-test/caseWithHearingDetails.json" assert { type: "json" };

test.describe('Cafcass Integration test', () => {
    const dateTime = new Date().toISOString();
    let caseNumber : string;
    let caseName : string;
    test.beforeEach(async ()  => {
        caseName = 'Cafcass Integration Test ' + dateTime;
        caseNumber = await createCase(caseName, newSwanseaLocalAuthorityUserOne);
    });

    test("Integration Test", async ({page,
                                        signInPage,
                                        startApplication,
                                        submitCase,
                                        makeAxeBuilder}, testInfo) => {
        await updateCase(caseName, caseNumber, caseData);
        console.log('caseID: ' + caseNumber);
    });
});
