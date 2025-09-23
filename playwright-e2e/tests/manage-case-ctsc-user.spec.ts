import {test} from '../fixtures/create-fixture';
import {
    newSwanseaLocalAuthorityUserOne,
    judgeWalesUser,
    CTSCUser,
    HighCourtAdminUser,
    privateSolicitorOrgUser
} from '../settings/user-credentials';
import {expect} from "@playwright/test";
import caseWithResSolicitor from '../caseData/caseWithRespondentSolicitor.json' assert {type: "json"};
import caseWithHearing from '../caseData/caseWithHearingDetails.json' assert {type: "json"};
import {createCase, giveAccessToCase, updateCase} from "../utils/api-helper";
import {getCurrentdate, subtractMonthDate} from "../utils/util-helper";
import CaseWithOrderDetails from '../caseData/caseWithAllTypesOfOrders.json' assert {type: "json"};

test.describe('Admin application management', () => {
    const dateTime = new Date().toISOString();
    let caseNumber: string;
    let caseName: string;

    test.beforeEach(async () => {
        caseNumber = await createCase('e2e case', newSwanseaLocalAuthorityUserOne);
    });
    //mark test as slow to give extra timeout

    test('CTSC admin request welsh language translation',
        async ({page, signInPage, welshLangRequirements}) => {
            caseName = 'CTSC request for welsh translation of documents/orders ' + dateTime.slice(0, 10);
            await updateCase(caseName, caseNumber, caseWithResSolicitor);
            await signInPage.visit();
            await signInPage.login(CTSCUser.email, CTSCUser.password);
            await signInPage.navigateTOCaseDetails(caseNumber);
            await welshLangRequirements.gotoNextStep('Welsh language requirements');
            await welshLangRequirements.CTSCRequestWelshTranslation('Yes');
            await welshLangRequirements.clickContinue();
            await welshLangRequirements.checkYourAnsAndSubmit();
            await welshLangRequirements.tabNavigation('Summary');
            await welshLangRequirements.page.reload();
            await expect(page.getByText('WELSH CASE')).toBeVisible();


        });


    test('CTSC admin add case note to the application',
        async ({page, signInPage, caseNote}) => {
            caseName = 'CTSC admin add case note' + dateTime.slice(0, 10);
            await updateCase(caseName, caseNumber, caseWithResSolicitor);
            await signInPage.visit();
            await signInPage.login(CTSCUser.email, CTSCUser.password);
            await signInPage.navigateTOCaseDetails(caseNumber);
            await caseNote.gotoNextStep('Add a case note');
            await expect.soft(page.getByText('Add note detail, including relevant dates and people involved')).toBeVisible();

            await caseNote.enterCaseNote('This application is classified as priority due to the vulnerability of the child involved.');
            await caseNote.clickSubmit();
            await caseNote.clickSaveAndContinue();
            await caseNote.tabNavigation('Notes');
            await expect(page.getByText('This application is classified as priority due to the vulnerability of the child involved.')).toBeVisible();

        });

    test('CTSC log expert report to the application',
        async ({page, signInPage, expertReport}) => {
            caseName = 'CTSC log expert report' + dateTime.slice(0, 10);
            await updateCase(caseName, caseNumber, caseWithResSolicitor);
            await signInPage.visit();
            await signInPage.login(CTSCUser.email, CTSCUser.password);
            await signInPage.navigateTOCaseDetails(caseNumber);
            await expertReport.gotoNextStep('Log expert report');

            await expertReport.addNewReport(0);


            await expertReport.selectExpertReportType('Pediatric', 0);
            await expertReport.enterRequestedDate(await subtractMonthDate(2), 0);
            await expertReport.checkDateValidationPass();
            await expertReport.orderApprovedYes(0)
            await expertReport.enterApprovedDate(await subtractMonthDate(1), 0);
            await expertReport.checkDateValidationPass();

            await expertReport.addNewReport(1);
            await expertReport.selectExpertReportType('Adult Psychiatric Report on Parents(s)', 1);
            await expertReport.enterRequestedDate(await subtractMonthDate(2), 1);
            await expertReport.checkDateValidationPass();
            await expertReport.orderApprovedYes(1);
            await expertReport.enterApprovedDate(await subtractMonthDate(1), 1);
            await expertReport.checkDateValidationPass();
            await expertReport.clickSubmit();
            await expertReport.clickSaveAndContinue();
            await expertReport.tabNavigation('Expert reports');

            await expect(expertReport.page.getByText('Report 1')).toBeVisible();
            await expect(expertReport.page.getByText('Pediatric')).toBeVisible();
            await expect(expertReport.page.getByText('Report 2')).toBeVisible();
            await expect(expertReport.page.getByText('Adult Psychiatric Report on')).toBeVisible();
        });


    test('CTSC request for 26 week Case extension', async ({page, signInPage, extend26WeekTimeline}) => {
        caseName = 'CTSC request 26 week case extension' + dateTime.slice(0, 10);
        await updateCase(caseName, caseNumber, caseWithHearing);
        await signInPage.visit();
        await signInPage.login(CTSCUser.email, CTSCUser.password);
        await signInPage.navigateTOCaseDetails(caseNumber);
        // await page.pause();
        await extend26WeekTimeline.gotoNextStep('Extend 26-week timeline');
        await extend26WeekTimeline.isExtensionApprovedAtHearing('yes');
        await extend26WeekTimeline.selectHearing('Case management hearing, 3 November 2012');
        await extend26WeekTimeline.clickContinue();
        await extend26WeekTimeline.isAboutAllChildren('Yes');
        await extend26WeekTimeline.clickContinue();
        await extend26WeekTimeline.sameExtensionDateForAllChildren('Yes');
        await extend26WeekTimeline.enterExtendsionDetails();
        await extend26WeekTimeline.clickContinue();
        await extend26WeekTimeline.checkYourAnsAndSubmit();
        await expect(page.getByText('Extended timeline date')).toBeVisible();
        await expect(page.getByText('Extended timeline:')).toBeVisible();
    })
    test('Close the case', async ({signInPage, page, recordFinalDecision}) => {
        caseName = 'CTSC make final decision' + dateTime.slice(0, 10);
        let decisionDate = await subtractMonthDate(1);
        await updateCase(caseName, caseNumber, caseWithHearing);
        await signInPage.visit();
        await signInPage.login(CTSCUser.email, CTSCUser.password);
        await signInPage.navigateTOCaseDetails(caseNumber);

        await recordFinalDecision.gotoNextStep('Record final decisions');

        await expect(recordFinalDecision.page.getByText('In a closed case, you can still:')).toBeVisible();
        await expect(recordFinalDecision.page.getByText(' add a case note')).toBeVisible();
        await expect(recordFinalDecision.page.getByText(' upload a document')).toBeVisible();
        await expect(recordFinalDecision.page.getByText(' issue a C21 (blank order)')).toBeVisible();
        await expect(recordFinalDecision.page.getByText(' submit a C2 application')).toBeVisible();
        await expect(recordFinalDecision.page.getByText('Appeals can still be made up to 21 days after a close is marked as closed/resolved.')).toBeVisible();

        await recordFinalDecision.selectFinalDecisionForAllChildren('Yes');
        await recordFinalDecision.clickContinue();
        await recordFinalDecision.enterDecisionDate(decisionDate);
        await recordFinalDecision.dateValidationPass();

        await recordFinalDecision.enterFinalOutCome();
        await recordFinalDecision.clickSubmit();
        await recordFinalDecision.clickSubmit();

        await recordFinalDecision.tabNavigation('Summary');
        await expect(page.getByText('Close the case')).toBeVisible();
        await expect(page.getByText(new Intl.DateTimeFormat('en-GB', {
            day: 'numeric',
            month: 'short',
            year: 'numeric'
        }).format(decisionDate))).toBeVisible();

    })
    test('CTSC admin send order remainder', async ({signInPage, page, sendOrderRemainder}) => {
        caseName = 'Admin send order remainders' + dateTime.slice(0, 10);
        await updateCase(caseName, caseNumber, caseWithHearing);
        await signInPage.visit();
        await signInPage.login(CTSCUser.email, CTSCUser.password);
        await signInPage.navigateTOCaseDetails(caseNumber);
        await page.pause()
        await sendOrderRemainder.gotoNextStep('Send order reminder');
        await expect.soft(sendOrderRemainder.page.getByText('These concluded hearings do not have CMOs attached (in draft or sealed):')).toBeVisible();
        await sendOrderRemainder.sendOrderRemainder('Yes');
        await sendOrderRemainder.clickSubmit();
        await sendOrderRemainder.gotoHistoryTab();
        await expect(sendOrderRemainder.page.getByRole('cell', { name: 'Send order reminder', exact: true })).toBeVisible();

    })


});
