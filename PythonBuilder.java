import java.util.ArrayList;
import java.util.HashMap;

public class PythonBuilder {
    ArrayList<String> sourceList;   // list of lines
    ArrayList<Integer> indentList;      // list of line indents

    String current;                 // current line

    private int cursor = 0;         // line location where cursor is at
    private int indentFactor = 0;   // factor in which to indent a line
    private int numLines = 0;       // number of lines currently committed
                                    // to the file

    private int lineUID = 0;        // UID for line position
    HashMap<Integer, Integer> lineTabs;     // tabulations for line positioning

    PythonBuilder() {
        sourceList = new ArrayList<String>();
        indentList = new ArrayList<Integer>();
        lineTabs = new HashMap<Integer, Integer>();

        sourceList.add("");
        indentList.add(0);
        current = "";
    }

    int getCursor() {
        return cursor;
    }

    void setCursor(int cursor) {
        this.cursor = cursor;
    }

    // tabulate line number
    //   returns index to lineTab where lineNumber is stored
    int tabLine(int lineNumber) {
        lineTabs.put(lineUID, lineNumber);
        return lineUID++;
    }

    // tabulates current line
    int tabLine() {
        return tabLine(cursor);
    }

    // get line tabulator
    int getLineTab(int tabIndex) {
        return lineTabs.get(tabIndex);
    }

    // destroys line tabulator
    //   using this is good practice after tabulator is no longer needed
    void destroyLineTab(int tabIndex) {
        lineTabs.remove(tabIndex);
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

    void increaseIndent(int indentIndex) {
        indentList.set(indentIndex, indentList.get(indentIndex) + 1);
    }

    void decreaseIndent(int indentIndex) {
        indentList.set(indentIndex, indentList.get(indentIndex) - 1);
    }

    void increaseIndent() {
        increaseIndent(cursor);
    }

    void decreaseIndent() {
        decreaseIndent(cursor);
    }

    // append to current line
    void append(String app) {
        sourceList.set(cursor, sourceList.get(cursor)+app);
    }

    void addLine(String line) {
        sourceList.add(++cursor, line);
        indentList.add(cursor, indentList.get(cursor-1));

        for (int i : lineTabs.keySet()) {
            if (lineTabs.get(i) >= cursor){
                lineTabs.put(i, lineTabs.get(i)+1);
            }
        }

        numLines++;
    }

    // adds an empty line
    void addLine() {
        addLine("");
    }

    void newLine() {
        addLine();
    }

    // add contents of current line to source
    void addCurrent() {
        addLine(current);
        current = "";
    }

    void subLine(int cursor, int beg, int end) {
        String line = sourceList.get(cursor);
        if (end < 0) end = line.length() + end;
        sourceList.set(cursor, line.substring(beg, end));
    }

    void subLine(int beg, int end) {
        subLine(cursor, beg, end);
    }

    void backspace() {
        subLine(0, -1);
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

    String getLine(int lineIndex) {
        return sourceList.get(lineIndex);
    }

    String getCurrent() {
        return getLine(cursor);
    }

    String getSource() {
        String source = "";
        String l;

        int indent = 0;
        for (String s : sourceList) {
            l = "";
            for (int i = 0; i < 4*indentList.get(indent); i++) {
                l += ' ';
            }

            source += l + s + '\n';
            indent++;
        }

        //if (current != "") source += current + '\n';

        return source;
    }
}