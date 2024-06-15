import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.json.JSONObject;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class Bot extends TelegramLongPollingBot {

    @Override
    public String getBotUsername() {
        return "@ParvumBot";
    }

    @Override
    public String getBotToken() {
        return "7304795454:AAGW8R4Vx9UjDKEiudakT9TS_4yZdFZ0kGA";
    }

    List<Subscriptions> subs = new ArrayList<>();

    @Override
    public void onUpdateReceived(Update update) {

        StandardServiceRegistry standardServiceRegistry = new StandardServiceRegistryBuilder()
                .configure("hibernate.cfg.xml").build();
        Metadata metadata = new MetadataSources(standardServiceRegistry)
                .getMetadataBuilder()
                .build();
        SessionFactory sessionFactory = metadata.getSessionFactoryBuilder()
                .build();
        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();

        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<Subscriptions> courseCriteriaQuery = criteriaBuilder.createQuery(Subscriptions.class);
        Root<Subscriptions> root = courseCriteriaQuery.from(Subscriptions.class);
        List<Subscriptions> baselist = session.createQuery(courseCriteriaQuery).getResultList();
        subs = baselist;

        if (update.hasMessage() && update.getMessage().hasText()) {
            Message message = update.getMessage();
            String chatId = message.getChatId().toString();

            String response = "";

            if (message.getText().contains("/subscribe") && !message.getText().equals("/subscribe")){
                String argument = message.getText().substring(11);

                Subscriptions sub = new Subscriptions(argument, chatId);
                subs.add(sub);
                session.save(sub);
                response = "Вы подписались на цену в " + argument + " USD";

            } else if (message.getText().equals("/get_subscriptions")){
                response = "Список ваших подписок:\n";
                for(Subscriptions sub :  subs){
                    if (Objects.equals(sub.getUser(), chatId)){
                    response += sub.getPrice() + " USD\n";
                    }
                }
            }else if (message.getText().contains("/unsubscribe") && !message.getText().equals("/unsubscribe")){
                String argument = message.getText().substring(13);

                Iterator<Subscriptions> iterator = subs.iterator(); // Сомнительно, но спустя часа жеского кодирования, точно работает
                while (iterator.hasNext()) {
                    Subscriptions sub = iterator.next();
                    if (sub.getPrice().equals(argument) && sub.getUser().equals(chatId)) {
                        iterator.remove();
                        response = "Подписка на цену " + argument + " USD удалена.";
                        session.remove(sub);
                    }
                }

            } else if (message.getText().equals("/get_price")){
                String url = "https://api.binance.com/api/v3/avgPrice?symbol=BTCUSDT";
                try {
                    URL obj = new URL(url);
                    HttpURLConnection con = (HttpURLConnection) obj.openConnection();

                    con.setRequestMethod("GET");

                    int responseCode = con.getResponseCode();
                    System.out.println("Response Code : " + responseCode);

                    BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    String inputLine;
                    StringBuilder response2 = new StringBuilder();

                    while ((inputLine = in.readLine()) != null) {
                        response2.append(inputLine);
                    }
                    in.close();

                    JSONObject myResponse = new JSONObject(response2.toString());
                    String price = myResponse.getString("price");

                    response = "Текущая цена биткоина: " + price + " USD";

                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                response = "Неизвестная команда.";
            }

            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(chatId);
            sendMessage.setText(response);

            try {
                execute(sendMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
        transaction.commit();
        sessionFactory.close();
    }
}