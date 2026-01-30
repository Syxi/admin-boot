CREATE TABLE IF NOT EXISTS `file_chunk` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '分片ID',
  `identifier` varchar(255) NOT NULL COMMENT '文件标识符（通常使用文件的MD5）',
  `filename` varchar(255) NOT NULL COMMENT '文件名',
  `total_size` bigint DEFAULT NULL COMMENT '文件总大小',
  `total_chunks` int DEFAULT NULL COMMENT '总分片数',
  `chunk_number` int DEFAULT NULL COMMENT '当前分片索引',
  `chunk_size` bigint DEFAULT NULL COMMENT '当前分片大小',
  `chunk_hash` varchar(255) DEFAULT NULL COMMENT '当前分片的MD5',
  `chunk_path` varchar(500) DEFAULT NULL COMMENT '当前分片的存储路径',
  `uploaded` tinyint(1) DEFAULT '0' COMMENT '文件是否已上传完成',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_identifier` (`identifier`),
  KEY `idx_chunk_number` (`chunk_number`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='文件分片表';