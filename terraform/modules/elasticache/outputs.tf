
output "port" {
  description = "ElastiCache cluster port"
  value       = aws_elasticache_cluster.this.port
}

output "cluster_id" {
  description = "ElastiCache cluster ID"
  value       = aws_elasticache_cluster.this.id
}

output "endpoint" {
  description = "ElastiCache cluster endpoint"
  value       = aws_elasticache_cluster.this.cache_nodes[0].address
}


output "subnet_group_name" {
  description = "ElastiCache subnet group name"
  value       = aws_elasticache_subnet_group.this.name
}

output "elasticache_sg_id" {
  value = aws_security_group.elasticache_sg.id
}