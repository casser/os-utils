package os.utils;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import os.utils.Types.Property.Index;



public class Types {
	public static interface Simple{
		public Object toSimple();
	}
	public static final Map<Class<?>, Class<?>> IMPLEMENTATIONS 	= new LinkedHashMap<Class<?>, Class<?>>();
	static{
		IMPLEMENTATIONS.put(List.class, ArrayList.class);
		IMPLEMENTATIONS.put(Map.class, HashMap.class);
	}
	
	public static final Class<?>[] MAP_CLASSES 		= {Map.class};
	public static final Class<?>[] INTERNAL_CLASSES = {Types.class,Class.class};
	public static final Class<?>[] SIMPLE_CLASSES 	= {Simple.class,String.class,Integer.class,Long.class,Double.class,Float.class,Character.class};
	public static final Class<?>[] ARRAY_CLASSES 	= {List.class,Set.class};
	
	private static Boolean isInternal(Class<?> clazz){
		for(Class<?> c : INTERNAL_CLASSES){
			if(c.isAssignableFrom(clazz)){
				return true;
			}
		}
		return false;
	}
	
	private static Boolean isSimple(Class<?> clazz){
		if(clazz.isPrimitive()){
			return true;
		}else{
			for(Class<?> c : SIMPLE_CLASSES){
				if(c.isAssignableFrom(clazz)){
					return true;
				}
			}
			return false;
		}
	}
	
	private static Boolean isArray(Class<?> clazz){
		if(clazz.isArray()){
			return true;
		}else{
			for(Class<?> c : ARRAY_CLASSES){
				if(c.isAssignableFrom(clazz)){
					return true;
				}
			}
			return false;
		}
	}
	
	private static Boolean isMap(Class<?> clazz){
		if(clazz.isPrimitive()){
			return true;
		}else{
			for(Class<?> c : MAP_CLASSES){
				if(c.isAssignableFrom(clazz)){
					return true;
				}
			}
			return false;
		}
	}
	
	
	public static class Type {
		
		@Retention(RetentionPolicy.RUNTIME)
		public @interface Info {
			String name() default "";
			String description() default "";
			String editor() default "";
		}
		
		public static enum Mode {
			INTERNAL,SIMPLE,ARRAY,MAP,BEAN,ENUM;
		}
		
		private static Class<?> determineArrayItemType(Class<?> clazz){
			if(clazz.isArray()){
				return clazz.getComponentType();
			}
			ParameterizedType parameterizedType = getParameterizedType(clazz);
			if(parameterizedType!=null && parameterizedType.getActualTypeArguments().length==1){
				try{
					return (Class<?>) parameterizedType.getActualTypeArguments()[0];
				}catch(ClassCastException ex){}
			}
			return Object.class;
		}
		
		private static Class<?> determineMapItemType(Class<?> clazz){
			ParameterizedType parameterizedType = getParameterizedType(clazz);
			if(parameterizedType!=null && parameterizedType.getActualTypeArguments().length==2){
				try{
					return (Class<?>) parameterizedType.getActualTypeArguments()[1];
				}catch(ClassCastException ex){}
			}
			return Object.class;
		}
		
		private static Class<?> determineMapKeyType(Class<?> clazz){
			
			ParameterizedType parameterizedType = getParameterizedType(clazz);
			if(parameterizedType!=null && parameterizedType.getActualTypeArguments().length==2){
				try{
					return (Class<?>) parameterizedType.getActualTypeArguments()[0];
				}catch(ClassCastException ex){}
			}
			return String.class;
		}
		
		private static ParameterizedType getParameterizedType(Class<?> clazz){
			java.lang.reflect.Type superClass = clazz;
			try{
				do{
					superClass = ((Class<?>)superClass).getGenericSuperclass();
				}while(!(superClass instanceof ParameterizedType));
				return (ParameterizedType) superClass;
			}catch(Exception ex){
				ex.printStackTrace();
			}
			return null;
		}
		
