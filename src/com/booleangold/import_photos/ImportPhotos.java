package com.booleangold.import_photos;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.*;

import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.*;
import com.drew.metadata.exif.ExifSubIFDDirectory;

public class ImportPhotos extends JFrame
                          implements ActionListener, PropertyChangeListener {
    static final long serialVersionUID = 1;
    private static final String SOURCE_KEY = "source_dir_";
    private static final String DEST_KEY = "dest_dir_";
    private static final int RECENT_DIR_COUNT = 4;

    private static final SimpleDateFormat mYearFormat = new SimpleDateFormat("yyyy");
    private static final SimpleDateFormat mMonthFormat = new SimpleDateFormat("MM");

    private final JComboBox mSource;
    private final JButton mBrowseSourceButton;
    private final JComboBox mDest;
    private final JButton mBrowseDestButton;
    private final JButton mImportButton;
    private final JButton mCancelButton;
    private final JProgressBar mProgressBar;
    private final Preferences mPrefs;
    private CopyTask mCopyTask;

    class CopyTask extends SwingWorker<Void, Void> {
        private final File mSource;
        private final String mDestRoot;

        CopyTask(File source, String destRoot) {
            mSource = source;
            mDestRoot = destRoot;
        }

        @Override
        protected Void doInBackground() throws Exception {
            try {
                List<File> filesToImport = CollectDirectory(mSource, mDestRoot);
                // System.out.println("Importing files: " + filesToImport.size());
                firePropertyChange("fileCount", 0, filesToImport.size());
            }
            catch (Exception e) {
                // TODO: Error dialog
                System.out.println("Failed to traverse directories");
            }
            return null;
        }
    }

    ImportPhotos(String title) {
        setTitle(title);

        // Create Components
        mPrefs = Preferences.userNodeForPackage(ImportPhotos.class);

        mSource = MakeComboBox("E:\\DCIM\\", SOURCE_KEY);
        JLabel sourceLabel = new JLabel("Import pictures from: ");
        mBrowseSourceButton = new JButton("Browse");
        mBrowseSourceButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ChooseDirectory(mSource);
                SaveComboBox(mSource, SOURCE_KEY);
            }
        });


        JLabel destLabel = new JLabel("Copy pictures to: ");
        mDest = MakeComboBox("D:\\Users\\Beth\\Documents\\Pictures", DEST_KEY);
        mBrowseDestButton = new JButton("Browse");
        mBrowseDestButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ChooseDirectory(mDest);
                SaveComboBox(mDest, DEST_KEY);
            }
        });

        mImportButton = new JButton("Import");
        mImportButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Import();
            }
        });

        mCancelButton = new JButton("Cancel");
        mCancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ImportPhotos.this.dispose();
            }
        });

        mProgressBar = new JProgressBar(0, 100);

        //Create a layout and add components to it.
        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);

        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(sourceLabel)
                        .addComponent(destLabel))
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                        .addComponent(mSource)
                        .addComponent(mDest))
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                        .addComponent(mBrowseSourceButton)
                        .addComponent(mBrowseDestButton))
                    )
                .addComponent(mProgressBar)
                .addGroup(layout.createSequentialGroup()
                    .addComponent(mImportButton)
                    .addComponent(mCancelButton))
            );

        layout.setVerticalGroup(
            layout.createSequentialGroup()
                .addGroup(layout.createSequentialGroup()
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(sourceLabel)
                        .addComponent(mSource)
                        .addComponent(mBrowseSourceButton))
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(destLabel)
                        .addComponent(mDest)
                        .addComponent(mBrowseDestButton))
                     )
                 .addComponent(mProgressBar)
                 .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                         .addComponent(mImportButton)
                         .addComponent(mCancelButton, GroupLayout.Alignment.TRAILING))
            );

        this.pack();
        this.setResizable(false);
    }

    JComboBox MakeComboBox(String defaultItem, String base_key) {
        JComboBox retval = new JComboBox();

        for (int i = 0; i < RECENT_DIR_COUNT; i++) {
            String key = base_key + Integer.toString(i);
            String dir = mPrefs.get(key, null);
            if (dir != null) {
                retval.addItem(dir);
            }
        }
        if (retval.getItemCount() == 0) {
            retval.addItem(defaultItem);
        }
        return retval;
    }

    void SaveComboBox(JComboBox cb, String base_key) {
        int itemCount = cb.getItemCount();
        for (int i = 0; i < RECENT_DIR_COUNT && i < itemCount; i++) {
            mPrefs.put(base_key + Integer.toString(i), (String) cb.getItemAt(i));
        }
    }

    void ChooseDirectory(JComboBox comboBox) {
        JFileChooser chooser;
        // Start at currently selected directory, if possible
        try {
            chooser = new JFileChooser((String) comboBox.getSelectedItem());
        }
        catch (Exception e) {
            chooser = new JFileChooser();
        }

        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnVal = chooser.showOpenDialog(this);
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            String path = chooser.getSelectedFile().getAbsolutePath();
            comboBox.insertItemAt(path, 0);
            comboBox.setSelectedItem(path);
        }
    }

    void Import()
    {
        String destRoot;
        File sourceDir;
        try {
            String sourceRoot = (String) mSource.getSelectedItem();
            sourceDir = new File(sourceRoot);
            System.out.println("Import From: " + sourceRoot);

            destRoot = (String) mDest.getSelectedItem();
            System.out.println("Import To: " + destRoot);
        }
        catch (Exception e) {
            // TODO:  Error dialog
            System.out.println("Failed to convert items to Strings");
            return;
        }
        mProgressBar.setIndeterminate(true);
        mCopyTask = new CopyTask(sourceDir, destRoot);
        mCopyTask.addPropertyChangeListener(this);
        mCopyTask.execute();
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

    File CollectFile(File file, String destRoot) {
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
            File yearDir = new File(destRoot, year);
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
                // return new File(file, destFile);
            }
        }
        return null;
    }

    /*
     *
                InputStream is = null;
                OutputStream os = null;
                try {
                    is = new FileInputStream(file);
                    os = new FileOutputStream(destFile);
                    int length;
                    while ((length = is.read(mBuffer)) > 0) {
                        os.write(mBuffer, 0, length);
                    }
                    System.out.println("Successfully copied " + file.getPath() + " to " + destFile.getPath());
                } catch (FileNotFoundException e) {
                    System.out.println("Failed to copy " + file.getPath() + " to " + destFile.getPath());
                } catch (IOException e) {
                    System.out.println("Failed to copy " + file.getPath() + " to " + destFile.getPath());
                } finally {
                    try {
                        is.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    try {
                        os.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }

     */
    public static void main(String[] args) {
       ImportPhotos window = new ImportPhotos("Import Photos");

       window.setVisible(true);
       window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if ("progress" == evt.getPropertyName()) {
            int progress = (Integer) evt.getNewValue();
            mProgressBar.setValue(progress);
        }
        else if ("fileCount" == evt.getPropertyName()) {
            int count = (Integer) evt.getNewValue();
            System.out.println("Importing files: " + count);
            mProgressBar.setIndeterminate(false);
            mProgressBar.setMaximum(count);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // TODO Auto-generated method stub

    }
}
