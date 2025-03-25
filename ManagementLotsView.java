import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public class ManagementLotsView extends JPanel {

    private DefaultListModel<String> lotListModel;
    private JList<String> lotList;
    private JPanel spacePanel;
    private final Color GREEN = new Color(127, 255, 212);
    private final Color RED = new Color(255, 127, 127);

    public ManagementLotsView(Consumer<String> switchTo) {
        // panel layout
        setLayout(new BorderLayout());

        // title
        JLabel titleLabel = new JLabel("Parking Lot Management", JLabel.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        add(titleLabel, BorderLayout.NORTH);

        // creating a split pane to hold parts of this view
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(300);

        // list that will hold the list of lots
        lotListModel = new DefaultListModel<>();
        lotList = new JList<>(lotListModel);
        lotList.setFont(new Font("SansSerif", Font.PLAIN, 16));

        // listener for the list of lots
        lotList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && lotList.getSelectedValue() != null) { //only trigger is an actual lot is selected
                loadSpacesForLot(lotList.getSelectedValue());
            }
        });

        //this is the actual panel that holds the list of lots
        JScrollPane lotScrollPane = new JScrollPane(lotList);
        splitPane.setLeftComponent(lotScrollPane);

        // this panel will display the parking spaces
        spacePanel = new JPanel(new GridLayout(10, 10, 5, 5));
        splitPane.setRightComponent(spacePanel);


        add(splitPane, BorderLayout.CENTER);

        // bottom Panel for adding lot button and back button
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        JButton addLotButton = new JButton("Add Parking Lot");
        addLotButton.setFont(new Font("SansSerif", Font.PLAIN, 16));
        addLotButton.addActionListener(e -> onAddParkingLot());

        JButton backButton = new JButton("Back to Dashboard");
        backButton.setFont(new Font("SansSerif", Font.PLAIN, 16));
        backButton.addActionListener(e -> switchTo.accept("ManagementDashboard"));

        buttonPanel.add(addLotButton);
        buttonPanel.add(backButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    //method for when manager makes a new lot
    private void onAddParkingLot() {
        String newLotName = JOptionPane.showInputDialog(this, "Enter Parking Lot Name:");
        if (newLotName != null && !newLotName.trim().isEmpty()) {
            lotListModel.addElement(newLotName + " (100 Spaces)");
        }
    }

    //method for loading parking spaces
    private void loadSpacesForLot(String lot) {
        spacePanel.removeAll(); // clear previous spaces

        // this is a temporary way to add example spaces, will update later
        for (int i = 1; i <= 100; i++) {
            JButton spaceButton = new JButton("S" + i);
            spaceButton.setFont(new Font("SansSerif", Font.PLAIN, 12));
            spaceButton.setBackground(GREEN);
            spaceButton.setOpaque(true);
            spaceButton.setBorderPainted(false);

            // add action listener to toggle spaces
            spaceButton.addActionListener(e -> toggleSpace((JButton) e.getSource()));

            spacePanel.add(spaceButton);
        }

        // Refresh the space panel
        spacePanel.revalidate();
        spacePanel.repaint();
    }

    //method for toggles between available and unavailable spaces, might change how this works when I implement more code
    private void toggleSpace(JButton spaceButton) {
        if (spaceButton.getBackground().equals(GREEN)) {
            spaceButton.setBackground(RED); // Disable the space
            JOptionPane.showMessageDialog(this, spaceButton.getText() + " is disabled for maintenance.");
        } else {
            spaceButton.setBackground(GREEN); // Enable the space
            JOptionPane.showMessageDialog(this, spaceButton.getText() + " is now available.");
        }
    }
}