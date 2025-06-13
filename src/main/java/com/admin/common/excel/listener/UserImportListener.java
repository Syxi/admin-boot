package com.admin.common.excel.listener;

import com.admin.common.constant.SystemConstants;
import com.admin.common.enums.*;
import com.admin.common.excel.ImportResult;
import com.admin.common.excel.ImportUserFailVO;
import com.admin.common.excel.importvo.UserImportVO;
import com.admin.common.util.SpringContextUtil;
import com.admin.module.system.entity.SysDept;
import com.admin.module.system.entity.SysRole;
import com.admin.module.system.entity.SysUser;
import com.admin.module.system.entity.SysUserRole;
import com.admin.module.system.service.SysDeptService;
import com.admin.module.system.service.SysRoleService;
import com.admin.module.system.service.SysUserRoleService;
import com.admin.module.system.service.SysUserService;
import com.alibaba.excel.context.AnalysisContext;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.extern.slf4j.Slf4j;
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

    private final SysDeptService organizationService;

    private final String encodeDefaultPassword;

    private final Set<String> existingUsernames;

    private final Set<String> existingRoleNames;

    private final Set<String> existingOrganNames;

    private final Set<String> existingDeptNames;

    private final Map<String, Long> deptNameOrganIdMap;

    private final Map<Long, String> parantIdOrganNameMap;

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

    // 校验结果列表
    List<ImportUserFailVO> userFailVOList = new ArrayList<>();





    public UserImportListener() {
        PasswordEncoder passwordEncoder = SpringContextUtil.getBean(PasswordEncoder.class);
        this.sysUserService = SpringContextUtil.getBean(SysUserService.class);
        this.userRoleService = SpringContextUtil.getBean(SysUserRoleService.class);
        this.roleService = SpringContextUtil.getBean(SysRoleService.class);
        this.organizationService = SpringContextUtil.getBean(SysDeptService.class);

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
        List<SysDept> sysOrganizationList = organizationService.list(wrapper);

        // 加载所有组织名称
        this.existingOrganNames = sysOrganizationList.stream()
                .filter(organ ->  OrganizationTypeEnum.ORGANIZATION.getValue().equals(organ.getDeptType()))
                .map(SysDept::getDeptName)
                .collect(Collectors.toSet());

        // 加载所有部门名称
        this.existingDeptNames = sysOrganizationList.stream()
                .filter(organ -> OrganizationTypeEnum.DEPT.getValue().equals(organ.getDeptType()))
                .map(SysDept::getDeptName)
                .collect(Collectors.toSet());

        // id 映射 机构名称
        this.parantIdOrganNameMap = sysOrganizationList.stream()
                .filter(organ ->  OrganizationTypeEnum.ORGANIZATION.getValue().equals(organ.getDeptType()))
                .collect(Collectors.toMap(SysDept::getId, SysDept::getDeptName));

        // 机构-部门名称组合映射到id
        this.deptNameOrganIdMap = sysOrganizationList.stream()
                .filter(organ -> OrganizationTypeEnum.DEPT.getValue().equals(organ.getDeptType()))
                .collect(Collectors.toMap(
                        organ -> parantIdOrganNameMap.get(organ.getParentId()) + "-" + organ.getDeptName(),
                        SysDept::getId));

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
        String organName = userImportVO.getOrganName();
        String deptName = userImportVO.getDeptName();

        // 校验用户名
        if (StringUtils.isBlank(userName)) {
            validationMsg.append("用户名不能为空");
        } else if (existingUsernames.contains(userName)) {
            validationMsg.append("用户名已存在");
        }

        // 校验组织
        if (StringUtils.isBlank(organName)) {
            validationMsg.append("机构名称不能为空");
        } else if (!existingOrganNames.contains(organName)) {
            validationMsg.append("系统中不存在这机构 ").append(organName);
        }

        // 校验部门名称
        if (existingDeptNames != null && !existingDeptNames.contains(deptName)) {
            validationMsg.append("系统中不存在这部门 ").append(deptName);
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

            validCount++;

        } else {
            // 校验检测失败结果
            ImportUserFailVO resultVO = new ImportUserFailVO();
            resultVO.setRowNum(validCount + invalidCount + 1); // 当前行
            resultVO.setMsg(validationMsg.toString());
            resultVO.setUsername(userName);
            resultVO.setRoleNames(roleNames);
            resultVO.setOrganName(organName);
            resultVO.setDeptName(deptName);
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
        Long organId = deptNameOrganIdMap.get(userImportVO.getOrganName() + "-" + userImportVO.getDeptName());
        user.setOrganId(organId);
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
        // 批量保存用户
        boolean result = sysUserService.saveBatch(userList);

        // roleId 和 roleName映射集合
        Map<Long, String> roleIdRoleNameMap = roleService.list().stream()
                .collect(Collectors.toMap(SysRole::getRoleId, SysRole::getRoleName));

        if (result) {
            // userId 和 userName映射集合
            Map<Long, String> userIdUserNameMap = sysUserService.list().stream()
                    .collect(Collectors.toMap(SysUser::getUserId, SysUser::getUsername));

            // 将用户和角色的映射关系转换为一个 SysUserRole 列表
            userRoleList = getUserRoleList(userIdUserNameMap, roleIdRoleNameMap);
            // 批量保存用户角色关联表
            userRoleService.saveBatch(userRoleList);
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
