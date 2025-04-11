import java.time.LocalDateTime;


public class Flag {
    private final int id;
    private final String itemType;        // "QUESTION", "ANSWER", "REVIEW"
    private final int itemId;
    private final String flaggedBy;       // staff username
    private String note = "";                  // internal note (null if empty)
    private boolean resolved = false;
    private String resolvedBy;            // staff / instructor username
    private LocalDateTime flaggedOn;
    private LocalDateTime resolvedOn;

    public Flag(int id, String itemType, int itemId, String flaggedBy, LocalDateTime flaggedOn) {
        this.id        = id;
        this.itemType  = itemType;
        this.itemId    = itemId;
        this.flaggedBy = flaggedBy;
        this.flaggedOn = flaggedOn;
    }

    public int getId()            { return id; }
    public String getItemType()   { return itemType; }
    public int getItemId()        { return itemId; }
    public String getFlaggedBy()  { return flaggedBy; }
    public String getNote()       { return note; }
    public boolean isResolved()   { return resolved; }
    public String getResolvedBy() { return resolvedBy; }
    public LocalDateTime getFlaggedOn()  { return flaggedOn; }
    public LocalDateTime getResolvedOn() { return resolvedOn; }

    void setNote(String note) { this.note = note; }

    void resolve(String resolver, LocalDateTime when) {
        this.resolved   = true;
        this.resolvedBy = resolver;
        this.resolvedOn = when;
    }
}
