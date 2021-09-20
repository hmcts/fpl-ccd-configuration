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

    let numberOfSteps = Math.min(test.steps.length, 50);

    for(let i=0; i<numberOfSteps; i++){
      output.print(`      ${test.steps[test.steps.length-1-i]}`);
    }
    if(numberOfSteps<test.steps.length){
      output.print(`      (${test.steps.length - numberOfSteps} earlier steps skipped)`);
    }
  }
});

