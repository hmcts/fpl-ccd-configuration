import { type Page, type Locator, expect } from "@playwright/test";
import {BasePage} from "./base-page.ts";

export class FactorsAffectingParenting extends BasePage{
    get factorsAffectingParentingHeading(): Locator {
        return this.page.getByRole("heading", {name: "Factors affecting parenting",exact:true});
    }

    get factorsAffectingParentingLink(): Locator {
        return this.page.getByRole("link", { name: "Factors affecting parenting" });
    }

    get alcoholOrDrugAbuse(): Locator {
        return this.page.getByRole('group', { name: 'Alcohol or drug abuse (Optional)' });
    }

    get detailsAlcoholOrDrugAbuse(): Locator {
        return this. page.locator('#factorsParenting_alcoholDrugAbuseReason');
    }

    get domesticViolence(): Locator {
        return this.page.getByRole('group', { name: 'Domestic violence (Optional)' }).getByLabel('Yes');
    }

    get detailsDomesticViolence(): Locator {
        return this.page.locator('#factorsParenting_domesticViolenceReason');
    }

    get anythingElse(): Locator {
        return this.page.getByRole('group', { name: 'Anything else (Optional)' }).getByLabel('Yes');
    }

    get detailsAnythingElse(): Locator {
        return this.page.locator('#factorsParenting_anythingElseReason');
    }


  public constructor(page: Page) {
      super(page);
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
    await this.clickContinue()
   await this.checkYourAnsAndSubmit();
  }
}