		private Mode 		mode;
		private Class<?> 	type;
		
		private Class<?> 	itemType;
		private Class<?> 	keyType;
		
		private Map<String,Property> 	properties;
		private Map<String,Annotation> annotations;
		
		@Property.Index(0)
		public Mode getMode() {
			return mode;
		}
		
		@Property.Index(1)
		public Class<?> getType() {
			return type;
		}
		
		@Property.Index(2)
		public Class<?> getKeyType() {
			return keyType;
		}
		
		@Property.Index(3)
		public Class<?> getValueType() {
			return itemType;
		}
		
		@Property.Index(4)
		public Map<String,Property> getProperties() {
			return properties;
		}
		
		public void setProperties(Map<String, Property> value) {
			properties = value;
		}
				
		@Property.Index(5)
		public Map<String,Annotation> getAnnotations() {
			return annotations;
		}
		
		public void setAnnotations(Map<String,Annotation> annotations) {
			this.annotations = annotations;
		}
		
		public Type(Class<?> javaType){
			
			this.type 	= javaType;
			
			if(Types.isInternal(javaType)){
				mode 			= Mode.INTERNAL;
			}else if(Types.isSimple(javaType)){
				mode 			= Mode.SIMPLE;
			}else if(Types.isMap(javaType)){
				mode 			= Mode.MAP;
				keyType			= determineMapKeyType(javaType);
				itemType		= determineMapItemType(javaType);
			}else if(Types.isArray(javaType)){
				mode 			= Mode.ARRAY;
				keyType			= Integer.class;
				itemType		= determineArrayItemType(javaType);
			}else if(javaType.isEnum()){
				mode 			= Mode.ENUM;
			}else{
				mode 			= Mode.BEAN;
				this.properties = new LinkedHashMap<String, Types.Property>();
			}
		}
		
		
		public <T> T newInstance() {
			return newInstance(null,null);
		}
		
		public <T> T newInstance(Object param) {
			return newInstance(param,param.getClass());
		}
		
		@SuppressWarnings("unchecked")
		public <T> T newInstance(Object param, Class<?> cls) {
			try{
				if(param!=null){
					try{
						Constructor<?> constructor = getType().getConstructor(new Class<?>[]{cls});
						if(constructor!=null){
							return (T) constructor.newInstance(param);
						}
					}catch(Exception e){}
				}
				return (T) getType().newInstance();
			}catch(Exception e){
				e.printStackTrace();
			}
			return null;
		}
		
		@Override
		public boolean equals(Object value) {
			if(value instanceof Type){
				Type t = (Type) value;
				boolean r = true;
				r = r && getType().equals(t.getType());
				if(getKeyType() !=null){
					r = r && getKeyType().equals(t.getKeyType());
				}
				if(getValueType()!=null){
					r = r && getValueType().equals(t.getValueType());
				}
				return r;
			}else if(value instanceof Class){
				return equals(this.getType().equals(value));
			}
			return false;
		}
		
		@Override
		public String toString() {
			String str = this.mode.name()+" : "+getType().getName();
			if(keyType!=null){
				if(itemType!=null){
					str += "< "+keyType.getName()+", "+itemType.getName()+" >";
				}else{
					str += "< "+keyType.getName()+" >";
				}
			}else if(itemType!=null){
				str += "< "+itemType.getName()+" >";
			}
			return "{ "+str+" }";
		}

		public boolean isSimple() {
			return mode.equals(Mode.SIMPLE);
		}
		public boolean isMap() {
			return mode.equals(Mode.MAP);
		}
		public boolean isArray() {
			return mode.equals(Mode.ARRAY);
		}
		public boolean isBean() {
			return mode.equals(Mode.BEAN);
		}
		public boolean isEnum() {
			return mode.equals(Mode.ENUM);
		}		
	}
	
	public static class Property{
		
