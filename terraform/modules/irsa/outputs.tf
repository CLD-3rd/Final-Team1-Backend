output "alb_irsa_role_arn" {
  description = "IAM Role ARN for ALB Controller ServiceAccount"
  value       = aws_iam_role.alb_irsa.arn
}
