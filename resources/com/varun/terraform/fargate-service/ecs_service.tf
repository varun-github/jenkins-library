terraform {
  backend "s3" {
    bucket = ""
    key = "aws/ecs-service/sample-service"
    region = ""
    profile = ""
  }
  required_version = ">= 0.12"
}
provider "aws" {
  region = ""
  profile = ""
}

# data "aws_secretsmanager_secret" "db_secret" {
#   name = var.db_secret_name
# }
data "aws_iam_role" "service_iam_role" {
  name = "ecsTaskExecutionRole"
}
resource "aws_ecs_task_definition" "service_td" {
  family = "${var.service_name}-family"
  container_definitions = jsonencode(
    [
      {
        name      = var.service_name
        image     = var.image_name
        essential = true
        portMappings = var.port_mappings
        # secrets = var.secrets
        environment = var.environment
      }
    ]
  )
  cpu = var.cpu
  memory = var.memory
  task_role_arn = data.aws_iam_role.service_iam_role.arn
  requires_compatibilities = [ "FARGATE" ]
  network_mode = "awsvpc"
  execution_role_arn = data.aws_iam_role.service_iam_role.arn
  tags = var.tags
}

resource "aws_lb_target_group" "service_tg" {
  name     = "${var.service_name}-tg"
  port     = 8080
  protocol = "HTTP"
  vpc_id   = var.vpc_id
  target_type = "ip"
  tags = var.tags
  health_check {
    path = "/welcome"
    matcher = "200-299"
    enabled = true
    healthy_threshold = 3
    unhealthy_threshold = 3
  }
  
}
resource "aws_lb_listener_rule" "service_rule" {
  listener_arn = var.lb_listener_arn
  priority     = 200

  action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.service_tg.arn
  }

  condition {
    path_pattern {
      values = var.listener_routing_rule_pattern
    }
  }
  tags = var.tags
}
data "aws_ecs_cluster" "service_cluster" {
  cluster_name = var.cluster_name
}
resource "aws_ecs_service" "ecs_service" {
  name            = var.service_name
  cluster         = data.aws_ecs_cluster.service_cluster.arn
  task_definition = aws_ecs_task_definition.service_td.arn
  desired_count   = var.service_desired_count
  launch_type = "FARGATE"


  load_balancer {
    target_group_arn = aws_lb_target_group.service_tg.arn
    container_name   = var.service_name
    container_port   = 8080
  }
  network_configuration {
    subnets = var.subnets
    security_groups = var.security_groups
    assign_public_ip = false
  }
  tags = var.tags
}


# resource "aws_s3_bucket" "state_bucket" {
#   bucket = "cloudstudio-tf-state"
#   acl    = "private"
#   versioning {
#     enabled = true
#   }
#   tags = var.tags
# }
