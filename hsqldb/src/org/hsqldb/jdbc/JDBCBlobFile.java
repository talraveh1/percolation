/* Copyright (c) 2001-2022, The HSQL Development Group
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


package org.hsqldb.jdbc;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.io.Writer;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.hsqldb.error.ErrorCode;
import org.hsqldb.lib.CountdownInputStream;
import org.hsqldb.lib.FileUtil;
import org.hsqldb.lib.FrameworkLogger;
import org.hsqldb.lib.InOutUtil;
import org.hsqldb.lib.KMPSearchAlgorithm;

/**
 * A client-side file-based implementation of Blob.
 *
 * <!-- start Release-specific documentation -->
 * <div class="ReleaseSpecificDocumentation">
 * <h1>HSQLDB-Specific Information:</h1> <p>
 *
 * Starting with 2.1, in addition to HSQLDB driver support for both client-side
 * in-memory and remote SQL CLOB data implementations, this class is provided
 * to expose efficient, relatively high-performance BLOB operations over client
 * accessible files.<p>
 *
 * <b>Design Notes</b><p>
 *
 * Although it is possible to implement a transactional version of this class,
 * the present implementation directly propagates changes to the underlying
 * file such that changes become visible as soon as they are either
 * implicitly or explicitly flushed to disk.
 * <p>
 *
 * </div>
 * <!-- end release-specific documentation -->
 * @author Campbell Burnet (campbell-burnet@users dot sourceforge.net)
 * @version 2.7.1
 * @since HSQLDB 2.1
 */
public class JDBCBlobFile implements java.sql.Blob {
    private static final FrameworkLogger LOG = FrameworkLogger.getLog(
            JDBCBlobFile.class);

    /**
     * Returns the number of bytes in the <code>BLOB</code> value
     * designated by this <code>Blob</code> object.
     * @return length of the <code>BLOB</code> in bytes
     * @exception SQLException if there is an error accessing the
     * length of the <code>BLOB</code>
     * @exception java.sql.SQLFeatureNotSupportedException if the JDBC driver does not support
     * this method
     * @since JDK 1.2
     */
    public long length() throws SQLException {

        checkClosed();

        try {
            return m_file.length();
        } catch (SecurityException e) {
            throw JDBCUtil.sqlException(e);
        }
    }

    /**
     * Retrieves all or part of the <code>BLOB</code>
     * value that this <code>Blob</code> object represents, as an array of
     * bytes.  This <code>byte</code> array contains up to <code>length</code>
     * consecutive bytes starting at position <code>pos</code>.
     *
     * @param pos the ordinal position of the first byte in the
     *        <code>BLOB</code> value to be extracted; the first byte is at
     *        position 1
     * @param length the number of consecutive bytes to be copied; the value
     * for length must be 0 or greater
     * @return a byte array containing up to <code>length</code>
     *         consecutive bytes from the <code>BLOB</code> value designated
     *         by this <code>Blob</code> object, starting with the
     *         byte at position <code>pos</code>
     * @exception SQLException if there is an error accessing the
     *            <code>BLOB</code> value; if pos is less than 1 or length is
     * less than 0
     * @exception java.sql.SQLFeatureNotSupportedException if the JDBC driver does not support
     * this method
     * @see #setBytes
     * @since JDK 1.2
     */
    public byte[] getBytes(final long pos,
                           final int length) throws SQLException {

        InputStream           is   = null;
        ByteArrayOutputStream baos = null;
        final int initialBufferSize =
            Math.min(InOutUtil.DEFAULT_COPY_BUFFER_SIZE, length);

        try {
            is   = getBinaryStream(pos, length);
            baos = new ByteArrayOutputStream(initialBufferSize);

            InOutUtil.copy(is, baos, length);
        } catch (IOException ex) {
            throw JDBCUtil.sqlException(ex);
        } finally {
            closeSafely(is);
        }

        return baos.toByteArray();
    }

    /**
     * Retrieves the <code>BLOB</code> value designated by this
     * <code>Blob</code> instance as a stream.
     *
     * @return a stream containing the <code>BLOB</code> data
     * @exception SQLException if there is an error accessing the
     *            <code>BLOB</code> value
     * @exception java.sql.SQLFeatureNotSupportedException if the JDBC driver does not support
     * this method
     * @see #setBinaryStream
     * @since JDK 1.2
     */
    public InputStream getBinaryStream() throws SQLException {
        return getBinaryStream(1, Long.MAX_VALUE);
    }

