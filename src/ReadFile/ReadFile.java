package ReadFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;

public class ReadFile {

    public static String path = null;
    public static String postingsPath = null;
    public static ArrayList<String> docsBuffer = new ArrayList<>(); // buffer for one chunk of docs
    //public int chunksNum;

    public static void start() {
        File[] dirs = new File(path).listFiles(File::isDirectory);
        ArrayList<File> files = new ArrayList<>();

        // add all the files from all the dirs to "files" arraylist
        for (int i = 0; i < dirs.length; i++) {
            File[] subfiles = dirs[i].listFiles(File::isFile);
            for (int j = 0; j < subfiles.length; j++)
                files.add(subfiles[j]);
        }

        //
        for (int i = 0; i < files.size(); i++) {
            try {
                byte[] lines = Files.readAllBytes(files.get(i).toPath());
                String text = new String(lines);

                String[] res = text.split("<DOC>");
                for (int j = 0; j < res.length; j++) {
                    if (!res[j].contains("<TEXT>"))
                        continue; //if no <TEXT> tag - ignore
                    docsBuffer.add(res[j]);
                }

                if ((i + 1) % 10 == 0 || i == files.size() - 1) {
                    clearDocsBuffer();
                }

            } catch (IOException ioException) {
                System.out.println("Exception thrown in readFile start function!");
            }
        }
    }
    private static void clearDocsBuffer() {
        if (docsBuffer.isEmpty()) return;

        //Parse.parse();

        docsBuffer.clear();
    }
    public static void setCorpusPath(String f){
        path = new String(f);
    }

    public static void setPostingsPath(String f){
        postingsPath = new String(f);
    }
}