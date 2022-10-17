import java.io.BufferedReader;
import java.io.BufferedWriter;

import java.util.LinkedHashMap;
import java.util.Comparator;

import java.util.regex.Pattern;

public class CsvFormatter
{
    public class RowComparator implements Comparator<String[]>
    {
        private int comparisonField;

        private RowComparator() throws Exception { throw new Exception("Error: call to RowComparator default constructor"); }
        public RowComparator(String columnName) throws Exception
        {
            Integer fieldNumber = header.get(columnName);
            if (fieldNumber == null)
                throw new Exception("Error: the supplied column name '" + columnName + "' does not match any column in the CSV file");

            comparisonField = fieldNumber;
        }

        @Override
        public int compare(String[] row1, String[] row2) { return row1[comparisonField].compareTo(row2[comparisonField]); }
    }

    private final Pattern headerPattern = Pattern.compile("\\A[-a-zA-Z0-9_ ]+\\z");
    private final Pattern valuePattern = Pattern.compile("\\A[^,\\v]*\\z");

    private LinkedHashMap<String, Integer> header = new LinkedHashMap<String, Integer>();

    public CsvFormatter(String[] headings) throws Exception { setHeadings(headings); }
    public CsvFormatter(BufferedReader br) throws Exception
    {
        if (br == null)
            throw new Exception("Error: cannot read from a null buffered reader");

        String line;
        if ((line = br.readLine()) == null)
            throw new Exception("Error: no further lines in the file");

        String[] headings = line.split(",", -1);
        setHeadings(headings);
    }

    public String[] readRow(BufferedReader br) throws Exception
    {
        if (br == null)
            throw new Exception("Error: cannot read from a null buffered reader");

        String line;
        if ((line = br.readLine()) == null)
            return null;

        String[] row = line.split(",", -1);
        if(row.length != header.size())
            throw new Exception("Error: expected " + header.size() + " fields but got " + row.length);

        return row;
    }

    public void writeHeader(BufferedWriter bw) throws Exception
    {
        if (bw == null)
            throw new Exception("Error: attempted to write to null buffered writer");

        bw.write(String.join(",", header.keySet()));
        bw.newLine();
    }
    public void writeRow(BufferedWriter bw, String[] row) throws Exception
    {
        if (row == null)
            throw new Exception("Error: attempted to write null row");

        if (row.length != header.size())
            throw new Exception("Error: " + header.size() + " fields required but attempted to write " + row.length);

        for (int i = 0; i < row.length; ++i)
        {
            if (!valuePattern.matcher(row[i]).matches())
                throw new Exception("Error: field" + i + " contains invalid characters");
        }

        if (bw == null)
            throw new Exception("Error: attempted to write to null buffered writer");

        bw.write(String.join(",", row));
        bw.newLine();
    }

    private void setHeadings(String[] headings) throws Exception
    {
        for (int i = 0; i < headings.length; ++i)
        {
            if (headings[i] == null || headings[i].length() == 0)
                throw new Exception("Error: column heading is null or empty");

            if (header.containsKey(headings[i]))
                throw new Exception("Error: header contains duplicate column headings");

            if (!headerPattern.matcher(headings[i]).matches())
                throw new Exception("Error: column heading " + i + " contains invalid characters");

            header.put(headings[i], i);
        }
    }
}