    /**
     * Retrieves the byte position at which the specified byte array
     * <code>pattern</code> begins within the <code>BLOB</code>
     * value that this <code>Blob</code> object represents.  The
     * search for <code>pattern</code> begins at position
     * <code>start</code>.
     *
     * @param pattern the byte array for which to search
     * @param start the position at which to begin searching; the
     *        first position is 1
     * @return the position at which the pattern appears, else -1
     * @exception SQLException if there is an error accessing the
     * <code>BLOB</code> or if start is less than 1
     * @exception java.sql.SQLFeatureNotSupportedException if the JDBC driver does not support
     * this method
     * @since JDK 1.2
     */
    public long position(final byte[] pattern,
                         final long start) throws SQLException {

        if (start < 1) {
            throw JDBCUtil.outOfRangeArgument("start: " + start);
        } else if (pattern == null || pattern.length == 0) {
            return -1L;
        }

        final long length = this.length();

        if (start > length || pattern.length > length || start > length
                - pattern.length) {
            return -1;
        }

        return position0(pattern, start);
    }

    private long position0(final byte[] pattern, final long start) throws
            SQLException {
        InputStream is = null;

        try {
            is = getBinaryStream(start, Long.MAX_VALUE);

            //@todo maybe single-byte encoding reader
            //      and java.util.Scanner.findWithinHorizon.
            //      Need to do comparative benchmark and unit
            //      tests first.
            final long matchOffset = KMPSearchAlgorithm.search(is, pattern,
                    KMPSearchAlgorithm.computeTable(pattern));

            return (matchOffset == -1) ? -1
                    : start + matchOffset;
        } catch (IOException ex) {
            throw JDBCUtil.sqlException(ex);
        } finally {
            closeSafely(is);
        }
    }

    /**
     * Retrieves the byte position in the <code>BLOB</code> value
     * designated by this <code>Blob</code> object at which
     * <code>pattern</code> begins.  The search begins at position
     * <code>start</code>.
     *
     * @param pattern the <code>Blob</code> object designating
     * the <code>BLOB</code> value for which to search
     * @param start the position in the <code>BLOB</code> value
     *        at which to begin searching; the first position is 1
     * @return the position at which the pattern begins, else -1
     * @exception SQLException if there is an error accessing the
     *            <code>BLOB</code> value or if start is less than 1
     * @exception java.sql.SQLFeatureNotSupportedException if the JDBC driver does not support
     * this method
     * @since JDK 1.2
     */
    public long position(final Blob pattern,
                         final long start) throws SQLException {

        if (start < 1) {
            throw JDBCUtil.outOfRangeArgument("start: " + start);
        }

        if (pattern == null) {
            return -1;
        }

        final long patternLength = pattern.length();

        if (patternLength == 0) {
            return -1;
        } else if (patternLength > Integer.MAX_VALUE) {
            throw JDBCUtil.outOfRangeArgument("pattern.length(): "
                    + patternLength);
        }

        final long length = this.length();

        if (start > length || patternLength > length || start > length
                - patternLength) {
            return -1;
        }

        final byte[] bytePattern = (pattern instanceof JDBCBlob)
                ? ((JDBCBlob) pattern).data()
                : pattern.getBytes(1L, (int) patternLength);

        return position0(bytePattern, start);
    }

    // -------------------------- JDBC 3.0 -----------------------------------

