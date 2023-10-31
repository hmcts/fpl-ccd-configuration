terraform {
  required_providers {
    azurerm = {
      source  = "hashicorp/azurerm"
      version = "3.78.0"
    }
    azuread = {
      source  = "hashicorp/azuread"
      version = "2.45.0"
    }
  }
}

provider "azurerm" {
  features {}
}

provider "azurerm" {
  features {}
  skip_provider_registration = true
  alias                      = "postgres_network"
  subscription_id            = var.aks_subscription_id
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

resource "azurerm_key_vault_secret" "AZURE_KEY_VAULT_SECRET" {
  name         = "app-insights-connection-string"
  value        = azurerm_application_insights.appinsights.connection_string
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
  create_managed_identity    = true
}

module "fpl-scheduler-postgres-v15-flexible-server" {

  providers = {
    azurerm.postgres_network = azurerm.postgres_network
  }

  source             = "git@github.com:hmcts/terraform-module-postgresql-flexible?ref=master"
  name                = "${var.product}-${var.component}-postgresql-v15-flexible-server"
  env                = var.env
  pgsql_admin_username = var.pgsql_admin_username

  product            = var.product
  component          = var.component
  business_area      = "cft"

  pgsql_databases = [
    {
      name : var.fpl_scheduler_db_name_v15
    }
  ]

  pgsql_version      = "15"

  pgsql_server_configuration = [
    {
      name  = "azure.extensions"
      value = "plpgsql, pg_stat_statements, pg_buffercache"
    }
  ]

  common_tags        = var.common_tags

  admin_user_object_id = var.jenkins_AAD_objectId

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

data "azurerm_key_vault_secret" "use-shuttered-case-def" {
  name      = "use-shuttered-case-def"
  key_vault_id = module.key-vault.key_vault_id
}

data "azurerm_key_vault_secret" "system-update-user-username" {
  name         = "system-update-user-username"
  key_vault_id = module.key-vault.key_vault_id
}

resource "azurerm_key_vault_secret" "idam-owner-username" {
  name         = "idam-owner-username"
  value        = data.azurerm_key_vault_secret.system-update-user-username.value
  key_vault_id = module.key-vault.key_vault_id
}

data "azurerm_key_vault_secret" "system-update-user-password" {
  name         = "system-update-user-password"
  key_vault_id = module.key-vault.key_vault_id
}

resource "azurerm_key_vault_secret" "idam-owner-password" {
  name         = "idam-owner-password"
  value        = data.azurerm_key_vault_secret.system-update-user-password.value
  key_vault_id = module.key-vault.key_vault_id
}

resource "azurerm_key_vault_secret" "scheduler-db-password-v15" {
  name         = "scheduler-db-password-v15"
  value        = module.fpl-scheduler-postgres-v15-flexible-server.password
  key_vault_id = module.key-vault.key_vault_id
}

data "azurerm_key_vault_secret" "update-summary-tab-cron" {
  name         = "update-summary-tab-cron"
  key_vault_id = module.key-vault.key_vault_id
}
