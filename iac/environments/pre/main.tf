#########################################################
# Environment: PRE-PRODUCTION
#
# Deploy CaT resources
#########################################################
module "deploy-all" {
  source      = "../../modules/configs/deploy-all"
  space       = "pre-production"
  environment = "pre"
  cf_username = var.cf_username
  cf_password = var.cf_password
  memory      = 2048
  instances   = 3
  log_level   = "INFO"
  dev_mode    = true
  eetime_enabled = false
}
