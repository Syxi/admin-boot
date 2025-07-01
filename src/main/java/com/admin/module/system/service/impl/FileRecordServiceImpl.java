package com.admin.module.system.service.impl;

import com.admin.common.constant.SystemConstants;
import com.admin.common.enums.FileConvertEnum;
import com.admin.module.system.entity.FileRecord;
import com.admin.module.system.mapper.FileRecordMapper;
import com.admin.module.system.query.FileRecordQuery;
import com.admin.module.system.service.FileRecordService;
import com.admin.module.system.vo.FileRecordVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jodconverter.core.DocumentConverter;
import org.jodconverter.core.document.DefaultDocumentFormatRegistry;
import org.jodconverter.core.office.OfficeException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
* @author sy
* @description 针对表【file_record】的数据库操作Service实现
* @createDate 2024-03-12 11:03:03
*/
@Slf4j
@Service
@RequiredArgsConstructor
public class FileRecordServiceImpl extends ServiceImpl<FileRecordMapper, FileRecord>  implements FileRecordService{

    // 上传文件源目录
    @Value("${file.upload-dir}")
    private String uploadDir;

    // 上传文件转换成pdf保存目录
    @Value("${file.pdf-dir}")
    private String pdfDir;


    @jakarta.annotation.Resource
    private DocumentConverter documentConverter;



    /**
     * 文件列表信息
     *
     * @param fileRecordQuery
     * @return
     */
    @Override
    public IPage<FileRecordVO> selectFilePage(FileRecordQuery fileRecordQuery) {
        LambdaQueryWrapper<FileRecord> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotEmpty(fileRecordQuery.getFileName())) {
            wrapper.like(FileRecord::getFileName, fileRecordQuery.getFileName());
        }
        wrapper.orderByDesc(FileRecord::getCreateTime);


        IPage<FileRecord> page = new Page<>(fileRecordQuery.getPage(), fileRecordQuery.getLimit());
        IPage<FileRecordVO> fileRecordIpage = this.page(page, wrapper).convert(fileRecord -> {
           return this.convertToVO(fileRecord);
        });
        return fileRecordIpage;
    }


    private FileRecordVO convertToVO(FileRecord fileRecord) {
        FileRecordVO fileRecordVO = new FileRecordVO();
        fileRecordVO.setId(fileRecord.getId());
        fileRecordVO.setFileName(fileRecord.getFileName());
        fileRecordVO.setFileMd5(fileRecord.getFileMd5());
        fileRecordVO.setFileSize(fileRecord.getFileSize());
        fileRecordVO.setFileType(fileRecord.getFileType());
        fileRecordVO.setFileDescription(fileRecord.getFileDescription());
        fileRecordVO.setUrl(SystemConstants.PDF_URL);
        fileRecordVO.setFileStoragePath(fileRecord.getFileStoragePath());
        fileRecordVO.setPdfStoragePath(fileRecord.getPdfStoragePath());
        fileRecordVO.setCreateTime(fileRecord.getCreateTime());
        fileRecordVO.setUpdateTime(fileRecord.getUpdateTime());
        return fileRecordVO;
    }

    /**
     * 删除文件
     *
     * @param ids
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean deleteFile(List<Long> ids) {
        // 用于跟踪所有操作是否成功
        boolean allOperationSuccess = true;
        for (Long id : ids) {
            try {
                // 原文件路径和pdf文件路径
                FileRecord fileRecord = this.getById(id);
                String sourceFilePath = fileRecord.getFileStoragePath();
                String pdfFilePath = fileRecord.getPdfStoragePath();
                File sourceFile = new File(sourceFilePath);
                File pdfFile = new File(pdfFilePath);

                // 删除磁盘上的原文件和pdf文件
                sourceFile.delete();
                pdfFile.delete();
                if (!sourceFile.exists() && !pdfFile.exists()) {
                    this.removeById(id);
                } else {
                    allOperationSuccess = false;
                }
            } catch (Exception e) {
                allOperationSuccess = false;
                log.error(e.getMessage());
                e.printStackTrace();
            }

        }

        return allOperationSuccess;
    }

    /**
     * 预览文件
     *
     * @param id
     * @return
     */
