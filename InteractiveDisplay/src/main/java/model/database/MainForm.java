package model.database;

/**
 * Created by moon on 09/04/14.
 */
import java.awt.FlowLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.*;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

class MainForm extends JFrame{
    TableColumnModel colmodel;
    DefaultTableModel model;
    DataManager dm;
    MainForm(){
        //setExtendedState(JFrame.MAXIMIZED_BOTH);
        setTitle("JTable");
        setSize(500,300);
        setLayout(new FlowLayout());
        dm=new DataManager();
        //add sample data to the sqlite database
        addSampleDataToDatabase();
        //create data model for a table component
        model=new DefaultTableModel();
        //add column names to the model
        model.addColumn("ID");
        model.addColumn("Name");
        model.addColumn("Sex");
        model.addColumn("Address");
        model.addColumn("Email");
        model.addColumn("Phone");
        //read sample data from the database and place them in the model
        dm.getData(model);
        //create a table object
        JTable table = new JTable(model);
        JScrollPane scrollpane = new JScrollPane(table);
        add(scrollpane);

        addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent e){
                try {
                    dm.closeConnection();
                } catch (SQLException se) {
                    se.printStackTrace();
                }
                System.exit(0);
            }
        });
        setVisible(true);

    }

    public void addSampleDataToDatabase(){
        dm.insertSampleData(1, "Sok Chan","F","#444,st.933,Phnom Penh","sk.chan@gmail.com","855334543");
        dm.insertSampleData(2, "Thida Vin","F","#23,st.103,Kampot","vin_thida@gmail.com","855135547");
        dm.insertSampleData(3, "Chea Som","M","#476,st.883,Phnom Penh","chea_som@yahoo.com","855988454");

    }

    public static void main(String[] args){
        new MainForm();
    }
}

