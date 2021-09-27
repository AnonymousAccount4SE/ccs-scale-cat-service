terraform {
  backend "s3" {
    region  = "eu-west-2"
    key     = "ccs-scale-cat-service-sbx2"
    encrypt = true
  }
}

provider "aws" {
  profile = "default"
  region  = "eu-west-2"
}