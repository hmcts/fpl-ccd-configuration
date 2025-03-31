import { type Page, type Locator, expect } from "@playwright/test";
import { BasePage } from "./base-page";

export class ManageLaTransferToCourts extends BasePage {
    get manageLaTransferToCourts(): Locator {
        return this.page.getByRole('heading', { name: 'Manage LAs / Transfer to court', exact: true });
    }

    get caseType(): Locator {
        return this.page.getByLabel('Case type');
    }

    get transferAnotherCourt(): Locator {
        return this.page.getByLabel('Transfer to another Court');
    }

    get selectNewCourt(): Locator {
        return this.page.getByLabel('Select new court');
    }

    get giveAccessToAnotherLa(): Locator {
        return this.page.getByRole('radio', { name: 'Give case access to another' });
    }

    get selectLocalAuthority(): Locator {
        return this.page.getByLabel('Select local authority');
    }

    get localAuthorityToTransfer(): Locator {
        return this.page.getByLabel('Select local authority to');
    }

    get removeAccess(): Locator {
        return this.page.getByRole('radio', { name: 'Remove case access from local' });
    }

    get transferToAnotherLa(): Locator {
        return this.page.getByLabel('Transfer the case to another');
    }

    get fullName(): Locator {
        return this. page.getByRole('textbox', { name: 'Full name' });
    }

    get email(): Locator {
        return this.page.getByLabel('Email', { exact: true });
    }

    get courtTransfer(): Locator {
        return this.page.getByRole('group', { name: 'Is the case transferring to a different court' });
    }
    // private readonly _manageLaTransferToCourts: Locator;
    // private readonly _caseType: Locator;
    // private readonly _transferAnotherCourt: Locator;
    // private readonly _selectNewCourt: Locator;
    // private readonly _giveAccessToAnotherLa: Locator;
    // private readonly _selectLocalAuthority: Locator;
    // private readonly _localAuthorityToTransfer: Locator;
    // private readonly _removeAccess: Locator;
    // private readonly _transferToAnotherLa: Locator;
    // private readonly _fullName: Locator;
    // private readonly _email: Locator;
    // private readonly _courtTransfer: Locator;

    // constructor(page: Page) {
    //     super(page);
    //     this._manageLaTransferToCourts =
    //     this._caseType =
    //     this._transferAnotherCourt =
    //     this._selectNewCourt =
    //     this._giveAccessToAnotherLa =
    //     this._selectLocalAuthority =
    //     this._localAuthorityToTransfer =
    //     this._removeAccess =
    //     this._transferToAnotherLa =
    //     this._fullName =
    //     this._email =
    //     this._courtTransfer =
    // }
    public async updateManageLaTransferToCourts() {
        await expect(this.manageLaTransferToCourts).toBeVisible();
        await this.transferAnotherCourt.click();
        await this.clickContinue();
        await this.selectNewCourt.selectOption('Central Family Court');
        await this.clickContinue();
        await this.checkYourAnsAndSubmit();
    }
    public async updateCourtAccess() {
        await expect(this.manageLaTransferToCourts).toBeVisible();
        await this.giveAccessToAnotherLa.click();
        await this.selectLocalAuthority.selectOption('London Borough Hillingdon');
        await this.clickContinue();
        await this.clickContinue();
        await this.checkYourAnsAndSubmit();
    }
    public async updateRemoveAccess() {
        await expect(this.manageLaTransferToCourts).toBeVisible();
        await this.removeAccess.click();
        await this.clickContinue();
        await this.clickContinue();
        await this.checkYourAnsAndSubmit();
    }
    public async updateTranferToLa() {
        await expect(this.manageLaTransferToCourts).toBeVisible();
        await this.transferToAnotherLa.click();
        await this.clickContinue();
        await this.localAuthorityToTransfer.selectOption('London Borough Hillingdon');
        await this.clickContinue();
        await this.fullName.fill('Sam Hill');
        await this.email.fill('sam@hillingdon.gov.uk');
        await this.clickContinue();
        await this.courtTransfer.getByLabel('Yes').check();
        await this.selectNewCourt.selectOption('Family Court sitting at West London');
        await this.clickContinue();
        await this.checkYourAnsAndSubmit();
    }
}
