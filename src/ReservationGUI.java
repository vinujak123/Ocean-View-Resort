package src;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class ReservationGUI extends JFrame {
    private final ReservationManager resManager;
    private final List<User> staffUsers;
    private User currentUser;

    private CardLayout cardLayout;
    private JPanel contentArea;

    // Modern Palette
    private final Color SIDEBAR_BG = new Color(15, 23, 42); // Navy Slate
    private final Color APP_BG = new Color(248, 250, 252); // Ultra Light Gray
    private final Color CARD_BG = Color.WHITE;
    private final Color ACCENT = new Color(37, 99, 235); // Royal Blue
    private final Color TEXT_MAIN = new Color(30, 41, 59);
    private final Color BORDER = new Color(226, 232, 240);

    private JPanel rootCardPanel;
    private CardLayout topCardLayout;

    public ReservationGUI() {
        this.resManager = new ReservationManager();
        this.staffUsers = FileHandler.loadUsers();

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        setTitle("Ocean View Resort | Management Platform");
        setSize(1150, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        topCardLayout = new CardLayout();
        rootCardPanel = new JPanel(topCardLayout);

        setupLoginScreen();

        JPanel mainApp = new JPanel(new BorderLayout());
        mainApp.setBackground(APP_BG);
        setupSidebar(mainApp);
        setupMainArea(mainApp);

        rootCardPanel.add(mainApp, "MainApp");

        add(rootCardPanel);
        setVisible(true);
    }

    private void setupLoginScreen() {
        JPanel loginBg = new JPanel(new GridBagLayout());
        loginBg.setBackground(SIDEBAR_BG);

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Color.WHITE);
        card.setPreferredSize(new Dimension(400, 500));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                new EmptyBorder(40, 40, 40, 40)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 0, 10, 0);

        JLabel title = new JLabel("STAFF PORTAL", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(SIDEBAR_BG);
        gbc.gridy = 0;
        card.add(title, gbc);

        gbc.gridy = 1;
        card.add(new JLabel("Username"), gbc);
        JTextField u = createField("Username");
        gbc.gridy = 2;
        card.add(u, gbc);

        gbc.gridy = 3;
        card.add(new JLabel("Password"), gbc);
        JPasswordField p = new JPasswordField();
        p.setPreferredSize(new Dimension(0, 45));
        p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER, 1),
                new EmptyBorder(5, 15, 5, 15)));
        gbc.gridy = 4;
        card.add(p, gbc);

        JButton login = new JButton("LOGIN");
        login.setBackground(ACCENT);
        login.setForeground(Color.WHITE);
        login.setOpaque(true);
        login.setBorderPainted(false);
        login.setFont(new Font("Segoe UI", Font.BOLD, 14));
        login.setPreferredSize(new Dimension(0, 50));
        gbc.gridy = 5;
        gbc.insets = new Insets(20, 0, 10, 0);
        card.add(login, gbc);

        login.addActionListener(e -> {
            String user = u.getText();
            String pass = new String(p.getPassword());
            for (User staff : staffUsers) {
                if (staff.getUsername().equals(user) && staff.authenticate(pass)) {
                    currentUser = staff;
                    topCardLayout.show(rootCardPanel, "MainApp");
                    return;
                }
            }
            JOptionPane.showMessageDialog(this, "Access Denied");
        });

        loginBg.add(card);
        rootCardPanel.add(loginBg, "Login");
    }

    private void setupSidebar(JPanel root) {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(260, 0));
        sidebar.setBackground(SIDEBAR_BG);
        sidebar.setBorder(new EmptyBorder(30, 0, 0, 0));

        // Brand
        JLabel brand = new JLabel("OCEAN VIEW");
        brand.setFont(new Font("Segoe UI", Font.BOLD, 22));
        brand.setForeground(Color.WHITE);
        brand.setAlignmentX(Component.CENTER_ALIGNMENT);
        brand.setBorder(new EmptyBorder(0, 0, 40, 0));
        sidebar.add(brand);

        String[] menuItems = { "Dashboard", "Add Reservation", "View All", "Billing", "Help", "Logout" };
        for (String item : menuItems) {
            JButton btn = createMenuBtn(item);
            sidebar.add(btn);
            sidebar.add(Box.createVerticalStrut(5));
        }

        root.add(sidebar, BorderLayout.WEST);
    }

    private JButton createMenuBtn(String text) {
        JButton b = new JButton(text);
        b.setAlignmentX(Component.CENTER_ALIGNMENT);
        b.setMaximumSize(new Dimension(240, 50));
        b.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        b.setForeground(new Color(148, 163, 184));
        b.setBackground(SIDEBAR_BG);
        b.setBorder(new EmptyBorder(0, 25, 0, 0));
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setFocusPainted(false);
        b.setContentAreaFilled(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));

        b.addActionListener(e -> handleNavigation(text));

        b.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                b.setForeground(Color.WHITE);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (!b.getText().equals(currentNav))
                    b.setForeground(new Color(148, 163, 184));
            }
        });
        return b;
    }

    private String currentNav = "Dashboard";

    private void setupMainArea(JPanel root) {
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(APP_BG);

        // Top Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 70));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));
        header.setBorder(new EmptyBorder(0, 30, 0, 30));

        JLabel title = new JLabel("Welcome back, " + (currentUser != null ? currentUser.getUsername() : "Staff"));
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(TEXT_MAIN);
        header.add(title, BorderLayout.WEST);

        main.add(header, BorderLayout.NORTH);

        // Content
        cardLayout = new CardLayout();
        contentArea = new JPanel(cardLayout);
        contentArea.setBackground(APP_BG);
        contentArea.setBorder(new EmptyBorder(30, 40, 30, 40));

        contentArea.add(createDashboard(), "Dashboard");
        contentArea.add(createAddForm(), "Add Reservation");
        contentArea.add(createListView(), "View All");
        contentArea.add(createBillingTerminal(), "Billing");
        contentArea.add(createHelpPage(), "Help");

        main.add(contentArea, BorderLayout.CENTER);
        root.add(main, BorderLayout.CENTER);
    }

    private void handleNavigation(String target) {
        if (target.equals("Logout")) {
            System.exit(0);
            return;
        }
        currentNav = target;
        cardLayout.show(contentArea, target);
        if (target.equals("View All"))
            refreshTableData();
        if (target.equals("Dashboard"))
            refreshStats();
    }

    private JLabel statTotal, statRevenue;

    private JPanel createDashboard() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);

        JPanel grid = new JPanel(new GridLayout(1, 4, 20, 0));
        grid.setOpaque(false);

        statTotal = new JLabel("0");
        statRevenue = new JLabel("LKR 0");

        grid.add(createStatCard("Total Bookings", statTotal, new Color(59, 130, 246)));
        grid.add(createStatCard("Total Revenue", statRevenue, new Color(16, 185, 129)));
        grid.add(createStatCard("Occupancy", new JLabel("78%"), new Color(245, 158, 11)));
        grid.add(createStatCard("Satisfaction", new JLabel("4.9/5"), new Color(139, 92, 246)));

        p.add(grid, BorderLayout.NORTH);

        // Performance Chart
        JPanel chartContainer = new JPanel(new BorderLayout());
        chartContainer.setOpaque(false);
        chartContainer.setBorder(new EmptyBorder(30, 0, 0, 0));

        JPanel chartCard = new JPanel(new BorderLayout());
        chartCard.setBackground(CARD_BG);
        chartCard.setBorder(BorderFactory.createLineBorder(BORDER, 1));

        JLabel chartTitle = new JLabel("Weekly Booking Analytics");
        chartTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        chartTitle.setBorder(new EmptyBorder(20, 20, 10, 20));
        chartCard.add(chartTitle, BorderLayout.NORTH);

        JPanel chartArea = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();
                int margin = 50;

                // Draw Axes
                g2.setColor(BORDER);
                g2.drawLine(margin, h - margin, w - margin, h - margin);
                g2.drawLine(margin, margin, margin, h - margin);

                // Mock Data Bars
                int[] data = { 40, 65, 30, 85, 50, 95, 70 };
                String[] labels = { "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun" };
                int barWidth = (w - 2 * margin) / data.length - 20;

                for (int i = 0; i < data.length; i++) {
                    int barHeight = (int) ((data[i] / 100.0) * (h - 2 * margin));
                    g2.setColor(ACCENT);
                    g2.fillRect(margin + 10 + i * (barWidth + 20), h - margin - barHeight, barWidth, barHeight);

                    g2.setColor(TEXT_MAIN);
                    g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                    g2.drawString(labels[i], margin + 10 + i * (barWidth + 20), h - margin + 20);
                }
            }
        };
        chartArea.setBackground(CARD_BG);
        chartCard.add(chartArea, BorderLayout.CENTER);

        chartContainer.add(chartCard, BorderLayout.CENTER);
        p.add(chartContainer, BorderLayout.CENTER);

        refreshStats();
        return p;
    }

    private void refreshStats() {
        int count = resManager.getAllReservations().size();
        double rev = resManager.getAllReservations().stream().mapToDouble(Reservation::calculateTotalBill).sum();
        statTotal.setText(String.valueOf(count));
        statRevenue.setText(String.format("LKR %,.0f", rev));
    }

    private JPanel createStatCard(String title, JLabel valueLbl, Color accent) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                new EmptyBorder(20, 20, 20, 20)));

        JLabel t = new JLabel(title);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        t.setForeground(new Color(100, 116, 139));

        valueLbl.setFont(new Font("Segoe UI Semibold", Font.BOLD, 24));
        valueLbl.setForeground(TEXT_MAIN);

        JPanel bar = new JPanel();
        bar.setPreferredSize(new Dimension(4, 0));
        bar.setBackground(accent);

        card.add(t, BorderLayout.NORTH);
        card.add(valueLbl, BorderLayout.CENTER);
        card.add(bar, BorderLayout.WEST);

        return card;
    }

    private JTextField fId;

    private JPanel createAddForm() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(CARD_BG);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                new EmptyBorder(40, 40, 40, 40)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        fId = createField("Generating...");
        fId.setEditable(false);
        fId.setText(resManager.getNextReferenceId());

        JTextField fName = createField("Full Guest Name");
        JTextField fAddr = createField("Permanent Address");
        JTextField fPhone = createField("Mobile Number");
        JComboBox<RoomType> fType = new JComboBox<>(RoomType.values());

        // Date Fields with Pickers
        JTextField fCheckIn = createField("YYYY-MM-DD");
        fCheckIn.setEditable(false);
        fCheckIn.setBackground(new Color(245, 245, 245));
        JButton btnIn = createStyledActionBtn("Select");
        btnIn.addActionListener(e -> ModernDatePicker.showPopup(fCheckIn, d -> fCheckIn.setText(d.toString())));

        JTextField fCheckOut = createField("YYYY-MM-DD");
        fCheckOut.setEditable(false);
        fCheckOut.setBackground(new Color(245, 245, 245));
        JButton btnOut = createStyledActionBtn("Select");
        btnOut.addActionListener(e -> ModernDatePicker.showPopup(fCheckOut, d -> fCheckOut.setText(d.toString())));

        int r = 0;
        addLabeledField(p, "Reference ID", fId, gbc, r++);
        addLabeledField(p, "Guest Name", fName, gbc, r++);
        addLabeledField(p, "Address", fAddr, gbc, r++);
        addLabeledField(p, "Contact", fPhone, gbc, r++);
        addLabeledField(p, "Room Type", fType, gbc, r++);

        // Date Row
        gbc.gridx = 0;
        gbc.gridy = r;
        gbc.gridwidth = 1;
        p.add(new JLabel("Arrival"), gbc);
        JPanel pIn = new JPanel(new BorderLayout(5, 0));
        pIn.setOpaque(false);
        pIn.add(fCheckIn);
        pIn.add(btnIn, BorderLayout.EAST);
        gbc.gridx = 1;
        p.add(pIn, gbc);
        r++;

        gbc.gridx = 0;
        gbc.gridy = r;
        p.add(new JLabel("Departure"), gbc);
        JPanel pOut = new JPanel(new BorderLayout(5, 0));
        pOut.setOpaque(false);
        pOut.add(fCheckOut);
        pOut.add(btnOut, BorderLayout.EAST);
        gbc.gridx = 1;
        p.add(pOut, gbc);
        r++;

        JButton save = new JButton("CREATE RESERVATION");
        save.setFont(new Font("Segoe UI", Font.BOLD, 14));
        save.setBackground(ACCENT);
        save.setForeground(Color.WHITE);
        save.setOpaque(true);
        save.setBorderPainted(false);
        save.setPreferredSize(new Dimension(0, 50));
        gbc.gridx = 0;
        gbc.gridy = r;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(30, 10, 10, 10);
        p.add(save, gbc);

        save.addActionListener(e -> {
            try {
                if (fId.getText().isEmpty() || fName.getText().isEmpty() || fCheckIn.getText().isEmpty())
                    throw new Exception("Please fill required fields.");
                Reservation res = new Reservation(fId.getText(),
                        new Guest(fName.getText(), fAddr.getText(), fPhone.getText()),
                        (RoomType) fType.getSelectedItem(), LocalDate.parse(fCheckIn.getText()),
                        LocalDate.parse(fCheckOut.getText()));
                resManager.addReservation(res);
                JOptionPane.showMessageDialog(this, "Success! Reservation created.");

                // Clear and Update ID
                fName.setText("");
                fAddr.setText("");
                fPhone.setText("");
                fCheckIn.setText("");
                fCheckOut.setText("");
                fId.setText(resManager.getNextReferenceId());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage());
            }
        });

        return p;
    }

    private JButton createStyledActionBtn(String text) {
        JButton b = new JButton(text);
        b.setBackground(new Color(241, 245, 249));
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setBorder(BorderFactory.createLineBorder(BORDER, 1));
        b.setPreferredSize(new Dimension(80, 0));
        return b;
    }

    private void addLabeledField(JPanel p, String l, JComponent c, GridBagConstraints gbc, int r) {
        gbc.gridx = 0;
        gbc.gridy = r;
        gbc.gridwidth = 1;
        p.add(new JLabel(l), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        p.add(c, gbc);
    }

    private JTextField createField(String placeholder) {
        JTextField f = new JTextField();
        f.setPreferredSize(new Dimension(300, 45));
        f.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                new EmptyBorder(5, 15, 5, 15)));
        return f;
    }

    private JTable listTable;

    private JPanel createListView() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);

        String[] cols = { "Reference", "Guest Name", "Room", "Dates", "Bill Amount" };
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        listTable = new JTable(model);
        listTable.setRowHeight(50);
        listTable.setGridColor(BORDER);
        listTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JTableHeader h = listTable.getTableHeader();
        h.setBackground(Color.WHITE);
        h.setFont(new Font("Segoe UI", Font.BOLD, 14));
        h.setPreferredSize(new Dimension(0, 50));
        h.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));

        JScrollPane s = new JScrollPane(listTable);
        s.setBorder(BorderFactory.createLineBorder(BORDER));
        s.getViewport().setBackground(Color.WHITE);
        p.add(s, BorderLayout.CENTER);

        return p;
    }

    private void refreshTableData() {
        DefaultTableModel m = (DefaultTableModel) listTable.getModel();
        m.setRowCount(0);
        for (Reservation r : resManager.getAllReservations()) {
            m.addRow(new Object[] {
                    r.getReservationNumber(),
                    r.getGuest().getName(),
                    r.getRoomType().getName(),
                    r.getCheckInDate() + " to " + r.getCheckOutDate(),
                    String.format("LKR %,.2f", r.calculateTotalBill())
            });
        }
    }

    private JPanel createBillingTerminal() {
        JPanel p = new JPanel(new BorderLayout(0, 20));
        p.setOpaque(false);

        JPanel input = new JPanel(new FlowLayout(FlowLayout.LEFT));
        input.setOpaque(false);
        JTextField search = createField("Enter Reference ID");
        search.setPreferredSize(new Dimension(300, 45));

        JButton find = new JButton("GENERATE BILL");
        find.setBackground(ACCENT);
        find.setForeground(Color.WHITE);
        find.setOpaque(true);
        find.setBorderPainted(false);
        find.setFont(new Font("Segoe UI", Font.BOLD, 12));
        find.setPreferredSize(new Dimension(150, 45));

        input.add(search);
        input.add(find);

        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setFont(new Font("Monospaced", Font.PLAIN, 18));
        area.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                new EmptyBorder(40, 40, 40, 40)));
        area.setBackground(CARD_BG);

        find.addActionListener(e -> {
            resManager.findReservation(search.getText()).ifPresentOrElse(r -> {
                area.setText(String.format(
                        "            OCEAN VIEW RESORT\n" +
                                "             OFFICIAL INVOICE\n" +
                                "===========================================\n" +
                                "REFERENCE ID: %s\n" +
                                "GUEST NAME:   %s\n" +
                                "STAY PERIOD:  %s to %s\n" +
                                "ROOM TYPE:    %s\n" +
                                "NIGHTLY RATE: LKR %,.2f\n" +
                                "TOTAL NIGHTS: %d\n" +
                                "-------------------------------------------\n" +
                                "GRAND TOTAL:  LKR %,.2f\n" +
                                "===========================================\n\n" +
                                "Thank you for your stay!",
                        r.getReservationNumber(), r.getGuest().getName().toUpperCase(),
                        r.getCheckInDate(), r.getCheckOutDate(),
                        r.getRoomType().getName(), r.getRoomType().getRate(),
                        r.getNights(), r.calculateTotalBill()));
            }, () -> JOptionPane.showMessageDialog(this, "Reference ID Not Found"));
        });

        p.add(input, BorderLayout.NORTH);
        p.add(new JScrollPane(area), BorderLayout.CENTER);
        return p;
    }

    private JPanel createHelpPage() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(CARD_BG);
        p.add(new JLabel("Staff Documentation Hub", SwingConstants.CENTER));
        return p;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ReservationGUI::new);
    }
}
