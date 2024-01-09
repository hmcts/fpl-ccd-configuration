import { DefaultPassword } from "../local-config/config";

let e2ePw = process.env.E2E_TEST_PASSWORD || DefaultPassword;

export const newSwanseaLocalAuthorityUserOne = {
  email: "local-authority-swansea-0001@maildrop.cc",
  password: e2ePw,
};

if (process.env.JENKINS_BUILD) {
  e2ePw = "";
}