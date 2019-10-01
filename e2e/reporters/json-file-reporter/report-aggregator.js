function sum(total, value) {
  return total + (value || 0);
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
        duration: metrics.map(value => value.stats.duration).reduce(sum, 0),
      },
      passes: metrics.flatMap(value => value.passes),
      pending: metrics.flatMap(value => value.pending),
      failures: metrics.flatMap(value => value.failures),
    };
  }
}

module.exports = ReportAggregator;
