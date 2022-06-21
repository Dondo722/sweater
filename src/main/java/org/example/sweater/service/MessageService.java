package org.example.sweater.service;

import org.example.sweater.domain.User;
import org.example.sweater.domain.dto.MessageDto;
import org.example.sweater.repos.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class MessageService {
    @Autowired
    private MessageRepository messageRepository;

    public Page<MessageDto> messageList(Pageable pageable, String filter, User user) {
        if (filter.isEmpty()) {
            return messageRepository.findAll(pageable, user);
        } else {
            return messageRepository.findByTag(filter, pageable, user);
        }
    }

    public Page<MessageDto> messageListForUser(Pageable pageable, User author, User user) {
        return messageRepository.findByUser(pageable, author, user);
    }
}
