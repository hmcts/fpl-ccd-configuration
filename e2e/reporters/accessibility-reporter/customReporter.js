const fs = require('fs');
const testConfig = require('../../config.js');

function generateAccessibilityReport(reportObj) {
  consoleReport(reportObj);

  const sourceReport = __dirname + '/Report.html';
  const destReport = testConfig.TestOutputDir + '/a11y.html';
  const destJson = testConfig.TestOutputDir + '/a11y_output.js';
  const previouschunkjson = testConfig.TestOutputDir + '/parallelexecution_a11y_result.json';

  const updatedReportObj = appendPreviousParallelExecTestResults(reportObj);
  const result = 'var replacejsoncontent = ' + JSON.stringify(updatedReportObj);

  fs.copyFileSync(sourceReport, destReport);
  fs.writeFileSync(previouschunkjson, JSON.stringify(updatedReportObj));
  fs.writeFileSync(destJson, result);
  copyResources();
}

function appendPreviousParallelExecTestResults (reportObj) {
  let previousObj;
  try {
    previousObj = fs.readFileSync(testConfig.TestOutputDir + '/parallelexecution_a11y_result.json');
  } catch (error) {
    return reportObj;
  }
  previousObj = JSON.parse(previousObj);
  reportObj.passCount+=previousObj.passCount;
  reportObj.failCount+=previousObj.failCount;
  reportObj.tests=reportObj.tests.concat(previousObj.tests);
  return reportObj;
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
}

module.exports = {generateAccessibilityReport};
