package src;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.function.Consumer;

public class ModernDatePicker extends JPanel {
    private LocalDate selectedDate;
    private final Consumer<LocalDate> onDateSelected;
    private LocalDate viewingDate;

    private final Color ACCENT = new Color(0, 102, 204);
    private final Color HOVER = new Color(230, 242, 255);

    public ModernDatePicker(Consumer<LocalDate> onDateSelected) {
        this.onDateSelected = onDateSelected;
        this.selectedDate = LocalDate.now();
        this.viewingDate = selectedDate;

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));

        renderCalendar();
    }

    private void renderCalendar() {
        removeAll();

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(ACCENT);
        header.setPreferredSize(new Dimension(300, 40));

        JButton prev = createNavBtn("<");
        prev.addActionListener(e -> {
            viewingDate = viewingDate.minusMonths(1);
            renderCalendar();
        });

        JButton next = createNavBtn(">");
        next.addActionListener(e -> {
            viewingDate = viewingDate.plusMonths(1);
            renderCalendar();
        });

        JLabel monthLbl = new JLabel(viewingDate.getMonth().name() + " " + viewingDate.getYear(),
                SwingConstants.CENTER);
        monthLbl.setForeground(Color.WHITE);
        monthLbl.setFont(new Font("Segoe UI", Font.BOLD, 14));

        header.add(prev, BorderLayout.WEST);
        header.add(monthLbl, BorderLayout.CENTER);
        header.add(next, BorderLayout.EAST);

        // Days Grid
        JPanel grid = new JPanel(new GridLayout(0, 7));
        grid.setBackground(Color.WHITE);

        String[] days = { "Su", "Mo", "Tu", "We", "Th", "Fr", "Sa" };
        for (String d : days) {
            JLabel l = new JLabel(d, SwingConstants.CENTER);
            l.setFont(new Font("Segoe UI", Font.BOLD, 10));
            l.setForeground(Color.GRAY);
            grid.add(l);
        }

        YearMonth ym = YearMonth.from(viewingDate);
        LocalDate firstOfMonth = viewingDate.withDayOfMonth(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue() % 7;

        for (int i = 0; i < dayOfWeek; i++)
            grid.add(new JLabel(""));

        for (int day = 1; day <= ym.lengthOfMonth(); day++) {
            LocalDate current = viewingDate.withDayOfMonth(day);
            JLabel dayLbl = new JLabel(String.valueOf(day), SwingConstants.CENTER);
            dayLbl.setOpaque(true);
            dayLbl.setBackground(Color.WHITE);
            dayLbl.setCursor(new Cursor(Cursor.HAND_CURSOR));

            if (current.equals(selectedDate)) {
                dayLbl.setBackground(ACCENT);
                dayLbl.setForeground(Color.WHITE);
            }

            dayLbl.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    selectedDate = current;
                    onDateSelected.accept(selectedDate);
                    renderCalendar();
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    if (!current.equals(selectedDate))
                        dayLbl.setBackground(HOVER);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    if (!current.equals(selectedDate))
                        dayLbl.setBackground(Color.WHITE);
                }
            });
            grid.add(dayLbl);
        }

        add(header, BorderLayout.NORTH);
        add(grid, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    private JButton createNavBtn(String text) {
        JButton b = new JButton(text);
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Monospaced", Font.BOLD, 18));
        return b;
    }

    public static void showPopup(Component parent, Consumer<LocalDate> onSelect) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(parent), "Select Date", true);
        dialog.setUndecorated(true);
        ModernDatePicker dp = new ModernDatePicker(date -> {
            onSelect.accept(date);
            dialog.dispose();
        });
        dialog.add(dp);
        dialog.pack();
        Point p = parent.getLocationOnScreen();
        dialog.setLocation(p.x, p.y + parent.getHeight());
        dialog.setVisible(true);
    }
}
