package ru.kmorozov.library.data.loader.utils;

import java.io.*;
import java.nio.charset.Charset;
import java.text.ParseException;

/**
 * Represents a Windows shortcut (typically visible to Java only as a '.lnk' file).
 * <p>
 * Retrieved 2011-09-23 from http://stackoverflow.com/questions/309495/windows-shortcut-lnk-parser-in-java/672775#672775
 * Originally called LnkParser
 * <p>
 * Written by: (the stack overflow users, obviously!)
 * Apache Commons VFS dependency removed by crysxd (why were we using that!?) https://github.com/crysxd
 * Headerified, refactored and commented by Code Bling http://stackoverflow.com/users/675721/code-bling
 * Network file support added by Stefan Cordes http://stackoverflow.com/users/81330/stefan-cordes
 * Adapted by Sam Brightman http://stackoverflow.com/users/2492/sam-brightman
 * Based on information in 'The Windows Shortcut File Format' by Jesse Hager &lt;jessehager@iname.com&gt;
 * And somewhat based on code from the book 'Swing Hacks: Tips and Tools for Killer GUIs'
 * by Joshua Marinacci and Chris Adamson
 * ISBN: 0-596-00907-0
 * http://www.oreilly.com/catalog/swinghks/
 */
public class WindowsShortcut {
    private boolean isDirectory;
    private boolean isLocal;
    private String real_file;
    private final Charset charset;

    /**
     * Provides a quick test to see if this could be a valid link !
     * If you try to instantiate a new WindowShortcut and the link is not valid,
     * Exceptions may be thrown and Exceptions are extremely slow to generate,
     * therefore any code needing to loop through several files should first check this.
     *
     * @param file the potential link
     * @return true if may be a link, false otherwise
     * @throws IOException if an IOException is thrown while reading from the file
     */
    public static boolean isPotentialValidLink(final File file) throws IOException {
        final int minimum_length = 0x64;
        final boolean isPotentiallyValid;
        try (InputStream fis = new FileInputStream(file)) {
            isPotentiallyValid = file.isFile()
                    && file.getName().toLowerCase().endsWith(".lnk")
                    && minimum_length <= fis.available()
                    && isMagicPresent(getBytes(fis, 32));
        }
        return isPotentiallyValid;
    }

    public WindowsShortcut(final File file, final Charset charset) throws IOException, ParseException {
        this.charset = charset;

        try (InputStream in = new FileInputStream(file)) {
            parseLink(getBytes(in));
        }
    }

    /**
     * @return the name of the filesystem object pointed to by this shortcut
     */
    public String getRealFilename() {
        return real_file;
    }

    /**
     * Tests if the shortcut points to a local resource.
     *
     * @return true if the 'local' bit is set in this shortcut, false otherwise
     */
    public boolean isLocal() {
        return isLocal;
    }

    /**
     * Tests if the shortcut points to a directory.
     *
     * @return true if the 'directory' bit is set in this shortcut, false otherwise
     */
    public boolean isDirectory() {
        return isDirectory;
    }

    /**
     * Gets all the bytes from an InputStream
     *
     * @param in the InputStream from which to read bytes
     * @return array of all the bytes contained in 'in'
     * @throws IOException if an IOException is encountered while reading the data from the InputStream
     */
    private static byte[] getBytes(final InputStream in) throws IOException {
        return getBytes(in, null);
    }

    /**
     * Gets up to max bytes from an InputStream
     *
     * @param in  the InputStream from which to read bytes
     * @param max maximum number of bytes to read
     * @return array of all the bytes contained in 'in'
     * @throws IOException if an IOException is encountered while reading the data from the InputStream
     */
    private static byte[] getBytes(final InputStream in, Integer max) throws IOException {
        // read the entire file into a byte buffer
        try(ByteArrayOutputStream bout = new ByteArrayOutputStream()) {
            final byte[] buff = new byte[256];
            while (null == max || 0 < max) {
                final int n = in.read(buff);
                if (-1 == n) {
                    break;
                }
                bout.write(buff, 0, n);
                if (null != max)
                    max -= n;
            }
            in.close();
            return bout.toByteArray();
        }
    }

    private static boolean isMagicPresent(final byte[] link) {
        final int magic = 0x0000004C;
        final int magic_offset = 0x00;
        return 32 <= link.length && magic == bytesToDword(link, magic_offset);
    }

    /**
     * Gobbles up link data by parsing it and storing info in member fields
     *
     * @param link all the bytes from the .lnk file
     */
    private void parseLink(final byte[] link) throws ParseException {
        try {
            if (!isMagicPresent(link))
                throw new ParseException("Invalid shortcut; magic is missing", 0);

            // get the flags byte
            final byte flags = link[0x14];

            // get the file attributes byte
            final int file_atts_offset = 0x18;
            final byte file_atts = link[file_atts_offset];
            final byte is_dir_mask = (byte) 0x10;
            isDirectory = 0 < (file_atts & is_dir_mask);

            // if the shell settings are present, skip them
            final int shell_offset = 0x4c;
            final byte has_shell_mask = (byte) 0x01;
            int shell_len = 0;
            if (0 < (flags & has_shell_mask)) {
                // the plus 2 accounts for the length marker itself
                shell_len = bytesToWord(link, shell_offset) + 2;
            }

            // get to the file settings
            final int file_start = 0x4c + shell_len;

            final int file_location_info_flag_offset_offset = 0x08;
            final int file_location_info_flag = link[file_start + file_location_info_flag_offset_offset];
            isLocal = 0 == (file_location_info_flag & 2);
            // get the local volume and local system values
            //final int localVolumeTable_offset_offset = 0x0C;
            final int basename_offset_offset = 0x10;
            final int networkVolumeTable_offset_offset = 0x14;
            final int finalname_offset_offset = 0x18;
            final int finalname_offset = link[file_start + finalname_offset_offset] + file_start;
            final String finalname = getNullDelimitedString(link, finalname_offset);
            if (isLocal) {
                final int basename_offset = link[file_start + basename_offset_offset] + file_start;
                final String basename = getNullDelimitedString(link, basename_offset);
                real_file = basename + finalname;
            } else {
                final int networkVolumeTable_offset = link[file_start + networkVolumeTable_offset_offset] + file_start;
                final int shareName_offset_offset = 0x08;
                final int shareName_offset = link[networkVolumeTable_offset + shareName_offset_offset]
                        + networkVolumeTable_offset;
                final String shareName = getNullDelimitedString(link, shareName_offset);
                real_file = shareName + '\\' + finalname;
            }
        } catch (final ArrayIndexOutOfBoundsException e) {
            throw new ParseException("Could not be parsed, probably not a valid WindowsShortcut", 0);
        }
    }

    private String getNullDelimitedString(final byte[] bytes, final int off) {
        int len = 0;
        // count bytes until the null character (0)
        while (true) {
            if (0 == bytes[off + len]) {
                break;
            }
            len++;
        }
        return new String(bytes, off, len, charset);
    }

    /*
     * convert two bytes into a short note, this is little endian because it's
     * for an Intel only OS.
     */
    private static int bytesToWord(final byte[] bytes, final int off) {
        return ((bytes[off + 1] & 0xff) << 8) | (bytes[off] & 0xff);
    }

    private static int bytesToDword(final byte[] bytes, final int off) {
        return (bytesToWord(bytes, off + 2) << 16) | bytesToWord(bytes, off);
    }

}
