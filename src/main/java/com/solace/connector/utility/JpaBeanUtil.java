package com.solace.connector.utility;

import cn.hutool.core.util.ClassUtil;
import com.solace.connector.exception.InvalidEntityException;
import com.solace.connector.exception.InvalidPropertyException;


import lombok.extern.slf4j.Slf4j;
import reactor.util.annotation.Nullable;


import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

@Slf4j
public class JpaBeanUtil {

    public static void invokeSetter(Object obj, String fieldName, Object value) throws InvalidPropertyException {
        try {
            PropertyDescriptor pd = null;

            if( ClassUtil.getDeclaredField(obj.getClass(),fieldName) ==null){
                if( ClassUtil.getDeclaredField(obj.getClass(),"id") !=null){
                    pd = new PropertyDescriptor("id", obj.getClass());
                    Object id = pd.getReadMethod().invoke(obj);
                    if(id == null){
                        log.error("ERROR in invokeSetter,id of entity {} is Null!",obj.getClass().getName());
                        throw new InvalidPropertyException("id of "+obj.getClass().getName());
                    }
                    pd = new PropertyDescriptor(fieldName, id.getClass());
                    obj = id;
                    if(value!=null){
                        if(pd.getPropertyType().equals(String.class)){
                            value= value.toString();
                        }else if(pd.getPropertyType().equals(Integer.class)){
                            value= Integer.parseInt(value.toString());
                        }
                    }
                }
            }else{
                pd = new PropertyDescriptor(fieldName, obj.getClass());
                if(value!=null){
                    if(pd.getPropertyType().equals(String.class)){
                        value= value.toString();
                    }else if(pd.getPropertyType().equals(Integer.class)){
                        value= Integer.parseInt(value.toString());
                    }
                }
            }
            // Call setter on specified property
            pd.getWriteMethod().invoke(obj, value);
        } catch (IntrospectionException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            log.error("ERROR in invokeSetter",e);
            throw new RuntimeException(e);
        }
    }

    @Nullable
    public static Object invokeGetter(Object obj, String fieldName) throws InvalidPropertyException {
        try {
            PropertyDescriptor pd = null;
            if( ClassUtil.getDeclaredField(obj.getClass(),fieldName) ==null){
                if( ClassUtil.getDeclaredField(obj.getClass(),"id") !=null){
                    pd = new PropertyDescriptor("id", obj.getClass());
                    Object id = pd.getReadMethod().invoke(obj);
                    if(id == null){
                        log.error("ERROR in invokeGetter,id of entity {} is Null!",obj.getClass().getName());
                        throw new InvalidPropertyException("id of "+obj.getClass().getName());
                    }
                    pd = new PropertyDescriptor(fieldName, id.getClass());
                    return pd.getReadMethod().invoke(id);
                }
            }else {
                pd = new PropertyDescriptor(fieldName, obj.getClass());
                // Call getter on specified property
                return  pd.getReadMethod().invoke(obj);
            }

        } catch (IntrospectionException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            log.error("ERROR in invokeGetter",e);
            throw new RuntimeException(e);
        }
        return null;
    }

    public static String getPropertyColumnName (Class clazz , String property){
        Column columnIndicator = null;
        try {
            columnIndicator = clazz.getDeclaredField(property).getAnnotation(Column.class);
            if(columnIndicator==null){
                PropertyDescriptor pd = new PropertyDescriptor(property, clazz);
                columnIndicator = pd.getReadMethod().getAnnotation(Column.class);
            }
        } catch (NoSuchFieldException e) {
            try {
                Class idClazz = clazz.getDeclaredField("id").getType();
                return getPropertyColumnName(idClazz,property);
            }catch (NoSuchFieldException ex) {
                log.error("ERROR in getPropertyColumnName",e);
                throw new RuntimeException(e);
            }
        } catch (IntrospectionException e) {
            log.error("ERROR in getPropertyColumnName",e);
            throw new RuntimeException(e);
        }
        if(columnIndicator==null){
            log.error("ERROR in getPropertyColumnName, property {} NOT ColumnName.",property);
            throw new RuntimeException();
        }
        return columnIndicator.name();
    }


    public static Object buildEntityObj(String jpaEntity) throws InvalidEntityException {
        Object obj=null;
        try{
            Class dbPOJOClass = Class.forName(jpaEntity);
            Constructor dbPOJConstructor = dbPOJOClass.getConstructor();
            obj = dbPOJConstructor.newInstance();
            Field field = ClassUtil.getDeclaredField(dbPOJOClass, "id");
            if (field != null) {
                PropertyDescriptor pd = new PropertyDescriptor("id", obj.getClass());
                if (field.getAnnotation(EmbeddedId.class) != null || pd.getReadMethod().getAnnotation(EmbeddedId.class)!=null) {
                    Class idClass = Class.forName(obj.getClass().getName() + "Id");
                    Constructor idConstructor = idClass.getConstructor();
                    Object id = idConstructor.newInstance();
                    pd.getWriteMethod().invoke(obj, id);
                }
            }
        }catch (ClassNotFoundException | NoSuchMethodException |InstantiationException | IllegalAccessException | InvocationTargetException | IntrospectionException e){
            log.error("ERROR in buildEntityObj, entity {} NOT initialized.",jpaEntity);
            throw new InvalidEntityException(e.getMessage());
        }

        return obj;



    }
}
