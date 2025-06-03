package com.admin.common.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * 发送信息给钉钉机器人
 */
@Slf4j
public class DingDingRobotUtil {

    // 在钉钉创建群聊时，获取到 WEBHOOK
    // private static  final String DINGDING_ROBOT_WEBHOOK = "https://oapi.dingtalk.com/robot/send?access_token=f1fac0ac50deb52eafcf7dab30545b7b244f4552a80a2c25333eee7d322cd7e7";
    private static  final String DINGDING_ROBOT_WEBHOOK = "";

    /**
     * 发送消息给钉钉机器人
     * @param message
     */
    public static void sendDingDingRobotWebHook(String message) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 构造符合钉钉机器人的 json 格式
        Map<String, Object> data = new HashMap<>();
        data.put("msgtype", "text");
        Map<String, String> textData = new HashMap<>();
        textData.put("content", message + "请检查");  // 自定义的返回信息
        data.put("text", textData);

        HttpEntity<Map<String, Object>> httpEntity = new HttpEntity<>(data, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(DINGDING_ROBOT_WEBHOOK, httpEntity, String.class);
        log.info(response.getBody());
    }

}
