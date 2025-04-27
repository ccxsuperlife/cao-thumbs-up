create database if not exists `thumbs`;

use `thumbs`;
-- 用户表
create table if not exists `user`
(
    `id`       bigint       not null auto_increment comment '用户id',
    `username` varchar(255) not null comment '用户昵称',
    primary key (`id`)
) engine = innodb
  default charset = utf8mb4;

-- 博客表
create table if not exists `blog`
(
    `id`         bigint        not null auto_increment comment '博客id',
    `title`      varchar(512)  not null comment '博客标题',
    `coverImg`   varchar(1024) not null comment '博客封面',
    `content`    text          not null comment '博客内容',
    `thumbCount` int      default 0 comment '点赞数',
    `user_id`    bigint        not null comment '用户id',
    `createTime` datetime default current_timestamp comment '创建时间',
    `updateTime` datetime default current_timestamp on update current_timestamp comment '更新时间',
    primary key (`id`)
) engine = innodb
  default charset = utf8mb4;

create index `idx_userId` on `blog` (`user_id`);

-- 点赞表
create table if not exists `thumb`
(
    `id`         bigint not null auto_increment comment '点赞id',
    `blog_id`    bigint not null comment '博客id',
    `user_id`    bigint not null comment '用户id',
    `createTime` datetime default current_timestamp comment '创建时间',
    primary key (`id`)
) engine = innodb
  default charset = utf8mb4;

create index `idx_userId_blogId` on `thumb` (user_id, `blog_id`);