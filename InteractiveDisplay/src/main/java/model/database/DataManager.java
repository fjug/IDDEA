package model.database;

import javax.swing.table.DefaultTableModel;
import java.sql.*;

/**
 * Created by moon on 09/04/14.
 */
class DataManager {
    Connection c = null;
    Statement stm=null;
    DataManager(){
        try {

            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:sales.db");
            stm=c.createStatement();
            String sql="CREATE TABLE tblcustomers(Id INT PRIMARY KEY, Name TEXT NOT NULL, Sex TEXT NOT NULL,"+
                    " Address TEXT NOT NULL, Email TEXT NOT NULL, Phone TEXT NOT NULL)";
            stm.executeUpdate(sql);

        } catch ( Exception e ) {
            e.printStackTrace();
            System.exit(0);
        }
    }
    public void insertSampleData(int id,String name, String sex, String address,String email, String phone){
        String sqlinsertion="INSERT INTO tblcustomers(Id,Name,Sex,Address,Email,Phone) VALUES("+id+",'"
                +sex+"','"+name+"','"+address+"','"+email+"','"+phone+"')";
        try{
            stm.executeUpdate(sqlinsertion);
        }catch(SQLException se){se.printStackTrace();}
    }

    public void getData(DefaultTableModel datamodel){
        String sqlselection="SELECT * FROM tblcustomers";
        try{
            ResultSet result=stm.executeQuery(sqlselection);
            if(result!=null){
                while(result.next()){
                    datamodel.addRow(new Object[]{result.getInt("Id"),result.getString("Name"),result.getString("Sex"),result.getString("Address"),result.getString("Email"),result.getString("Phone")});
                }
            }

        }catch(SQLException se){se.printStackTrace();}

    }

    public void closeConnection() throws SQLException{
        stm.close();
        c.close();
    }
}

