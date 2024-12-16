import {test} from '../fixtures/create-fixture';
import {expect} from "../fixtures/fixtures";
import {updateCase} from "../utils/api-helper";
import caseData from "../caseData/mandatorySubmissionFields.json";
import {CTSCUser} from "../settings/user-credentials";
import config from "../settings/test-docs/config";


let adminEmail = process.env.ADMIN_USERNAME;
let adminPassword = process.env.ADMIN_PASSWORD;
const adminAuthFile = ".auth/admin.json";

let customer01Email = process.env.CUSTOMER_01_USERNAME;
let customer01Password = process.env.CUSTOMER_01_PASSWORD;
const customer01AuthFile = ".auth/customer01.json";

let customer02Email = process.env.CUSTOMER_02_USERNAME;
let customer02Password = process.env.CUSTOMER_02_PASSWORD;
const customer02AuthFile = ".auth/customer02.json";

test("Create CTSC Auth", async ({ page,signInPage, context }) => {

    await signInPage.visit();
    await signInPage.login(CTSCUser.email, CTSCUser.password);
    await context.storageState({ path: config.CTSCUserAuthFile });
});


