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


package org.hsqldb.types;

import org.hsqldb.error.Error;
import org.hsqldb.error.ErrorCode;
import org.hsqldb.lib.HashSet;
import org.hsqldb.lib.IntValueHashMap;
import org.hsqldb.persist.HsqlDatabaseProperties;

// campbell-burnet@users 20051207 - patch 1.8.x initial JDBC 4.0 support work
// fredt@users - 2.0.0 code changes

/**
 * Defines the constants that are used to identify SQL types for HSQLDB JDBC
 * interface type reporting. The actual type constant values are equivalent
 * to those defined in the latest java.sql.Types, where available,
 * or those defined by ansi/iso SQL 2003 otherwise. A type sub-identifier
 * has been added to differentiate HSQLDB-specific type specializations.
 *
 * @author Campbell Burnet (campbell-burnet@users dot sourceforge.net)
 * @version 2.6.0
 * @since 1.7.2
 */
public class Types {

    /**
     * Names of types returned by JDBC methods and accepted as
     * library and user function arguments
     */
    public static final String DecimalClassName   = "java.math.BigDecimal";
    public static final String DateClassName      = "java.sql.Date";
    public static final String TimeClassName      = "java.sql.Time";
    public static final String TimestampClassName = "java.sql.Timestamp";
    public static final String BlobClassName      = "java.sql.Blob";
    public static final String ClobClassName      = "java.sql.Clob";
    /*
     SQL specifies predefined data types named by the following <key word>s:
     CHARACTER, CHARACTER VARYING, CHARACTER LARGE OBJECT, BINARY LARGE OBJECT,
     NUMERIC, DECIMAL, SMALLINT, INTEGER, BIGINT, FLOAT, REAL, DOUBLE PRECISION,
     BOOLEAN, DATE, TIME, TIMESTAMP, and INTERVAL. Also BINARY and VARBINARY
     in SQL post-2003
     SQL 2003 adds DATALINK in Part 9: Management of External TimeOutput (SQL/MED)
     and adds XML in Part 14: XML-Related Specifications (SQL/XML)
     */

    // CLI type list from Table 37
    public static final int SQL_CHAR              = 1;
    public static final int SQL_NUMERIC           = 2;
    public static final int SQL_DECIMAL           = 3;
    public static final int SQL_INTEGER           = 4;
    public static final int SQL_SMALLINT          = 5;
    public static final int SQL_FLOAT             = 6;
    public static final int SQL_REAL              = 7;
    public static final int SQL_DOUBLE            = 8;
    public static final int SQL_VARCHAR           = 12;
    public static final int SQL_BOOLEAN           = 16;
    public static final int SQL_USER_DEFINED_TYPE = 17;
    public static final int SQL_ROW               = 19;
    public static final int SQL_REF               = 20;
    public static final int SQL_BIGINT            = 25;             // different in JDBC
    public static final int SQL_BLOB              = 30;             // different in JDBC
    public static final int SQL_CLOB              = 40;             // different in JDBC
    public static final int SQL_ARRAY             = 50;             // different in JDBC - not predefined
    public static final int SQL_MULTISET = 55;                      //
    public static final int SQL_BINARY   = 60;                      // different in JDBC -in SQL post-2003
    public static final int SQL_VARBINARY = 61;                     // different in JDBC - in SQL post-2003
    public static final int SQL_DATE                      = 91;
    public static final int SQL_TIME                      = 92;
    public static final int SQL_TIMESTAMP                 = 93;     //
    public static final int SQL_TIME_WITH_TIME_ZONE       = 94;
    public static final int SQL_TIMESTAMP_WITH_TIME_ZONE  = 95;     //
    public static final int SQL_INTERVAL_YEAR             = 101;    //
    public static final int SQL_INTERVAL_MONTH            = 102;
    public static final int SQL_INTERVAL_DAY              = 103;
    public static final int SQL_INTERVAL_HOUR             = 104;
    public static final int SQL_INTERVAL_MINUTE           = 105;
    public static final int SQL_INTERVAL_SECOND           = 106;
    public static final int SQL_INTERVAL_YEAR_TO_MONTH    = 107;
    public static final int SQL_INTERVAL_DAY_TO_HOUR      = 108;
    public static final int SQL_INTERVAL_DAY_TO_MINUTE    = 109;
    public static final int SQL_INTERVAL_DAY_TO_SECOND    = 110;
    public static final int SQL_INTERVAL_HOUR_TO_MINUTE   = 111;
    public static final int SQL_INTERVAL_HOUR_TO_SECOND   = 112;
    public static final int SQL_INTERVAL_MINUTE_TO_SECOND = 113;

