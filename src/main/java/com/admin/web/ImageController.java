package com.admin.web;

import com.admin.common.result.PageResult;
import com.admin.common.result.ResultVO;
import com.admin.module.system.entity.Image;
import com.admin.module.system.query.ImageQuery;
import com.admin.module.system.service.ImageService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "图片库接口")
@RequiredArgsConstructor
@RequestMapping("/api/image")
@RestController
public class ImageController {

    private final ImageService imageService;

    @Operation(summary = "上传单张图片")
    @PreAuthorize("@pms.hasPerm('sys:image:upload')")
    @PostMapping("/uploadImage")
    public ResultVO<String> uploadImages(@RequestParam("file") MultipartFile file){
        String imgUrl = imageService.uploadImage(file);
        return ResultVO.success(imgUrl);
    }


    @Operation(summary = "上传多张图片")
    @PostMapping("/uploadImages")
    public ResultVO<List<String>> uploadImages(@RequestParam("file") MultipartFile[] files) {
        List<String> imgUrls = imageService.uploadImages(files);
        return ResultVO.success(imgUrls);
    }


    @Operation(summary = "图片分页列表")
    @GetMapping("/page")
    public PageResult<Image> selectImagesPage(ImageQuery imageQuery) {
        IPage<Image> imageIPage = imageService.selectImagePage(imageQuery);
        return PageResult.success(imageIPage);
    }


    @Operation(summary = "获取图片表单信息")
    @GetMapping("/{id}")
    public ResultVO<Image> getImageFormData(@PathVariable("id") Long id) {
        Image image = imageService.getById(id);
        return ResultVO.success(image);
    }



    @Operation(summary = "编辑图片")
    @PreAuthorize("@pms.hasPerm('sys:image:edit')")
    @PutMapping("/edit")
    public ResultVO<Boolean> updateImage(@RequestBody Image image) {
        boolean result = imageService.updateImage(image);
        return ResultVO.success(result);
    }


    @Operation(summary = "删除图片")
    @PreAuthorize("@pms.hasPerm('sys:image:delete')")
    @DeleteMapping("/delete")
    public ResultVO<Boolean> deleteImage(@RequestBody List<Long> ids) {
        boolean result = imageService.deleteImage(ids);
        return ResultVO.success(result);
    }


    @Operation(summary= "下载文件")
    @PreAuthorize("@pms.hasPerm('sys:image:download')")
    @GetMapping("/downloadImage/{id}")
    public ResponseEntity<Resource> handleDownloadFile(@PathVariable("id") Long id) {
        Image imageEntity = imageService.getById(id);
        if (imageEntity == null) {
            return ResponseEntity.notFound().build();
        }

        String fileName = imageEntity.getImageName();
        String mimeType = imageEntity.getImageType();
        MediaType mediaType = MediaType.parseMediaType(mimeType);
        String encodeFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8);

        // 设置响应头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(mediaType);
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodeFileName);

        String fileSavePath = imageEntity.getStoragePath();
        Resource fileResource = imageService.handleDownloadImage(fileSavePath);
        if (fileResource == null ) {
            return ResponseEntity.notFound().build();
        }


        return ResponseEntity.ok()
                .headers(headers)
                .body(fileResource);
    }


    @Operation(summary = "门户首页图片库")
    @GetMapping("/portal/image")
    public ResultVO<List<String>> selectImageUrls() {
        List<String> urls = imageService.selectImageUrls();
        return ResultVO.success(urls);
    }

}
