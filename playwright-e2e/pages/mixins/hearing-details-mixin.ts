import {expect, type Locator, Page} from "@playwright/test";

export function HearingDetailsMixin(BasePage) {
  return class extends BasePage {
    readonly hearingTypesLabelLocator: Locator;

    constructor(page: Page) {
      super(page);
      this.hearingTypesLabelLocator = this.page.locator('#hearingType .multiple-choice > label');
    }

    async completeHearingDetails() {
      await expect(this.page.getByText('Type of hearing')).toBeVisible();
      await this.verifyHearingTypesSelection();
      await this.page.getByLabel('Case management', { exact: true }).check();
      await this.page.locator('#hearingVenue').selectOption({ label: 'Swansea Crown Court' });
      if (!(await this.page.getByLabel('In person').isChecked())) {
        await this.page.getByLabel('In person').check();
      }
      await this.page.getByRole('textbox', { name: 'Day' }).fill('1');
      await this.page.getByRole('textbox', { name: 'Month' }).fill('6');
      await this.page.getByRole('textbox', { name: 'Year' }).fill('2024');
      await this.page.getByRole('spinbutton', { name: 'Hour' }).fill('01');
      await this.page.getByLabel('Set number of hours and').check();
      await this.page.getByLabel('Hearing length, in hours').fill('1');
      await this.page.getByLabel('Hearing length, in minutes').fill('30');
      await this.clickContinue();
    }

    async verifyHearingTypesSelection() {
      const expectedHearingTypes = [
        'Emergency protection order',
        'Interim care order',
        'Case management',
        'Further case management',
        'Fact finding',
        'Issue resolution',
        'Final',
        'Judgment after hearing',
        'Discharge of care',
        'Family drug & alcohol court',
        'Placement hearing'
      ];
      const hearingTypes = await this.hearingTypesLabelLocator.allTextContents();
      expect(hearingTypes).toEqual(expectedHearingTypes);
    }
  };
}
