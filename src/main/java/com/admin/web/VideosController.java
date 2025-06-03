package com.admin.web;

import com.admin.common.result.PageResult;
import com.admin.common.result.ResultVO;
import com.admin.module.system.query.VideoQuery;
import com.admin.module.system.service.VideosService;
import com.admin.module.system.vo.VideosVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Tag(name = "多媒体视频")
@RequiredArgsConstructor
@RequestMapping("/api/video")
@RestController
public class VideosController {

    private final VideosService videosService;

    private static final String RANGE_HEADER = "Range";


    @Operation(summary = "上传多媒体文件")
    @PreAuthorize("@pms.hasPerm('sys:video:upload')")
    @PostMapping("/upload")
    public ResultVO<String> uploadVideo(MultipartFile file) {
        return videosService.uploadVideo(file);
    }



    @Operation(summary = "下载多媒体视频")
    @PreAuthorize("@pms.hasPerm('sys:video:download')")
    @GetMapping("download/{fileName}")
    public ResponseEntity<?> downloadVideo(@PathVariable("fileName") String fileName) throws IOException {
        return videosService.downloadVideo(fileName);
    }


    @Operation(summary = "多媒体视频分页列表")
    @GetMapping("/page")
    public PageResult<VideosVO> selectVideosPage(VideoQuery videoQuery) {
        IPage<VideosVO> videosIPage = videosService.selectVideosPage(videoQuery);
        return PageResult.success(videosIPage);
    }

    @Operation(summary = "删除多媒体文件")
    @PreAuthorize("@pms.hasPerm('sys:video:delete')")
    @DeleteMapping("/delete")
    public ResultVO<Boolean> deleteVideo(@RequestBody List<Long> ids) {
        boolean result =  videosService.deleteVideo(ids);
        return ResultVO.success(result);
    }


}
