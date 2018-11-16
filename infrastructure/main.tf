provider "azurerm" {}

locals {
  ase_name               = "${data.terraform_remote_state.core_apps_compute.ase_name[0]}"

  local_env = "${(var.env == "preview" || var.env == "spreview") ? (var.env == "preview") ? "aat" : "saat" : var.env}"
  local_ase = "${(var.env == "preview" || var.env == "spreview") ? (var.env == "preview") ? "core-compute-aat" : "core-compute-saat" : local.ase_name}"

  vault_name = "${(var.env == "preview" || var.env == "spreview") ? (var.env == "preview") ? "${var.product}-aat" : "${var.product}-saat" : "${var.product}-${var.env}"}"

  # URLs
  IDAM_S2S_AUTH_URL = "http://rpe-service-auth-provider-${local.local_env}.service.${local.local_ase}.internal"
  DOCUMENT_MANAGEMENT_URL = "http://dm-store-${local.local_env}.service.${local.local_ase}.internal"
  CORE_CASE_DATA_API_URL = "http://ccd-data-store-api-${local.local_env}.service.${local.local_ase}.internal"
}

data "azurerm_key_vault" "key_vault" {
  name = "${local.vault_name}"
  resource_group_name = "${local.vault_name}"
}

module "case-service" {
  source              = "git@github.com:hmcts/moj-module-webapp?ref=master"
  product             = "${var.product}-${var.component}"
  location            = "${var.location}"
  env                 = "${var.env}"
  ilbIp               = "${var.ilbIp}"
  subscription        = "${var.subscription}"
  capacity            = "${var.capacity}"
  common_tags         = "${var.common_tags}"

  app_settings = {
    IDAM_S2S_AUTH_URL = "${local.IDAM_S2S_AUTH_URL}"
    IDAM_S2S_AUTH_TOTP_SECRET = "AABBCCDDEEFFGGHH"
    DOCUMENT_MANAGEMENT_URL = "${local.DOCUMENT_MANAGEMENT_URL}"
    CORE_CASE_DATA_API_URL = "${local.CORE_CASE_DATA_API_URL}"

    LOGBACK_REQUIRE_ALERT_LEVEL = false
    LOGBACK_REQUIRE_ERROR_CODE  = false
  }
}
