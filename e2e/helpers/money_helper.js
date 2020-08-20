const parse =  money => Number(money.replace(/[^0-9.]+/g, ''));

module.exports = {
  parse,
};
