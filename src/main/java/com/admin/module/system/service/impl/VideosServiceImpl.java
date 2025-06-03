package com.admin.module.system.service.impl;

import com.admin.common.constant.SystemConstants;
import com.admin.common.result.ResultVO;
import com.admin.module.system.mapper.VideosMapper;
import com.admin.module.system.service.VideosService;
import com.admin.module.system.entity.Videos;
import com.admin.module.system.query.VideoQuery;
import com.admin.module.system.vo.VideosVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
* @author sy
* @description 针对表【t_videos(视频文件记录表)】的数据库操作Service实现
* @createDate 2024-07-03 10:20:10
*/
@Slf4j
@Service
public class VideosServiceImpl extends ServiceImpl<VideosMapper, Videos> implements VideosService {

    @Value("${video.upload.dir}")
    private String videoDir;


    /**
     * 上传多媒体视频文件
     *
     * @param file
     * @return
     */
    @Override
    public ResultVO<String> uploadVideo(MultipartFile file) {
        if (file.isEmpty()) {
            return ResultVO.error("上传的文件不能为空");
        }

        String fileName = file.getOriginalFilename();

        // 同名文件不可以再上传
        boolean result = checkFileExist(fileName);
        if (result) {
            return ResultVO.error("已存在该文件，请勿重复上传");
        }

        try {

            Path saveVideoPath = Paths.get(videoDir).resolve(fileName);
            Files.createDirectories(saveVideoPath.getParent());
            Files.copy(file.getInputStream(), saveVideoPath);

            // 保存文件信息到数据库
            Videos video = new Videos();
            video.setFileName(fileName);
            video.setFilePath(saveVideoPath.toString());
            this.save(video);
        } catch (IOException e) {
            log.error("文件上传失败: {}", e.getMessage());
            throw new RuntimeException(e);
        }

        return ResultVO.success("文件上传成功");
    }



    /**
     * 同名文件，则不能再上传
     * @param fileName
     * @return
     */
    private boolean checkFileExist(String fileName) {
        LambdaQueryWrapper<Videos> queryWrapper = new LambdaQueryWrapper<Videos>();
        queryWrapper.eq(Videos::getFileName, fileName);

        boolean result = this.exists(queryWrapper);
        return result;
    }



    /**
     * 下载视频
     *
     * @param fileName
     * @return
     */
    @Override
    public ResponseEntity<?> downloadVideo(String fileName) throws IOException {
        Path videoPath = Paths.get(videoDir, fileName);
        FileSystemResource fileResource = new FileSystemResource(videoPath);

        if (!fileResource.exists()) {
            return ResponseEntity.notFound().build();
        }

        MediaType mediaType = MediaTypeFactory.getMediaType(fileResource).orElse(MediaType.APPLICATION_OCTET_STREAM);
        String contentType = mediaType.toString();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.builder("attachment").filename(fileName).build());
        headers.setContentType(MediaType.parseMediaType(contentType));
        headers.setContentLength(fileResource.contentLength());

        return ResponseEntity.ok()
                .headers(headers)
                .body(fileResource);
    }




    /**
     * 多媒体视频文件分页列表
     *
     * @param videoQuery
     * @return
     */
    @Override
    public IPage<VideosVO> selectVideosPage(VideoQuery videoQuery) {
        LambdaQueryWrapper<Videos> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(videoQuery.getFileName())) {
            queryWrapper.like(Videos::getFileName, videoQuery.getFileName());
        }
        queryWrapper.orderByDesc(Videos::getCreateTime);
        IPage<Videos> pageInfo = new Page<>(videoQuery.getPage(), videoQuery.getLimit());
        IPage<VideosVO> videos = this.page(pageInfo, queryWrapper).convert(video -> {
            return convertToVO(video);
        });
        return videos;
    }


    private VideosVO convertToVO(Videos video) {
        VideosVO vo = new VideosVO();
        vo.setId(video.getId());
        vo.setFileName(video.getFileName());
        vo.setDescription(video.getDescription());
        vo.setFilePath(video.getFilePath());
        vo.setUrl(SystemConstants.VIDEO_URL);
        vo.setRemark(video.getRemark());
        vo.setCreateTime(video.getCreateTime());
        vo.setCreateUser(video.getCreateUser());
        return vo;
    }


    /**
     * 删除多媒体视频文件
     *
     * @param ids
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean deleteVideo(List<Long> ids) {
        // 用于所有操作是否成功
        boolean allOk = true;

        for (long id : ids) {
            try {
                Videos video = this.getById(id);
                String filePath = video.getFilePath();
                File originalFile = new File(filePath);

                // 删除源文件
                boolean originalFileDeleted = originalFile.delete();

                if (originalFileDeleted || !originalFile.exists()) {
                    // 删除数据库记录
                    this.removeById(id);
                } else {
                    allOk = false;
                }
            } catch (Exception e) {
                allOk = false;
                log.error(e.getMessage());
                e.printStackTrace();
            }
        }

        return allOk;
    }

    /**
     * 播放视频
     *
     * @param id
     * @return
     */
//    @Override
//    public String playVideo(Long id) {
//        Videos videos = this.getById(id);
//        String filePath = videos.getFilePath();
//        Resource resource = new FileSystemResource(videos.getFilePath());
//        return resource.toString();
//    }


}





