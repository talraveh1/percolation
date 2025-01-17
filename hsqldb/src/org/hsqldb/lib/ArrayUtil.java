/* Copyright (c) 2001-2021, The HSQL Development Group
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * Neither the name of the HSQL Development Group nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL HSQL DEVELOPMENT GROUP, HSQLDB.ORG,
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */


package org.hsqldb.lib;

import java.lang.reflect.Array;
import java.util.Arrays;

/**
 * Collection of static methods for operations on arrays
 *
 * @author Fred Toussi (fredt@users dot sourceforge.net)
 * @version 2.6.1
 * @since 1.7.2
 */
public final class ArrayUtil {

    public static final int              CLASS_CODE_BYTE    = 'B';
    public static final int              CLASS_CODE_CHAR    = 'C';
    public static final int              CLASS_CODE_DOUBLE  = 'D';
    public static final int              CLASS_CODE_FLOAT   = 'F';
    public static final int              CLASS_CODE_INT     = 'I';
    public static final int              CLASS_CODE_LONG    = 'J';
    public static final int              CLASS_CODE_OBJECT  = 'L';
    public static final int              CLASS_CODE_SHORT   = 'S';
    public static final int              CLASS_CODE_BOOLEAN = 'Z';
    private static final IntValueHashMap classCodeMap = new IntValueHashMap(16);

    static {
        classCodeMap.put(byte.class, ArrayUtil.CLASS_CODE_BYTE);
        classCodeMap.put(char.class, ArrayUtil.CLASS_CODE_SHORT);
        classCodeMap.put(short.class, ArrayUtil.CLASS_CODE_SHORT);
        classCodeMap.put(int.class, ArrayUtil.CLASS_CODE_INT);
        classCodeMap.put(long.class, ArrayUtil.CLASS_CODE_LONG);
        classCodeMap.put(float.class, ArrayUtil.CLASS_CODE_FLOAT);
        classCodeMap.put(double.class, ArrayUtil.CLASS_CODE_DOUBLE);
        classCodeMap.put(boolean.class, ArrayUtil.CLASS_CODE_BOOLEAN);
        classCodeMap.put(Object.class, ArrayUtil.CLASS_CODE_OBJECT);
    }

    /**
     * Returns a distinct int code for each primitive type and for all Object
     * types.
     *
     * @param cla Class
     * @return int
     */
    static int getClassCode(Class cla) {

        if (!cla.isPrimitive()) {
            return ArrayUtil.CLASS_CODE_OBJECT;
        }

        return classCodeMap.get(cla, -1);
    }

    /**
     * Clears an area of the given array of the given type.
     *
     * @param type int
     * @param data Object
     * @param from int
     * @param to int
     */
    public static void clearArray(int type, Object data, int from, int to) {

        switch (type) {

            case ArrayUtil.CLASS_CODE_BYTE : {
                byte[] array = (byte[]) data;

                Arrays.fill(array, from, to, (byte) 0);

                return;
            }
            case ArrayUtil.CLASS_CODE_CHAR : {
                char[] array = (char[]) data;

                Arrays.fill(array, from, to, (char) 0);

                return;
            }
            case ArrayUtil.CLASS_CODE_SHORT : {
                short[] array = (short[]) data;

                Arrays.fill(array, from, to, (short) 0);

                return;
            }
            case ArrayUtil.CLASS_CODE_INT : {
                int[] array = (int[]) data;

                Arrays.fill(array, from, to, 0);

                return;
            }
            case ArrayUtil.CLASS_CODE_LONG : {
                long[] array = (long[]) data;

                Arrays.fill(array, from, to, 0L);

                return;
            }
            case ArrayUtil.CLASS_CODE_FLOAT : {
                float[] array = (float[]) data;

                Arrays.fill(array, from, to, 0);

                return;
            }
            case ArrayUtil.CLASS_CODE_DOUBLE : {
                double[] array = (double[]) data;

                Arrays.fill(array, from, to, 0);

                return;
            }
            case ArrayUtil.CLASS_CODE_BOOLEAN : {
                boolean[] array = (boolean[]) data;

                Arrays.fill(array, from, to, false);

                return;
            }
            default : {
                Object[] array = (Object[]) data;

                Arrays.fill(array, from, to, null);

                return;
            }
        }
    }

    /**
     * Moves the contents of an array to allow both addition and removal of
     * elements. Used arguments must be in range.
     *
     * @param type class type of the array
     * @param array the array
     * @param usedElements count of elements of array in use
     * @param index point at which to add or remove elements
     * @param count number of elements to add or remove
     */
    public static void adjustArray(int type, Object array, int usedElements,
                                   int index, int count) {

        if (index >= usedElements || count == 0) {
            return;
        }

        int newCount = usedElements + count;
        int source;
        int target;
        int size;

        if (count >= 0) {
            source = index;
            target = index + count;
            size   = usedElements - index;
        } else {
            source = index - count;
            target = index;
            size   = usedElements - index + count;
        }

        if (size > 0) {
            System.arraycopy(array, source, array, target, size);
        }

        if (count < 0) {
            clearArray(type, array, newCount, usedElements);
        }
    }

    /**
     * Basic sort for small arrays of int.
     *
     * @param array int[]
     */
    public static void sortArray(int[] array) {

        boolean swapped;

        do {
            swapped = false;

            for (int i = 0; i < array.length - 1; i++) {
                if (array[i] > array[i + 1]) {
                    int temp = array[i + 1];

                    array[i + 1] = array[i];
                    array[i]     = temp;
                    swapped      = true;
                }
            }
        } while (swapped);
    }

