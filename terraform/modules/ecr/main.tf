resource "aws_ecr_repository" "msa_services" {
  for_each = toset(var.services)
  name                 = "${var.team_name}-${each.key}"
  image_tag_mutability = "MUTABLE"

  image_scanning_configuration {
    scan_on_push = true
  }

  tags = {
    Name = "${var.team_name}-${each.key}"
  }
}