		@Retention(RetentionPolicy.RUNTIME)
		public @interface Index {
			int value() default 0;
		}
		
		@Retention(RetentionPolicy.RUNTIME)
		public @interface Info {
			String name() default "";
			String description() default "";
			String editor() default "";
		}
		
				
		public static enum Access{
			R,W,RW;
		}
		
		private String name;
		private Class<?> type;
		private Class<?> parentType;
		private Access access;
		private Integer index;
		private Set<Class<? extends Annotation>> annotationTypes;
		private Map<String,Annotation> annotations;
		
		@Property.Index(0)
		public String getName() {
			return name;
		}
		
		public Class<?> parentType() {
			return parentType;
		}
		
		@Property.Index(1)
		public Class<?> getType() {
			return type;
		}
		public void setType(Class<?> type) {
			this.type = type;
		}
		
		@Property.Index(2)
		public Integer getIndex() {
			return index;
		}
		public void setIndex(Integer i) {
			index = i;
		}
		
		@Property.Index(3)
		public Map<String,Annotation> getAnnotations() {
			return annotations;
		}
		public void setAnnotations(Map<String,Annotation> annotations) {
			this.annotations = annotations;
			annotationTypes = new HashSet<Class<? extends Annotation>>();
			for(Annotation an:annotations.values()){
				annotationTypes.add(an.annotationType());
			}
		}
		
		public Boolean hasAccess(Access access) {
			return (this.access==Access.RW || this.access == access);
		}

		public void addAccess(Access access) {
			if(access==null){
				return;
			}
			if(this.access == null){
				this.access = access;	
			}else{
				if(this.access != access){
					this.access = Access.RW;
				}
			}
		}

		public Property(String name, Class<?> parentType){
			this.name 		= name;
			this.parentType = parentType;
		}

