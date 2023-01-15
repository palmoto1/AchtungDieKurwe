public class MessageHandler {


    /**
     * Creates a message in the format <string1>,<string2>,<string2> ...etc.
     * Allowing messages to be sent in the same format.
     * @param strings
     * @return a formatted message
     */
    public String createMessage(String... strings){
        StringBuilder sb = new StringBuilder();

        int i = 0;
        while (i < strings.length-1){
            sb.append(strings[i]).append(",");
            i++;
        }
        sb.append(strings[strings.length-1]);

        return sb.toString();
    }
}
