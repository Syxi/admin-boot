package com.admin.common.excel.listener;

import com.admin.common.constant.SystemConstants;
import com.admin.common.enums.*;
import com.admin.common.excel.ImportResult;
import com.admin.common.excel.ImportUserFailVO;
import com.admin.common.excel.importvo.UserImportVO;
import com.admin.common.util.SpringContextUtil;
import com.admin.module.system.entity.*;
import com.admin.module.system.service.*;
import com.alibaba.excel.context.AnalysisContext;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户导入监听器
 * @Author: suYan
 * @Date: 2024-01-22
 */
@Slf4j
public class UserImportListener extends MyAnalysisEventListener<UserImportVO> {

    private final SysUserService sysUserService;

    private final SysUserRoleService userRoleService;

    private final SysRoleService roleService;

    private final SysDeptService sysDeptService;

    private final SysUserDeptService sysUserDeptService;

    private final String encodeDefaultPassword;

    private final Set<String> existingUsernames;

    private final Set<String> existingRoleNames;

    private final Set<String> existingDeptCodes;
    
    // 部门编码映射到id
    private final Map<String, Long> deptCodeIdMap;

    // 有效条数
    private int validCount;

    // 无效条数
    private int invalidCount;


    // 批量导入的用户
    List<SysUser> userList = new ArrayList<>();

    // 批量导入的用户角色关系
    List<SysUserRole> userRoleList = new ArrayList<>();

    // username和 roleNames的一对多关系
    Map<String, List<String>> usernameRoleNamesMap = new HashMap<>();

    // 用户Id和部门id关系
    private Map<Long, Long> userIdDeptIdMap = new HashMap<>();

    private List<UserImportVO> importVOList = new ArrayList<>();

    // 校验结果列表
    List<ImportUserFailVO> userFailVOList = new ArrayList<>();




    public UserImportListener() {
        PasswordEncoder passwordEncoder = SpringContextUtil.getBean(PasswordEncoder.class);
        this.sysUserService = SpringContextUtil.getBean(SysUserService.class);
        this.userRoleService = SpringContextUtil.getBean(SysUserRoleService.class);
        this.roleService = SpringContextUtil.getBean(SysRoleService.class);
        this.sysDeptService = SpringContextUtil.getBean(SysDeptService.class);
        this.sysUserDeptService = SpringContextUtil.getBean(SysUserDeptService.class);

        this.encodeDefaultPassword = passwordEncoder.encode(SystemConstants.DEFAULT_PASSWORD);

        // 加载所有用户名
        this.existingUsernames = sysUserService.list(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getDeleted, DeletedEnum.NO_DELETE.getValue())
                )
                .stream()
                .map(SysUser::getUsername)
                .collect(Collectors.toSet());

        // 加载所有角色名称
        this.existingRoleNames= roleService.list(
                new LambdaQueryWrapper<SysRole>().eq(SysRole::getDeleted, DeletedEnum.NO_DELETE.getValue())
                )
                .stream()
                .map(SysRole::getRoleName)
                .collect(Collectors.toSet());

        LambdaUpdateWrapper<SysDept> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(SysDept::getDeleted, DeletedEnum.NO_DELETE.getValue());
        List<SysDept> sysOrganizationList = sysDeptService.list(wrapper);

        // 加载所有部门编码
        this.existingDeptCodes = sysOrganizationList.stream()
                .filter(organ -> OrganizationTypeEnum.DEPT.getValue().equals(organ.getDeptType()))
                .map(SysDept::getDeptCode)
                .collect(Collectors.toSet());

