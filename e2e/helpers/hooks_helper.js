/* global process */

const fs = require('fs');
const { clearString, screenshotOutputFolder } = require('codeceptjs/lib/utils');

function buildOutputFileName (test, extension) {
  let fileName = clearString(test.title);
  if (test.ctx && test.ctx.test && test.ctx.test.type === 'hook') {
    fileName += clearString(`_${test.ctx.test.title}`);
  }
  return screenshotOutputFolder(`${fileName}.${extension}`);
}

module.exports = class HooksHelpers extends Helper {
  _test(test) {
    test.retries(parseInt(process.env.TEST_RETRIES || '2'));
  }

  async _failed(test) {
    const logs = (await this.helpers['Puppeteer'].grabBrowserLogs()).map(log => {
      return {
        type: log.type(),
        message: log.text(),
        location: log.location().url,
      };
    });

    fs.writeFileSync(buildOutputFileName(test, 'browser.log'), JSON.stringify(logs, undefined, 2));
  }

  _afterStep(step) {
    if (step.name === 'attachFile') {
      return this.helpers['Puppeteer'].wait(2); // in seconds; time needed for document store to store uploaded files
    }
  }
};
