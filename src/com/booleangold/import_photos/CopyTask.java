package com.booleangold.import_photos;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.swing.SwingWorker;

import org.apache.commons.io.FileUtils;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;

class CopyTask extends SwingWorker<Void, Void> {
    private static final SimpleDateFormat mYearFormat = new SimpleDateFormat("yyyy");
    private static final SimpleDateFormat mMonthFormat = new SimpleDateFormat("MM");

    private final File mSource;
    private final String mDestRoot;
    private final List<File> mCopiedFiles = new LinkedList<File>();

    CopyTask(File source, String destRoot) {
        mSource = source;
        mDestRoot = destRoot;
    }

    @Override
    protected Void doInBackground() throws Exception {
        try {
            List<File> filesToImport = CollectDirectory(mSource, mDestRoot);
            int total = filesToImport.size();
            firePropertyChange("fileCount", 0, total);

            int count = 0;
            for (File file : filesToImport) {
                if (isCancelled()) {
                    break;
                }
                count++;
                ImportFile(file);
                setProgress(count * 100 / total);
            }
        }
        catch (Exception e) {
            // TODO: Error dialog
            System.out.println("Failed to traverse directories" + e);
        }
        return null;
    }

    List<File> CollectDirectory(File sourceDir, String destRoot) {
        LinkedList<File> result = new LinkedList<File>();
        for (File file : sourceDir.listFiles()) {
            if (file.isDirectory()) {
                result.addAll(CollectDirectory(file, destRoot));
            }
            else {
                System.out.println("File: " + file);
                result.add(file);
            }
        }
        return result;
    }

    void ImportFile(File file) {
        String year = null;
        String month = null;

        try {
            Metadata meta = ImageMetadataReader.readMetadata(file);
            Directory exif = meta.getDirectory(ExifSubIFDDirectory.class);
            Date date = exif.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
            year = mYearFormat.format(date);
            month = mMonthFormat.format(date);
        }
        catch (ImageProcessingException e) {
            if (file.getName().endsWith(".mov") || file.getName().endsWith(".MOV")) {
                year = mYearFormat.format(file.lastModified());
                month = mMonthFormat.format(file.lastModified());
            }
           // TODO
        }
        catch (IOException e) {
            // TODO
        }

        if (year != null && month != null) {
            File yearDir = new File(mDestRoot, year);
            File monthDir = new File(yearDir, month);
            if (!monthDir.exists()) {
                monthDir.mkdirs();
            }
            // TODO:  Copy file to monthDir
            File destFile = new File(monthDir, file.getName());
            if (destFile.exists()) {
                System.out.println("Skipping duplicate file " + destFile.getPath());
            }
            else {
                System.out.println("Preparing to import " + file.getPath() + " to " + destFile.getPath());
                try {
                    FileUtils.copyFile(file, destFile, true);
                    mCopiedFiles.add(destFile);
                }
                catch (Exception e) {
                    // TODO: Error dialog
                    System.out.println("Failed to import file:  " + e);
                }
            }
        }
    }
}