    /**
     * Writes the given array of bytes to the <code>BLOB</code> value that
     * this <code>Blob</code> object represents, starting at position
     * <code>pos</code>, and returns the number of bytes written.
     * The array of bytes will overwrite the existing bytes
     * in the <code>Blob</code> object starting at the position
     * <code>pos</code>.  If the end of the <code>Blob</code> value is reached
     * while writing the array of bytes, then the length of the <code>Blob</code>
     * value will be increased to accommodate the extra bytes.
     * <p>
     * <b>Note:</b> If the value specified for <code>pos</code>
     * is greater than the length+1 of the <code>BLOB</code> value then the
     * behavior is undefined. Some JDBC drivers may throw a
     * <code>SQLException</code> while other drivers may support this
     * operation.
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h1>HSQLDB-Specific Information:</h1> <p>
     *
     * This operation affects only the content of the underlying file; it has no
     * effect upon a value stored in a database. To propagate the updated
     * Blob value to a database, it is required to supply the Blob instance to
     * an updating or inserting setXXX method of a Prepared or Callable
     * Statement, or to supply the Blob instance to an updateXXX method of an
     * updatable ResultSet. <p>
     *
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param pos the position in the <code>BLOB</code> object at which
     *        to start writing; the first position is 1
     * @param bytes the array of bytes to be written to the <code>BLOB</code>
     *        value that this <code>Blob</code> object represents
     * @return the number of bytes written
     * @exception SQLException if there is an error accessing the
     *            <code>BLOB</code> value or if pos is less than 1
     * @exception java.sql.SQLFeatureNotSupportedException if the JDBC driver does not support
     * this method
     * @see #getBytes
     * @since JDK 1.4
     */
    public int setBytes(final long pos,
                        final byte[] bytes) throws SQLException {
        return setBytes(pos, bytes, 0, bytes == null ? 0
                                                     : bytes.length);
    }

    /**
     * Writes all or part of the given <code>byte</code> array to the
     * <code>BLOB</code> value that this <code>Blob</code> object represents
     * and returns the number of bytes written.
     * Writing starts at position <code>pos</code> in the <code>BLOB</code>
     * value; <code>len</code> bytes from the given byte array are written.
     * The array of bytes will overwrite the existing bytes
     * in the <code>Blob</code> object starting at the position
     * <code>pos</code>.  If the end of the <code>Blob</code> value is reached
     * while writing the array of bytes, then the length of the <code>Blob</code>
     * value will be increased to accommodate the extra bytes.
     * <p>
     * <b>Note:</b> If the value specified for <code>pos</code>
     * is greater than the length+1 of the <code>BLOB</code> value then the
     * behavior is undefined. Some JDBC drivers may throw a
     * <code>SQLException</code> while other drivers may support this
     * operation.
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h1>HSQLDB-Specific Information:</h1> <p>
     *
     * This operation affects only the content of the underlying file; it has no
     * effect upon a value stored in a database. To propagate the updated
     * Blob value to a database, it is required to supply the Blob instance to
     * an updating or inserting setXXX method of a Prepared or Callable
     * Statement, or to supply the Blob instance to an updateXXX method of an
     * updatable ResultSet. <p>
     *
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param pos the position in the <code>BLOB</code> object at which
     *        to start writing; the first position is 1
     * @param bytes the array of bytes to be written to this <code>BLOB</code>
     *        object
     * @param offset the offset into the array <code>bytes</code> at which
     *        to start reading the bytes to be set
     * @param len the number of bytes to be written to the <code>BLOB</code>
     *        value from the array of bytes <code>bytes</code>
     * @return the number of bytes written
     * @exception SQLException if there is an error accessing the
     *            <code>BLOB</code> value or if pos is less than 1
     * @exception java.sql.SQLFeatureNotSupportedException if the JDBC driver does not support
     * this method
     * @see #getBytes
     * @since JDK 1.4
     */
    public int setBytes(final long pos, final byte[] bytes, final int offset,
                        final int len) throws SQLException {

        if (bytes == null) {
            throw JDBCUtil.nullArgument("bytes");
        }

        final OutputStream os = setBinaryStream(pos);

        try {
            os.write(bytes, offset, len);
        } catch (IOException ex) {
            throw JDBCUtil.sqlException(ex);
        } finally {
            closeSafely(os);
        }

        return len;
    }

