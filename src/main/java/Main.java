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
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class Main {
    public static void main(String[] args) {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(new Bot());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                Bot bot = new Bot();

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
                for (Subscriptions sub : baselist) {
                    double subPriceDouble = Double.parseDouble(sub.getPrice());
                    double bitPriceDouble = Double.parseDouble(BitPrice.getPrice());

                    int subPrice = (int) Math.round(subPriceDouble);
                    int bitPrice = (int) Math.round(bitPriceDouble);
                    System.out.println(subPrice + " " + bitPrice);

                    if (subPrice >= bitPrice) {
                        bot.sendMessage(sub.getUser(), "Внимание! Цена биткоина " + BitPrice.getPrice());
                    }

                }

                transaction.commit();
                sessionFactory.close();

            }
        };
        timer.scheduleAtFixedRate(task, 0, 120000);

    }
}