    // units or components not in SQL or JDBC lists, used with TRUNCATE, ROUND and other functions
    public static final int DTI_TIMEZONE_HOUR    = 121;
    public static final int DTI_TIMEZONE_MINUTE  = 122;
    public static final int DTI_DAY_OF_WEEK      = 123;
    public static final int DTI_DAY_OF_MONTH     = 124;
    public static final int DTI_DAY_OF_YEAR      = 125;
    public static final int DTI_WEEK_OF_YEAR     = 126;
    public static final int DTI_QUARTER          = 127;
    public static final int DTI_DAY_NAME         = 128;
    public static final int DTI_MONTH_NAME       = 129;
    public static final int DTI_SECONDS_MIDNIGHT = 130;
    public static final int DTI_ISO_YEAR         = 131;
    public static final int DTI_MILLISECOND      = 132;
    public static final int DTI_MICROSECOND      = 133;
    public static final int DTI_NANOSECOND       = 134;
    public static final int DTI_TIMEZONE         = 135;
    public static final int DTI_ISO_WEEK_OF_YEAR = 136;

    // used where local defines are used in the same range as SQL type numbers
    public static final int SQL_TYPE_NUMBER_LIMIT = 256;

    // These values are not in table 37 of the SQL CLI 2003 FCD, but some
    // are found in tables 6-9 and some are found in Annex A1:
    // c Header File SQLCLI.H and/or addenda in other documents,
    // such as:
    // SQL 2003 Part 9: Management of External TimeOutput (SQL/MED) : DATALINK
    // SQL 2003 Part 14: XML-Related Specifications (SQL/XML) : XML
    public static final int SQL_BIT         = 14;                   // is in SQL99 but removed from 2003
    public static final int SQL_BIT_VARYING = 15;                   // is in SQL99 but removed from 2003
    public static final int SQL_DATALINK         = 70;
    public static final int SQL_UDT              = 17;
    public static final int SQL_UDT_LOCATOR      = 18;
    public static final int SQL_BLOB_LOCATOR     = 31;
    public static final int SQL_CLOB_LOCATOR     = 41;
    public static final int SQL_ARRAY_LOCATOR    = 51;
    public static final int SQL_MULTISET_LOCATOR = 56;
    public static final int SQL_ALL_TYPES        = 0;
    public static final int SQL_DATETIME         = 9;               // collective name
    public static final int SQL_INTERVAL         = 10;              // collective name
    public static final int SQL_XML              = 137;

    // These values are taken from various SQL CLI header files
    public static final int SQL_NCHAR         = (-8);
    public static final int SQL_WCHAR         = (-8);
    public static final int SQL_WVARCHAR      = (-9);
    public static final int SQL_NVARCHAR      = (-9);
    public static final int SQL_WLONGVARCHAR  = (-10);
    public static final int SQL_NTEXT         = (-10);
    public static final int SQL_LONGVARBINARY = (-4);
    public static final int SQL_IMAGE         = (-4);
    public static final int SQL_GUID          = (-11);
    public static final int SQL_VARIANT       = (-150);

    // SQL_UDT subcodes
    public static final int SQL_SUB_DISTINCT   = 1;
    public static final int SQL_SUB_STRUCTURED = 2;

    // non-standard type not in JDBC or SQL CLI
    public static final int VARCHAR_IGNORECASE = 100;

    /**
     * The constant in the Java programming language, sometimes referred to
     * as a type code, that identifies the generic SQL type
     * <code>ARRAY</code>.
     *
     * @since JDK 1.2
     */
    public static final int ARRAY = 2003;

    /**
     * <P>The constant in the Java programming language, sometimes referred
     * to as a type code, that identifies the generic SQL type
     * <code>BIGINT</code>.
     */
    public static final int BIGINT = -5;

    /**
     * <P>The constant in the Java programming language, sometimes referred
     * to as a type code, that identifies the generic SQL type
     * <code>BINARY</code>.
     */
    public static final int BINARY = -2;

    /**
     * <P>The constant in the Java programming language, sometimes referred
     * to as a type code, that identifies the generic SQL type
     * <code>BIT</code>.
     */
    public static final int BIT = -7;

