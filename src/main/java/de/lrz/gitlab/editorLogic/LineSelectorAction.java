package de.lrz.gitlab.editorLogic;


import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.util.Range;


import javax.swing.*;
import java.io.IOException;


public class LineSelectorAction extends AnAction {

    public LineSelectorAction() {
        super("Ask Co-Brain for Help");
    }

    @Override
    public void actionPerformed(AnActionEvent e) {

        Project project = e.getProject();                   // Some general Objects to interact with the IDE
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        final Document document = editor.getDocument();

        String selection = editor.getSelectionModel().getSelectedText();

        int selectionStart = editor.getSelectionModel().getSelectionStart();
        int selectionEnd = editor.getSelectionModel().getSelectionEnd();
        int lineStart = document.getLineNumber(selectionStart) + 1;
        int lineEnd = document.getLineNumber(selectionEnd) + 1;

        String projectPath = e.getData(PlatformDataKeys.EDITOR).getProject().getBasePath();
        String filePath = e.getData(PlatformDataKeys.VIRTUAL_FILE).getPath();

        String relFilePath = filePath.replaceAll(projectPath, "");

        // create Ticket
        Ticket ticket = new Ticket(lineStart, lineEnd, relFilePath); // Prepare Ticket Object for the message.
        int firstLine= document.getLineStartOffset(lineStart-1); // first line above the selected lines

       // insert a TO-DO tag and a unique ticket number as a reference for the issue, which is reported to the responsible author
        WriteCommandAction.runWriteCommandAction(project, () ->  document.insertString(firstLine,"//TODO: "+ticket.ticketId+"\n"));

        String[] results = new String[5];
        try {
            results = DiffHandler.handle(projectPath+"/", relFilePath.substring(1, relFilePath.length()), lineStart, lineEnd);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        ticket.developingPersonMail = results[0];
        ticket.developingPersonName = results[1];
        ticket.responsiblePersonMail = results[2];
        ticket.responsiblePersonName = results[3];
        ticket.gitLink = results[4];


        ticket.messageContent= Messages.showInputDialog(project, "Please type a message to the responsible code author.", "Message to the code author", Messages.getQuestionIcon());
        EMailHandler eMailHandler = new EMailHandler();
        boolean result= eMailHandler.handleTicket(ticket);
        // if mailLogic returns true then acknowledge the transmission of the message
        Messages.showMessageDialog(project, "Your message has been sent!", "Transmission successful!", Messages.getInformationIcon());
        if(result){
            Messages.showMessageDialog(project, "Your message has been sent!", "Transmission successful!", Messages.getInformationIcon());
        }else{
            Messages.showMessageDialog(project, "Your message could not be sent!", "Transmission FAILED!", Messages.getInformationIcon());
        }


    }
}
