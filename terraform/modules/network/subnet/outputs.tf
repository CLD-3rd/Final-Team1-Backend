output "public_subnet_ids" {
  value       = aws_subnet.public[*].id
  description = "Public Subnet ID 리스트"
}

output "private_subnet_ids" {
  value       = aws_subnet.private[*].id
  description = "Private Subnet ID 리스트"
}