    /**
     * Retrieves a stream that can be used to write to the <code>BLOB</code>
     * value that this <code>Blob</code> object represents.  The stream begins
     * at position <code>pos</code>.
     * The  bytes written to the stream will overwrite the existing bytes
     * in the <code>Blob</code> object starting at the position
     * <code>pos</code>.  If the end of the <code>Blob</code> value is reached
     * while writing to the stream, then the length of the <code>Blob</code>
     * value will be increased to accommodate the extra bytes.
     * <p>
     * <b>Note:</b> If the value specified for <code>pos</code>
     * is greater than the length+1 of the <code>BLOB</code> value then the
     * behavior is undefined. Some JDBC drivers may throw a
     * <code>SQLException</code> while other drivers may support this
     * operation.
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h1>HSQLDB-Specific Information:</h1> <p>
     *
     * TimeOutput written to the returned stream affects only the content of the
     * underlying file; it has no effect upon a value stored in a database.
     * To propagate the updated Blob value to a database, it is required to
     * supply the Blob instance to an updating or inserting setXXX method of a
     * Prepared or Callable Statement, or to supply the Blob instance to an
     * updateXXX method of an updateable ResultSet. <p>
     *
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param pos the position in the <code>BLOB</code> value at which
     *        to start writing; the first position is 1
     * @return a <code>java.io.OutputStream</code> object to which data can
     *         be written
     * @exception SQLException if there is an error accessing the
     *            <code>BLOB</code> value or if pos is less than 1
     * @exception java.sql.SQLFeatureNotSupportedException if the JDBC driver does not support
     * this method
     * @see #getBinaryStream
     * @since JDK 1.4
     */
    public OutputStream setBinaryStream(final long pos) throws SQLException {

        if (pos < 1) {
            throw JDBCUtil.invalidArgument("pos: " + pos);
        }

        checkClosed();
        createFile();

        OutputStream adapter;

        try {
            adapter = new OutputStreamAdapter(m_file, pos - 1) {
                private boolean closed;
                public synchronized void close() throws IOException {
                    if (closed) {
                        return;
                    }
                    closed = true;
                    try {
                        super.close();
                    } finally {
                        m_streams.remove(this);
                    }
                }
            };
        } catch (IOException ex) {
            throw JDBCUtil.sqlException(ex);
        } catch (IllegalArgumentException ex) {
            throw JDBCUtil.sqlException(ex);
        } catch (SecurityException ex) {
            throw JDBCUtil.sqlException(ex);
        }

        m_streams.add(adapter);

        final OutputStream result = new BufferedOutputStream(adapter);

        return result;
    }

    /**
     * Truncates the <code>BLOB</code> value that this <code>Blob</code>
     * object represents to be <code>len</code> bytes in length.
     * <p>
     * <b>Note:</b> If the value specified for <code>pos</code>
     * is greater than the length+1 of the <code>BLOB</code> value then the
     * behavior is undefined. Some JDBC drivers may throw a
     * <code>SQLException</code> while other drivers may support this
     * operation.
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h1>HSQLDB-Specific Information:</h1> <p>
     *
     * This operation affects only the length of the underlying file; it has no
     * effect upon a value stored in a database. To propagate the truncated
     * Blob value to a database, it is required to supply the Blob instance to
     * an updating or inserting setXXX method of a Prepared or Callable
     * Statement, or to supply the Blob instance to an updateXXX method of an
     * updatable ResultSet. <p>
     *
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param len the length, in bytes, to which the <code>BLOB</code> value
     *        that this <code>Blob</code> object represents should be truncated
     * @exception SQLException if there is an error accessing the
     *            <code>BLOB</code> value or if len is less than 0
     * @exception java.sql.SQLFeatureNotSupportedException if the JDBC driver does not support
     * this method
     * @since JDK 1.4
     */
    public void truncate(long len) throws SQLException {

        if (len < 0) {
            throw JDBCUtil.invalidArgument("len: " + len);
        }

        checkClosed();

        RandomAccessFile randomAccessFile = null;

        try {
            randomAccessFile = new RandomAccessFile(m_file, "rw");

            randomAccessFile.setLength(len);
        } catch (IOException ex) {
            throw JDBCUtil.sqlException(ex);
        } catch (IllegalArgumentException ex) {
            throw JDBCUtil.sqlException(ex);
        } catch (SecurityException ex) {
            throw JDBCUtil.sqlException(ex);
        } finally {
            closeSafely(randomAccessFile);
        }
    }

    /**
     * This method frees the <code>Blob</code> object and releases the resources that
     * it holds. The object is invalid once the <code>free</code>
     * method is called.
     * <p>
     * After <code>free</code> has been called, any attempt to invoke a
     * method other than <code>free</code> will result in a <code>SQLException</code>
     * being thrown.  If <code>free</code> is called multiple times, the subsequent
     * calls to <code>free</code> are treated as a no-op.
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h1>HSQLDB-Specific Information:</h1> <p>
     *
     * This operation closes any input and/or output streams obtained
     * via {@link #getBinaryStream()}, {@link #getBinaryStream(long, long)} or
     * {@link #setBinaryStream(long)}. <p>
     *
     * Additionally, if the property {@link #isDeleteOnFree()} is true, then
     * an attempt is made to delete the backing file.
     *
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @throws SQLException if an error occurs releasing
     * the Blob's resources
     * @exception java.sql.SQLFeatureNotSupportedException if the JDBC driver does not support
     * this method
     * @see #setDeleteOnFree(boolean)
     * @see #isDeleteOnFree()
     * @since JDK 1.6
     */
    public synchronized void free() throws SQLException {

        if (m_closed) {
            return;
        }

        m_closed = true;

        final List<Object> streams = new ArrayList<Object>(m_streams);

        m_streams = null;

        for (Iterator<Object> itr = streams.iterator(); itr.hasNext(); ) {
            final Object stream = itr.next();

            closeSafely(stream);
        }

        if (m_deleteOnFree) {
            try {
                m_file.delete();
            } catch (SecurityException e) {}
        }
    }

