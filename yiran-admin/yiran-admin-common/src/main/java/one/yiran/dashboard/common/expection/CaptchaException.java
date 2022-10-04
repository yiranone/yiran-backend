package one.yiran.dashboard.common.expection;

import one.yiran.common.exception.BusinessException;

/**
 * 验证码错误异常类
 */
public class CaptchaException extends BusinessException {
    private static final long serialVersionUID = 1L;

    public CaptchaException() {
        super(400, "验证码错误");
    }

    public CaptchaException(String msg) {
        super(400, msg);
    }
}
