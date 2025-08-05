terraform {
  required_providers {
    kubectl = {
      source  = "gavinbunney/kubectl"
      version = "~> 1.14.0"
    }
    # 이미 사용 중일 수도 있는 프로바이더도 함께 명시해 주세요
    kubernetes = {
      source  = "hashicorp/kubernetes"
      # 2.20.0 이상의 3.0.0 미만 버전을 모두 허용
      version = ">= 2.20.0, < 3.0.0"
    }
    helm = {
      source  = "hashicorp/helm"
      version = "~> 3.0.0"
    }
  }
}