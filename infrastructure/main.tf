terraform {
  required_providers {
    azuread = {
      source  = "hashicorp/azuread"
      version = "1.6.0"
    }
  }
}

provider "azurerm" {
  version = "=2.49.0"

  features {}
}

resource "azurerm_resource_group" "rg" {
  name     = "${var.product}-${var.component}-${var.env}"
  location = var.location

  tags = var.common_tags
}

resource "azurerm_application_insights" "appinsights" {
  name                = "${var.product}-${var.component}-appinsights-${var.env}"
  location            = var.appinsights_location
  resource_group_name = azurerm_resource_group.rg.name
  application_type    = "web"

  tags = var.common_tags
}

#Copying appinsights key to the valut
resource "azurerm_key_vault_secret" "AZURE_APPINSGHTS_KEY" {
  name         = "AppInsightsInstrumentationKey"
  value        = azurerm_application_insights.appinsights.instrumentation_key
  key_vault_id = module.key-vault.key_vault_id
}

module "key-vault" {
  source                  = "git@github.com:hmcts/cnp-module-key-vault?ref=master"
  name                    = "fpl-${var.env}"
  product                 = var.product
  env                     = var.env
  tenant_id               = var.tenant_id
  object_id               = var.jenkins_AAD_objectId
  resource_group_name     = azurerm_resource_group.rg.name
  product_group_name      = "dcd_group_fpl_v2"
  common_tags             = var.common_tags

  #aks migration
  managed_identity_object_id = var.managed_identity_object_id
}

module "fpl-scheduler-db" {
  source             = "git@github.com:hmcts/cnp-module-postgres?ref=master"
  product            = "${var.product}-${var.component}"
  location           = var.location_db
  env                = var.env
  database_name      = "fpl_scheduler"
  postgresql_user    = "fpl_scheduler"
  postgresql_version = "11"
  sku_name           = "GP_Gen5_2"
  sku_tier           = "GeneralPurpose"
  common_tags        = var.common_tags
  subscription       = var.subscription
}

data "azurerm_key_vault_secret" "fpl_support_email_secret" {
  name      = "${var.product}-support-email"
  key_vault_id = module.key-vault.key_vault_id
}

resource "azurerm_key_vault_secret" "scheduler-db-password" {
  name      = "scheduler-db-password"
  value     = module.fpl-scheduler-db.postgresql_password
  key_vault_id = module.key-vault.key_vault_id
}
