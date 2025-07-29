
# eks
module "eks" {
  source                = "./modules/eks"
  team_name             = var.team_name
  subnet_ids            = module.subnet.public_subnet_ids
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
}


#iam
module "iam" {
  source     = "./modules/iam"
  team_name  = var.team_name
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
  public_subnet_cidrs  = ["192.168.1.0/24"]
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