        // 部门编码映射到id
        this.deptCodeIdMap = sysOrganizationList.stream()
                .filter(organ -> OrganizationTypeEnum.DEPT.getValue().equals(organ.getDeptType()))
                .collect(Collectors.toMap(SysDept::getDeptCode, SysDept::getId));

    }


    /**
     * 每一条数据解析都会来调用
     *
     * 1. 数据校验，全字段校验
     * 2. 数据持久化
     * @param userImportVO easyExcel 每一行数据，是一个对象实例
     * @param analysisContext
     */
    @Override
    public void invoke(UserImportVO userImportVO, AnalysisContext analysisContext) {
        // 校验数据
        StringBuilder validationMsg = new StringBuilder();
        String userName = userImportVO.getUsername();
        String roleNames = userImportVO.getRoleNames();
        String deptCode = userImportVO.getDeptCode();

        // 校验用户名
        if (StringUtils.isBlank(userName)) {
            validationMsg.append("用户名不能为空");
        } else if (existingUsernames.contains(userName)) {
            validationMsg.append("用户名已存在");
        }

        // 校验部门编码
        if (StringUtils.isBlank(deptCode)) {
            validationMsg.append("部门编码不能为空");
        } else if (existingDeptCodes != null && !existingDeptCodes.contains(deptCode)) {
            validationMsg.append("系统中不存在这部门编码 ").append(deptCode);
        }

        // 校验角色名称
        if (roleNames != null && !existingRoleNames.contains(roleNames)) {
            validationMsg.append("系统中不存在这 ").append(roleNames).append(" 角色名称");
        }

        // 校验手机号
//        if (StringUtils.isBlank(userImportVO.getMobile())) {
//            validationMsg.append("手机号不能为空");
//        }


        if (validationMsg.isEmpty()) {
            SysUser user = this.convertToUser(userImportVO);
            user.setPassword(encodeDefaultPassword);
            // 性别翻译
            String genderLabel = userImportVO.getGender();
            if (StringUtils.isNotEmpty(genderLabel)) {
                Integer genderValue = (Integer) IBaseEnum.getValueByLabel(genderLabel, GenderEnum.class);
                user.setGender(genderValue);
            }

            // 报存到用户集合
            userList.add(user);


            // 构造 username 和 roleName 的映射关系集合
            if (StringUtils.isNotEmpty(roleNames)) {
                String[] roleName = roleNames.split(",");
                List<String> roleNameList = Arrays.asList(roleName);
                usernameRoleNamesMap.computeIfAbsent(userName, k -> new ArrayList<>()).addAll(roleNameList);
            }

            importVOList.add(userImportVO);

            validCount++;

        } else {
            // 校验检测失败结果
            ImportUserFailVO resultVO = new ImportUserFailVO();
            resultVO.setRowNum(validCount + invalidCount + 1); // 当前行
            resultVO.setMsg(validationMsg.toString());
            resultVO.setUsername(userName);
            resultVO.setRoleNames(roleNames);
            resultVO.setDeptName(deptCode); // 注意这里我们使用deptCode变量存储部门编码
            resultVO.setMobile(userImportVO.getMobile());
            resultVO.setEmail(userImportVO.getEmail());
            userFailVOList.add(resultVO);

            invalidCount++;
        }


    }


    private SysUser convertToUser(UserImportVO userImportVO) {
        SysUser user = new SysUser();
        user.setUsername(userImportVO.getUsername());
        user.setRealName(userImportVO.getRealName());
        user.setMobile(userImportVO.getMobile());
        user.setEmail(userImportVO.getEmail());
        Long deptId = deptCodeIdMap.get(userImportVO.getDeptCode());
        if (deptId != null) {
            // 这里暂时不保存，只记录映射关系，user.getUserId()是数据库自动生成，user.getUserId()现在还是null
            userIdDeptIdMap.put(user.getUserId(), deptId);
        }
        user.setStatus(StatusEnum.ENABLE.getValue());
        return user;
    }



    /**
     * 所有数据解析完成会来调用
     * @param analysisContext
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        try {
            // 批量保存用户
            boolean result = sysUserService.saveBatch(userList);
            
            if (!result) {
                log.error("批量保存用户失败");
                return;
            }

            // roleId 和 roleName映射集合
            Map<Long, String> roleIdRoleNameMap = roleService.list().stream()
                    .collect(Collectors.toMap(SysRole::getRoleId, SysRole::getRoleName));

            // userId 和 userName映射集合
            List<SysUser> sysUserList = sysUserService.list();
            Map<Long, String> userIdUserNameMap = sysUserList.stream()
                    .collect(Collectors.toMap(SysUser::getUserId, SysUser::getUsername));

            // 将用户和角色的映射关系转换为一个 SysUserRole 列表
            userRoleList = getUserRoleList(userIdUserNameMap, roleIdRoleNameMap);
            // 批量保存用户角色关联表
           if (CollectionUtils.isNotEmpty(userRoleList)) {
               boolean roleResult = userRoleService.saveBatch(userRoleList);
               if (!roleResult) {
                   log.error("批量保存用户角色关联失败");
               }
           }

           Map<String, Long> usernameToUserIdMap = sysUserList.stream()
                   .collect(Collectors.toMap(SysUser::getUsername, SysUser::getUserId));

           // 构建userId -> deptId 映射关系
            List<SysUserDept> userDeptList = new ArrayList<>();
           for (UserImportVO userImportVO : importVOList) {
               String userName = userImportVO.getUsername();
               String deptCode = userImportVO.getDeptCode();

               Long deptId = deptCodeIdMap.get(deptCode);
               Long userId = usernameToUserIdMap.get(userName);

               if (deptId != null && userId != null) {
                   userDeptList.add(new SysUserDept(userId, deptId));
               }
           }

           if (CollectionUtils.isNotEmpty(userDeptList)) {
               boolean deptResult = sysUserDeptService.saveBatch(userDeptList);
               if (!deptResult) {
                   log.error("批量保存用户部门关联失败");
               }
           }
        } catch (Exception e) {
            log.error("用户导入过程中发生异常", e);
            throw e; // 重新抛出异常，让上层能够捕获到
        }
    }


    /**
     * 将用户和角色的映射关系转换为一个 SysUserRole 列表
     * @param userIdUserNameMap
     * @param roleIdRoleNameMap
     * @return
     */
    private List<SysUserRole> getUserRoleList(Map<Long, String> userIdUserNameMap, Map<Long, String> roleIdRoleNameMap) {
        List<SysUserRole> userRoles = new ArrayList<>();

        Map<String, Long> usernameToUserIdMap = userIdUserNameMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));

        Map<String, Long> roleNameToRoleIdMap = roleIdRoleNameMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));

        for (Map.Entry<String, List<String>> entry : usernameRoleNamesMap.entrySet()) {
            String username = entry.getKey();
            List<String> roleNames = entry.getValue();

            // 通过 userIdUserNameMap 找到 usrId
            Long userId = usernameToUserIdMap.get(username);
            // 跳过未找到用户的记录
            if (userId == null) {
                continue;
            }

            // 通过 roleIdRoleNameMap 找到 roleId
            for (String roleName : roleNames) {
                Long roleId = roleNameToRoleIdMap.get(roleName);
                    // 未找到对应的 roleId，则跳过当前循环
                    if (roleId == null) {
                        continue;
                    }

                    SysUserRole userRole = new SysUserRole();
                    userRole.setUserId(userId);
                    userRole.setRoleId(roleId);
                    userRoles.add(userRole);
                }
            }


        return userRoles;
    }




    /**
     * @return 批量导入结果
     */
    @Override
    public ImportResult getResult() {
        ImportResult importResult = new ImportResult();
        importResult.setValidCount(validCount);
        importResult.setInvalidCount(invalidCount);
        return importResult;
    }
}
