package com.booleangold.import_photos;

import java.io.*;
import javax.swing.*;

import java.awt.event.*;

public class ImportPhotos extends JFrame {
    private final JComboBox mSource;
    private final JButton mBrowseSourceButton;
    private final JComboBox mDest;
    private final JButton mBrowseDestButton;
    private final JButton mImportButton;
    private final JButton mCancelButton;

    static final long serialVersionUID = 1;

    ImportPhotos(String title) {
        setTitle(title);

        //Create a pane and add components to it.
        JLabel sourceLabel = new JLabel("Import pictures from: ");
        mSource = new JComboBox();
        mSource.setPrototypeDisplayValue("E:\\DCIM\\");
        mSource.addItem("E:\\DCIM\\");
        mBrowseSourceButton = new JButton("Browse");
        mBrowseSourceButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ChooseDirectory(mSource);
            }
        });

        JLabel destLabel = new JLabel("Copy pictures to: ");
        mDest = new JComboBox();
        mDest.setPrototypeDisplayValue("D:\\Users\\Beth\\Documents\\Pictures");
        mDest.addItem("D:\\Users\\Beth\\Documents\\Pictures");
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

    void ChooseDirectory(JComboBox comboBox) {
        JFileChooser chooser;
        // Start at currently selected directory, if possible
        try {
            chooser = new JFileChooser((String) comboBox.getItemAt(0));
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
            RecursiveImport(sourceDir, destRoot);
        }
        catch (Exception e) {
            // TODO: Error dialog
            System.out.println("Failed to traverse directories");
        }
    }

    void RecursiveImport(File sourceDir, String destRoot) {
        for (File file : sourceDir.listFiles()) {
            System.out.println("Hello " + file.getAbsolutePath());
            if (file.isDirectory()) {
                RecursiveImport(file, destRoot);
            }
            else {
            }
        }
    }

    public static void main(String[] args) {
       ImportPhotos window = new ImportPhotos("Import Photos");

       window.setVisible(true);
       window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}