    /**
     * The constant in the Java programming language, sometimes referred to
     * as a type code, that identifies the generic SQL type
     * <code>BLOB</code>.
     *
     * @since JDK 1.2
     */
    public static final int BLOB = 2004;

    /**
     * The constant in the Java programming language, sometimes referred to
     * as a type code, that identifies the generic SQL type
     * <code>BOOLEAN</code>.
     *
     * @since JDK 1.4
     */
    public static final int BOOLEAN = SQL_BOOLEAN;

    /**
     * <P>The constant in the Java programming language, sometimes referred
     * to as a type code, that identifies the generic SQL type
     * <code>CHAR</code>.
     */
    public static final int CHAR = SQL_CHAR;

    /**
     * The constant in the Java programming language, sometimes referred to
     * as a type code, that identifies the generic SQL type
     * <code>CLOB</code>
     *
     * @since JDK 1.2
     */
    public static final int CLOB = 2005;

    /**
     * The constant in the Java programming language, sometimes referred to
     * as a type code, that identifies the generic SQL type <code>DATALINK</code>.
     *
     * @since JDK 1.4
     */
    public static final int DATALINK = 70;

    /**
     * <P>The constant in the Java programming language, sometimes referred
     * to as a type code, that identifies the generic SQL type
     * <code>DATE</code>.
     */
    public static final int DATE = SQL_DATE;

    /**
     * <P>The constant in the Java programming language, sometimes referred
     * to as a type code, that identifies the generic SQL type
     * <code>DECIMAL</code>.
     */
    public static final int DECIMAL = SQL_DECIMAL;

    /**
     * The constant in the Java programming language, sometimes referred to
     * as a type code, that identifies the generic SQL type
     * <code>DISTINCT</code>.
     *
     * @since JDK 1.2
     */
    public static final int DISTINCT = 2001;

    /**
     * <P>The constant in the Java programming language, sometimes referred
     * to as a type code, that identifies the generic SQL type
     * <code>DOUBLE</code>.
     */
    public static final int DOUBLE = SQL_DOUBLE;

    /**
     * <P>The constant in the Java programming language, sometimes referred
     * to as a type code, that identifies the generic SQL type
     * <code>FLOAT</code>.
     */
    public static final int FLOAT = SQL_FLOAT;

    /**
     * <P>The constant in the Java programming language, sometimes referred
     * to as a type code, that identifies the generic SQL type
     * <code>INTEGER</code>.
     */
    public static final int INTEGER = SQL_INTEGER;

    /**
     * The constant in the Java programming language, sometimes referred to
     * as a type code, that identifies the generic SQL type
     * <code>JAVA_OBJECT</code>.
     *
     * @since JDK 1.2
     */
    public static final int JAVA_OBJECT = 2000;

    /**
     * <P>The constant in the Java programming language, sometimes referred
     * to as a type code, that identifies the generic SQL type
     * <code>LONGVARBINARY</code>.
     */
    public static final int LONGVARBINARY = -4;

    /**
     * <P>The constant in the Java programming language, sometimes referred
     * to as a type code, that identifies the generic SQL type
     * <code>LONGVARCHAR</code>.
     */
    public static final int LONGVARCHAR = -1;

    /**
     * <P>The constant in the Java programming language, sometimes referred
     * to as a type code, that identifies the generic SQL type
     * <code>MULTISET</code>.
     */
    public static final int MULTISET = 0;                           // no java.sql.Types definition

    /**
     * <P>The constant in the Java programming language, sometimes referred
     * to as a type code, that identifies the generic SQL type
     * <code>NULL</code>.
     */
    public static final int NULL = 0;

    /**
     * <P>The constant in the Java programming language, sometimes referred
     * to as a type code, that identifies the generic SQL type
     * <code>NUMERIC</code>.
     */
    public static final int NUMERIC = SQL_NUMERIC;

    /**
     * The constant in the Java programming language that indicates
     * that the SQL type is database-specific and
     * gets mapped to a Java object that can be accessed via
     * the methods <code>getObject</code> and <code>setObject</code>.
     */
    public static final int OTHER = 1111;

    /**
     * <P>The constant in the Java programming language, sometimes referred
     * to as a type code, that identifies the generic SQL type
     * <code>REAL</code>.
     */
    public static final int REAL = SQL_REAL;

    /**
     * The constant in the Java programming language, sometimes referred to
     * as a type code, that identifies the generic SQL type
     * <code>REF</code>.
     *
     * @since JDK 1.2
     */
    public static final int REF = 2006;

