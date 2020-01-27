/**
 * Converts a JavaScript value to a JSON string in pretty format
 */
function stringify(value) {
  const replacer = undefined;
  const indentationSize = 2;
  return JSON.stringify(value, replacer, indentationSize);
}

module.exports = {
  stringify,
};
