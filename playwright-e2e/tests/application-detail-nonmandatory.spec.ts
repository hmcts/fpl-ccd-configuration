import {test} from "../fixtures/fixtures";
import {newSwanseaLocalAuthorityUserOne} from "../settings/user-credentials";
import {createCase} from "../utils/api-helper";

test.describe('Non mandatory application details before application submit @sessionreuse', () => {
    const dateTime = new Date().toISOString();
    let caseNumber: string;
    let casename: string;

    test('LA add risk and harm to children',
        async ({startApplication, localAuthorityUser, riskAndHarmToChildren, makeAxeBuilder}, testInfo) => {

            casename = 'Risk and harm  ' + dateTime.slice(0, 10);
            caseNumber = await createCase(casename, newSwanseaLocalAuthorityUserOne);
            await riskAndHarmToChildren.switchUser(localAuthorityUser.page);
            await startApplication.switchUser(localAuthorityUser.page);
            await riskAndHarmToChildren.navigateTOCaseDetails(caseNumber);
            // Risk and harm to children
            await startApplication.riskAndHarmToChildren();
            await riskAndHarmToChildren.riskAndHarmToChildrenSmokeTest();

        });

    test('LA add factors affecting parenting details',
        async ({startApplication, localAuthorityUser, factorsAffectingParenting, makeAxeBuilder}, testInfo) => {
            casename = 'Factor affecting parenting  ' + dateTime.slice(0, 10);
            caseNumber = await createCase(casename, newSwanseaLocalAuthorityUserOne);

            await factorsAffectingParenting.switchUser(localAuthorityUser.page);
            await startApplication.switchUser(localAuthorityUser.page);
            await factorsAffectingParenting.navigateTOCaseDetails(caseNumber);

            // Factors affecting parenting
            await factorsAffectingParenting.addFactorsAffectingParenting();
            await startApplication.addApplicationDetailsHeading.isVisible();

        });

    test('LA add welsh language requirement',
        async ({startApplication, welshLangRequirements, localAuthorityUser}, testInfo) => {

            casename = 'Welsh language requirement  ' + dateTime.slice(0, 10);
            caseNumber = await createCase(casename, newSwanseaLocalAuthorityUserOne);
            // 1. Sign in as local-authority user
            await welshLangRequirements.switchUser(localAuthorityUser.page);
            await startApplication.switchUser(localAuthorityUser.page);
            await welshLangRequirements.navigateTOCaseDetails(caseNumber);

            // Welsh language requirements
            await startApplication.welshLanguageReq();
            await welshLangRequirements.welshLanguageSmokeTest();
            await startApplication.welshLanguageReqUpdated();
        });


    test('LA add international element',
        async ({startApplication, localAuthorityUser, internationalElement, makeAxeBuilder}, testInfo) => {
            casename = 'International element  ' + dateTime.slice(0, 10);
            caseNumber = await createCase(casename, newSwanseaLocalAuthorityUserOne);
            // 1. Sign in as local-authority user
            await internationalElement.switchUser(localAuthorityUser.page);
            await startApplication.switchUser(localAuthorityUser.page);
            await internationalElement.navigateTOCaseDetails(caseNumber)

            // International element
            await startApplication.internationalElementReqUpdated();
            await internationalElement.internationalElementSmokeTest();
        });

    test('LA add court service',
        async ({startApplication, localAuthorityUser, courtServicesNeeded, makeAxeBuilder}, testInfo) => {
            casename = 'Court service  ' + dateTime.slice(0, 10);
            caseNumber = await createCase(casename, newSwanseaLocalAuthorityUserOne);

            await courtServicesNeeded.switchUser(localAuthorityUser.page);
            await startApplication.switchUser(localAuthorityUser.page)
            await courtServicesNeeded.navigateTOCaseDetails(caseNumber);

            // Court Services Needed
            await startApplication.courtServicesNeededReqUpdated();
            await courtServicesNeeded.CourtServicesSmoketest();

        });


    test('LA add c1 application',
        async ({startApplication, localAuthorityUser, c1WithSupplement, makeAxeBuilder}, testInfo) => {
            casename = 'c1 application  ' + dateTime.slice(0, 10);
            caseNumber = await createCase(casename, newSwanseaLocalAuthorityUserOne);

            await c1WithSupplement.switchUser(localAuthorityUser.page);
            await c1WithSupplement.navigateTOCaseDetails(caseNumber);

            // C1 With Supplement
            await c1WithSupplement.c1WithSupplementSmokeTest();

        });

    test('@LA add other people',
        async ({startApplication, localAuthorityUser, otherPeopleInCase, makeAxeBuilder}, testInfo) => {
            casename = 'Other people in case ' + dateTime.slice(0, 10);
            caseNumber = await createCase(casename, newSwanseaLocalAuthorityUserOne);

            await otherPeopleInCase.switchUser(localAuthorityUser.page);
            await startApplication.switchUser(localAuthorityUser.page);
            await otherPeopleInCase.navigateTOCaseDetails(caseNumber);
//add other people in the case
            await startApplication.addOtherPeopleInCase()
            await otherPeopleInCase.personOneToBeGivenNotice();
            await otherPeopleInCase.personTwoToBeGivenNotice();
            await otherPeopleInCase.continueAndCheck();

        });

});