    /**
     * The constant in the Java programming language, sometimes referred to
     * as a type code, that identifies the generic SQL type
     * <code>REF_CURSOR</code>.
     *
     * @since JDK 1.8
     */
    public static final int REF_CURSOR = 2012;

    /**
     * <P>The constant in the Java programming language, sometimes referred
     * to as a type code, that identifies the generic SQL type
     * <code>SMALLINT</code>.
     */
    public static final int SMALLINT = SQL_SMALLINT;

    /**
     * The constant in the Java programming language, sometimes referred to
     * as a type code, that identifies the generic SQL type
     * <code>STRUCT</code>.
     *
     * @since JDK 1.2
     */
    public static final int STRUCT = 2002;

    /**
     * <P>The constant in the Java programming language, sometimes referred
     * to as a type code, that identifies the generic SQL type
     * <code>TIME</code>.
     */
    public static final int TIME = SQL_TIME;

    /**
     * <P>The constant in the Java programming language, sometimes referred
     * to as a type code, that identifies the generic SQL type
     * <code>TIME_WITH_TIMEZONE </code>.
     *
     * @since JDK 1.8
     */
    public static final int TIME_WITH_TIMEZONE = 2013;

    /**
     * <P>The constant in the Java programming language, sometimes referred
     * to as a type code, that identifies the generic SQL type
     * <code>TIMESTAMP</code>.
     */
    public static final int TIMESTAMP = SQL_TIMESTAMP;

    /**
     * <P>The constant in the Java programming language, sometimes referred
     * to as a type code, that identifies the generic SQL type
     * <code>TIMESTAMP_WITH_TIMEZONE </code>.
     *
     * @since JDK 1.8
     */
    public static final int TIMESTAMP_WITH_TIMEZONE = 2014;

    /**
     * <P>The constant in the Java programming language, sometimes referred
     * to as a type code, that identifies the generic SQL type
     * <code>TINYINT</code>.
     */
    public static final int TINYINT = -6;

    /**
     * <P>The constant in the Java programming language, sometimes referred
     * to as a type code, that identifies the generic SQL type
     * <code>VARBINARY</code>.
     */
    public static final int VARBINARY = -3;

    /**
     * <P>The constant in the Java programming language, sometimes referred
     * to as a type code, that identifies the generic SQL type
     * <code>VARCHAR</code>.
     */
    public static final int VARCHAR = SQL_VARCHAR;

//    /**
//     * <P>The constant in the Java programming language, sometimes referred
//     * to as a type code, that identifies the recent SQL 2003 SQL type
//     * <code>XML</code>.
//     *
//     * @since SQL 2003
//     * @deprecated
//     * @see #SQLXML
//     */
//    public static final int XML = 137;
    //------------------------- JDBC 4.0 -----------------------------------

    /**
     * The constant in the Java programming language, sometimes referred to
     * as a type code, that identifies the generic SQL type <code>ROWID</code>
     *
     * @since JDK 1.6, HSQLDB 1.8.x
     *
     */
    public static final int ROWID = 2008;

    /**
     * The constant in the Java programming language, sometimes referred to
     * as a type code, that identifies the generic SQL type <code>NCHAR</code>
     *
     * @since JDK 1.6, HSQLDB 1.8.x
     */
    public static final int NCHAR = -8;

    /**
     * The constant in the Java programming language, sometimes referred to
     * as a type code, that identifies the generic SQL type <code>NVARCHAR</code>.
     *
     * @since JDK 1.6, HSQLDB 1.8.x
     */
    public static final int NVARCHAR = -9;

    /**
     * The constant in the Java programming language, sometimes referred to
     * as a type code, that identifies the generic SQL type <code>LONGNVARCHAR</code>.
     *
     * @since JDK 1.6, HSQLDB 1.8.x
     */
    public static final int LONGNVARCHAR = -10;

    /**
     * The constant in the Java programming language, sometimes referred to
     * as a type code, that identifies the generic SQL type <code>NCLOB</code>.
     *
     * @since JDK 1.6, HSQLDB 1.8.x
     */
    public static final int NCLOB = 2007;

    /**
     * The constant in the Java programming language, sometimes referred to
     * as a type code, that identifies the generic SQL type <code>XML</code>.
     *
     * @since JDK 1.6, HSQLDB 1.8.x
     */
    public static final int SQLXML = 2009;

