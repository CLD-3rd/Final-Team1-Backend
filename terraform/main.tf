
# eks
module "eks" {
  source                = "./modules/eks"
  team_name             = var.team_name
  subnet_ids = module.subnet.private_subnet_ids
  cluster_iam_role_arn  = module.iam.eks_cluster_role_arn
  node_iam_role_arn     = module.iam.eks_node_role_arn
}

# ec2 bastion
module "bastion" {
  source                    = "./modules/bastion"
  team_name                 = var.team_name
  ami_id                    = var.ami_id
  instance_type             = "t3.medium"
  public_subnet_id          = module.subnet.public_subnet_ids[0]
  security_group_id         = module.sg.bastion_sg_id
  iam_instance_profile_name = module.iam.bastion_instance_profile_name
  cluster_name              = module.eks.cluster_name
  eks_node_role_arn        = module.iam.eks_node_role_arn
  bastion_role_arn         = module.iam.bastion_role_arn
  key_name                  = var.bastion_key_name

    depends_on = [
    module.eks,         # 클러스터 먼저 생성
    module.iam          # IAM 역할도 먼저 있어야 함
  ]
}

#iam
module "iam" {
  source     = "./modules/iam"
  team_name  = var.team_name
  cluster_name = module.eks.cluster_name
  oidc_url     = module.eks.oidc_url
}


module "alb_irsa" {
  source               = "./modules/irsa"
  team_name            = var.team_name
  oidc_url             = module.eks.oidc_url
  cluster_name         = module.eks.cluster_name
  namespace            = "argocd"
  service_account_name = "aws-load-balancer-controller"

  depends_on = [module.eks]
}

module "ebs_csi_irsa" {
  source               = "./modules/irsa"
  team_name            = var.team_name
  oidc_url             = module.eks.oidc_url
  cluster_name         = module.eks.cluster_name
  namespace            = "kube-system"
  service_account_name = "ebs-csi-controller-sa"

  depends_on = [module.eks]
}

module "ebs_csi_driver" {
  source              = "./modules/ebs-csi-driver"
  namespace           = "kube-system"
  chart_version       = "2.30.0"
  service_account_name = "ebs-csi-controller-sa"
  irsa_role_arn       = module.ebs_csi_irsa.ebs_csi_irsa_role_arn

  providers = {
    helm.eks = helm.eks
  }

  depends_on = [
    module.ebs_csi_irsa,
    module.eks
  ]
}

#security group
module "sg" {
  source     = "./modules/security-group"
  vpc_id     = module.vpc.vpc_id
  team_name  = var.team_name
}

#vpc
module "vpc" {
  source      = "./modules/network/vpc"
  name_prefix = var.team_name
  environment = "dev"
  vpc_cidr    = "192.168.0.0/16"
}

#subnet
module "subnet" {
  source               = "./modules/network/subnet"
  name_prefix          = var.team_name
  environment          = "dev"
  vpc_id               = module.vpc.vpc_id
  public_subnet_cidrs  = ["192.168.1.0/24", "192.168.4.0/24"]
  private_subnet_cidrs = ["192.168.2.0/24", "192.168.3.0/24"]
  azs                  = ["ap-northeast-2a", "ap-northeast-2c"]
}
  
# route table
module "route_table" {
  source            = "./modules/network/route-table"
  name_prefix       = var.team_name
  environment       = "dev"
  vpc_id            = module.vpc.vpc_id
  igw_id            = module.internet_gateway.igw_id
  public_subnet_ids = module.subnet.public_subnet_ids
}

# nat gateway
module "nat_gateway" {
  source             = "./modules/network/nat-gateway"
  name_prefix        = var.team_name
  environment        = "dev"
  vpc_id             = module.vpc.vpc_id
  igw_id             = module.internet_gateway.igw_id
  public_subnet_id   = module.subnet.public_subnet_ids[0]
  private_subnet_ids = module.subnet.private_subnet_ids
}
    

# internet gateway
module "internet_gateway" {
  source      = "./modules/network/internet-gateway"
  name_prefix = var.team_name
  environment = "dev"
  vpc_id      = module.vpc.vpc_id
}

