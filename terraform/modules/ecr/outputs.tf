output "ecr_repositories" {
  description = "ECR repository URLs for each service"
  value = {
    for service, repo in aws_ecr_repository.msa_services :
    service => repo.repository_url
  }
}