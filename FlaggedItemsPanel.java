import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class FlaggedItemsPanel extends JPanel {
    private final String currentUser;
    private final DefaultTableModel model;
    private final JTable table;

    public FlaggedItemsPanel(String currentUser) {
        this.currentUser = currentUser;

        setLayout(new BorderLayout(10,10));
        setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        String[] cols = {"Type","Title / Excerpt","Flagged By","Flagged On","ID"};
        model = new DefaultTableModel(cols,0){ public boolean isCellEditable(int r,int c){return false;} };
        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JButton viewNote = new JButton("View Note");
        JButton resolve  = new JButton("Mark Resolved");
        JButton refresh  = new JButton("Refresh");
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btns.add(viewNote); btns.add(resolve); btns.add(refresh);
        add(btns, BorderLayout.SOUTH);

        refresh.addActionListener(e -> loadFlags());

        resolve.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) return;
            int flagId = (Integer) model.getValueAt(row, 4);
            MemoryStorage.resolveFlag(flagId, currentUser);
            loadFlags();
        });

        viewNote.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) return;
            int flagId = (Integer) model.getValueAt(row, 4);
            Flag flag = MemoryStorage.getFlags(true).stream()
                         .filter(f -> f.getId() == flagId)
                         .findFirst().orElse(null);
            if (flag == null) return;

            JTextArea area = new JTextArea(
                flag.getNote() == null || flag.getNote().isEmpty()
                    ? "(no note)" : flag.getNote(),
                8, 40);
            area.setEditable(false);
            area.setLineWrap(true);
            area.setWrapStyleWord(true);

            JOptionPane.showMessageDialog(
                this,
                new JScrollPane(area),
                "Note for " + flag.getItemType() + " #" + flag.getItemId(),
                JOptionPane.INFORMATION_MESSAGE
            );
        });

        loadFlags();
    }

    private void loadFlags() {
        model.setRowCount(0);
        List<Flag> flags = MemoryStorage.getFlags(false);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        for (Flag f : flags) {
            String title = "";
            if ("QUESTION".equals(f.getItemType())) {
                title = MemoryStorage.getQuestion(f.getItemId())
                        .map(Question::getTitle).orElse("");
            } else if ("ANSWER".equals(f.getItemType())) {
                title = MemoryStorage.getAnswers().stream()
                        .filter(a -> a.getId() == f.getItemId())
                        .map(a -> {
                            String txt = a.getContent();
                            return txt.length() > 40 ? txt.substring(0,40)+"…" : txt;
                        })
                        .findFirst().orElse("");
            }
            model.addRow(new Object[]{
                    f.getItemType(), title, f.getFlaggedBy(),
                    f.getFlaggedOn().format(fmt), f.getId()
            });
        }
        if (table.getColumnCount() == 5) {
            table.removeColumn(table.getColumnModel().getColumn(4));
        }
    }
}
