import {expect, test} from "../fixtures/fixtures";
import {
    privateSolicitorOrgUser, privateSolicitorOrgUserTwo,
    swanseaOrgCAAUser,
    wiltshireCountyUserOne,
    wiltshireCountyUserTwo,
    WiltshireOrgCAAUser
} from "../settings/user-credentials";

test.describe('Case outsourced another Localauthority or Solicitor Organisation', () => {
    const dateTime = new Date().toISOString();
    test(' Managing LA creates case for their own organisation',
        async ({page, signInPage, createCase, organisation}) => {
            await signInPage.visit();
            await signInPage.login(wiltshireCountyUserOne.email, wiltshireCountyUserOne.password,);
            let caseName = 'ManangingLA create case' + dateTime.slice(0, 10);
            await createCase.createCase();
            await createCase.selectLA('Wiltshire Council');
            await createCase.clickContinue();
            await createCase.fillcaseName(caseName);
            await createCase.submitOutSourceCase();
            await createCase.getCaseNumber();

            //Other Users of managing LA have access to case
            await createCase.clickSignOut();
            await signInPage.login(wiltshireCountyUserTwo.email, wiltshireCountyUserTwo.password);
            await signInPage.navigateTOCaseDetails(createCase.casenumber);
            await expect(page.getByRole('heading', {name: `${caseName}`})).toBeVisible();

            //Login into manage Org and verify the CAA have access to the case created
            await signInPage.visit(signInPage.mourl);
            await signInPage.login(WiltshireOrgCAAUser.email, WiltshireOrgCAAUser.password);
            await signInPage.isLoggedInMO();
            await organisation.searchUnassignedCase(createCase.casenumber, caseName);

        })

    test(' Managing LA submit application on behalf of outsourced LA',
        async ({page, signInPage, createCase, organisation}) => {
            await signInPage.visit();
            await signInPage.login(wiltshireCountyUserOne.email, wiltshireCountyUserOne.password,);
            let caseName = 'ManangingLA create case for Swansea' + dateTime.slice(0, 10);
            await createCase.createCase();
            await createCase.selectLA('Swansea City Council');
            await createCase.clickContinue();
            await createCase.fillcaseName(caseName);
            await createCase.submitOutSourceCase();
            await createCase.shareWithOrganisationUser('No');
            await createCase.submitOutSourceCase();
            await createCase.getCaseNumber();

            //Other Users of managing LA have access to case
            await createCase.clickSignOut();
            await signInPage.login(wiltshireCountyUserTwo.email, wiltshireCountyUserTwo.password);
            await signInPage.navigateTOCaseDetails(createCase.casenumber);
            await expect(page.getByRole('heading', {name: `${caseName}`})).toBeHidden();

            //Login into manage Org and verify the outsourced lA CAA have access to the case created
            await signInPage.visit(signInPage.mourl);
            await signInPage.login(swanseaOrgCAAUser.email, swanseaOrgCAAUser.password);
            await signInPage.isLoggedInMO();
            await organisation.searchUnassignedCase(createCase.casenumber, caseName);

        })

    test('@local ManagingLA share case within its organisation',
        async ({page, createCase, signInPage, organisation}) => {
         await signInPage.visit();
         await signInPage.login(wiltshireCountyUserOne.email, wiltshireCountyUserOne.password,);
         let caseName = 'ManangingLA share case within Org' + dateTime.slice(0, 10)
        await createCase.createCase();
        await createCase.selectLA('Swansea City Council');
        await createCase.clickContinue();
        await createCase.fillcaseName(caseName);
        await createCase.submitOutSourceCase();
        await createCase.shareWithOrganisationUser('Yes');
        await createCase.submitOutSourceCase();
        await createCase.getCaseNumber();

        //Other Users of managing LA have access to case
        await createCase.clickSignOut();
        await signInPage.login(wiltshireCountyUserTwo.email, wiltshireCountyUserTwo.password);
        await signInPage.navigateTOCaseDetails(createCase.casenumber);
        await expect(page.getByRole('heading', {name: `${caseName}`})).toBeVisible();

        //Login into manage Org and verify the CAA have access to the case created
        await signInPage.visit(signInPage.mourl);
        await signInPage.login(swanseaOrgCAAUser.email, swanseaOrgCAAUser.password);
        await signInPage.isLoggedInMO();
        await organisation.searchUnassignedCase(createCase.casenumber, caseName);

    })

    test('Managing LA share case with a  one user within Org',
        async ({page, signInPage, createCase, organisation, shareCase}) =>
        {
        await signInPage.visit();
        await signInPage.login(wiltshireCountyUserOne.email, wiltshireCountyUserOne.password);
        let caseName = 'ManangingLA share case within Org' + dateTime.slice(0, 10);
        await createCase.createCase();
        await createCase.selectLA('Swansea City Council');
        await createCase.clickContinue();
        await createCase.fillcaseName(caseName);
        await createCase.submitOutSourceCase();
        await createCase.shareWithOrganisationUser('No');
        await createCase.submitOutSourceCase();
        await createCase.getCaseNumber();
        await createCase.findCase(createCase.casenumber);
       // await page.pause();
        await shareCase.shareCaseWithinOrg(wiltshireCountyUserTwo.email);
        await createCase.clickSignOut();

        //login in as shared user and access the case
        await signInPage.login(wiltshireCountyUserTwo.email,wiltshireCountyUserTwo.password);
        await signInPage.navigateTOCaseDetails(createCase.casenumber);
        await expect(page.getByRole('heading', {name: `${caseName}`})).toBeVisible();


    })

    test('@local EPS create case for LA',
        async ({page,signInPage,createCase,organisation}) =>
        {
            await signInPage.visit();
            await signInPage.login(privateSolicitorOrgUser.email, privateSolicitorOrgUser.password);
            let caseName = 'EPS create case for LA' + dateTime.slice(0, 10);
            await createCase.createCase();
            await createCase.selectRepresentLA();
            await createCase.selectLA('Swansea City Council');
            await createCase.clickContinue();
            await createCase.fillcaseName(caseName);
            await createCase.submitOutSourceCase();
            await createCase.getCaseNumber();
            await expect(page.getByRole('heading', { name: `${caseName}` })).toBeVisible();
            await createCase.clickSignOut();

            //other org user doesnot have access to case
            await signInPage.login(privateSolicitorOrgUserTwo.email,privateSolicitorOrgUserTwo.password);
            await signInPage.navigateTOCaseDetails(createCase.casenumber);
            await expect(page.getByRole('heading', {name: `${caseName}`})).toBeHidden();

            //Login into manage Org and verify the CAA have access to the case created
            await signInPage.visit(signInPage.mourl);
            await signInPage.login(swanseaOrgCAAUser.email, swanseaOrgCAAUser.password);
            await signInPage.isLoggedInMO();
            await organisation.searchUnassignedCase(createCase.casenumber, caseName);
        })
})





