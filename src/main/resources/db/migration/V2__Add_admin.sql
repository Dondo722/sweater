insert into usr (id, username, password, active)
    VALUES (0,'admin','123',true);

insert into user_role (user_id, roles)
    VALUES (0,'USER'),
           (0,'ADMIN');