// aat, demo, perftest, ithc
const env = process.env.ENVIRONMENT || "aat";

interface UrlConfig {
    [key: string]: string;
}

export const urlConfig: UrlConfig = {

  idamUrl: process.env.IDAM_API_URL || `https://idam-api.${env}.platform.hmcts.net`,
 //serviceUrl: process.env.CASE_SERVICE_URL || `http://fpl-case-service-${env}.service.core-compute-${env}.internal`,
    serviceUrl: process.env.CASE_SERVICE_URL || `https://fpl-case-service-pr-5570.preview.platform.hmcts.net`,
    frontEndBaseURL: process.env.FE_BASE_URL || `https://manage-case.${env}.platform.hmcts.net`,
  manageOrgURL: process.env.MO_BASE_URL || `https://manage-org.${env}.platform.hmcts.net`,
  cafcassAPISearch: process.env.CAFCASS_SEARCH_URL || `https://fpl-case-service-pr-5570.preview.platform.hmcts.net/cases`
    // You can add other URLs as needed
};
