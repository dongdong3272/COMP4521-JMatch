package edu.hkust.jmatch.adapter;

public class MatchRecord {
    private String documentID;
    private String title;
    private String time;

    /**
     * Constructor for the card data model.
     * @param documentID The documentID of the record.
     * @param title The title of the record.
     * @param time  The time when the record is created
     */
    public MatchRecord(String documentID, String title, String time) {
        this.documentID = documentID;
        this.title = title;
        this.time = time;
    }

    String getTitle() { return title;}
    String getTime() {
        return time;
    }
    String getDocumentID() {
        return documentID;
    }
}
