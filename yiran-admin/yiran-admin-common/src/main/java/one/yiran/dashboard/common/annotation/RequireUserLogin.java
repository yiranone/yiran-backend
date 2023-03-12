package one.yiran.dashboard.common.annotation;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 允许管理员登陆。客户端的用户为Member 后台管理员为User
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireUserLogin {
    String[] roles() default {""};
    String[] permissions() default {""};
}