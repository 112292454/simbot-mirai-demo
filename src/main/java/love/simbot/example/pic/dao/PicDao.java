package love.simbot.example.pic.dao;

import love.simbot.example.group.component.GroupCode;
import love.simbot.example.pic.component.PicPath;
import org.apache.ibatis.annotations.*;

import java.util.ArrayList;
import java.util.List;

@Mapper
public interface PicDao {
	@Select("SELECT * FROM `picpath` WHERE id >= (SELECT floor(RAND() * (SELECT MAX(id) FROM `picpath`))) AND  kind=#{kind} LIMIT #{num}")
	List<PicPath> getPathsByKind (@Param("num") Integer num,@Param("kind") String kind);

}
