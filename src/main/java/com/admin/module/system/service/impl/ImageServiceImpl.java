package com.admin.module.system.service.impl;

import com.admin.common.constant.SystemConstants;
import com.admin.common.exception.CustomException;
import com.admin.module.system.mapper.ImageMapper;
import com.admin.module.system.entity.Image;
import com.admin.module.system.query.ImageQuery;
import com.admin.module.system.service.ImageService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
* @author sy
* @description 针对表【t_image(图片表)】的数据库操作Service实现
* @createDate 2024-08-12 17:21:31
*/
@Service
public class ImageServiceImpl extends ServiceImpl<ImageMapper, Image> implements ImageService{

    @Value(("${image.upload.dir}"))
    private String imageDir;


    /**
     * 上传单张图片
     *
     * @param file
     * @return
     */
    @Override
    public String uploadImage(MultipartFile file) {
        if (file.isEmpty()) {
            log.error("上传图片为空");
            throw  new CustomException("上传图片为空");
        }

        // 生成唯一文件名
        String uuidStr = UUID.randomUUID().toString().replace("-", "");
        String uniqueFileName = uuidStr + "." + file.getOriginalFilename().split("\\.")[1];
        // 在根目录 uploadDir 下创建 avatars 目录保存头像
        String uploadAvatarPath = Paths.get(imageDir, uniqueFileName).toString();
        // 图片完整保存路径
        File filePath = new File(uploadAvatarPath);

        // 创建文件夹如果文件不存在
        File dir = new File(filePath.getParent());
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 保存文件到服务器
        try ( FileOutputStream outputStream = new FileOutputStream(filePath) ) {
            outputStream.write(file.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // 构建图片的 url
        String imageUrl = SystemConstants.IMG_URL + uniqueFileName;

        // 保存上传图片信息到数据库
        Image image = new Image();
        image.setImageName(file.getOriginalFilename());
        // 文件大小，转换成只有2位小数的MB单位
        BigDecimal fileSize = new BigDecimal(file.getSize()).divide(new BigDecimal(1024 * 1024));
        String filSizeInMb = fileSize.setScale(2, BigDecimal.ROUND_HALF_UP).toString() + SystemConstants.FILE_SIZE_MB;
        image.setImageSize(filSizeInMb);
        image.setImageType(file.getContentType());
        image.setUrl(imageUrl);
        image.setStoragePath(filePath.toString());
        this.save(image);

        return imageUrl;
    }

    /**
     * 上传多张图片
     *
     * @param files
     * @return
     */
    @Override
    public List<String> uploadImages(MultipartFile[] files) {
        List<String> imgUrls = new ArrayList<>();

        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                log.error("上传图片为空");
                throw  new CustomException("上传图片为空");
            }

            // 生成唯一文件名
            String uuidStr = UUID.randomUUID().toString().replace("-", "");
            String uniqueFileName = uuidStr + "." + file.getOriginalFilename().split("\\.")[1];
            // 在根目录 uploadDir 下创建 avatars 目录保存头像
            String uploadAvatarPath = Paths.get(imageDir, uniqueFileName).toString();
            // 图片完整保存路径
            File filePath = new File(uploadAvatarPath);

            // 创建文件夹如果文件不存在
            File dir = new File(filePath.getParent());
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 保存文件到服务器
            try ( FileOutputStream outputStream = new FileOutputStream(filePath) ) {
                outputStream.write(file.getBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            // 构建图片的 url
            String imageUrl = SystemConstants.IMG_URL + uniqueFileName;

            imgUrls.add(imageUrl);

            // 保存上传图片的信息到数据库
            Image image = new Image();
            image.setImageName(file.getOriginalFilename());
            // 文件大小，转换成只有2位小数的MB单位
            BigDecimal fileSize = new BigDecimal(file.getSize()).divide(new BigDecimal(1024 * 1024));
            String filSizeInMb = fileSize.setScale(2, BigDecimal.ROUND_HALF_UP).toString() + SystemConstants.FILE_SIZE_MB;
            image.setImageSize(filSizeInMb);

            image.setImageType(file.getContentType());
            image.setUrl(imageUrl);
            image.setStoragePath(filePath.toString());

            this.save(image);
        }


        return imgUrls;
    }


    /**
     * 图片分页列表
     *
     * @param imageQuery
     * @return
     */
    @Override
    public IPage<Image> selectImagePage(ImageQuery imageQuery) {
        LambdaQueryWrapper<Image> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotEmpty(imageQuery.getImageName())) {
            wrapper.like(Image::getImageName, imageQuery.getImageName());
        }
        wrapper.orderByDesc(Image::getCreateTime);

        IPage<Image> page = new Page<>(imageQuery.getPage(), imageQuery.getLimit());
        IPage<Image> pageData = this.page(page, wrapper);
        return pageData;
    }

    /**
     * 更新图片名称
     *
     * @param image
     * @return
     */
    @Override
    public boolean updateImage(Image image) {
        return this.updateById(image);
    }


    /**
     * 删除图片
     *
     * @param ids
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean deleteImage(List<Long> ids) {
        // 用于跟踪所有操作是否成功
        boolean allOperationSuccess = true;

        for (Long id : ids) {
            try {
                // 源文件路径
                Image image = this.getById(id);
                String originalFilePath = image.getStoragePath();
                File originalFile = new File(originalFilePath);

                // 检测并删除源文件
                boolean originalFileDeleted = originalFile.delete();

                if (originalFileDeleted || !originalFile.exists()) {
                    // 删除数据库记录
                    this.removeById(id);
                } else {
                    allOperationSuccess = false;
                }

            } catch (Exception e) {
                allOperationSuccess = false;
                log.error(e.getMessage());
                e.printStackTrace();
            }
        };

        return allOperationSuccess;
    }


    /**
     * 下载图片
     *
     * @param fileSavePath 图片保存路径
     * @return
     */
    @Override
    public Resource handleDownloadImage(String fileSavePath) {
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
            log.error("failed to access the image: {}");
            return null;
        }
    }

    /**
     * 门户首页图片库
     *
     * @return
     */
    @Override
    public List<String> selectImageUrls() {
        List<String> urls = this.list().stream()
                .map(Image::getUrl)
                .collect(Collectors.toList());
        return urls;
    }


}




