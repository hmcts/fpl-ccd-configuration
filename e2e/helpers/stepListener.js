'use strict';

const recorder = require('codeceptjs').recorder;
const event = require('codeceptjs').event;

const delays = {
  'click': 200,
  'doubleClick': 200,
  'selectOption': 300,
  'fillField': 100,
  'checkOption': 300,
  'attachFile': 2000,
};

module.exports = function() {

  event.dispatcher.on(event.step.after, (step) => {

    recorder.add('custom-delay', async () => {
      return new Promise((resolve) => {

        const delay = delays[step.name] || 200;

        setTimeout(resolve, delay);
      });
    });
  });
};
