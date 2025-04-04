import {expect, type Locator, type Page} from "@playwright/test";
import {BasePage} from "./base-page";

export class WelshLangRequirements extends BasePage {
    constructor(page: Page) {
        super(page);

    }

    get welshLangHeading(): Locator {
        return this.page.getByRole('heading', {name: 'Welsh language requirements'});
    }

    get doesAnyRespondentQuestion(): Locator {
        return this.page.getByText('Does any respondent, child or other person on this case need orders or court documents in Welsh?');
        ;
    }

    get langRequirementYesRadio(): Locator {
        return this.page.getByRole('radio', {name: 'Yes'});
    }

    get whichLanguageAreYouUsingQuestion(): Locator {
        return this.page.getByText('Which language are you using to complete this application?');
    }

    get englishLangRadio(): Locator {
        return this.page.getByLabel('English');
    }

    get needToBeTranslatedQuestion(): Locator {
        return this.page.getByText('Does this application need to be translated into Welsh?');
    }

    get needToBeInWelshYesRadio(): Locator {
        return this.page.getByRole('group', {name: 'Does this application need to be translated into Welsh?'}).getByLabel('Yes');
    }

    async welshLanguageSmokeTest() {
        await expect(this.welshLangHeading).toBeVisible();
        await expect(this.doesAnyRespondentQuestion).toBeVisible();
        await this.langRequirementYesRadio.click();
        await expect(this.whichLanguageAreYouUsingQuestion).toBeVisible();
        await this.englishLangRadio.click();
        await expect(this.needToBeTranslatedQuestion).toBeVisible();
        await this.needToBeInWelshYesRadio.click();
        await this.clickContinue();
        await this.checkYourAnsAndSubmit();
    }
}


