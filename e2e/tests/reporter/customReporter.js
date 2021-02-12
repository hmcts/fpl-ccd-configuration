const fs = require('fs');
const testConfig = require('../../config.js');

function generateAccessibilityReport(reportJson) {
  consoleReport(reportJson);

  const result = 'var replacejsoncontent = ' + JSON.stringify(reportJson);

  const sourceReport = __dirname + '/Report.html';
  const destReport = testConfig.TestOutputDir + '/a11y.html';
  const destJson = testConfig.TestOutputDir + '/a11y_output.js';

  fs.copyFileSync(sourceReport, destReport);
  fs.writeFileSync(destJson, result);
  copyResources();

}

function copyResources() {
  const resourceDir = testConfig.TestOutputDir + '/resources/';
  const cssDir = resourceDir + 'css/';
  if (!fs.existsSync(cssDir)) {
    fs.mkdirSync(cssDir, {recursive: true});
  }

  const webfontsDir = resourceDir + 'webfonts/';
  if (!fs.existsSync(webfontsDir)) {
    fs.mkdirSync(webfontsDir, {recursive: true});
  }

  fs.copyFileSync(__dirname + '/resources/angular.min.js', resourceDir + 'angular.min.js');
  fs.copyFileSync(__dirname + '/resources/css/all.css', cssDir + 'all.css');
  fs.copyFileSync(__dirname + '/resources/webfonts/fa-solid-900.woff2', webfontsDir + 'fa-solid-900.woff2');
}

function consoleReport(reportjson) {
  /* eslint-disable no-console */
  console.log('\t Total tests : ' + reportjson.tests.length);
  console.log('\t Passed tests : ' + reportjson.passCount);
  console.log('\t Failed tests : ' + reportjson.passCount);

  for (let count = 0; count < reportjson.tests.length; count++) {
    const test = reportjson.tests[count];
    if (test.status === 'failed') {
      const a11yIssues = test.a11yIssues;

      console.log('\t \t Page title : ' + test.documentTitle);
      console.log('\t \t Page url : ' + test.pageUrl);
      console.log('\t \t Screenshot of the page : ' + test.screenshot);
      console.log('\t \t Issues:');
      if (a11yIssues.length > 0) {
        for (let issueCounter = 0; issueCounter < a11yIssues.length; issueCounter++) {
          console.log('\t \t \t ' + (issueCounter + 1) + '. ' + a11yIssues[issueCounter].code);
          console.log('\t \t \t ' + a11yIssues[issueCounter].message);
        }
      } else {
        console.log('\t \t \t Error executing test steps');
      }
    }
    console.log('\t');
  }
}

module.exports = {generateAccessibilityReport};
