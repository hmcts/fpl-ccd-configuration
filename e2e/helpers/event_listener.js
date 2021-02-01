const event = require('codeceptjs').event;
const output = require('codeceptjs').output;

event.dispatcher.on(event.test.started, function (test) {
  if (test.retryNum === undefined) {
    output.print(`  ► ${test.title}`);
  } else {
    output.print(`  ► ${test.title} (retry ${test.retryNum + 1})`);
  }
});
