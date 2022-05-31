package one.yiran.db.common.util;

import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Path;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;

@Slf4j
public class QClassUtil {

    private static HashMap<String,EntityPath> clazz = new HashMap<>();

    private static EntityPath cacheEntityPath(String qFullName, String lowerName){
        if(StringUtils.length(qFullName) < 2 || StringUtils.isBlank(lowerName))
            return null;
        int lastDot = qFullName.lastIndexOf(".");
        if(lastDot < 0)
            return null;
        String isQ = qFullName.substring( lastDot + 1,lastDot +2);
        if(!StringUtils.equals(isQ,"Q"))
            return null;

        EntityPath entityPath = clazz.get(qFullName);
        if(entityPath == null) {
            try {
                Constructor cs = Class.forName(qFullName).getConstructor(String.class);
                Object o = cs.newInstance(lowerName);
                clazz.put(qFullName,(EntityPath)o);
                entityPath = (EntityPath)o;
            } catch (Exception e) {
                log.error("{}对象不存在",qFullName);
                return null;
            }
        }
        return entityPath;
    }

    public static Path getFieldPathByEntityName(String fullName,String fieldName) {
        String entitySimpleName = fullName.substring(fullName.lastIndexOf(".") + 1);
        String qFullName = fullName.substring(0,fullName.lastIndexOf(".")) + ".Q" + entitySimpleName;
        return getFieldPath(qFullName,fieldName);
    }
    public static Path getFieldPath(Class qClazz,String fieldName){
        return getFieldPath(qClazz.getName(),fieldName);
    }
    public static Path getFieldPath(String qFullName,String fieldName){
        EntityPath entityPath = getPath(qFullName);
        if(entityPath == null)
            return null;
        Field f = FieldUtils.getField(entityPath.getClass(),fieldName,true);
        if(f == null)
            return null;
        try {
            return (Path)FieldUtils.readField(f,entityPath);
        } catch (IllegalAccessException e) {
        }
        return null;
    }

    public static EntityPath getPathByEntityName(String fullName){
        String entitySimpleName = fullName.substring(fullName.lastIndexOf(".") + 1);
        String qFullName = fullName.substring(0,fullName.lastIndexOf(".")) + ".Q" + entitySimpleName;
        return getPath(qFullName);
    }

    public static EntityPath getPath(String qFullName){
        String qSimpleName = qFullName.substring(qFullName.lastIndexOf(".") + 1);
//        String lowerName = qSimpleName.substring(0,1).toLowerCase() + qSimpleName.substring(1);
        String lowerName = qSimpleName.substring(1,2).toLowerCase() + qSimpleName.substring(2);
        return cacheEntityPath(qFullName,lowerName);
    }

    public static EntityPath getPath(Class qClass){
        return getPath(qClass.getName());
    }
}