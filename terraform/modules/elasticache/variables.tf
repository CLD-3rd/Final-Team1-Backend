variable "name" {
  description = "ElastiCache cluster name"
  type        = string
  default     = "team1-elasticache-cluster"
}
variable "vpc_id" {
  description = "VPC ID for security group"
  type        = string
}
variable "subnet_ids" {
  description = "List of subnet IDs for subnet group"
  type        = list(string)
}

variable "security_group_ids" {
  description = "List of security group IDs"
  type        = list(string)
}

variable "node_type" {
  description = "ElastiCache instance type"
  type        = string
  default     = "cache.m4.large"
}

variable "num_nodes" {
  description = "Number of cache nodes"
  type        = number
  default     = 1
}

variable "port" {
  description = "Redis port"
  type        = number
  default     = 6379
}

variable "engine_version" {
  description = "Redis engine version"
  type        = string
  default     = "7.0"
}

variable "parameter_group_name" {
  description = "Parameter group name"
  type        = string
  default     = "default.redis7"
}
