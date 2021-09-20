const HTMLCS = require('html_codesniffer');
const fs = require('fs');
const testConfig = require('../../config.js');

const result = {
  PASSED: 'passed',
  FAILED: 'failed',
};

const resultObj = {
  appName: 'FPL',
  passCount: 0,
  failCount: 0,
  tests: [],
};

async function runAccessibility(url, page) {
  //Add HMTL code sniffer script
  await page.addScriptTag({
    path: 'node_modules/html_codesniffer/build/HTMLCS.js',
  });

  const screenshotPath = testConfig.TestOutputDir + '/assets';
  const screenshotName = Date.now() + '.png';
  const screenshotReportRef = 'assets/' + screenshotName;

  const accessibilityErrorsOnThePage = await page.evaluate(() => {
    const processIssue = function(issue) {
      return {
        code: issue.code,
        message: issue.msg,
        type: 'error',
        element: issue.element,
        runner: 'htmlcs',
      };
    };

    const STANDARD = 'WCAG2AA';
    let messages;

    HTMLCS.process(STANDARD, window.document, function () {
      messages = HTMLCS
        .getMessages()
        .filter(function (m) {
          return m.type === HTMLCS.ERROR;
        })
        .map(processIssue);
    });
    return messages;
  });

  try {
    await page.screenshot({path: screenshotPath + '/' + screenshotName, fullPage: true});
  } catch (err) {
    fs.mkdirSync(screenshotPath, {recursive: true});
    await page.screenshot({path: screenshotPath + '/' + screenshotName, fullPage: true});
  }

  updateResultObject(url, await page.title(), screenshotReportRef, accessibilityErrorsOnThePage);
}

function updateResultObject(url, pageTitle, screenshotReportRef, accessibilityErrorsOnThePage) {
  const isPageAccessible = accessibilityErrorsOnThePage && accessibilityErrorsOnThePage.length === 0 ? result.PASSED : result.FAILED;

  const urlArr = url.split('/');

  if (isPageAccessible === result.PASSED) {
    resultObj.passCount += 1;
  } else {
    resultObj.failCount += 1;
  }

  resultObj.tests.push({
    name: urlArr[urlArr.length -2] + '/' + urlArr[urlArr.length -1],
    pageUrl: url,
    documentTitle: pageTitle,
    status: isPageAccessible,
    screenshot: screenshotReportRef,
    a11yIssues: accessibilityErrorsOnThePage,
  });
}

function getAccessibilityTestResult() {
  return resultObj;
}

module.exports = {runAccessibility, getAccessibilityTestResult};
