/*global locate*/

const I = actor();

module.exports = {
  openExistingCase(caseId) {
    const href = `/case/PUBLICLAW/Shared_Storage_DRAFTType/${caseId.replace(/\D/g, '')}`;
    const caseLink = locate('a').withAttr({href: href});
    I.click(caseLink);
  },
};
