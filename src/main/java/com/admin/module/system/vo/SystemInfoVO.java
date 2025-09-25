package com.admin.module.system.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
public class SystemInfoVO {
    private String osName;
    private String osVersion;
    private String osArch;
    private double cpuLoad;
    private double totalMemory;
    private double usedMemory;
    private double freeMemory;
    private List<DiskInfo> disks;

    @AllArgsConstructor
    @Data
    public static class DiskInfo {
        private String name;
        private String mountPoint;
        private double totalSpace;
        private double usedSpace;
        private double freeSpace;
    }
}
