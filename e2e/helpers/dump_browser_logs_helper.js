const fs = require('fs');
const { clearString, screenshotOutputFolder } = require('codeceptjs/lib/utils');

/**
 * Builds output file name for given test. Name includes test name and hook name (if applicable).
 *
 * @param test
 */
function buildOutputFileName (test) {
  let fileName = clearString(test.title);
  if (test.ctx && test.ctx.test && test.ctx.test.type === 'hook') {
    fileName += clearString(`_${test.ctx.test.title}`);
  }
  return screenshotOutputFolder(fileName);
}

/**
 * Converts a JavaScript value to a JSON string in pretty format.
 *
 * @param value - JavaScript object
 * @returns {string} - pretty formatted value
 */
function stringify(value) {
  const replacer = undefined;
  const indentationSize = 2;
  return JSON.stringify(value, replacer, indentationSize);
}

module.exports = class HooksHelpers extends Helper {
  async _failed(test) {
    const helper = this.helpers['Puppeteer'] || this.helpers['WebDriver'];
    let logs = await helper.grabBrowserLogs();
    if (logs !== undefined) {
      logs = logs.map(log => {
        return {
          type: log.type(),
          message: log.text(),
          location: log.location().url,
        };
      });

      if (logs.length > 0) {
        fs.writeFileSync(`${buildOutputFileName(test)}.browser.log`, stringify(logs));
      }
    }

    const source = await helper.grabSource();
    fs.writeFileSync(`${buildOutputFileName(test)}.browser.html`, source);

    const url = await helper.grabCurrentUrl();
    fs.writeFileSync(`${buildOutputFileName(test)}.browser.url`, url);
  }
};
