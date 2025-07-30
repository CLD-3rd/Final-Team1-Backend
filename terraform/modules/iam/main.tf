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


# 정책 커스텀으로 eks클러스터 접근하게 만들어 버리기
resource "aws_iam_policy" "bastion_eks_admin" {
  name        = "${var.team_name}-eks-bastion-admin"
  description = "Full EKS access for bastion"

  policy = jsonencode({
    Version = "2012-10-17",
    Statement = [
      {
        Effect   = "Allow",
        Action   = [
          "eks:*",
          "iam:PassRole"
        ],
        Resource = "*"
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "bastion_eks_admin_attach" {
  role       = aws_iam_role.bastion.name
  policy_arn = aws_iam_policy.bastion_eks_admin.arn
}


# alb controller irsa role
# OIDC 공급자 참조 (이미 생성된 Provider)
#data "aws_iam_openid_connect_provider" "oidc" {
#  url = var.oidc_url
#}
# ALB Controller용 IAM Role
resource "aws_iam_role" "alb_irsa_role" {
  name = "${var.team_name}-alb-irsa-role"

#  assume_role_policy = jsonencode({
#    Version = "2012-10-17",
#    Statement = [
#      {
#        Effect = "Allow",
#        Principal = {
#          Federated = data.aws_iam_openid_connect_provider.oidc.arn
#        },
#        Action = "sts:AssumeRoleWithWebIdentity",
#        Condition = {
#          StringEquals = {
#            "${replace(var.oidc_url, "https://", "")}:sub" = "system:serviceaccount:argocd:aws-load-balancer-controller"
#          }
#        }
#      }
#    ]
#  })
#}

# ALB Controller에 필요한 정책 연결
resource "aws_iam_policy" "alb_controller_policy" {
  name        = "${var.team_name}-alb-controller-policy"
  description = "Policy for AWS ALB Controller"
  policy      = file("${path.module}/alb-controller-policy.json")
}

resource "aws_iam_role_policy_attachment" "alb_controller_attach" {
  role       = aws_iam_role.alb_irsa_role.name
  policy_arn = aws_iam_policy.alb_controller_policy.arn
}
