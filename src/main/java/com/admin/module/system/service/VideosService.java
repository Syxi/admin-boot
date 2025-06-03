package com.admin.module.system.service;

import com.admin.common.result.ResultVO;
import com.admin.module.system.entity.Videos;
import com.admin.module.system.query.VideoQuery;
import com.admin.module.system.vo.VideosVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
* @author sy
* @description 针对表【t_videos(视频文件记录表)】的数据库操作Service
* @createDate 2024-07-03 10:20:10
*/
public interface VideosService extends IService<Videos> {



    /**
     * 上传多媒体视频文件
     * @param file
     * @return
     */
    ResultVO<String> uploadVideo(MultipartFile file);


    /**
     * 下载视频
     * @param fileName
     * @return
     */
    ResponseEntity<?> downloadVideo(String fileName) throws IOException;

    /**
     * 多媒体视频文件分页列表
     * @param videoQuery
     * @return
     */
    IPage<VideosVO> selectVideosPage(VideoQuery videoQuery);

    /**
     * 删除多媒体视频文件
     * @param ids
     * @return
     */
    boolean deleteVideo(List<Long> ids);


    /**
     * 播放视频
     * @param id
     * @return
     */
//    String playVideo(Long id);
}
