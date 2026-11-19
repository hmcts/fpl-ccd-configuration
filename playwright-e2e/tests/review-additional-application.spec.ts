import {expect, test} from "../fixtures/fixtures";
import {
    CTSCUser,
    judgeWalesUser,
    newSwanseaLocalAuthorityUserOne,
    privateSolicitorOrgUser
} from "../settings/user-credentials";
import {createCase, updateCase} from "../utils/api-helper";
import caseWithC2App from "../caseData/caseWithC2Application.json" assert {type: "json"}
import {testConfig} from "../settings/test-config";
import caseWithChildrenCafcassSolicitor from "../caseData/caseWithMultipleChildCafcassSolicitor.json";


test.describe('Court Service', () => {
    const dateTime = new Date().toISOString();
    let caseNumber: string;

    test.beforeEach(async () => {
        caseNumber = await createCase('Court service', privateSolicitorOrgUser);
        expect(caseNumber).toBeDefined();
        expect(await updateCase("caseName", caseNumber, caseWithC2App)).toBeTruthy();
    });
    test('Judge approve additional application and draft order @xbrowser',
        async ({startApplication, signInPage, courtServices, makeAxeBuilder}, testInfo) => {
      let caseName = 'Judge approve additional application and draft order ' + dateTime;
            expect(await updateCase(caseName, caseNumber, caseWithC2App)).toBeTruthy();
            test.step('Login and navigate to case', async () => {

                await signInPage.login(judgeWalesUser.email, judgeWalesUser.password);
                await signInPage.navigateToCaseDetails(caseNumber);
                await signInPage.gotoNextStep('Review additional application');
            });
            test.step('Judge review the application details', async () => {

            });
            test.step(
                'Judge approve the application and draft order', async () => {

                }
            );

            test.step('Assert the event success ', async () => {

            });
            if(testConfig.waEnabled) {
            test.step(
                'Assert the WA task', async () => {
                });

            }
        });
});
