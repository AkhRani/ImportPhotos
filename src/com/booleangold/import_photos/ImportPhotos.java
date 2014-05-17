package com.booleangold.import_photos;

import java.io.*;
import java.util.prefs.Preferences;

import javax.swing.*;

import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class ImportPhotos extends JFrame
                          implements ActionListener, PropertyChangeListener {
    static final long serialVersionUID = 1;
    private static final String SOURCE_KEY = "source_dir_";
    private static final String DEST_KEY = "dest_dir_";
    private static final int RECENT_DIR_COUNT = 4;

    private final JComboBox mSource;
    private final JButton mBrowseSourceButton;
    private final JComboBox mDest;
    private final JButton mBrowseDestButton;
    private final JButton mImportButton;
    private final JButton mCancelButton;
    private final JProgressBar mProgressBar;
    private final Preferences mPrefs;
    private CopyTask mCopyTask;

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

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if ("progress" == evt.getPropertyName()) {
            int progress = (Integer) evt.getNewValue();
            mProgressBar.setValue(progress);
        }
        else if ("fileCount" == evt.getPropertyName()) {
            mProgressBar.setIndeterminate(false);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // TODO Auto-generated method stub

    }

    public static void main(String[] args) {
        ImportPhotos window = new ImportPhotos("Import Photos");

        window.setVisible(true);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
     }
}
