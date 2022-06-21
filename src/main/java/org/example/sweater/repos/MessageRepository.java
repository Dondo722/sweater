package org.example.sweater.repos;

import org.example.sweater.domain.Message;
import org.example.sweater.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface MessageRepository extends CrudRepository<Message,Long> {

    Page<Message> findAll(Pageable pageable);

    Page<Message> findByTag(String tag, Pageable pageable);

    @Query("from Message  m where m.author = :author")
    Page<Message> findByUser(Pageable pageable, @Param("author") User author);
}
