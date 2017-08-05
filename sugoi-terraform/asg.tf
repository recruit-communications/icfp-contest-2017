resource "aws_launch_configuration" "sugoi-taisen" {
  name                        = "sugoi-taisen_${replace(var.ec2_sugoi_taisen_id, "ami-", "")}"
  image_id                    = "${var.ec2_sugoi_taisen_id}"
  instance_type               = "t2.medium"
  key_name                    = "sugoi-key"
  user_data                   = ""
  security_groups             = ["sg-06da4d60"]
  associate_public_ip_address = true

  root_block_device = {
    volume_type = "gp2"
    volume_size = 30
  }

  lifecycle {
    create_before_destroy = true
  }
}

resource "aws_autoscaling_group" "sugoi-taisen" {
  availability_zones        = ["ap-northeast-1a", "ap-northeast-1c"]
  name                      = "sugoi-taisen"
  max_size                  = 10
  min_size                  = 0
  health_check_grace_period = 300
  health_check_type         = "EC2"
  vpc_zone_identifier       = ["subnet-e7414da1", "subnet-6bf9101c"]
  force_delete              = true
  launch_configuration      = "${aws_launch_configuration.sugoi-taisen.name}"

  tag {
    key                 = "Name"
    value               = "sugoi-taisen"
    propagate_at_launch = true
  }

  tag {
    key                 = "ict:srv"
    value               = "icfpc"
    propagate_at_launch = true
  }
}
