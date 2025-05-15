import { test } from "../../fixtures/api-test-fixture";
import {
    newSwanseaLocalAuthorityUserOne,
    wiltshireCountyUserOne,
    wiltshireCountyUserTwo
} from "../../settings/user-credentials";
import { expect } from "@playwright/test";


test('Create and share case API test @apiTest', async ({callback}) => {
    let caseDetails: any
    await test.step('create a new case', async () => {
        caseDetails = await callback.createCase(wiltshireCountyUserOne, "Create case API test");
    });

    await test.step('verify case data', () => {
        const caseData = caseDetails.caseData;
        expect(caseData).toBeDefined();
        expect(caseData.id).toBeDefined();
        expect(caseData.state).toEqual("Open")
        expect(caseData.caseLocalAuthority).toEqual("SNW");
        expect(caseData.localAuthorityPolicy).toBeDefined();
        expect(caseData.localAuthorityPolicy.Organisation).toEqual({
            OrganisationID: "11VOC93",
            OrganisationName: "Wiltshire County Council"
        });
    });

    await test.step('verify case access', async () => {
        await callback.getCase(wiltshireCountyUserTwo, caseDetails.id);
        await callback.getCase(newSwanseaLocalAuthorityUserOne, caseDetails.id, false);
    });
});