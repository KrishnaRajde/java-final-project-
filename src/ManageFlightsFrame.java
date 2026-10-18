import java.awt.*;
import java.awt.event.*;
import java.sql.*;

// view all flights, search, add, update and delete flights
public class ManageFlightsFrame extends Frame implements ActionListener, WindowListener
{
    Label l1, l2, l3;
    TextField searchTf;
    List flightList;
    Button searchBtn, showAllBtn, addBtn, updateBtn, deleteBtn, backBtn;
    int[] flightIds = new int[500]; // id of every row in the list
    int total = 0;                  // number of rows in the list

    public ManageFlightsFrame()
    {
        // setting the layout (no layout manager, every component gets x, y, width, height)
        setLayout(null);

        // creating objects
        l1 = new Label("MANAGE FLIGHTS", Label.CENTER);
        l2 = new Label("Flight No / ID");
        l3 = new Label(String.format(" %-4s %-7s %-10s %-22s %-17s %-11s %s", "ID", "Flight", "Airline", "Route", "Departure", "Price(Rs)", "Seats Left"));
        searchTf = new TextField(10);
        flightList = new List(14);
        searchBtn = new Button("Search");
        showAllBtn = new Button("List All");
        addBtn = new Button("Add Flight");
        updateBtn = new Button("Update Selected");
        deleteBtn = new Button("Delete Selected");
        backBtn = new Button("Back");

        // design
        setBackground(new Color(232, 240, 250));
        setFont(new Font("Arial", Font.PLAIN, 15));
        l1.setBackground(new Color(22, 54, 110));
        l1.setForeground(Color.white);
        l1.setFont(new Font("Arial", Font.BOLD, 22));
        l3.setFont(new Font("Monospaced", Font.BOLD, 14));
        flightList.setFont(new Font("Monospaced", Font.PLAIN, 14));

        // event handling
        searchBtn.addActionListener(this);
        showAllBtn.addActionListener(this);
        addBtn.addActionListener(this);
        updateBtn.addActionListener(this);
        deleteBtn.addActionListener(this);
        backBtn.addActionListener(this);

        // adding components (x, y, width, height)
        l1.setBounds(0, 30, 900, 50);
        l2.setBounds(20, 95, 120, 30);
        searchTf.setBounds(145, 95, 150, 30);
        searchBtn.setBounds(310, 95, 120, 30);
        showAllBtn.setBounds(440, 95, 120, 30);
        addBtn.setBounds(20, 135, 160, 30);
        updateBtn.setBounds(190, 135, 160, 30);
        deleteBtn.setBounds(360, 135, 160, 30);
        backBtn.setBounds(530, 135, 120, 30);
        l3.setBounds(20, 180, 860, 25);
        flightList.setBounds(20, 205, 860, 375);
        add(l1);
        add(l2);
        add(searchTf);
        add(searchBtn);
        add(showAllBtn);
        add(addBtn);
        add(updateBtn);
        add(deleteBtn);
        add(backBtn);
        add(l3);
        add(flightList);

        // closing the window
        addWindowListener(this);

        // creating the frame
        setTitle("Manage Flights");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setVisible(true);

        loadFlights("");
    }

    // shows flights in the list, if search is empty it shows all flights
    public void loadFlights(String search)
    {
        flightList.removeAll();
        total = 0;
        try
        {
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM flights WHERE ? = '' OR flight_no = ? OR flight_id = ? ORDER BY departure_time");
            ps.setString(1, search);
            ps.setString(2, search);
            ps.setString(3, search);
            ResultSet rs = ps.executeQuery();
            while (rs.next() && total < 500)
            {
                flightIds[total] = rs.getInt("flight_id");
                total++;
                flightList.add(String.format("%-4d %-7s %-10s %-22s %-17s %-11.2f %d/%d", rs.getInt("flight_id"), rs.getString("flight_no"),
                        rs.getString("airline"), rs.getString("source") + " -> " + rs.getString("destination"),
                        rs.getString("departure_time").substring(0, 16), rs.getDouble("price"), rs.getInt("available_seats"), rs.getInt("total_seats")));
            }
            con.close();
            if (total == 0)
                new MessageBox(this, "No flight found");
        }
        catch (Exception e)
        {
            System.out.println(e);
            new MessageBox(this, "Error: " + e.getMessage());
        }
    }

    // event handling method
    public void actionPerformed(ActionEvent e)
    {
        Button btn = (Button) e.getSource();
        int i = flightList.getSelectedIndex();

        if (btn == backBtn)
            dispose();
        else if (btn == searchBtn)
            loadFlights(searchTf.getText().trim().toUpperCase());
        else if (btn == showAllBtn)
        {
            searchTf.setText("");
            loadFlights("");
        }
        else if (btn == addBtn)
            new FlightFormFrame(this, 0); // 0 means new flight
        else if (i < 0)
            new MessageBox(this, "Please select a flight from the list first");
        else if (btn == updateBtn)
            new FlightFormFrame(this, flightIds[i]);
        else if (new MessageBox(this, "Delete flight ID " + flightIds[i] + "?", true).yes)
        {
            try
            {
                // deletes only if there are no confirmed bookings (seats are deleted by ON DELETE CASCADE)
                Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement("DELETE FROM flights WHERE flight_id = ? AND NOT EXISTS "
                        + "(SELECT * FROM bookings WHERE flight_id = ? AND status = 'CONFIRMED')");
                ps.setInt(1, flightIds[i]);
                ps.setInt(2, flightIds[i]);
                if (ps.executeUpdate() > 0)
                    new MessageBox(this, "Flight deleted successfully");
                else
                    new MessageBox(this, "Cannot delete. This flight has active bookings");
                con.close();
                loadFlights("");
            }
            catch (Exception ex)
            {
                System.out.println(ex);
                new MessageBox(this, "Error: " + ex.getMessage());
            }
        }
    }

    // window events (only closing is used, WindowListener needs all 7 methods)
    public void windowClosing(WindowEvent we)
    {
        dispose();
    }
    public void windowOpened(WindowEvent we) {}
    public void windowClosed(WindowEvent we) {}
    public void windowIconified(WindowEvent we) {}
    public void windowDeiconified(WindowEvent we) {}
    public void windowActivated(WindowEvent we) {}
    public void windowDeactivated(WindowEvent we) {}
}
