
const { I } = inject();

module.exports = {

  enterEmail(email = 'familypubliclaw+gatekeeper@gmail.com') {
    I.fillField('#gateKeeperEmail', email);
  },
};
