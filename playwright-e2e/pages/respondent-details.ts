import {type Page, type Locator, expect} from "@playwright/test";
import {BasePage} from "./base-page.ts";

export class RespondentDetails extends BasePage{

    get respondentDetailsHeading(): Locator {
        return this.page.getByRole("heading", { name: 'Respondents\' details' });
    }

    get firstName(): Locator {
        return this.page.getByLabel('*First name (Optional)');;
    }

    get lastName(): Locator {
        return this.page.getByLabel('*Last name (Optional)');
    }

    get dobDay(): Locator {
        return this.page.getByLabel('Day');
    }

    get dobMonth(): Locator {
        return this.page.getByLabel('Month');
    }

    get dobYear(): Locator {
        return this.page.getByLabel('Year');
    }

    get gender(): Locator {
        return this. page.getByLabel('Gender (Optional)');
    }

    get currentAddress(): Locator {
        return this.page.getByRole('group', { name: '*Current address known?' });
    }

    get telephone(): Locator {
        return this.page.getByRole('group', { name: 'Telephone (Optional)' }).locator('#respondents1_0_party_telephoneNumber_telephoneNumber');;
    }

    get relationToChild(): Locator {
        return this. page.getByLabel('*What is the respondent\'s relationship to the child or children in this case? (Optional)');
    }

    get relationToChildContact(): Locator {
        return this.page.getByRole('group', { name: 'Do you need contact details hidden from other parties? (Optional)' });
    }

    get relationToChildContactReason(): Locator {
        return this.page.getByLabel('Give reason (Optional)');
    }

    get litigationCapacity(): Locator {
        return this.page.getByRole('group', { name: 'Do you believe this person will have problems with litigation capacity (understanding what\'s happening in the case)? (Optional)' });
    }

    get litigationCapacityReason(): Locator {
        return this._litigationCapacityReason;
    }

    get legalRepresentation(): Locator {
        return this._legalRepresentation;
    }

    get continue(): Locator {
        return this._continue;
    }

    get saveAndContinue(): Locator {
        return this._saveAndContinue;
    }

    get addressNotKnownReason(): Locator {
        return this._addressNotKnownReason;
    }

  private readonly _page: Page;
  private readonly _respondentDetailsHeading: Locator;
  private readonly _firstName: Locator;
  private readonly _lastName: Locator;
  private readonly _dobDay: Locator; //DOB (date of birth)
  private readonly _dobMonth: Locator;
  private readonly _dobYear: Locator;
  private readonly _gender: Locator;
  private readonly _currentAddress: Locator;
  private readonly _telephone: Locator;
  private readonly _relationToChild: Locator;
  private readonly _relationToChildContact: Locator; //corresponds to yes or no radio feature: 'Do you need contact details hidden from other parties? (Optional)'
  private readonly _relationToChildContactReason: Locator;
  private readonly _litigationCapacity: Locator; //ie Ability to take part in proceedings
  private readonly _litigationCapacityReason: Locator;
  private readonly _legalRepresentation: Locator;
  private readonly _continue: Locator;
  private readonly _saveAndContinue: Locator;
  private readonly _addressNotKnownReason: Locator;

  public constructor(page: Page) {
      super(page);
    this._page = page;
    this._respondentDetailsHeading =
    this._firstName =
    this._lastName =
    this._dobDay =
    this._dobMonth =
    this._dobYear =
    this._gender =
    this._currentAddress =
    this._addressNotKnownReason = page.getByLabel('*Reason the address is not known');
    this._telephone =
    this._relationToChild =
    this._relationToChildContact =
    this._relationToChildContactReason =
    this._litigationCapacity =
    this._litigationCapacityReason = page.getByLabel('Give details, including assessment outcomes and referrals to health services (Optional)');
    this._legalRepresentation = page.getByRole('group', { name: '*Do they have legal representation? (Optional)' });
    this._continue = page.getByRole('button', { name: 'Continue' });
    this._saveAndContinue = page.getByRole('button', { name: 'Save and continue' });
  }

  async respondentDetailsNeeded() {
    await expect(this._respondentDetailsHeading).toBeVisible();
    await this._firstName.click();
    await this._firstName.fill('John');
    await this._lastName.click();
    await this._lastName.fill('Smith');
    await this._dobDay.click();
    await this._dobDay.fill('10');
    await this._dobMonth.click();
    await this._dobMonth.fill('11');
    await this._dobYear.click();
    await this._dobYear.fill('2001');
    await this._gender.click(); //not sure if click needed
    await this._gender.selectOption('1: Male');
    await this._currentAddress.getByLabel('No').check();
    await this._addressNotKnownReason.selectOption('2: Person deceased');
    await this._telephone.fill('01234567890');
    await this._relationToChild.click();
    await this._relationToChild.fill('aunt');
    await this._relationToChildContact.getByLabel('Yes').check();
    await this._relationToChildContactReason.click();
    await this._relationToChildContactReason.fill('this is the reason');
    await this._litigationCapacity.getByLabel('Yes').check();
    await this._litigationCapacityReason.click();
    await this._litigationCapacityReason.fill('these are the details');
    await this._legalRepresentation.getByLabel('No').check();
    await this._continue.click();
    await this._saveAndContinue.click();
  }
}
