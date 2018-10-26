const I = actor();

module.exports = {
  openExistingCase(caseId) {
    let href = `/case/PUBLICLAW/Shared_Storage_DRAFTType/${caseId.replace(/\D/g, '')}`;
    I.click(locate('a').withAttr({href: href}));
  },
};
