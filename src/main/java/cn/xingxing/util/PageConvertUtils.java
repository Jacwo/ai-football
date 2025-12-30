package cn.xingxing.util;


import cn.hutool.core.bean.BeanUtil;
import cn.xingxing.dto.PageVO;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 分页转换工具类
 *
 * @author jiaquanwei
 */
public class PageConvertUtils {

    /**
     * 将MyBatis-Plus的Page对象转换为自定义PageVO对象
     *
     * @param page MyBatis-Plus的Page对象
     * @param <T>  数据类型
     * @return 自定义PageVO对象
     */
    public static <T> PageVO<T> convert(IPage<T> page) {
        if (page == null) {
            return empty();
        }

        return new PageVO<>(
                page.getCurrent(),
                page.getSize(),
                page.getTotal(),
                page.getRecords()
        );
    }

    /**
     * 将MyBatis-Plus的Page对象转换为自定义PageVO对象
     *
     * @param page MyBatis-Plus的Page对象
     * @param <T>  from数据类型
     * @param <R>  to数据类型
     * @return 自定义PageVO对象
     */
    public static <T, R> PageVO<R> convert(IPage<T> page, Class<R> source) {
        if (page == null) {
            return empty();
        }
        return new PageVO<>(
                page.getCurrent(),
                page.getSize(),
                page.getTotal(),
                BeanUtil.copyToList(page.getRecords(), source)
        );
    }

    /**
     * 将MyBatis-Plus的Page对象转换为自定义PageVO对象（带记录转换）
     *
     * @param page      MyBatis-Plus的Page对象
     * @param converter 记录转换函数
     * @param <T>       源数据类型
     * @param <R>       目标数据类型
     * @return 自定义PageVO对象
     */
    public static <T, R> PageVO<R> convert(IPage<T> page, Function<T, R> converter) {
        if (page == null) {
            return empty();
        }

        List<R> convertedRecords = page.getRecords()
                .stream()
                .map(converter)
                .collect(Collectors.toList());

        return new PageVO<>(
                page.getCurrent(),
                page.getSize(),
                page.getTotal(),
                convertedRecords
        );
    }

    /**
     * 创建空的分页结果
     *
     * @param <T> 数据类型
     * @return 空的PageVO对象
     */
    public static <T> PageVO<T> empty() {
        return new PageVO<>(0L, 0L, 0L, null);
    }

    /**
     * 创建成功的分页结果（简化版）
     *
     * @param page MyBatis-Plus的Page对象
     * @param <T>  数据类型
     * @return 自定义PageVO对象
     */
    public static <T> PageVO<T> success(IPage<T> page) {
        return convert(page);
    }
}