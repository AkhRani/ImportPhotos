package com.booleangold.import_photos;

import java.io.*;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.InputMismatchException;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JTextArea;
import javax.swing.SwingWorker;

import org.apache.commons.io.FileUtils;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;

class CopyTask extends SwingWorker<Void, String> {
    private static final SimpleDateFormat mYearFormat = new SimpleDateFormat("yyyy");
    private static final SimpleDateFormat mMonthFormat = new SimpleDateFormat("MM");
    private static final SimpleDateFormat mIditFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy");

    private static final int LIST_CC = 0x5453494c;
    private static final int AVIH_CC = 0x68697661;
    private static final int IDIT_CC = 0x54494449;

    private final File mSource;
    private final String mDestRoot;
    private final List<File> mCopiedFiles = new LinkedList<File>();

    private final JTextArea mMessages;
    private int mTotalCount = 0;
    private int mCopyCount = 0;
    private int mDupCount = 0;
    private int mFailCount = 0;

    CopyTask(JTextArea messages, File source, String destRoot) {
        mMessages = messages;
        mSource = source;
        mDestRoot = destRoot;
    }

    @Override
    protected void process(List<String> chunks) {
       for (String message : chunks) {
           mMessages.append(message + "\n");
       }
    }

    @Override
    protected Void doInBackground() throws Exception {
        try {
            List<File> filesToImport = CollectDirectory(mSource, mDestRoot);
            mTotalCount = filesToImport.size();
            publish("Found " + mTotalCount + " files.");
            firePropertyChange("fileCount", 0, mTotalCount);

            int count = 0;
            for (File file : filesToImport) {
                if (isCancelled()) {
                    break;
                }
                count++;
                ImportFile(file);
                setProgress(count * 100 / mTotalCount);
            }
        }
        catch (Exception e) {
            // TODO: Error dialog
            publish("Failure: " + e);
        }
        publish("Copied " + mCopyCount + " files.");
        publish("Skipped " + mDupCount + " Duplicates.");
        publish("Encountered " + mFailCount + " Failures.");
        return null;
    }

    List<File> CollectDirectory(File sourceDir, String destRoot) {
        LinkedList<File> result = new LinkedList<File>();
        for (File file : sourceDir.listFiles()) {
            if (file.isDirectory()) {
                result.addAll(CollectDirectory(file, destRoot));
            }
            else {
                // System.out.println("File: " + file);
                result.add(file);
            }
        }
        return result;
    }

    void CopyFile(File file, String year, String month) {
        File yearDir = new File(mDestRoot, year);
        File monthDir = new File(yearDir, month);
        if (!monthDir.exists()) {
            monthDir.mkdirs();
        }
        // TODO:  Copy file to monthDir
        File destFile = new File(monthDir, file.getName());
        if (destFile.exists()) {
            System.out.println("Skipping duplicate file " + destFile.getPath());
            mDupCount++;
        }
        else {
            publish("Copying " + file.getPath() + " to " + destFile.getPath());
            try {
                FileUtils.copyFile(file, destFile, true);
                mCopiedFiles.add(destFile);
                mCopyCount++;
            }
            catch (Exception e) {
                publish("Failed to import file:  " + e);
                mFailCount++;
            }
        }
    }

    void ImportFile(File file) {
        if (file.getName().endsWith(".avi") || file.getName().endsWith(".AVI")) {
            ImportAviFile(file);
        }
        else if (file.getName().endsWith(".mov") || file.getName().endsWith(".MOV")) {
            ImportMovFile(file);
        }
        else {
            try {
                Metadata meta = ImageMetadataReader.readMetadata(file);
                Directory exif = meta.getDirectory(ExifSubIFDDirectory.class);
                Date date = exif.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
                String year = mYearFormat.format(date);
                String month = mMonthFormat.format(date);
                CopyFile(file, year, month);
            }
            catch (ImageProcessingException e) {
                publish("Image Failure reading " + file + ": " + e);
                mFailCount++;
            }
            catch (IOException e) {
                publish("IO Failure reading " + file + ": " + e);
                mFailCount++;
            }
            finally {

            }
        }
    }


    void ImportMovFile(File file) {
        // TODO
        String year = mYearFormat.format(file.lastModified());
        String month = mMonthFormat.format(file.lastModified());
        CopyFile(file, year, month);
    }

    void ImportAviFile(File file) {
        ByteBuffer buff = ByteBuffer.allocate(1024);
        DataInputStream is;
        try {
            is = new DataInputStream(new FileInputStream(file));
            is.read(buff.array());
            // In L.E. the strings (fourcc codes) will be backwards
            // but the lengths will be correct.
            buff.order(java.nio.ByteOrder.LITTLE_ENDIAN);
            int riff = buff.getInt();
            buff.position(8);
            int avi = buff.getInt();
            int list = buff.getInt();
            if (riff == 0x46464952 &&
                    avi == 0x20495641 &&
                    list == LIST_CC) {
                // Skip List length
                buff.position(20);
                SkipHdrl(buff);
                SkipFourCC(buff, LIST_CC);
                SkipFourCC(buff, LIST_CC);
                String dateString = ReadFourCC(buff, IDIT_CC).trim();
                Date date = mIditFormat.parse(dateString);
                // System.out.println("AVI: " + date );
                String year = mYearFormat.format(date);
                String month = mMonthFormat.format(date);
                CopyFile(file, year, month);
            }
            else {
                publish("Unrecognized AVI File: " + file);
                mFailCount++;
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            publish("Error Reading AVI file: " + e);
            e.printStackTrace();
            mFailCount++;
        }
    }

    // Buff is in little-endian mode
    void SkipHdrl(ByteBuffer buff) {
        int hdrl = buff.getInt();
        if (hdrl != 0x6c726468) {
            throw new InputMismatchException("Bad hdr1: " + Integer.toHexString(hdrl));
        }
        SkipFourCC(buff, AVIH_CC);
    }

    // Buff is in little-endian mode
    void SkipFourCC(ByteBuffer buff, int expected_cc) {
        int cc = buff.getInt();
        int len = buff.getInt();
        if (cc != expected_cc || len < 0) {
            throw new InputMismatchException("Bad CC.  Expected " + expected_cc + " Actual " + cc);
        }
        int newPos = buff.position() + len;
        buff.position(newPos);
    }

    // Buff is in little-endian mode
    String ReadFourCC(ByteBuffer buff, int expected_cc) {
        int cc = buff.getInt();
        int len = buff.getInt();
        if (cc != expected_cc || len < 0) {
            throw new InputMismatchException("Bad CC.  Expected " + expected_cc + " Actual " + cc);
        }
        byte stringBytes[] = new byte[len];
        buff.get(stringBytes);
        return new String(stringBytes);
    }
}