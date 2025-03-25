import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.function.Consumer;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class UserBookLot extends JPanel {
    private final MainSystem mainSystem = MainSystem.getInstance();
    private final ArrayList<ParkingLot> parkingLots;
    private Consumer<String> switchTo;
    private JList<String> lotList; // list of parking lots
    private JPanel spacePanel;
    private JLabel titleLabel;
    private DefaultListModel<String> lotListModel;
    private final Color GREEN = new Color(127, 255, 212);
    private final Color RED = new Color(255, 127, 127);

    private JTextField durationField;
    private JLabel totalCostLabel;
    private User currentUser;



    public UserBookLot(Consumer<String> switchTo) {
        this.currentUser = MainSystem.currentUser;


        this.parkingLots = mainSystem.getLots();
        this.switchTo = switchTo;
        // panel layout
        setLayout(new BorderLayout());

        // back button
        JButton backButton = new JButton("Back to Dashboard");
        backButton.setFont(new Font("SansSerif", Font.PLAIN, 18));
        add(backButton, BorderLayout.NORTH);
        backButton.addActionListener(e -> switchTo.accept("UserDashboard"));


        // creating a split pane to hold parts of this view
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(300);

        // list that will hold the list of lots
        lotListModel = new DefaultListModel<>();
        lotList = new JList<>(lotListModel);
        lotList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lotList.setFont(new Font("SansSerif", Font.PLAIN, 16));
        lotList.setFixedCellHeight(40);

        // example lots; will change after database integration
//        lotListModel.addElement("Lot 1 - 20/50 Spaces Available");
//        lotListModel.addElement("Lot 2 - 10/25 Spaces Available");
//        lotListModel.addElement("Lot 3 - 5/18 Spaces Available");

        //adding lots
        for (ParkingLot lot : parkingLots) {
            if(lot.getEnabled()){// only show user the lot if its enabled
                lotListModel.addElement(lot.getLotName());
            }
        }

        // listener for the list of lots
        lotList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    // Load spaces for the selected lot
                    int selectedLot = lotList.getSelectedIndex();
                    loadSpacesForLot(selectedLot);
                }
            }
        });

        //this is the actual panel that holds the list of lots
        JScrollPane lotScrollPane = new JScrollPane(lotList);
        splitPane.setLeftComponent(lotScrollPane);

        // this panel will display the parking spaces
        spacePanel = new JPanel();
        spacePanel.setLayout(new GridLayout(10, 10, 5, 5)); // 5 rows x 10 spaces (example)

        // Panel for booking controls
        JPanel bookingPanel = new JPanel();
        bookingPanel.setLayout(new GridLayout(3, 2, 5, 5));

        // Label and input field for duration
        JLabel durationLabel = new JLabel("Enter Duration (hours):");
        durationField = new JTextField(5);
        totalCostLabel = new JLabel("Total Cost: $0");

        // Update total cost dynamically when duration is entered
        durationField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                try {
                    int duration = Integer.parseInt(durationField.getText().trim());
                    if (duration > 0) {
                        double totalCost = currentUser.getRate() * duration;
                        totalCostLabel.setText("Total Cost: $" + totalCost);
                    } else {
                        totalCostLabel.setText("Total Cost: $0");
                    }
                } catch (NumberFormatException ex) {
                    totalCostLabel.setText("Total Cost: $");
                }
            }
        });
        // Add components to booking panel
        bookingPanel.add(durationLabel);
        bookingPanel.add(durationField);
        bookingPanel.add(new JLabel("Hourly Rate: $" + currentUser.getRate()));
        bookingPanel.add(totalCostLabel);

// Add booking panel to the bottom of the main panel
        add(bookingPanel, BorderLayout.SOUTH);


        // placeholder initial spaces
        loadSpacesForLot(-1);
        splitPane.setRightComponent(spacePanel);

        // Add the split pane to the panel
        add(splitPane, BorderLayout.CENTER);
    }

    // method to load parking spaces
    private void loadSpacesForLot(int lot) {
        spacePanel.removeAll(); // clear previous spaces

        if (lot == -1) { //-1 case is for the intial view
            JLabel emptyLabel = new JLabel("Select a lot to view spaces", JLabel.CENTER);
            emptyLabel.setFont(new Font("SansSerif", Font.ITALIC, 18));
            spacePanel.add(emptyLabel);
        } else { //this case if for when a lot is selected
            // this is a temporary way to add example spaces, will update later
            ParkingSpace[] spaces = parkingLots.get(lot).getSpaces();
            for (ParkingSpace space : spaces) {
                JButton bt = new JButton(String.valueOf(space.getIndex()));
                if(space.getState().getClass().getSimpleName().equals("EmptyState")){
                    bt.setBackground(GREEN);
                    bt.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            int result = JOptionPane.showConfirmDialog(
                                    null,
                                    "Do you want to book this space?",
                                    "Book Space",
                                    JOptionPane.YES_NO_OPTION
                            );

                            // Step 2: If user clicks "Yes"
                            if (result == JOptionPane.YES_OPTION) {
                                // Show a custom dialog to gather booking details
                                displayBookingForm(lot, space.getIndex());
                            }

                        }
                    });
                }else if(space.getState().getClass().getSimpleName().equals("MaintenanceState")){
                    bt.setBackground(Color.ORANGE);
                    bt.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            JOptionPane.showMessageDialog(null, "Space is under maintenance");
                        }
                    });
                }else if(space.getState().getClass().getSimpleName().equals("OccupiedState")){
                    bt.setBackground(RED);
                    bt.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            JOptionPane.showMessageDialog(null, "Space is already occupied");
                        }
                    });
                }
                bt.setOpaque(true);
                bt.setBorderPainted(false);

                spacePanel.add(bt);
            }
        }

        // refresh the space panel
        spacePanel.revalidate();
        spacePanel.repaint();
    }
//method for showing payment box
    public void displayBookingForm(int lot, int spaceIndex) {
        JDialog dialog = new JDialog((Frame) null, "Enter Booking Details", true);
        dialog.setSize(400, 200);
        dialog.setLayout(new GridLayout(3, 2, 10, 10));
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        JTextField carInfoField = new JTextField();
        JTextField paymentField = new JTextField();

        dialog.add(new JLabel("Car Info (license plate):"));
        dialog.add(carInfoField);
        dialog.add(new JLabel("Payment Email (e-transfer):"));
        dialog.add(paymentField);

        JButton confirmButton = new JButton("Confirm Booking");
        confirmButton.addActionListener(e -> {
            String carInfo = carInfoField.getText();
            //currently i dont store this anywhere, i dont think we have to
            String paymentInfo = paymentField.getText();

            if (carInfo.isEmpty() || paymentInfo.isEmpty() || !paymentInfo.contains("@")) {
                JOptionPane.showMessageDialog(dialog, "Please fill in all fields correctly.", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(dialog, "Space booked!\nCar Info: " + carInfo + "\nEmail: " + paymentInfo,"Booking Confirmation", JOptionPane.INFORMATION_MESSAGE);
                parkingLots.get(lot).setSpace(spaceIndex, "OccupiedState", MainSystem.currentUser.getUsername(), carInfo);
                dialog.dispose(); // close the dialog
                mainSystem.updateFile("data/parkingSpaceData.csv");//update file
                //reload the lot
                loadSpacesForLot(lot);
            }
        });

        dialog.add(new JLabel());
        dialog.add(confirmButton);

        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }
}