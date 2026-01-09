package cn.xingxing.exception;

import lombok.Getter;

/**
 * 公共异常类
 *
 * @author lilei
 */
@Getter
public class CommonException extends RuntimeException {

    /**
     * 错误代码
     */
    private int code;

    /**
     * 错误信息
     */
    private String message;

    public CommonException() {
        super();
    }


    public CommonException(int code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public CommonException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.message = message;
    }

}
