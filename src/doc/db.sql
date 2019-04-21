CREATE TABLE `application` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `appKey` varchar(20) NOT NULL COMMENT 'appkey',
  `appName` varchar(20) DEFAULT NULL COMMENT 'app名称',
  `port` int(11) NOT NULL COMMENT 'app端口',
  `description` varchar(255) DEFAULT NULL COMMENT 'app描述',
  `status` varchar(20) DEFAULT NULL COMMENT 'app状态',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `relays` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `appId` int(11) DEFAULT NULL COMMENT 'application主键',
  `inUrl` varchar(512) DEFAULT NULL COMMENT 'inbound uri',
  `inMethod` varchar(512) DEFAULT NULL COMMENT 'inbound method',
  `outUrl` varchar(512) DEFAULT NULL COMMENT 'outbound url',
  `outMethod` varchar(512) DEFAULT NULL COMMENT 'outbound method',
  `transmission` tinyint(1) DEFAULT NULL COMMENT '是否透传body',
  `status` varchar(512) DEFAULT NULL COMMENT 'relay状态',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `parampair` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `relayId` int(11) DEFAULT NULL COMMENT 'relayId',
  `inName` varchar(20) DEFAULT NULL COMMENT '入参名称',
  `inType` varchar(20) DEFAULT NULL COMMENT '入参类型',
  `outName` varchar(20) DEFAULT NULL COMMENT '出参名称',
  `outType` varchar(20) DEFAULT NULL COMMENT '出参类型',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;