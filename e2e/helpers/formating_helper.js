module.exports = {
  uiFormatted(id) {
    return id.match(/.{1,4}/g).join('-');
  },
};
