import { type Page, type Locator, expect } from "@playwright/test";
import { BasePage } from "./base-page";

export class CourtServicesNeeded extends BasePage {
    readonly interpreterOptional: Locator;
    readonly giveDetailsIncludingPerson: Locator;
    readonly courtServicesNeededHeading: Locator;
    readonly SpokenOrWrittenWelsh: Locator;
    readonly intermediaryOptional: Locator;
    readonly facilitiesOrAssistanceForDisability: Locator;
    readonly separateWatingRoomOrSecurityMeasures: Locator;
    readonly somethingElse: Locator;

    public constructor(page: Page) {
        super(page);
        this.courtServicesNeededHeading = page.getByRole('heading', { name: 'Court services', exact: true });
        this.interpreterOptional = page.getByRole('group', { name: 'Interpreter (Optional)' });
        this.giveDetailsIncludingPerson = page.getByRole('group', { name: 'Give details including person' });
        this.SpokenOrWrittenWelsh = page.getByRole('group', { name: 'Spoken or written Welsh (' });
        this.intermediaryOptional = page.getByRole('group', { name: 'Intermediary (Optional)' });
        this.facilitiesOrAssistanceForDisability = page.getByRole('group', { name: 'Facilities or assistance for' });
        this.separateWatingRoomOrSecurityMeasures = page.getByRole('group', { name: 'Separate waiting room or' });
        this.somethingElse = page.getByRole('group', { name: 'Something else (Optional)' });
    }
    async CourtServicesSmoketest() {
        await expect(this.courtServicesNeededHeading).toBeVisible();
        await this.interpreterOptional.getByLabel('Yes').check();
       // await expect(this.giveDetailsIncludingPerson).toBeVisible();
        await this.page.getByLabel('Give details including person').fill('Test');
        await this.SpokenOrWrittenWelsh.getByLabel('No').check();
        await this.intermediaryOptional.getByLabel('No').check();
        await this.facilitiesOrAssistanceForDisability.getByLabel('No').check();
        await this.separateWatingRoomOrSecurityMeasures.getByLabel('No').check();
        await this.somethingElse.getByLabel('No').check();
        await this.clickContinue();
        await this.checkYourAnsAndSubmit();
    }
}
