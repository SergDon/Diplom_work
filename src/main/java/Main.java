import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.List;

public class Main {
    public static void main(String[] args) {

        try (ServerSocket serverSocket = new ServerSocket(8989)) {
            System.out.println("Сервер успешно запущен!\nОн работает на порту 8989\n");
            String searchWord = "блокчейн";
            System.out.println("Поисковик ищет слово: " + searchWord + "\n");
            BooleanSearchEngine engine = new BooleanSearchEngine(new File("pdfs"));
            var searchResult = Collections.unmodifiableList(engine.search(searchWord));
            System.out.println(searchResult);
            engine.readUnusefulWords("stop-ru.txt");
            Gson gson = new GsonBuilder().create();

            while (true) {
                try (Socket socket = serverSocket.accept();
                     BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                     PrintWriter out = new PrintWriter(socket.getOutputStream())) {

                    String word = in.readLine();
                    List<PageEntry> resp = engine.search(word);
                    out.println(gson.toJson(resp));

                }
            }
        } catch (IOException e) {
            System.out.println("Невозможно, к сожалению, запустить сервер!");
            e.printStackTrace();
        }
    }
}