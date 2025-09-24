import { type Page, type Locator, expect } from "@playwright/test";
import { BasePage } from "./base-page";

export class ExpertReport extends BasePage {



    get caseNote(): Locator {
        return  this.page.getByRole('textbox', { name: 'Note' });
    }
    get addNewButton(): Locator {
        //return  this.page.getByRole('button', { name: 'Add new' });
       return  this.page.getByRole('button', { name: 'Add new' })
    }
    get expertReportOneTypeOption(): Locator {
        return  this.page.getByText('What type of report have you requested? is required');
    }
    get expertReportTwoTypeOption(): Locator {
        return  this.page.locator('#expertReport_1_expertReportList')
    }

    get reportOneRequestedDateD(): Locator {
        return  this.page.locator('#expertReport_1_expertReportList')
    }
    get dateRequested(): Locator {
        return  this.page.getByRole('group', { name: 'Date requested' });
    }
    get dateApproved(): Locator {
        return  this.page.getByRole('group', { name: 'Date approved' });
    }
    get reportApprovedYes(): Locator {
        return    this.page.getByRole('group', { name: 'Has it been approved? (Optional)' }).getByLabel('Yes')

    }
    get reportApprovedNo(): Locator {
        return    this.page.getByRole('group', { name: 'Has it been approved? (Optional)' }).getByLabel('No')

    }


    constructor(page: Page) {
        super(page);

    }


    async selectExpertReportType(type: string, reportNumber:number=0){
        await this.page.getByText('What type of report have you requested?').nth(reportNumber).selectOption({label: type});
    }


    async addNewReport(reportNumber:number){
        await this.addNewButton.nth(reportNumber).focus();
        await this.addNewButton.nth(reportNumber).click();
        await expect(this.addNewButton.nth(1)).toBeVisible();

    }
async waitForLoadReportDetails(){
    await expect(this.addNewButton.nth(1)).toBeVisible();
}

async checkDateValidationPass(reportNumber:number=0){
   await this.page.press('body', 'Tab');
    await expect (this.page.getByText(' The data entered is not valid for Date requested ')).toBeHidden()
}

    async enterRequestedDate(requestDate:Date,reportNumber:number=0) {
        await this.dateRequested.getByLabel('Day').nth(reportNumber).fill(requestDate.getDate().toString());
        await this.dateRequested.getByLabel('Month').nth(reportNumber).fill(requestDate.getMonth().toString());
        await this.dateRequested.getByLabel('Year').nth(reportNumber).fill(requestDate.getFullYear().toString());
    }
    async enterApprovedDate(approvedDate:Date,reportNumber:number=0) {
        await this.dateApproved.nth(reportNumber).getByLabel('Day').fill(approvedDate.getDate().toString());
        await this.dateApproved.nth(reportNumber).getByLabel('Month').fill(approvedDate.getMonth().toString());
            await this.dateApproved.nth(reportNumber).getByLabel('Year').fill(approvedDate.getFullYear().toString());

    }

    async orderApprovedYes(reportNumber:number=0){
        await this.reportApprovedYes.nth(reportNumber).click();
        await this.reportApprovedYes.nth(reportNumber).click();

    }
    async orderApprovedNo(reportNumber:number=0){
        await this.reportApprovedNo.nth(reportNumber).click();
        // await this.reportApprovedNo.nth(reportNumber).click();
    //
    }
    // async addReportTwo(){
    //     await this.reportTwo.click();
    // }

