import {expect, test} from "../fixtures/fixtures";
import {newSwanseaLocalAuthorityUserOne} from "../settings/user-credentials";
import {createCase} from "../utils/api-helper";

test.describe('Non mandatory application details before application submit', () => {
    const dateTime = new Date().toISOString();
    let caseNumber: string;
    let casename: string;

    test('LA add risk and harm to children @xbrowser',
        async ({startApplication, signInPage, riskAndHarmToChildren, makeAxeBuilder}, testInfo) => {

            casename = 'Risk and harm  ' + dateTime.slice(0, 10);
            caseNumber = await createCase(casename, newSwanseaLocalAuthorityUserOne);
            // 1. Sign in as local-authority user
            await signInPage.visit();
            await signInPage.login(
                newSwanseaLocalAuthorityUserOne.email,
                newSwanseaLocalAuthorityUserOne.password,
            );
            //sign in page
            await signInPage.isSignedIn();
            await signInPage.navigateTOCaseDetails(caseNumber);
            // Risk and harm to children
            await riskAndHarmToChildren.gotoNextStep('Risk and harm to children');
            await riskAndHarmToChildren.riskAndHarmToChildrenSmokeTest();

            await riskAndHarmToChildren.tabNavigation('View application');
            await expect(riskAndHarmToChildren.page.getByText('Physical harm including non-')).toBeVisible();
            await expect(riskAndHarmToChildren.page.getByText('Emotional harm')).toBeVisible();
            await expect(riskAndHarmToChildren.page.getByText('Sexual abuse')).toBeVisible();
            await expect(riskAndHarmToChildren.page.getByRole('cell', { name: 'Neglect', exact: true })).toBeVisible();
            await expect(riskAndHarmToChildren.page.getByText('Alcohol or drug abuse')).toBeVisible();
            await expect(riskAndHarmToChildren.page.getByText('Domestic abuse')).toBeVisible();


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

    test('LA add welsh language requirement @xbrowser',
        async ({startApplication, signInPage, welshLangRequirements, makeAxeBuilder}, testInfo) => {

            casename = 'Welsh language requirement  ' + dateTime.slice(0, 10);
            caseNumber = await createCase(casename, newSwanseaLocalAuthorityUserOne);
            // 1. Sign in as local-authority user
            await signInPage.visit();
            await signInPage.login(
                newSwanseaLocalAuthorityUserOne.email,
                newSwanseaLocalAuthorityUserOne.password,
            );
            //sign in page
            await signInPage.isSignedIn();
            await signInPage.navigateTOCaseDetails(caseNumber);

            // Welsh language requirements
            await welshLangRequirements.gotoNextStep('Welsh language requirements');
            await welshLangRequirements.welshLanguageSmokeTest();
            await startApplication.welshLanguageReqUpdated();
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


    test('LA add international element @xbrowser',
        async ({startApplication, signInPage, internationalElement, makeAxeBuilder}, testInfo) =>  {
            casename = 'International element  ' + dateTime.slice(0, 10);
            caseNumber = await createCase(casename, newSwanseaLocalAuthorityUserOne);
            // 1. Sign in as local-authority user
            await signInPage.visit();
            await signInPage.login(
                newSwanseaLocalAuthorityUserOne.email,
                newSwanseaLocalAuthorityUserOne.password,
            );
            //sign in page
            await signInPage.isSignedIn();
            await signInPage.navigateTOCaseDetails(caseNumber)

            // International element
           await internationalElement.gotoNextStep('International element');
            await internationalElement.internationalElementSmokeTest();
            //assert
            await internationalElement.tabNavigation('View application')
            await expect(internationalElement.page.locator('#case-viewer-field-read--internationalElement').getByText('International element', { exact: true })).toBeVisible();
            await expect(internationalElement.page.locator('ccd-read-complex-field-table ccd-field-read-label').filter({ hasText: 'Spain Itlay France' }).locator('div')).toBeVisible();
            await expect(internationalElement.page.getByText('Convention Care order by the')).toBeVisible();

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

    test('LA add c1 application',
        async ({startApplication, signInPage, c1WithSupplement, makeAxeBuilder}, testInfo) => {
            casename = 'c1 application  ' + dateTime.slice(0, 10);
            caseNumber = await createCase(casename, newSwanseaLocalAuthorityUserOne);
            // 1. Sign in as local-authority user
            await signInPage.visit();
            await signInPage.login(
                newSwanseaLocalAuthorityUserOne.email,
                newSwanseaLocalAuthorityUserOne.password,
            );
            //sign in page
            await signInPage.isSignedIn();
            await signInPage.navigateTOCaseDetails(caseNumber);

            // C1 With Supplement
            await c1WithSupplement.c1WithSupplementSmokeTest();
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

    test('LA add other people @xbrowser',
        async ({startApplication, signInPage, otherPeopleInCase, makeAxeBuilder}, testInfo) => {
            casename = 'Other people in case ' + dateTime.slice(0, 10);
            caseNumber = await createCase(casename, newSwanseaLocalAuthorityUserOne);
            // 1. Sign in as local-authority user
            await signInPage.visit();
            await signInPage.login(
                newSwanseaLocalAuthorityUserOne.email,
                newSwanseaLocalAuthorityUserOne.password,
            );
            //sign in page
            await signInPage.isSignedIn();
            await signInPage.navigateTOCaseDetails(caseNumber);
            //add other people in the case
            await otherPeopleInCase.gotoNextStep('Other people in the case');
            await otherPeopleInCase.addOtherPerson();
            await expect( otherPeopleInCase.page.getByRole('paragraph').filter({ hasText: 'Other people in the case' }).getByRole('img', { name: 'In progress' })).toBeVisible();

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
});
