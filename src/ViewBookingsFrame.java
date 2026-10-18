import java.awt.*;
import java.awt.event.*;
import java.sql.*;

// admin can see all bookings and search a booking by PNR
public class ViewBookingsFrame extends Frame implements ActionListener, ItemListener, WindowListener
{
    Label l1, l2, l3;
    TextField pnrTf;
    List bookingList;
    TextArea detailsArea;
    Button searchBtn, showAllBtn, backBtn;
    String[] pnrs = new String[1000]; // pnr of every row in the list
    int total = 0;                    // number of rows in the list

    public ViewBookingsFrame()
    {
        // setting the layout (no layout manager, every component gets x, y, width, height)
        setLayout(null);

        // creating objects
        l1 = new Label("ALL BOOKINGS", Label.CENTER);
        l2 = new Label("PNR");
        l3 = new Label(String.format(" %-7s %-7s %-22s %-17s %-5s %-10s %s", "PNR", "Flight", "Route", "Departure", "Seat", "Status", "Passenger"));
        pnrTf = new TextField(10);
        bookingList = new List(10);
        detailsArea = new TextArea(14, 60);
        searchBtn = new Button("Search");
        showAllBtn = new Button("List All");
        backBtn = new Button("Back");

        // design
        setBackground(new Color(232, 240, 250));
        setFont(new Font("Arial", Font.PLAIN, 15));
        l1.setBackground(new Color(22, 54, 110));
        l1.setForeground(Color.white);
        l1.setFont(new Font("Arial", Font.BOLD, 22));
        l3.setFont(new Font("Monospaced", Font.BOLD, 14));
        bookingList.setFont(new Font("Monospaced", Font.PLAIN, 14));
        detailsArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        detailsArea.setEditable(false);

        // event handling
        searchBtn.addActionListener(this);
        showAllBtn.addActionListener(this);
        backBtn.addActionListener(this);
        bookingList.addItemListener(this);

        // adding components (x, y, width, height)
        l1.setBounds(0, 30, 900, 50);
        l2.setBounds(20, 95, 40, 30);
        pnrTf.setBounds(65, 95, 150, 30);
        searchBtn.setBounds(230, 95, 110, 30);
        showAllBtn.setBounds(350, 95, 110, 30);
        backBtn.setBounds(470, 95, 110, 30);
        l3.setBounds(20, 140, 860, 25);
        bookingList.setBounds(20, 165, 860, 250);
        detailsArea.setBounds(20, 425, 860, 260);
        add(l1);
        add(l2);
        add(pnrTf);
        add(searchBtn);
        add(showAllBtn);
        add(backBtn);
        add(l3);
        add(bookingList);
        add(detailsArea);

        // closing the window
        addWindowListener(this);

        // creating the frame
        setTitle("All Bookings");
        setSize(900, 720);
        setLocationRelativeTo(null);
        setVisible(true);

        loadBookings("");
    }

    // shows bookings in the list, if pnr is empty it shows all bookings
    void loadBookings(String pnr)
    {
        bookingList.removeAll();
        total = 0;
        try
        {
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT b.*, s.seat_no, f.flight_no, f.source, f.destination, f.departure_time FROM bookings b "
                    + "JOIN seats s ON b.seat_id = s.seat_id JOIN flights f ON b.flight_id = f.flight_id "
                    + "WHERE ? = '' OR b.pnr = ? ORDER BY b.booking_date DESC, b.pnr, s.seat_id");
            ps.setString(1, pnr);
            ps.setString(2, pnr);
            ResultSet rs = ps.executeQuery();
            while (rs.next() && total < 1000)
            {
                pnrs[total] = rs.getString("pnr");
                total++;
                bookingList.add(String.format("%-7s %-7s %-22s %-17s %-5s %-10s %s", rs.getString("pnr"), rs.getString("flight_no"),
                        rs.getString("source") + " -> " + rs.getString("destination"), rs.getString("departure_time").substring(0, 16),
                        rs.getString("seat_no"), rs.getString("status"), rs.getString("passenger_name")));
            }
            con.close();
            detailsArea.setText("Total tickets: " + total + "\nClick on a booking to see the details.");
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

        if (btn == backBtn)
            dispose();
        else if (btn == searchBtn)
            loadBookings(pnrTf.getText().trim().toUpperCase());
        else
        {
            pnrTf.setText("");
            loadBookings("");
        }
    }

    // when a booking is clicked in the list, show its details
    public void itemStateChanged(ItemEvent e)
    {
        try
        {
            detailsArea.setText(TicketService.getDetails(pnrs[bookingList.getSelectedIndex()]));
        }
        catch (Exception ex)
        {
            detailsArea.setText("Error: " + ex.getMessage());
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
