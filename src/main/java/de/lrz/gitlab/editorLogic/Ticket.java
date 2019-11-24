package de.lrz.gitlab.editorLogic;

public class Ticket {


    private static int ticketIdCounter = 42;
    public int ticketId;

    public int lineStart;
    public int lineEnd;
    public String filePath;
    public String developingPersonName;
    public String responsiblePersonName;
    public String responsiblePersonMail;
    public String developingPersonMail;
    public String messageContent;
    public String gitLink;

    Ticket(
            int lineStart,
            int lineEnd,
            String filePath
    ) {

        this.lineStart = lineStart;
        this.lineEnd = lineEnd;
        this.filePath = filePath;

        ticketId = ticketIdCounter;
        ticketIdCounter++;

    }
}




