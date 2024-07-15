package com.nowcoder.comunity.community.dao;


import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

@Repository("MyBatis")
public class AlphaDaoMyBatisimpl implements AlphaDao{

    @Override
    public String select() {
        return "Great";
    }
}
