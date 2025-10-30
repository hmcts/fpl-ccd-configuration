import { Locator, Page } from '@playwright/test';
import {BasePage} from "../base-page";

export class HistoryPage extends BasePage {
    readonly endStateCell: Locator;
    readonly gatekeepingCell: Locator;

    public constructor(page: Page) {
        super(page);
        this.endStateCell = page.getByRole('cell', { name: 'End state', exact: true });
        this.gatekeepingCell = page.getByRole('cell', { name: 'Gatekeeping', exact: true });
    }
}
