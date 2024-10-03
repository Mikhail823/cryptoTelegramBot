package com.skillbox.cryptobot.service;

import com.skillbox.cryptobot.bot.CryptoBot;
import com.skillbox.cryptobot.client.BinanceClient;
import com.skillbox.cryptobot.dto.PriceDto;
import com.skillbox.cryptobot.model.Subscribers;
import com.skillbox.cryptobot.repository.SubscribersRepository;
import com.skillbox.cryptobot.utils.TextUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Locale;

import static com.skillbox.cryptobot.utils.ContentMessage.MSG_TEXT_BUY;

@Component
@EnableAsync
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "scheduler.enabled", matchIfMissing = true)
public class MailingService {

    private final CryptoBot cryptoBot;
    private final SubscribersRepository subscribersRepository;
    private final BinanceClient binanceClient;
    private PriceDto total = new PriceDto();

    @Scheduled(fixedRate = 120000)
    public void checkingСostBitcoin(){
        try {
            total.setPrice(binanceClient.getBitcoinPrice());
            log.info("Price bitcoin: " + TextUtil.toString(total.getPrice()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Scheduled(fixedRate = 4000)
    @Async
    public void sendingNotificationsSubscribers(){
        log.info("Рассылка уведомлений");
        List<Subscribers> subscribers = subscribersRepository.findAll();
        SendMessage sendMessage = new SendMessage();
        try {
            subscribers.stream().filter(s -> s.getPrice() <= total.getPrice()).forEach(sub -> {
                sendMessage.setText(MSG_TEXT_BUY + TextUtil.toString(total.getPrice()) + " USD");
                sendMessage.setChatId(sub.getChatId());
                try {
                    cryptoBot.execute(sendMessage);
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (NullPointerException e) {
            log.info("The user does not have a bitcoin subscription");
        }
    }
}
