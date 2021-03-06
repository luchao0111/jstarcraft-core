package com.jstarcraft.core.orm.lucene.converter.index;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.LinkedList;

import org.apache.lucene.document.DoublePoint;
import org.apache.lucene.document.FloatPoint;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.index.IndexableField;

import com.jstarcraft.core.common.reflection.TypeUtility;
import com.jstarcraft.core.orm.exception.OrmException;
import com.jstarcraft.core.orm.lucene.annotation.LuceneIndex;
import com.jstarcraft.core.orm.lucene.converter.IndexConverter;
import com.jstarcraft.core.orm.lucene.converter.LuceneContext;
import com.jstarcraft.core.utility.ClassUtility;

/**
 * 数值索引转换器
 * 
 * @author Birdy
 *
 */
public class NumberIndexConverter implements IndexConverter {

    @Override
    public Iterable<IndexableField> convert(LuceneContext context, String path, Field field, LuceneIndex annotation, Type type, Object data) {
        Collection<IndexableField> indexables = new LinkedList<>();
        Class<?> clazz = TypeUtility.getRawType(type, null);
        clazz = ClassUtility.primitiveToWrapper(clazz);
        if (Byte.class.isAssignableFrom(clazz)) {
            indexables.add(new IntPoint(path, (byte) data));
            return indexables;
        }
        if (Short.class.isAssignableFrom(clazz)) {
            indexables.add(new IntPoint(path, (short) data));
            return indexables;
        }
        if (Integer.class.isAssignableFrom(clazz)) {
            indexables.add(new IntPoint(path, (int) data));
            return indexables;
        }
        if (Long.class.isAssignableFrom(clazz)) {
            indexables.add(new LongPoint(path, (long) data));
            return indexables;
        }
        if (Float.class.isAssignableFrom(clazz)) {
            indexables.add(new FloatPoint(path, (float) data));
            return indexables;
        }
        if (Double.class.isAssignableFrom(clazz)) {
            indexables.add(new DoublePoint(path, (double) data));
            return indexables;
        }
        throw new OrmException();
    }

}
