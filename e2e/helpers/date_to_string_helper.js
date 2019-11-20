// In "American" format so that it is understood properly by the formatter
module.exports = (date) => date.month + '-' + date.day + '-' + date.year + ' ' +
  date.hour + ':' + date.minute + ':' + date.second;
