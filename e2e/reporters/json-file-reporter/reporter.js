const fs = require('fs');

const Base = require('mocha/lib/reporters/base');

const { stringify } = require('./utils');

exports = module.exports = function JSONReporter(runner, options) {
  Base.call(this, runner, options);

  const pending = [];
  const failures = [];
  const passes = [];

  runner.on('pass', (test) => {
    passes.push(test);
  });

  runner.on('pending', (test) => {
    pending.push(test);
  });

  runner.on('fail', (test) => {
    failures.push(test);
  });

  runner.once('end', () => {
    const outcome = {
      stats: this.stats,
      passes: passes.map(convert),
      pending: pending.map(convert),
      failures: failures.map(convert),
    };

    runner.testResults = outcome;

    fs.writeFileSync(`${global.output_dir}/metrics.json`, stringify(outcome));
  });
};

/**
 * Returns a plain-object representation of `test` free of cyclic properties etc
 */
function convert(test) {
  let error = test.err || {};
  if (error instanceof Error) {
    error = convertToJSON(error);
  }
  return {
    title: test.fullTitle(),
    code: test.body,
    error: cleanCircularReferences(error),
  };
}

/**
 * Transforms an error object into a JSON object
 */
function convertToJSON(error) {
  const outcome = {};
  Object.getOwnPropertyNames(error).forEach((key) => {
    outcome[key] = error[key];
  });
  return outcome;
}

/**
 * Replaces any circular references inside `obj` with '[object Object]'
 */
function cleanCircularReferences(obj) {
  const cache = [];
  return JSON.parse(
    JSON.stringify(obj, (key, value) => {
      if (typeof value === 'object' && value !== null) {
        if (cache.indexOf(value) !== -1) {
          // instead of going in a circle, we'll print [object Object]
          return `${value}`;
        }
        cache.push(value);
      }

      return value;
    })
  );
}
