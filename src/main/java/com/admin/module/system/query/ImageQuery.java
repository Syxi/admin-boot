package com.admin.module.system.query;

import com.admin.common.base.BasePage;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class ImageQuery extends BasePage {

    private String imageName;
}
