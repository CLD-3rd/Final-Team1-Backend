variable "name_prefix" {
  description = "리소스 이름 접두어 (ex: Team1-backend)"
  type        = string
}

variable "environment" {
  description = "환경 (dev, prod 등)"
  type        = string
}

variable "vpc_cidr" {
  description = "VPC의 CIDR 블록"
  type        = string
}