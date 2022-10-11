/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package extras.apache.org.jsintegration.protocol.responseWriter;


import java.lang.reflect.Array;

/**
 * Utility class for managing arrays
 *
 * @author Anton Koinov (latest modification by $Author: matzew $)
 * @version $Revision: 557350 $ $Date: 2007-07-18 13:19:50 -0500 (Wed, 18 Jul 2007) $
 */
@SuppressWarnings("unchecked")
public class ArrayUtils
{
    public static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];
    public static final String[] EMPTY_STRING_ARRAY = new String[0];

    //~ Constructors -------------------------------------------------------------------------------

    protected ArrayUtils()
    {
        // hide from public access
    }

    //~ Methods ------------------------------------------------------------------------------------

    public static Class commonClass(Class c1, Class c2)
    {
        if (c1 == c2)
        {
            return c1;
        }

        if ((c1 == Object.class) || c1.isAssignableFrom(c2))
        {
            return c1;
        }

        if (c2.isAssignableFrom(c1))
        {
            return c2;
        }

        if (c1.isPrimitive() || c2.isPrimitive())
        {
            // REVISIT: we could try to autoconvert to Object or something appropriate
            throw new IllegalArgumentException("incompatible types " + c1 + " and " + c2);
        }

        // REVISIT: we could try to find a common supper class or interface
        return Object.class;
    }

    /**
     * Concatenates two arrays into one. If arr1 is null or empty, returns arr2.
     * If arr2 is null or empty, returns arr1. May return null if both arrays are null,
     * or one is empty and the other null. <br>
     * The concatenated array has componentType which is compatible with both input arrays (or Object[])
     *
     * @param arr1 input array
     * @param arr2 input array
     *
     * @return Object the concatenated array, elements of arr1 first
     */
    public static Object concat(Object arr1, Object arr2)
    {
        int len1 = (arr1 == null) ? (-1) : Array.getLength(arr1);

        if (len1 <= 0)
        {
            return arr2;
        }

        int len2 = (arr2 == null) ? (-1) : Array.getLength(arr2);

        if (len2 <= 0)
        {
            return arr1;
        }

        Class  commonComponentType =
            commonClass(arr1.getClass().getComponentType(), arr2.getClass().getComponentType());
        Object newArray = Array.newInstance(commonComponentType, len1 + len2);
        System.arraycopy(arr1, 0, newArray, 0, len1);
        System.arraycopy(arr2, 0, newArray, len1, len2);

        return newArray;
    }

    /**
     * Concatenates arrays into one. Any null or empty arrays are ignored.
     * If all arrays are null or empty, returns null.
     * Elements will be ordered in the order in which the arrays are supplied.
     *
     * @param arrs array of arrays
     * @return the concatenated array
     */
    public static Object concat(Object[] arrs)
    {
        int   totalLen            = 0;
        Class commonComponentType = null;
        for (int i = 0, len = arrs.length; i < len; i++)
        {
            // skip all null arrays
            if (arrs[i] == null)
            {
                continue;
            }

            int arrayLen = Array.getLength(arrs[i]);

            // skip all empty arrays
            if (arrayLen == 0)
            {
                continue;
            }

            totalLen += arrayLen;

            Class componentType = arrs[i].getClass().getComponentType();
            commonComponentType =
                (commonComponentType == null) ? componentType
                                              : commonClass(commonComponentType, componentType);
        }

        if (commonComponentType == null)
        {
            return null;
        }

        return concat(Array.newInstance(commonComponentType, totalLen), totalLen, arrs);
    }

    public static Object concat(Object toArray, int totalLen, Object[] arrs)
    {
        if (totalLen == 0)
        {
            // Should we allocate an empty array instead?
            return toArray;
        }

        if (totalLen > Array.getLength(toArray))
        {
            toArray = Array.newInstance(toArray.getClass().getComponentType(), totalLen);
        }

        for (int i = 0, len = arrs.length, offset = 0; i < len; i++)
        {
            final Object arr = arrs[i];
            if (arr != null)
            {
                int arrayLen = Array.getLength(arr);
                if (arrayLen > 0)
                {
                    System.arraycopy(arr, 0, toArray, offset, arrayLen);
                    offset += arrayLen;
                }
            }
        }

        return toArray;
    }

    public static Object concat(Object arr1, Object arr2, Object arr3)
    {
        return concat(new Object[] {arr1, arr2, arr3});
    }

    public static Object concat(Object arr1, Object arr2, Object arr3, Object arr4)
    {
        return concat(new Object[] {arr1, arr2, arr3, arr4});
    }

    public static Object concat(Object arr1, Object arr2, Object arr3, Object arr4, Object arr5)
    {
        return concat(new Object[] {arr1, arr2, arr3, arr4, arr5});
    }

    public static Object concatSameType(Object toArray, Object[] arrs)
    {
        int totalLen = 0;
        for (int i = 0, len = arrs.length; i < len; i++)
        {
            if (arrs[i] != null)
            {
                totalLen += Array.getLength(arrs[i]);
            }
        }

        return concat(toArray, totalLen, arrs);
    }



    public static boolean contains(Object[] array, Object value)
    {
        if (array == null || array.length == 0)
        {
            return false;
        }

        for (int i = 0; i < array.length; i++)
        {
            Object o = array[i];
            if ((o == null && value == null) ||
                (o != null && o.equals(value)))
            {
                return true;
            }
        }

        return false;
    }

}