    //----------------------------- End JDBC 4.0 -------------------------------

    /**
     * The default HSQLODB type sub-identifier. This indicates that an
     * HSQLDB type with this sub-type, if supported, is the very closest
     * thing HSQLDB offers to the JDBC/SQL2003 type
     */
    public static final int TYPE_SUB_DEFAULT = 1;

    /**
     * Every (type,type-sub) combination known in the HSQLDB context.
     * Not every combination need be supported as a table or procedure
     * column type -- such determinations are handled in DITypeInfo.
     */
    public static final int[][] ALL_TYPES = {
        {
            SQL_ARRAY, TYPE_SUB_DEFAULT
        }, {
            SQL_BIGINT, TYPE_SUB_DEFAULT
        }, {
            SQL_BINARY, TYPE_SUB_DEFAULT
        }, {
            SQL_VARBINARY, TYPE_SUB_DEFAULT
        }, {
            SQL_BLOB, TYPE_SUB_DEFAULT
        }, {
            SQL_BOOLEAN, TYPE_SUB_DEFAULT
        }, {
            SQL_CHAR, TYPE_SUB_DEFAULT
        }, {
            SQL_CLOB, TYPE_SUB_DEFAULT
        }, {
            DATALINK, TYPE_SUB_DEFAULT
        }, {
            SQL_DATE, TYPE_SUB_DEFAULT
        }, {
            SQL_DECIMAL, TYPE_SUB_DEFAULT
        }, {
            DISTINCT, TYPE_SUB_DEFAULT
        }, {
            SQL_DOUBLE, TYPE_SUB_DEFAULT
        }, {
            SQL_FLOAT, TYPE_SUB_DEFAULT
        }, {
            SQL_INTEGER, TYPE_SUB_DEFAULT
        }, {
            JAVA_OBJECT, TYPE_SUB_DEFAULT
        }, {
            SQL_NCHAR, TYPE_SUB_DEFAULT
        }, {
            NCLOB, TYPE_SUB_DEFAULT
        }, {
            SQL_ALL_TYPES, TYPE_SUB_DEFAULT
        }, {
            SQL_NUMERIC, TYPE_SUB_DEFAULT
        }, {
            SQL_NVARCHAR, TYPE_SUB_DEFAULT
        }, {
            OTHER, TYPE_SUB_DEFAULT
        }, {
            SQL_REAL, TYPE_SUB_DEFAULT
        }, {
            SQL_REF, TYPE_SUB_DEFAULT
        }, {
            ROWID, TYPE_SUB_DEFAULT
        }, {
            SQL_SMALLINT, TYPE_SUB_DEFAULT
        }, {
            STRUCT, TYPE_SUB_DEFAULT
        }, {
            SQL_TIME, TYPE_SUB_DEFAULT
        }, {
            SQL_TIMESTAMP, TYPE_SUB_DEFAULT
        }, {
            TINYINT, TYPE_SUB_DEFAULT
        }, {
            SQL_VARCHAR, TYPE_SUB_DEFAULT
        }, {
            SQL_XML, TYPE_SUB_DEFAULT
        }
    };

// lookup for types
    static final IntValueHashMap javaTypeNumbers;

//  campbell-burnet@users - We can't handle method invocations in
//                   Function.java whose number class is
//                   narrower than the corresponding internal
//                   wrapper
    private static final HashSet illegalParameterClasses;

