package cn.xingxing.mapper;


import cn.xingxing.entity.SmsInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;

/**
 * @Author: yangyuanliang
 * @Date: 2026-01-09
 * @Version: 1.0
 */
public interface SmsInfoMapper extends BaseMapper<SmsInfo> {
	@Delete("DELETE FROM sms_info WHERE phone = #{phone} and code = #{code}")
	boolean deleteByPhoneAndCode(@Param("phone") String phone,@Param("code") String code);
}
