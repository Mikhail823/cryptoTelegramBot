package com.skillbox.cryptobot.bot.command;

import com.skillbox.cryptobot.model.Subscribers;
import com.skillbox.cryptobot.repository.SubscribersRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Optional;

/**
 * Обработка команды отмены подписки на курс валюты
 */
@Service
@Slf4j
@AllArgsConstructor
public class UnsubscribeCommand implements IBotCommand {


    private final SubscribersRepository subscribersRepository;

    @Override
    public String getCommandIdentifier() {
        return "unsubscribe";
    }

    @Override
    public String getDescription() {
        return "Отменяет подписку пользователя";
    }

    @Override
    public void processMessage(AbsSender absSender, Message message, String[] arguments) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId());

        long userId = message.getFrom().getId();
        try {

            Subscribers subscriber = subscribersRepository.findByUserId(userId);
            if (subscriber.getPrice() != null) {
                subscriber.setPrice(null);
                subscribersRepository.save(subscriber);
                sendMessage.setText("Подписка отменена");
                sendMessage.setChatId(message.getChatId());
            } else {
                sendMessage.setText("Активные подписки отсутствуют");
                sendMessage.setChatId(message.getChatId());
            }

            absSender.execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}