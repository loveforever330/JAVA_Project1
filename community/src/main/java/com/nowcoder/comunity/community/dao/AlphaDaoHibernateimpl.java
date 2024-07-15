package com.nowcoder.comunity.community.dao;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

@Repository//访问数据库的
@Primary
public class AlphaDaoHibernateimpl implements  AlphaDao {

    @Override
    public String select() {
        return "Hibernate";
    }
}
