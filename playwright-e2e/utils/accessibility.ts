
import { TestInfo, Page } from "@playwright/test";
import { AxeResults } from "axe-core";
import {createHtmlReport} from "axe-html-reporter"

export async function a11yHTMLReport(axeScanResult: AxeResults, testInfo: TestInfo, label: string) {

        createHtmlReport({
            results: axeScanResult,
            options: {
                outputDir: '../test-results/functionalTest',
                reportFileName: `axe-report-${label.replace(/\s+/g, '_')}.html`,
            },
        });
    const violationCount = axeScanResult.violations.length;
    if (violationCount > 0) {
        console.error(`[A11Y WARNING]: Found ${violationCount} accessibility violations.`);

    }
    await testInfo.attach(label, {
        path: `../test-results/functionalTest/axe-report-${label.replace(/\s+/g, '_')}.html`,
    });

}

