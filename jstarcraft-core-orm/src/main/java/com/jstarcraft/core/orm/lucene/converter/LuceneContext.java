package com.jstarcraft.core.orm.lucene.converter;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.jstarcraft.core.codec.specification.ClassDefinition;
import com.jstarcraft.core.codec.specification.CodecDefinition;
import com.jstarcraft.core.common.reflection.ReflectionUtility;
import com.jstarcraft.core.common.reflection.Specification;
import com.jstarcraft.core.orm.exception.OrmException;
import com.jstarcraft.core.orm.lucene.annotation.LuceneIndex;
import com.jstarcraft.core.orm.lucene.annotation.LuceneSort;
import com.jstarcraft.core.orm.lucene.annotation.LuceneStore;
import com.jstarcraft.core.orm.lucene.converter.index.ArrayIndexConverter;
import com.jstarcraft.core.orm.lucene.converter.index.BooleanIndexConverter;
import com.jstarcraft.core.orm.lucene.converter.index.CollectionIndexConverter;
import com.jstarcraft.core.orm.lucene.converter.index.EnumerationIndexConverter;
import com.jstarcraft.core.orm.lucene.converter.index.InstantIndexConverter;
import com.jstarcraft.core.orm.lucene.converter.index.MapIndexConverter;
import com.jstarcraft.core.orm.lucene.converter.index.NumberIndexConverter;
import com.jstarcraft.core.orm.lucene.converter.index.ObjectIndexConverter;
import com.jstarcraft.core.orm.lucene.converter.index.StringIndexConverter;
import com.jstarcraft.core.orm.lucene.converter.sort.ArraySortConverter;
import com.jstarcraft.core.orm.lucene.converter.sort.BooleanSortConverter;
import com.jstarcraft.core.orm.lucene.converter.sort.CollectionSortConverter;
import com.jstarcraft.core.orm.lucene.converter.sort.EnumerationSortConverter;
import com.jstarcraft.core.orm.lucene.converter.sort.InstantSortConverter;
import com.jstarcraft.core.orm.lucene.converter.sort.MapSortConverter;
import com.jstarcraft.core.orm.lucene.converter.sort.NumberSortConverter;
import com.jstarcraft.core.orm.lucene.converter.sort.ObjectSortConverter;
import com.jstarcraft.core.orm.lucene.converter.sort.StringSortConverter;
import com.jstarcraft.core.orm.lucene.converter.store.ArrayStoreConverter;
import com.jstarcraft.core.orm.lucene.converter.store.BooleanStoreConverter;
import com.jstarcraft.core.orm.lucene.converter.store.CollectionStoreConverter;
import com.jstarcraft.core.orm.lucene.converter.store.EnumerationStoreConverter;
import com.jstarcraft.core.orm.lucene.converter.store.InstantStoreConverter;
import com.jstarcraft.core.orm.lucene.converter.store.MapStoreConverter;
import com.jstarcraft.core.orm.lucene.converter.store.NumberStoreConverter;
import com.jstarcraft.core.orm.lucene.converter.store.ObjectStoreConverter;
import com.jstarcraft.core.orm.lucene.converter.store.StringStoreConverter;
import com.jstarcraft.core.utility.KeyValue;

/**
 * 搜索上下文
 * 
 * @author Birdy
 *
 */
public class LuceneContext {

    public static final EnumMap<Specification, IndexConverter> INDEX_CONVERTERS = new EnumMap<>(Specification.class);

    public static final EnumMap<Specification, SortConverter> SORT_CONVERTERS = new EnumMap<>(Specification.class);

    public static final EnumMap<Specification, StoreConverter> STORE_CONVERTERS = new EnumMap<>(Specification.class);

    static {
        INDEX_CONVERTERS.put(Specification.ARRAY, new ArrayIndexConverter());
        INDEX_CONVERTERS.put(Specification.BOOLEAN, new BooleanIndexConverter());
        INDEX_CONVERTERS.put(Specification.COLLECTION, new CollectionIndexConverter());
        INDEX_CONVERTERS.put(Specification.ENUMERATION, new EnumerationIndexConverter());
        INDEX_CONVERTERS.put(Specification.INSTANT, new InstantIndexConverter());
        INDEX_CONVERTERS.put(Specification.MAP, new MapIndexConverter());
        INDEX_CONVERTERS.put(Specification.NUMBER, new NumberIndexConverter());
        INDEX_CONVERTERS.put(Specification.OBJECT, new ObjectIndexConverter());
        INDEX_CONVERTERS.put(Specification.STRING, new StringIndexConverter());
    }

    static {
        SORT_CONVERTERS.put(Specification.ARRAY, new ArraySortConverter());
        SORT_CONVERTERS.put(Specification.BOOLEAN, new BooleanSortConverter());
        SORT_CONVERTERS.put(Specification.COLLECTION, new CollectionSortConverter());
        SORT_CONVERTERS.put(Specification.ENUMERATION, new EnumerationSortConverter());
        SORT_CONVERTERS.put(Specification.INSTANT, new InstantSortConverter());
        SORT_CONVERTERS.put(Specification.MAP, new MapSortConverter());
        SORT_CONVERTERS.put(Specification.NUMBER, new NumberSortConverter());
        SORT_CONVERTERS.put(Specification.OBJECT, new ObjectSortConverter());
        SORT_CONVERTERS.put(Specification.STRING, new StringSortConverter());
    }

