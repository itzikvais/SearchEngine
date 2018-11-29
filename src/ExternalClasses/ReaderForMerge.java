package ExternalClasses;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * class that contains BufferReader and save thw line in the main memory for merging the posting files.
 */
public class ReaderForMerge {
    private BufferedReader br;

    public String line;
    public String val;
    public String key;
    private String filePath;


    public ReaderForMerge(BufferedReader r, String filePath) throws IOException {
        this.br = r;
        this.filePath = filePath;
        reload();
    }

    public void close() throws IOException {
        this.br.close();
    }


    public void reload() throws IOException {
        this.line = this.br.readLine();
        if (this.line != null) {
            String[] split = this.line.split("#");
            this.key = split[0].trim();
            this.val = split[1].trim();
        }
    }

    public void deleteFile(){
        try {
            Files.deleteIfExists(Paths.get(filePath));
        }
        catch (IOException e){e.printStackTrace();}
    }
}
