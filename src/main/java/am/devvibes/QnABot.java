package am.devvibes;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;

public class QnABot extends TelegramLongPollingBot {
  private final Map<String, String> questionLabels = Map.of(
    "q1", "Էկրանի փոխարինում",
    "q2", "Ետնապակու փոխարինում",
    "q3", "Դիմապակու փոխարինում",
    "q4", "Կորպուսի փոխարինում",
    "q5", "Կամերայի փոխարինում",
    "q6", "Կամերայի ապակու փոխարինում"
  );


  private final Map<String, Map<String, Integer>> prices = Map.of(
    "q1", Map.of("iPhone 11", 35000, "iPhone 12", 40000, "iPhone 13", 45000),
    "q2", Map.of("iPhone 11", 20000, "iPhone 12", 25000, "iPhone 13", 30000),
    "q3", Map.of("iPhone 11", 10000, "iPhone 12", 12000, "iPhone 13", 15000),
    "q4", Map.of("iPhone 11", 25000, "iPhone 12", 30000, "iPhone 13", 35000),
    "q5", Map.of("iPhone 11", 15000, "iPhone 12", 18000, "iPhone 13", 20000),
    "q6", Map.of("iPhone 11", 7000, "iPhone 12", 9000, "iPhone 13", 10000)
  );

  private final Map<Long, String> userSelectedQuestion = new HashMap<>();

  @Override
  public String getBotUsername() {
    return System.getenv("TELEGRAM_BOT_USER_NAME");
  }

  @Override
  public String getBotToken() {
    return System.getenv("TELEGRAM_BOT_TOKEN");
  }

  @Override
  public void onUpdateReceived(Update update) {
    try {
      if (update.hasMessage() && update.getMessage().getText().equals("/start")) {
        sendQuestionMenu(update.getMessage().getChatId());
      } else if (update.hasCallbackQuery()) {
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        String data = update.getCallbackQuery().getData();

        if (data.startsWith("q")) {
          userSelectedQuestion.put(chatId, data);
          sendModelMenu(chatId);
        } else if (data.startsWith("model:")) {
          String model = data.replace("model:", "");
          String questionId = userSelectedQuestion.get(chatId);
          sendFinalAnswer(chatId, questionId, model);
          sendQuestionMenu(chatId); // Show menu again after answering
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void sendQuestionMenu(long chatId) throws TelegramApiException {
    SendMessage message = new SendMessage();
    message.setChatId(chatId);
    message.setText("📋 Ընտրեք հարցը");
    message.setParseMode("HTML");

    List<List<InlineKeyboardButton>> rows = new ArrayList<>();
    for (Map.Entry<String, String> entry : questionLabels.entrySet()) {
      rows.add(List.of(InlineKeyboardButton.builder()
        .text(entry.getValue())
        .callbackData(entry.getKey())
        .build()));
    }

    InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
    markup.setKeyboard(rows);
    message.setReplyMarkup(markup);

    execute(message);
  }

  private void sendModelMenu(long chatId) throws TelegramApiException {
    SendMessage message = new SendMessage();
    message.setChatId(chatId);
    message.setText("📱 Ընտրեք մոդելը");

    List<String> models = List.of("iPhone 11", "iPhone 12", "iPhone 13");

    List<List<InlineKeyboardButton>> rows = new ArrayList<>();
    for (String model : models) {
      rows.add(List.of(InlineKeyboardButton.builder()
        .text(model)
        .callbackData("model:" + model)
        .build()));
    }

    InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
    markup.setKeyboard(rows);
    message.setReplyMarkup(markup);

    execute(message);
  }

  private void sendFinalAnswer(long chatId, String qid, String model) throws TelegramApiException {
    String questionLabel = questionLabels.getOrDefault(qid, "Հարց");
    int price = prices.getOrDefault(qid, Map.of()).getOrDefault(model, 0);

    String answer =
      "📱 <b>" + questionLabel + " " + model + "-ի համար արժե " + price + " ֏</b>\n\n" +
        "<b>Առավելություններ՝</b>\n" +
        "• ջրակայունության ապահովում IP67, որը կատարվում է միայն Արի մոբայլ սերվիսում։\n" +
        "• կարգավորումների բաժնում էկրանի փոխարինման հետ կապված որևէ նշում չի լինում։\n\n" +
        "📍 Ք. Երևան Կոմիտաս 3\n" +
        "🌐 www.arimobile.am\n" +
        "📱 TikTok / Instagram: arimobile.am\n" +
        "📞 +37494457566 | +37493139116";

    SendMessage message = new SendMessage();
    message.setChatId(chatId);
    message.setText(answer);
    message.setParseMode("HTML");

    execute(message);
  }
}