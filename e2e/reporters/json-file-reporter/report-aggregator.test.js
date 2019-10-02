/* global describe, it */

const { strictEqual, deepEqual } = require('assert');

const ReportAggregator = require('./report-aggregator');

describe('ReportAggregator', () => {
  it('returns undefined when input array is not provided', () => {
    strictEqual(ReportAggregator.aggregate(undefined), undefined);
    strictEqual(ReportAggregator.aggregate(null), undefined);
  });

  it('returns empty aggregation when input array is empty', () => {
    deepEqual(ReportAggregator.aggregate([]), {
      stats: {
        suites: 0,
        tests: 0,
        passes: 0,
        pending: 0,
        failures: 0,
        start: undefined,
        end: undefined,
        duration: 0,
      },
      passes: [],
      pending: [],
      failures: [],
    });
  });

  it('returns same metrics when input array has only one element', () => {
    const metrics = [{
      stats: {
        suites: 1,
        tests: 0,
        passes: 1,
        pending: 0,
        failures: 0,
        start: '2019-12-31T23:59:59.000Z',
        end: '2019-12-31T23:59:59.999Z',
        duration: 100,
      },
      passes: [{
        title: 'Negative equality test',
        code: 'notEqual(true, false);',
      }],
      pending: [],
      failures: [],
    }];

    deepEqual(ReportAggregator.aggregate(metrics), {
      stats: {
        suites: 1,
        tests: 0,
        passes: 1,
        pending: 0,
        failures: 0,
        start: '2019-12-31T23:59:59.000Z',
        end: '2019-12-31T23:59:59.999Z',
        duration: 100,
      },
      passes: [{
        title: 'Negative equality test',
        code: 'notEqual(true, false);',
      }],
      pending: [],
      failures: [],
    });
  });

  it('returns aggregated metrics when input array has more than one element', () => {
    const metrics = [{
      stats: {
        suites: 0,
        tests: 0,
        passes: 0,
        pending: 0,
        failures: 0,
        start: '2019-12-31T23:59:59.001Z',
        end: '2019-12-31T23:59:59.998Z',
        duration: 0,
      },
      passes: [],
      pending: [],
      failures: [],
    }, {
      stats: {
        suites: 1,
        tests: 0,
        passes: 1,
        pending: 0,
        failures: 0,
        start: '2019-12-31T23:59:59.000Z',
        end: '2019-12-31T23:59:59.999Z',
        duration: 100,
      },
      passes: [{
        title: 'Negative equality test',
        code: 'notEqual(true, false);',
      }],
      pending: [],
      failures: [],
    }, {
      stats: {
        suites: 1,
        tests: 0,
        passes: 1,
        pending: 0,
        failures: 1,
        start: '2019-12-31T23:59:59.001Z',
        end: '2019-12-31T23:59:59.998Z',
        duration: 199,
      },
      passes: [{
        title: 'Positive equality test',
        code: 'equal(true, true);',
      }],
      pending: [],
      failures: [{
        title: 'Non zero test',
        code: 'equal(0, 0.0);',
        error: {
          message: 'Unexpected error',
          stack: '...',
        },
      }],
    }];

    deepEqual(ReportAggregator.aggregate(metrics), {
      stats: {
        suites: 2,
        tests: 0,
        passes: 2,
        pending: 0,
        failures: 1,
        start: '2019-12-31T23:59:59.000Z',
        end: '2019-12-31T23:59:59.999Z',
        duration: 299,
      },
      passes: [{
        title: 'Negative equality test',
        code: 'notEqual(true, false);',
      }, {
        title: 'Positive equality test',
        code: 'equal(true, true);',
      }],
      pending: [],
      failures: [{
        title: 'Non zero test',
        code: 'equal(0, 0.0);',
        error: {
          message: 'Unexpected error',
          stack: '...',
        },
      }],
    });
  });
});
