package com.admin.web;

import com.admin.common.result.PageResult;
import com.admin.common.result.ResultVO;
import com.admin.module.system.entity.FileRecord;
import com.admin.module.system.query.FileRecordQuery;
import com.admin.module.system.service.FileChunkService;
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
import java.util.Map;

@Tag(name = "文件接口")
@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
public class FileRecordController {

    private final FileRecordService fileRecordService;
    
    private final FileChunkService fileChunkService;


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
        boolean result = fileRecordService.handleFileUpload(file).join();
        return ResultVO.judge(result);
    }


    @Operation(summary= "下载原文件")
    @PreAuthorize("@pms.hasPerm('sys:file:downloadSourceFile')")
    @GetMapping("/downloadSourceFile/{id}")
    public ResponseEntity<Resource> handleDownloadSourceFile(@PathVariable("id") Long id, HttpServletRequest request) {
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
        headers.add(HttpHeaders.ACCEPT_RANGES, "bytes");

        String fileSavePath = fileRecord.getFileStoragePath();
        
        return fileRecordService.handleDownloadSourceFileWithResume(fileSavePath, request);
    }


    @Operation(summary= "下载pdf文件")
    @PreAuthorize("@pms.hasPerm('sys:file:downloadPdfFile')")
    @GetMapping("/downloadPdfFile/{id}")
    public ResponseEntity<Resource> handleDownloadPdfFile(@PathVariable("id") Long id, HttpServletRequest request) {
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
        headers.add(HttpHeaders.ACCEPT_RANGES, "bytes");

        String pdfStoragePath = fileRecord.getPdfStoragePath();
        
        return fileRecordService.handleDownloadPdfFileWithResume(pdfStoragePath, request);
    }


    @Operation(summary = "文件上传进度")
    @GetMapping("/uploadProgress")
    public ResultVO<Integer> getUploadProgress(HttpServletRequest request) {
        HttpSession session = request.getSession();
        Object percent = session.getAttribute("uploadProgress");
        return null == percent ? ResultVO.success(0) : ResultVO.success((Integer) percent);
    }

    @Operation(summary = "检查分片是否存在")
    @PreAuthorize("@pms.hasPerm('sys:file:upload')")
    @PostMapping(value = "/checkChunk", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResultVO<Boolean> checkChunk(@RequestBody Map<String, Object> requestBody) {
        String identifier = (String) requestBody.get("identifier");
        Integer chunkNumber = parseInteger(requestBody.get("chunkNumber"));
        boolean exists = fileChunkService.checkChunkExists(identifier, chunkNumber);
        return ResultVO.success(exists);
    }

    private Integer parseInteger(Object value) {
        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof String) {
            return Integer.valueOf((String) value);
        } else if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return null;
    }

    @Operation(summary = "上传分片")
    @PreAuthorize("@pms.hasPerm('sys:file:upload')")
    @PostMapping(value = "/uploadChunk", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResultVO<Boolean> uploadChunk(
            @RequestParam("identifier") String identifier,
            @RequestParam("chunkNumber") Integer chunkNumber,
            @RequestParam("totalChunks") Long totalChunks,
            @RequestParam("totalSize") Long totalSize,
            @RequestParam("filename") String filename,
            @RequestParam("chunkHash") String chunkHash,
            @RequestParam("file") MultipartFile file) {
        boolean result = fileChunkService.uploadChunk(identifier, chunkNumber, totalChunks, totalSize, filename, chunkHash, file);
        return ResultVO.judge(result);
    }

    @Operation(summary = "合并分片")
    @PreAuthorize("@pms.hasPerm('sys:file:upload')")
    @PostMapping(value = "/mergeChunks", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResultVO<Boolean> mergeChunks(@RequestBody Map<String, Object> requestBody) {
        String identifier = (String) requestBody.get("identifier");
        String filename = (String) requestBody.get("filename");
        String fileMd5 = (String) requestBody.get("fileMd5");
        String fileType = (String) requestBody.get("fileType");
        String description = (String) requestBody.get("description");
        
        boolean result = fileChunkService.mergeChunks(identifier, filename, fileMd5, fileType, description);
        return ResultVO.judge(result);
    }

    @Operation(summary = "查询已上传的分片")
    @PreAuthorize("@pms.hasPerm('sys:file:upload')")
    @GetMapping("/uploadedChunks")
    public ResultVO<List<Integer>> getUploadedChunks(@RequestParam String identifier) {
        List<Integer> uploadedChunks = fileChunkService.getUploadedChunks(identifier);
        return ResultVO.success(uploadedChunks);
    }

    @Operation(summary = "检查文件是否已存在")
    @PreAuthorize("@pms.hasPerm('sys:file:upload')")
    @PostMapping(value = "/checkFileExists", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResultVO<Boolean> checkFileExists(@RequestBody Map<String, Object> requestBody) {
        String fileMd5 = (String) requestBody.get("fileMd5");
        boolean exists = fileRecordService.checkFileExistsByMd5(fileMd5);
        return ResultVO.success(exists);
    }

}