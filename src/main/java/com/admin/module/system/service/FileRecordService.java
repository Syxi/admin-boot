package com.admin.module.system.service;

import com.admin.module.system.entity.FileRecord;
import com.admin.module.system.query.FileRecordQuery;
import com.admin.module.system.vo.FileRecordVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
* @author sy
* @description 针对表【file_record】的数据库操作Service
* @createDate 2024-03-12 11:03:03
*/
public interface FileRecordService extends IService<FileRecord> {


    /**
     * 文件列表信息
     * @param fileRecordQuery
     * @return
     */
    IPage<FileRecordVO> selectFilePage(FileRecordQuery fileRecordQuery);

    /**
     * 删除文件
     * @param ids
     */
    boolean deleteFile(List<Long> ids);


    /**
     * 预览文件
     * @param id
     * @return
     */
//    Resource previewFile(Long id);

    /**
     * 上传文件
     * @param files
     * @return
     */
    CompletableFuture<Boolean> handleFileUpload(MultipartFile[] files);

    /**
     * 下载文件
     * @param fileSavePath
     * @return
     */
    Resource handleDownloadSourceFile(String fileSavePath);


    /**
     * 下载pdf文件
     * @param pdfStoragePath
     * @return
     */
    Resource handleDownloadPdfFile(String pdfStoragePath);

    /**
     * 检查文件是否已存在（基于MD5）
     * @param fileMd5
     * @return
     */
    boolean checkFileExistsByMd5(String fileMd5);


}
