import {expect, type Locator, type Page} from "@playwright/test";
import {BasePage} from "./base-page";

export class CourtServicesNeeded extends BasePage {
    public constructor(page: Page) {
        super(page);

    }

    get interpreterOptional(): Locator {
        return this.page.getByRole('group', {name: 'Interpreter (Optional)'});
    }

    get giveDetailsIncludingPerson(): Locator {
        return this.page.getByRole('group', {name: 'Give details including person'});
    }

    get courtServicesNeededHeading(): Locator {
        return this.page.getByRole('heading', {name: 'Court services needed', exact: true});
    }

    get SpokenOrWrittenWelsh(): Locator {
        return this.page.getByRole('group', {name: 'Spoken or written Welsh ('});
    }

    get intermediaryOptional(): Locator {
        return this.page.getByRole('group', {name: 'Intermediary (Optional)'});
    }

    get facilitiesOrAssistanceForDisability(): Locator {
        return this.page.getByRole('group', {name: 'Facilities or assistance for'});
    }

    get separateWatingRoomOrSecurityMeasures(): Locator {
        return this.page.getByRole('group', {name: 'Separate waiting room or'});
    }

    get somethingElse(): Locator {
        return this.page.getByRole('group', {name: 'Something else (Optional)'});
    }

    async CourtServicesSmoketest() {
        await expect(this.courtServicesNeededHeading).toBeVisible();
        await this.interpreterOptional.getByLabel('Yes').check();
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
