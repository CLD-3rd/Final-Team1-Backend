resource "aws_elasticache_subnet_group" "this" {
  name       = "team1-elasticache-subnet-group"
  subnet_ids = var.subnet_ids
}

resource "aws_security_group" "elasticache_sg" {
  name        = "team1-elasticache-sg"
  description = "Security group for Redis"
  vpc_id      = var.vpc_id

  ingress {
    from_port   = 6379
    to_port     = 6379
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"] # TODO: private ip로 변경
    description = "Allow Redis access from private networks"
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

resource "aws_elasticache_cluster" "this" {
  cluster_id           = var.name
  engine               = "redis"
  engine_version       = var.engine_version
  node_type            = var.node_type
  num_cache_nodes      = var.num_nodes
  parameter_group_name = var.parameter_group_name
  port                 = var.port
  subnet_group_name    = aws_elasticache_subnet_group.this.name
  security_group_ids   = var.security_group_ids
}