import {expect, test} from "../fixtures/fixtures";
import {newSwanseaLocalAuthorityUserOne} from "../settings/user-credentials";
import {createCase} from "../utils/api-helper";
import {LAUserPage} from "../pages/local-authority-user-browser.ts";

test.describe('Non mandatory application details before application submit', () => {
    const dateTime = new Date().toISOString();
    let caseNumber: string;
    let casename: string;

    test('LA add risk and harm to children',
        async ({startApplication,localAuthorityUser, riskAndHarmToChildren, makeAxeBuilder}, testInfo) => {

            casename = 'Risk and harm  ' + dateTime.slice(0, 10);
            caseNumber = await createCase(casename, newSwanseaLocalAuthorityUserOne);
            // 1. Sign in as local-authority user
            // await signInPage.visit();
            // await signInPage.login(
            //     newSwanseaLocalAuthorityUserOne.email,
            //     newSwanseaLocalAuthorityUserOne.password,
            // );
            // //sign in page
            // await signInPage.isSignedIn();
            await riskAndHarmToChildren.switchUser(localAuthorityUser.page);
            await riskAndHarmToChildren.navigateTOCaseDetails(caseNumber);
            // Risk and harm to children
            await startApplication.riskAndHarmToChildren();
            await riskAndHarmToChildren.riskAndHarmToChildrenSmokeTest();
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

    test('LA add factors affecting parenting details',
        async ({startApplication, signInPage, factorsAffectingParenting, makeAxeBuilder}, testInfo) => {
            casename = 'Factor affecting parenting  ' + dateTime.slice(0, 10);
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

            // Factors affecting parenting
            await factorsAffectingParenting.addFactorsAffectingParenting();
            await startApplication.addApplicationDetailsHeading.isVisible();
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

    test('LA add welsh language requirement',
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
            await startApplication.welshLanguageReq();
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


    test('LA add international element',
        async ({startApplication, signInPage, internationalElement, makeAxeBuilder}, testInfo) => {
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
            await startApplication.internationalElementReqUpdated();
            await internationalElement.internationalElementSmokeTest();
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
    test('LA add court service',
        async ({startApplication, signInPage, courtServicesNeeded, makeAxeBuilder}, testInfo) => {
            casename = 'Court service  ' + dateTime.slice(0, 10);
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

            // Court Services Needed
            await startApplication.courtServicesNeededReqUpdated();
            await courtServicesNeeded.CourtServicesSmoketest();
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

    test('@LA add other people',
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
            await startApplication.addOtherPeopleInCase()
            await otherPeopleInCase.personOneToBeGivenNotice();
            await otherPeopleInCase.personTwoToBeGivenNotice();
            await otherPeopleInCase.continueAndCheck();

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
