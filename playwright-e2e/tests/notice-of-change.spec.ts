import {test} from '../fixtures/create-fixture';
import {createRequire} from 'node:module';

const require = createRequire(import.meta.url);
import {
    newSwanseaLocalAuthorityUserOne,
    FPLSolicitorOrgUser, privateSolicitorOrgUser, FPLPrivateSolicitorOrgUser
} from '../settings/user-credentials';
import {expect} from "@playwright/test";
import thirdPartyCaseData from '../caseData/thirdPartyApplication.json' assert {type: "json"};
import LAc110WithRespondent from '../caseData/LAC110AApplication.json' assert {type: "json"};

import {createCase, giveAccessToCase, updateCase} from "../utils/api-helper";

test.describe('Notice Of Change', () => {
    const dateTime = new Date().toISOString();
    let caseNumber: string;
    let caseName: string;


    test('Notice of Change - Respondent Solicitor c110A application',
        async ({page, signInPage, noticeOfChange}) => {
            let hypenCase: string;
            caseNumber = await createCase('e2e case', newSwanseaLocalAuthorityUserOne);
            caseName = 'NoC of Respondent Solicitor of C110a Application ' + dateTime.slice(0, 10);
            await updateCase(caseName, caseNumber, LAc110WithRespondent);
            await giveAccessToCase(caseNumber, privateSolicitorOrgUser, '[SOLICITORB]');

            await signInPage.visit();
            await signInPage.login(FPLSolicitorOrgUser.email, FPLSolicitorOrgUser.password);

            await noticeOfChange.clickNoticeOfChange();
            await noticeOfChange.clickContinue();
            await noticeOfChange.enterCaseNumber(caseNumber);
            await noticeOfChange.clickContinue();
            await noticeOfChange.enterClientDetails('Thierry', 'Jordan');
            await noticeOfChange.clickContinue();
            await noticeOfChange.confirmDetails();
            await noticeOfChange.clickSubmit();
            await noticeOfChange.accessTheCase();
            hypenCase = noticeOfChange.hypenateCaseNumber(caseNumber);

            //assert
            await expect(page.getByText(hypenCase)).toBeVisible();

            await noticeOfChange.tabNavigation('People in the case');
            await expect(page.getByRole('row', {
                name: 'Do they have legal representation? Yes',
                exact: true
            }).locator('div').nth(1)).toBeVisible();
            await expect(noticeOfChange.page.getByRole('cell', {
                name: 'Organisation Name: FPLSolicitorOrg Address: Barnet County Court, St. Marys Court Regents Park Road London United Kingdom N3 1BQ',
                exact: true
            })).toBeVisible();

            await noticeOfChange.tabNavigation('Change of representatives');
            await expect(noticeOfChange.page.getByRole('cell', {
                name: 'Removed representative First name respondent Last name samy Email email@eamil.com Organisation Name: Private solicitors Address: Flat 1 Private Solicitors Apartments London CR0 2GE',
                exact: true
            })).toBeVisible();
            await expect(noticeOfChange.page.getByRole('cell', {
                name: 'Added representative First name Mando Last name John Email fpl_sol_org_01@mailinator.com Organisation Name: FPLSolicitorOrg Address: Barnet County Court, St. Marys Court Regents Park Road London United Kingdom N3 1BQ',
                exact: true
            })).toBeVisible();


        });

    test('Notice of Change - Applicant Solicitor 3rd Party c110A application',
        async ({page, signInPage, noticeOfChange}) => {
            let hypenCase: string;
            caseNumber = await createCase('e2e case', privateSolicitorOrgUser);
            caseName = 'Noc Of Applicant Solicitor of 3rd Party C110a Application ' + dateTime.slice(0, 10);
            await updateCase(caseName, caseNumber, thirdPartyCaseData);
            await giveAccessToCase(caseNumber, privateSolicitorOrgUser, '[APPSOLICITOR]');

            await signInPage.visit();
            await signInPage.login(FPLSolicitorOrgUser.email, FPLSolicitorOrgUser.password);
            await noticeOfChange.clickNoticeOfChange();
            await noticeOfChange.clickContinue();
            await noticeOfChange.enterCaseNumber(caseNumber);
            await noticeOfChange.clickContinue();
            await noticeOfChange.enterClientDetails('John', 'Somuy');
            await noticeOfChange.clickContinue();
            await noticeOfChange.confirmDetails();
            await noticeOfChange.clickSubmit();
            await noticeOfChange.accessTheCase();
            hypenCase = noticeOfChange.hypenateCaseNumber(caseNumber);

            //assert
            await expect(page.getByText(hypenCase)).toBeVisible();

        })
    test('Notice of Change - Respondent Solicitor 3rd Party c110A application',
        async ({page, signInPage, noticeOfChange}) => {
            let hypenCase: string;
            caseNumber = await createCase('e2e case', privateSolicitorOrgUser);
            caseName = 'Noc Of Respondent Solicitor of 3rd Party C110a Application ' + dateTime.slice(0, 10);
            await updateCase(caseName, caseNumber, thirdPartyCaseData);
            await giveAccessToCase(caseNumber, privateSolicitorOrgUser, '[APPSOLICITOR]');
            await giveAccessToCase(caseNumber, FPLSolicitorOrgUser, '[SOLICITORB]');

            await signInPage.visit();
            await signInPage.login(FPLPrivateSolicitorOrgUser.email, FPLPrivateSolicitorOrgUser.password);

            await noticeOfChange.clickNoticeOfChange();
            await noticeOfChange.clickContinue();
            await noticeOfChange.enterCaseNumber(caseNumber);
            await noticeOfChange.clickContinue();
            await noticeOfChange.enterClientDetails('Dianah', 'Asa');
            await noticeOfChange.clickContinue();
            await noticeOfChange.confirmDetails();
            await noticeOfChange.clickSubmit();
            await noticeOfChange.accessTheCase();
            hypenCase = noticeOfChange.hypenateCaseNumber(caseNumber);

            //assert
            await expect(page.getByText(hypenCase)).toBeVisible();

            await noticeOfChange.tabNavigation('People in the case');
            await expect(noticeOfChange.page.getByRole('row', {
                name: 'Do they have legal representation? Yes',
                exact: true
            }).locator('div').nth(1)).toBeVisible();
            await expect(noticeOfChange.page.getByRole('cell', {
                name: 'Representative Representative\'s first name Solicitor Representative\'s last name John Email address FPLPrivateSolicitorOne@mailinator.com Organisation Name: FPLPrivateSolicitor Address: Barnet County Court, St. Marys Court Regents Park Road vxccvvbx London United Kingdom N3 1BQ',
                exact: true
            })).toBeVisible();
            await noticeOfChange.tabNavigation('Change of representatives');
            await expect(page.getByRole('cell', {name: 'Notice of change', exact: true})).toBeVisible();
            await expect(noticeOfChange.page.getByRole('cell', {
                name: 'Removed representative First name respondent 1 Last name Solicitor Email email@email.com Organisation Name: FPLSolicitorOrg Address: Barnet County Court, St. Marys Court Regents Park Road London United Kingdom N3 1BQ',
                exact: true
            })).toBeVisible();
            await expect(noticeOfChange.page.getByRole('cell', {
                name: 'Added representative First name Solicitor Last name John Email FPLPrivateSolicitorOne@mailinator.com Organisation Name: FPLPrivateSolicitor Address: Barnet County Court, St. Marys Court Regents Park Road vxccvvbx London United Kingdom N3 1BQ',
                exact: true
            })).toBeVisible();
        })

})
