
import {ElementHandle, expect, type Locator, type Page} from "@playwright/test";
import {BasePage} from "./base-page";
import {TableUtils} from "@hmcts/playwright-common";


export class ALLWork extends BasePage {

    private readonly heading: Locator;
    private readonly ServiceDropdown: Locator;
    private readonly workTypeSearchOption: Locator;
    private readonly taskTypeSearchOption: Locator;
    private readonly tasknameSearchOption: Locator;
    private readonly applyButton: Locator;
    private readonly allLocationRadio: Locator;
    private readonly searchlocationRadio: any;
    private selectRole: Locator;
    private selectPerson: Locator;
    private locationSearchInput: Locator;
    private taskLink: Locator;
    private toDefaultButton: Locator;
    private workAllocationTable: Locator;
    taskTableFirstRow: any;


    public constructor(page: Page) {
        super(page);
        this.heading = this.page.getByRole('heading', {name: 'All work'});
        this.ServiceDropdown = this.page.locator('#select_service');
        this.taskLink = this.page.getByRole('link', {name: 'Tasks'});
        this.workTypeSearchOption = this.page.locator('#inputWorkTypeSearch');
        this.taskTypeSearchOption = this.page.locator('#select_taskType');
        this.tasknameSearchOption = this.page.locator('#inputSelectTaskName');
        this.selectRole = this.page.locator('select_role');
        this.selectPerson = this.page.locator('inputSelectPerson');
        this.applyButton = this.page.getByRole('button', {name: 'Apply'});
        this.toDefaultButton = this.page.getByRole('button', {name: 'Reset to default'});
        this.allLocationRadio = this.page.getByRole('radio', {name: 'All'});
        this.searchlocationRadio = this.page.getByRole('radio', {name: 'search for a location'});
        this.locationSearchInput = this.page.locator('inputLocationSearch');
        this.workAllocationTable = this.page.locator('table.cdk-table.govuk-table')
        this.taskTableFirstRow = this.page.locator('tr.cdk-row.govuk-table__row:not(.actions-row)').first()
    }


    async filterService(serviceName: string = "Family Public Law"): Promise<void> {
        await this.ServiceDropdown.selectOption(serviceName);
    }

    async filterLocation(location: string = "All"): Promise<void> {
        if (location === "All") {
            await this.allLocationRadio.check();
        }
        if (location === "search for a location") {
            await this.searchlocationRadio.check();
            await this.locationSearchInput.fill(location);
            // need to add the code to select the location from the dropdown after filling in the name
        }
    }

    async filterByWorkType(workType: string = "Routine work"): Promise<void> {
        await this.workTypeSearchOption.fill(workType);
        await this.page.getByText(workType, {exact: true}).click();
        await this.page.getByRole('link', {name: 'Add work type'}).click();

    }

    async filterTaskBYRoleType(taskType: string): Promise<void> {
        await this.taskTypeSearchOption.selectOption(taskType);
    }


    async filterByTaskName(taskName: string): Promise<void> {
        await this.tasknameSearchOption.fill(taskName);
        await this.page.getByText(taskName, {exact: true}).click();
    }


    async clickSearchButton(): Promise<void> {

        const [response] = await Promise.all([
            this.page.waitForResponse(response =>
                response.url().includes('/workallocation/task') &&
                response.request().method() === 'POST' &&
                response.status() === 200
            ),
            this.applyButton.click()
        ]);
        await this.page.locator('body').click();
    }

    async clickCancelLink(): Promise<void> {
        await this.cancelLink.click();
    }

    async ParseTaskTable(): Promise<Array<Record<string, string>>> {
        const tableUtils = new TableUtils();
        const TaskMap = await tableUtils.parseWorkAllocationTable(this.workAllocationTable);
        return TaskMap;
    }

    async getDistinctColumnValues(columnName: string): Promise<string[]> {
        const rows = await this.ParseTaskTable();

        return [...new Set(
            rows
                .map(row => (row[columnName] ?? '').trim())
                .filter(value => value.length > 0)
        )];
    }

    async resetFilters(): Promise<void> {
        await this.toDefaultButton.click();
    }

    async sortColumn(sortBy: string = 'case_name', sortOrder: string = 'descending'): Promise<void> {
        const headerCell = this.page.locator(`th.cdk-column-${sortBy}`);
        const sortButton = headerCell.locator('button');
        console.log(headerCell);
        await sortButton.click();
        let sortState = await this.page.locator('th.cdk-column-case_name')
            .getAttribute('aria-sort');
        console.log(sortState);

        // If it's not already descending, click the sort button until it is
        while (sortState !== sortOrder) {
            await sortButton.click();

            // Optional: If sorting triggers a network request, add your waitForResponse here

            // Refresh the sort state value for the next loop check
            sortState = await headerCell.getAttribute('aria-sort');
        }
        await expect(headerCell).toHaveAttribute('aria-sort', sortOrder);
    }
}


