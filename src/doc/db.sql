
CREATE TABLE `application` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `app_key` varchar(20) NOT NULL COMMENT 'appkey',
  `app_name` varchar(20) DEFAULT NULL COMMENT 'app名称',
  `app_port` int(11) NOT NULL COMMENT 'app端口',
  `description` varchar(255) DEFAULT NULL COMMENT 'app描述',
  `app_status` varchar(20) DEFAULT NULL COMMENT 'app状态',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8;

CREATE TABLE `relay` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `app_id` int(11) DEFAULT NULL COMMENT 'application主键',
  `in_url` varchar(512) DEFAULT NULL COMMENT 'inbound uri',
  `in_method` varchar(512) DEFAULT NULL COMMENT 'inbound method',
  `out_url` varchar(512) DEFAULT NULL COMMENT 'outbound url',
  `out_method` varchar(512) DEFAULT NULL COMMENT 'outbound method',
  `transmission` tinyint(1) DEFAULT NULL COMMENT '是否透传body',
  `relay_status` varchar(512) DEFAULT NULL COMMENT 'relay状态',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;

CREATE TABLE `parampair` (
  `id` int(11) NOT NULL COMMENT '自增主键',
  `relay_id` int(11) DEFAULT NULL COMMENT 'relayId',
  `in_name` varchar(20) DEFAULT NULL COMMENT '入参名称',
  `in_type` varchar(20) DEFAULT NULL COMMENT '入参类型',
  `out_name` varchar(20) DEFAULT NULL COMMENT '出参名称',
  `out_type` varchar(20) DEFAULT NULL COMMENT '出参类型',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;