    /**
     * Returns an <code>InputStream</code> object that contains a partial <code>Blob</code> value,
     * starting  with the byte specified by pos, which is length bytes in length.
     *
     * @param pos the offset to the first byte of the partial value to be retrieved.
     *  The first byte in the <code>Blob</code> is at position 1
     * @param length the length in bytes of the partial value to be retrieved
     * @return <code>InputStream</code> through which the partial <code>Blob</code> value can be read.
     * @throws SQLException if pos is less than 1 or if pos is greater than the number of bytes
     * in the <code>Blob</code> or if pos + length is greater than the number of bytes
     * in the <code>Blob</code>
     *
     * @exception java.sql.SQLFeatureNotSupportedException if the JDBC driver does not support
     * this method
     * @since JDK 1.6
     */
    public InputStream getBinaryStream(final long pos,
                                       final long length) throws SQLException {

        if (pos < 1) {
            throw JDBCUtil.outOfRangeArgument("pos: " + pos);
        }

        checkClosed();

        InputStream result;
        
        final List streams = m_streams;

        try {
            result = new InputStreamAdapter(m_file, pos - 1, length) {
                private boolean closed;
                private final InputStream self = this;

                public void close() throws IOException {
                    if (closed) {
                        return;
                    }
                    closed = true;
                    try {
                        super.close();
                    } finally {
                        streams.remove(self);
                    }
                }
            };
        } catch (Exception ex) {
            throw JDBCUtil.sqlException(ex);
        }

        streams.add(result);

        return result;
    }

    /**
     * Retrieves the canonical <code>File</code> object denoting the file that
     * backs this BLOB.
     *
     * @return the file that backs this BLOB.
     */
    public File getFile() {
        return m_file;
    }

    /**
     * Retrieves whether an attempt to delete the backing file
     * is made in response to invocation of {@link #free()}.
     *
     * @return true if backing file deletion is attempted; otherwise false.
     */
    public boolean isDeleteOnFree() {
        return m_deleteOnFree;
    }

    /**
     * Assigns whether an attempt to delete the backing file
     * is made in response to invocation of {@link #free()}.
     *
     * @param deleteOnFree the new value to assign
     */
    public void setDeleteOnFree(boolean deleteOnFree) {
        m_deleteOnFree = deleteOnFree;
    }

    //--------------------------------------------------------------------------
    // Internal Implementation
    //--------------------------------------------------------------------------
    public static final String TEMP_FILE_PREFIX = "hsql_jdbc_blob_file_";
    public static final String TEMP_FILE_SUFFIX = ".tmp";

    //
    private final File m_file;
    private boolean    m_closed;
    private boolean    m_deleteOnFree;
    private List<Object> m_streams = new ArrayList<Object>();


    /**
     * Convenience constructor; equivalent to JDBCBlobFile(true);
     *
     * @throws SQLException If a file could not be created or if a security
     *         manager exists and its <code>{@link
     *         java.lang.SecurityManager#checkWrite(java.lang.String)}</code>
     *         method does not allow a file to be created.
     */
    public JDBCBlobFile() throws SQLException {
        this(true);
    }

    /**
     * Constructs a new instance backed by a File object created in response
     * to invoking File.createTempFile(TEMP_FILE_PREFIX,TEMP_FILE_SUFFIX) <p>
     *
     * @param deleteOnFree Assigns whether an attempt to delete the backing file
     *                     is to be made in response to invocation of {@link #free()}.
     *
     * @throws SQLException If a file could not be created or if a security
     *         manager exists and its <code>{@link
     *         java.lang.SecurityManager#checkWrite(java.lang.String)}</code>
     *         method does not allow a file to be created.
     */
    public JDBCBlobFile(boolean deleteOnFree) throws SQLException {

        m_deleteOnFree = deleteOnFree;

        try {
            m_file = File.createTempFile(TEMP_FILE_PREFIX,
                                         TEMP_FILE_SUFFIX).getCanonicalFile();
        } catch (IOException ex) {
            throw JDBCUtil.sqlException(ex);
        }
    }

