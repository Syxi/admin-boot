package com.admin.web;

import com.admin.common.result.PageResult;
import com.admin.common.result.ResultVO;
import com.admin.module.system.entity.FileRecord;
import com.admin.module.system.query.FileRecordQuery;
import com.admin.module.system.service.FileRecordService;
import com.admin.module.system.vo.FileRecordVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Tag(name = "文件接口")
@RestController
@RequestMapping("/api/file")
@RequiredArgsConstructor
public class FileRecordController {

    private final FileRecordService fileRecordService;


    @Operation(summary = "文件列表")
    @GetMapping("/page")
    public PageResult<FileRecordVO> selectFilePage(FileRecordQuery fileRecordQuery) {
        IPage<FileRecordVO> fileList = fileRecordService.selectFilePage(fileRecordQuery);
        return PageResult.success(fileList);
    }




    @Operation(summary = "删除文件")
    @PreAuthorize("@pms.hasPerm('sys:file:delete')")
    @DeleteMapping("/delete")
    public ResultVO<Boolean> deleteFile(@RequestBody List<Long> ids) {
        boolean result = fileRecordService.deleteFile(ids);
        return ResultVO.judge(result);
    }

//    @Operation(summary = "预览文件")
//    @PreAuthorize("@pms.hasPerm('sys:file:previewer')")
//    @GetMapping("/preview/{id}")
//    public ResponseEntity<Resource> previewFile(@PathVariable("id") Long id) {
//        try {
//            Resource resource = fileRecordService.previewFile(id);
//
//            // 设置响应头，告诉浏览器这是一个PDF文件
//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.APPLICATION_PDF);
//
//            return ResponseEntity.ok()
//                    .headers(headers)
//                    .body(resource);
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(null);
//        }
//    }


    @Operation(summary = "上传文件")
    @PreAuthorize("@pms.hasPerm('sys:file:upload')")
    @PostMapping("/upload")
    public ResultVO<Boolean> handleFileUpload(@RequestParam("file") MultipartFile[] file) {
        boolean result = fileRecordService.handleFileUpload(file);
        return ResultVO.judge(result);
    }


    @Operation(summary= "下载原文件")
    @PreAuthorize("@pms.hasPerm('sys:file:downloadSourceFile')")
    @GetMapping("/downloadSourceFile/{id}")
    public ResponseEntity<Resource> handleDownloadSourceFile(@PathVariable("id") Long id) {
        FileRecord fileRecord = fileRecordService.getById(id);
        if (fileRecord == null) {
            return ResponseEntity.notFound().build();
        }

        String fileName = fileRecord.getFileName();
        String mimeType = fileRecord.getFileType();
        MediaType mediaType = MediaType.parseMediaType(mimeType);
        String encodeFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8);

        // 设置响应头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(mediaType);
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodeFileName);

        String fileSavePath = fileRecord.getFileStoragePath();
        Resource fileResource = fileRecordService.handleDownloadSourceFile(fileSavePath);
        if (fileResource == null ) {
            return ResponseEntity.notFound().build();
        }


        return ResponseEntity.ok()
                .headers(headers)
                .body(fileResource);
    }


    @Operation(summary= "下载pdf文件")
    @PreAuthorize("@pms.hasPerm('sys:file:downloadPdfFile')")
    @GetMapping("/downloadPdfFile/{id}")
    public ResponseEntity<Resource> handleDownloadPdfFile(@PathVariable("id") Long id) {
        FileRecord fileRecord = fileRecordService.getById(id);
        if (fileRecord == null) {
            return ResponseEntity.notFound().build();
        }

        String fileName = fileRecord.getFileName();
        String mimeType = fileRecord.getFileType();
        MediaType mediaType = MediaType.parseMediaType(mimeType);
        String encodeFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8);

        // 设置响应头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(mediaType);
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodeFileName);

        String pdfStoragePath = fileRecord.getPdfStoragePath();
        Resource fileResource = fileRecordService.handleDownloadPdfFile(pdfStoragePath);
        if (fileResource == null ) {
            return ResponseEntity.notFound().build();
        }


        return ResponseEntity.ok()
                .headers(headers)
                .body(fileResource);
    }




    @Operation(summary = "文件上传进度")
    @GetMapping("/uploadProgress")
    public ResultVO<Integer> getUploadProgress(HttpServletRequest request) {
        HttpSession session = request.getSession();
        Object percent = session.getAttribute("uploadProgress");
        return null == percent ? ResultVO.success(0) : ResultVO.success((Integer) percent);
    }


    @Operation(summary = "检查文件转换是否完成")
    @GetMapping("/checkFileConvert/{id}")
    public ResultVO<Integer> checkFileConvertStatus(@PathVariable Long id) {
        Integer result = fileRecordService.checkFileConvertStatus(id);
        return  ResultVO.success(result);
    }











}
