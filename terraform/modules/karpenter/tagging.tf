resource "aws_ec2_tag" "karpenter_subnet_tags" {
  for_each = {
    for idx, subnet_id in var.subnet_ids :
    "subnet-${idx}" => subnet_id
  }

  resource_id = each.value
  key         = "karpenter.sh/discovery"
  value       = var.cluster_name
}