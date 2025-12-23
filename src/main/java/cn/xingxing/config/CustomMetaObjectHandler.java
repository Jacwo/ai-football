package cn.xingxing.config;


import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * mybatis-plus 字段自动填充
 *
 * @author zhenguo.yao 2022/6/22 14:32
 */
@Slf4j
@Primary
@Component
@RequiredArgsConstructor
public class CustomMetaObjectHandler implements MetaObjectHandler {


    private static final String CREATE_TIME = "createTime";
    private static final String CREATE_USER_ID = "createUserId";
    private static final String CREATE_USER_NAME = "createUserName";
    private static final String UPDATED_TIME = "updateTime";
    private static final String UPDATED_USER_ID = "updateUserId";
    private static final String UPDATED_USER_NAME = "updateUserName";

    @Override
    public void insertFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();
        this.strictInsertFill(metaObject, CREATE_TIME, LocalDateTime.class, now);
        this.strictInsertFill(metaObject, UPDATED_TIME, LocalDateTime.class, now);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.setFieldValByName(UPDATED_TIME, LocalDateTime.now(), metaObject);

    }

//    @Override
//    public MetaObjectHandler strictFillStrategy(MetaObject metaObject, String fieldName, Supplier<Object> fieldVal) {
//        Object obj = fieldVal.get();
//        if (Objects.nonNull(obj)) {
//            metaObject.setValue(fieldName, obj);
//        }
//        return this;
//    }

}
