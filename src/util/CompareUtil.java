//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.sankuai.mpmctleads.common.util;

import com.dp.arts.client.response.Record;
import com.google.common.collect.Lists;
import com.sankuai.mpmctleads.common.util.exception.BizException;
import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CompareUtil {
    private static final Logger log = LoggerFactory.getLogger(CompareUtil.class);
    private static final List<String> CAN_COMPARE_TYPE_LIST;

    public CompareUtil() {
    }

    public static int chainCompare(int... compares) {
        int[] var1 = compares;
        int var2 = compares.length;

        for(int var3 = 0; var3 < var2; ++var3) {
            int compareI = var1[var3];
            if (0 != compareI) {
                return compareI;
            }
        }

        return 0;
    }

    public static <K, V> int compareTwoMap(Map<K, V> map1, Map<K, V> map2, Comparator<V> valueComparator) {
        if (MapUtils.isEmpty(map1) && MapUtils.isEmpty(map2)) {
            return 0;
        } else if (MapUtils.isNotEmpty(map1) && MapUtils.isEmpty(map2)) {
            return 1;
        } else if (MapUtils.isEmpty(map1)) {
            return -1;
        } else if (map1.size() != map2.size()) {
            return map1.size() - map2.size();
        } else {
            Iterator var3 = map1.entrySet().iterator();

            int valueCompareResult;
            do {
                if (!var3.hasNext()) {
                    return 0;
                }

                Map.Entry<K, V> entry = (Map.Entry)var3.next();
                V value1 = entry.getValue();
                V value2 = map2.get(entry.getKey());
                valueCompareResult = valueComparator.compare(value1, value2);
            } while(valueCompareResult == 0);

            return valueCompareResult;
        }
    }

    public static <K, V> Comparator<Map<K, V>> generateMapComparator(Comparator<V> valueComparator) {
        return (o1, o2) -> {
            return compareTwoMap(o1, o2, valueComparator);
        };
    }

    public static <T> int compareTwoList(List<T> list1, List<T> list2, Comparator<T> elementComparator) {
        if (CollectionUtils.isEmpty(list1) && CollectionUtils.isEmpty(list2)) {
            return 0;
        } else if (CollectionUtils.isNotEmpty(list1) && CollectionUtils.isEmpty(list2)) {
            return 1;
        } else if (CollectionUtils.isEmpty(list1)) {
            return -1;
        } else if (list1.size() != list2.size()) {
            return list1.size() - list2.size();
        } else {
            list1 = (List)list1.stream().sorted(elementComparator).collect(Collectors.toList());
            list2 = (List)list2.stream().sorted(elementComparator).collect(Collectors.toList());

            for(int i = 0; i < list1.size(); ++i) {
                int elementCompareResult = elementComparator.compare(list1.get(i), list2.get(i));
                if (elementCompareResult != 0) {
                    return elementCompareResult;
                }
            }

            return 0;
        }
    }

    public static int compareTwoList(List<Record> list1, List<Record> list2, Comparator<Record> elementComparator, Comparator<Record> elementSortComparator) {
        if (CollectionUtils.isEmpty(list1) && CollectionUtils.isEmpty(list2)) {
            return 0;
        } else if (CollectionUtils.isNotEmpty(list1) && CollectionUtils.isEmpty(list2)) {
            return 1;
        } else if (CollectionUtils.isEmpty(list1)) {
            return -1;
        } else if (list1.size() != list2.size()) {
            return list1.size() - list2.size();
        } else {
            if (elementSortComparator != null) {
                list1 = (List)list1.stream().sorted(elementSortComparator).collect(Collectors.toList());
                list2 = (List)list2.stream().sorted(elementSortComparator).collect(Collectors.toList());
            } else {
                list1 = (List)list1.stream().sorted(elementComparator).collect(Collectors.toList());
                list2 = (List)list2.stream().sorted(elementComparator).collect(Collectors.toList());
            }

            for(int i = 0; i < list1.size(); ++i) {
                int elementCompareResult = elementComparator.compare(list1.get(i), list2.get(i));
                if (elementCompareResult != 0) {
                    return elementCompareResult;
                }
            }

            return 0;
        }
    }

    public static <T> Comparator<List<T>> generateListComparator(Comparator<T> elementComparator) {
        return (o1, o2) -> {
            return compareTwoList(o1, o2, elementComparator);
        };
    }

    public static <T extends Comparable<T>> int compareTwoComparable(T o1, T o2) {
        if (o1 == null && null == o2) {
            return 0;
        } else if (o1 != null && null == o2) {
            return 1;
        } else {
            return o1 == null ? -1 : o1.compareTo(o2);
        }
    }

    public static <T> int shallowCompareTwoObject(T o1, T o2, List<String> compareFields) {
        if (Objects.isNull(o1) && Objects.isNull(o2)) {
            return 0;
        } else if (Objects.nonNull(o1) && Objects.isNull(o2)) {
            return 1;
        } else if (Objects.isNull(o1)) {
            return -1;
        } else {
            try {
                Class<?> clazz = o1.getClass();
                Iterator var4 = compareFields.iterator();

                while(var4.hasNext()) {
                    String compareField = (String)var4.next();
                    Field field = clazz.getDeclaredField(compareField);
                    field.setAccessible(true);
                    if (CAN_COMPARE_TYPE_LIST.contains(field.getType().getName())) {
                        int result = compareTwoFiled(field, o1, o2);
                        if (result != 0) {
                            return result;
                        }
                    }
                }

                return 0;
            } catch (Exception var8) {
                log.error("[比较工具]出现异常, o1:{},o2:{},compareFields:{}", new Object[]{GsonUtil.toJson(o1), GsonUtil.toJson(o2), GsonUtil.toJson(compareFields), var8});
                throw new BizException("比较工具出现异常");
            }
        }
    }

    public static <T> Comparator<T> generateTwoObjectShallowComparator(List<String> compareFields) {
        return (o1, o2) -> {
            return shallowCompareTwoObject(o1, o2, compareFields);
        };
    }

    private static int compareTwoFiled(Field field, Object o1, Object o2) {
        try {
            String fieldType = field.getType().getTypeName();
            Object value1 = field.get(o1);
            Object value2 = field.get(o2);
            if (!Integer.TYPE.getTypeName().equalsIgnoreCase(fieldType) && !Integer.class.getTypeName().equalsIgnoreCase(fieldType)) {
                if (!Double.TYPE.getTypeName().equalsIgnoreCase(fieldType) && !Double.class.getTypeName().equalsIgnoreCase(fieldType)) {
                    if (!Float.TYPE.getTypeName().equalsIgnoreCase(fieldType) && !Float.class.getTypeName().equalsIgnoreCase(fieldType)) {
                        if (!Long.TYPE.getTypeName().equalsIgnoreCase(fieldType) && !Long.class.getTypeName().equalsIgnoreCase(fieldType)) {
                            if (!Short.TYPE.getTypeName().equalsIgnoreCase(fieldType) && !Short.class.getTypeName().equalsIgnoreCase(fieldType)) {
                                if (!Byte.TYPE.getTypeName().equalsIgnoreCase(fieldType) && !Byte.class.getTypeName().equalsIgnoreCase(fieldType)) {
                                    if (!Boolean.TYPE.getTypeName().equalsIgnoreCase(fieldType) && !Boolean.class.getTypeName().equalsIgnoreCase(fieldType)) {
                                        if (!Character.TYPE.getTypeName().equalsIgnoreCase(fieldType) && !Character.class.getTypeName().equalsIgnoreCase(fieldType)) {
                                            return String.class.getTypeName().equalsIgnoreCase(fieldType) ? compareTwoComparable((String)value1, (String)value2) : 0;
                                        } else {
                                            return compareTwoComparable((Character)value1, (Character)value2);
                                        }
                                    } else {
                                        return compareTwoComparable((Boolean)value1, (Boolean)value2);
                                    }
                                } else {
                                    return compareTwoComparable((Byte)value1, (Byte)value2);
                                }
                            } else {
                                return compareTwoComparable((Short)value1, (Short)value2);
                            }
                        } else {
                            return compareTwoComparable((Long)value1, (Long)value2);
                        }
                    } else {
                        return compareTwoComparable((Float)value1, (Float)value2);
                    }
                } else {
                    return compareTwoComparable((Double)value1, (Double)value2);
                }
            } else {
                return compareTwoComparable((Integer)value1, (Integer)value2);
            }
        } catch (Throwable var6) {
            throw var6;
        }
    }

    static {
        CAN_COMPARE_TYPE_LIST = Lists.newArrayList(new String[]{Integer.TYPE.getTypeName(), Integer.class.getTypeName(), Double.TYPE.getTypeName(), Double.class.getTypeName(), Float.TYPE.getTypeName(), Float.class.getTypeName(), Long.TYPE.getTypeName(), Long.class.getTypeName(), Short.TYPE.getTypeName(), Short.class.getTypeName(), Byte.TYPE.getTypeName(), Byte.class.getTypeName(), Boolean.TYPE.getTypeName(), Boolean.class.getTypeName(), Character.TYPE.getTypeName(), Character.class.getTypeName(), String.class.getTypeName()});
    }
}
