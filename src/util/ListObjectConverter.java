
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public class ListObjectConverter<S, D> implements ObjectsConverter<S, D> {
    private ObjectConverter<S, D> objectConverter;
    private boolean ignoreNullObjectToBeConverted;
    private boolean ignoreNullObjectAfterConverted;

    public ListObjectConverter(ObjectConverter<S, D> objectConverter) {
        this(objectConverter, true, true);
    }

    public ListObjectConverter(ObjectConverter<S, D> objectConverter, boolean ignoreNullObjectToBeConverted) {
        this(objectConverter, ignoreNullObjectToBeConverted, true);
    }

    public ListObjectConverter(ObjectConverter<S, D> objectConverter, boolean ignoreNullObjectToBeConverted, boolean ignoreNullObjectAfterConverted) {
        this.ignoreNullObjectToBeConverted = true;
        this.ignoreNullObjectAfterConverted = true;
        this.objectConverter = objectConverter;
        this.ignoreNullObjectToBeConverted = ignoreNullObjectToBeConverted;
        this.ignoreNullObjectAfterConverted = ignoreNullObjectAfterConverted;
    }

    public List<D> convert(List<S> sList) {
        if (CollectionUtils.isEmpty(sList)) {
            return new ArrayList();
        } else {
            List<D> result = Lists.newArrayList();
            Iterator i$ = sList.iterator();

            while(true) {
                Object d;
                do {
                    Object s;
                    do {
                        if (!i$.hasNext()) {
                            return result;
                        }

                        s = i$.next();
                    } while(this.ignoreNullObjectToBeConverted && s == null);

                    d = this.objectConverter.convert(s);
                } while(this.ignoreNullObjectAfterConverted && d == null);

                result.add(d);
            }
        }
    }
}
