package de.lrz.gitlab.editorLogic;

import com.sun.jna.StringArray;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.util.ArrayList;

public class DiffHandler {

/*    public static void main(String[] args) throws IOException {
//        handle("/Users/florianangermeir/Projects/Sonstig/co-brain-reloaded/src/main/java/de/lrz/gitlab/diff.py", 3, 5);
        //System.out.println(runCommand("echo hello world").get(0));
    }
*/
    public static String gitDirect = "";

    public static String[] handle(String gitDir, String path, int beginn, int end) throws IOException {
        gitDirect = gitDir;
        ArrayList <String> changedLines = runCommand("diff -U0 " + path);
        ArrayList <float[]> newLines = new ArrayList();
        for (String line : changedLines) {
            if (line.matches("@@ ([-+][0-9]+[,0-9]* )+@@")) {
                String[] splitLine = line.split(" ");
                float[] temp = {Float.valueOf(splitLine[1]), Float.valueOf(splitLine[2])};
                newLines.add(temp);
            }
        }
        int[] originLines = reconstructRemoteLineNumbers(newLines, beginn, end);
        String url = createRemoteURL(path, originLines);
        String[] blameUser = blame(path, originLines);
        String[] currentUser = getCurrentUser();
        return new String[] {currentUser[0], currentUser[1], blameUser[0], blameUser[1], url};
    }

    public static String[] getCurrentUser() throws IOException {
        ArrayList<String> gitConfig = runCommand("config --list");
        String userName = "";
        String userMail = "";
        for (String line : gitConfig) {
            if (line.startsWith("user.name")) {
                userName = line.split("=")[1];
            } else if (line.startsWith("user.email")) {
                userMail = line.split("=")[1];
            }
            if (!userName.isEmpty() && !userMail.isEmpty()) {
                break;
            }
        }
        return new String[]{userMail, userName};
    }

    public static String createRemoteURL(String path, int[] originLines) throws IOException {
        String commitID = getCommitID();
        String originURL = getOriginUrl();
        return originURL + "/blob/" + commitID + "/" + path + "#L" + originLines[0] + "-" + originLines[1];
    }

    public static String getOriginUrl() throws IOException {
        String url = runCommand("remote get-url origin").get(0);
        if (url.contains("@")) {
            url = url.split("@")[1];
        }
        if (url.contains(":")) {
            url = url.replace(":", "/");
        }
        url = url.substring(0,url.length()-4);
        return url;
    }

    public static String getCommitID() throws IOException {
        String[] commit = runCommand("remote get-url origin").get(0).split(" ");
        String commitID = commit[commit.length-1];
        return commitID;
    }

    public static String[] blame(String path, int[] originLines) throws IOException {
        int snippetLength = originLines[1] - originLines[0];
        String blamingMailRaw = runCommand("blame " + "-e -L " + originLines[0]+ ",+" + snippetLength +" " + path).get(0);
        String blamingNameRaw = runCommand("blame " + "-L " + originLines[0]+ ",+" + snippetLength +" " + path).get(0);
        blamingMailRaw = blamingMailRaw.split("<")[1];
        // Vllt .strip nötig
        String blamingMail = blamingMailRaw.split(">")[0];
        blamingNameRaw = blamingNameRaw.split("\\(")[1];
        String blamingName = blamingNameRaw.split(" [0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9]")[0];
        return new String[]{blamingMail, blamingName};
    }

    public static int[] transformLineAssignment(float lineAssignment) {
        String[] temp = String.valueOf(lineAssignment).split(".");
        return new int[] {Integer.parseInt(temp[0]), Integer.parseInt(temp[0])};
    }

    public static int[] reconstructRemoteLineNumbers(ArrayList <float[]> changes, int beginn, int end) {
        ArrayList <float[]> changesWithImpact = new ArrayList();
        for(float[] item : changes) {
            if (Math.abs(beginn) >= Math.abs(item[1])) {
                changesWithImpact.add(item);
            }
        }
        if (changesWithImpact.isEmpty()) {
            return new int[]{beginn, end};
        } else {
            int snippetlength = end-beginn;
            int[] originChange = transformLineAssignment(changesWithImpact.get(changesWithImpact.size()-1)[0]);
            int[] localChange = transformLineAssignment(changesWithImpact.get(changesWithImpact.size()-1)[0]);
            int originChangeStart = originChange[0];
            int originAffected = originChange[1];
            int localChangeStart = localChange[0];
            int localAffected = localChange[1];
            int diffStartToSnippet = 0;
            if (originAffected != 0) {
                diffStartToSnippet = beginn + originAffected - localChangeStart - 1;
            } else if (localAffected != 0) {
                diffStartToSnippet = beginn - originAffected - localChangeStart + 1;
            }
            int actual_begin = originChangeStart + diffStartToSnippet;
            int actual_end = originChangeStart + diffStartToSnippet + snippetlength;
            return new int[]{actual_begin, actual_end};
        }
    }

    public static ArrayList<String> runCommand(String command) throws IOException {
        Runtime rt = Runtime.getRuntime();
        Process proc = rt.exec("git -C " + gitDirect + " " + command);
        //Process proc = rt.exec("pwd");
        BufferedReader stdInput = new BufferedReader(new
                InputStreamReader(proc.getInputStream()));

        BufferedReader stdError = new BufferedReader(new
                InputStreamReader(proc.getErrorStream()));

        ArrayList result = new ArrayList();

        // Read the output from the command
        //System.out.println("Here is the standard output of the command:\n");
        String s = null;
        while ((s = stdInput.readLine()) != null) {
            result.add(s);
        }
        return result;
    }

}