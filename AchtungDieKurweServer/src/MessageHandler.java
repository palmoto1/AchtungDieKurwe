public class MessageHandler {


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
