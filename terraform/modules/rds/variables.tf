variable "vpc_id" {
  description = "VPC ID for RDS"
  type        = string
}

variable "db_subnet_ids" {
  description = "Subnets for RDS subnet group"
  type        = list(string)
}

variable "allowed_cidrs" {
  description = "CIDR blocks allowed to access RDS"
  type        = list(string)
  default     = ["10.0.0.0/16"]
}

variable "db_name" {
  description = "Initial database name"
  type        = string
  default     = "mindscape"
}

variable "db_username" {
  description = "Master username for RDS"
  type        = string
  default     = "root"  
}

variable "db_password" {
  description = "Master password for RDS"
  type        = string
  sensitive   = true
}

variable "identifier" {
  description = "rds identifier"
  default = "team1-rds-identifier"
}

variable "engine" {
  description = "Database engine"
  type        = string
  default     = "mysql"
}

variable "engine_version" {
  description = "Database engine version"
  type        = string
  default     = "8.0"
}

variable "instance_class" {
  description = "RDS instance class"
  type        = string
  default     = "db.m7g.large"
}
variable "allocated_storage" {
  description = "Initial allocated storage in GB"
  type        = number
  default     = 20
}