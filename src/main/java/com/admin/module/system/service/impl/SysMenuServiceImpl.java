package com.admin.module.system.service.impl;

import com.admin.common.constant.CacheConstants;
import com.admin.common.constant.SystemConstants;
import com.admin.common.enums.DeletedEnum;
import com.admin.common.enums.MenuTypeEnum;
import com.admin.common.enums.StatusEnum;
import com.admin.module.system.dto.RouteDTO;
import com.admin.module.system.entity.SysMenu;
import com.admin.module.system.entity.SysRole;
import com.admin.module.system.entity.SysRoleMenu;
import com.admin.module.system.form.MenuForm;
import com.admin.module.system.mapper.SysMenuMapper;
import com.admin.module.system.query.MenuQuery;
import com.admin.module.system.service.SysMenuService;
import com.admin.module.system.service.SysRoleMenuService;
import com.admin.module.system.service.SysRoleService;
import com.admin.module.system.vo.KeyValueVO;
import com.admin.module.system.vo.MenuVO;
import com.admin.module.system.vo.OptionVO;
import com.admin.module.system.vo.RouteVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;


/**
 * 菜单
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements SysMenuService {

    private final SysRoleMenuService roleMenuService;

    private final SysRoleService roleService;

    private final RedisTemplate<String, Object> redisTemplate;



    /**
     * 菜单列表
     * 1.我们首先将原始菜单列表转换为一个Map，键是菜单ID，值是对应的菜单对象，便于通过ID快速查找菜单。
     * 2. 然后我们遍历这个扁平列表，找出所有parentId为0的菜单作为根节点，并对每个根节点调用addChildMenus方法填充其子节点。
     *  3.addChildMenus方法会递归地找到具有给定parentId的所有子菜单项，并将其添加到当前菜单的children列表中。
     *
     * @param menuQuery
     * @return
     */
    @Override
    public List<MenuVO> menuTree(MenuQuery menuQuery) {
        // 通过menuName模糊查询菜单表单
        LambdaQueryWrapper<SysMenu> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(menuQuery.getMenuName())) {
            queryWrapper.like(SysMenu::getMenuName, menuQuery.getMenuName());
        }
        queryWrapper.orderByAsc(SysMenu::getSort);
        List<SysMenu> menuList = this.list(queryWrapper);


        // 获取所有菜单id
        Set<Long> menuIds = menuList.stream()
                .map(SysMenu::getMenuId)
                .collect(toSet());

        // 获取所有父级 id
        Set<Long> parentIds = menuList.stream()
                .map(SysMenu::getParentId)
                .collect(toSet());

        // 获取根节点， 注意这里不能拿顶级菜单0作为根节点，菜单筛选时0会被过滤掉
        List<Long> rootIds = parentIds.stream()
                .filter(parentId -> !menuIds.contains(parentId))
                .collect(toList());

        // 从根节点开始构建菜单树
        List<MenuVO> menuVOList = rootIds.stream()
                .flatMap(rootId -> buildMenuTree(rootId, menuList).stream())
                .collect(toList());

        return menuVOList;
    }


    /**
     * 递归添加子菜单
     * @param parentId
     */
    private List<MenuVO> buildMenuTree(Long parentId, List<SysMenu> menuList) {
        List<MenuVO> menuVOList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(menuList)) {
            menuVOList = menuList.stream()
                    .filter(menu -> menu.getParentId().equals(parentId))
                    .map(menu -> {
                        MenuVO menuVO = this.convertToMenuVO(menu);
                        List<MenuVO> children = buildMenuTree(menu.getMenuId(), menuList);
                        menuVO.setChildren(children);
                        return menuVO;
                    })
                    .collect(toList());
        }

        return menuVOList;
    }

    /**
     * Menu 转 MenuVO
     * @param menu
     * @return
     */
    private MenuVO convertToMenuVO(SysMenu menu) {
        MenuVO menuVO = new MenuVO();
        menuVO.setMenuId(menu.getMenuId());
        menuVO.setParentId(menu.getParentId());
        menuVO.setMenuName(menu.getMenuName());
        menuVO.setMenuType(menu.getMenuType());
        menuVO.setRouteName(menu.getRouteName());
        menuVO.setRoutePath(menu.getRoutePath());
        menuVO.setComponent(menu.getComponent());
        menuVO.setIcon(menu.getIcon());
        menuVO.setRedirect(menu.getRedirect());
        menuVO.setSort(menu.getSort());
        menuVO.setStatus(menu.getStatus());
        menuVO.setPerm(menu.getPerm());
        return menuVO;
    }



    /**
     * 菜单下拉列表
     *我们使用Collectors.groupingBy函数将菜单列表按父ID分组到一个Map中，键为父ID，值为子菜单列表。然后，我们只遍历一次原始菜单列表，
     * 找出所有顶级菜单（即父ID为空或null的菜单）。对于每个顶级菜单，递归地构建Option树结构，通过预先构建好的Map来快速获取子菜单项。
     * @return
     */
    @Override
    public List<OptionVO> menuTreeOption() {
        LambdaQueryWrapper<SysMenu> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByAsc(SysMenu::getSort);
        List<SysMenu> menuList = this.list(queryWrapper);

        // 键为parentId, 值为子菜单列表
        Map<Long, List<SysMenu>> parentIdMap = menuList.stream()
                .collect(Collectors.groupingBy(SysMenu::getParentId));

        List<OptionVO> optionVOList = menuList.stream()
                .filter(menu -> SystemConstants.ROOT_NODE_ID.equals(menu.getParentId()))
                .map(menu -> buildOptionsTree(menu, parentIdMap))
                .collect(Collectors.toList());

        return optionVOList;
    }


    /**
     * 递归构建子菜单
     * @param menu
     * @param parentIdMap
     * @return
     */
    private OptionVO buildOptionsTree(SysMenu menu, Map<Long, List<SysMenu>> parentIdMap) {
        OptionVO optionVo = new OptionVO<>(menu.getMenuId(), menu.getMenuName());

        List<SysMenu> subMenus = parentIdMap.getOrDefault(menu.getMenuId(), Collections.emptyList());
        if (CollectionUtils.isNotEmpty(subMenus)) {
            List<OptionVO> subOptionVOS = subMenus.stream()
                    .map(subMenu -> buildOptionsTree(subMenu, parentIdMap))
                    .collect(Collectors.toList());

            optionVo.setChildren(subOptionVOS);
        }

        return optionVo;
    }









    /**
     *
     * @return 路由列表
     */
    @Override
    public List<RouteVO> selectRouteList(Set<String> roleCodes) {
        // 没有角色，返回空路由
        if (CollectionUtils.isEmpty(roleCodes)) {
            return Collections.emptyList();
        }

        // 根据角色，找出角色分配的菜单
        Set<Long> roleIds = roleService.selectRoleIds(roleCodes);
        List<Long> menuIds = roleMenuService.selectMenuIds(roleIds);

        // 菜单列表
        if (CollectionUtils.isEmpty(menuIds)) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<SysMenu> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.ne(SysMenu::getMenuType, MenuTypeEnum.BUTTON.getValue());
        queryWrapper.in(SysMenu::getMenuId, menuIds);
        queryWrapper.orderByAsc(SysMenu::getSort);
        List<SysMenu> menuList = this.list(queryWrapper);


        // 转换成 RouteBO并填充roleCode
        List<RouteDTO> routeDTOList = menuList.stream()
                .map(menu -> {
                    RouteDTO routeDto = this.convertToRouteBO(menu);
                    routeDto.setRoleCodes(roleCodes);
                    return routeDto;
                })
                .collect(toList());

       return buildTreeRoutes(0L, routeDTOList);
    }


    /**
     * SysMenu 转 RouteBO
     * @param menu
     * @return
     */
    private RouteDTO convertToRouteBO(SysMenu menu) {
        RouteDTO routeDto = new RouteDTO();
        routeDto.setMenuId(menu.getMenuId());
        routeDto.setParentId(menu.getParentId());
        routeDto.setMenuName(menu.getMenuName());
        routeDto.setMenuType(menu.getMenuType());
        routeDto.setPath(menu.getRoutePath());
        routeDto.setComponent(menu.getComponent());
        routeDto.setPerm(menu.getPerm());
        routeDto.setStatus(menu.getStatus());
        routeDto.setSort(menu.getSort());
        routeDto.setIcon(menu.getIcon());
        routeDto.setRedirect(menu.getRedirect());
        routeDto.setAlwaysShow(menu.getAlwaysShow());
        routeDto.setKeepAlive(menu.getKeepAlive());
        return routeDto;

    }


    /**
     * 递归生成路由层次列表
     * @param parentId 父
     * @param routeDTOList 菜单列表
     * @return 路由层次列表
     */
    private List<RouteVO> buildTreeRoutes(Long parentId, List<RouteDTO> routeDTOList) {
        List<RouteVO> routeList = new ArrayList<>();

        routeDTOList.forEach(routeDTO -> {
            if (routeDTO.getParentId().equals(parentId)) {
                RouteVO routeVO = this.toRouteVo(routeDTO);
                List<RouteVO> children = buildTreeRoutes(routeDTO.getMenuId(), routeDTOList);
                if (CollectionUtils.isNotEmpty(children)) {
                    routeVO.setChildren(children);
                }
                routeList.add(routeVO);
            }
        });

        return routeList;
    }


    /**
     * 根据RouteBO 创建RouteVO对象
     * @param routeDto
     * @return
     */
    private RouteVO toRouteVo(RouteDTO routeDto) {
        // 先分离字符串，再转换首字母大小
        String[] routePath = StringUtils.split(routeDto.getPath(), '-');
        // 路由 name 需要驼峰命名，首字母大写
        String routeName = StringUtils.capitalize(StringUtils.join(routePath, ""));

        RouteVO routeVO = new RouteVO();
        routeVO.setPath(routeDto.getPath());
        routeVO.setComponent(routeDto.getComponent());
        routeVO.setRedirect(routeDto.getRedirect());
        routeVO.setName(routeName);


        RouteVO.Meta meta = new RouteVO.Meta();
        meta.setTitle(routeDto.getMenuName());
        meta.setIcon(routeDto.getIcon());
//        meta.setRoles(routeDto.getRoleCodes());
        meta.setHideInMenu(StatusEnum.DISABLE.getValue().equals(routeDto.getStatus()));

        // 菜单是否开启页面缓存
        if (MenuTypeEnum.MENU.equals(routeDto.getMenuType())
                && ObjectUtils.nullSafeEquals(routeDto.getKeepAlive(), 1)) {
            meta.setKeepAlive(true);
        }

        // 目录 只有一个子路由是否始终显示
//        if (MenuTypeEnum.CATALOG.equals(routeBO.getMenuType())
//                && ObjectUtils.nullSafeEquals(routeBO.getAlwaysShow(), 1)) {
//            meta.setAlwaysShow(true);
//        }
//        meta.setAlwaysShow(ObjectUtils.nullSafeEquals(routeDto.getAlwaysShow(), 1));

        String paramsJson = routeDto.getParams();
        // 将 JSON 字符串转换为 Map<String, String>
        if (StringUtils.isNotBlank(paramsJson)) {
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                Map<String, String> paramMap = objectMapper.readValue(paramsJson, new TypeReference<>() {});
                meta.setQuery(paramMap);
            } catch (JsonProcessingException e) {
                log.error("解析参数失败", e);
                throw new RuntimeException("解析参数失败", e);
            }
        }

        routeVO.setMeta(meta);
        return routeVO;
    }


    /**
     * 新增或编辑菜单
     * @param menuForm
     * @return
     */
    @Override
    public boolean saveMenu(MenuForm menuForm) {
        String routePath = menuForm.getRoutePath();
        MenuTypeEnum menuType = menuForm.getMenuType();

        // 如果是目录类型, 且父级id 为根节点, 且路径不以正斜杠开头
        if (menuType == MenuTypeEnum.CATALOG ) {
            if (SystemConstants.ROOT_NODE_ID.equals(menuForm.getParentId()) && !routePath.startsWith("/")){
                // 在路径前添加斜杠
                menuForm.setRoutePath("/" + routePath);
            }
                menuForm.setComponent(SystemConstants.Layout);
            } else if (menuType == MenuTypeEnum.EXLINK) {
            menuForm.setComponent(null);
        }

        // MenuForm 对象转换 menu
        SysMenu menu = this.convertToMenu(menuForm);

        // treePath
        String treePath = generateMenuTreePath(menuForm.getParentId());
        menu.setTreePath(treePath);

        // 解析路由参数成字符串
        List<KeyValueVO> params = menuForm.getParams();
        this.paramsParsingToJson(params, menu);

        if (menuType != MenuTypeEnum.BUTTON && !this.checkRouteNameExist(menuForm.getRouteName(), null)) {
            menu.setRouteName(menuForm.getRouteName());
        }


        boolean result = this.save(menu);

        // 缓存菜单的权限标识
        if (result && StringUtils.isNotEmpty(menu.getPerm())) {
            this.refreshRolePermsCache();
        }
        return result;
    }



    /**
     * 检测路由名称是否存在
     * @param routeName
     * @param id
     * @return
     */
    private boolean checkRouteNameExist(String routeName, Long id) {
        LambdaQueryWrapper<SysMenu> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysMenu::getMenuName, routeName);
        if (id != null) {
            queryWrapper.ne(SysMenu::getMenuId, id);
        }
        boolean result = this.exists(queryWrapper);
        return result;
    }


    /**
     * 解析路由参数成字符串
     * 路由参数 [{key: "id", value: "1"}, {key：“name", value: "张三"] 转换为 [{"id": "1", {"name": "张三"]
     *
     * @param params
     * @param menu
     */
    private void paramsParsingToJson(List<KeyValueVO> params, SysMenu menu) {
        if (CollectionUtils.isNotEmpty(params)) {
            // 先转换陈 map 列表
            List<Map<String, String>> mapParams = params.stream()
                    .map(kv -> Map.of(kv.getKey(), kv.getValue()))
                    .collect(toList());

            // 转换成 json 字符串
            Gson gson = new Gson();
            String jsonStr = gson.toJson(mapParams);
            menu.setParams(jsonStr);
        } else {
            menu.setParams(null);
        }
    }

    /**
     * 编辑菜单
     *
     * @param menuForm
     * @return
     */
    @Override
    public boolean updateMenu(MenuForm menuForm) {
        String routePath = menuForm.getRoutePath();
        MenuTypeEnum menuType = menuForm.getMenuType();

        // 如果是目录类型, 且父级id 为根节点, 且路径不以正斜杠开头
        if (menuType == MenuTypeEnum.CATALOG ) {
            if (SystemConstants.ROOT_NODE_ID.equals(menuForm.getParentId()) && !routePath.startsWith("/")){
                // 在路径前添加斜杠
                menuForm.setRoutePath("/" + routePath);
            }
            menuForm.setComponent(SystemConstants.Layout);
        } else if (menuType == MenuTypeEnum.EXLINK) {
            menuForm.setComponent(null);
        }

        // MenuForm 对象转换 menu
        SysMenu menu = this.convertToMenu(menuForm);
        String treePath = generateMenuTreePath(menuForm.getParentId());
        menu.setTreePath(treePath);

        // 解析路由参数成字符串
        List<KeyValueVO> params = menuForm.getParams();
        this.paramsParsingToJson(params, menu);


        if (menuType != MenuTypeEnum.BUTTON && !this.checkRouteNameExist(menuForm.getRouteName(), menuForm.getMenuId())) {
            menu.setRouteName(menuForm.getRouteName());
        }

        // 获取权限标识
        SysMenu sysMenu = this.getById(menuForm.getMenuId());
        String newPerm = menuForm.getPerm();
        String oldPerm = sysMenu.getPerm();

        // 更详细菜单
        boolean result = this.updateById(menu);
        // 更新角色权限缓存
        if (result) {
            if (StringUtils.isNotBlank(oldPerm)  && !oldPerm.equals(newPerm)) {
                this.refreshRolePermsCache();
            }

        }

        // 修改中间层次菜单的父菜单，如果菜单有子菜单，则更新子菜单的树路径
        updateChildrenTreePath(sysMenu.getMenuId(), treePath);
        return result;
    }


    /**
     * 菜单路径生成
     * @param parentId 父id
     * @return 父节点路径以英文逗号(,)分隔
     */
    private String generateMenuTreePath(Long parentId) {
        if (SystemConstants.ROOT_NODE_ID.equals(parentId)) {
            return String.valueOf(parentId);
        } else {
            SysMenu parent = this.getById(parentId);
            return parent != null ? parent.getTreePath() + "," + parent.getMenuId() : null;
        }
    }


    /**
     * 更新子菜单树路径
     * @param id 当前菜单id
     * @param treePath 当前菜单树路径
     */
    private void updateChildrenTreePath(Long id, String treePath) {
        List<SysMenu> children = this.list(new LambdaQueryWrapper<SysMenu>().eq(SysMenu::getParentId, id));
        if (CollectionUtils.isNotEmpty(children)) {
            // 子菜单的树路径等于父菜单的树路径加上父菜单id
            String childTreePath = treePath + "," + id;
            this.update(new LambdaUpdateWrapper<SysMenu>()
                    .eq(SysMenu::getParentId, id)
                    .set(SysMenu::getTreePath, childTreePath)
            );

            for (SysMenu sysMenu : children) {
                // 递归更新子菜单的treePath
                updateChildrenTreePath(sysMenu.getMenuId(), childTreePath);
            }
        }
    }


    /**
     * MenuForm 转 Menu
     * @param menuForm
     * @return
     */
    private  SysMenu convertToMenu(MenuForm menuForm) {
        SysMenu menu = new SysMenu();
        menu.setMenuId(menuForm.getMenuId());
        menu.setParentId(menuForm.getParentId());
        menu.setTreePath(menuForm.getTreePath());
        menu.setMenuName(menuForm.getMenuName());
        menu.setMenuType(menuForm.getMenuType());
        menu.setRoutePath(menuForm.getRoutePath());
        menu.setComponent(menuForm.getComponent());
        menu.setRedirect(menuForm.getRedirect());
        menu.setPerm(menuForm.getPerm());
        menu.setStatus(menuForm.getStatus());
        menu.setSort(menuForm.getSort());
        menu.setIcon(menuForm.getIcon());
        menu.setAlwaysShow(menuForm.getAlwaysShow());
        menu.setKeepAlive(menuForm.getKeepAlive());
        return menu;
    }



    /**
     * 获取菜单详情，(编辑菜单)
     *
     * @param menuId
     * @return
     */
    @Override
    public MenuForm getMenuDetail(Long menuId) {
        SysMenu menu = this.getById(menuId);
        MenuForm menuForm = this.convertToMenuForm(menu);

        //[{"id": "1", {"name": "张三"] 转换为[{key: "id", value: "1"}, {key：“name", value: "张三"]
        String params = menu.getParams();
        if (StringUtils.isNotBlank(params)) {
            ObjectMapper objectMapper = new ObjectMapper();

            try {
                // 解析 JSON 字符串为 Map<String, String>
                Map<String, String> paramMap = objectMapper.readValue(params, new TypeReference<>() {});

                // 转换为 [{key: "id", value: "1"}, {key：“name", value: "张三"]
                List<KeyValueVO> keyValueVOList = paramMap.entrySet().stream()
                        .map(entry -> new KeyValueVO(entry.getKey(), entry.getValue()))
                        .collect(toList());

                // 将转换后的列表存入 menuForm
                menuForm.setParams(keyValueVOList);
            } catch (JsonProcessingException e) {
                log.error("解析参数失败", e);
                throw new RuntimeException("解析参数失败", e);
            }
        }

        return menuForm;
    }




    /**
     * Menu 转 MenuForm
     * @param menu
     * @return
     */
    private  MenuForm convertToMenuForm(SysMenu menu) {
        MenuForm menuForm = new MenuForm();
        menuForm.setMenuId(menu.getMenuId());
        menuForm.setParentId(menu.getParentId());
        menuForm.setTreePath(menu.getTreePath());
        menuForm.setMenuName(menu.getMenuName());
        menuForm.setMenuType(menu.getMenuType());
        menuForm.setRouteName(menu.getRouteName());
        menuForm.setRoutePath(menu.getRoutePath());
        menuForm.setComponent(menu.getComponent());
        menuForm.setRedirect(menu.getRedirect());
        menuForm.setPerm(menu.getPerm());
        menuForm.setStatus(menu.getStatus());
        menuForm.setSort(menu.getSort());
        menuForm.setIcon(menu.getIcon());
        menuForm.setAlwaysShow(menu.getAlwaysShow());
        menuForm.setKeepAlive(menu.getKeepAlive());
        return menuForm;
    }







    /**
     * 批量删除菜单
     *
     * @param menuId
     * @return
     */
    @Transactional
    @Override
    public boolean deleteMenu(Long menuId) {
        LambdaUpdateWrapper<SysMenu> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(SysMenu::getMenuId, menuId)
                .or()
                .apply("CONCAT (',',tree_path,',') LIKE CONCAT('%,',{0},',%')", menuId);
        boolean result = this.remove(updateWrapper);

        if (result) {
            // 刷新角色权限缓存
            this.refreshRolePermsCache();
        }

        return result;
    }


    /**
     * 获取菜单权限
     *
     * @param menuIds
     * @return
     */
    @Override
    public Set<String> selectMenuPerms(List<Long> menuIds) {
        LambdaQueryWrapper<SysMenu> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(SysMenu::getMenuId, menuIds);
        queryWrapper.isNotNull(SysMenu::getPerm);
        List<SysMenu> menuList = this.list(queryWrapper);

        Set<String> perms = menuList.stream()
                .map(SysMenu::getPerm)
                .collect(toSet());
        return perms;
    }



    /**
     * 更新角色的菜单资源
     * @param roleId
     * @param menuIds
     * @return
     */
    @Override
    public boolean updateRoleMenuList(Long roleId, List<Long> menuIds) {

        // 删除角色菜单的关联
        roleMenuService.deleteRoleMenu(roleId);

        if (CollectionUtils.isNotEmpty(menuIds)) {
            // 构建新增角色菜单的关联
            List<SysRoleMenu> roleMenuList = menuIds
                    .stream()
                    .map(menuId -> new SysRoleMenu(roleId, menuId))
                    .collect(Collectors.toList());
            // 批量新增角色菜单的关联
            roleMenuService.saveBatch(roleMenuList);
        }

        // 刷新角色缓存中的权限
        this.refreshRolePermsCache(roleId);

        return true;
    }


    /**
     * 初始化缓存
     */
    @PostConstruct
    public void initRolePermsCache() {
        this.refreshRolePermsCache();
    }


    /**
     * 刷新权限缓存 (所有角色)
     */
    @Override
    public void refreshRolePermsCache() {
        // 清理权限缓存
        redisTemplate.opsForHash().delete(CacheConstants.ROLE_PERMS_PREFIX, "*");

        // 角色列表
        List<SysRole> roleList = roleService.selectRoleList();

        // 角色id 集合
        List<Long> roleIds  = roleList.stream()
                .map(SysRole::getRoleId)
                .collect(Collectors.toList());



        // 角色菜单列表
        List<SysRoleMenu> roleMenuList = roleMenuService.selectRoleMenus(roleIds);

        // menuIds 集合
        List<Long> menuIds = roleMenuList.stream()
                .map(SysRoleMenu::getMenuId)
                .collect(Collectors.toList());

        // menuId 和 perm的关联映射
        Map<Long, String> menuIdPermMap = this.getMenuIdPermMap(menuIds);

        // roleId 和 menuIds的关联关系
        Map<Long, List<Long>> roleIdMenuIdsMap = this.roleIdMenuIdsMap(roleMenuList);



        // 构造角色权限DTO列表
        roleList.forEach(role -> {
            // 获取角色对应的所有menuIds
            List<Long> roleMenuIds = roleIdMenuIdsMap.getOrDefault(role.getRoleId(), Collections.emptyList());

            // 根据menuId 获取权限字符串
            Set<String> perms = roleMenuIds.stream()
                    .map(menuId -> menuIdPermMap.getOrDefault(menuId, ""))
                    .collect(Collectors.toSet());

            // 存储角色权限到redis中
            redisTemplate.opsForHash().put(CacheConstants.ROLE_PERMS_PREFIX, role.getRoleCode(), perms);
        });


    }




    /**
     * 查询 menuId 和 perm 的映射关系
     *
     * @param menuIds
     * @return
     */
    private Map<Long, String> getMenuIdPermMap(List<Long> menuIds) {
        LambdaQueryWrapper<SysMenu> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(SysMenu::getMenuId, menuIds);
        queryWrapper.eq(SysMenu::getStatus, StatusEnum.ENABLE.getValue());
        queryWrapper.eq(SysMenu::getDeleted, DeletedEnum.NO_DELETE.getValue());
        List<SysMenu> menuList = this.list(queryWrapper);

        Map<Long, String> menuIdPermMap = menuList.stream()
                .filter(menu -> StringUtils.isNotBlank(menu.getPerm()))
                .collect(Collectors.toMap(SysMenu::getMenuId, SysMenu::getPerm));
        return menuIdPermMap;
    }

    /**
     * roleId 和 menuIds的关联关系
     * @param roleMenuList
     * @return
     */
    private Map<Long, List<Long>> roleIdMenuIdsMap(List<SysRoleMenu> roleMenuList) {
        Map<Long, List<Long>> roleIdMenuIdsMap = roleMenuList.stream()
                .collect(Collectors.groupingBy(
                        SysRoleMenu::getRoleId,
                        Collectors.mapping(SysRoleMenu::getMenuId, Collectors.toList())
                ));
        return roleIdMenuIdsMap;
    }



    /**
     * 更新指定角色缓存
     *
     * @param roleId
     */
    @Override
    public void refreshRolePermsCache(Long roleId) {

        SysRole role = roleService.getOne(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getRoleId, roleId)
                .last("limit 1"));

        String roleCode = role.getRoleCode();

        // 清理权限缓存
        redisTemplate.opsForHash().delete(CacheConstants.ROLE_PERMS_PREFIX, roleCode);



        List<Long> menuIds = roleMenuService.selectMenuIds(roleId);

        if (CollectionUtils.isNotEmpty(menuIds)) {
            LambdaQueryWrapper<SysMenu> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.in(SysMenu::getMenuId, menuIds);
            queryWrapper.eq(SysMenu::getDeleted, DeletedEnum.NO_DELETE.getValue());
            queryWrapper.eq(SysMenu::getStatus, StatusEnum.ENABLE.getValue());
            List<SysMenu> menuList = this.list(queryWrapper);

            Set<String> perms = menuList.stream()
                    .map(SysMenu::getPerm)
                    .collect(Collectors.toSet());

            redisTemplate.opsForHash().put(CacheConstants.ROLE_PERMS_PREFIX, roleCode, perms);
        } else {
            redisTemplate.opsForHash().put(CacheConstants.ROLE_PERMS_PREFIX, roleCode, Collections.emptySet());
        }

    }


}
