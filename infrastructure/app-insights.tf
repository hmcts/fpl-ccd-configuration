data "azurerm_application_insights" "ai" {
  name                = "${var.product}-case-service-appinsights-${var.env}"
  resource_group_name = "${var.product}-case-service-${var.env}"
}

locals {
  app_insights_config = jsonencode(
    merge(
      jsondecode(
        file("${path.module}/../lib/applicationinsights.json")), { connectionString = data.azurerm_application_insights.ai.connection_string }
    )
  )
}

data "azurerm_key_vault" "key_vault" {
  name                = "${var.product}-${var.env}"
  resource_group_name = "${var.product}-case-service-${var.env}"
}

resource "azurerm_key_vault_secret" "app_insights_connection_string" {
  name         = "app-insights-connection-string"
  value        = data.azurerm_application_insights.ai.connection_string
  key_vault_id = data.azurerm_key_vault.key_vault.id
}
