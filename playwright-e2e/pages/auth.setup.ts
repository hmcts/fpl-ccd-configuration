import { test as setup, expect } from "@playwright/test";
import { test } from '../fixtures/create-fixture';
import {CTSCUser} from "../settings/user-credentials";


setup("Create Admin Auth", async ({ page,signInPage,context }) => {

    signInPage.login(CTSCUser.email,CTSCUser.password);
    await context.storageState({ path: adminAuthFile });
});

