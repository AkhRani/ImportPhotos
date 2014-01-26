package com.booleangold.import_photos;

import javax.swing.*; 

public class ImportPhotos extends JFrame {
    private JComboBox mSource;
    private JButton mBrowseSourceButton;
    private JComboBox mDest;
    private JButton mBrowseDestButton;
    private JButton mImportButton;
    private JButton mCancelButton;
    
    static final long serialVersionUID = 1;

    ImportPhotos(String title) { 
        setTitle(title); 
        
        //Create a pane and add components to it.
        JLabel sourceLabel = new JLabel("Import pictures from: ");
        mSource = new JComboBox();
        mSource.setPrototypeDisplayValue("E:\\DCIM\\");
        mBrowseSourceButton = new JButton("Browse");
        
        JLabel destLabel = new JLabel("Copy pictures to: ");
        mDest = new JComboBox();
        mDest.setPrototypeDisplayValue("D:\\Users\\Beth\\Documents\\Pictures");
        mBrowseDestButton = new JButton("Browse");
        
        mImportButton = new JButton("Import");
        mCancelButton = new JButton("Cancel");
        
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

    public static void main(String[] args) {
       ImportPhotos window = new ImportPhotos("Import Photos");

       window.setVisible(true);
       window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
    }
}
