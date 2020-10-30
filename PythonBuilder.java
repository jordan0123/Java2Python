import java.util.ArrayList;

public class PythonBuilder {
    ArrayList<String> sourceList;
    String current; // current line

    private boolean userSetCursor = false;
    
    private int cursor = 0;
    private int indentFactor = 0;
    private int numLines = 0;

    PythonBuilder() {
        sourceList = new ArrayList<String>();
        current = "";
    }

    boolean isEmpty() {
        if (sourceList.size() != 0) {
            boolean allStringsEmpty = true;

            for (String s : sourceList) {
                allStringsEmpty = s.length() == 0;
                if (!allStringsEmpty) break;
            }

            return allStringsEmpty;
        } else return true;
    }

    void increaseIndent() {
        indentFactor += 1;
    }

    void decreaseIndent() {
        indentFactor -= 1;
    }

    // append to current line
    void append(String app) {
        current += app;
    }

    void addLine(String line) {
        String l = "";
        //System.out.println(line);

        for (int i = 0; i < 4*indentFactor; i++) {
            l += ' ';
        }

        sourceList.add(l + line);
        numLines++;
    }

    // add contents of current line to source
    void addCurrent() {
        addLine(current);
        current = "";
    }

    void addLines(ArrayList<String> lines) {
        for (String s : lines) {
            addLine(s);
        }
    }

    void addLines(PythonBuilder pBuilder, boolean force) {
        if (force || !pBuilder.isEmpty()) addLines(pBuilder.getLines());
    }

    // add lines of another PythonBuilder object
    void addLines(PythonBuilder pBuilder) {
        addLines(pBuilder, false);
    }

    ArrayList<String> getLines() {
        return sourceList;
    }

    void clearCurrent() {
        current = "";
    }

    String getCurrent() {
        return current;
    }

    String getSource() {
        String source = "";

        for (String s : sourceList) {
            source += s + '\n';
        }

        //if (current != "") source += current + '\n';

        return source;
    }
}