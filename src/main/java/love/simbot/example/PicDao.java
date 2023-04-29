package love.simbot.example;

import love.simbot.example.pic.component.PicPath;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface PicDao {
	@Select("SELECT * FROM `picpath` WHERE id >= (SELECT floor(RAND() * (SELECT MAX(id) FROM `picpath`))) AND  kind=#{kind} LIMIT #{num}")
	List<PicPath> getPathsByKind (@Param("num") Integer num,@Param("kind") String kind);

}
