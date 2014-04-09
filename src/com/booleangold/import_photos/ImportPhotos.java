package com.booleangold.import_photos;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.*;

import java.awt.event.*;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.*;
import com.drew.metadata.exif.ExifIFD0Directory;

public class ImportPhotos extends JFrame {
    private final JComboBox mSource;
    private final JButton mBrowseSourceButton;
    private final JComboBox mDest;
    private final JButton mBrowseDestButton;
    private final JButton mImportButton;
    private final JButton mCancelButton;
    private static final String SOURCE_KEY = "source_dir_";
    private static final String DEST_KEY = "source_dir_";

    static final long serialVersionUID = 1;

    ImportPhotos(String title) {
        setTitle(title);

        // Create Components
        Preferences prefs = Preferences.userNodeForPackage(ImportPhotos.class);

        mSource = MakeComboBox(prefs, "E:\\DCIM\\", SOURCE_KEY);
        JLabel sourceLabel = new JLabel("Import pictures from: ");
        mBrowseSourceButton = new JButton("Browse");
        mBrowseSourceButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ChooseDirectory(mSource);
            }
        });


        JLabel destLabel = new JLabel("Copy pictures to: ");
        mDest = MakeComboBox(prefs, "D:\\Users\\Beth\\Documents\\Pictures", DEST_KEY);
        mBrowseDestButton = new JButton("Browse");
        mBrowseDestButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ChooseDirectory(mDest);
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

        //Create a layout and add components to it.
        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);

        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        layout.setHorizontalGroup(
            layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(sourceLabel)
                    .addComponent(destLabel))
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                    .addComponent(mSource)
                    .addComponent(mDest)
                    .addComponent(mImportButton))
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                    .addComponent(mBrowseSourceButton)
                    .addComponent(mBrowseDestButton)
                    .addComponent(mCancelButton))
                    );

        layout.setVerticalGroup(
            layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(sourceLabel)
                    .addComponent(mSource)
                    .addComponent(mBrowseSourceButton))
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(destLabel)
                    .addComponent(mDest)
                    .addComponent(mBrowseDestButton))
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(mImportButton)
                    .addComponent(mCancelButton))
                    );

        this.pack();
        this.setResizable(false);
    }

    JComboBox MakeComboBox(Preferences prefs, String defaultItem, String base_key) {
        JComboBox retval = new JComboBox();

        try {
            String[] keys = prefs.keys();
            for (String key : keys) {
                retval.addItem(prefs.get(key, "Missing Preference"));
            }
        }
        catch (BackingStoreException e) {
            // TODO
        }
        if (retval.getItemCount() == 0) {
            retval.addItem(defaultItem);
        }
        return retval;
    }

    void SaveComboBox(JComboBox cb, Preferences prefs, String base_key) {
        int itemCount = cb.getItemCount();
        for (int i = 0; i < itemCount; i++) {
            prefs.put(base_key + Integer.toString(i), (String) cb.getItemAt(i));
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
            comboBox.addItem(path);
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
        try {
            ImportDirectory(sourceDir, destRoot);
        }
        catch (Exception e) {
            // TODO: Error dialog
            System.out.println("Failed to traverse directories");
        }
    }

    void ImportDirectory(File sourceDir, String destRoot) {
        for (File file : sourceDir.listFiles()) {
            if (file.isDirectory()) {
                ImportDirectory(file, destRoot);
            }
            else {
                ImportFile(file);
            }
        }
    }

    void ImportFile(File file) {
        try {
            Metadata meta = ImageMetadataReader.readMetadata(file);
            Directory exif = meta.getDirectory(ExifIFD0Directory.class);
            Date date = exif.getDate(ExifIFD0Directory.TAG_DATETIME);
            System.out.println("Hello " + file.getAbsolutePath() + " " + date.toString());
        }
        catch (ImageProcessingException e) {
            if (file.getName().endsWith(".mov") || file.getName().endsWith(".MOV")) {
                SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
                System.out.println("Hello " + file.getAbsolutePath() + " " + sdf.format(file.lastModified()));
            }
           // TODO
        }
        catch (IOException e) {
            // TODO
        }
    }

    public static void main(String[] args) {
       ImportPhotos window = new ImportPhotos("Import Photos");

       window.setVisible(true);
       window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}
