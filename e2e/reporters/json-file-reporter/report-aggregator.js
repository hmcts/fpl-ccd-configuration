function sum(result, value) {
  if (result == null) {
    return value;
  }
  return result + value;
}

function flatten(result, array) {
  if (result == null) {
    return array;
  }
  return result.concat(array);
}

function min(result, value) {
  if (result == null) {
    return value;
  }
  return value.localeCompare(result) < 0 ? value : result;
}

function max(result, value) {
  if (result == null) {
    return value;
  }
  return value.localeCompare(result) > 0 ? value : result;
}

class ReportAggregator {
  /**
   * Aggregates JSON reports produced by JSON file reporter.
   *
   * Note: Irrelevant statistics are omitted.
   *
   * @param metrics - array of reports in JSON format to aggregate
   * @returns {{}|undefined} - JSON object with aggregated report metrics
   */
  static aggregate(metrics) {
    if (!metrics) {
      return undefined;
    }

    return {
      stats: {
        suites: metrics.map(value => value.stats.suites).reduce(sum, 0),
        tests: metrics.map(value => value.stats.tests).reduce(sum, 0),
        passes: metrics.map(value => value.stats.passes).reduce(sum, 0),
        pending: metrics.map(value => value.stats.pending).reduce(sum, 0),
        failures: metrics.map(value => value.stats.failures).reduce(sum, 0),
        start: metrics.map(value => value.stats.start).reduce(min, undefined),
        end: metrics.map(value => value.stats.end).reduce(max, undefined),
        duration: metrics.map(value => value.stats.duration).reduce(sum, 0),
      },
      passes: metrics.map(value => value.passes).reduce(flatten, []),
      pending: metrics.map(value => value.pending).reduce(flatten, []),
      failures: metrics.map(value => value.failures).reduce(flatten, []),
    };
  }
}

module.exports = ReportAggregator;
