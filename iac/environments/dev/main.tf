#########################################################
# Environment: DEV
#
# Deploy CaT resources
#########################################################
module "deploy-all" {
  source      = "../../modules/configs/deploy-all"
  space       = "development"
  environment = "dev"
  cf_username = var.cf_username
  cf_password = var.cf_password
  dev_mode    = true
  log_level   = "TRACE"
  eetime_enabled = true
}
