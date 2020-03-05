const zeroByDefault = (value) => value || '00';

module.exports = (date) => zeroByDefault(date.year) + '-' + zeroByDefault(date.month) + '-' + zeroByDefault(date.day)
  + ' ' + zeroByDefault(date.hour) + ':' + zeroByDefault(date.minute) + ':' + zeroByDefault(date.second);
