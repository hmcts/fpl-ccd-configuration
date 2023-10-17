const fs = require('fs');
const { clearString } = require('codeceptjs/lib/utils');
const { screenshotOutputFolder } = require('codeceptjs').output;
const { Helper } = require('codeceptjs');

/**
 * Builds output file name for a given test. The name includes the test title and hook name (if applicable).
 *
 * @param test
 */
function buildOutputFileName(test) {
  let fileName = clearString(test.title);
  if (test.ctx && test.ctx.test && test.ctx.test.type === 'hook') {
    fileName += clearString(`_${test.ctx.test.title}`);
  }
  return screenshotOutputFolder(fileName);
}

module.exports = class HooksHelpers extends Helper {
  async _failed(test) {
    const helper = this.helpers['Playwright'];

    const logs = await helper.page.context().tracing.stop();

    if (logs.length > 0) {
      fs.writeFileSync(`${buildOutputFileName(test)}.trace.json`, JSON.stringify(logs, null, 2));
    }

    const source = await helper.page.content();
    fs.writeFileSync(`${buildOutputFileName(test)}.browser.html`, source);

    const url = await helper.page.url();
    fs.writeFileSync(`${buildOutputFileName(test)}.browser.url`, url);
  }
};
