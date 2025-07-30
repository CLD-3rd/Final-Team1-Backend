variable "vpc_id" {
  description = "VPC ID"
  type        = string
}

variable "name_prefix" {
  description = "리소스 이름 접두어"
  type        = string
}

variable "environment" {
  description = "환경 (dev, prod 등)"
  type        = string
}

variable "public_subnet_cidrs" {
  description = "Public Subnet CIDR 리스트"
  type        = list(string)
}

variable "private_subnet_cidrs" {
  description = "Private Subnet CIDR 리스트"
  type        = list(string)
}

variable "azs" {
  description = "사용할 가용영역 리스트"
  type        = list(string)
}
