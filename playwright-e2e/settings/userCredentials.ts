import { DefaultPassword } from "../local-config/config";

const e2ePw = process.env.E2E_TEST_PASSWORD || DefaultPassword;

export const newSwanseaLocalAuthorityUserOne = {
  email: "local-authority-swansea-0001@maildrop.cc",
  password: e2ePw,
};
