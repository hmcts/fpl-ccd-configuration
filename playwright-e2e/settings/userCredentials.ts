import * as dotenv from "dotenv";
dotenv.config();

const e2ePw = process.env.E2E_TEST_PASSWORD || "";
const e2eJudgePw = process.env.E2E_TEST_JUDGE_PASSWORD || "";

export const newSwanseaLocalAuthorityUserOne = {
  email: "local-authority-swansea-0001@maildrop.cc",
  password: e2ePw,
};
export const systemUpdateUser = {
  email: "fpl-system-update@mailnesia.com",
  password: e2ePw,
};
export const judgeWalesUser = {
  email: "judge-wales@ejudiciary.net",
  password: e2eJudgePw,
}
