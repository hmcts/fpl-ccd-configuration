variable "subscription" {}

variable "product" {
  type = "string"
}

variable "raw_product" {
  type    = "string"
  default = "fpl"    // jenkins-library overrides product for PRs and adds e.g. pr-1-fpl
}

variable "component" {
  type = "string"
}

variable "location" {
  type    = "string"
  default = "UK South"
}

variable "env" {
  type = "string"
}

variable "ilbIp" {}

variable "common_tags" {
  type = "map"
}

variable "team_contact" {
  default = "#fpl-tech"
}

variable "tenant_id" {
  description = "(Required) The Azure Active Directory tenant ID that should be used for authenticating requests to the key vault. This is usually sourced from environemnt variables and not normally required to be specified."
}

variable "jenkins_AAD_objectId" {
  description = "(Required) The Azure AD object ID of a user, service principal or security group in the Azure Active Directory tenant for the vault. The object ID must be unique for the list of access policies."
}

variable "idam_api_url" {
  type = "string"
}

variable "ccd_ui_base_url" {
  type = "string"
}

variable "managed_identity_object_id" {
  default = ""
}
variable "enable_ase" {
  default = true
}
variable "appinsights_location" {
  type        = "string"
  default     = "West Europe"
  description = "Location for Application Insights"
}

variable "docmosis_subscription_id" {
  default = "fa397675-6ddf-4cb0-b1fc-bc7df0e51bf4"
}

variable "docmosis_resource_group" {
  default = "docmosis-iaas-dev-rg"
}

variable "docmosis_vault" {
  default = "docmosisiaasdevkv"
}
