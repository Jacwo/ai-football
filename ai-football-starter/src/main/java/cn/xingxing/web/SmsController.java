package cn.xingxing.web;


import cn.xingxing.dto.ApiResponse;
import cn.xingxing.dto.sms.SendSmsDto;
import cn.xingxing.service.SmsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author: yangyuanliang
 * @Date: 2026-01-09
 * @Version: 1.0
 */
@RestController
@RequestMapping("/api")
public class SmsController {
    @Autowired
    private SmsService smsService;
    @PostMapping("/sms/send")
    public ApiResponse<Boolean> sendSms(@RequestBody SendSmsDto sendSmsDto) {
        return ApiResponse.success(smsService.sendSms(sendSmsDto.getPhone()));
    }
}