//    @Override
//    public Resource previewFile(Long id) {
//        FileRecord fileRecord = this.getById(id);
//
//        // pdf保存路径下的文件名
//        String fileName= fileRecord.getFileMd5() + ".pdf";
//        String fileSavePath = Paths.get(pdfDir, fileName).toString();
//
//         Resource resource = new FileSystemResource(fileSavePath);
//
//        return resource;
//    }


    /**
     * 上传文件
     *
     * @param files
     * @return
     */
    @Async
    @Override
    public boolean handleFileUpload(MultipartFile[] files) {

        boolean result = false;

        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                log.error("上传的文件 {} 为空", file.getOriginalFilename());
                continue;
            }


            // 获取上传文件的原始名称
            String originalFileName = file.getOriginalFilename();
            if (originalFileName == null || originalFileName.isEmpty()) {
                log.error("无效的文件名: {}", originalFileName);
                continue;
            }

            // 获取文件拓展名
            String fileExtension = this.getFileExtension(originalFileName);


            try {
                // 计算上传文件的Md5散列值
                byte[] fileBytes = file.getBytes();
                String fileMd5 = DigestUtils.md5DigestAsHex(fileBytes);

                // 构建上传文件的完整保存路径
                String hashFileName = fileMd5 + "." + fileExtension;
                Path saveFilePath = Paths.get(uploadDir).resolve(hashFileName);
                Path savePdfPath = Paths.get(pdfDir).resolve(fileMd5 + ".pdf" );

                // 检查服务器保存文件的目录是否存在, 如果不存在则创建
                Files.createDirectories(saveFilePath.getParent());
                Files.createDirectories(savePdfPath.getParent());
                // 内容写入到指定的文件中
                Files.write(saveFilePath, fileBytes);

                // 保存上传原文件信息到数据库
                FileRecord fileRecord = new FileRecord();
                fileRecord.setFileMd5(fileMd5);
                fileRecord.setFileName(originalFileName);

                // 文件大小，转换成只有2位小数的MB单位
                BigDecimal fileSize = new BigDecimal(file.getSize()).divide(new BigDecimal(1024 * 1024));
                String filSizeInMb = fileSize.setScale(2, BigDecimal.ROUND_HALF_UP).toString() + SystemConstants.FILE_SIZE_MB;
                fileRecord.setFileSize(filSizeInMb);


                fileRecord.setFileType(file.getContentType());
                fileRecord.setFileStoragePath(saveFilePath.toString());
                fileRecord.setPdfStoragePath(savePdfPath.toString());
                fileRecord.setFileConversionStatus(FileConvertEnum.UNCONVERTED.getValue());
                fileRecord.setCreateTime(LocalDateTime.now());
                result = this.save(fileRecord);



                if (fileExtension.equalsIgnoreCase("pdf")) {
                   // 是pdf文件，直接复制到保存pdf的目录
                    Files.copy(file.getInputStream(), savePdfPath, StandardCopyOption.REPLACE_EXISTING);
                } else {
                    // 转换成pdf文件
                    CompletableFuture<Void> completableFuture = CompletableFuture.runAsync(() -> {
                        try {
                            convertToPDF(saveFilePath, savePdfPath, originalFileName, fileRecord.getId());
                        } catch (OfficeException e) {
                            log.error("文件转换失败： {}", e.getMessage());
                            throw new RuntimeException(e);
                        }
                    });
                }



            } catch (IOException e) {
                log.error("上传文件 {} 失败：{}", originalFileName, e.getMessage());
            }

        }


        return result;
    }



    // 获取文件拓展名
    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf(".");
        return (lastDotIndex != -1) ? fileName.substring(lastDotIndex + 1).toLowerCase() : "";
    }


    /**
     * 上传文件转换成 pdf
     * @param inputFile
     * @param outputFile
     * @param originalFileName
     * @throws OfficeException
     */
    private void convertToPDF(Path inputFile, Path outputFile, String originalFileName, Long id) throws OfficeException {
        documentConverter
                .convert(inputFile.toFile())
                .as(DefaultDocumentFormatRegistry.getFormatByExtension(getFileExtension(originalFileName)))
                .to(outputFile.toFile())
                .as(DefaultDocumentFormatRegistry.PDF)
                .execute();

        log.info("convert file {} to pdf success", originalFileName);
    }





    /**
     * 下载文件
     * 给定的文件SHA-256哈希值和文件名获取一个 Resource 对象，该对象代表了待下载的文件资源
     * @param fileSavePath
     * @return
     */
    @Override
    public Resource handleDownloadSourceFile(String fileSavePath) {
        File file = new File(fileSavePath);
        if (!file.exists()) {
            return null;
        }

        // 返回资源对象
        try {
            FileSystemResource fileSystemResource = new FileSystemResource(file);
            // 获取文件长度，验证文件的有效性
            fileSystemResource.contentLength();
            return fileSystemResource;
        } catch (IOException e) {
            log.error("failed to access the file: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 下载pdf文件
     *
     * @param pdfStoragePath
     * @return
     */
    @Override
    public Resource handleDownloadPdfFile(String pdfStoragePath) {
        File file = new File(pdfStoragePath);
        if (!file.exists()) {
            return null;
        }

        // 返回资源对象
        try {
            FileSystemResource fileSystemResource = new FileSystemResource(file);
            // 获取文件长度，验证文件的有效性
            fileSystemResource.contentLength();
            return fileSystemResource;
        } catch (IOException e) {
            log.error("failed to access the file: {}", e.getMessage());
            return null;
        }
    }


}




