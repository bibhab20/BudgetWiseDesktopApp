package util;

public class ColoredTextPrinter {

    // ANSI escape codes for text color
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_BLUE = "\u001B[34m";

    public static String colorizeText(String input) {
        String[] words = input.split(" "); // Split the input string into words
        StringBuilder result = new StringBuilder();

        for (String word : words) {
            switch (word.toLowerCase()) {
                case "red":
                    result.append(ANSI_RED).append(word).append(ANSI_RESET).append(" ");
                    break;
                case "green":
                    result.append(ANSI_GREEN).append(word).append(ANSI_RESET).append(" ");
                    break;
                case "yellow":
                    result.append(ANSI_YELLOW).append(word).append(ANSI_RESET).append(" ");
                    break;
                case "blue":
                    result.append(ANSI_BLUE).append(word).append(ANSI_RESET).append(" ");
                    break;
                default:
                    result.append(word).append(" ");
                    break;
            }
        }

        return result.toString();
    }

    public static void main(String[] args) {
        String input = "This is a red green blue yellow example.";
        String coloredText = colorizeText(input);
        System.out.println(coloredText);
    }
}
