import { test } from '../fixtures/create-fixture';
import { CTSCUser, judgeUser, newSwanseaLocalAuthorityUserOne } from "../settings/user-credentials";
import caseData from '../caseData/caseWithHearingDetails.json' assert {type: 'json'};
import caseWithOrderData from '../caseData/caseWithAllTypesOfOrders.json' assert {type: 'json'};
import { expect } from "@playwright/test";
import { createCase, updateCase } from "../utils/api-helper";

test.describe('manage orders', () => {
    let dateTime = new Date().toISOString();
    new Date().toTimeString()
    let caseNumber: string;
    let caseName: string;
    test.beforeEach(async () => {
        caseNumber = await createCase('e2e case', newSwanseaLocalAuthorityUserOne);
    });
    [{ user: CTSCUser, role: 'CTSC', EPOtype: 'Remove to accommodation' },
    { user: judgeUser, role: 'Legal', EPOtype: 'Prevent removal from an address' }].
        forEach(({ user, role, EPOtype }) => {
            test(` @xbrowser EPO order created by ${role}`,
                async ({ page, signInPage, orders }) => {
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

                    await expect(page.getByText(' Add issuing details', { exact: true })).toBeVisible();
                    await orders.addIssuingDetailsOfApprovedOrder('No');
                    await orders.clickContinue();

                    //add children involved
                    await expect(page.getByRole('heading', { name: 'Add children\'s details', exact: true })).toBeVisible();
                    await orders.addChildDetails('No');
                    await orders.clickContinue();

                    //add order details
                    await orders.addEPOOrderDetails(EPOtype);
                    await orders.clickContinue();

                    // preview order generated
                    await expect(page.getByRole('heading', { name: 'Check your order', exact: true })).toBeVisible();
                    await orders.openOrderDoc('Preview order.pdf');
                    await expect(orders.orderPage.getByText('Timothy Jones', { exact: true })).toBeVisible();
                    await expect(orders.orderPage.getByText('John Black', { exact: true })).toBeVisible();
                    await expect(orders.orderPage.getByText('Sarah Black', { exact: true })).toBeVisible();
                    await expect(orders.orderPage.getByText('This order ends on 2 October 2013 at 10:00am.', { exact: true })).toBeVisible();
                    await orders.clickContinue();

                    //check your answer and submit
                    await orders.checkYourAnsAndSubmit();

                    await orders.tabNavigation('Orders')
                    await expect(page.getByRole('cell', {
                        name: 'Emergency protection order (C23)',
                        exact: true
                    })).toBeVisible();
                    await expect(page.getByText('Timothy Jones, John Black, Sarah Black', { exact: true })).toBeVisible();

                });
        })

    test('Amend order under slip rule', async ({ signInPage, orders }) => {
        caseName = 'Amend EPO order ' + dateTime.slice(0, 10);
        await updateCase(caseName, caseNumber, caseWithOrderData);
        await signInPage.visit();
        await signInPage.login(CTSCUser.email, CTSCUser.password);
        await signInPage.navigateTOCaseDetails(caseNumber);
        await orders.gotoNextStep('Manage orders');

        await orders.selectOrderOperation('Amend order under the slip');
        await orders.orderToAmend.selectOption('C23 - Emergency protection order - 7 July 2021');
        await orders.clickContinue();
        await expect(orders.page.getByRole('heading', { name: 'Download order' })).toBeVisible();
        await expect(orders.page.getByText('Open the attached order in PDF-Xchange Editor to make changes.', { exact: true })).toBeVisible();
        await expect(orders.page.getByRole('link', { name: 'C23 - Emergency protection order' })).toBeVisible();
        await orders.clickContinue();

        await expect(orders.page.getByRole('heading', { name: 'Replace old order' })).toBeVisible();
        await orders.uploadAmendedOrder();
        await orders.clickContinue();
        await orders.checkYourAnsAndSubmit();

        await orders.tabNavigation('Orders');
        await expect(orders.page.getByText('Amended', { exact: true })).toBeVisible();
        await expect(orders.page.locator('#case-viewer-field-read--orderCollection')).toContainText(orders.getCurrentDate());
        await expect(orders.page.getByRole('link', { name: 'amended_C23 - Emergency' })).toBeVisible();
        await orders.openOrderDoc('amended_C23 - Emergency');
        await expect(orders.orderPage.getByText('Amended under the slip rule')).toBeVisible();
    })
    test('C32 Care Order', async ({ page, signInPage, orders }) => {
        caseName = 'C32 Care Order ' + dateTime.slice(0, 10);
        await updateCase(caseName, caseNumber, caseWithOrderData);
        await signInPage.visit();
        await signInPage.login(CTSCUser.email, CTSCUser.password);
        await signInPage.navigateTOCaseDetails(caseNumber);
        await orders.gotoNextStep('Manage orders');

        await orders.selectOrderOperation('Create an order');
        await orders.clickContinue();
        await orders.selectOrder('Care order (C32A)');
        await orders.clickContinue();

        await expect.soft(page.getByText(' Add issuing details', { exact: true })).toBeVisible();
        await orders.addIssuningDeatilsOfUnApprovedOrder();
        await orders.clickContinue();

        await orders.addChildDetails('Yes');
        await orders.clickContinue();

        await orders.addC32CareOrder();
        await orders.clickContinue();

        await expect.soft(page.getByRole('heading', { name: 'Check your order', exact: true })).toBeVisible();
        await orders.openOrderDoc('Preview order.pdf');
        await expect(orders.orderPage.getByText('It is ordered that the child')).toBeVisible();
        await expect(orders.orderPage.getByText('Care order', { exact: true })).toBeVisible();

        await orders.closeTheOrder('Yes');
        await expect(page.locator('#manageOrdersCloseCaseWarning')).toContainText('The case will remain open for 21 days to allow for appeal.');
        await orders.clickContinue();
        await orders.checkYourAnsAndSubmit();

        await orders.tabNavigation('Orders');
        await expect(page.getByText('Order 1', { exact: true })).toBeVisible();
        await expect(page.getByText('Care order (C32A)')).toBeVisible();
        await expect(page.getByRole('link', { name: 'c32a_care_order.pdf' })).toBeVisible();

        //assert the state of the case
        // await orders.tabNavigation('History'); EXUI issue with tab lables having the hint text . it has to rollback when the issue fixed
        await orders.page.getByRole('tab', { name: 'History', exact: true }).click();
        await expect(page.getByText('Closed', { exact: true })).toBeVisible();

    })

    test('C32B Discharge of Care Order', async ({ page, signInPage, orders }) => {
        caseName = 'C32B Discharge of Care Order ' + dateTime.slice(0, 10);
        await updateCase(caseName, caseNumber, caseData);
        await signInPage.visit();
        await signInPage.login(CTSCUser.email, CTSCUser.password);
        await signInPage.navigateTOCaseDetails(caseNumber);
        await orders.gotoNextStep('Manage orders');

        await orders.selectOrderOperation('Create an order');
        await orders.clickContinue();
        await orders.selectOrder('Discharge of care order (C32B)');
        await orders.clickContinue();

        await expect.soft(page.getByText(' Add issuing details', { exact: true })).toBeVisible();
        await orders.addIssuingDetailsOfApprovedOrder('No');
        await orders.clickContinue();
        await orders.addChildDetails('Yes');
        await orders.clickContinue();

        await orders.addC32BDischargeOfCareOrder();

        await orders.clickContinue();
        await expect.soft(page.getByRole('heading', { name: 'Check your order', exact: true })).toBeVisible();

        await orders.openOrderDoc('Preview order.pdf');
        await expect(orders.orderPage.getByText('Discharge of care order')).toBeVisible();
        await expect(orders.orderPage.getByText('The Court discharges the care')).toBeVisible();

        await orders.clickContinue();
        await orders.checkYourAnsAndSubmit();

        await orders.tabNavigation('Orders');
        await expect(page.locator('#case-viewer-field-read--orderCollection')).toContainText('Discharge of care order (C32B)');
        await expect(page.locator('ccd-read-document-field')).toContainText('c32b_discharge_of_care_order.pdf');
    })

    test('C47A Appointment of a children\'s guardian ', async ({ page, signInPage, orders }) => {
        caseName = 'C47A Order ' + dateTime.slice(0, 10);
        await updateCase(caseName, caseNumber, caseData);
        await signInPage.visit();
        await signInPage.login(CTSCUser.email, CTSCUser.password);
        await signInPage.navigateTOCaseDetails(caseNumber);
        await orders.gotoNextStep('Manage orders');

        await orders.selectOrderOperation('Create an order');
        await orders.clickContinue();
        await orders.selectOrder('Appointment of a children\'s guardian (C47A)');
        await orders.clickContinue();

        await expect.soft(page.getByText(' Add issuing details', { exact: true })).toBeVisible();
        await orders.addIssuingDetailsOfApprovedOrder('No');
        await orders.clickContinue();

        await orders.addC47AppointOfGuardianOrder();
        await orders.clickContinue();
        await expect.soft(page.getByRole('heading', { name: 'Check your order', exact: true })).toBeVisible();

        await orders.openOrderDoc('Preview order.pdf');
        await expect(orders.orderPage.getByText('Appointment of a children\'s')).toBeVisible();
        await expect(orders.orderPage.getByText('The Court appoints Cafcass Swansea as a children\'s guardian for the children in the')).toBeVisible();

        await orders.clickContinue();
        await orders.checkYourAnsAndSubmit();

        await orders.tabNavigation('Orders');
        await expect(page.getByRole('cell', { name: 'Appointment of a children\'s guardian (C47A)', exact: true })).toBeVisible();
        await expect(page.locator('ccd-read-document-field')).toContainText('c47a_appointment_of_a_childrens_guardian.pdf');

    })

    test('C26 Authority to keep a child in secure accommodation ', async ({ page, signInPage, orders }) => {
        caseName = 'C26 Order ' + dateTime.slice(0, 10);
        await updateCase(caseName, caseNumber, caseData);
        await signInPage.visit();
        await signInPage.login(CTSCUser.email, CTSCUser.password);
        await signInPage.navigateTOCaseDetails(caseNumber);
        await orders.gotoNextStep('Manage orders');

        await orders.selectOrderOperation('Create an order');
        await orders.clickContinue();
        await orders.selectOrder('Authority to keep a child in secure accommodation (C26)');
        await orders.clickContinue();

        await expect.soft(page.getByText(' Add issuing details', { exact: true })).toBeVisible();
        await orders.addIssuingDetailsOfApprovedOrder('Yes');
        await orders.clickContinue();

        await expect.soft(page.getByRole('heading', { name: 'Add child\'s details' })).toBeVisible();
        await orders.selectChildInvolved();
        await orders.clickContinue();

        await expect.soft(page.getByText('Authority to keep a child in secure accommodation (C26)')).toBeVisible();
        await orders.addC26SecureAccomadation();
        await orders.clickContinue();

        await expect.soft(page.getByRole('heading', { name: 'Check your order', exact: true })).toBeVisible();
        await orders.openOrderDoc('Preview order.pdf');
        await expect(orders.orderPage.getByText('Authority to keep a child in')).toBeVisible();
        await expect(orders.orderPage.getByText('The Court authorises Swansea')).toBeVisible();

        await orders.clickContinue();
        await orders.checkYourAnsAndSubmit();
        await orders.tabNavigation('Orders');
        await expect(page.getByText('Authority to keep a child in')).toBeVisible();
        await expect(page.getByRole('link', { name: 'c26_secure_accommodation_order.pdf' })).toBeVisible();

    })

    test('C36 Child assessment order ', async ({ page, signInPage, orders }) => {
        caseName = 'C26 Order ' + dateTime.slice(0, 10);
        await updateCase(caseName, caseNumber, caseData);
        await signInPage.visit();
        await signInPage.login(CTSCUser.email, CTSCUser.password);
        await signInPage.navigateTOCaseDetails(caseNumber);

        await orders.gotoNextStep('Manage orders');
        await orders.selectOrderOperation('Create an order');
        await orders.clickContinue();

        await orders.selectOrder('Child assessment order (C39)');
        await orders.clickContinue();

        await orders.addIssuingJudgeDetails('Yes');
        await orders.clickContinue();

        await orders.selectChildInvolved();
        await orders.clickContinue();

        await orders.addC39childAssessment();
        await orders.clickContinue();

        await expect.soft(page.getByRole('heading', { name: 'Check your order', exact: true })).toBeVisible();
        await orders.openOrderDoc('Preview order.pdf');
        await expect(orders.orderPage.getByText('Child assessment order')).toBeVisible();
        await orders.clickContinue();
        await orders.checkYourAnsAndSubmit();

        await orders.tabNavigation('Orders');
        await expect(page.getByText('Child assessment order (C39)')).toBeVisible();
        await expect(page.getByRole('link', { name: 'c39_child_assessment_order.pdf' })).toBeVisible();

    })

    test('C21 blank order ', async ({ page, signInPage, orders }) => {
        caseName = 'C21 blank Order ' + dateTime.slice(0, 10);
        await updateCase(caseName, caseNumber, caseData);
        await signInPage.visit();
        await signInPage.login(CTSCUser.email, CTSCUser.password);
        await signInPage.navigateTOCaseDetails(caseNumber);

        await orders.gotoNextStep('Manage orders');
        await orders.selectOrderOperation('Create an order');
        await orders.clickContinue();

        await orders.selectOrder('Blank order (C21)');
        await orders.clickContinue();

        await orders.addIssuingDetailsOfApprovedOrder('Yes');
        await orders.clickContinue();

        await orders.addChildDetails('Yes');
        await orders.clickContinue();

        await orders.addC21BlankOrderDetails();
        await orders.clickContinue();

        await expect.soft(page.getByRole('heading', { name: 'Check your order', exact: true })).toBeVisible();
        await expect.soft(page.getByText('Check the details now. You can use the next page to correct any errors')).toBeVisible();
        await orders.openOrderDoc('Preview order.pdf');
        await expect(orders.orderPage.getByText('Prohibited Steps Order')).toBeVisible();

        await orders.clickContinue();
        await orders.checkYourAnsAndSubmit();

        await orders.tabNavigation('Orders');
        await expect(page.getByText('Blank order (C21)')).toBeVisible();
        await expect(page.getByText('Prohibited Steps Order')).toBeVisible();
        await expect(page.getByRole('link', { name: 'c21_blank_order.pdf' })).toBeVisible();

    })

    test('CTSC uploads Transparency Order', async ({ page, signInPage, orders }) => {
        caseName = 'Transparency Order ' + dateTime.slice(0, 10);

        await updateCase(caseName, caseNumber, caseData);
        await signInPage.visit();
        await signInPage.login(CTSCUser.email, CTSCUser.password);
        await signInPage.navigateTOCaseDetails(caseNumber);

        await orders.gotoNextStep('Manage orders');
        await orders.selectOrderOperation('Create an order');
        await orders.clickContinue();

        await orders.selectOrder('Transparency Order');
        await orders.clickContinue();
        await expect.soft(page.getByText('Transparency Order', { exact: true })).toBeVisible();

        await orders.ctscUploadsTransparencyOrder();

        await orders.clickContinue();
        await orders.clickContinue();
        await orders.checkYourAnsAndSubmit();

        await orders.tabNavigation('Orders');
        await expect(page.getByRole('link', { name: 'transparency_order.pdf' })).toBeVisible();
    })

    test('Judge uploads Transparency Order ', async ({ page, signInPage, orders }) => {
        caseName = 'Transparency Order ' + dateTime.slice(0, 10);

        await updateCase(caseName, caseNumber, caseData);
        await signInPage.visit();
        await signInPage.login(judgeUser.email, judgeUser.password);
        await signInPage.navigateTOCaseDetails(caseNumber);

        await orders.gotoNextStep('Manage orders');
        await orders.selectOrderOperation('Create an order');
        await orders.clickContinue();

        await orders.selectOrder('Transparency Order');
        await orders.clickContinue();
        await expect.soft(page.getByText('Transparency Order', { exact: true })).toBeVisible();

        await orders.judgeUploadsTransparencyOrder();

        await orders.clickContinue();
        await orders.clickContinue();
        await orders.checkYourAnsAndSubmit();

        await orders.tabNavigation('Orders');
        await expect(page.getByRole('link', { name: 'transparency_order.pdf' })).toBeVisible();
    })

    test('CTSC uploads Family assistance order', async ({ page, signInPage, orders }) => {
        caseName = 'Family Assistance Order ' + dateTime.slice(0, 10);
        await updateCase(caseName, caseNumber, caseData);
        await signInPage.visit();
        await signInPage.login(CTSCUser.email, CTSCUser.password);
        await signInPage.navigateTOCaseDetails(caseNumber);

        await orders.gotoNextStep('Manage orders');
        await orders.selectOrderOperation('Create an order');
        await orders.clickContinue();

        await orders.selectOrder('Family assistance order (C42)');
        await orders.clickContinue();

        await orders.ctscFamilyAssistanceOrder();
        await orders.clickContinue();

        await orders.clickContinue();
        await orders.checkYourAnsAndSubmit();

    })

    test('Judge uploads Family assistance order ', async ({ page, signInPage, orders }) => {
        caseName = 'Family Assistance Order ' + dateTime.slice(0, 10);

        await updateCase(caseName, caseNumber, caseData);
        await signInPage.visit();
        await signInPage.login(judgeUser.email, judgeUser.password);
        await signInPage.navigateTOCaseDetails(caseNumber);

        await orders.gotoNextStep('Manage orders');
        await orders.selectOrderOperation('Create an order');
        await orders.clickContinue();

        await orders.selectOrder('Family assistance order (C42)');
        await orders.clickContinue();

        await orders.judgeUploadsFamilyAssistanceOrder();
        await orders.clickContinue();

        await orders.clickContinue();
        await orders.checkYourAnsAndSubmit();

        await orders.tabNavigation('Orders');
        await expect(page.getByText('c42_family_assistance_order.pdf', { exact: true })).toBeVisible();

    })

    test('CTSC uploads Interim care order (C33)', async ({ page, signInPage, orders }) => {
        caseName = 'Interim care order (C33) ' + dateTime.slice(0, 10);
        await updateCase(caseName, caseNumber, caseData);
        await signInPage.visit();
        await signInPage.login(CTSCUser.email, CTSCUser.password);
        await signInPage.navigateTOCaseDetails(caseNumber);
        await orders.gotoNextStep('Manage orders');

        await orders.selectOrderOperation('Create an order');
        await orders.clickContinue();

        await orders.selectOrder('Interim care order (C33)');
        await orders.clickContinue();

        await orders.ctscUploadsInterimCareOrder();
        await orders.clickContinue();

        await orders.clickContinue();
        await orders.checkYourAnsAndSubmit();

        await orders.tabNavigation('Orders');
        await expect(page.getByRole('link', { name: 'c33_interim_care_order.pdf', exact: true })).toBeVisible();

    })

    test('Judge uploads Interim care order (C33)', async ({ page, signInPage, orders }) => {
        caseName = 'Interim care order (C33) ' + dateTime.slice(0, 10);
        await updateCase(caseName, caseNumber, caseData);
        await signInPage.visit();
        await signInPage.login(judgeUser.email, judgeUser.password);
        await signInPage.navigateTOCaseDetails(caseNumber);

        await orders.gotoNextStep('Manage orders');
        await orders.selectOrderOperation('Create an order');
        await orders.clickContinue();

        await orders.selectOrder('Interim care order (C33)');
        await orders.clickContinue();

        await orders.judgeUploadsInterimCareOrder();
        await orders.clickContinue();

        await orders.clickContinue();
        await orders.checkYourAnsAndSubmit();

        await orders.tabNavigation('Orders');
        await expect(page.getByRole('link', { name: 'c33_interim_care_order.pdf', exact: true })).toBeVisible();

    })

    test('CTSC uploads Declaration of percentage (C63A)', async ({ page, signInPage, orders }) => {
        caseName = 'Declaration of Percentage (C63A) ' + dateTime.slice(0, 10);
        await updateCase(caseName, caseNumber, caseData);
        await signInPage.visit();
        await signInPage.login(CTSCUser.email, CTSCUser.password);
        await signInPage.navigateTOCaseDetails(caseNumber);
        await orders.gotoNextStep('Manage orders');

        await orders.selectOrderOperation('Create an order');
        await orders.clickContinue();

        await orders.selectOrder('Declaration of parentage (C63A)');
        await orders.clickContinue()

        await orders.ctscUploadsDeclarationOfPercentage();
        await orders.clickContinue();

        await orders.clickContinue();
        await orders.checkYourAnsAndSubmit();

        await orders.tabNavigation('Orders');
        await expect(page.getByRole('link', { name: 'c63a_declaration_of_parentage.pdf', exact: true })).toBeVisible();

    })

    test('Judge uploads Declaration of percentage (C63A)', async ({ page, signInPage, orders }) => {
        caseName = 'Declaration of Percentage (C63A) ' + dateTime.slice(0, 10);
        await updateCase(caseName, caseNumber, caseData);
        await signInPage.visit();
        await signInPage.login(judgeUser.email, judgeUser.password);
        await signInPage.navigateTOCaseDetails(caseNumber);
        await orders.gotoNextStep('Manage orders');

        await orders.selectOrderOperation('Create an order');
        await orders.clickContinue();

        await orders.selectOrder('Declaration of parentage (C63A)');
        await orders.clickContinue()

        await orders.judgeUploadsDeclarationOfPercentage();
        await orders.clickContinue();

        await orders.clickContinue();
        await orders.checkYourAnsAndSubmit();

        await orders.tabNavigation('Orders');
        await expect(page.getByRole('link', { name: 'c63a_declaration_of_parentage.pdf', exact: true })).toBeVisible();

    })
})
