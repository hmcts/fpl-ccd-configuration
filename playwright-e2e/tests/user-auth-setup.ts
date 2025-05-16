import {test} from '../fixtures/create-fixture';
import {CTSCUser, HighCourtAdminUser, judgeUser, newSwanseaLocalAuthorityUserOne} from "../settings/user-credentials";
import config from "../settings/test-docs/config";
import {testConfig} from "../settings/test-config.ts";




test("Create CTSC session state", async ({ signInPage, context }) => {

    await signInPage.visit();
    await signInPage.login(CTSCUser.email, CTSCUser.password);
    await context.storageState({ path: config.CTSCUserAuthFile });
});
test("Create legal user session state", async ({ signInPage, context }) => {

    await signInPage.visit();
    await signInPage.login(judgeUser.email,judgeUser.password);
    await context.storageState({ path: config.legalUserAuthFile });
});
test("Create LA user session state", async ({ signInPage, context }) => {
    await signInPage.visit();
    await signInPage.login(newSwanseaLocalAuthorityUserOne.email,newSwanseaLocalAuthorityUserOne.password);
    await context.storageState({ path: config.LAUserAuthFile });
});

test("Create court admin user session state", async ({ signInPage, context }) => {
        await signInPage.visit();
        await signInPage.login(CTSCUser.email,CTSCUser.password);
        await context.storageState({ path: config.courtAdminAuthFile });
});


