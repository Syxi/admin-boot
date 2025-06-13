package com.admin.module.system.form;

import com.admin.module.system.vo.DeptVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "组织")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeptForm {

    @Schema(description = "组织id")
    private Long id;

    @Schema(description = "组织名称")
    private String deptName;

    @Schema(description = "组织编码")
    private String deptCode;

    @Schema(description = "组织类型 0：组织，1：部门")
    private Integer deptType;

    @Schema(description = "父节点id")
    private Long parentId;

    @Schema(description = "父节点路径")
    private String treePath;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "状态")
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Long createUser;

    private Long updateUser;

    @Schema(description = "组织图标")
    private String deptImg;

    @Schema(description = "组织简介")
    private String deptIntroduction;


    @Schema(description = "组织联系电话")
    private String deptPhone;


    @Schema(description = "组织地址")
    private String deptAddress;


    @Schema(description = "备注")
    private String remark;

    @Schema(description = "组织子列表")
    private List<DeptVO> children;



}
