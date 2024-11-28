import {type Locator, type Page} from "@playwright/test";
import {BasePage} from "./base-page";

export class ListingHearingAction extends BasePage {

    private listingRequired: Locator;
    private amendhearing: Locator;
    private specialMeasure: Locator;
    private listingActionDetails: Locator;
    private listingAction: any;
    private confirmAction: any;

    constructor(page: Page) {
        super(page);
        this.listingRequired = page.getByLabel('Listing required');
        this.amendhearing = page.getByLabel('Amend/vacate a hearing');
        this.specialMeasure = page.getByLabel('Special measures required');
        this.listingActionDetails = page.getByLabel('Give details');
        this.listingAction = page.getByLabel('Select request to review');
        this.confirmAction = page.getByLabel('Confirm appropriate action');
    }

    async requestListing() {

        await this.listingRequired.check();
        await this.amendhearing.click();
        await this.specialMeasure.check();
        await this.listingActionDetails.fill('Request listing action to  amend a hearing and add special measures for the respondent');
        await this.clickContinue();
        await this.clickRequest();
    }

    async reviewListingAction() {
        await this.assignTaskToMe();
        await this.markTaskDone();
        await this.listingAction.selectOption({index: 1});
        await this.clickContinue();
        await this.confirmAction.check();
        await this.clickContinue();
        await this.confirmListedTaskReviewed();
    }


}