		public Object invokeSetter(Object data, Object value) {
			if(value==null){
				return null;
			}
			try {
				parentType().getMethod("set"+StringUtils.capitalize(name), new Class<?>[]{value.getClass()}).invoke(data, value);
				return invokeGetter(data);
			} catch(NoSuchMethodException mex){
				try{
					parentType().getDeclaredMethod("set"+StringUtils.capitalize(name), new Class<?>[]{Object.class}).invoke(data, value);
					return invokeGetter(data);
				}catch (Exception e) {
					e.printStackTrace();
				}
			} catch (Exception e) {
			}
			return null;
		}
		public Object invokeGetter(Object data) {
			try {
				return parentType().getMethod("get"+StringUtils.capitalize(name)).invoke(data);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
		
		@Override
		public String toString() {
			return "Property["+index+"] "+parentType()+"."+getName()+":"+getType();
		}

		public boolean hasAnnotation(Class<? extends Annotation> annotation) {
			return annotations!= null && annotationTypes.contains(annotation);
		}

		
		
		
	}
	
	public static final Map<String,Type> classMap = new ConcurrentHashMap<String,Type>();
	
	public static synchronized void register(Class<?> clazz){
		Type type=null;
		if(clazz.isInterface()){
			if(IMPLEMENTATIONS.containsKey(clazz)){
				type = new Type(IMPLEMENTATIONS.get(clazz));
			}else{
				return;
			}
		}else{
			type = new Type(clazz);
		}
		if(type.getMode().equals(Type.Mode.BEAN)){
			Method[] methods 	= clazz.getMethods();
			int i=0;
			LinkedHashMap<String, Types.Property> properties = new LinkedHashMap<String, Types.Property>();
			for(Method method : methods){
				int modifiers = method.getModifiers();
				if(Modifier.isPublic(modifiers) && !Modifier.isStatic(modifiers)){
					String mName  	=  method.getName();
					if(mName.length()>3 && mName.indexOf("get")==0 && !mName.equals("getClass")){
						String pName = StringUtils.uncapitalize(mName.substring(3));
						if(!properties.containsKey(pName)){
							int index = i++;
							if(method.isAnnotationPresent(Property.Index.class)){
								index = method.getAnnotation(Property.Index.class).value();
							}
							Types.Property property = new Property(pName,type.getType());
							property.setIndex(index);
							property.setType(method.getReturnType());
							property.addAccess(Types.Property.Access.R);
							
							Annotation[] annotations = method.getAnnotations();
							if(annotations!=null && annotations.length>0){
								Map<String,Annotation> annotationsMap = new LinkedHashMap<String, Annotation>();
								for(Annotation annotation: annotations){
									if(!annotation.annotationType().equals(Index.class)){
										annotationsMap.put(annotation.annotationType().getName(), annotation);
									}
								}
								if(annotationsMap.size()>0){
									property.setAnnotations(annotationsMap);
								}
							}
							
							properties.put(pName,property);
							
						}
					}
				}
			}
			for(Method method : methods){
				String mName  	=  method.getName();
				if(mName.length()>3 && mName.indexOf("set")==0){
					String pName = StringUtils.uncapitalize(mName.substring(3));
					if(properties.containsKey(pName)){
						Property property = properties.get(pName);
						property.addAccess(Types.Property.Access.W);
					}
				}
			}
			type.setProperties(sortByComparator(properties));
		}
		
		Annotation[] annotations = clazz.getAnnotations();
		if(annotations!=null && annotations.length>0){
			Map<String,Annotation> annotationsMap = new LinkedHashMap<String, Annotation>();
			for(Annotation annotation: annotations){
				annotationsMap.put(annotation.annotationType().getName(), annotation);
			}
			if(annotationsMap.size()>0){
				type.setAnnotations(annotationsMap);
			}
		}
		
		classMap.put(clazz.getName(),type);
		
		if(type.isBean() && type.getProperties()!=null && type.getProperties().size()>0){
			for(Property property:type.getProperties().values()){
				if(!classMap.containsKey(property.getType().getName())){
					register(property.getType());
				}
			}
		}
		if(type.getKeyType()!=null && !classMap.containsKey(type.getKeyType().getName())){
			register(type.getKeyType());
		}
		if(type.getValueType()!=null && !classMap.containsKey(type.getValueType().getName())){
			register(type.getValueType());
		}
		
	}
	
	public static synchronized Type getType(Class<?> clazz){
		if(!classMap.containsKey(clazz.getName())){
			register(clazz);
		}
		return classMap.get(clazz.getName()); 
	}
	
	public static synchronized List<Type> getTypes(){
		return sortByComparator(new ArrayList<Types.Type>(classMap.values()));
	}
	public static Map<String,Type> getTypesMap() {
		List<Type> types = getTypes();
		Map<String,Type> map = new LinkedHashMap<String,Type>();
		for(Type type : types){
			map.put(type.getType().getName(), type);
		}
		return map;
	}
	public static synchronized List<Type> getAnnotatedTypes(Class<? extends Annotation> clazz){
		List<Class<?>> classes = new ArrayList<Class<?>>();
		classes.add(clazz);
		return getAnnotatedTypes(classes);
	}
	
	public static synchronized List<Type> getAnnotatedTypes(List<Class<?>> classes){
		return getAnnotatedTypes(classes,false);
	}
	
	@SuppressWarnings("unchecked")
	public static synchronized List<Type> getAnnotatedTypes(List<Class<?>> classes, Boolean all){
		List<Type>		list = getTypes();
		Iterator<Type> 	types = list.iterator();
		while(types.hasNext()){
			Type type = types.next();
			Boolean available = all;
			
			for(Class<?> clazz:classes){
				if(clazz.isAnnotation()){
					available = all?
						(available&&type.getType().isAnnotationPresent((Class<? extends Annotation>) clazz)):
						(available||type.getType().isAnnotationPresent((Class<? extends Annotation>) clazz));
				}
			}
			if(!available){
				types.remove();
			}
		}
		return list;
	}
	
	public static synchronized List<Type> getAssignableTypes(Class<? extends Annotation> clazz){
		List<Class<?>> classes = new ArrayList<Class<?>>();
		classes.add(clazz);
		return getAssignableTypes(classes);
	}
	
	public static synchronized List<Type> getAssignableTypes(List<Class<?>> classes){
		return getAssignableTypes(classes,false);
	}
	
	public static synchronized List<Type> getAssignableTypes(List<Class<?>> classes, Boolean all){
		List<Type>		list = getTypes();
		Iterator<Type> 	types = list.iterator();
		while(types.hasNext()){
			Type type = types.next();
			Boolean available = all;
			
			for(Class<?> clazz:classes){
				if(clazz.isAnnotation()){
					available = all?
							(available&&clazz.isAssignableFrom(type.getType())):
							(available||clazz.isAssignableFrom(type.getType()));
				}
			}
			if(!available){
				types.remove();
			}
		}
		return list;
	}
		
	public static synchronized List<Type> getNestedTypes(Class<?> clazz){
		return getPackagedTypes(clazz.getName(),true);
	}
	
	public static synchronized List<Type> getNestedTypes(List<Class<?>> classes){
		return getNestedTypes(classes,false);
	}
	
	public static synchronized List<Type> getNestedTypes(List<Class<?>> classes,Boolean all){
		List<String> clss = new ArrayList<String>();
		for(Class<?> cls:classes){
			clss.add(cls.getName());
		}
		return getPackagedTypes(clss,all,true);
	}
	
	public static synchronized List<Type> getPackagedTypes(String pkg){
		return getPackagedTypes(Arrays.asList(pkg), true, false);
	}
	
	public static synchronized List<Type> getPackagedTypes(String pkg, Boolean nested){
		return getPackagedTypes(Arrays.asList(pkg), true, nested);
	}
	
	public static synchronized List<Type> getPackagedTypes(List<String> packages, Boolean all){
		return getPackagedTypes(packages, all, false);
	}
	
	public static synchronized List<Type> getPackagedTypes(List<String> packages, Boolean all, Boolean nested){
		List<Type>		list = getTypes();
		Iterator<Type> 	types = list.iterator();
		while(types.hasNext()){
			Type type   = types.next();
			Boolean available = all;
			for(String pkg:packages){
				String name = type.getType().getName();
				if(!nested && name.contains("$")){
					name = name.substring(name.indexOf('$'));
				}
				available = all?
						(available&&!name.startsWith(pkg)):
						(available||!name.startsWith(pkg));
			}
			if(!available){
				types.remove();
			}
		}
		return list;
	}
	
	
	
	
	private static Map<String,Property> sortByComparator(Map<String,Property> unsortMap) {
		 
        List<Map.Entry<String,Property>> list = new LinkedList<Map.Entry<String,Property>>(unsortMap.entrySet());
 
        //sort list based on comparator
        Collections.sort(list, new Comparator<Map.Entry<String,Property>>()  {
             public int compare(Map.Entry<String,Property> o1, Map.Entry<String,Property> o2) {
	           return o1.getValue().getIndex().compareTo(o2.getValue().getIndex());
             }
		});
	 
	        //put sorted list into map again
		Map<String,Property> sortedMap = new LinkedHashMap<String,Property>();
		for (Iterator<Map.Entry<String,Property>> it = list.iterator(); it.hasNext();) {
			Map.Entry<String,Property> entry = it.next();
		    sortedMap.put(entry.getKey(), entry.getValue());
		}
		
		return sortedMap;
	}
	
	private static List<Type> sortByComparator(List<Type> unsortList) {
        Collections.sort(unsortList, new Comparator<Type>()  {
             public int compare(Type o1, Type o2) {
	           return o1.getType().getName().compareTo(o2.getType().getName());
             }
		});
        return unsortList;
	}
	
	
	
}
