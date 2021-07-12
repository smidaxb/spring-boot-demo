package com.xkcoding.cache.redis.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by yusong on 2018/5/29.
 * AOP工具类
 */
public class AopUtil {

    /**
     * 获取被拦截方法对象
     * <p>
     * MethodSignature.getMethod() 获取的是顶层接口或者父类的方法对象
     * 而缓存的注解在实现类的方法上
     * 所以应该使用反射获取当前对象的方法对象
     */
    public static Method getMethod(ProceedingJoinPoint pjp) {
        //获取参数的类型
        Object[] args = pjp.getArgs();
        Class[] argTypes = new Class[pjp.getArgs().length];
        for (int i = 0; i < args.length; i++) {
            argTypes[i] = fixType(args[i].getClass());
        }
        Method method = null;
        try {
            method = pjp.getTarget().getClass().getMethod(pjp.getSignature().getName(), argTypes);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        return method;
    }


    /**
     * 方法中使用的集合类替换成接口
     * 不然通过getMethod 可能因为参数类型不同而找不到方法
     * @param clazz
     * @return
     */
    private static Class fixType(Class clazz) {
        String className = clazz.getName();
        if (("java.util.ArrayList").equalsIgnoreCase(className) || ("java.util.LinkedList").equalsIgnoreCase(className)) {
            return List.class;
        } else if (("java.util.HashMap").equalsIgnoreCase(className) || ("java.util.LinkedHashMap").equalsIgnoreCase(className)) {
            return Map.class;
        } else if (("java.util.HashSet").equalsIgnoreCase(className) || ("java.util.TreeSet").equalsIgnoreCase(className)) {
            return Set.class;
        }
        return clazz;
    }

    /**
     * 将参数中的ID Value通过表达式放入到注解的值中
     */
    public static String addId(String originKey, ProceedingJoinPoint pjp) {
        Method method = getMethod(pjp);

        LocalVariableTableParameterNameDiscoverer nameDiscoverer = new LocalVariableTableParameterNameDiscoverer();
        String[] paraNameArr = nameDiscoverer.getParameterNames(method);
        Object[] args = pjp.getArgs();
        //SPEL上下文
        EvaluationContext context = new StandardEvaluationContext();
        //把方法参数放入SPEL上下文中
        if (null != paraNameArr) {
            for (int i = 0; i < paraNameArr.length; i++) {
                context.setVariable(paraNameArr[i], args[i]);
            }
        }

        String newKey = originKey;
        if (null != paraNameArr && paraNameArr.length > 0) {
            ExpressionParser parser = new SpelExpressionParser();
            Expression expression = parser.parseExpression(originKey,
                    new TemplateParserContext());
            newKey = expression.getValue(context, String.class);
        }
        return newKey;
    }


    /**
     * 获取缓存的key
     * key 定义在注解上，支持SPEL表达式
     *
     * @return
     */
    public static String parseStringArg(String key, Method method, Object[] args) {
        LocalVariableTableParameterNameDiscoverer u = new LocalVariableTableParameterNameDiscoverer();
        String[] paraNameArr = u.getParameterNames(method);

        //SPEL上下文
        StandardEvaluationContext context = new StandardEvaluationContext();
        ExpressionParser parser = new SpelExpressionParser();
        //把方法参数放入SPEL上下文中
        if (null != paraNameArr) {
            for (int i = 0; i < paraNameArr.length; i++) {
                context.setVariable(paraNameArr[i], args[i]);
            }
        }
        if (null != paraNameArr && paraNameArr.length > 0) {
            try {
                key = parser.parseExpression(key).getValue(context, String.class);
            } catch (Exception ignore) {
                //不适配参数的spel表达式用原key
                return key;
            }
        }
        return key;
    }


}
