import {test} from '../fixtures/create-fixture';
import {CTSCUser, newSwanseaLocalAuthorityUserOne,judgeUser} from "../settings/user-credentials";
import caseData from '../caseData/caseWithHearingDetails.json' assert {type: 'json'};
import caseWithOrderData from '../caseData/caseWithAllTypesOfOrders.json' assert  {type:'json'};
import {expect} from "@playwright/test";
import {createCase, updateCase} from "../utils/api-helper";
import {userInfo} from "node:os";

test.describe('manage orders', () => {
    const dateTime = new Date().toISOString();
    let caseNumber: string;
    let caseName: string;
    test.beforeEach(async () => {
        caseNumber = await createCase('e2e case', newSwanseaLocalAuthorityUserOne);
    });
    [{user: CTSCUser, role: 'CTSC', EPOtype: 'Remove to accommodation'},
     {user: judgeUser, role: 'Legal', EPOtype: 'Prevent removal from an address'}].forEach(({user,role,EPOtype})=>{
        test(` EPO order created by ${role}`,
            async ({page, signInPage, orders}) => {
                caseName = 'EPO order by ' + role + ' ' + dateTime.slice(0, 10);
                await updateCase(caseName, caseNumber, caseData);
                await signInPage.visit();
                await signInPage.login(user.email, user.password);
                await signInPage.navigateTOCaseDetails(caseNumber);

                await orders.gotoNextStep('Manage orders');
                await orders.selectOrderOperation('Create an order');
                await orders.clickContinue();
                await orders.selectOrder('Emergency protection order (C23)');
                await orders.clickContinue();

                await expect(page.getByText(' Add issuing details', {exact: true})).toBeVisible();
                await orders.addIssuingDetails();
                await orders.clickContinue();

                //add children involved
                await expect(page.getByRole('heading', {name: 'Add children\'s details', exact: true})).toBeVisible();
                await orders.addChildDetails();
                await orders.clickContinue();

                //add order details
                await orders.addEPOOrderDetails(EPOtype);
                await orders.clickContinue();

                // preview order generated
                await expect(page.getByRole('heading', {name: 'Check your order', exact: true})).toBeVisible();
                await orders.openOrderDoc();
                await expect(orders.orderPage.getByText('Timothy Jones', {exact: true})).toBeVisible();
                await expect(orders.orderPage.getByText('John Black', {exact: true})).toBeVisible();
                await expect(orders.orderPage.getByText('Sarah Black', {exact: true})).toBeVisible();
                await expect(orders.orderPage.getByText('This order ends on 2 October 2013 at 10:00am.', {exact: true})).toBeVisible();
                await orders.clickContinue();

                //check your answer and submit
                await orders.checkYourAnsAndSubmit();

                await orders.tabNavigation('Orders')
                await expect(page.getByRole('cell', {name: 'Emergency protection order (C23)', exact: true})).toBeVisible();
                await expect(page.getByText('Timothy Jones, John Black, Sarah Black', {exact: true})).toBeVisible();

            });
    })

    test('@local Amend order under slip rule',async({signInPage,orders})=>{
        caseName = 'EPO order by '  + dateTime.slice(0, 10);
        await updateCase(caseName, caseNumber, caseWithOrderData);
        await signInPage.visit();
        await signInPage.login(CTSCUser.email, CTSCUser.password);
        await signInPage.navigateTOCaseDetails(caseNumber);
        await orders.gotoNextStep('Manage orders');
        await orders.selectOrderOperation('Amend order under the slip');
        await orders.orderToAmend.selectOption('C23 - Emergency protection order - 7 July 2021');
        await orders.clickContinue();
        await expect(orders.page.getByRole('heading', { name: 'Download order' })).toBeVisible();
        await expect(orders.page.getByText('Open the attached order in PDF-Xchange Editor to make changes.',{exact:true})).toBeVisible();
        await expect(orders.page.getByRole('link', { name: 'C23 - Emergency protection order - 7 July 2021' })).toBeVisible();
        await orders.clickContinue();
        await expect(orders.page.getByRole('heading', { name: 'Replace old order' })).toBeVisible();
        await orders.uploadAmendedOrder();
        await orders.clickContinue();
        await orders.checkYourAnsAndSubmit();

        await orders.tabNavigation('Orders');
        await expect(orders.page.getByText('Amended', { exact: true })).toBeVisible();

        await signInPage.page.pause();




    })


});
