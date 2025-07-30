output "rds_endpoint" {
  value = aws_db_instance.rds.endpoint
}

output "rds_port" {
  description = "The port of the RDS instance"
  value       = aws_db_instance.rds.port
}


output "rds_db_name" {
  description = "The name of the default database"
  value       = aws_db_instance.rds.db_name
}

output "rds_username" {
  description = "The master username for the RDS instance"
  value       = aws_db_instance.rds.username
}

output "rds_security_group_id" {
  description = "The security group ID attached to the RDS instance"
  value       = aws_security_group.rds.id
}