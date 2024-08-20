package com.nowcoder.community.dao;


import com.nowcoder.community.entity.LoginTicket;
import org.apache.ibatis.annotations.*;

@Mapper
public interface LoginTicketMapper {

    //Ticket作为核心数据
    //sql的每句话的后面加上空格
    @Insert({"insert into login_ticket(user_id,ticket,status,expired) ",
            "values(#{userId},#{ticket},#{status},#{expired})"
            })
    @Options(useGeneratedKeys = true,keyProperty = "id")
    int insertLoginTicket(LoginTicket loginTicket);
    @Select({
            "select  id,user_id,ticket,status,expired ",
            "from login_ticket where ticket=#{ticket}"
    })
    LoginTicket selectByTicket(String ticket);
//下面演示如何写动态的SQL
    //要写if前面必须要套着一个script
    @Update({
    "<script>",
    "update login_ticket set status=#{status} where ticket=#{ticket} ",
    "<if test=\"ticket!=null\"> " ,//\"表示的含义是转义为”
    "and 1=1 ",
    "</if>",
    "</script>"
    })
    int updateStatus(@Param("ticket") String ticket,@Param("status") int status);

}
