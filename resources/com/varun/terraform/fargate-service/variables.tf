variable "tags" {
  type        = map(string)
}
variable "aws_region" {
  type = string
}
variable "vpc_id" {
  type = string
}
variable "security_groups" {
  type = list(string)
}
variable "subnets" {
  type = list(string)
}
variable "service_name" {
  type = string
}
variable "cluster_name" {
  type = string
}
variable "service_desired_count" {
  type = number
  default = 1
}
variable "secrets" {
  type = list(object({
    name      = string
    valueFrom = string
  }))
  description = "The secrets to pass to the container. This is a list of maps"
  default     = []
}
variable "port_mappings" {
  type = list(object({
    containerPort = number
    hostPort      = number
    protocol      = string
  }))
}
variable "environment" {
  type = list(object({
    name  = string
    value = string
  }))
  description = "The environment variables to pass to the container. This is a list of maps. map_environment overrides environment"
  default     = []
}

variable "cpu" {
  type = number
  default = 512
}
variable "memory" {
  type = number
  default = 1024
}
variable "image_name" {
  type = string
}
variable "lb_listener_arn" {
  type = string
}
variable "listener_routing_rule_pattern" {
  type = list(string)
}

