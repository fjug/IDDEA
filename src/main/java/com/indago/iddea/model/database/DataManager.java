package com.indago.iddea.model.database;

import javax.swing.table.DefaultTableModel;
import java.io.File;
import java.sql.*;

/**
 * Created by moon on 09/04/14.
 */
class DataManager {
    String db = "neurites.db";
    Connection c = null;
    Statement stm=null;
    DataManager(){
        try {
            String workingDirectory = System.getProperty("user.dir");
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:" + db);
            stm=c.createStatement();

            File f = new File(workingDirectory + "/" + db);

//            if(!f.exists())
//            {
//                String sql="CREATE TABLE junctions(Id INT PRIMARY KEY, X INT NOT NULL, Y INT NOT NULL,"+
//                        " T INT NOT NULL, SpatialNeighbors TEXT, TemporalNeighbors TEXT)";
//                stm.executeUpdate(sql);
//
//                sql="CREATE TABLE endpoints(Id INT PRIMARY KEY, X INT NOT NULL, Y INT NOT NULL,"+
//                        " T INT NOT NULL, SpatialNeighbors TEXT, TemporalNeighbors TEXT, Junction INT, FOREIGN KEY(Junction) REFERENCES junctions(Id))";
//                stm.executeUpdate(sql);
//            }

        } catch ( Exception e ) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    public void insertJunctionData(int id, int x, int y, int t, String spatialN, String temporalN){
        String sqlinsertion="INSERT INTO junctions(Id, X, Y, T, SpatialNeighbors, TemporalNeighbors) VALUES("+id+","
                +x+","+y+","+t+",'"+spatialN+"','"+temporalN+"')";
        try{
            stm.executeUpdate(sqlinsertion);
        }catch(SQLException se){se.printStackTrace();}
    }

    public void insertEndpointData(int id, int x, int y, int t, String spatialN, String temporalN, int junction){
        String sqlinsertion="INSERT INTO endpoints(Id, X, Y, T, SpatialNeighbors, TemporalNeighbors, Junction) VALUES("+id+","
                +x+","+y+","+t+",'"+spatialN+"','"+temporalN+"'," + junction + ")";
        try{
            stm.executeUpdate(sqlinsertion);
        }catch(SQLException se){se.printStackTrace();}
    }



    public void getData(DefaultTableModel datamodel){
        String sqlselection="SELECT junctions.Id AS Junction, junctions.X AS JX, junctions.Y AS JY, junctions.T AS JT, junctions.SpatialNeighbors AS JSps, junctions.TemporalNeighbors AS JTps, " +
                "endpoints.Id AS Endpoint, endpoints.X AS EX, endpoints.Y AS EY, endpoints.T AS ET, endpoints.SpatialNeighbors AS ESps, endpoints.TemporalNeighbors AS ETps " +
                "FROM junctions LEFT JOIN endpoints ON junctions.Id = endpoints.Junction";
        try{
            ResultSet result=stm.executeQuery(sqlselection);
            if(result!=null){
                while(result.next()){
                    datamodel.addRow(new Object[]{
                            result.getInt("Junction"), result.getInt("JX"), result.getInt("JY"),
                            result.getInt("JT"), result.getString("JSps"), result.getString("JTps"),

                            result.getInt("Endpoint"), result.getInt("EX"), result.getInt("EY"),
                            result.getInt("ET"), result.getString("ESps"), result.getString("ETps")
                    });
                }
            }

        }catch(SQLException se){se.printStackTrace();}

    }

    public void closeConnection() throws SQLException{
        stm.close();
        c.close();
    }
}

