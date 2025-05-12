import { test } from "../../fixtures/api-test-fixture";
import { swanseaOrgCAAUser } from "../../settings/user-credentials";

test.describe('Admin manage order API test @apiTest', () => {
    let caseDetailsBefore : any;
    test.beforeAll(async ({ callback }) => {
        caseDetailsBefore = await callback.createCase(swanseaOrgCAAUser, "Submit case API test");
    });

    test('C32a', async ({ manageOrderTestService }) => {
        await manageOrderTestService.testManageOrderContentSame(caseDetailsBefore, 'c32a');
    });

    test('C32b', async ({ manageOrderTestService }) => {
        await manageOrderTestService.testManageOrderContentSame(caseDetailsBefore, 'c32b');
    });

    test('C23', async ({ manageOrderTestService }) => {
        await manageOrderTestService.testManageOrderContentSame(caseDetailsBefore, 'c23');
    });

    test('C33', async ({ manageOrderTestService }) => {
        await manageOrderTestService.testManageOrderContentSame(caseDetailsBefore, 'c33');
    });

    test('C35a', async ({ manageOrderTestService }) => {
        await manageOrderTestService.testManageOrderContentSame(caseDetailsBefore, 'c35a');
    });

    test('C35b', async ({ manageOrderTestService }) => {
        await manageOrderTestService.testManageOrderContentSame(caseDetailsBefore, 'c35b');
    });

    test('C43a', async ({ manageOrderTestService }) => {
        await manageOrderTestService.testManageOrderContentSame(caseDetailsBefore, 'c43a');
    });

    test('C29', async ({ manageOrderTestService }) => {
        await manageOrderTestService.testManageOrderContentSame(caseDetailsBefore, 'c29');
    });

    test('C47a', async ({ manageOrderTestService }) => {
        await manageOrderTestService.testManageOrderContentSame(caseDetailsBefore, 'c47a');
    });
});
