package com.admin.common.config;

import com.admin.common.handler.MyMetaObjectHandler;
import com.admin.common.interceptor.DataPermissionInterceptor;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * MyBatis-Plus 配置类
 * 
 * @author suYan
 * @date 2022/12/13 16:18
 */
@Configuration
@EnableTransactionManagement
public class MybatisPlusConfig {

    /**
     * MyBatis-Plus 拦截器配置
     * 包括：分页插件、数据权限插件、乐观锁插件
     *
     * @return MybatisPlusInterceptor
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 分页插件
        PaginationInnerInterceptor paginationInterceptor = new PaginationInnerInterceptor();
        paginationInterceptor.setOverflow(false); // 合理化分页参数
        paginationInterceptor.setMaxLimit(1000L); // 单页最大数量限制
        interceptor.addInnerInterceptor(paginationInterceptor);

        // 使用自定义的数据权限拦截器
        interceptor.addInnerInterceptor(new DataPermissionInterceptor());
        
        // 乐观锁插件，防止并发修改数据时出现覆盖问题
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());

        // 多租户插件（如需要可启用）
        // interceptor.addInnerInterceptor(new TenantLineInnerInterceptor(new MyTenantLineHandler()));

        return interceptor;
    }

    /**
     * 全局配置
     * 自动填充数据库创建人、创建时间、更新人、更新时间
     *
     * @return GlobalConfig
     */
    @Bean
    public GlobalConfig globalConfig() {
        GlobalConfig globalConfig = new GlobalConfig();
        globalConfig.setMetaObjectHandler(new MyMetaObjectHandler());
        globalConfig.setBanner(false); // 关闭MyBatis-Plus启动banner
        return globalConfig;
    }
}