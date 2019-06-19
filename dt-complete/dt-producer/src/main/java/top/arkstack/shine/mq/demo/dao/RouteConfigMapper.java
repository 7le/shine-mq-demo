package top.arkstack.shine.mq.demo.dao;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;
import top.arkstack.shine.mq.demo.dao.model.RouteConfig;

/**
 * @author 7le
 * @version 1.0.0
 */
@Mapper
public interface RouteConfigMapper {

    int deleteByPrimaryKey(Long id);

    int insert(RouteConfig record);

    int insertSelective(RouteConfig record);

    RouteConfig selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(RouteConfig record);

    int updateByPrimaryKey(RouteConfig record);
}