    /**
     * Convenience constructor; equivalent to JDBCBlobFile(file, false);
     *
     * @param file used to back this BLOB instance.
     * @throws SQLException If an I/O error occurs, which is possible because
     *         the construction of the canonical pathname may require file system
     *         queries; if a required system property value cannot be accessed,
     *         or if a security manager exists and its <code>{@link
     *         java.lang.SecurityManager#checkRead}</code> method denies
     *         read access to the file
     */
    public JDBCBlobFile(final File file) throws SQLException {
        this(file, false);
    }

    /**
     *  Constructs a new instance backed by the given File object.
     *
     * @param file used to back this BLOB instance.
     * @param deleteOnFree Assigns whether an attempt to delete the backing file
     *                     is to be made in response to invocation of {@link #free()}.
     * @throws SQLException If an I/O error occurs, which is possible because
     *         the construction of the canonical pathname may require file system
     *         queries; if a required system property value cannot be accessed,
     *         or if a security manager exists and its <code>{@link
     *         java.lang.SecurityManager#checkRead}</code> method denies
     *         read access to the file
     */
    public JDBCBlobFile(final File file,
                        boolean deleteOnFree) throws SQLException {

        m_deleteOnFree = deleteOnFree;

        try {
            m_file = file.getCanonicalFile();
        } catch (IOException ex) {
            throw JDBCUtil.sqlException(ex);
        }

        checkIsFile(/*checkExists*/false);
    }

    protected final void checkIsFile(boolean checkExists) throws SQLException {

        boolean exists = false;
        boolean isFile = false;

        try {
            exists = m_file.exists();
        } catch (Exception ex) {
            throw JDBCUtil.sqlException(ex);
        }

        if (exists) {
            try {
                isFile = m_file.isFile();
            } catch (Exception ex) {
                throw JDBCUtil.sqlException(ex);
            }
        }

        if (exists) {
            if (!isFile) {
                throw JDBCUtil.invalidArgument("Is not a file: " + m_file);
            }
        } else if (checkExists) {
            throw JDBCUtil.invalidArgument("Does not exist: " + m_file);
        }
    }

    private void checkClosed() throws SQLException {

        if (m_closed) {
            throw JDBCUtil.sqlException(ErrorCode.X_07501);
        }
    }

    private void createFile() throws SQLException {

        try {
            if (!m_file.exists()) {
                FileUtil.getFileUtil().makeParentDirectories(m_file);
                m_file.createNewFile();
            }
        } catch (IOException ex) {
            throw JDBCUtil.sqlException(ex);
        }

        checkIsFile( /*checkExists*/true);
    }

   //<editor-fold defaultstate="collapsed" desc="JAVA 1.2 compliant closeSafely(...)">
    private static void closeSafely(RandomAccessFile target) {
        if (target != null) {
            try {
                target.close();
            } catch (IOException ignoredIoe) {
                LOG.info(ignoredIoe.getMessage(), ignoredIoe);
            } catch (Throwable ignoredRex) {
                LOG.info(ignoredRex.getMessage(), ignoredRex);
            }
        }
    }

    private static void closeSafely(InputStream target) {
        if (target != null) {
            try {
                target.close();
            } catch (IOException ignoredIoe) {
                LOG.info(ignoredIoe.getMessage(), ignoredIoe);
            } catch (Throwable ignoredRex) {
                LOG.info(ignoredRex.getMessage(), ignoredRex);
            }
        }
    }

    private static void closeSafely(OutputStream target) {
        if (target != null) {
            try {
                target.close();
            } catch (IOException ignoredIoe) {
                LOG.info(ignoredIoe.getMessage(), ignoredIoe);
            } catch (Throwable ignoredRex) {
                LOG.info(ignoredRex.getMessage(), ignoredRex);
            }
        }
    }

    private static void closeSafely(Reader target) {
        if (target != null) {
            try {
                target.close();
            } catch (IOException ignoredIoe) {
                LOG.info(ignoredIoe.getMessage(), ignoredIoe);
            } catch (Throwable ignoredRex) {
                LOG.info(ignoredRex.getMessage(), ignoredRex);
            }
        }
    }

