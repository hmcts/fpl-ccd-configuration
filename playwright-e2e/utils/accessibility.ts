import {TestInfo, Page} from "@playwright/test";
import {AxeResults} from "axe-core";
import {createHtmlReport} from "axe-html-reporter"

export async function a11yHTMLReport(axeScanResult: AxeResults, testInfo: TestInfo, label: string) {


    const violationCount = axeScanResult.violations.length;
    if (violationCount > 0) {
        createHtmlReport({
            results: axeScanResult,
            options: {
                outputDir: '../test-results/functionalTest/axe-reports',
                reportFileName: `axe-report-${label.replace(/\s+/g, '_')}.html`,
            },
        });
        await testInfo.attach(label, {
            path: `../test-results/functionalTest/axe-reports/axe-report-${label.replace(/\s+/g, '_')}.html`,
        });


    }

}

