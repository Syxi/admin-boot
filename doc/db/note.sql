-- 笔记表
CREATE TABLE `t_note` (
  `note_id` bigint NOT NULL AUTO_INCREMENT COMMENT '笔记ID',
  `title` varchar(200) NOT NULL COMMENT '笔记标题',
  `content` text NOT NULL COMMENT '笔记内容',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：1-正常，-1-禁用',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '删除标记：0-未删除，1-已删除',
  PRIMARY KEY (`note_id`),
  KEY `idx_title` (`title`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='笔记表';
