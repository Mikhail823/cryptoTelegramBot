package com.skillbox.cryptobot.bot;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.extensions.bots.commandbot.TelegramLongPollingCommandBot;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class CryptoBot extends TelegramLongPollingCommandBot {

    private final String botUsername;


    public CryptoBot(
            @Value("${telegram.bot.token}") String botToken,
            @Value("${telegram.bot.username}") String botUsername,
            List<IBotCommand> commandList) {
        super(botToken);
        this.botUsername = botUsername;

        commandList.forEach(this::register);
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public void processNonCommandUpdate(Update update) {
        //ToDo Добавить проверку команд
        checkingCommands(update);
    }

    public void checkingCommands(Update update){
        String text = update.getMessage().getText();
        long chatId = update.getMessage().getChatId();
        String regex = "^/{1}([a-z_]{3,})";

        if (text != null) {
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(text);

            if (!matcher.find()) {
                try {
                    sendMessage(chatId, "Вы ввели не правильную команду");
                    sendMessage(chatId, messageHelp());
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public void sendMessage(Long chatId, String msg) throws TelegramApiException {
        var sm = SendMessage.builder()
                .chatId(chatId)
                .text(msg)
                .build();
        execute(sm);
    }

    public String messageHelp(){
        return """
                Поддерживаемые команды:
                 /subscribe [число] - подписаться на стоимость битковина в USD
                 /get_price - получить стоимость биткоина
                 /get_subscription - получить текущую подписку
                 /unsubscribe - отменить подписку на стоимость
                """;
    }

}
