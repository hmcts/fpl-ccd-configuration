import { type Page, type Locator } from "@playwright/test";
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
        await this.welshLangHeading.isVisible();
        await this.doesAnyRespondentQuestion.isVisible();
        await this.langRequirementYesRadio.click();
        await this.whichLanguageAreYouUsingQuestion.isVisible();
        await this.englishLangRadio.click();
        await this.needToBeTranslatedQuestion.isVisible();
        await this.needToBeInWelshYesRadio.click();
        await this.clickContinue();
        await this.checkYourAnswersHeader.isVisible();
        await this.checkYourAnsAndSubmit();
    }
}


