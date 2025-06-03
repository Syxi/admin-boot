package com.admin.module.system.service;

import com.admin.module.system.entity.Image;
import com.admin.module.system.query.ImageQuery;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
* @author sy
* @description 针对表【t_image(图片表)】的数据库操作Service
* @createDate 2024-08-12 17:21:31
*/
public interface ImageService extends IService<Image> {

    /**
     * 上传单张图片
     * @param file
     * @return
     */
    String uploadImage(MultipartFile file);

    /**
     * 上传多张图片
     * @param files
     * @return
     */
    List<String> uploadImages(MultipartFile[] files);


    /**
     * 图片分页列表
     * @return
     */
    IPage<Image> selectImagePage(ImageQuery imageQuery);


    /**
     * 更新图片名称
     * @param image
     * @return
     */
    boolean updateImage(Image image);

    /**
     * 删除图片
     * @param ids
     * @return
     */
    boolean deleteImage(List<Long> ids);

    /**
     * 下载图片
     * @param fileSavePath
     * @return
     */
    Resource handleDownloadImage(String fileSavePath);

    /**
     * 门户首页图片库
     * @return
     */
    List<String> selectImageUrls();
}
