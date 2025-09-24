import {expect, type Locator, type Page} from "@playwright/test";
import {BasePage} from "./base-page";
import config from "../settings/test-docs/config";

export class Extend26WeekTimeline extends BasePage {


    public constructor(page: Page) {
        super(page);

    }

    async isExtensionApprovedAtHearing(YesNo = 'Yes') {
        await this.page.getByRole('group', {name: 'Was this timeline extension approved at a hearing?'}).getByLabel(`${YesNo}`).click();
    }

    async selectHearing(hearingDetails: string) {
        await this.page.getByLabel('Which hearing was this extension approved at?').selectOption(hearingDetails);
    }

    async isAboutAllChildren(YesNo = 'Yes') {
        await this.page.getByRole('group', {name: 'Is the timeline extending for all the children?'}).getByLabel(`${YesNo}`).click();
    }

    async sameExtensionDateForAllChildren(YesNo = 'Yes') {
        await this.page.getByRole('group', {name: 'Are all the selected children’s timelines being extended by the same amount of time, and for the same reason?'}).getByLabel(`${YesNo}`).click();
    }

    async enterExtendsionDetails() {
        await this.page.getByRole('radio', {name: 'Extend by 8 Weeks'}).check();
        await this.page.getByRole('radio', {name: 'Timetable for proceedings'}).check();
    }


}
