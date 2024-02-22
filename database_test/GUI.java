import java.sql.*;
import java.awt.event.*;
import javax.swing.*;

/*
  TODO:
  1) Change credentials for your own team's database
  2) Change SQL command to a relevant query that retrieves a small amount of data
  3) Create a JTextArea object using the queried data
  4) Add the new object to the JPanel p
*/

public class GUI extends JFrame implements ActionListener {
    static JFrame f;

    public static void main(String[] args)
    {
      //Building the connection
      Connection conn = null;
      //TODO STEP 1 (see line 7)
      String database_name = "csce315_902_02_db";
      String database_user = "csce315_902_02_user";
      String database_password = "password123";
      String database_url = String.format("jdbc:postgresql://csce-315-db.engr.tamu.edu/%s", database_name);
      try {
        conn = DriverManager.getConnection(database_url, database_user, database_password);
      } catch (Exception e) {
        e.printStackTrace();
        System.err.println(e.getClass().getName()+": "+e.getMessage());
        System.exit(0);
      }
      JOptionPane.showMessageDialog(null,"Opened database successfully");

      String name = "";
      try{
        //create a statement object
        Statement stmt = conn.createStatement();
        //create a SQL statement
        //TODO Step 2 (see line 8)
        String sqlStatement = "SELECT * FROM menu_item;";
        //send statement to DBMS
        ResultSet result = stmt.executeQuery(sqlStatement);
        while (result.next()) {
          // TODO you probably need to change the column name tat you are retrieving
          //      this command gets the data from the "name" attribute
          name += result.getString("name")+ " - " + result.getString("price")+ "\n";
        }
      } catch (Exception e){
        JOptionPane.showMessageDialog(null,"Error accessing Database.");
      }
      // create a new frame
      f = new JFrame("DB GUI");

      // create a object
      GUI s = new GUI();

      // create a panel
      JPanel p = new JPanel();

      JButton b = new JButton("Close");

      // add actionlistener to button
      b.addActionListener(s);

      //TODO Step 3 (see line 9)
      JTextArea hold = new JTextArea();
      hold.setText(name);


      //TODO Step 4 (see line 10)
      // 4) Add the new object to the JPanel p
      p.add(hold);

      // add button to panel
      p.add(b);

      // add panel to frame
      f.add(p);

      // set the size of frame
      f.setSize(400, 400);

      f.setVisible(true);

      //closing the connection
      try {
        conn.close();
        JOptionPane.showMessageDialog(null,"Connection Closed.");
      } catch(Exception e) {
        JOptionPane.showMessageDialog(null,"Connection NOT Closed.");
      }
    }

    // if button is pressed
    public void actionPerformed(ActionEvent e)
    {
        String s = e.getActionCommand();
        if (s.equals("Close")) {
            f.dispose();
        }
    }
}
