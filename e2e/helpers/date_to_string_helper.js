module.exports = (date) => {
  let hour = '00', minute = '00', second = '00';

  if (date.hour) {
    hour = date.hour;
  }
  if (date.minute) {
    minute = date.minute;
  }
  if (date.second) {
    second = date.second;
  }

  return date.year + '-' + date.month + '-' + date.day + ' ' + hour + ':' + minute + ':' + second;
};
