import java.io.File;
import java.io.FilenameFilter;
import java.io.BufferedReader;
import java.io.BufferedWriter;

import java.nio.file.Files;
import java.nio.file.Path;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.PriorityQueue;

public class CsvUtils
{
    private static final int NUM_ROW_LIMIT = 50;
    private static final FilenameFilter FILTER = (directory, name) -> name.startsWith("temp_");

    public static void sortCsv(Path fromPath, Path toPath, String columnName) throws Exception
    {
        Path[] paths = splitSort(fromPath, columnName);
        mergeList(paths, columnName, toPath);

        File directory = fromPath.getParent().toFile();
        for (File file : directory.listFiles(FILTER))
        {
            file.delete();
        }
    }

    private static Path[] splitSort(Path fromPath, String columnName) throws Exception
    {
        ArrayList<Path> pathList = new ArrayList<Path>();
        try (BufferedReader br = Files.newBufferedReader(fromPath))
        {
            CsvFormatter formatter = new CsvFormatter(br);
            PriorityQueue<String[]> pQueue = new PriorityQueue<String[]>(formatter.new RowComparator(columnName));

            String[] row = formatter.readRow(br);
            while(row != null)
            {
                pQueue.add(row);
                if ((row = formatter.readRow(br)) == null || pQueue.size() == NUM_ROW_LIMIT)
                {
                    Path toPath = fromPath.resolveSibling(String.format("temp_split_%05d.csv", pathList.size()));
                    try (BufferedWriter bw = Files.newBufferedWriter(toPath))
                    {
                        formatter.writeHeader(bw);
                        while (!pQueue.isEmpty())
                        {
                            formatter.writeRow(bw, pQueue.poll());
                        }

                        pathList.add(toPath);
                    }
                }
            }

            return pathList.toArray(new Path[0]);
        }

    }
    private static void mergePair(Path filePath1, Path filePath2, String columnName, Path toPath) throws Exception
    {
        try (BufferedReader br1 = Files.newBufferedReader(filePath1); BufferedReader br2 = Files.newBufferedReader(filePath2); BufferedWriter bw = Files.newBufferedWriter(toPath))
        {
            CsvFormatter formatter1 = new CsvFormatter(br1), formatter2 = new CsvFormatter(br2);
            formatter1.writeHeader(bw);

            String[] row1 = formatter1.readRow(br1), row2 = formatter2.readRow(br2);
            CsvFormatter.RowComparator comparator = formatter1.new RowComparator(columnName);
            while(row1 != null && row2 != null)
            {
                if (comparator.compare(row1, row2) > 0)
                {
                    formatter2.writeRow(bw, row2);
                    row2 = formatter2.readRow(br2);
                }
                else
                {
                    formatter1.writeRow(bw, row1);
                    row1 = formatter1.readRow(br1);
                }
            }

            while (row1 != null)
            {
                formatter1.writeRow(bw, row1);
                row1 = formatter1.readRow(br1);
            }

            while (row2 != null)
            {
                formatter2.writeRow(bw, row2);
                row2 = formatter2.readRow(br2);
            }
        }
    }
    private static void mergeList(Path[] paths, String columnName, Path toPath) throws Exception
    {
        if (paths == null || paths.length == 0)
            throw new Exception("Error: path array is null or empty");

        ArrayDeque<Path> pathList = new ArrayDeque<Path>(Arrays.asList(paths));
        while (pathList.size() != 1)
        {
            Path mergePath = toPath.resolveSibling(String.format("temp_merge_%05d.csv", paths.length - pathList.size()));
            mergePair(pathList.poll(), pathList.poll(), columnName, mergePath);
            pathList.add(mergePath);
        }

        Path sortedOutput = pathList.poll();
        Files.move(sortedOutput, toPath);
    }
}