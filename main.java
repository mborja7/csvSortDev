import java.nio.file.Path;
import java.nio.file.Paths;

public class main
{
    public static void main(String[] args) throws Exception
    {
        Path fromPath = Paths.get(args[0]), toPath = Paths.get(args[1]);
        String columnName = args[2];

        CsvUtils.sortCsv(fromPath, toPath, columnName);
    }
}

