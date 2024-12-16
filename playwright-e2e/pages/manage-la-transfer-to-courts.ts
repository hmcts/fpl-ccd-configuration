import { type Page, type Locator, expect } from "@playwright/test";
import { BasePage } from "./base-page";

export class ManageLaTransferToCourts extends BasePage {
    readonly manageLaTransferToCourts: Locator;
    readonly caseType: Locator;
    readonly transferAnotherCourt: Locator;
    readonly selectNewCourt: Locator;
    readonly giveAccessToAnotherLa: Locator;
    readonly selectLocalAuthority: Locator;
    readonly localAuthorityToTransfer: Locator;
    readonly removeAccess: Locator;
    readonly transferToAnotherLa: Locator;
    readonly fullName: Locator;
    readonly email: Locator;
    readonly courtTransfer: Locator;
    readonly saveAndContinueButton: Locator;

    constructor(page: Page) {
        super(page);
        this.manageLaTransferToCourts = page.getByRole('heading', { name: 'Manage LAs / Transfer to court', exact: true });
        this.caseType = page.getByLabel('Case type');
        this.transferAnotherCourt = page.getByLabel('Transfer to another Court');
        this.selectNewCourt = page.getByLabel('Select new court');
        this.giveAccessToAnotherLa = page.getByRole('radio', { name: 'Give case access to another' });
        this.selectLocalAuthority = page.getByLabel('Select local authority');
        this.localAuthorityToTransfer = page.getByLabel('Select local authority to');
        this.removeAccess = page.getByRole('radio', { name: 'Remove case access from local' });
        this.transferToAnotherLa = page.getByLabel('Transfer the case to another');
        this.fullName = page.getByRole('textbox', { name: 'Full name' });
        this.email = page.getByLabel('Email', { exact: true });
        this.courtTransfer = page.getByRole('group', { name: 'Is the case transferring to a different court' });
        this.saveAndContinueButton = page.getByRole("button", {name: "Save and continue"});
    }
    public async updateManageLaTransferToCourts() {
        await expect(this.manageLaTransferToCourts).toBeVisible();
        await this.transferAnotherCourt.click();
        await this.continueButton.click();
        await this.selectNewCourt.selectOption('Central Family Court');
        await this.continueButton.click();
        await this.saveAndContinue.click();
    }
    public async updateCourtAccess() {
        await expect(this.manageLaTransferToCourts).toBeVisible();
        await this.giveAccessToAnotherLa.click();
        await this.selectLocalAuthority.selectOption('London Borough Hillingdon');
        await this.continueButton.click();
        await this.continueButton.click();
        await this.saveAndContinue.click();
    }
    public async updateRemoveAccess() {
        await expect(this.manageLaTransferToCourts).toBeVisible();
        await this.removeAccess.click();
        await this.continueButton.click();
        await this.continueButton.click();
        await this.saveAndContinue.click();
    }
    public async updateTranferToLa() {
        await expect(this.manageLaTransferToCourts).toBeVisible();
        await this.transferToAnotherLa.click();
        await this.continueButton.click();
        await this.localAuthorityToTransfer.selectOption('4: HN');
        await this.continueButton.click();
        await this.fullName.fill('Sam Hill');
        await this.email.fill('sam@hillingdon.gov.uk');
        await this.continueButton.click();
        await this.courtTransfer.getByLabel('Yes').check();
        await this.selectNewCourt.selectOption('2: 332');
        await this.continueButton.click();
        await this.saveAndContinue.click();
    }
}