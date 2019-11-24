package de.lrz.gitlab.editorLogic;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class PythonRunner {

    public static String[] runPython(String path, int start, int end) throws IOException {
        String[] results = new String[5];

        File currentDirFile = new File(".");
        String helper = currentDirFile.getAbsolutePath();
        // System.out.println(helper);
        String command = "python3 " + helper.substring(0, helper.length() - 1) + "de/lrz/gitlab/src/main/java/de/lrz/gitlab/diff.py -p " + path + " -b " + start + " -e " + end;

        System.out.println(command);

        Runtime rt = Runtime.getRuntime();
        Process proc = rt.exec(command);

        BufferedReader stdInput = new BufferedReader(new
                InputStreamReader(proc.getInputStream()));

        BufferedReader stdError = new BufferedReader(new
                InputStreamReader(proc.getErrorStream()));

        String[] result = new String[5];

        // Read the output from the command
        //System.out.println("Here is the standard output of the command:\n");
        String s = null;
        int i = 0;
        while ((s = stdInput.readLine()) != null) {
            result[i] = s;
            i++;
        }

        return results;
    }
}