# config 설정 뒤에 aws-auth 연결되게 설정 
# resource "null_resource" "wait_for_kubeconfig" {
#   provisioner "local-exec" {
#     command = <<EOT
# aws ssm send-command \
#   --document-name "AWS-RunShellScript" \
#   --instance-ids "${module.bastion.instance_id}" \
#   --region ap-northeast-2 \
#   --comment "Check for kubeconfig" \
#   --parameters 'commands=["until [ -f /home/ubuntu/.kube/config ]; do sleep 3; done"]' \
#   --output text
# EOT
#   }
# }

#ebs 스토리지 클래스
module "ebs_storage_class" {
  source = "./modules/storageclass"

  name          = "ebs-sc"
  volume_type   = "gp3"
  fs_type       = "ext4"
  reclaim_policy = "Delete"
  binding_mode  = "WaitForFirstConsumer"

    depends_on = [
    module.ebs_csi_irsa,
    module.eks,
    module.ebs_csi_driver
  ]
}

# argocd 모듈 및 네임스페이스

module "argocd_namespace" {
  source  = "./modules/namespace"
  name    = "argocd"
  labels = {
    "managed-by" = "terraform"
  }

  providers = {
    kubernetes.eks = kubernetes.eks 
  }

    depends_on = [
    module.eks,
    module.bastion
  ]
}

module "argocd" {
  source        = "./modules/argocd"
  namespace     = module.argocd_namespace.name
  chart_version = "5.51.6"
  providers = {
    helm = helm.eks
    kubernetes = kubernetes.eks
  }

    depends_on = [
    module.eks,
    module.bastion,
    module.argocd_namespace
  ]
}

# 프로메테오스 모듈 및 네임스페이스

module "prometheus_namespace" {
  source  = "./modules/namespace"
  name    = "prometheus"
  labels = {
    "managed-by" = "terraform"
  }

  providers = {
    kubernetes.eks = kubernetes.eks 
  }
    depends_on = [
    module.eks,
    module.bastion,
    module.ebs_csi_driver,
    module.ebs_storage_class
  ]
}

module "prometheus" {
  source        = "./modules/monitoring/prometheus"
  namespace     = module.prometheus_namespace.name
  chart_version = "25.21.0"

  providers = {
    helm       = helm.eks
    kubernetes = kubernetes.eks
  }

  depends_on = [
    module.eks,
    module.bastion,
    module.prometheus_namespace,
    module.ebs_csi_driver,
    module.ebs_storage_class
  ]

}

# 그라파나 모듈 및 네임스페이스
module "grafana_namespace" {
  source  = "./modules/namespace"
  name    = "grafana"
  labels = {
    "managed-by" = "terraform"
  }

  providers = {
    kubernetes.eks = kubernetes.eks 
  }
    depends_on = [
    module.eks,
    module.bastion
  ]
}

module "grafana" {
  source        = "./modules/monitoring/grafana"
  namespace     = module.grafana_namespace.name
  chart_version = "7.3.11"

  providers = {
    helm       = helm.eks
    kubernetes = kubernetes.eks
  }

    depends_on = [
    module.eks,
    module.bastion,
    module.grafana_namespace
  ]

}

# Elasticache
module "elasticache" {
  source              = "./modules/elasticache"
  vpc_id              = module.vpc.vpc_id
  name                = "team1-redis"
  subnet_ids          = module.subnet.private_subnet_ids
  security_group_ids  = [module.elasticache.elasticache_sg_id]
  node_type           = "cache.m4.large"
  num_nodes           = 1
  port                = 6379
  engine_version      = "7.0"
  parameter_group_name = "default.redis7"
}

# RDS
module "rds" {
  source           = "./modules/rds"
  vpc_id           = module.vpc.vpc_id
  db_subnet_ids    = module.subnet.private_subnet_ids
  db_name          = "mindscape"
  db_username      = "root"
  db_password      = var.db_password
}

# ECR
module "ecr" {
  source     = "./modules/ecr"
}
