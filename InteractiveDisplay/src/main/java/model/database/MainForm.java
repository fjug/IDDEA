package model.database;

/**
 * Created by moon on 09/04/14.
 */
import java.awt.*;
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
        setTitle("Junctions/Endpoints table");
        setSize(500,300);
        setLayout(new BorderLayout());
        dm=new DataManager();
        //add sample data to the sqlite database
        addSampleDataToDatabase();
        //create data model for a table component
        model=new DefaultTableModel();
        //add column names to the model
        model.addColumn("Junction");
        model.addColumn("JX");
        model.addColumn("JY");
        model.addColumn("JT");
        model.addColumn("JSps");
        model.addColumn("JTps");

        model.addColumn("Endpoint");
        model.addColumn("EX");
        model.addColumn("EY");
        model.addColumn("ET");
        model.addColumn("ESps");
        model.addColumn("ETps");

        //read sample data from the database and place them in the model
        dm.getData(model);
        //create a table object
        JTable table = new JTable(model);
        JScrollPane scrollpane = new JScrollPane();
        scrollpane.setViewportView(table);
        add(scrollpane, java.awt.BorderLayout.CENTER);

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
//        dm.insertJunctionData(1, 200, 300, 0, "2:3", "1");
//        dm.insertJunctionData(2, 130, 302, 0, "1:3", "2");
//        dm.insertJunctionData(3, 160, 276, 0, "1:2", "3");
//
//        dm.insertEndpointData(1, 205, 320, 0, "", "1", 1);
//        dm.insertEndpointData(2, 205, 320, 0, "", "2", 1);
//
//        dm.insertEndpointData(3, 205, 320, 0, "", "3", 2);
//        dm.insertEndpointData(4, 205, 320, 0, "", "4", 3);

//        dm.insertSampleData(1, "Sok Chan","F","#444,st.933,Phnom Penh","sk.chan@gmail.com","855334543");
//        dm.insertSampleData(2, "Thida Vin","F","#23,st.103,Kampot","vin_thida@gmail.com","855135547");
//        dm.insertSampleData(3, "Chea Som","M","#476,st.883,Phnom Penh","chea_som@yahoo.com","855988454");

    }

    public static void main(String[] args){
        new MainForm();
    }
}

