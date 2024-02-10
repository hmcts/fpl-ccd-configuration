import { type Page, type Locator, expect } from "@playwright/test";

export class FactorsAffectingParenting {
  readonly page: Page;
  readonly factorsAffectingParentingHeading: Locator;
  readonly alcoholOrDrugAbuse: Locator;
  readonly detailsAlcoholOrDrugAbuse: Locator;
  readonly domesticViolence: Locator;
  readonly detailsDomesticViolence: Locator;
  readonly anythingElse: Locator;
  readonly detailsAnythingElse: Locator;
  readonly Continue: Locator;
  readonly CheckYourAnswers: Locator;
  readonly SaveAndContinue: Locator;

  public constructor(page: Page) {
    this.page = page;
    this.factorsAffectingParentingHeading = page.getByRole("heading", {name: "Factors affecting parenting"});
    this.alcoholOrDrugAbuse = page.getByRole('group', { name: 'Alcohol or drug abuse (Optional)' }).getByLabel('Yes');
    this.detailsAlcoholOrDrugAbuse = page.getByRole('textbox', { name: 'Give details (Optional)' });
    this.domesticViolence = page.getByRole('group', { name: 'Domestic violence (Optional)' }).getByLabel('Yes');
    this.detailsDomesticViolence = page.getByRole('textbox', { name: 'Give details (Optional)' });
    this.anythingElse = page.getByRole('group', { name: 'Anything else (Optional)' }).getByLabel('No');
    this.detailsAnythingElse = page.getByRole('textbox', { name: 'Give details (Optional)' });
    this.Continue = page.getByRole('button', { name: 'Continue' });
    this.CheckYourAnswers = page.getByRole('heading', { name: 'Check your answers' });
    this.SaveAndContinue = page.getByRole('button', { name: 'Save and continue' });
  }

  async addFactorsAffectingParenting() {
    await this.factorsAffectingParentingHeading.isVisible();
    await this.alcoholOrDrugAbuse.check();
    await this.detailsAlcoholOrDrugAbuse.fill('details alcohol abuse');
    await this.domesticViolence.check();
    await this.detailsDomesticViolence.fill('details domestic violence');
    await this.anythingElse.check();
    await this.detailsAnythingElse.fill('details anything else');
    await this.Continue.click();
    expect(this.CheckYourAnswers).toBeVisible;
    await this.SaveAndContinue.click();
  }
}
