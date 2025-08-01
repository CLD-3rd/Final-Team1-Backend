resource "aws_subnet" "public" {
  count                   = length(var.public_subnet_cidrs)
  vpc_id                  = var.vpc_id
  cidr_block              = var.public_subnet_cidrs[count.index]
  availability_zone       = var.azs[count.index]
  map_public_ip_on_launch = true

  tags = {
    Name        = "${var.name_prefix}-public-${count.index}"
    Environment = var.environment
    Tier        = "public"
        # ALB Controller용 태그
    "kubernetes.io/role/elb"            = "1"
    "kubernetes.io/cluster/Team1-backend-eks-cluster" = "shared"
  }
}

resource "aws_subnet" "private" {
  count             = length(var.private_subnet_cidrs)
  vpc_id            = var.vpc_id
  cidr_block        = var.private_subnet_cidrs[count.index]
  availability_zone = element(var.azs, count.index % length(var.azs))

  tags = {
    Name        = "${var.name_prefix}-private-${count.index}"
    Environment = var.environment
    Tier        = "private"
    type        = count.index < length(var.azs) ? "app" : "data"
  }
}
