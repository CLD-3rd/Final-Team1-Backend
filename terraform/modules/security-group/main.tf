# Bastion EC2 인스턴스를 위한 보안 그룹
resource "aws_security_group" "bastion" {
  name        = "${var.team_name}-bastion-sg"
  description = "Security group for Bastion host"
  vpc_id      = var.vpc_id

  ingress {
    description = "Allow SSH from anywhere"
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

# InfluxDB HTTP API (8086) 허용 룰 추가
  ingress {
    description = "Allow InfluxDB HTTP API"
    from_port   = 8086
    to_port     = 8086
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    description = "Allow all outbound traffic"
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "${var.team_name}-bastion-sg"
  }
}

# EKS 노드 그룹을 위한 보안 그룹
resource "aws_security_group" "eks_node" {
  name        = "${var.team_name}-eks-node-sg"
  description = "Security group for EKS worker nodes"
  vpc_id      = var.vpc_id

  ingress {
    description = "Allow all traffic between nodes"
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    self        = true
  }

  egress {
    description = "Allow all outbound traffic"
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "${var.team_name}-eks-node-sg"
    "karpenter.sh/discovery" = var.cluster_name
  }
}

# EKS 컨트롤 플레인을 위한 보안 그룹
resource "aws_security_group" "eks_control_plane" {
  name        = "${var.team_name}-eks-control-sg"
  description = "Security group for EKS control plane"
  vpc_id      = var.vpc_id

  ingress {
    description     = "Allow Bastion to connect to EKS API server"
    from_port       = 443
    to_port         = 443
    protocol        = "tcp"
    security_groups = [aws_security_group.bastion.id]
  }

  egress {
    description = "Allow all outbound traffic"
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "${var.team_name}-eks-control-sg"
  }
}
