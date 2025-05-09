import { Page} from "@playwright/test";

export class CourtAdminUserPage  {
    get page(): Page {
        return this._page;
    }

    set page(value: Page) {
        this._page = value;
    }
    private _page: Page;

    constructor(page: Page) {
        this._page = page;
    }

}
