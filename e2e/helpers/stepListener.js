'use strict';

const recorder = require('codeceptjs').recorder;
const event = require('codeceptjs').event;

const delays = {
  'click': 500,
  'doubleClick': 500,
  'selectOption': 500,
  'fillFiled': 500,
  'checkOption': 500,
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
