delete from user_role;
delete from usr;

insert into usr (id, active, password, username)
values (1, true, '$2a$08$.f7N0LR8pFG3LYpbB311xueoju5997F3Fpoekq6qKB4iMBA3MdXyO', 'dru'),
       (2, true, '$2a$08$.f7N0LR8pFG3LYpbB311xueoju5997F3Fpoekq6qKB4iMBA3MdXyO', 'dan');

insert into user_role (user_id, roles)
values (1, 'USER'), (1, 'ADMIN'),
       (2, 'USER');