    static {
        javaTypeNumbers = new IntValueHashMap(32);

        javaTypeNumbers.put("int", Types.SQL_INTEGER);
        javaTypeNumbers.put("java.lang.Integer", Types.SQL_INTEGER);
        javaTypeNumbers.put("double", Types.SQL_DOUBLE);
        javaTypeNumbers.put("java.lang.Double", Types.SQL_DOUBLE);
        javaTypeNumbers.put("java.lang.String", Types.SQL_VARCHAR);
        javaTypeNumbers.put("java.lang.CharSequence", Types.SQL_VARCHAR);
        javaTypeNumbers.put(DateClassName, Types.SQL_DATE);
        javaTypeNumbers.put(TimeClassName, Types.SQL_TIME);
        javaTypeNumbers.put(TimestampClassName, Types.SQL_TIMESTAMP);
        javaTypeNumbers.put(BlobClassName, Types.SQL_BLOB);
        javaTypeNumbers.put(ClobClassName, Types.SQL_CLOB);
        javaTypeNumbers.put("java.util.Date", Types.SQL_DATE);
        javaTypeNumbers.put(DecimalClassName, Types.SQL_DECIMAL);
        javaTypeNumbers.put("boolean", Types.SQL_BOOLEAN);
        javaTypeNumbers.put("java.lang.Boolean", Types.SQL_BOOLEAN);
        javaTypeNumbers.put("byte", Types.TINYINT);
        javaTypeNumbers.put("java.lang.Byte", Types.TINYINT);
        javaTypeNumbers.put("short", Types.SQL_SMALLINT);
        javaTypeNumbers.put("java.lang.Short", Types.SQL_SMALLINT);
        javaTypeNumbers.put("long", Types.SQL_BIGINT);
        javaTypeNumbers.put("java.lang.Long", Types.SQL_BIGINT);
        javaTypeNumbers.put("[B", Types.SQL_VARBINARY);
        javaTypeNumbers.put("java.lang.Object", Types.OTHER);
        javaTypeNumbers.put("java.lang.Void", Types.SQL_ALL_TYPES);
        javaTypeNumbers.put("java.util.UUID", Types.SQL_GUID);
        javaTypeNumbers.put("java.time.LocalDate", Types.SQL_DATE);
        javaTypeNumbers.put("java.time.LocalTime", Types.SQL_TIME);
        javaTypeNumbers.put("java.time.LocalDateTime", Types.SQL_TIMESTAMP);
        javaTypeNumbers.put("java.time.OffsetDateTime",
                            Types.SQL_TIMESTAMP_WITH_TIME_ZONE);
        javaTypeNumbers.put("java.time.OffsetTime",
                            Types.SQL_TIME_WITH_TIME_ZONE);
        javaTypeNumbers.put("java.time.Duration", Types.SQL_INTERVAL_SECOND);
        javaTypeNumbers.put("java.time.Period", Types.SQL_INTERVAL_MONTH);

        illegalParameterClasses = new HashSet();

        illegalParameterClasses.add(Byte.TYPE);
        illegalParameterClasses.add(Short.TYPE);
        illegalParameterClasses.add(Float.TYPE);
        illegalParameterClasses.add(Byte.class);
        illegalParameterClasses.add(Short.class);
        illegalParameterClasses.add(Float.class);

        //
    }

    /**
     * Retrieves the type object corresponding to the class
     * of an IN, IN OUT or OUT parameter or a return type.  <p>
     *
     *
     * @param  c a Class instance
     * @return java.sql.Types int value
     */
    public static Type getParameterSQLType(Class c) {

        String name;
        int    typeCode;

        if (c == null) {
            throw Error.runtimeError(ErrorCode.U_S0500, "Types");
        }

        if (Void.TYPE.equals(c)) {
            return Type.SQL_ALL_TYPES;
        }

        name     = c.getName();
        typeCode = javaTypeNumbers.get(name, Integer.MIN_VALUE);

        if (typeCode != Integer.MIN_VALUE) {
            return Type.getDefaultTypeWithSize(typeCode);
        }

        if (c.isArray()) {
            Class c1 = c.getComponentType();

            name     = c1.getName();
            typeCode = javaTypeNumbers.get(name, Integer.MIN_VALUE);

            if (typeCode == Types.SQL_ALL_TYPES) {
                return null;
            }

            if (typeCode != Integer.MIN_VALUE) {
                return Type.getDefaultTypeWithSize(typeCode);
            }

            return null;
        }

        if (name.equals("java.sql.Array")) {
            return Type.getDefaultArrayType(Types.SQL_ALL_TYPES);
        }

        return null;
    }

    public static boolean acceptsZeroPrecision(int type) {

        switch (type) {

            case Types.SQL_TIME :
            case Types.SQL_TIMESTAMP :
                return true;

            default :
                return false;
        }
    }

    public static boolean requiresPrecision(int type) {

        switch (type) {

            case Types.SQL_BIT_VARYING :
            case Types.SQL_VARBINARY :
            case Types.SQL_VARCHAR :
            case Types.SQL_NVARCHAR :
                return true;

            default :
                return false;
        }
    }

