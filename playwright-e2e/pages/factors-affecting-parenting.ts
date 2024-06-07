import { type Page, type Locator, expect } from "@playwright/test";
import { BasePage } from "./base-page";

export class FactorsAffectingParenting extends BasePage{
  readonly factorsAffectingParentingHeading: Locator;
  readonly factorsAffectingParentingLink: Locator;
  readonly alcoholOrDrugAbuse: Locator;
  readonly detailsAlcoholOrDrugAbuse: Locator;
  readonly domesticViolence: Locator;
  readonly detailsDomesticViolence: Locator;
  readonly anythingElse: Locator;
  readonly detailsAnythingElse: Locator;


  public constructor(page: Page) {
    super(page);
    this.factorsAffectingParentingHeading = page.getByRole("heading", {name: "Factors affecting parenting",exact:true});
    this.factorsAffectingParentingLink = page.getByRole("link", { name: "Factors affecting parenting" });
    this.alcoholOrDrugAbuse = page.getByRole('group', { name: 'Alcohol or drug abuse (Optional)' });
    this.detailsAlcoholOrDrugAbuse = page.locator('#factorsParenting_alcoholDrugAbuseReason');
    this.domesticViolence = page.getByRole('group', { name: 'Domestic violence (Optional)' }).getByLabel('Yes');
    this.detailsDomesticViolence = page.locator('#factorsParenting_domesticViolenceReason');
    this.anythingElse = page.getByRole('group', { name: 'Anything else (Optional)' }).getByLabel('Yes');
    this.detailsAnythingElse = page.locator('#factorsParenting_anythingElseReason');
  }

  async addFactorsAffectingParenting() {
    await this.factorsAffectingParentingLink.click();
    await expect(this.factorsAffectingParentingHeading).toBeVisible();
    await this.alcoholOrDrugAbuse.getByLabel('Yes').check();
    await this.detailsAlcoholOrDrugAbuse.fill('details alcohol abuse');
    await this.domesticViolence.check();
    await this.detailsDomesticViolence.fill('details domestic violence');
    await this.anythingElse.check();
    await this.detailsAnythingElse.fill('details anything else');
    await this.continueButton.click();
    await this.checkYourAnsAndSubmit();
  }
}
