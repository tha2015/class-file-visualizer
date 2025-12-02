import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class HelloWorld {
    public static void main(String[] args) {
        List<String> names = Arrays.asList("Alice", "Bob", "Charlie", "David");

        System.out.println("Printing names using forEach:");
        names.stream().forEach(name -> System.out.println(name));
    }
}