    async note(note: string) {

        // await.click();
        // await expect(page.getByLabel('What type of report have you')).toBeVisible();
        // await expect(page.getByLabel('What type of report have you')).toBeVisible();
        // await expect(page.getByLabel('What type of report have you')).toContainText('--Select a value--PediatricPediatric RadiologistOther Medical reportFamily Centre Assessments - ResidentialFamily Centre Assessments - Non-ResidentialPsychiatric - On child and Parent(s)/carersPsychiatric - On child onlyAdult Psychiatric Report on Parents(s)Psychological Report on Child Only - ClinicalPsychological Report on Child Only - EducationalPsychological Report on Parent(s) - full cognitivePsychological Report on Parent(s) - functioningPsychological Report on Parent(s) and childMulti Disciplinary AssessmentIndependent social workerHaematologistOphthalmologistNeurosurgeonOther Expert ReportProfessional: Drug/AlcoholProfessional: Hair StrandProfessional: DNA testingProfessional: OtherToxicology report/statement');
      //  await .click();
      //  await page.getByLabel('What type of report have you').selectOption('10: educationalReportOnChild');
      //   await page.getByRole('textbox', { name: 'Day' }).click();
      //   await page.getByRole('textbox', { name: 'Day' }).fill('3');
      //   await page.getByRole('textbox', { name: 'Month' }).click();
      //   await page.getByRole('textbox', { name: 'Month' }).fill('4');
      //   await page.getByRole('group', { name: 'Date requested' }).click();
      //   await page.getByRole('textbox', { name: 'Year' }).click();
      //   await page.getByRole('textbox', { name: 'Year' }).fill('2025');
      //   await page.getByRole('radio', { name: 'Yes' }).check();
      //   await page.getByRole('group', { name: 'Date approved' }).getByLabel('Day').click();
      //   await page.getByRole('group', { name: 'Date approved' }).getByLabel('Day').fill('2');
      //   await page.getByRole('group', { name: 'Date approved' }).getByLabel('Month').click();
      //   await page.getByRole('group', { name: 'Date approved' }).getByLabel('Month').fill('6');
      //   await page.getByRole('group', { name: 'Date approved' }).getByLabel('Year').click();
      //   await page.getByRole('group', { name: 'Date approved' }).getByLabel('Year').fill('2025');
      //   await page.getByRole('button', { name: 'Add new' }).nth(1).click();
      //   await page.locator('#expertReport_1_expertReportList').selectOption('14: multiDisciplinaryAssessment');
      //   await page.getByRole('textbox', { name: 'Day' }).nth(2).click();
      //   await page.getByRole('textbox', { name: 'Day' }).nth(2).fill('45');
      //   await page.locator('ccd-write-date-field').filter({ hasText: 'Date requestedFor example, 27 3 2007 The data entered is not valid for Date' }).getByLabel('Month').click();
      //   await page.locator('ccd-write-date-field').filter({ hasText: 'Date requestedFor example, 27 3 2007 The data entered is not valid for Date' }).getByLabel('Day').dblclick();
      //   await page.locator('ccd-write-date-field').filter({ hasText: 'Date requestedFor example, 27 3 2007 The data entered is not valid for Date' }).getByLabel('Day').fill('5');
      //   await page.locator('ccd-write-date-field').filter({ hasText: 'Date requestedFor example, 27 3 2007 The data entered is not valid for Date' }).getByLabel('Day').press('Tab');
      //   await page.locator('ccd-write-date-field').filter({ hasText: 'Date requestedFor example, 27 3 2007 The data entered is not valid for Date' }).getByLabel('Month').fill('5');
      //   await page.locator('ccd-write-date-field').filter({ hasText: 'Date requestedFor example, 27 3 2007 The data entered is not valid for Date' }).getByLabel('Month').press('Tab');
      //   await page.locator('ccd-write-date-field').filter({ hasText: 'Date requestedFor example, 27 3 2007 The data entered is not valid for Date' }).getByLabel('Year').fill('2025');
      //   await page.locator('ccd-write-date-field').filter({ hasText: 'Date requestedFor example, 27 3 2007 The data entered is not valid for Date' }).getByLabel('Year').press('Tab');
      //   await page.locator('#expertReport_1_reportApproval_Yes').check();
      //   await page.locator('#expertReport_1_reportApproval_No').check();
      //   await page.getByRole('button', { name: 'Submit' }).click();
      //   await page.getByRole('button', { name: 'Save and continue' }).click();
      //   await page.locator('button').nth(3).click();
      //   await page.locator('button').nth(3).click();
      //   await page.getByText('Expert Reports').click();
      //   // await expect(page.getByText('Psychological Report on Child')).toBeVisible();
      //   // await expect(page.getByText('Multi Disciplinary Assessment')).toBeVisible();
      //   await page.getByLabel('Next step').selectOption('21: Object');
      //   await page.getByRole('button', { name: 'Go' }).click();
      //   // await expect(page.locator('#expertReport_0_expertReportList')).toContainText('--Select a value--PediatricPediatric RadiologistOther Medical reportFamily Centre Assessments - ResidentialFamily Centre Assessments - Non-ResidentialPsychiatric - On child and Parent(s)/carersPsychiatric - On child onlyAdult Psychiatric Report on Parents(s)Psychological Report on Child Only - ClinicalPsychological Report on Child Only - EducationalPsychological Report on Parent(s) - full cognitivePsychological Report on Parent(s) - functioningPsychological Report on Parent(s) and childMulti Disciplinary AssessmentIndependent social workerHaematologistOphthalmologistNeurosurgeonOther Expert ReportProfessional: Drug/AlcoholProfessional: Hair StrandProfessional: DNA testingProfessional: OtherToxicology report/statement');


    };
};
