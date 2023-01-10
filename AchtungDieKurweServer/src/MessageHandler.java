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
    /*public String createMessage(String type, String content, String userName) {
        return type + "," + content + "," + userName;
    }

    //move to messagehandler
    public String createMessage(String type, String userName) {
        return createMessage(type, null, userName);
    }

    public String createMessage(String type) {
        return createMessage(type, null);
    }*/
}
