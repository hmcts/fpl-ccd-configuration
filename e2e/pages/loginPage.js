const I = actor();
const testStartingUrl = 'http://localhost:3451/list/case?jurisdiction=PUBLICLAW&case-type=Shared_Storage_DRAFTType&case-state=1_Initiation';

module.exports = {

    fields: {
        username: "#username",
        password: "#password",
    },
    submitButton: "Sign in",

    signIn(username, password) {
        I.amOnPage(testStartingUrl);
        I.fillField(this.fields.username, username);
        I.fillField(this.fields.password, password);
        I.click("Sign in");
    }
};
