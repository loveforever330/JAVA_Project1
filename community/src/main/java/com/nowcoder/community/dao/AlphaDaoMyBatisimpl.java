package com.nowcoder.community.dao;


import org.springframework.stereotype.Repository;

@Repository("MyBatis")
public class AlphaDaoMyBatisimpl implements AlphaDao{

    @Override
    public String select() {
        return "Great";
    }
}
