package com.skillbox.cryptobot.bot.command;

import com.skillbox.cryptobot.repository.SubscribersRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Service
@Slf4j
@AllArgsConstructor
public class GetSubscriptionCommand implements IBotCommand {

    private final SubscribersRepository subscribersRepository;

    @Override
    public String getCommandIdentifier() {
        return "get_subscription";
    }

    @Override
    public String getDescription() {
        return "Возвращает текущую подписку";
    }

    @Override
    public void processMessage(AbsSender absSender, Message message, String[] arguments) {

        getSubscription(absSender, message);
    }


    public void getSubscription(AbsSender absSender, Message message) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId());

        var subscription = subscribersRepository.findByUserId(message.getFrom().getId());
        try {
            if (subscription.getPrice() != null) {
                sendMessage.setText("Вы подписаны на стоимость биткоина " + subscription.getPrice() + " USD");
                absSender.execute(sendMessage);
            }else{
                sendMessage.setText("У Вас нет активных подписок.");
                absSender.execute(sendMessage);
            }
        }
        catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}