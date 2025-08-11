output "nat_gateway_id" {
  value       = aws_nat_gateway.this.id
  description = "NAT Gateway ID"
}

output "private_route_table_id" {
  value       = aws_route_table.private.id
  description = "Private Route Table ID"
}
