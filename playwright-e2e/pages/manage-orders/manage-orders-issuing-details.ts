import { Locator, Page } from '@playwright/test';
import { BasePage } from '../base-page';

export class ManageOrdersIssuingDetails extends BasePage {
    readonly judgeIssuingOrderGroup: Locator;
    readonly yesRadioButton: Locator;
    readonly noRadioButton: Locator;
    readonly judgeOrMagistrateGroup: Locator;
    readonly herHonourJudgeRadioButton: Locator;
    readonly lastNameLabel: Locator;
    readonly emailAddressLabel: Locator;
    readonly legalAdvisorLabel: Locator;
    readonly dayLabel: Locator;
    readonly monthLabel: Locator;
    readonly yearLabel: Locator;

    constructor(page: Page) {
        super(page);
        this.judgeIssuingOrderGroup = page.getByRole('group', { name: 'Is this judge issuing the order?' });
        this.yesRadioButton = this.judgeIssuingOrderGroup.getByRole('radio', { name: 'Yes' });
        this.noRadioButton = this.judgeIssuingOrderGroup.getByRole('radio', { name: 'No' });
        this.judgeOrMagistrateGroup = page.getByRole('group', { name: 'Judge or magistrate\'s title' });
        this.herHonourJudgeRadioButton = this.judgeOrMagistrateGroup.getByRole('radio', { name: 'Her Honour Judge' });
        this.lastNameLabel = this.page.getByLabel('Last name');
        this.emailAddressLabel = this.page.getByLabel('Email Address');
        this.legalAdvisorLabel = page.getByLabel('Justices\' Legal Adviser\'s full name (Optional)');
        this.dayLabel = page.getByRole('textbox', { name: 'Day' });
        this.monthLabel = page.getByRole('textbox', { name: 'Month' });
        this.yearLabel = page.getByRole('textbox', { name: 'Year' });
    }

    async checkYes(): Promise<void> {
        await this.yesRadioButton.check();
    }

    async checkNo(): Promise<void> {
        await this.noRadioButton.check();
    }

    async checkHerHonourJudge(): Promise<void> {
        await this.herHonourJudgeRadioButton.check();
    }

    async fillLastName(name: string): Promise<void> {
        await this.lastNameLabel.fill(name);
    }

    async fillEmailAddress(email: string): Promise<void> {
        await this.emailAddressLabel.fill(email);
    }

    async fillLegalAdviser(name: string): Promise<void> {
        await this.legalAdvisorLabel.fill(name);
    }

    async fillDate(day: string, month: string, year: string): Promise<void> {
        await this.dayLabel.fill(day);
        await this.monthLabel.fill(month);
        await this.yearLabel.fill(year);
    }
}
