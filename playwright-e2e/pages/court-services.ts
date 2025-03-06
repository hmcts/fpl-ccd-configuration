import {expect, type Locator, type Page} from "@playwright/test";
import {BasePage} from "./base-page";

export class CourtServices extends BasePage {

    readonly somethingElse: Locator;
    readonly courtServiceHintText: Locator;
    readonly interpreter: Locator;
    readonly intermediary: Locator;
    readonly disabilityAssistance: Locator;
    readonly separateWaitingRoom: Locator;
    readonly interpreterDetails: Locator;
    readonly intermediaryDetails: Locator;
    readonly disabilityAssistanceDetails: Locator;
    readonly separateWaitingRoomDetails: Locator;
    readonly somethingElseDetails: Locator;
    readonly courtServicesHeading: Locator

    public constructor(page: Page) {
        super(page);
        this.courtServicesHeading = page.locator('h1').filter({hasText: 'Court services'});
        this.courtServiceHintText = page.getByText('Choose which court services you need to be considered before first hearing (Optional)');
        this.interpreter = page.getByLabel('Interpreter', {exact: true});
        this.intermediary = page.getByLabel('Intermediary', {exact: true});
        this.disabilityAssistance = page.getByLabel('Facilities or assistance for');
        this.separateWaitingRoom = page.getByLabel('Separate waiting rooms', {exact: true});
        this.somethingElse = page.getByLabel('Something else');
        this.interpreterDetails = page.getByLabel('Interpreter details');
        this.intermediaryDetails = page.getByLabel('Intermediary details');
        this.disabilityAssistanceDetails = page.getByLabel('Disability facilities and');
        this.separateWaitingRoomDetails = page.getByLabel('Separate waiting rooms details');
        this.somethingElseDetails = page.getByLabel('What else is needed?');

    }
    async CourtServicesSmoketest() {
        await expect(this.courtServicesHeading).toBeVisible();
        await expect.soft(this.courtServiceHintText).toBeVisible();
        await this.interpreter.check();
        await this.intermediary.check();
        await this.disabilityAssistance.check();
        await this.separateWaitingRoom.check();
        await this.somethingElse.check();
        await this.interpreterDetails.fill('Interpreter needed for the spanish language for the respondent ');
        await this.intermediaryDetails.fill('Intermediary for the child one');
        await this.disabilityAssistanceDetails.fill('Wheel chair access need for the child');
        await this.separateWaitingRoomDetails.fill('Isolated waiting rooms for the vulnerable child Tom');
        await this.somethingElseDetails.fill('Needed child entertainer for the baby Julie');
        await this.clickContinue();
        await this.checkYourAnsAndSubmit();
    }

    async updateCourtServices() {
        await expect.soft(this.courtServiceHintText).toBeVisible();
        await this.somethingElse.uncheck();
        await this.separateWaitingRoom.uncheck();
        await this.interpreterDetails.fill('Needed new intrepreter for welsh language');
        await this.clickSubmit();
        await this.saveAndContinue.click();

    }
}
