import java.util.Arrays;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

public class PythonBuilder {
    ArrayList<String> sourceList;   // list of lines
    ArrayList<Integer> indentList;  // list of line indents

    String current;                 // current line

    private int cursor = 0;         // line location where cursor is at
    private int indentFactor = 0;   // factor in which to indent a line
    private int numLines = 0;       // number of lines currently committed
                                    // to the file

    private int lineUID = 0;        // UID for line position
    HashMap<Integer, Integer> lineTabs;     // tabulations for line positioning
    HashMap<Integer, Integer> lineMap;      // maps line in source-file to logical line

    PythonBuilder() {
        sourceList = new ArrayList<String>();
        indentList = new ArrayList<Integer>();
        lineTabs = new HashMap<Integer, Integer>();
        lineMap = new HashMap<Integer, Integer>();

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

    void addSourceLine(int lineNumber) {
        int tempCursor = cursor;
        for (; lineNumber > 0 && !lineMap.containsKey(lineNumber); lineNumber--) {
            lineMap.put(lineNumber, (tempCursor > -1) ? tempCursor : 0);
            tempCursor -= 1;
        }
    }

    int mapLine(int lineNumber) {
        if (lineMap.containsKey(lineNumber)) return lineMap.get(lineNumber);
        else return -1;
    }

    // append to current line
    void append(String app) {
        sourceList.set(cursor, sourceList.get(cursor)+app);
    }

    void addLine(String line, int location) {
        sourceList.add(location, line);
        indentList.add(location, (location-1>-1) ? indentList.get(location-1) : 0);

        for (int i : lineTabs.keySet()) {
            if (lineTabs.get(i) >= location){
                lineTabs.put(i, lineTabs.get(i)+1);
            }
        }

        for (int i : lineMap.keySet()) {
            if (lineMap.get(i) >= location){
                lineMap.put(i, lineMap.get(i)+1);
            }
        }

        numLines++;
    }

    void addLine(String line) {
        addLine(line, ++cursor);
    }

    // adds an empty line
    void addLine() {
        addLine("");
    }

    void newLine() {
        addLine();
    }

    void newLine(int repeat) {
        for (int i = 0; i < repeat; i++) newLine();
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

    void backspace(int bspaces) {
        subLine(0, -1 * bspaces);
    }

    void backspace() {
        backspace(1);
    }

    void addLines(List<String> lines) {
        for (String s : lines) {
            addLine(s);
        }
    }

    void addLines(String[] lines) {
        addLines(Arrays.asList(lines));
    }

    void addLines(PythonBuilder pBuilder, boolean force) {
        if (force || !pBuilder.isEmpty()) addLines(pBuilder.getLines());
    }

    // add lines of another PythonBuilder object
    void addLines(PythonBuilder pBuilder) {
        addLines(pBuilder, false);
    }

    String getLine() {
        return sourceList.get(cursor);
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