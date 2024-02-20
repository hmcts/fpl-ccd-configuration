import {type Page, type Locator, expect} from "@playwright/test";
import {BasePage} from "./base-page";

export class ManageHearings extends BasePage
{
    readonly hearingTypesLabelLocator: Locator;
    readonly hearingDetails: Locator;
    readonly hearingDay: Locator;
    readonly hearingMonth: Locator;
    readonly hearingYear: Locator;
    readonly hearingLengthInHours: Locator;
    readonly hearingLengthInMinutes: Locator;
    readonly inpPersonCheckbox: Locator;

    constructor(page: Page) {
      super(page);
      this.hearingDetails = this.page.getByLabel('Add details (Optional)');
      this.hearingDay = this.page.getByRole('textbox', { name: 'Day' });
      this.hearingMonth = this.page.getByRole('textbox', { name: 'Month' });
      this.hearingYear = this.page.getByRole('textbox', { name: 'Year' });
      this.hearingLengthInHours = this.page.getByLabel('Hearing length, in hours');
      this.hearingLengthInMinutes = this.page.getByLabel('Hearing length, in minutes');
      this.hearingTypesLabelLocator = this.page.locator('#hearingType .multiple-choice > label');
      this.inpPersonCheckbox = this.page.getByText('In person');
    }

    async createNewHearingOnCase(){
      await this.page.getByLabel('Add a new hearing').check();
      await this.clickContinue();
      await this.completeHearingDetails();
      await this.page.getByRole('radio', { name: 'Yes' }).check();
      await this.page.getByRole('button', { name: 'Continue' }).click();
      await this.page.getByRole('radio', { name: 'No' }).check();
      await this.clickContinue();
      await this.checkYourAnsAndSubmit();
    }

    private async completeHearingDetails() {
      await this.page.getByLabel('Case management', {exact: true}).check();
      await this.page.locator('#hearingVenue').selectOption('2: -1');
      if (!(await this.inpPersonCheckbox.isChecked())) {
        await this.inpPersonCheckbox.click();
      }
      await this.hearingDetails.click();
      await this.hearingDetails.fill('test');
      await this.verifyHearingTypesSelection();
      await this.hearingDay.click();
      await this.hearingDay.fill('1');
      await this.hearingDay.press('Tab');
      await this.hearingMonth.fill('12');
      await this.hearingMonth.press('Tab');
      await this.hearingYear.fill('2024');
      await this.hearingYear.press('Tab');
      await this.page.getByRole('spinbutton', {name: 'Hour'}).fill('01');
      await this.page.getByLabel('Set number of hours and').check();
      await this.hearingLengthInHours.click();
      await this.hearingLengthInHours.fill('1');
      await this.hearingLengthInHours.press('Tab');
      await this.hearingLengthInMinutes.fill('30');
      await this.clickContinue();
    }

    async editPastHearingOnCase() {
        await this.page.getByLabel('Edit a hearing that has taken').check();
        await this.page.getByLabel('Which draft hearing do you').selectOption('1: f2be08a2-4daf-4aa3-b7ba-95843b4bcb88');
        await this.clickContinue();
        await this.page.getByRole('radio', { name: 'Yes' }).check();
        await this.clickContinue();
        await this.checkYourAnsAndSubmit();
      }

    async vacateHearing() {
      await this.page.getByLabel('Vacate a hearing - the').check();
      await this.page.locator('#vacateHearingDateList').selectOption('2: f2be08a2-4daf-4aa3-b7ba-95843b4bcb88');
      await this.page.getByLabel('Day').click();
      await this.page.getByLabel('Day').fill('3');
      await this.page.getByLabel('Day').press('Tab');
      await this.page.getByLabel('Month').fill('11');
      await this.page.getByLabel('Month').press('Tab');
      await this.page.getByLabel('Year').fill('2012');
      await this.clickContinue();
      await this.clickContinue();
      await this.page.getByText('Yes - and I can add the new').click();
      await this.page.getByRole('button', { name: 'Continue' }).click();
      await this.page.getByText('The local authority').click();
      await this.page.locator('#vacatedReason_reason-LocalAuthority').selectOption('3: LA3');
      await this.clickContinue();
      await this.completeHearingDetails();
      await this.clickContinue();
      await this.page.getByRole('radio', { name: 'Yes' }).check();
      await this.clickContinue();
      await this.clickContinue();
      await this.checkYourAnsAndSubmit();
    }

    async adjournHearing() {
      await this.page.getByLabel('Adjourn a hearing - the').check();
      await this.page.locator('#pastAndTodayHearingDateList').selectOption('1: f2be08a2-4daf-4aa3-b7ba-95843b4bcb88');
      await this.clickContinue();
      await this.page.getByLabel('The local authority').check();
      await this.page.locator('#adjournmentReason_reason-LocalAuthority').selectOption('1: LA1');
      await this.page.getByRole('button', { name: 'Continue' }).click();
      await this.page.getByText('Yes - and I can add the new').click();
      await this.clickContinue();
      await this.completeHearingDetails();
      await this.clickContinue();
      await this.page.getByRole('radio', { name: 'Yes' }).check();
      await this.clickContinue()
      await this.clickContinue()
      await this.checkYourAnsAndSubmit();
    }

    async editFutureHearingOnCase() {
      await this.page.getByLabel('Edit a future hearing').check();
      await this.page.locator('#futureHearingDateList').selectOption('1: f2be08a2-4daf-4aa3-b7ba-95843b4bcb89');
      await this.clickContinue();
      await this.page.getByLabel('Further case management', { exact: true }).check();
      await this.verifyHearingTypesSelection();
      await this.clickContinue();
      await this.page.getByRole('radio', { name: 'Yes' }).check();
      await this.clickContinue();
      await this.clickContinue();
      await this.checkYourAnsAndSubmit();
    }

    async reListHearing() {
      await this.page.getByLabel('Re-list an adjourned or vacated hearing').check();
      await this.page.locator('#toReListHearingDateList').selectOption('1: f2be08a2-4daf-4aa3-b7ba-95843b4bcb88');
      await this.clickContinue();
      await this.completeHearingDetails();
      await this.page.getByRole('radio', { name: 'Yes' }).check();
      await this.clickContinue();
      await this.clickContinue();
      await this.checkYourAnsAndSubmit();
    };

    async verifyHearingTypesSelection() {
      const expectedHearingTypes = [
        'Emergency protection order',
        'Interim care order',
        'Case management',
        'Further case management',
        'Fact finding',
        'Issue resolution',
        'Full hearing',
        'Judgment after hearing',
        'Discharge of care',
        'Family drug & alcohol court',
        'Placement hearing',
        'Other'
      ];
      const hearingTypes = await this.hearingTypesLabelLocator.allTextContents();
      expect(hearingTypes).toEqual(expectedHearingTypes);
    }
}
