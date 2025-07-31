# eks cluster
resource "aws_iam_role" "eks_cluster" {
  name = "${var.team_name}-eks-cluster-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17",
    Statement = [{
      Effect = "Allow"
      Principal = {
        Service = "eks.amazonaws.com"
      }
      Action = "sts:AssumeRole"
    }]
  })
}

resource "aws_iam_role_policy_attachment" "eks_cluster_attach" {
  role       = aws_iam_role.eks_cluster.name    # eks_cluster 역할에
  policy_arn = "arn:aws:iam::aws:policy/AmazonEKSClusterPolicy"
}

#node group
resource "aws_iam_role" "eks_node" {
  name = "${var.team_name}-eks-node-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17",
    Statement = [{
      Effect = "Allow"
      Principal = {
        Service = "ec2.amazonaws.com"
      }
      Action = "sts:AssumeRole"
    }]
  })
}

resource "aws_iam_role_policy_attachment" "eks_worker_AmazonEKSWorkerNodePolicy" {
  role       = aws_iam_role.eks_node.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonEKSWorkerNodePolicy"
}

resource "aws_iam_role_policy_attachment" "eks_cni_AmazonEKSCNIPolicy" {
  role       = aws_iam_role.eks_node.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonEKS_CNI_Policy"
}

resource "aws_iam_role_policy_attachment" "eks_ec2_AmazonEC2ContainerRegistryReadOnly" {
  role       = aws_iam_role.eks_node.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonEC2ContainerRegistryReadOnly"
}

#bastion
resource "aws_iam_role" "bastion" {
  name = "${var.team_name}-bastion-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17",
    Statement = [
      {
        Effect = "Allow",
        Principal = {
          Service = "ec2.amazonaws.com"
        },
        Action = "sts:AssumeRole"
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "bastion_eks" {
  role       = aws_iam_role.bastion.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonEKSClusterPolicy"
}

resource "aws_iam_role_policy_attachment" "bastion_ssm" {
  role       = aws_iam_role.bastion.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonSSMManagedInstanceCore"
}

resource "aws_iam_role_policy_attachment" "bastion_ec2" {
  role       = aws_iam_role.bastion.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonEC2ReadOnlyAccess"
}

resource "aws_iam_role_policy_attachment" "bastion_rds" {
  role       = aws_iam_role.bastion.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonRDSReadOnlyAccess"
}

resource "aws_iam_role_policy_attachment" "bastion_elasticache" {
  role       = aws_iam_role.bastion.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonElastiCacheReadOnlyAccess"
}

resource "aws_iam_role_policy_attachment" "bastion_cloudwatch" {
  role       = aws_iam_role.bastion.name
  policy_arn = "arn:aws:iam::aws:policy/CloudWatchReadOnlyAccess"
}

resource "aws_iam_instance_profile" "bastion" {
  name = "${var.team_name}-bastion-profile"
  role = aws_iam_role.bastion.name
}

resource "aws_iam_role_policy_attachment" "bastion_eks_attach" {
  role       = aws_iam_role.bastion.name        # bastion 역할에
  policy_arn = "arn:aws:iam::aws:policy/AmazonEKSClusterPolicy"
}

# 1) 커스텀 읽기 전용 EKS 정책 생성
resource "aws_iam_policy" "bastion_eks_readonly" {
  name        = "${var.team_name}-eks-bastion-readonly"
  description = "Allow Bastion to describe and list EKS clusters"

  policy = jsonencode({
    Version = "2012-10-17",
    Statement = [{
      Effect   = "Allow",
      Action   = [
        "eks:DescribeCluster",
        "eks:ListClusters"
      ],
      Resource = "*"
    }]
  })
}

# 2) Bastion 역할에 방금 만든 정책 붙이기
resource "aws_iam_role_policy_attachment" "bastion_eks_readonly_attach" {
  role       = aws_iam_role.bastion.name
  policy_arn = aws_iam_policy.bastion_eks_readonly.arn
}

# node group iam 권한
resource "aws_iam_role_policy_attachment" "eks_node_amazon_ebs_csi_driver" {
  role       = aws_iam_role.eks_node.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonEBSCSIDriverPolicy"
}