    private static void closeSafely(Writer target) {
        if (target != null) {
            try {
                target.close();
            } catch (IOException ignoredIoe) {
                LOG.info(ignoredIoe.getMessage(), ignoredIoe);
            } catch (Throwable ignoredRex) {
                LOG.info(ignoredRex.getMessage(), ignoredRex);
            }
        }
    }

    private void closeSafely(Object target) {
        if (target instanceof RandomAccessFile) {
            closeSafely((RandomAccessFile) target);
        } else if (target instanceof InputStream) {
            closeSafely((InputStream) target);
        } else if (target instanceof OutputStream) {
            closeSafely((OutputStream) target);
        } else if (target instanceof Reader) {
            closeSafely((Reader) target);
        } else if (target instanceof Writer) {
            closeSafely((Writer) target);
        }
    }
    //</editor-fold>

    // @todo JDK7 + can use java.nio.SeekableByteChannel instead of adapters.

    protected static class OutputStreamAdapter extends OutputStream {

        private final RandomAccessFile m_randomAccessFile;

        protected OutputStreamAdapter(final File file,
                                   final long pos)
                                   throws FileNotFoundException, IOException,
                                          IllegalArgumentException,
                                          NullPointerException,
                                          SecurityException {

            if (pos < 0) {
                throw new IllegalArgumentException("pos: " + pos);
            }

            m_randomAccessFile = new RandomAccessFile(file, "rw");

            boolean seekSucceeded = false;

            try {
                m_randomAccessFile.seek(pos);
                seekSucceeded = true;
            } finally {
                if (!seekSucceeded) {
                    closeSafely(m_randomAccessFile);
                }
            }
        }

        protected OutputStreamAdapter(RandomAccessFile randomAccessFile) {
            if (randomAccessFile == null) {
                throw new NullPointerException();
            }
            m_randomAccessFile = randomAccessFile;
        }

        public void write(int b) throws IOException {
            m_randomAccessFile.write(b);
        }

        public void write(byte[] b) throws IOException {
            m_randomAccessFile.write(b);
        }

        public void write(byte[] b, int off, int len) throws IOException {
            m_randomAccessFile.write(b, off, len);
        }

        public void flush() throws IOException {
            if (m_randomAccessFile.getFD().valid()) {
                m_randomAccessFile.getFD().sync();
            }
        }

        public void close() throws IOException {
            m_randomAccessFile.close();
        }
    }

    static class InputStreamAdapter extends InputStream {

        private final CountdownInputStream m_ciStream;

        InputStreamAdapter(final File file, final long pos,
                           final long length)
                           throws FileNotFoundException, IOException,
                                  SecurityException, NullPointerException,
                                  IllegalArgumentException  {

            if (file == null) {
                throw new NullPointerException("file");
            }

            if (pos < 0) {
                throw new IllegalArgumentException("pos: " + pos);
            }

            if (length < 0) {
                throw new IllegalArgumentException("length: " + length);
            }

            FileInputStream fis = null;
            boolean success = false;
            try {
                fis = new FileInputStream(file);

                if (pos > 0) {
                    // skip is a 'native' method and is likely to
                    // be at least as efficient as RandomAccessFile.seek
                    final long actualPos = fis.skip(pos);
                    // Ignoring actualPos because it is 'ok' if
                    // pos > file.length or pos > file.length - length, since
                    // the client code ends up with an 'empty' stream (no data).

                }
                success = true;
            } finally {
                if (!success) {
                    closeSafely(fis);
                }
            }

            final BufferedInputStream  bis = new BufferedInputStream(fis);
            final CountdownInputStream cis = new CountdownInputStream(bis);

            cis.setCount(length);

            m_ciStream = cis;
        }

        public int available() throws IOException {
            return m_ciStream.available();
        }

        public int read() throws IOException {
            return m_ciStream.read();
        }

        public int read(byte[] b) throws IOException {
            return m_ciStream.read(b);
        }

        public int read(byte[] b, int off, int len) throws IOException {
            return m_ciStream.read(b, off, len);
        }

        public long skip(long n) throws IOException {
            return m_ciStream.skip(n);
        }

        public void close() throws IOException {
            m_ciStream.close();
        }
    }
}
