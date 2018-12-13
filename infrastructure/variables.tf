variable "subscription" {}

variable "product" {
  type    = "string"
}

variable "raw_product" {
  type    = "string"
  default = "fpl" // jenkins-library overrides product for PRs and adds e.g. pr-1-fpl
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

variable "capacity" {
  default = "1"
}

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
