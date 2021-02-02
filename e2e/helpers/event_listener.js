const event = require('codeceptjs').event;
const output = require('codeceptjs').output;

event.dispatcher.on(event.test.started, function (test) {
  let tryNum = test.retryNum === undefined ? 1 : test.retryNum + 2;
  output.print(`  ► ${test.title} (try ${tryNum})`);
});

event.dispatcher.on(event.test.failed, function (test) {
  if (test._retries > 0) {
    let tryNum = test.retryNum === undefined ? 1 : test.retryNum + 1;
    output.print(`  ✖ ${test.title} (try ${tryNum}) failed:`);
    output.print(`    ${test.steps}`);
  }
});