    static {
        STORE_CONVERTERS.put(Specification.ARRAY, new ArrayStoreConverter());
        STORE_CONVERTERS.put(Specification.BOOLEAN, new BooleanStoreConverter());
        STORE_CONVERTERS.put(Specification.COLLECTION, new CollectionStoreConverter());
        STORE_CONVERTERS.put(Specification.ENUMERATION, new EnumerationStoreConverter());
        STORE_CONVERTERS.put(Specification.INSTANT, new InstantStoreConverter());
        STORE_CONVERTERS.put(Specification.MAP, new MapStoreConverter());
        STORE_CONVERTERS.put(Specification.NUMBER, new NumberStoreConverter());
        STORE_CONVERTERS.put(Specification.OBJECT, new ObjectStoreConverter());
        STORE_CONVERTERS.put(Specification.STRING, new StringStoreConverter());
    }

    private Map<Class<?>, ClassDefinition> classDefinitions;

    private Map<Class<?>, List<KeyValue<Field, IndexConverter>>> indexKeyValues;

    private Map<Class<?>, List<KeyValue<Field, SortConverter>>> sortKeyValues;

    private Map<Class<?>, List<KeyValue<Field, StoreConverter>>> storeKeyValues;

    private void parse(ClassDefinition definition) {
        this.classDefinitions.put(definition.getType(), definition);
        List<KeyValue<Field, IndexConverter>> indexKeyValues = new LinkedList<>();
        List<KeyValue<Field, SortConverter>> sortKeyValues = new LinkedList<>();
        List<KeyValue<Field, StoreConverter>> storeKeyValues = new LinkedList<>();

        ReflectionUtility.doWithFields(definition.getType(), (field) -> {
            ReflectionUtility.makeAccessible(field);
            Type type = field.getGenericType();
            Specification specification = Specification.getSpecification(type);

            try {
                LuceneIndex index = field.getAnnotation(LuceneIndex.class);
                if (index != null) {
                    Class<? extends IndexConverter> clazz = index.clazz();
                    if (IndexConverter.class == clazz) {
                        IndexConverter converter = INDEX_CONVERTERS.get(specification);
                        indexKeyValues.add(new KeyValue<>(field, converter));
                    } else {
                        IndexConverter converter = clazz.newInstance();
                        indexKeyValues.add(new KeyValue<>(field, converter));
                    }
                }

                LuceneSort sort = field.getAnnotation(LuceneSort.class);
                if (sort != null) {
                    Class<? extends SortConverter> clazz = sort.clazz();
                    if (SortConverter.class == clazz) {
                        SortConverter converter = SORT_CONVERTERS.get(specification);
                        sortKeyValues.add(new KeyValue<>(field, converter));
                    } else {
                        SortConverter converter = clazz.newInstance();
                        sortKeyValues.add(new KeyValue<>(field, converter));
                    }
                }

                LuceneStore store = field.getAnnotation(LuceneStore.class);
                if (store != null) {
                    Class<? extends StoreConverter> clazz = store.clazz();
                    if (StoreConverter.class == clazz) {
                        StoreConverter converter = STORE_CONVERTERS.get(specification);
                        storeKeyValues.add(new KeyValue<>(field, converter));
                    } else {
                        StoreConverter converter = clazz.newInstance();
                        storeKeyValues.add(new KeyValue<>(field, converter));
                    }
                }
            } catch (Exception exception) {
                throw new OrmException(exception);
            }
        });

        this.indexKeyValues.put(definition.getType(), new ArrayList<>(indexKeyValues));
        this.sortKeyValues.put(definition.getType(), new ArrayList<>(sortKeyValues));
        this.storeKeyValues.put(definition.getType(), new ArrayList<>(storeKeyValues));
    }

    public LuceneContext(CodecDefinition... definitions) {
        this.classDefinitions = new HashMap<>();
        this.indexKeyValues = new HashMap<>();
        this.sortKeyValues = new HashMap<>();
        this.storeKeyValues = new HashMap<>();
        for (CodecDefinition codecDefinition : definitions) {
            for (ClassDefinition classDefinition : codecDefinition.getClassDefinitions()) {
                // 预定义的规范类型不需要分析
                if (Specification.type2Specifitions.containsKey(classDefinition.getType())) {
                    continue;
                }
                parse(classDefinition);
            }
        }
    }

    /**
     * 根据类型获取实例
     * 
     * @param clazz
     * @return
     * @throws Exception
     */
    public <T> T getInstance(Class<T> clazz) throws Exception {
        return (T) classDefinitions.get(clazz).getInstance();
    }

    /**
     * 根据规范获取索引转换器
     * 
     * @param specification
     * @return
     */
    public IndexConverter getIndexConverter(Specification specification) {
        return INDEX_CONVERTERS.get(specification);
    }

    /**
     * 根据规范获取排序转换器
     * 
     * @param specification
     * @return
     */
    public SortConverter getSortConverter(Specification specification) {
        return SORT_CONVERTERS.get(specification);
    }

    /**
     * 根据规范获取存储转换器
     * 
     * @param specification
     * @return
     */
    public StoreConverter getStoreConverter(Specification specification) {
        return STORE_CONVERTERS.get(specification);
    }

    /**
     * 根据类型获取索引转换器
     * 
     * @param clazz
     * @return
     */
    public List<KeyValue<Field, IndexConverter>> getIndexKeyValues(Class<?> clazz) {
        return this.indexKeyValues.get(clazz);
    }

    /**
     * 根据类型获取排序转换器
     * 
     * @param clazz
     * @return
     */
    public List<KeyValue<Field, SortConverter>> getSortKeyValues(Class<?> clazz) {
        return this.sortKeyValues.get(clazz);
    }

    /**
     * 根据类型获取存储转换器
     * 
     * @param clazz
     * @return
     */
    public List<KeyValue<Field, StoreConverter>> getStoreKeyValues(Class<?> clazz) {
        return this.storeKeyValues.get(clazz);
    }

}
