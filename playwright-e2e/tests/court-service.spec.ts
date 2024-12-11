import {expect, test} from "../fixtures/fixtures";
import {CTSCUser, newSwanseaLocalAuthorityUserOne} from "../settings/user-credentials";
import {createCase, updateCase} from "../utils/api-helper";
import caseWithCourtService from "../caseData/mandatoryWithOtherSubmissionFields.json" assert {type: "json"}


test.describe('Court Service', () => {
    const dateTime = new Date().toISOString();
    let caseNumber: string;

    test.beforeEach(async () => {
        caseNumber = await createCase('Court service', newSwanseaLocalAuthorityUserOne);
    });
    test('LA add court service',
        async ({startApplication, signInPage, courtServices, makeAxeBuilder}, testInfo) => {

            // 1. Sign in as local-authority user
            await signInPage.visit();
            await signInPage.login(
                newSwanseaLocalAuthorityUserOne.email,
                newSwanseaLocalAuthorityUserOne.password,
            );
            //sign in page
            await signInPage.isSignedIn();
            await signInPage.navigateTOCaseDetails(caseNumber);

            // Court Services Needed
            await startApplication.courtServicesReqUpdated();
            await courtServices.CourtServicesSmoketest();

            await courtServices.tabNavigation('View application')

            await expect(courtServices.page.locator('#case-viewer-field-read--hearingPreferences').getByText('Court services', {exact: true})).toBeVisible();
            await expect(courtServices.page.locator('ccd-read-complex-field-table')).toContainText('Court services');
            await expect(courtServices.page.getByRole('cell', {name: 'Interpreter', exact: true})).toBeVisible();
            await expect(courtServices.page.getByRole('cell', {name: 'Intermediary', exact: true})).toBeVisible();
            await expect(courtServices.page.getByRole('cell', {
                name: 'Facilities or assistance for a disability',
                exact: true
            })).toBeVisible();
            await expect(courtServices.page.getByRole('cell', {
                name: 'Separate waiting rooms',
                exact: true
            })).toBeVisible();
            await expect(courtServices.page.getByRole('cell', {name: 'Something else', exact: true})).toBeVisible();
            await expect(courtServices.page.getByText('Interpreter needed for the spanish language for the respondent', {exact: true})).toBeVisible();
            await expect(courtServices.page.getByText('Intermediary for the child one', {exact: true})).toBeVisible();
            await expect(courtServices.page.getByText('Wheel chair access need for the child', {exact: true})).toBeVisible();
            await expect(courtServices.page.getByText('Isolated waiting rooms for the vulnerable child Tom', {exact: true})).toBeVisible();
            await expect(courtServices.page.getByText('Needed child entertainer for the baby Julie', {exact: true})).toBeVisible();


            const accessibilityScanResults = await makeAxeBuilder()
                // Automatically uses the shared AxeBuilder configuration,
                // but supports additional test-specific configuration too
                .analyze();

            await testInfo.attach('accessibility-scan-results', {
                body: JSON.stringify(accessibilityScanResults, null, 2),
                contentType: 'application/json'
            });

            expect(accessibilityScanResults.violations).toEqual([]);

        });

    test('CTSC user update court service',
        async ({ signInPage, courtServices}) => {
            let casename = 'Amend Court service  ' + dateTime.slice(0, 10);
            console.log('caseNumber' + caseNumber);

            await updateCase(casename, caseNumber, caseWithCourtService)

            // 1. Sign in as local-authority user
            await signInPage.visit();
            await signInPage.login(
                CTSCUser.email,
                CTSCUser.password,
            );
            //sign in page
            await signInPage.isSignedIn();
            await signInPage.navigateTOCaseDetails(caseNumber);
            await courtServices.gotoNextStep('Court services');
            await courtServices.updateCourtServices();

            await courtServices.tabNavigation('Legal basis');
            await expect(courtServices.page.locator('#case-viewer-field-read--hearingPreferences').getByText('Court services', {exact: true})).toBeVisible();
            await expect(courtServices.page.getByRole('cell', {name: 'Choose which court services you need to be considered before first hearing'})).toBeVisible();
            await expect(courtServices.page.getByRole('cell', {name: 'Interpreter', exact: true})).toBeVisible();
            await expect(courtServices.page.getByRole('cell', {name: 'Intermediary', exact: true})).toBeVisible();
            await expect(courtServices.page.getByRole('cell', {
                name: 'Facilities or assistance for a disability',
                exact: true
            })).toBeVisible();
            await expect(courtServices.page.getByRole('cell', {
                name: 'Separate waiting rooms',
                exact: true
            })).toBeHidden();
            await expect(courtServices.page.getByRole('cell', {name: 'Something else', exact: true})).toBeHidden();
            await expect(courtServices.page.getByText('Needed new intrepreter for welsh language', {exact: true})).toBeVisible();


        });


});

