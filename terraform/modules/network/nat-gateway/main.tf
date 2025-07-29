resource "aws_eip" "this" {
  domain = "vpc"

  tags = {
    Name        = "${var.name_prefix}-nat-eip"
    Environment = var.environment
  }
}

resource "aws_nat_gateway" "this" {
  allocation_id = aws_eip.this.id
  subnet_id     = var.public_subnet_id
  depends_on    = [var.igw_id] # IGW가 먼저 있어야 NAT이 동작함

  tags = {
    Name        = "${var.name_prefix}-nat-gw"
    Environment = var.environment
  }
}

resource "aws_route_table" "private" {
  vpc_id = var.vpc_id

  route {
    cidr_block     = "0.0.0.0/0"
    nat_gateway_id = aws_nat_gateway.this.id
  }

  tags = {
    Name        = "${var.name_prefix}-private-rt"
    Environment = var.environment
  }
}

resource "aws_route_table_association" "private" {
  count          = length(var.private_subnet_ids)
  subnet_id      = var.private_subnet_ids[count.index]
  route_table_id = aws_route_table.private.id
}
