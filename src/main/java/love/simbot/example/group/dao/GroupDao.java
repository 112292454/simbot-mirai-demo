package love.simbot.example.group.dao;

import love.simbot.example.group.component.GroupCode;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
public interface GroupDao {
	@Select("select * from groupcode where id=#{id}")
	GroupCode getGroupCodeById(Integer id);

	@Select("select * from groupcode where name=#{name}")
	GroupCode getGroupCodeByName(String name);

	@Select("select * from groupcode ")
	List<GroupCode> getAllGroupCode();

	@Insert("insert into groupcode(name,code) values (#{name}, #{code})")
	int addGroupCode(GroupCode groupCode);

	@Delete("delete from groupcode where id=#{id}")
	int deleteGroupCodeById(int id);

	@Update("update groupcode set name=#{name},code=#{code}  where id=#{id}")
	int updateGroupCode(GroupCode groupCode);

}
