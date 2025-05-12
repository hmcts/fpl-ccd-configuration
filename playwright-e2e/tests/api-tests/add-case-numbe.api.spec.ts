import { test } from "../../fixtures/api-test-fixture";
import { swanseaOrgCAAUser } from "../../settings/user-credentials";
import { expect } from "@playwright/test";

test.describe('Add case number API test @apiTest', () => {
    let caseDetailsBefore : any;
    test.beforeAll(async ({ callback }) => {
        caseDetailsBefore = await callback.createCase(swanseaOrgCAAUser, "Submit case API test");
    });

    test('submitted', async ({ callback }) => {
        caseDetailsBefore.caseData.familyManCaseNumber = '123456789';
        let rspResult = await callback.callSubmitted("add-case-number", swanseaOrgCAAUser, 
            caseDetailsBefore, caseDetailsBefore);

        expect(rspResult.httpStatus).toBe(200);
    })
});