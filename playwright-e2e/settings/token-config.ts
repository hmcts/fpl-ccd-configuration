import {judgeWalesUser, newSwanseaLocalAuthorityUserOne, systemUpdateUser} from "./user-credentials";

process.env.IDAM_RETRY_ATTEMPTS = '3'
process.env.IDAM_RETRY_BASE_MS = '200'
process.env.S2S_RETRY_ATTEMPTS = '3'
process.env.S2S_RETRY_BASE_MS = '200'

export interface User {
  email: string;
  password: string;
}

export const users: User[] = [
    systemUpdateUser,
    newSwanseaLocalAuthorityUserOne,
    judgeWalesUser
];

export const services: string[] = [
  'fpl_case_service',
  'ccd_data'
];


