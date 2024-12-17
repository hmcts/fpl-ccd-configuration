import {test} from '../fixtures/create-fixture';
import {expect} from "../fixtures/fixtures";
import {updateCase} from "../utils/api-helper";
import caseData from "../caseData/mandatorySubmissionFields.json";
import {CTSCUser, judgeUser, newSwanseaLocalAuthorityUserOne} from "../settings/user-credentials";
import config from "../settings/test-docs/config";




test("Create CTSC session state", async ({ page,signInPage, context }) => {

    await signInPage.visit();
    await signInPage.login(CTSCUser.email, CTSCUser.password);
    await context.storageState({ path: config.CTSCUserAuthFile });
});
test("Create legal user session state", async ({ page,signInPage, context }) => {

    await signInPage.visit();
    await signInPage.login(judgeUser.email,judgeUser.password);
    await context.storageState({ path: config.legalUserAuthFile });
});
test("Create legal user session state", async ({ page,signInPage, context }) => {
    await signInPage.visit();
    await signInPage.login(newSwanseaLocalAuthorityUserOne.email,newSwanseaLocalAuthorityUserOne.password);
    await context.storageState({ path: config.LAUserAuthFile });
});


