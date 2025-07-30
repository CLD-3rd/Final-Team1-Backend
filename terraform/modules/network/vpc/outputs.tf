output "vpc_id" {
  description = "생성된 VPC의 ID"
  value       = aws_vpc.main.id
}