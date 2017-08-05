terraform {
  backend "s3" {
    bucket  = "icfp2017-kst3-jp"
    key     = "terraform/terraform.tfstate"
    region  = "ap-northeast-1"
    profile = "analysis"
  }
}