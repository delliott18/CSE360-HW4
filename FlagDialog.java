import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;

public class FlagDialog extends JDialog {
    private final String itemType;   // "QUESTION", "ANSWER", "REVIEW"
    private final int    itemId;
    private final String staffUser;
    private final Runnable onComplete;   // refresh callback

    public FlagDialog(Frame owner,
                      String itemType,
                      int itemId,
                      String staffUser,
                      Runnable onComplete) {
        super(owner, "Flag " + itemType, true);
        this.itemType = itemType;
        this.itemId   = itemId;
        this.staffUser = staffUser;
        this.onComplete = onComplete;

        buildUI();
    }

    private void buildUI() {
        setSize(400, 250);
        setLocationRelativeTo(getOwner());
        setLayout(new BorderLayout(10,10));
        ((JComponent)getContentPane()).setBorder(
            BorderFactory.createEmptyBorder(10,10,10,10));

        JTextArea noteArea = new JTextArea(5, 30);
        noteArea.setLineWrap(true);
        noteArea.setWrapStyleWord(true);
        JScrollPane sp = new JScrollPane(noteArea);
        sp.setBorder(BorderFactory.createTitledBorder("Internal note (optional)"));

        JButton flagBtn = new JButton("Flag");
        JButton cancel  = new JButton("Cancel");

        flagBtn.addActionListener(e -> {
            int flagId = MemoryStorage.addFlag(itemType, itemId, staffUser);
            if (flagId == -1) {
                JOptionPane.showMessageDialog(this,
                        "This item is already flagged.",
                        "Duplicate Flag", JOptionPane.INFORMATION_MESSAGE);
                dispose();
                return;
            }
            String note = noteArea.getText().trim();
            if (!note.isEmpty()) {
                MemoryStorage.setFlagNote(flagId, note);
            }
            dispose();
            if (onComplete != null) onComplete.run();
        });
        cancel.addActionListener(e -> dispose());

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.add(flagBtn);
        btnPanel.add(cancel);

        add(sp, BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);
    }

    /** Convenience helper */
    public static void showDialog(Frame owner,
                                  String itemType,
                                  int itemId,
                                  String staffUser,
                                  Runnable onComplete) {
        new FlagDialog(owner, itemType, itemId, staffUser, onComplete).setVisible(true);
    }
}
