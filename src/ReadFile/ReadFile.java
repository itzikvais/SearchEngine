package ReadFile;

import Parse.Parse;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;

public class ReadFile {

    private String corpusPath;
    private String postingDirPath;
    private ArrayList<String[]> docsBuffer; // buffer for one chunk of docs
    private Parse parser;

    public ReadFile(String path, String postingsPath) {
        this.corpusPath = path;
        this.postingDirPath = postingsPath;
        this.parser = new Parse(docsBuffer);
    }

    public void start() {
        File[] dirs = new File(corpusPath).listFiles(File::isDirectory);
        ArrayList<File> files = new ArrayList<>();

        //initial files ArrayList
        for (File dir : dirs) {
            File[] subFiles = dir.listFiles(File::isFile);
            Collections.addAll(files, subFiles);
        }

        //add all the docs from all the files in current chunk to docBuffer
        for (int i = 0; i < files.size(); i++) {
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(files.get(i))));
                String [] toDocBuffer = new String[4];
                toDocBuffer[0] = files.get(i).getAbsolutePath();  // file path

                String line = br.readLine();
                int lineNum = 1;
                StringBuilder doc = new StringBuilder();

                while(!line.contains("<DOC>")){
                    line = br.readLine();
                    lineNum++;
                }
                toDocBuffer[1] = String.valueOf(lineNum); // doc start line
                while(!line.contains("</DOC>")){
                    line = br.readLine();
                    doc.append(line);
                    lineNum++;
                }
                toDocBuffer[2] = String.valueOf(lineNum); // doc end line
                toDocBuffer[3] = doc.toString(); // doc start line

                docsBuffer.add(toDocBuffer);
                if ((i + 1) % 10 == 0 || i == files.size() - 1) {
                    prosesChunk();
                }

            } catch (IOException ioException) {
                System.out.println("Exception thrown in readFile start function!");
            }
        }
    }
    private void prosesChunk() {
        if (docsBuffer.isEmpty()) return;

//        parser.parse(docsBuffer);

        docsBuffer.clear();
    }
    public void setCorpusPath(String f){
        corpusPath = f;
    }

    public void setPostingDirPath(String f){
        postingDirPath = f;
    }
}