variable "team_name" {
  description = "Team name prefix for repositories"
  type        = string
  default     = "team1-mindscape"
}

variable "services" {
  description = "Services to create ECR repositories"
  type        = list(string)
  default     = ["auth", "info", "api"]
}