    /**
     * Basic find for small arrays of Object.
     *
     * @param array Object[]
     * @param object Object
     * @return int
     */
    public static int find(Object[] array, Object object) {

        for (int i = 0; i < array.length; i++) {
            if (array[i] == object) {

                // handles both nulls
                return i;
            }

            if (object != null && object.equals(array[i])) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Finds index of value in small array.
     *
     * @param array int[]
     * @param value int
     * @return int
     */
    public static int find(int[] array, int value) {

        for (int i = 0; i < array.length; i++) {
            if (array[i] == value) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Finds index of value in the first count elements of small array.
     *
     * @param array int[]
     * @param count int
     * @param value int
     * @return int
     */
    public static int find(int[] array, int count, int value) {

        for (int i = 0; i < count; i++) {
            if (array[i] == value) {
                return i;
            }
        }

        return -1;
    }

    public static int find(short[] array, int value) {

        for (int i = 0; i < array.length; i++) {
            if (array[i] == value) {
                return i;
            }
        }

        return -1;
    }

    public static int find(short[] array, int value, int offset, int count) {

        for (int i = offset; i < offset + count; i++) {
            if (array[i] == value) {
                return i;
            }
        }

        return -1;
    }

    public static int find(char[] array, int value) {

        for (int i = 0; i < array.length; i++) {
            if (array[i] == value) {
                return i;
            }
        }

        return -1;
    }
    /**
     * Finds the first element of the array that is not equal to the given value.
     *
     * @param array int[]
     * @param value int
     * @return int
     */
    public static int findNot(int[] array, int value) {

        for (int i = 0; i < array.length; i++) {
            if (array[i] != value) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Returns true if arra and arrb contain the same set of integers, not
     * necessarily in the same order. This implies the arrays are of the same
     * length.
     *
     * @param arra int[]
     * @param arrb int[]
     * @return boolean
     */
    public static boolean areEqualSets(int[] arra, int[] arrb) {
        return arra.length == arrb.length
               && ArrayUtil.haveEqualSets(arra, arrb, arra.length);
    }

    /**
     * Returns true if the first count elements of arra and arrb are identical
     * sets of integers (not necessarily in the same order).
     *
     * @param arra int[]
     * @param arrb int[]
     * @param count int
     * @return boolean
     */
    public static boolean haveEqualSets(int[] arra, int[] arrb, int count) {

        if (count > arra.length || count > arrb.length) {
            return false;
        }

        outerloop:
        for (int i = 0; i < count; i++) {
            int val = arra[i];

            for (int j = 0; j < count; j++) {
                if (arrb[j] == val) {
                    continue outerloop;
                }
            }

            return false;
        }

        return true;
    }

    /**
     * Returns true if the first count elements of arra and arrb are identical
     * subarrays of integers
     *
     * @param arra int[]
     * @param arrb int[]
     * @param count int
     * @return boolean
     */
    public static boolean haveEqualArrays(int[] arra, int[] arrb, int count) {

        if (count > arra.length || count > arrb.length) {
            return false;
        }

        for (int j = 0; j < count; j++) {
            if (arra[j] != arrb[j]) {
                return false;
            }
        }

        return true;
    }

    /**
     * Returns true if the first count elements of arra and arrb are identical
     * subarrays of Objects
     *
     * @param arra Object[]
     * @param arrb Object[]
     * @param count int
     * @return boolean
     */
    public static boolean haveEqualArrays(Object[] arra, Object[] arrb,
                                          int count) {

        if (count > arra.length || count > arrb.length) {
            return false;
        }

        for (int j = 0; j < count; j++) {
            if (arra[j] != arrb[j]) {
                if (arra[j] == null || !arra[j].equals(arrb[j])) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Returns true if arra and arrb share any element.
     *
     * <p> Used for checks for any overlap between two arrays of column indexes.
     *
     * @param arra int[]
     * @param arrb int[]
     * @return boolean
     */
    public static boolean haveCommonElement(int[] arra, int[] arrb) {

        if (arra == null || arrb == null) {
            return false;
        }

        for (int i = 0; i < arra.length; i++) {
            int c = arra[i];

            for (int j = 0; j < arrb.length; j++) {
                if (c == arrb[j]) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Returns an int[] containing elements shared between the two arrays
     * arra and arrb. The arrays contain sets (no value is repeated).
     *
     * Used to find the overlap between two arrays of column indexes.
     * Ordering of the result arrays will be the same as in array
     * arra. The method assumes that each index is only listed
     * once in the two input arrays.
     * <p>
     * e.g.
     * <table>
     * <caption>Example</caption>
     * <tr><td colspan="3">The arrays</td></tr>
     * <tr><td>int []arra</td><td>=</td><td>{2,11,5,8}</td></tr>
     * <tr><td>int []arrb</td><td>=</td><td>{20,8,10,11,28,12}</td></tr>
     * <tr><td colspan="3">will result in:</td></tr>
     * <tr><td>int []arrc</td><td>=</td><td>{11,8}</td></tr>
     * </table>
     *
     * @param arra int[] first column indexes
     * @param arrb int[] second column indexes
     * @return int[] common indexes or <code>null</code> if there is no overlap.
     */
    public static int[] commonElements(int[] arra, int[] arrb) {

        int[] c = null;
        int   n = countCommonElements(arra, arrb);

        if (n > 0) {
            c = new int[n];

            int k = 0;

            for (int i = 0; i < arra.length; i++) {
                for (int j = 0; j < arrb.length; j++) {
                    if (arra[i] == arrb[j]) {
                        c[k++] = arra[i];
                    }
                }
            }
        }

        return c;
    }

    /**
     * Returns the number of elements shared between the two arrays containing
     * sets.<p>
     *
     * Returns the number of elements shared by two column index arrays.
     * This method assumes that each of these arrays contains a set (each
     * element index is listed only once in each index array). Otherwise the
     * returned number will NOT represent the number of unique column indexes
     * shared by both index array.
     *
     * @param arra int[] first array of column indexes.
     *
     * @param arrb int[] second array of column indexes
     *
     * @return int number of elements shared by <code>arra</code> and <code>arrb</code>
     */
    public static int countCommonElements(int[] arra, int[] arrb) {

        int k = 0;

        for (int i = 0; i < arra.length; i++) {
            for (int j = 0; j < arrb.length; j++) {
                if (arra[i] == arrb[j]) {
                    k++;

                    break;
                }
            }
        }

        return k;
    }

    public static int countCommonElements(Object[] arra, int alen,
                                          Object[] arrb) {

        int k = 0;

        for (int i = 0; i < alen; i++) {
            for (int j = 0; j < arrb.length; j++) {
                if (arra[i] == arrb[j]) {
                    k++;

                    break;
                }
            }
        }

        return k;
    }

    /**
     * Returns the count of elements in arra from position start that are
     * sequentially equal to the elements of arrb.
     *
     * @param arra byte[]
     * @param start int
     * @param arrb byte[]
     * @return int
     */
    public static int countSameElements(byte[] arra, int start, byte[] arrb) {

        int k     = 0;
        int limit = arra.length - start;

        if (limit > arrb.length) {
            limit = arrb.length;
        }

        for (int i = 0; i < limit; i++) {
            if (arra[i + start] == arrb[i]) {
                k++;
            } else {
                break;
            }
        }

        return k;
    }

    /**
     * Returns the count of elements in arra from position start that are
     * sequentially equal to the elements of arrb.
     *
     * @param arra char[]
     * @param start int
     * @param arrb char[]
     * @return int
     */
    public static int countSameElements(char[] arra, int start, char[] arrb) {

        int k     = 0;
        int limit = arra.length - start;

        if (limit > arrb.length) {
            limit = arrb.length;
        }

        for (int i = 0; i < limit; i++) {
            if (arra[i + start] == arrb[i]) {
                k++;
            } else {
                break;
            }
        }

        return k;
    }

    /**
     * Returns the count of elements in arra from position start that are
     * sequentially equal to the elements of arrb.
     *
     * @param arra int[]
     * @param start int
     * @param arrb int[]
     * @return int
     */
    public static int countSameElements(int[] arra, int start, int[] arrb) {

        int k     = 0;
        int limit = arra.length - start;

        if (limit > arrb.length) {
            limit = arrb.length;
        }

        for (int i = 0; i < limit; i++) {
            if (arra[i + start] == arrb[i]) {
                k++;
            } else {
                break;
            }
        }

        return k;
    }

    /**
     * Returns the count of elements in arra that are smaller than the value.
     *
     * @param arra int[]
     * @param value int
     * @return int
     */
    public static int countSmallerElements(int[] arra, int value) {

        int count = 0;

        for (int i = 0; i < arra.length; i++) {
            if (arra[i] < value) {
                count++;
            }
        }

        return count;
    }

    /**
     * Returns the count of elements in arra that are smaller/equal than the
     * value.
     *
     * @param arra int[]
     * @param value int
     * @return int
     */
    public static int countSmallerEqualElements(int[] arra, int value) {

        int count = 0;

        for (int i = 0; i < arra.length; i++) {
            if (arra[i] <= value) {
                count++;
            }
        }

        return count;
    }

    /**
     * Returns an array that contains all the elements of the two arrays.
     * Each array contains a set.
     *
     * @param arra int[] containing unique vlaues
     * @param arrb int[] containing unique values
     * @return int[]
     */
    public static int[] union(int[] arra, int[] arrb) {

        int commonSize  = ArrayUtil.countCommonElements(arra, arrb);

        if (commonSize == arrb.length) {
            return arra;
        }

        if (commonSize == arra.length) {
            return arrb;
        }

        int   newSize = arra.length + arrb.length - commonSize;
        int[] arrn    = Arrays.copyOf(arra, newSize);
        int   pos     = arra.length;

            mainloop:
            for (int i = 0; i < arrb.length; i++) {
                for (int j = 0; j < arra.length; j++) {
                    if (arrb[i] == arra[j]) {
                        continue mainloop;
                    }
                }

                arrn[pos++] = arrb[i];
            }

            return arrn;
    }

    /**
     * Returns an array that contains all the elements of the two arrays.
     *
     * @param arra int[]
     * @param arrb int[]
     * @return int[]
     */
    public static int[] concat(int[] arra, int[] arrb) {
        int newSize = arra.length + arrb.length;

        int[] arrn = Arrays.copyOf(arra, newSize);
        int   pos  = arra.length;

        for (int i = 0; i < arrb.length; i++) {
            arrn[pos++] = arrb[i];
        }

        return arrn;

    }

    /**
     * Returns the index of the first occurrence of arrb in arra. Or -1 if not
     * found.
     *
     * @param arra byte[]
     * @param start int
     * @param limit int
     * @param arrb byte[]
     * @return int
     */
    public static int find(byte[] arra, int start, int limit, byte[] arrb) {

        int k = start;

        limit = limit - arrb.length + 1;

        int value = arrb[0];

        for (; k < limit; k++) {
            if (arra[k] == value) {
                if (arrb.length == 1) {
                    return k;
                }

                if (containsAt(arra, k, arrb)) {
                    return k;
                }
            }
        }

        return -1;
    }

    /**
     * Returns an index into arra (or -1) where the character is not in the
     * charset byte array.
     *
     * @param arra byte[]
     * @param start int
     * @param limit int
     * @param byteSet byte[]
     * @return int
     */
    public static int findNotIn(byte[] arra, int start, int limit,
                                byte[] byteSet) {

        mainloop:
        for (int k = start; k < limit; k++) {
            for (int i = 0; i < byteSet.length; i++) {
                if (arra[k] == byteSet[i]) {
                    continue mainloop;
                }
            }

            return k;
        }

        return -1;
    }

    /**
     * Returns an index into arra (or -1) where the character is in the byteSet
     * byte array.
     *
     * @param arra byte[]
     * @param start int
     * @param limit int
     * @param byteSet byte[]
     * @return int
     */
    public static int findIn(byte[] arra, int start, int limit,
                             byte[] byteSet) {

        for (int k = start; k < limit; k++) {
            for (int i = 0; i < byteSet.length; i++) {
                if (arra[k] == byteSet[i]) {
                    return k;
                }
            }
        }

        return -1;
    }

    /**
     * Returns the index of b or c in arra. Or -1 if not found.
     *
     * @param arra byte[]
     * @param start int
     * @param limit int
     * @param b int
     * @param c int
     * @return int
     */
    public static int find(byte[] arra, int start, int limit, int b, int c) {

        int k = 0;

        for (; k < limit; k++) {
            if (arra[k] == b || arra[k] == c) {
                return k;
            }
        }

        return -1;
    }

    /**
     * Set elements of arrb true if their indexes appear in arrb.
     *
     * @param arrb boolean[]
     * @return int[]
     */
    public static int[] booleanArrayToIntIndexes(boolean[] arrb) {

        int count = 0;

        for (int i = 0; i < arrb.length; i++) {
            if (arrb[i]) {
                count++;
            }
        }

        int[] intarr = new int[count];

        count = 0;

        for (int i = 0; i < arrb.length; i++) {
            if (arrb[i]) {
                intarr[count++] = i;
            }
        }

        return intarr;
    }

    /**
     * Set elements of arrb true if their indexes appear in arrb.
     *
     * @param arra int[]
     * @param arrb boolean[]
     */
    public static void intIndexesToBooleanArray(int[] arra, boolean[] arrb) {

        for (int i = 0; i < arra.length; i++) {
            if (arra[i] < arrb.length) {
                arrb[arra[i]] = true;
            }
        }
    }

    /**
     * Return array of indexes of boolean elements that are true.
     *
     * @param arra int[]
     * @param arrb boolean[]
     * @return int
     */
    public static int countStartIntIndexesInBooleanArray(int[] arra,
            boolean[] arrb) {

        int k = 0;

        for (int i = 0; i < arra.length; i++) {
            if (arrb[arra[i]]) {
                k++;
            } else {
                break;
            }
        }

        return k;
    }

    public static void orBooleanArray(boolean[] source, boolean[] dest) {

        for (int i = 0; i < dest.length; i++) {
            dest[i] |= source[i];
        }
    }

    /**
     * Returns true if all indexes and no other positions are true in arrb. arra
     * must have no duplicates.
     *
     * arra must have no duplicates.
     *
     * @param arra int[]
     * @param arrb boolean[]
     * @return boolean
     */
    public static boolean areAllIntIndexesAsBooleanArray(int[] arra,
            boolean[] arrb) {

        for (int i = 0; i < arra.length; i++) {
            if (arrb[arra[i]]) {
                continue;
            }

            return false;
        }

        return arra.length == countTrueElements(arrb);
    }

    public static boolean areAllIntIndexesInBooleanArray(int[] arra,
            boolean[] arrb) {

        for (int i = 0; i < arra.length; i++) {
            if (arrb[arra[i]]) {
                continue;
            }

            return false;
        }

        return true;
    }

    public static boolean isAnyIntIndexInBooleanArray(int[] arra,
            boolean[] arrb) {

        for (int i = 0; i < arra.length; i++) {
            if (arrb[arra[i]]) {
                return true;
            }
        }

        return false;
    }

    /**
     * Return true if for each true element in arrb, the corresponding element
     * in arra is true
     *
     * @param arra boolean[]
     * @param arrb boolean[]
     * @return boolean
     */
    public static boolean containsAllTrueElements(boolean[] arra,
            boolean[] arrb) {

        for (int i = 0; i < arra.length; i++) {
            if (arrb[i] && !arra[i]) {
                return false;
            }
        }

        return true;
    }

    /**
     * Return count of true elements in array
     *
     * @param arra boolean[]
     * @return int
     */
    public static int countTrueElements(boolean[] arra) {

        int count = 0;

        for (int i = 0; i < arra.length; i++) {
            if (arra[i]) {
                count++;
            }
        }

        return count;
    }

    /**
     * Determines if the array has a null column for any of the positions given
     * in the rowColMap array.
     *
     * @param array Object[]
     * @param columnMap int[]
     * @return boolean
     */
    public static boolean hasNull(Object[] array, int[] columnMap) {

        int count = columnMap.length;

        for (int i = 0; i < count; i++) {
            if (array[columnMap[i]] == null) {
                return true;
            }
        }

        return false;
    }

    public static boolean hasAllNull(Object[] array, int[] columnMap) {

        int count = columnMap.length;

        for (int i = 0; i < count; i++) {
            if (array[columnMap[i]] != null) {
                return false;
            }
        }

        return true;
    }

    /**
     * Returns true if arra from position start contains all elements of arrb in
     * sequential order.
     *
     * @param arra byte[]
     * @param start int
     * @param arrb byte[]
     * @return boolean
     */
    public static boolean containsAt(byte[] arra, int start, byte[] arrb) {
        return countSameElements(arra, start, arrb) == arrb.length;
    }

    /**
     * Returns the count of elements in arra from position start that are among
     * the elements of arrb. Stops at any element not in arrb.
     *
     * @param arra byte[]
     * @param start int
     * @param arrb byte[]
     * @return int
     */
    public static int countStartElementsAt(byte[] arra, int start,
                                           byte[] arrb) {

        int k = 0;

        mainloop:
        for (int i = start; i < arra.length; i++) {
            for (int j = 0; j < arrb.length; j++) {
                if (arra[i] == arrb[j]) {
                    k++;

                    continue mainloop;
                }
            }

            break;
        }

        return k;
    }

    /**
     * Returns true if arra from position start contains all elements of arrb in
     * sequential order.
     *
     * @param arra char[]
     * @param start int
     * @param arrb char[]
     * @return boolean
     */
    public static boolean containsAt(char[] arra, int start, char[] arrb) {
        return countSameElements(arra, start, arrb) == arrb.length;
    }

    /**
     * Returns the count of elements in arra from position start that are not
     * among the elements of arrb.
     *
     * @param arra byte[]
     * @param start int
     * @param arrb byte[]
     * @return int
     */
    public static int countNonStartElementsAt(byte[] arra, int start,
            byte[] arrb) {

        int k = 0;

        mainloop:
        for (int i = start; i < arra.length; i++) {
            for (int j = 0; j < arrb.length; j++) {
                if (arra[i] == arrb[j]) {
                    break mainloop;
                }
            }

            k++;
        }

        return k;
    }

    /**
     * Byte arrays source and dest each begin at an offset in the common space.
     * If there is an overlap between dest and the first sourceLength elements
     * of the source, the overlapping elements are copied to dest. Returns count
     * of copied bytes.
     *
     * @param sourceOffset long
     * @param source byte[]
     * @param sourceOff int
     * @param sourceLength int
     * @param destOffset long
     * @param dest byte[]
     * @param destLength int
     * @return int
     */
    public static int copyBytes(long sourceOffset, byte[] source,
                                int sourceOff, int sourceLength,
                                long destOffset, byte[] dest, int destLength) {

        if (sourceOff >= source.length) {
            return 0;
        }

        if (sourceOff + sourceLength > source.length) {
            sourceLength = source.length - sourceOff;
        }

        if (destLength > dest.length) {
            destLength = dest.length;
        }

        if (sourceOffset + sourceOff >= destOffset + destLength
                || sourceOffset + sourceOff + sourceLength <= destOffset) {
            return 0;
        }

        long sourceIndex = destOffset - sourceOffset;
        long destIndex   = 0;
        int  sourceLimit = sourceOff + sourceLength;

        if (sourceIndex >= 0) {
            if (sourceIndex < sourceOff) {
                sourceIndex = sourceOff;
            }
        } else {
            destIndex   = -sourceIndex + sourceOff;
            sourceIndex = sourceOff;
        }

        sourceLength = sourceLimit - (int) sourceIndex;

        if (sourceLength > destLength - destIndex) {
            sourceLength = destLength - (int) destIndex;
        }

        System.arraycopy(source, (int) sourceIndex, dest, (int) destIndex,
                         sourceLength);

        return sourceLength;
    }

    /**
     * Copy the source to dest, returning dest or an enlarged array of result is
     * larger than dest.
     *
     * @param source byte[]
     * @param dest byte[]
     * @param destOffset int
     * @return byte[]
     */
    public static byte[] copyBytes(byte[] source, byte[] dest,
                                   int destOffset) {

        if (source.length + destOffset > dest.length) {
            byte[] newDest = new byte[source.length + destOffset];

            System.arraycopy(dest, 0, newDest, 0, dest.length);

            dest = newDest;
        }

        System.arraycopy(source, 0, dest, destOffset, source.length);

        return dest;
    }

    /**
     * Convenience wrapper for System.arraycopy().
     *
     * @param source Object
     * @param dest Object
     * @param count int
     */
    public static void copyArray(Object source, Object dest, int count) {
        System.arraycopy(source, 0, dest, 0, count);
    }

    public static void copyMoveSegment(Object source, Object dest, int size,
                                       int index, int segmentSize,
                                       int destIndex) {

        boolean forward   = index < destIndex;
        int     sliceSize = forward ? index
                                    : destIndex;

        System.arraycopy(source, 0, dest, 0, sliceSize);

        sliceSize = forward ? size - destIndex - segmentSize
                            : size - index - segmentSize;

        int sliceIndex = forward ? destIndex + segmentSize
                                 : index + segmentSize;

        System.arraycopy(source, sliceIndex, dest, sliceIndex, sliceSize);
        System.arraycopy(source, index, dest, destIndex, segmentSize);

        sliceSize  = Math.abs(index - destIndex);
        sliceIndex = forward ? index + segmentSize
                             : destIndex;

        int targetSliceIndex = forward ? index
                                       : destIndex + segmentSize;

        System.arraycopy(source, sliceIndex, dest, targetSliceIndex,
                         sliceSize);
    }

    /**
     * Returns a range of elements of source from start to end of the array.
     *
     * @param source int[]
     * @param start int
     * @param count int
     * @return int[]
     */
    public static int[] arraySlice(int[] source, int start, int count) {

        int[] slice = new int[count];

        System.arraycopy(source, start, slice, 0, count);

        return slice;
    }

    /**
     * Fills part of the array with a value.
     *
     * @param array char[]
     * @param offset int
     * @param value char
     */
    public static void fillArray(char[] array, int offset, char value) {

        int to = array.length;

        while (--to >= offset) {
            array[to] = value;
        }
    }

    /**
     * Fills part of the array with a value.
     *
     * @param array byte[]
     * @param offset int
     * @param value byte
     */
    public static void fillArray(byte[] array, int offset, byte value) {

        int to = array.length;

        while (--to >= offset) {
            array[to] = value;
        }
    }

    /**
     * Fills the array with a value.
     *
     * @param array Object[]
     * @param value Object
     */
    public static void fillArray(Object[] array, Object value) {

        int to = array.length;

        while (--to >= 0) {
            array[to] = value;
        }
    }

    /**
     * Fills the int array with a value
     *
     * @param array int[]
     * @param value int
     */
    public static void fillArray(int[] array, int value) {

        int to = array.length;

        while (--to >= 0) {
            array[to] = value;
        }
    }

    /**
     * Fills the double array with a value
     *
     * @param array double[]
     * @param value double
     */
    public static void fillArray(double[] array, double value) {

        int to = array.length;

        while (--to >= 0) {
            array[to] = value;
        }
    }

    /**
     * Fills the int array with a value
     *
     * @param array boolean[]
     * @param value boolean
     */
    public static void fillArray(boolean[] array, boolean value) {

        int to = array.length;

        while (--to >= 0) {
            array[to] = value;
        }
    }

    /**
     * Returns a duplicates of an array.
     *
     * @param source Object
     * @return Object
     */
    public static Object duplicateArray(Object source) {

        int size = Array.getLength(source);
        Object newarray =
            Array.newInstance(source.getClass().getComponentType(), size);

        System.arraycopy(source, 0, newarray, 0, size);

        return newarray;
    }

    /**
     * Returns the given array if newsize is the same as existing. Returns a new
     * array of given size, containing as many elements of the original array as
     * it can hold.
     *
     * @param source Object
     * @param newsize int
     * @return Object
     */
    public static Object resizeArrayIfDifferent(Object source, int newsize) {

        int oldsize = Array.getLength(source);

        if (oldsize == newsize) {
            return source;
        }

        Object newarray =
            Array.newInstance(source.getClass().getComponentType(), newsize);

        if (oldsize < newsize) {
            newsize = oldsize;
        }

        System.arraycopy(source, 0, newarray, 0, newsize);

        return newarray;
    }

    /**
     * Returns a new array of given size, containing as many elements of the
     * original array as it can hold. N.B. Always returns a new array even if
     * newsize parameter is the same as the old size.
     *
     * @param source Object
     * @param newsize int
     * @return Object
     */
    public static Object resizeArray(Object source, int newsize) {

        Object newarray =
            Array.newInstance(source.getClass().getComponentType(), newsize);
        int oldsize = Array.getLength(source);

        if (oldsize < newsize) {
            newsize = oldsize;
        }

        System.arraycopy(source, 0, newarray, 0, newsize);

        return newarray;
    }

    /**
     * Returns a new array containing the elements of parameter source with an
     * added element at the end. Parameter addition is an Object to add.
     *
     * @param <T> type of array element
     * @param source T[]
     * @param addition element to append to array
     * @return T[]
     */
    public static <T> T[] toAdjustedArray(T[] source, T addition) {

        int size     = source.length;
        T[] newArray = Arrays.copyOf(source, size + 1);

        newArray[size] = addition;

        return newArray;
    }

    /**
     * Returns an array containing the elements of parameter source, with one
     * element removed or added. Parameter adjust {-1, +1} indicates the
     * operation. Parameter colindex indicates the position at which an element
     * is removed or added. Parameter addition is an Object to add when adjust
     * is +1.
     *
     * @param source Object
     * @param addition Object
     * @param colindex int
     * @param adjust int
     * @return Object
     */
    public static Object toAdjustedArray(Object source, Object addition,
                                         int colindex, int adjust) {

        int newsize = Array.getLength(source) + adjust;
        Object newarray =
            Array.newInstance(source.getClass().getComponentType(), newsize);

        copyAdjustArray(source, newarray, addition, colindex, adjust);

        return newarray;
    }

    /**
     * Copies elements of source to dest. If adjust is -1 the element at
     * colindex is not copied. If adjust is +1 that element is filled with the
     * Object addition. All the rest of the elements in source are shifted left
     * or right accordingly when they are copied. If adjust is 0 the addition is
     * copied over the element at colindex. No checks are performed on array
     * sizes and an exception is thrown if they are not consistent with the
     * other arguments.
     *
     * @param source Object
     * @param dest Object
     * @param addition Object
     * @param colindex int
     * @param adjust int
     */
    public static void copyAdjustArray(Object source, Object dest,
                                       Object addition, int colindex,
                                       int adjust) {

        int length = Array.getLength(source);

        if (colindex < 0) {
            System.arraycopy(source, 0, dest, 0, length);

            return;
        }

        System.arraycopy(source, 0, dest, 0, colindex);

        if (adjust == 0) {
            int endcount = length - colindex - 1;

            Array.set(dest, colindex, addition);

            if (endcount > 0) {
                System.arraycopy(source, colindex + 1, dest, colindex + 1,
                                 endcount);
            }
        } else if (adjust < 0) {
            int endcount = length - colindex - 1;

            if (endcount > 0) {
                System.arraycopy(source, colindex + 1, dest, colindex,
                                 endcount);
            }
        } else {
            int endcount = length - colindex;

            Array.set(dest, colindex, addition);

            if (endcount > 0) {
                System.arraycopy(source, colindex, dest, colindex + 1,
                                 endcount);
            }
        }
    }

    /**
     * Similar to single slot adjusted copy, with multiple slots added or
     * removed. The colindex array is the ordered lists the slots to be added or
     * removed. The adjust argument can be {-1, +1) for remove or add. No checks
     * are performed on array sizes and no exception is thrown if they are not
     * consistent with the other arguments.
     *
     * @param source Object[]
     * @param dest Object[]
     * @param colindex int[]
     * @param adjust int
     */
    public static void copyAdjustArray(Object[] source, Object[] dest,
                                       int[] colindex, int adjust) {

        if (adjust == 0) {
            System.arraycopy(source, 0, dest, 0, source.length);

            return;
        }

        for (int i = 0, j = 0, counter = 0;
                i < source.length && j < dest.length; i++, j++) {

            if (counter < colindex.length) {
                int adjustPos = colindex[counter];

                if (adjust > 0) {
                    if (adjustPos == j) {
                        j++;
                        counter++;
                    }
                } else {
                    if (adjustPos == i) {
                        i++;
                        counter++;
                    }
                }
            }

            dest[j] = source[i];
        }
    }

    /**
     * Returns a new array with the elements in collar adjusted to reflect
     * changes at colindex. <p>
     *
     * Each element in collarr represents an index into another array
     * otherarr. <p>
     *
     * colindex is the index at which an element is added or removed.
     * Each element in the result array represents the new,
     * adjusted index. <p>
     *
     * For each element of collarr that represents an index equal to
     * colindex and adjust is -1, the result will not contain that element
     * and will be shorter than collar by one element.
     *
     * @param  colarr the source array
     * @param  colindex index at which to perform adjustment
     * @param  adjust +1, 0 or -1
     * @return new, adjusted array
     */
    public static int[] toAdjustedColumnArray(int[] colarr, int colindex,
            int adjust) {

        if (colarr == null) {
            return null;
        }

        if (colindex < 0) {
            return colarr;
        }

        int[] intarr = new int[colarr.length];
        int   j      = 0;

        for (int i = 0; i < colarr.length; i++) {
            if (colarr[i] > colindex) {
                intarr[j] = colarr[i] + adjust;

                j++;
            } else if (colarr[i] == colindex) {
                if (adjust < 0) {

                    // skip an element from colarr
                } else {
                    intarr[j] = colarr[i] + adjust;

                    j++;
                }
            } else {
                intarr[j] = colarr[i];

                j++;
            }
        }

        if (colarr.length != j) {
            int[] newarr = new int[j];

            copyArray(intarr, newarr, j);

            return newarr;
        }

        return intarr;
    }

    /**
     * similar to the function with single colindex, but with multiple
     * adjustments.
     *
     * @param colarr int[]
     * @param colindex int[]
     * @param adjust int
     * @return int[]
     */
    public static int[] toAdjustedColumnArray(int[] colarr, int[] colindex,
            int adjust) {

        if (colarr == null) {
            return null;
        }

        int[] intarr = new int[colarr.length];

        if (adjust == 0) {
            for (int i = 0; i < colarr.length; i++) {
                intarr[i] = colarr[i];
            }
        } else if (adjust < 0) {
            for (int i = 0; i < colarr.length; i++) {
                int count = countSmallerElements(colindex, colarr[i]);

                intarr[i] = colarr[i] - count;
            }
        } else {
            for (int i = 0; i < colarr.length; i++) {
                int count = countSmallerEqualElements(colindex, colarr[i]);

                intarr[i] = colarr[i] + count;
            }
        }

        return intarr;
    }

    /**
     *  Copies some elements of row into newRow by using columnMap as
     *  the list of indexes into row. <p>
     *
     *  columnMap and newRow are of equal length and are normally
     *  shorter than row. <p>
     *
     *  @param row the source array
     *  @param columnMap the list of indexes into row
     *  @param newRow the destination array
     */
    public static void projectRow(Object[] row, int[] columnMap,
                                  Object[] newRow) {

        for (int i = 0; i < columnMap.length; i++) {
            newRow[i] = row[columnMap[i]];
        }
    }

    public static void projectRow(int[] row, int[] columnMap, int[] newRow) {

        for (int i = 0; i < columnMap.length; i++) {
            newRow[i] = row[columnMap[i]];
        }
    }

    /**
     *  As above but copies in reverse direction. <p>
     *
     *  @param row the target array
     *  @param columnMap the list of indexes into row
     *  @param newRow the source array
     */
    public static void projectRowReverse(Object[] row, int[] columnMap,
                                         Object[] newRow) {

        for (int i = 0; i < columnMap.length; i++) {
            row[columnMap[i]] = newRow[i];
        }
    }

/*
    public static void copyColumnValues(int[] row, int[] colindex,
                                        int[] colobject) {

        for (int i = 0; i < colindex.length; i++) {
            colobject[i] = row[colindex[i]];
        }
    }

    public static void copyColumnValues(boolean[] row, int[] colindex,
                                        boolean[] colobject) {

        for (int i = 0; i < colindex.length; i++) {
            colobject[i] = row[colindex[i]];
        }
    }

    public static void copyColumnValues(byte[] row, int[] colindex,
                                        byte[] colobject) {

        for (int i = 0; i < colindex.length; i++) {
            colobject[i] = row[colindex[i]];
        }
    }
*/
    public static void projectMap(int[] mainMap, int[] subMap,
                                  int[] newSubMap) {

        for (int i = 0; i < subMap.length; i++) {
            for (int j = 0; j < mainMap.length; j++) {
                if (subMap[i] == mainMap[j]) {
                    newSubMap[i] = j;

                    break;
                }
            }
        }
    }

    public static void reorderMaps(int[] mainMap, int[] firstMap,
                                   int[] secondMap) {

        for (int i = 0; i < mainMap.length; i++) {
            for (int j = i; j < firstMap.length; j++) {
                if (mainMap[i] == firstMap[j]) {
                    int temp = firstMap[i];

                    firstMap[i]  = firstMap[j];
                    firstMap[j]  = temp;
                    temp         = secondMap[i];
                    secondMap[i] = secondMap[j];
                    secondMap[j] = temp;

                    break;
                }
            }
        }
    }

    public static void fillSequence(int[] colindex) {

        for (int i = 0; i < colindex.length; i++) {
            colindex[i] = i;
        }
    }

    public static char[] byteArrayToChars(byte[] bytes) {
        return byteArrayToChars(bytes, bytes.length);
    }

    public static char[] byteArrayToChars(byte[] bytes, int bytesLength) {

        char[] chars = new char[bytesLength / 2];

        for (int i = 0, j = 0; j < chars.length; i += 2, j++) {
            chars[j] = (char) ((bytes[i] << 8) + (bytes[i + 1] & 0xff));
        }

        return chars;
    }

    public static byte[] charArrayToBytes(char[] chars) {
        return charArrayToBytes(chars, chars.length);
    }

    public static byte[] charArrayToBytes(char[] chars, int length) {

        byte[] bytes = new byte[length * 2];

        for (int i = 0, j = 0; j < length; i += 2, j++) {
            int c = chars[j];

            bytes[i]     = (byte) (c >> 8);
            bytes[i + 1] = (byte) c;
        }

        return bytes;
    }

    /**
     * Returns true if char argument is in array.
     *
     * @param ch char
     * @param array char[]
     * @return boolean
     */
    public static boolean isInSortedArray(char ch, char[] array) {

        if (array.length == 0 || ch < array[0]
                || ch > array[array.length - 1]) {
            return false;
        }

        int low  = 0;
        int high = array.length;

        while (low < high) {
            int mid = (low + high) >>> 1;

            if (ch < array[mid]) {
                high = mid;
            } else if (ch > array[mid]) {
                low = mid + 1;
            } else {
                return true;
            }
        }

        return false;
    }

    /**
     * returns true if arra contains all elements of arrb
     *
     * @param arra Object[]
     * @param arrb Object[]
     * @return boolean
     */
    public static boolean containsAll(Object[] arra, Object[] arrb) {

        mainLoop:
        for (int i = 0; i < arrb.length; i++) {
            for (int j = 0; j < arra.length; j++) {
                if (arrb[i] == arra[j] || arrb[i].equals(arra[j])) {
                    continue mainLoop;
                }
            }

            return false;
        }

        return true;
    }

    /**
     * returns true if arra contains any element of arrb
     *
     * @param arra Object[]
     * @param arrb Object[]
     * @return boolean
     */
    public static boolean containsAny(Object[] arra, Object[] arrb) {

        for (int i = 0; i < arrb.length; i++) {
            for (int j = 0; j < arra.length; j++) {
                if (arrb[i] == arra[j] || arrb[i].equals(arra[j])) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * returns true if arra contains all elements of arrb
     *
     * @param arra int[]
     * @param arrb int[]
     * @return boolean
     */
    public static boolean containsAll(int[] arra, int[] arrb) {

        mainLoop:
        for (int i = 0; i < arrb.length; i++) {
            for (int j = 0; j < arra.length; j++) {
                if (arrb[i] == arra[j]) {
                    continue mainLoop;
                }
            }

            return false;
        }

        return true;
    }

    /**
     * returns true if arra contains all elements of arrb at its start
     *
     * @param arra int[]
     * @param arrb int[]
     * @return boolean
     */
    public static boolean containsAllAtStart(int[] arra, int[] arrb) {

        if (arrb.length > arra.length) {
            return false;
        }

        mainLoop:
        for (int i = 0; i < arra.length; i++) {
            if (i == arrb.length) {
                return true;
            }

            for (int j = 0; j < arrb.length; j++) {
                if (arra[i] == arrb[j]) {
                    continue mainLoop;
                }
            }

            return false;
        }

        return true;
    }

    /**
     * converts two longs to a byte[]
     *
     * @param hi long
     * @param lo long
     * @return byte[]
     */
    public static byte[] toByteArray(long hi, long lo) {

        byte[] bytes = new byte[16];
        int    count = 0;
        int    v;

        while (count < 16) {
            if (count == 0) {
                v = (int) (hi >>> 32);
            } else if (count == 4) {
                v = (int) hi;
            } else if (count == 8) {
                v = (int) (lo >>> 32);
            } else {
                v = (int) lo;
            }

            bytes[count++] = (byte) (v >>> 24);
            bytes[count++] = (byte) (v >>> 16);
            bytes[count++] = (byte) (v >>> 8);
            bytes[count++] = (byte) v;
        }

        return bytes;
    }

    public static long byteSequenceToLong(byte[] bytes, int pos) {

        long val = 0;

        for (int i = 0; i < 8; i++) {
            long b = bytes[pos + i] & 0xff;

            val += (b << ((7 - i) * 8));
        }

        return val;
    }

    /**
     * Compares two arrays. Returns -1, 0, +1. If one array is shorter and all
     * the elements are equal to the other's elements, -1 is returned.
     *
     * @param a byte[]
     * @param b byte[]
     * @return int
     */
    public static int compare(byte[] a, byte[] b) {
        return compare(a, 0, a.length, b, 0, b.length);
    }

    public static int compare(byte[] a, int aOffset, int aLength, byte[] b,
                              int bOffset, int bLength) {

        int length = aLength;

        if (length > bLength) {
            length = bLength;
        }

        for (int i = 0; i < length; i++) {
            if (a[aOffset + i] == b[bOffset + i]) {
                continue;
            }

            return (((int) a[aOffset + i]) & 0xff)
                   > (((int) b[bOffset + i]) & 0xff) ? 1
                                                     : -1;
        }

        if (aLength == bLength) {
            return 0;
        }

        return aLength < bLength ? -1
                                 : 1;
    }

    /**
     * uses 2**scale form and returns a multiple of unit that is larger or equal
     * to value
     *
     * @param value long
     * @param unit long
     * @return long
     */
    public static long getBinaryMultipleCeiling(long value, long unit) {

        long newSize = value & -unit;

        if (newSize != value) {
            newSize += unit;
        }

        return newSize;
    }

    /**
     * uses 2**scale form and returns a multiple of this that is larger or equal
     * to value
     *
     * @param value long
     * @param scale int
     * @return long
     */
    public static long getBinaryNormalisedCeiling(long value, int scale) {

        long mask    = 0xffffffffffffffffL << scale;
        long newSize = value & mask;

        if (newSize != value) {
            newSize = newSize + (1L << scale);
        }

        return newSize;
    }

    /**
     * returns the smallest value that is a power of 2 and larger or equal to
     * value
     *
     * @param value long
     * @return long
     */
    public static long getBinaryNormalisedCeiling(long value) {

        long newSize = 2;

        while (newSize < value) {
            newSize <<= 1;
        }

        return newSize;
    }

    /**
     * returns true if log2 n is in the range (0, val)
     *
     * @param n int
     * @param max int
     * @return boolean
     */
    public static boolean isTwoPower(int n, int max) {

        for (int i = 0; i <= max; i++) {
            if ((n & 1) != 0) {
                return n == 1;
            }

            n >>= 1;
        }

        return false;
    }

    /**
     * returns the largest value that is 0 or a power of 2 and is smaller or
     * equal to n
     *
     * @param n int
     * @return int
     */
    public static int getTwoPowerFloor(int n) {

        int shift = getTwoPowerScale(n);

        if (shift == 0) {
            return 0;
        }

        return 1 << shift;
    }

    /**
     * returns the log2 of largest value that is 0 or a power of 2 and is
     * smaller or equal to n
     *
     * @param n int
     * @return int
     */
    public static int getTwoPowerScale(int n) {

        int shift = 0;

        if (n == 0) {
            return 0;
        }

        for (int i = 0; i < 32; i++) {
            if ((n & 1) != 0) {
                shift = i;
            }

            n >>= 1;
        }

        return shift;
    }

    /**
     * a and b must be both positive returns (a / b) or (a / b) + 1 if remainder
     * is larger than zero
     *
     * @param a int
     * @param b int
     * @return int
     */
    public static int cdiv(int a, int b) {

        int c = a / b;

        if (a % b != 0) {
            c++;
        }

        return c;
    }

    public static long cdiv(long a, long b) {

        long c = a / b;

        if (a % b != 0) {
            c++;
        }

        return c;
    }
}
