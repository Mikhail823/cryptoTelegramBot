package com.skillbox.cryptobot.bot.command;

import com.skillbox.cryptobot.repository.SubscribersRepository;
import com.skillbox.cryptobot.service.CryptoCurrencyService;
import com.skillbox.cryptobot.utils.TextUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;

/**
 * Обработка команды подписки на курс валюты
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SubscribeCommand implements IBotCommand {

    private final SubscribersRepository subscribersRepository;
    private final CryptoCurrencyService service;

    @Override
    public String getCommandIdentifier() {
        return "subscribe";
    }

    @Override
    public String getDescription() {
        return "Подписывает пользователя на стоимость биткоина";
    }

    @Override
    public void processMessage(AbsSender absSender, Message message, String[] arguments) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId());
        try {
            sendMessage.setText("Текущая цена биткоина " + TextUtil.toString(service.getBitcoinPrice()) + " USD");
            absSender.execute(sendMessage);
            sendMessage.setText("Новая подписка создана на стоимость " + splitText(message) + " USD");
            absSender.execute(sendMessage);
            savePriceUser(message);
        } catch (TelegramApiException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void savePriceUser(Message message){
        Long userId = message.getFrom().getId();
        var subscriber = subscribersRepository.findByUserId(userId);
        subscriber.setPrice(splitText(message));
        subscribersRepository.save(subscriber);
    }

    public Double splitText(Message message){
        String[] price = message.getText().toString().split(" ");
        return Double.parseDouble(price[1]);
    }
}