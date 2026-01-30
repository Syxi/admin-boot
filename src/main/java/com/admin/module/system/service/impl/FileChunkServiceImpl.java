package com.admin.module.system.service.impl;

import com.admin.common.constant.SystemConstants;
import com.admin.common.enums.FileConvertEnum;
import com.admin.module.system.entity.FileChunk;
import com.admin.module.system.entity.FileRecord;
import com.admin.module.system.mapper.FileChunkMapper;
import com.admin.module.system.service.FileChunkService;
import com.admin.module.system.service.FileRecordService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jodconverter.core.DocumentConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.Resource;
import java.io.*;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static org.jodconverter.core.document.DefaultDocumentFormatRegistry.getFormatByExtension;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileChunkServiceImpl extends ServiceImpl<FileChunkMapper, FileChunk> implements FileChunkService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${file.pdf-dir}")
    private String pdfDir;

    @Resource
    private FileRecordService fileRecordService;

    @jakarta.annotation.Resource
    private DocumentConverter documentConverter;

    @Override
    public boolean checkChunkExists(String identifier, Integer chunkNumber) {
        LambdaQueryWrapper<FileChunk> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FileChunk::getIdentifier, identifier)
                .eq(FileChunk::getChunkNumber, chunkNumber)
                .eq(FileChunk::getUploaded, true);
        return this.count(queryWrapper) > 0;
    }

    @Override
    public boolean uploadChunk(String identifier, Integer chunkNumber, Long totalChunks, Long totalSize, String filename, String chunkHash, MultipartFile file) {
        try {
            // 检查分片是否已存在
            if (checkChunkExists(identifier, chunkNumber)) {
                log.info("分片已存在: {}, chunk: {}", identifier, chunkNumber);
                return true;
            }

            // 创建分片存储路径
            String chunkDir = uploadDir + "/chunks/" + identifier;
            Path chunkPath = Paths.get(chunkDir).resolve(chunkNumber + ".part");

            // 创建目录
            Files.createDirectories(chunkPath.getParent());

            // 保存分片文件
            Files.write(chunkPath, file.getBytes());

            // 保存分片记录到数据库
            FileChunk fileChunk = new FileChunk();
            fileChunk.setIdentifier(identifier);
            fileChunk.setFilename(filename);
            fileChunk.setTotalSize(totalSize);
            fileChunk.setTotalChunks(Math.toIntExact(totalChunks));
            fileChunk.setChunkNumber(chunkNumber);
            fileChunk.setChunkSize(file.getSize());
            fileChunk.setChunkHash(chunkHash);
            fileChunk.setChunkPath(chunkPath.toString());
            fileChunk.setUploaded(true);
            fileChunk.setCreateTime(LocalDateTime.now());
            fileChunk.setUpdateTime(LocalDateTime.now());

            return this.save(fileChunk);
        } catch (Exception e) {
            log.error("上传分片失败: {}, chunk: {}", identifier, chunkNumber, e);
            return false;
        }
    }

    @Override
    public boolean mergeChunks(String identifier, String filename, String fileMd5, String fileType, String description) {
        try {
            // 获取所有分片
            List<FileChunk> chunks = this.baseMapper.selectChunksByIdentifier(identifier);
            if (chunks.isEmpty()) {
                log.warn("没有找到分片: {}", identifier);
                return false;
            }

            // 按分片号排序
            chunks.sort((c1, c2) -> Integer.compare(c1.getChunkNumber(), c2.getChunkNumber()));

            // 构建最终文件路径
            String fileExtension = getFileExtension(filename);
            String hashFileName = fileMd5 + "." + fileExtension;
            Path finalFilePath = Paths.get(uploadDir).resolve(hashFileName);
            Path finalPdfPath = Paths.get(pdfDir).resolve(fileMd5 + ".pdf");

            // 创建目录
            Files.createDirectories(finalFilePath.getParent());
            Files.createDirectories(finalPdfPath.getParent());

            // 合并分片
            try (FileOutputStream fos = new FileOutputStream(finalFilePath.toFile());
                 BufferedOutputStream bos = new BufferedOutputStream(fos)) {

                for (FileChunk chunk : chunks) {
                    Path chunkPath = Paths.get(chunk.getChunkPath());
                    if (Files.exists(chunkPath)) {
                        byte[] chunkData = Files.readAllBytes(chunkPath);
                        bos.write(chunkData);
                    }
                }
                bos.flush();
            }

            // 保存文件记录到数据库
            FileRecord fileRecord = new FileRecord();
            fileRecord.setFileMd5(fileMd5);
            fileRecord.setFileName(filename);
            
            // 文件大小，转换成只有2位小数的MB单位
            long fileSizeBytes = Files.size(finalFilePath);
            BigDecimal fileSize = new BigDecimal(fileSizeBytes).divide(new BigDecimal(1024 * 1024));
            String fileSizeInMb = fileSize.setScale(2, BigDecimal.ROUND_HALF_UP).toString() + SystemConstants.FILE_SIZE_MB;
            fileRecord.setFileSize(fileSizeInMb);

            fileRecord.setFileType(fileType);
            fileRecord.setFileStoragePath(finalFilePath.toString());
            fileRecord.setPdfStoragePath(finalPdfPath.toString());
            fileRecord.setFileDescription(description);
            fileRecord.setFileConversionStatus(FileConvertEnum.UNCONVERTED.getValue());
            fileRecord.setCreateTime(LocalDateTime.now());
            
            boolean saved = fileRecordService.save(fileRecord);

            if (saved) {
                // 如果是PDF文件，直接复制
                if (fileExtension.equalsIgnoreCase("pdf")) {
                    Files.copy(finalFilePath, finalPdfPath);
                } else {
                    // 异步转换为PDF
                    CompletableFuture.runAsync(() -> {
                        try {
                            convertToPDF(finalFilePath, finalPdfPath, filename, fileRecord.getId());
                        } catch (Exception e) {
                            log.error("PDF转换失败: {}", e.getMessage());
                        }
                    });
                }

                // 删除临时分片文件
                deleteTempChunks(identifier);
                
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            log.error("合并分片失败: {}", identifier, e);
            return false;
        }
    }

    @Override
    public List<Integer> getUploadedChunks(String identifier) {
        List<FileChunk> chunks = this.baseMapper.selectChunksByIdentifier(identifier);
        return chunks.stream()
                .map(FileChunk::getChunkNumber)
                .sorted()
                .collect(Collectors.toList());
    }

    @Override
    public boolean deleteTempChunks(String identifier) {
        try {
            // 删除数据库中的分片记录
            int deletedCount = this.baseMapper.deleteChunksByIdentifier(identifier);
            
            // 删除临时分片文件
            String chunkDir = uploadDir + "/chunks/" + identifier;
            Path chunkPath = Paths.get(chunkDir);
            if (Files.exists(chunkPath)) {
                Files.walk(chunkPath)
                        .filter(Files::isRegularFile)
                        .forEach(path -> {
                            try {
                                Files.delete(path);
                            } catch (IOException e) {
                                log.error("删除临时分片文件失败: {}", path, e);
                            }
                        });
                
                // 删除目录
                try {
                    Files.delete(chunkPath);
                } catch (IOException e) {
                    log.error("删除分片目录失败: {}", chunkPath, e);
                }
            }
            
            log.info("删除临时分片: identifier={}, deletedCount={}", identifier, deletedCount);
            return true;
        } catch (Exception e) {
            log.error("删除临时分片失败: {}", identifier, e);
            return false;
        }
    }

    private String getFileExtension(String fileName) {
        if (StringUtils.isBlank(fileName)) {
            return "";
        }
        int lastDotIndex = fileName.lastIndexOf(".");
        return (lastDotIndex != -1) ? fileName.substring(lastDotIndex + 1).toLowerCase() : "";
    }

    /**
     * 上传文件转换成 pdf
     * @param inputFile
     * @param outputFile
     * @param originalFileName
     */
    private void convertToPDF(Path inputFile, Path outputFile, String originalFileName, Long id) {
        try {
            String fileExtension = getFileExtension(originalFileName);
            org.jodconverter.core.document.DocumentFormat inputFormat = 
                org.jodconverter.core.document.DefaultDocumentFormatRegistry.getFormatByExtension(fileExtension);
            if (inputFormat == null) {
                log.info("不支持的文件格式，无法转换为PDF: {}", originalFileName);
                // 更新数据库中的转换状态为失败
                FileRecord fileRecord = new FileRecord();
                fileRecord.setId(id);
                fileRecord.setFileConversionStatus(FileConvertEnum.FAIL.getValue());
                fileRecordService.updateById(fileRecord);
                return; // 不支持转换的文件类型
            }

            documentConverter
                    .convert(inputFile.toFile())
                    .as(inputFormat)
                    .to(outputFile.toFile())
                    .as(org.jodconverter.core.document.DefaultDocumentFormatRegistry.PDF)
                    .execute();

            log.info("convert file {} to pdf success", originalFileName);

            // 更新数据库中的转换状态
            FileRecord fileRecord = new FileRecord();
            fileRecord.setId(id);
            fileRecord.setFileConversionStatus(FileConvertEnum.SUCCESS.getValue());
            fileRecordService.updateById(fileRecord);
        } catch (Exception e) {
            log.error("文件转换失败： {}", e.getMessage());
            // 更新数据库中的转换状态为失败
            FileRecord fileRecord = new FileRecord();
            fileRecord.setId(id);
            fileRecord.setFileConversionStatus(FileConvertEnum.FAIL.getValue());
            fileRecordService.updateById(fileRecord);
        }
    }
}