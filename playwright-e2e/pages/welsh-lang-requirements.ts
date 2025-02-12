import {type Page, type Locator, expect} from "@playwright/test";
import { BasePage } from "./base-page";

export class WelshLangRequirements extends BasePage {

    readonly welshLangHeading: Locator;
    readonly doesAnyRespondentQuestion: Locator;
    readonly langRequirementYesRadio: Locator;
    readonly whichLanguageAreYouUsingQuestion: Locator;
    readonly englishLangRadio: Locator;
    readonly needToBeTranslatedQuestion: Locator;
    readonly needToBeInWelshYesRadio: Locator;

    constructor(page: Page) {
        super(page);
        this.welshLangHeading = page.getByRole('heading', { name: 'Welsh language requirements' });
        this.doesAnyRespondentQuestion = page.getByText('Does any respondent, child or other person on this case need orders or court documents in Welsh?');
        this.langRequirementYesRadio = page.getByRole('radio', { name: 'Yes' });
        this.whichLanguageAreYouUsingQuestion = page.getByText('Which language are you using to complete this application?');
        this.englishLangRadio = page.getByLabel('English');
        this.needToBeTranslatedQuestion = page.getByText('Does this application need to be translated into Welsh?');
        this.needToBeInWelshYesRadio = page.getByRole('group', { name: 'Does this application need to be translated into Welsh?' }).getByLabel('Yes');
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
        await expect(this.checkYourAnswersHeader).toBeVisible();
        await this.checkYourAnsAndSubmit();
    }
}


