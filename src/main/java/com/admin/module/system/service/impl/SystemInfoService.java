package com.admin.module.system.service.impl;

import com.admin.module.system.vo.SystemInfoVO;
import org.springframework.stereotype.Service;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.FileSystem;
import oshi.software.os.OSFileStore;
import oshi.software.os.OperatingSystem;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Service
public class SystemInfoService {
    private final HardwareAbstractionLayer hardware;
    private final OperatingSystem os;

    public SystemInfoService() {
        SystemInfo systemInfo = new SystemInfo();
        this.hardware = systemInfo.getHardware();
        this.os = systemInfo.getOperatingSystem();
    }

    public SystemInfoVO getSystemInfoVO() {
        SystemInfoVO systemInfoVO = new SystemInfoVO();

        // 操作系统信息
        Properties props = System.getProperties();
        systemInfoVO.setOsName(props.getProperty("os.name"));
        systemInfoVO.setOsVersion(props.getProperty("os.version"));
        systemInfoVO.setOsArch(props.getProperty("os.arch"));

        // cpu使用率
        CentralProcessor processor = hardware.getProcessor();
        double cpuLoad = processor.getSystemCpuLoad(1000) ;
        if (cpuLoad < 0) {
            cpuLoad = 0.0;
        }
        // 保留一位小数
        systemInfoVO.setCpuLoad(Math.round(cpuLoad * 1000.0) / 10.0);

        // 内存信息（单位：GB）
        GlobalMemory memory = hardware.getMemory();
        double totalMemory = memory.getTotal() / (1024.0 * 1024.0 * 1024.0);
        double usedMemory =( memory.getTotal() - memory.getAvailable()) / (1024.0 * 1024.0 * 1024.0);
        double freeMemory =memory.getAvailable() / (1024.0 * 1024.0 * 1024.0);

        // 保留一位小数
        totalMemory = Math.round(totalMemory * 10.0) / 10.0;
        usedMemory = Math.round(usedMemory * 10.0) / 10.0;
        freeMemory = Math.round(freeMemory * 10.0) / 10.0;

        systemInfoVO.setTotalMemory(totalMemory);
        systemInfoVO.setUsedMemory(usedMemory);
        systemInfoVO.setFreeMemory(freeMemory);

        // 磁盘信息 （单位：GB）
        List<SystemInfoVO.DiskInfo> disks = new ArrayList<>();
        FileSystem fileSystem = os.getFileSystem();
        OSFileStore[] fileStores = fileSystem.getFileStores(true).toArray(new OSFileStore[0]);

        for (OSFileStore fileStore : fileStores) {
            double totalSpace = fileStore.getTotalSpace() / (1024.0 * 1024.0 * 1024.0);
            double freeSpace = fileStore.getUsableSpace() / (1024.0 * 1024.0 * 1024.0);
            double usedSpace = totalSpace - freeSpace;
            // 保留一位小数
            totalSpace = Math.round(totalSpace * 10.0) / 10.0;
            freeSpace = Math.round(freeSpace * 10.0) / 10.0;
            usedSpace = Math.round(usedSpace * 10.0) / 10.0;

            disks.add(new SystemInfoVO.DiskInfo(
                    fileStore.getName(),
                    fileStore.getMount(),
                    totalSpace,
                    usedSpace,
                    freeSpace
                    ));
        }
        systemInfoVO.setDisks(disks);

        return systemInfoVO;
    }
}
