terraform {
  required_providers {
    # helm = {
    #   source  = "hashicorp/helm"
    #   version = ">= 2.12.0"
    # }
    # kubernetes = {
    #   source  = "hashicorp/kubernetes"
    #   version = ">= 2.20.0"
    # }
    aws = {
      source  = "hashicorp/aws"
      version = ">= 5.0.0"
    }
  }
}

provider "aws" {
  region = "ap-northeast-2"
}