    /**
     * Types that accept precision params in column definition or casts.
     */
    public static boolean acceptsPrecision(int type) {

        switch (type) {

            case Types.LONGVARCHAR :
            case Types.LONGVARBINARY :
            case Types.SQL_ARRAY :
            case Types.SQL_BINARY :
            case Types.SQL_BIT :
            case Types.SQL_BIT_VARYING :
            case Types.SQL_BLOB :
            case Types.SQL_CHAR :
            case Types.SQL_NCHAR :
            case Types.SQL_CLOB :
            case Types.NCLOB :
            case Types.SQL_VARBINARY :
            case Types.SQL_VARCHAR :
            case Types.SQL_NVARCHAR :
            case Types.VARCHAR_IGNORECASE :
            case Types.SQL_DECIMAL :
            case Types.SQL_NUMERIC :
            case Types.SQL_FLOAT :
            case Types.SQL_TIME :
            case Types.SQL_TIMESTAMP :
            case Types.SQL_INTERVAL_YEAR :
            case Types.SQL_INTERVAL_YEAR_TO_MONTH :
            case Types.SQL_INTERVAL_MONTH :
            case Types.SQL_INTERVAL_DAY :
            case Types.SQL_INTERVAL_DAY_TO_HOUR :
            case Types.SQL_INTERVAL_DAY_TO_MINUTE :
            case Types.SQL_INTERVAL_DAY_TO_SECOND :
            case Types.SQL_INTERVAL_HOUR :
            case Types.SQL_INTERVAL_HOUR_TO_MINUTE :
            case Types.SQL_INTERVAL_HOUR_TO_SECOND :
            case Types.SQL_INTERVAL_MINUTE :
            case Types.SQL_INTERVAL_MINUTE_TO_SECOND :
            case Types.SQL_INTERVAL_SECOND :
                return true;

            default :
                return false;
        }
    }

    public static boolean acceptsScaleCreateParam(int type) {

        switch (type) {

            case Types.SQL_INTERVAL_SECOND :
                return true;

            case Types.SQL_DECIMAL :
            case Types.SQL_NUMERIC :
                return true;

            default :
                return false;
        }
    }

    /**
     * A reasonable/customizable number to avoid the shortcomings/defects
     * associated with doing a dynamic scan of results to determine
     * the value.  In practice, it turns out that single query yielding
     * widely varying values for display size of CHAR and VARCHAR columns
     * on repeated execution results in patently poor usability, as some fairly
     * high-profile, otherwise "enterprise-quality" RAD tools depend on
     * on the first value returned to lay out forms and limit the size of
     * single line edit controls, set corresponding local datastore storage
     * sizes, etc. In practice, It also turns out that many tools (due to
     * the original lack of PreparedStatement.getMetaData() in JDK 1.1) emulate
     * a SQL_DESCRIBE by executing a query hopefully guaranteed to return no
     * or very few rows for example: select ... from ... where 1=0.
     * Using the dynamic scan of 1.7.2 RC5 and previous, therefore, the
     * minimum display size value (1) was often being generated during
     * a tool's describe phase.  Upon subsequent "real" retrievals, some
     * tools complain that CHAR and VARCHAR result values exceeded the
     * originally reported display size and refused to fetch further values.
     */
    public static final int MAX_CHAR_OR_VARCHAR_DISPLAY_SIZE =
        MAX_CHAR_OR_VARCHAR_DISPLAY_SIZE();

    // So that the variable can be both public static final and
    // customizable through system properties if required.
    //
    // 32766 (0x7ffe) seems to be a magic number over which several
    // rather high-profile RAD tools start to have problems
    // regarding layout and allocation stress.  It is gently
    // recommended that LONGVARCHAR be used for larger values in RAD
    // tool layout & presentation use cases until such time as we provide
    // true BLOB support (at which point, LONGVARCHAR will most likely become
    // an alias for CLOB).
    //
    // Most GUI tools seem to handle LONGVARCHAR gracefully by:
    //
    // 1.) refusing to directly display such columns in graphical query results
    // 2.) providing other means to retrieve and display such values
    private static int MAX_CHAR_OR_VARCHAR_DISPLAY_SIZE() {

        try {
            return Integer.getInteger(
                HsqlDatabaseProperties.system_max_char_or_varchar_display_size,
                32766).intValue();
        } catch (SecurityException e) {
            return 32766;
        }
    }

    public static boolean isSearchable(int type) {

        switch (type) {

            case Types.SQL_BLOB :
            case Types.SQL_CLOB :
            case Types.NCLOB :
            case Types.JAVA_OBJECT :
            case Types.STRUCT :
            case Types.OTHER :
            case Types.ROWID :
                return false;

            case Types.SQL_ARRAY :
            default :
                return true;
        }
    }
}
