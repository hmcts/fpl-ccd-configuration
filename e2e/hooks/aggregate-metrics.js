const fs = require('fs');

const output = require('codeceptjs').output;
const glob = require('glob');

const ReportAggregator = require('../reporters/json-file-reporter/report-aggregator');
const { stringify } = require('../reporters/json-file-reporter/utils');

/**
 * Reads files found using defined glob expression.
 *
 * @param expression - glob expression
 * @returns {string[]}
 */
function readFiles(expression) {
  const filePaths = glob.sync(expression);
  return filePaths.map(filePath => {
    return fs.readFileSync(filePath, { encoding: 'utf8' });
  });
}

/**
 * Generates metrics aggregate from `metrics.json` files located in output directory
 * and saves it to `metrics` JSON file is output directory.
 */
module.exports = function () {
  output.log('Generating aggregated metrics...');

  const config = require('../../codecept.conf').config;

  const metrics = readFiles(`${config.output}/**/metrics.json`).map(content => JSON.parse(content));
  const aggregate = ReportAggregator.aggregate(metrics);

  if (aggregate !== undefined) {
    const outputFilePath = `${config.output}/metrics`;
    fs.writeFileSync(outputFilePath, stringify(aggregate));
    output.log(`Aggregated metrics has been saved to ${outputFilePath}`);
  }
};
