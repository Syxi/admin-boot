package com.admin.common.config;

import com.admin.common.constant.SystemConstants;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    @Value("${image.upload.dir}")
    private String imagesPath;


    @Value("${video.upload.dir}")
    private String videosDir;

    @Value("${file.pdf-dir}")
    private String pdfDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 前后端分离，怎么访问后端服务器本地资源：先把请求url加入白名单，再映射本地文件夹
        registry.addResourceHandler(SystemConstants.PDF_URL + "**").addResourceLocations("file:" + pdfDir);
        registry.addResourceHandler(SystemConstants.IMG_URL + "**").addResourceLocations("file:" + imagesPath);
        registry.addResourceHandler(SystemConstants.VIDEO_URL + "**").addResourceLocations("file:" + videosDir);
        WebMvcConfigurer.super.addResourceHandlers(registry);
    }

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(customizeJackson2HttpMessageConverter());
    }



    /**
     * 时间格式的转换
     * @return
     */
    @Bean
    public MappingJackson2HttpMessageConverter customizeJackson2HttpMessageConverter() {
        ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json()
                .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .featuresToDisable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .modules(additionalModules())
                .build();

        // 序列化和反序列化 LocalDateTime
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DATE_TIME_FORMATTER));
        simpleModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DATE_TIME_FORMATTER));

        // 序列化和反序列化 LocalDate
        simpleModule.addSerializer(LocalDate.class, new LocalDateSerializer(DATE_FORMATTER));
        simpleModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(DATE_FORMATTER));

        // 序列化和反序列化 LocalTime
        simpleModule.addSerializer(LocalTime.class, new LocalTimeSerializer(TIME_FORMATTER));
        simpleModule.addDeserializer(LocalTime.class, new LocalTimeDeserializer(TIME_FORMATTER));

        objectMapper.registerModule(simpleModule);
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter(objectMapper);
        return converter;
    }



    private List<Module> additionalModules() {
        SimpleModule simpleModule = new SimpleModule();
        // 处理后台Long类型传递给前端精度丢失问题
        simpleModule.addSerializer(Long.class, ToStringSerializer.instance);
        return List.of(simpleModule);
    }


}
